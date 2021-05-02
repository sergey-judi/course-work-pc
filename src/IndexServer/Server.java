package IndexServer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

public class Server {

    private static final int SERVER_PORT = 9090;

    public static void main(String[] args) {
        Indexer indexBuilder  = new Indexer("stop-words.txt", 4);
        indexBuilder.buildIndex("data");
        try {
            ServerSocket server = new ServerSocket(SERVER_PORT);
            while (true) {
                Socket clientSocket = server.accept();
                Thread clientHandler = new Thread(() -> handleClient(clientSocket));
                clientHandler.start();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    private static void handleClient(Socket clientSocket) {
        try {
            DataInputStream in = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
            int messageLength = in.readInt();
            if (messageLength > 0) {
                byte[] message = new byte[messageLength];
                in.readFully(message, 0, messageLength);
                System.out.println(new String(message));
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
