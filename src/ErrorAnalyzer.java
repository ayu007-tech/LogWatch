import java.util.HashMap;
import java.util.Map;

public class ErrorAnalyzer {

    private Map<String, Integer> errorCounts;

    public ErrorAnalyzer() {

        errorCounts = new HashMap<>();
    }


    // Analyze only ERROR and EXCEPTION logs
    public void analyze(LogEntry logEntry) {

        String level = logEntry.getLevel();

        if (level.equals("ERROR")
                || level.equals("EXCEPTION")) {

            String message = logEntry.getMessage();

            errorCounts.put(
                    message,
                    errorCounts.getOrDefault(message, 0) + 1
            );
        }
    }


    // Display all detected errors
    public void displayErrorAnalysis() {

        System.out.println();
        System.out.println(
                "========== ERROR ANALYSIS =========="
        );

        if (errorCounts.isEmpty()) {

            System.out.println(
                    "No errors detected."
            );

        } else {

            for (Map.Entry<String, Integer> entry
                    : errorCounts.entrySet()) {

                System.out.println(
                        entry.getKey()
                                + " : "
                                + entry.getValue()
                );
            }

            displayMostFrequentError();
        }

        System.out.println(
                "===================================="
        );
    }


    // Find the most frequently occurring error
    private void displayMostFrequentError() {

        String mostFrequentError = null;

        int highestCount = 0;

        for (Map.Entry<String, Integer> entry
                : errorCounts.entrySet()) {

            if (entry.getValue() > highestCount) {

                highestCount = entry.getValue();

                mostFrequentError =
                        entry.getKey();
            }
        }

        System.out.println();
        System.out.println(
                "Most Frequent Error:"
        );

        System.out.println(
                mostFrequentError
        );

        System.out.println(
                "Occurrences: " + highestCount
        );
    }
}