package src.IndexServer;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Indexer {

    // amount of threads used to build index
    private final int threadAmount;
    // destination file to load/write serialized index built for the recent data
    private final String indexSnapshotFilePath = "assets/inverted-index.ser";
    private final boolean loadFileFlag;
    private final boolean writeFileFlag;
    // stop words obtained from file path given to constructor
    private List<String> stopWords;
    // array of files that an index is built on
    private File[] targetFiles;
    // thread-safe data structure for inverted index
    private ConcurrentMap<String, CopyOnWriteArrayList<String>> invertedIndex;

    public Indexer(String stopWordsPath, int threadAmount, boolean loadFileFlag, boolean writeFileFlag) {
        this.threadAmount = threadAmount;
        this.loadFileFlag = loadFileFlag;
        this.writeFileFlag = writeFileFlag;
        // read stop words 
        this.readStopWords(stopWordsPath);
    }

    public void listDirectory(String path) {
        File fileIn = new File(path);
        File[] files = fileIn.listFiles();
        for (File file : files) {
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
        if (this.loadFileFlag) {
            File savedIndex = new File(indexSnapshotFilePath);
            if (savedIndex.exists()) {
                try {
                    FileInputStream indexFile = new FileInputStream(indexSnapshotFilePath);
                    ObjectInputStream inputStream = new ObjectInputStream(indexFile);
                    this.invertedIndex = (ConcurrentMap<String, CopyOnWriteArrayList<String>>) inputStream.readObject();
                    inputStream.close();
                } catch (ClassNotFoundException | IOException ex) {
                    ex.printStackTrace();
                }
                return;
            }
        }

        this.targetFiles = new File(path).listFiles();
        assert this.targetFiles != null;
        this.invertedIndex = new ConcurrentHashMap<String, CopyOnWriteArrayList<String>>();

        Thread[] threads = new Thread[this.threadAmount];

        // run threads for different file blocks
        for (int i = 0; i < this.threadAmount; i++) {
            int startIndex = this.targetFiles.length / this.threadAmount * i;
            int endIndex = (i == (this.threadAmount - 1)) ? this.targetFiles.length : this.targetFiles.length / this.threadAmount * (i + 1);

            threads[i] = new Thread(() -> this.processFileBlock(startIndex, endIndex));
            threads[i].start();
        }

        // waiting threads to finish
        for (int i = 0; i < this.threadAmount; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }

        if (this.writeFileFlag) {
            try {
                FileOutputStream indexFile = new FileOutputStream(indexSnapshotFilePath);
                ObjectOutputStream outputStream = new ObjectOutputStream(indexFile);
                outputStream.writeObject(this.invertedIndex);
                outputStream.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void processFileBlock(int startIndex, int endIndex) {
        // read files contents for the interval given
        for (int i = startIndex; i < endIndex; i++) {
            File currentFile = this.targetFiles[i];
            String currentFileName = currentFile.getName();

            try {
                String fileContent = Files.readString(currentFile.toPath());
                // remove stop words from file content and unnecessary symbols
                List<String> reducedText = this.reduceText(fileContent);

                // put each word obtained to the map
                for (String word : reducedText) {
                    CopyOnWriteArrayList<String> newList = invertedIndex.getOrDefault(word, new CopyOnWriteArrayList<String>());

                    newList.add(currentFileName);
                    invertedIndex.put(word, newList);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public List<String> get(String word) {
        return this.invertedIndex.get(word);
    }

    private ArrayList<String> reduceText(String textToReduce) {
        ArrayList<String> reducedTextList = new ArrayList<String>();
        // remove unnecessary html <br/> tags, digits and underscores
        String reducedText = textToReduce
                .replaceAll("< *br */ *>", "")
                .replaceAll("[0-9]+", "")
                .replaceAll("_", " ");
        // regex to pick words only
        Pattern regexPattern = Pattern.compile("\\b\\w+[']*\\w+\\b");
        Matcher contentMatcher = regexPattern.matcher(reducedText);
        // add all words except for stop words to the list
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
        // remove stop words from client query content and unnecessary symbols
        List<String> reducedInput = this.reduceText(inputText);

        for (String word : reducedInput) {
            List<String> wordLocations = this.invertedIndex.get(word);
            wordIndex.put(word, wordLocations);
        }

        // find intersections for all words in inputText string
        List<String> intersection = intersectLists(wordIndex.values());
        wordIndex.put(inputText, intersection);

        return wordIndex;
    }

    private List<String> intersectLists(Collection<List<String>> values) {
        if (values.isEmpty()) {
            return null;
        }

        // obtain the first list
        Iterator<List<String>> listIterator = values.iterator();
        List<String> intersectedList = new ArrayList<String>(listIterator.next());

        // intersect all lists
        while (listIterator.hasNext()) {
            intersectedList.retainAll(listIterator.next());
        }

        return intersectedList;
    }
}
