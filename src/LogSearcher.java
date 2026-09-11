import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class LogSearcher {

    private final String filePath;
    private final LogParser parser;

    public LogSearcher(String filePath, LogParser parser) {
        this.filePath = filePath;
        this.parser = parser;
    }


    // =========================================================
    // SEARCH LOG MESSAGE
    // =========================================================

    public void search(String keyword) {

        int matches = 0;

        System.out.println();
        System.out.println(
                "========== SEARCH RESULTS =========="
        );

        try (BufferedReader reader =
                     new BufferedReader(
                             new FileReader(filePath))) {

            String line;

            while ((line = reader.readLine()) != null) {

                if (line.toLowerCase()
                        .contains(keyword.toLowerCase())) {

                    System.out.println(line);

                    matches++;
                }
            }

            System.out.println();
            System.out.println(
                    "Total matches: " + matches
            );

        } catch (IOException e) {

            System.out.println(
                    "Search error: " + e.getMessage()
            );
        }

        System.out.println(
                "===================================="
        );
    }


    // =========================================================
    // FILTER BY LOG LEVEL
    // =========================================================

    public void filterByLevel(String level) {

        int matches = 0;

        System.out.println();
        System.out.println(
                "========== " + level + " LOGS =========="
        );

        try (BufferedReader reader =
                     new BufferedReader(
                             new FileReader(filePath))) {

            String line;

            while ((line = reader.readLine()) != null) {

                LogEntry logEntry =
                        parser.parse(line);

                if (logEntry != null &&
                        logEntry.getLevel()
                                .equalsIgnoreCase(level)) {

                    System.out.println(logEntry);

                    matches++;
                }
            }

            System.out.println();
            System.out.println(
                    "Total " + level
                            + " logs: " + matches
            );

        } catch (IOException e) {

            System.out.println(
                    "Filter error: "
                            + e.getMessage()
            );
        }

        System.out.println(
                "===================================="
        );
    }
}