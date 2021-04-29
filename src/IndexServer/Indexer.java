package IndexServer;

import java.io.File;

public class Indexer {

    public void listDirectory(String path) {
        File fileIn = new File(path);
        File[] files = fileIn.listFiles();
        for(File file : files) {
            String filePath = path + "/" + file.getName();
            if (file.isDirectory()) {
                System.out.println();
                this.renameFiles(filePath);
            } else {
                System.out.println(file.getName());
            }
        }
    }

    public void renameFiles(String path) {
        File fileIn = new File(path);
        File[] files = fileIn.listFiles();
        for(File file : files) {
            String filePath = path + "/" + file.getName();
            if (file.isDirectory()) {
                this.renameFiles(filePath);
            } else {
                String newFilePath = filePath.substring(0, filePath.indexOf(".")) + "-" + file.getParentFile().getName() + ".txt";
                File renamedFile = new File(newFilePath);

                boolean wasRenamed = file.renameTo(renamedFile);
                if (!wasRenamed) {
                    System.out.printf("Error while renaming %n %s %n to %n %s", filePath, newFilePath);
                }
            }
        }
    }

}
