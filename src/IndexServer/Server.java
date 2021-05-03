package IndexServer;

import Utility.CustomLogger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;

public class Server {

    private static final int SERVER_PORT = 9090;
    private static CustomLogger logger = new CustomLogger("Server", "logs");

    public static void main(String[] args) {
        Indexer indexBuilder  = new Indexer("stop-words.txt", 4);
        indexBuilder.buildIndex("data");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> logger.info("Shutting down the server.")));

        try {
            logger.info("Server is running on port: " + SERVER_PORT);
            ServerSocket server = new ServerSocket(SERVER_PORT);
            int clientId = 0;
            while (true) {
                Socket clientSocket = server.accept();
                int finalClientId = clientId++;
                Thread clientHandler = new Thread(() -> handleClient(clientSocket, indexBuilder, finalClientId));
                clientHandler.start();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    private static void handleClient(Socket clientSocket, Indexer invertedIndex, int clientId) {
        String exitHash = generateHash();
        byte[] encodedHash = exitHash.getBytes();
        logger.info("Handling client#" + clientId + " in " + Thread.currentThread());
        try {
            DataInputStream inputStream = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream outputStream = new DataOutputStream(clientSocket.getOutputStream());

            sendBytes(encodedHash, outputStream);

            byte[] message;
            String decodedMessage;
            while (!Arrays.equals((message = receiveBytes(inputStream)), encodedHash)) {
                decodedMessage = new String(message);
                logger.info("Received '" + decodedMessage + "' from client#" + clientId);

                TreeMap<String, List<String>> locations = invertedIndex.locateEach(decodedMessage);

                ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
                oos.writeObject(locations);
                logger.info("Response sent to client#" + clientId);
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

    private static void sendBytes(byte[] encodedMessage, DataOutputStream outputStream) throws IOException {
        outputStream.writeInt(encodedMessage.length);
        outputStream.write(encodedMessage);
    }

    private static byte[] receiveBytes(DataInputStream inputStream) throws IOException {
        byte[] message = null;
        int messageLength = inputStream.readInt();
        if (messageLength > 0) {
            message = new byte[messageLength];
            inputStream.readFully(message, 0, messageLength);
        }
        return message;
    }
}
