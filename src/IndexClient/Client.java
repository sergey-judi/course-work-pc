package IndexClient;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;
import java.util.TreeMap;

public class Client {

    private static final String CLIENT_STOP_WORD = "exit";

    public static void main(String[] args) {
        try {
            Socket serverSocket = new Socket("localhost", 9090);

            DataOutputStream outputStream = new DataOutputStream(serverSocket.getOutputStream());
            DataInputStream inputStream = new DataInputStream(serverSocket.getInputStream());

            byte[] exitHash = receiveBytes(inputStream);

            Scanner terminalScanner = new Scanner(System.in);
            String clientQueryString;

            System.out.println("Enter word/combination of words you want to search");
            while (!(clientQueryString = terminalScanner.nextLine()).equals(CLIENT_STOP_WORD)) {
                sendBytes(clientQueryString.getBytes(), outputStream);
                System.out.println("Message '" + clientQueryString + "' was sent.");

                ObjectInputStream ois = new ObjectInputStream(serverSocket.getInputStream());

                TreeMap<String, List<String>> locations = (TreeMap<String, List<String>>) ois.readObject();

                System.out.println("Received response from the server.");

                locations.forEach((word, locationsList) -> {
                    if (locationsList != null) {
                        System.out.println("Word '" + word + "' is present in: ");
                        for (String location : locationsList) {
                            System.out.println("\t\t" + location);
                        }
                    } else {
                        System.out.println("Word '" + word + "' is not present in any of the documents.");
                    }
                });
                System.out.println();
                System.out.println("Enter word/combination of words you want to search:");
            }

            sendBytes(exitHash, outputStream);
            serverSocket.close();
        } catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }

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
