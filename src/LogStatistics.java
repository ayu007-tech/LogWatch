import java.util.HashMap;
import java.util.Map;

public class LogStatistics {

    private int totalLogs;

    private Map<String, Integer> logCounts;

    public LogStatistics() {

        totalLogs = 0;

        logCounts = new HashMap<>();

        logCounts.put("INFO", 0);
        logCounts.put("WARNING", 0);
        logCounts.put("ERROR", 0);
        logCounts.put("EXCEPTION", 0);
    }

    public void addLog(LogEntry logEntry) {

        totalLogs++;

        String level = logEntry.getLevel();

        if (logCounts.containsKey(level)) {

            logCounts.put(
                level,
                logCounts.get(level) + 1
            );

        } else {

            logCounts.put(level, 1);
        }
    }

    public int getTotalLogs() {

        return totalLogs;
    }

    public int getCount(String level) {

        return logCounts.getOrDefault(level, 0);
    }

    public void displayStatistics() {

        System.out.println("\n========== LOGWATCH STATISTICS ==========\n");

        System.out.println("Total Logs : " + totalLogs);

        System.out.println();

        System.out.println("INFO      : " + getCount("INFO"));
        System.out.println("WARNING   : " + getCount("WARNING"));
        System.out.println("ERROR     : " + getCount("ERROR"));
        System.out.println("EXCEPTION : " + getCount("EXCEPTION"));

        System.out.println("\n=========================================");
    }
}