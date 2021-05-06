package src.IndexServer;

import src.Utility.CustomLogger;
import src.Utility.IOStreamManager;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {

    private static final int SERVER_PORT = 9090;
    private static CustomLogger logger = new CustomLogger("Server", "logs");

    public static void main(String[] args) {
        Indexer indexBuilder = new Indexer("assets/stop-words.txt", 4, false, true);
        indexBuilder.buildIndex("data");

        // add ctrl+c terminal interrupt handling
        Runtime.getRuntime().addShutdownHook(new Thread(() -> logger.info("Shutting down the server.")));

        try {
            logger.info("Server is running on port: " + SERVER_PORT);
            ServerSocket server = new ServerSocket(SERVER_PORT);
            int clientId = 0;
            while (true) {
                Socket clientSocket = server.accept();
                int finalClientId = clientId++;
                // create thread to handle client with obtained socket, invertedIndex and clientId
                Thread clientHandler = new Thread(() -> handleClient(clientSocket, indexBuilder, finalClientId));
                clientHandler.start();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static void handleClient(Socket clientSocket, Indexer invertedIndex, int clientId) {
        /**
         * secret hash to send it to the client
         * when the exit hash received, it means that the client finished execution
         */
        String exitHash = generateHash();
        byte[] encodedHash = exitHash.getBytes();
        logger.info("Handling client#" + clientId + " in " + Thread.currentThread().getName());
        try {
            ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());

            IOStreamManager socketManager = new IOStreamManager(ois, oos);

            socketManager.send(encodedHash);

            // receive client query string at first
            byte[] message = socketManager.receive();
            String decodedMessage;

            // continue execution if client still don't want to finish 
            while (!Arrays.equals(message, encodedHash)) {
                decodedMessage = new String(message);
                logger.info("Received '" + decodedMessage + "' from client#" + clientId);

                // obtain a map for each word in the client query string
                Map<String, List<String>> wordsLocations = invertedIndex.locateEach(decodedMessage);
                // send it to the client
                socketManager.sendObject(wordsLocations);
                logger.info("Response sent to client#" + clientId);
                message = socketManager.receive();
            }
            logger.info("Done work with client#" + clientId);
            clientSocket.close();
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
}
