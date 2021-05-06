package src.IndexClient;

import src.Utility.CustomLogger;
import src.Utility.IOStreamManager;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Client {

    // stop word received from the client, due to which the program ends execution
    private static final String CLIENT_STOP_WORD = "";
    // log every step to Client.log file
    private static CustomLogger logger = new CustomLogger("Client", "logs");

    public static void main(String[] args) {
        try {
            Socket serverSocket = new Socket("localhost", 9090);

            ObjectOutputStream oos = new ObjectOutputStream(serverSocket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(serverSocket.getInputStream());
            
            // create a manager to communicate with the server using io-streams obtained earlier
            IOStreamManager socketManager = new IOStreamManager(ois, oos);
            
            // receive secret hash to send it to the server later to let him know that client finished execution
            byte[] exitHash = socketManager.receive();

            Scanner terminalScanner = new Scanner(System.in);
            String clientQueryString = "";

            System.out.print("Enter word/combination of words you want to search (Press 'Enter' to leave): ");
            clientQueryString = terminalScanner.nextLine();

            // continue execution if the client didn't enter the stop word
            while (!clientQueryString.equals(CLIENT_STOP_WORD)) {
                logger.info("Input is: '" + clientQueryString + "'.");
                // send query to the server
                socketManager.send(clientQueryString.getBytes());
                logger.info("Message '" + clientQueryString + "' was sent.");

                // receive answer from the server
                Map<String, List<String>> wordsLocations = (Map<String, List<String>>) socketManager.receiveObject();

                logger.info("Received response from the server.");
                if (wordsLocations.isEmpty()) {
                    logger.info("None of the words entered is present in any of the documents.");
                } else {
                    // for each word print all locations that it is present in
                    wordsLocations.forEach((word, locationsList) -> {
                        if (!locationsList.isEmpty()) {
                            logger.info("'" + word + "' is present in: ");
                            for (String location : locationsList) {
                                logger.info("\t'" + location + "'");
                            }
                        } else {
                            logger.info("'" + word + "' is not present in any of the documents.");
                        }
                    });
                }
                logger.info("");
                System.out.print("Enter word/combination of words you want to search (Press 'Enter' to leave): ");
                clientQueryString = terminalScanner.nextLine();
            }
            logger.info("Shutting down.");
            // let server know that the client finished executing
            socketManager.send(exitHash);
            terminalScanner.close();
            serverSocket.close();
        } catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }

    }
}
