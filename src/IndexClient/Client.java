package IndexClient;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Client {

    public static void main(String[] args) {
        String queryString = "apple";
        byte[] stringBytes = queryString.getBytes();

        try {
            Socket serverSocket = new Socket("localhost", 9090);
            DataOutputStream out = new DataOutputStream(serverSocket.getOutputStream());
            DataInputStream in = new DataInputStream(serverSocket.getInputStream());
            out.writeInt(stringBytes.length);
            out.write(stringBytes);
            serverSocket.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }
}
