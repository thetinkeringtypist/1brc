/*
 *  Copyright 2023 The original authors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package dev.morling.onebrc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class CalculateAverage_thetinkeringtypist3 {

    private static final String FILE = "./measurements.txt";
    private static final Map<String, Measurement> MAP = new ConcurrentHashMap<>(512);

    public static void main(String[] args) throws IOException {
        File file = Paths.get(FILE).toFile();

        if (!file.exists()) {
            System.out.println("File doesn't exist");
            System.exit(0);
        }

        // 1/2 GiB buffer for fewer syscalls to fetch data from disk
        // 1/4 GiB buffer is roughly the same performance for less memory usage
        BufferedReader reader = new BufferedReader(new FileReader(file), 268_435_456);
        reader.lines().parallel().forEach(CalculateAverage_thetinkeringtypist3::insert);
        reader.close();

        // Sort keys into
        TreeMap<String, Measurement> treeMap = new TreeMap<>(MAP);

        StringBuilder builder = new StringBuilder(treeMap.toString());
        builder.replace(0, 1, "{");
        builder.replace(builder.length() - 1, builder.length() + 1, "}");

        System.out.println(builder);
    }

    private static class Measurement {
        double min, mean, max;
        int count;

        private Measurement(double measurement) {
            this.count = 1;
            this.min = measurement;
            this.mean = measurement;
            this.max = measurement;
        }

        public String toString() {
            return String.format("%.1f/%.1f/%.1f", min, mean, max);
        }

        private void update(double m) {
            // cal min
            if (m < min) {
                min = m;
            }

            // calc max
            if (m > max) {
                max = m;
            }

            // calc running mean
            mean = mean + ((m - mean) / ++count);
        }
    }

    private static void insert(String line) {
        int delimiterIndex = line.indexOf(';');
        String id = line.substring(0, delimiterIndex);
        int eolIndex = line.length();
        double m = Double.parseDouble(line.substring(delimiterIndex + 1, eolIndex));

        if (MAP.containsKey(id)) {
            MAP.get(id).update(m);
        }
        else {
            MAP.put(id, new Measurement(m));
        }
    }
}
