package src.Utility;

import src.IndexServer.Indexer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

public class ExecutionTesting {

    private static final int SERVER_PORT = 9090;
    private static final boolean USE_SOCKETS = true;

    private static final int THREAD_RANGE = 16;
    private static final int[] FILE_RANGE = {2000, 5000, 20000, 50000, 100000};

    public static void main(String[] args) {
        try {
            ServerSocket server ;
            Socket clientSocket;

            DataOutputStream oos;
            DataInputStream ois;
            if (USE_SOCKETS) {
                server = new ServerSocket(SERVER_PORT);
                clientSocket = server.accept();

                oos = new DataOutputStream(clientSocket.getOutputStream());
                ois = new DataInputStream(clientSocket.getInputStream());

                String exitHash = generateHash();
                byte[] encodedHash = exitHash.getBytes();
                oos.write(encodedHash);
                oos.flush();
                ois.readUTF();

                for (int filesAmount: FILE_RANGE) {
                    oos.write(String.valueOf(filesAmount).getBytes());
                    oos.flush();
                    ois.readUTF();
                }
                oos.write(encodedHash);
                oos.flush();
                ois.readUTF();

                oos.write(String.valueOf(THREAD_RANGE).getBytes());
                oos.flush();
                ois.readUTF();
            }

            Indexer indexBuilder;

            System.out.printf("%3s ", "");
            for (int filesAmount : FILE_RANGE) {
                System.out.printf("%8s ", filesAmount);
            }
            System.out.println();

            for (int threadsAmount = 1; threadsAmount <= THREAD_RANGE; threadsAmount++) {
                System.out.printf("%3s ", threadsAmount);

                if (USE_SOCKETS) {
                    oos.write(String.valueOf(threadsAmount).getBytes());
                    oos.flush();
                    ois.readUTF();
                }

                for (int filesAmount : FILE_RANGE) {
                    indexBuilder = new Indexer("assets/stop-words.txt", threadsAmount, false, false);
                    String testSetPath = String.format("../test-sets/time-test-set-%s/", filesAmount);
                    long startTime = System.nanoTime();
                    indexBuilder.buildIndex(testSetPath);
                    long endTime = System.nanoTime();
                    long execTime = endTime - startTime;
                    System.out.printf("%8.2f ", execTime/1000000.0);

                    if (USE_SOCKETS) {
                        oos.write(String.valueOf(execTime).getBytes());
                        oos.flush();
                        ois.readUTF();
                    }
                }
                System.out.println();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static String generateHash() {
        Random random = new Random();
        String bigLetter = Character.toString(random.nextInt(26) + 65);
        String smallLetter = Character.toString(random.nextInt(26) + 97);
        String hash = random.nextInt(20) + bigLetter + random.nextInt(20) + smallLetter;
        return hash;
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
