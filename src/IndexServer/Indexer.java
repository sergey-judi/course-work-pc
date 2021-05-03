package IndexServer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Indexer {

    private final int threadAmount;
    private List<String> stopWords;
    private File[] targetFiles;
    private ConcurrentMap<String, List<String>> invertedIndex;
    private TreeMap<String, List<String>> sortedInvertedIndex;

    Indexer(String stopWordsPath, int threadAmount) {
        this.threadAmount = threadAmount;
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

    private void readStopWords(String path) {
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

    public void buildIndex(String path) {
        this.targetFiles = new File(path).listFiles();
        assert this.targetFiles != null;
        this.invertedIndex = new ConcurrentHashMap<String, List<String>>();

        Thread[] threads = new Thread[this.threadAmount];

        for (int i = 0; i < this.threadAmount; i++) {
            int startIndex = this.targetFiles.length / this.threadAmount * i;
            int endIndex = (i == (this.threadAmount-1)) ? this.targetFiles.length : this.targetFiles.length / this.threadAmount * (i+1);

            threads[i] = new Thread(() -> this.processFileBlock(startIndex, endIndex));
            threads[i].start();
        }

        for (int i = 0; i < this.threadAmount; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException ex){
                ex.printStackTrace();
            }
        }

        this.sortedInvertedIndex = new TreeMap<>(this.invertedIndex);
        this.invertedIndex.clear();
    }

    private void processFileBlock(int startIndex, int endIndex) {
        for (int i = startIndex; i < endIndex; i++) {
            File currentFile = this.targetFiles[i];
            String currentFileName = currentFile.getName();

            try {
                String fileContent =  Files.readString(currentFile.toPath());
                List<String> reducedText = this.reduceText(fileContent);

                for (String word : reducedText) {
                    List<String> newList = invertedIndex.getOrDefault(word, new ArrayList<String>());

                    newList.add(currentFileName);
                    invertedIndex.put(word, newList);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public List<String> get(String word) {
        return this.sortedInvertedIndex.get(word);
    }

    private ArrayList<String> reduceText(String textToReduce) {
        ArrayList<String> reducedTextList = new ArrayList<String>();
        String reducedText = textToReduce
                .replaceAll("< *br */ *>", "")
                .replaceAll("[0-9]+", "")
                .replaceAll("_", " ");

        Pattern regexPattern = Pattern.compile("\\b\\w+[']*\\w+\\b");
        Matcher contentMatcher = regexPattern.matcher(reducedText);

        contentMatcher.results()
                .map(matchResult -> matchResult.group().toLowerCase())
                .distinct()
                .forEach(matchingWord -> {
                    if (!this.stopWords.contains(matchingWord)) {
                        reducedTextList.add(matchingWord);
                    }
                });
        return reducedTextList;
    }

    public Map<String, List<String>> locateEach(String inputText) {
        Map<String, List<String>> wordIndex = new HashMap<>();
        List<String> reducedInput = this.reduceText(inputText);

        for (String word : reducedInput) {
            List<String> wordLocations = this.sortedInvertedIndex.get(word);
            wordIndex.put(word, wordLocations);
        }

        return wordIndex;
    }
}
