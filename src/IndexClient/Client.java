package IndexClient;

import Utility.CustomLogger;
import Utility.IOStreamManager;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;
import java.util.TreeMap;

public class Client {

    private static final String CLIENT_STOP_WORD = "exit";
    private static CustomLogger logger = new CustomLogger("Client", "logs");

    public static void main(String[] args) {
        try {
            Socket serverSocket = new Socket("localhost", 9090);

            ObjectOutputStream oos = new ObjectOutputStream(serverSocket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(serverSocket.getInputStream());

            IOStreamManager socketManager = new IOStreamManager(ois, oos);

            byte[] exitHash = socketManager.receive();

            Scanner terminalScanner = new Scanner(System.in);
            String clientQueryString = "";

            logger.info("Enter word/combination of words you want to search ('exit' to leave):");
            clientQueryString = terminalScanner.nextLine();
            while (!clientQueryString.equals(CLIENT_STOP_WORD)) {
                socketManager.send(clientQueryString.getBytes());
                logger.info("Message '" + clientQueryString + "' was sent.");

                TreeMap<String, List<String>> locations = (TreeMap<String, List<String>>) socketManager.receiveObject();

                logger.info("Received response from the server.");
                if (locations.isEmpty()) {
                    logger.info("None of words entered is present in any of the documents.");
                } else {

                    locations.forEach((word, locationsList) -> {
                        if (locationsList != null) {
                            logger.info("Word '" + word + "' is present in: ");
                            for (String location : locationsList) {
                                logger.info("'" + location + "'");
                            }
                        } else {
                            logger.info("Word '" + word + "' is not present in any of the documents.");
                        }
                    });
                }
                logger.info("");
                logger.info("Enter word/combination of words you want to search ('exit' to leave):");
                clientQueryString = terminalScanner.nextLine();
            }

            socketManager.send(exitHash);
            serverSocket.close();
        } catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }

    }
}
