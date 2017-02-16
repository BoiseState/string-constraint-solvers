package edu.boisestate.cs.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

public class DotToGraph {
    public static void outputDotFileAndPng(String dot) {
        outputDotFileAndPng(dot, "temp");
    }
    public static void outputDotFileAndPng(String dot, String filename) {
        // output dot to file
        try {
            try (PrintWriter writer = new PrintWriter(filename + ".dot")) {
                writer.println(dot);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // use dot on file to produce png
        Runtime rt = Runtime.getRuntime();

        try {
            rt.exec("dot -Tpng " + filename + ".dot -o " + filename + ".png");

            // trim png using imagemagik
            rt.exec("convert " + filename + ".png -trim " + filename + ".png");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
