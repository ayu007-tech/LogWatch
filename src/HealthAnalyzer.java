public class HealthAnalyzer {

    public void displayHealth(LogStatistics statistics) {

        int totalLogs = statistics.getTotalLogs();

        int errors = statistics.getCount("ERROR");

        int warnings = statistics.getCount("WARNING");

        int exceptions = statistics.getCount("EXCEPTION");

        int problems = errors + exceptions;


        // If there are no logs, system is healthy by default
        if (totalLogs == 0) {

            System.out.println();
            System.out.println("========== SYSTEM HEALTH ==========");
            System.out.println("Status       : NO DATA");
            System.out.println("Health Score : 100 / 100");
            System.out.println("==================================");

            return;
        }


        // Calculate health score
        int penalty =
                (errors * 10)
                + (exceptions * 15)
                + (warnings * 3);

        int healthScore =
                100 - penalty;


        // Keep score between 0 and 100
        if (healthScore < 0) {
            healthScore = 0;
        }


        String status;


        if (healthScore >= 80) {

            status = "HEALTHY";

        } else if (healthScore >= 50) {

            status = "WARNING";

        } else {

            status = "CRITICAL";
        }


        System.out.println();
        System.out.println(
                "========== SYSTEM HEALTH =========="
        );

        System.out.println(
                "Status          : " + status
        );

        System.out.println(
                "Total Logs      : " + totalLogs
        );

        System.out.println(
                "Errors          : " + errors
        );

        System.out.println(
                "Warnings        : " + warnings
        );

        System.out.println(
                "Exceptions      : " + exceptions
        );

        System.out.println(
                "Health Score    : "
                        + healthScore
                        + " / 100"
        );

        System.out.println(
                "==================================="
        );
    }
}