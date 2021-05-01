package IndexServer;

import java.util.List;
import java.util.TreeMap;

public class Server {

    public static void main(String[] args) {
        Indexer indexBuilder  = new Indexer("stop-words.txt", 4);
        indexBuilder.buildIndex("data");
        String word = "house";
        List<String> result = indexBuilder.get(word);
        System.out.println(word + " : " + result);
        TreeMap<String, List<String>> complexResult = indexBuilder.locateEach("apple, sun_love, hat, parrot, 2 <br />");
        complexResult.forEach((k, v) -> System.out.println(k + " : " + v));
    }
}
