package IndexServer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Indexer {

    List<String> stopWords;

    Indexer(String stopWordsPath) {
        this.readStopWords(stopWordsPath);
    }

    public void listDirectory(String path) {
        File fileIn = new File(path);
        File[] files = fileIn.listFiles();
        for(File file : files) {
            String filePath = path + "/" + file.getName();
            if (file.isDirectory()) {
                System.out.println();
                this.listDirectory(filePath);
            } else {
                System.out.println(file.getName());
            }
        }
    }

    public void readStopWords(String path) {
        File stopWordsFile = new File(path);

        if (stopWordsFile.exists()) {
            try {
                this.stopWords = Files.readAllLines(stopWordsFile.toPath());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            if (this.stopWords.isEmpty()) {
                System.out.println("File \"" + stopWordsFile.getName() + "\" with the given path: \"" + path + "\" is empty.");
            }
        }
    }

    public ArrayList<String> reduceText(File file) {
        ArrayList<String> reducedText = new ArrayList<String>();
        try {
            String fileContent = Files.readString(file.toPath())
                    .replaceAll("< *br */ *>", "")
                    .replaceAll("[0-9]+", "");

            Pattern regexPattern = Pattern.compile("\\b\\w+\\b");
            Matcher contentMatcher = regexPattern.matcher(fileContent);

            while (contentMatcher.find()) {
                String currentWord = contentMatcher.group();
                if (!this.stopWords.contains(currentWord)) {
                    reducedText.add(currentWord);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return reducedText;
    }
}
