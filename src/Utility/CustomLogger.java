package Utility;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.*;

public class CustomLogger {

    private Logger logger;
    private FileHandler logHandler;
    private ConsoleHandler consoleLogHandler;

    public CustomLogger(String classname, String logDirectory) {
        try {
            this.logger = Logger.getLogger(classname);
            logHandler = new FileHandler(logDirectory + "/" + logger.getName() + ".log", 50000, 1, true);
            consoleLogHandler = new ConsoleHandler();
            logger.addHandler(logHandler);
            logger.addHandler(consoleLogHandler);
            Formatter customFormatter = new Formatter() {
                @Override
                public String format(LogRecord record) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss yyyy/MM/dd");
                    LocalDateTime currentTime = LocalDateTime.now();

                    String timeString = formatter.format(currentTime);
                    String message = record.getMessage();

                    return String.format("[%s] %s\n", timeString, message);
                }
            };
            logHandler.setFormatter(customFormatter);
            consoleLogHandler.setFormatter(customFormatter);
            logger.setUseParentHandlers(false);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void info(String message) {
        this.logger.info(message);
    }
}
