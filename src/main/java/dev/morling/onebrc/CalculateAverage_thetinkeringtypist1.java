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
import java.util.Scanner;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

public class CalculateAverage_thetinkeringtypist1 {

    private static final String FILE = "./measurements.txt";
    private static final TreeMap<String, Measurement> MAP = new TreeMap<>();

    public static void main(String[] args) throws IOException {
        File file = Paths.get(FILE).toFile();

        if (!file.exists()) {
            System.out.println("File doesn't exist");
            System.exit(0);
        }

        Scanner scanner = new Scanner(file);
        while (scanner.hasNext()) {
            CalculateAverage_thetinkeringtypist1.insert(scanner.nextLine());
        }

        StringBuilder builder = new StringBuilder(MAP.toString());
        builder.replace(0, 1, "{");
        builder.replace(builder.length() - 1, builder.length() + 1, "}");

        System.out.println(builder);
    }

    private static class Measurement {

        double[] data;
        long count;

        private Measurement(double measurement) {
            this.count = 1;
            this.data = new double[]{ measurement, measurement, measurement };
        }

        public String toString() {
            return String.format("%.1f/%.1f/%.1f", data[0], data[1], data[2]);
        }

        private void update(double m) {
            // min
            if (m < data[0]) {
                data[0] = m;
            }

            // max
            if (m > data[2]) {
                data[2] = m;
            }

            // mean
            data[1] = data[1] + ((m - data[1]) / ++count);
        }
    }

    private static void insert(String line) {
        String[] split = line.split(";");
        String id = split[0];
        double m = Double.parseDouble(split[1]);

        if (MAP.containsKey(id)) {
            MAP.get(id).update(m);
        }
        else {
            MAP.put(id, new Measurement(m));
        }
    }
}
