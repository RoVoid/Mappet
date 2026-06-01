package mchorse.mappet;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;

public class MappetLogger {
    public static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    private final Logger forgeLogger;
    private final org.apache.logging.log4j.core.Logger coreLogger;
    private FileAppender worldFileAppender;

    public MappetLogger(Logger forgeLogger) {
        this.forgeLogger = forgeLogger;
        coreLogger = (org.apache.logging.log4j.core.Logger) forgeLogger;
    }

    public void setupWorldLogging(File worldFolder) {
        closeWorldLogging();
        try {
            File logsFolder = new File(worldFolder, "logs");
            logsFolder.mkdirs();

            File file = new File(logsFolder, "latest.log");
            if (file.exists()) {
                DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                String date = LocalDate.now().format(dateFormat);

                File[] todayFiles = logsFolder.listFiles(f -> f.getName().startsWith(date));
                if (todayFiles == null) todayFiles = new File[0];

                int lastIndex = todayFiles.length == 0 ? 0 : Arrays.stream(todayFiles).map(element -> {
                    String fileName = element.getName();
                    return Integer.parseInt(fileName.substring(fileName.lastIndexOf("-") + 1, fileName.lastIndexOf(".")));
                }).max(Comparator.naturalOrder()).orElse(0);

                Path source = file.toPath();
                Path target = new File(logsFolder, LocalDate.now().format(dateFormat) + "-" + (lastIndex + 1) + ".log").toPath();
                java.nio.file.Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
            }

            PatternLayout layout = PatternLayout.newBuilder().withPattern("[%d{yyyy/MM/dd HH:mm:ss}] [%p] %m%n").build();

            worldFileAppender = FileAppender.newBuilder()
                    .withFileName(file.getPath())
                    .withAppend(true)
                    .setName("MappetFileAppender")
                    .setLayout(layout)
                    .build();
            worldFileAppender.start();
            coreLogger.addAppender(worldFileAppender);
        } catch (Exception e) {
            error("Failed to set up world file logger: " + e.getMessage());
        }
    }

    public void closeWorldLogging() {
        if (worldFileAppender != null) {
            coreLogger.removeAppender(worldFileAppender);
            worldFileAppender.stop();
            worldFileAppender = null;
        }
    }

    private static String join(Object[] parts) {
        if (parts.length == 1) return String.valueOf(parts[0]);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) sb.append(' ');
            sb.append(parts[i]);
        }
        return sb.toString();
    }

    public void info(Object... message) {
        forgeLogger.info(join(message));
    }

    public void warn(Object... message) {
        forgeLogger.warn(join(message));
    }

    public void error(Object... message) {
        forgeLogger.error(join(message));
    }

    public void debug(Object... message) {
        forgeLogger.debug(join(message));
    }
}