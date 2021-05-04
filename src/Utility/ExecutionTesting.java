package Utility;

import IndexServer.Indexer;

import java.io.*;

public class ExecutionTesting {

    private static final int THREAD_RANGE = 8;
    private static final int THREAD_STEP = 1;
    private static final int[] FILE_RANGE = {2000, 5000, 20000, 50000, 100000};

    public static void main(String[] args) {
        Indexer indexBuilder;

        for (int i = 1; i <= THREAD_RANGE; i += THREAD_STEP) {
            for (int filesAmount : FILE_RANGE) {
                indexBuilder = new Indexer("stop-words.txt", i, false, false);
                String testSetPath = String.format("../test-sets/time-test-set-%s/", filesAmount);
                long startTime = System.nanoTime();
                indexBuilder.buildIndex(testSetPath);
                long endTime = System.nanoTime();
                long execTime = endTime - startTime;
                System.out.println("Finished index building for '" + testSetPath + "' in " + execTime/1000000.0 + " ms");
            }
        }

    }

    private static void renameFiles(String path) {
        File fileIn = new File(path);
        File[] files = fileIn.listFiles();
        for(File file : files) {
            String filePath = path + "/" + file.getName();
            if (file.isDirectory()) {
                renameFiles(filePath);
            } else {
                String pathToDirectory = filePath.substring(0, filePath.lastIndexOf("/"));
                String fileName = file.getName();
                fileName = fileName.substring(0, fileName.indexOf("-"));
                String newFilePath = pathToDirectory + "/" + fileName + "-" + file.getParentFile().getName() + ".txt";
                File renamedFile = new File(newFilePath);

                boolean wasRenamed = file.renameTo(renamedFile);
                if (!wasRenamed) {
                    System.out.printf("%nError while renaming %n %s %n to %n %s%n%n", filePath, newFilePath);
                }
            }
        }
    }

    public static void listDirectory(String path) {
        File fileIn = new File(path);
        File[] files = fileIn.listFiles();
        for(File file : files) {
            String filePath = path + "/" + file.getName();
            if (file.isDirectory()) {
                System.out.println();
                listDirectory(filePath);
            } else {
                System.out.println(file.getName());
            }
        }
    }

}
