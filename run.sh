#!/usr/bin/env bash

set -euo pipefail

# Compile project
./mvnw clean validate package

# Determine target implementation (defaults to highest version if unspecified)
TARGET="${1:-}"

if [[ -z "$TARGET" ]]; then
    LATEST_NUM=$(ls calculate_average_thetinkeringtypist*.sh 2>/dev/null | grep -oE '[0-9]+' | sort -n | tail -n 1 || true)
    if [[ -n "$LATEST_NUM" ]]; then
        TARGET="$LATEST_NUM"
    else
        TARGET="0" # Fallback to baseline if no numbered versions exist
    fi
fi

# Map target: '0' or 'baseline' points to baseline, any other number points to thetinkeringtypist<N>
if [[ "$TARGET" == "0" || "$TARGET" == "baseline" ]]; then
    IMPL_NAME="baseline"
    TARGET_SCRIPT="./calculate_average_baseline.sh"
else
    IMPL_NAME="thetinkeringtypist${TARGET}"
    TARGET_SCRIPT="./calculate_average_${IMPL_NAME}.sh"
fi

if [[ ! -f "$TARGET_SCRIPT" ]]; then
    echo "Error: Target script '${TARGET_SCRIPT}' does not exist." >&2
    exit 1
fi

# Directory for logs
LOG_DIR="log"
mkdir -p "$LOG_DIR"

# Scratch file to capture live stream
TMP_LOG=$(mktemp "${LOG_DIR}/run_temp.XXXXXX")

# Execute using subshell timing and stream output to terminal & temp file
echo "Calculating with ${IMPL_NAME} (${TARGET_SCRIPT})..."
(time "$TARGET_SCRIPT") 2>&1 | tee "$TMP_LOG"

# Extract real time metric from the last lines
real_line=$(tail -n 15 "$TMP_LOG" | grep -Ei 'real' | tail -n 1 || true)


# Parse minutes, seconds, and milliseconds into mm:ss.SSS
if [[ "$real_line" =~ ([0-9]+)m([0-9]+)\.([0-9]+) ]]; then
    # Handles Bash time output (e.g., "real  0m04.321s" or "real 1m4.5s")
    min="${BASH_REMATCH[1]}"
    sec="${BASH_REMATCH[2]}"
    ms_raw="${BASH_REMATCH[3]}"
    ms_padded="${ms_raw}000"
    TIME_FMT=$(printf "%02d:%02d.%s" "$min" "$sec" "${ms_padded:0:3}")

elif [[ "$real_line" =~ ([0-9]+):([0-9]+)\.([0-9]+) ]]; then
    # Handles mm:ss.fraction outputs (e.g., "0:04.32elapsed")
    min="${BASH_REMATCH[1]}"
    sec="${BASH_REMATCH[2]}"
    ms_raw="${BASH_REMATCH[3]}"
    ms_padded="${ms_raw}000"
    TIME_FMT=$(printf "%02d:%02d.%s" "$min" "$sec" "${ms_padded:0:3}")

elif [[ "$real_line" =~ ([0-9]+)\.([0-9]+) ]]; then
    # Handles raw seconds with decimal (e.g., "64.321s")
    total_sec="${BASH_REMATCH[1]}"
    ms_raw="${BASH_REMATCH[2]}"
    ms_padded="${ms_raw}000"
    TIME_FMT=$(printf "%02d:%02d.%s" "$((total_sec / 60))" "$((total_sec % 60))" "${ms_padded:0:3}")

elif [[ "$real_line" =~ ([0-9]+)m([0-9]+) ]]; then
    # Fallback if no fraction was reported
    TIME_FMT=$(printf "%02d:%02d.000" "${BASH_REMATCH[1]}" "${BASH_REMATCH[2]}")

else
    TIME_FMT="00:00.000"
fi

# Destination path: log/<mm:ss>-baseline.log or log/<mm:ss>-thetinkeringtypist<N>.log
LOG_FILE="${LOG_DIR}/${TIME_FMT}-${IMPL_NAME}.log"

# Move the completed log to final destination
mv "$TMP_LOG" "$LOG_FILE"

echo ""
echo "Output saved to: $LOG_FILE"
