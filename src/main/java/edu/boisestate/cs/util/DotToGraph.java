package edu.boisestate.cs.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

public class DotToGraph {
    public static void outputDotFileAndPng(String dot) {
        outputDotFileAndPng(dot, "temp");
    }
    
    public static void outputDotFile(String dot, String filename ){
    	File tempDir = new File("./temp");
        if (!tempDir.exists()) {
            if (!tempDir.mkdir()) {
                return;
            }
        }
        String filePath = String.format("%s/%s", tempDir.getPath(), filename);
        // output dot to file
        try {
            try (PrintWriter writer = new PrintWriter(filePath + ".dot")) {
                writer.println(dot);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        
        File outFile = new File(filePath + ".dot");
    }
    
    public static void outputDotFileAndPng(String dot, String filename) {
        // ensure temp directory exists
        File tempDir = new File("./temp");
        if (!tempDir.exists()) {
            if (!tempDir.mkdir()) {
                return;
            }
        }
        String filePath = String.format("%s/%s", tempDir.getPath(), filename);
        // output dot to file
        try {
            try (PrintWriter writer = new PrintWriter(filePath + ".dot")) {
                writer.println(dot);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // use dot on file to produce png
        Runtime rt = Runtime.getRuntime();

        try {
            Process dotProcess = rt.exec("dot -Tpng " + filePath + ".dot -o " + filePath + ".png");
            dotProcess.waitFor();


            // trim png using imagemagik
            Process convertProcess = rt.exec("convert " + filePath + ".png -trim " + filePath + ".png");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        File outFile = new File(filePath + ".dot");
        outFile.deleteOnExit();
    }
}
