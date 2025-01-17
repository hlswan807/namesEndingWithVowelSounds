package org.example;

import java.io.FileWriter;
import java.io.IOException;

public class NameWriter {
    private static FileWriter fl;

    static {
        try {
            fl = new FileWriter("/resources/data/statistics.txt");
        } catch (IOException e) {
            System.out.println("An error reading the file location occurred");
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private static void writeToStatistics(NameReader nameReader) throws IOException {
        fl.write("Hello World!");

    }


}
