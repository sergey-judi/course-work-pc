package IndexServer;

import org.w3c.dom.ls.LSOutput;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.List;
import java.util.Scanner;

public class Indexer {

    List<String> stopWords;

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
            if(this.stopWords.isEmpty()) {
                System.out.println("File \"" + stopWordsFile.getName() + "\" with the given path: \"" + path + "\" is empty.");
            }
        }
    }
}
