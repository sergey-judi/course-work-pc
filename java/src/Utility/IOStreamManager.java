package src.Utility;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class IOStreamManager {

    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;

    public IOStreamManager(ObjectInputStream inputStream, ObjectOutputStream outputStream) {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
    }

    public void send(byte[] buffer) throws IOException {
        this.outputStream.writeInt(buffer.length);
        this.outputStream.flush();
        this.outputStream.write(buffer);
        this.outputStream.flush();
    }

    public void sendObject(Object obj) throws IOException {
        this.outputStream.writeObject(obj);
        this.outputStream.flush();
    }

    public byte[] receive() throws IOException {
        byte[] message = null;
        int messageLength = this.inputStream.readInt();
        if (messageLength > 0) {
            message = new byte[messageLength];
            inputStream.readFully(message, 0, messageLength);
        }
        return message;
    }

    public Object receiveObject() throws IOException, ClassNotFoundException {
        return this.inputStream.readObject();
    }
}
