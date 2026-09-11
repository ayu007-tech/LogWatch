public class LogParser {

    public LogEntry parse(String line) {

        String[] parts = line.split(" ", 4);

        if (parts.length < 4) {
            return null;
        }

        String timestamp = parts[0] + " " + parts[1];
        String level = parts[2];
        String message = parts[3];

        return new LogEntry(timestamp, level, message);
    }
}