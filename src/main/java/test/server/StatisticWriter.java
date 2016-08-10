package test.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.TimerTask;

public class StatisticWriter extends TimerTask {
    private final Logger logger = LoggerFactory.getLogger(StatisticWriter.class);
    private Statistic stat;
    private String path;

    public StatisticWriter(Statistic stat, String path) {
        this.stat = stat;
        this.path = path;
    }

    public void run() {
        logger.info("Save statistic to file");
        try {
            File file = new File(path);

            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("%s%10s%s\n", "File", " ", "Download counts"));
            for (Map.Entry<String, Integer> entry : stat.getStat().entrySet()) {
                sb.append(String.format("%s%10s%d\n", entry.getKey(), " ", entry.getValue()));
            }
            bw.write(sb.toString());
            bw.close();
        } catch (IOException e) {
            logger.error("When writing to the statistic file the error occurred: " + e);
        }
    }
}
