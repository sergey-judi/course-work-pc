package IndexServer;

import java.util.List;

public class Server {

    public static void main(String[] args) {
        Indexer indexBuilder  = new Indexer("stop-words.txt", 4);
        indexBuilder.buildIndex("data");
        List<String> result = indexBuilder.get("house");
        System.out.println(result);
    }
}
