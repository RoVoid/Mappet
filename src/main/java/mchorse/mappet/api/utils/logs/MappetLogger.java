package mchorse.mappet.api.utils.logs;

import mchorse.mappet.api.scripts.user.logs.IMappetLogger;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.logging.*;

public class MappetLogger extends Logger implements IMappetLogger {
    public static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    public MappetLogger(String name, File worldFile) {
        super(name, null);
        FileHandler handler;
        try {
            File logsFolder = new File(worldFile, "logs");
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
                Path target = new File(logsFolder, LocalDateTime.now().format(dateFormat) + "-" + (lastIndex + 1) + ".log").toPath();
                java.nio.file.Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
            }

            handler = new FileHandler(file.getPath(), true);
            handler.setEncoding("UTF-8");
            handler.setFormatter(new Formatter() {
                @Override
                public String format(LogRecord record) {
                    return "[" + LocalDateTime.now().format(dtf) + "] " + "[" + record.getLevel().getName() + "] " + formatMessage(record) + "\n";
                }
            });

            addHandler(handler);
        } catch (Exception e) {
            e.printStackTrace();
        }

        setUseParentHandlers(false);
        setLevel(Level.ALL);
    }

    @Override
    public void error(String message) {
        log(LoggerLevel.ERROR.value, message);
    }

    @Override
    public void debug(String message) {
        log(LoggerLevel.DEBUG.value, message);
    }
}


