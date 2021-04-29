package IndexServer;

public class Server {

    public static void main(String[] args) {
        Indexer indexBuilder  = new Indexer();
        int N = 12500;
        int V = 22;
        float startIndex = N/50*(V-1);
        float endIndex = N/50*V;
        System.out.printf("%5.1f %5.1f %n", startIndex, endIndex);
        indexBuilder.listDirectory("data");
    }
}
