package IndexServer;

public class Server {

    public static void main(String[] args) {
        Indexer indexBuilder  = new Indexer();
        indexBuilder.listDirectory("data");
        indexBuilder.readStopWords("stop-words.txt");
    }
}
