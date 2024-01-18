package com.adamcalculator.cheststofox.util;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;

public class Files {
    public static void writeFile(File file, String text) {
        try {
            if (!file.exists()) {
                File dir = new File(getParOfFile(file.getAbsolutePath()));
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                file.createNewFile();
            }
            final FileWriter fileWriter = new FileWriter(file, StandardCharsets.UTF_8, false);
            fileWriter.write(text);
            fileWriter.flush();
            fileWriter.close();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String readFile(File file) {
        try {
            final StringBuilder result = new StringBuilder();
            final FileReader fileReader = new FileReader(file, StandardCharsets.UTF_8);
            final char[] buff = new char[256];
            int i;
            while ((i = fileReader.read(buff)) > 0) {
                result.append(new String(buff, 0, i));
            }
            fileReader.close();
            return result.toString();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String getParOfFile(String pathToChild) {
        pathToChild = pathToChild
                .replace("\\", File.separator)
                .replace("/", File.separator);


        int lastSep = pathToChild.lastIndexOf(File.separator);
        if (lastSep > 0) {
            return pathToChild.substring(0, lastSep) + File.separator;
        }

        throw new UnsupportedOperationException("Parent of this file is root of filesystem.");
    }
}
