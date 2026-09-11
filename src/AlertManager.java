public class AlertManager {

    private final int errorThreshold;

    public AlertManager(int errorThreshold) {
        this.errorThreshold = errorThreshold;
    }


    // =========================================================
    // CHECK ALERTS
    // =========================================================

    public void checkAlerts(LogStatistics statistics) {

        int errors =
                statistics.getCount("ERROR");

        int exceptions =
                statistics.getCount("EXCEPTION");

        int totalCritical =
                errors + exceptions;


        System.out.println();

        System.out.println(
                "========== ALERT STATUS =========="
        );


        // -----------------------------------------------------
        // CRITICAL ALERT
        // -----------------------------------------------------

        if (totalCritical >= errorThreshold) {

            System.out.println();

            System.out.println(
                    "!!! CRITICAL ALERT !!!"
            );

            System.out.println(
                    "High number of critical logs detected."
            );

            System.out.println(
                    "Errors + Exceptions : "
                            + totalCritical
            );

            System.out.println(
                    "Threshold           : "
                            + errorThreshold
            );

            System.out.println(
                    "System Status       : CRITICAL"
            );

            System.out.println();

        }


        // -----------------------------------------------------
        // WARNING
        // -----------------------------------------------------

        else if (totalCritical >=
                errorThreshold / 2) {

            System.out.println();

            System.out.println(
                    "WARNING: Critical logs increasing."
            );

            System.out.println(
                    "Errors + Exceptions : "
                            + totalCritical
            );

            System.out.println(
                    "System Status       : WARNING"
            );

            System.out.println();

        }


        // -----------------------------------------------------
        // NORMAL
        // -----------------------------------------------------

        else {

            System.out.println(
                    "Alert Status: NORMAL"
            );
        }


        System.out.println(
                "================================="
        );
    }
}