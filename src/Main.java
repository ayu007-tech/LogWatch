import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        // =====================================================
        // LOG FILE PATH
        // =====================================================

        String filePath = "logs/application.log";


        // =====================================================
        // CREATE COMPONENTS
        // =====================================================

        LogParser parser =
                new LogParser();

        LogStatistics statistics =
                new LogStatistics();

        ErrorAnalyzer errorAnalyzer =
                new ErrorAnalyzer();

        AlertManager alertManager =
                new AlertManager(5);

        HealthAnalyzer healthAnalyzer =
                new HealthAnalyzer();

        LogSearcher searcher =
                new LogSearcher(
                        filePath,
                        parser
                );

        ReportGenerator reportGenerator =
                new ReportGenerator("reports");


        // =====================================================
        // CREATE LOG MONITOR
        // =====================================================

        LogMonitor monitor =
                new LogMonitor(
                        filePath,
                        parser,
                        statistics,
                        errorAnalyzer,
                        alertManager,
                        healthAnalyzer
                );


        // =====================================================
        // PROCESS EXISTING LOGS
        // =====================================================

        monitor.processExistingLogs();


        // =====================================================
        // CREATE SCANNER
        // =====================================================

        Scanner scanner =
                new Scanner(System.in);


        boolean running = true;


        // =====================================================
        // MAIN MENU
        // =====================================================

        while (running) {

            System.out.println();

            System.out.println(
                    "========================================"
            );

            System.out.println(
                    "           LOGWATCH CONSOLE"
            );

            System.out.println(
                    "========================================"
            );

            System.out.println(
                    "1. View Statistics"
            );

            System.out.println(
                    "2. View Error Analysis"
            );

            System.out.println(
                    "3. View System Health"
            );

            System.out.println(
                    "4. Search Logs"
            );

            System.out.println(
                    "5. Filter Logs"
            );

            System.out.println(
                    "6. Start Real-Time Monitoring"
            );

            System.out.println(
                    "7. Generate Report"
            );

            System.out.println(
                    "8. Exit"
            );

            System.out.println(
                    "========================================"
            );

            System.out.print(
                    "Enter your choice: "
            );


            String choice =
                    scanner.nextLine();


            // =================================================
            // OPTION 1 - VIEW STATISTICS
            // =================================================

            if (choice.equals("1")) {

                statistics.displayStatistics();
            }


            // =================================================
            // OPTION 2 - ERROR ANALYSIS
            // =================================================

            else if (choice.equals("2")) {

                errorAnalyzer.displayErrorAnalysis();
            }


            // =================================================
            // OPTION 3 - SYSTEM HEALTH
            // =================================================

            else if (choice.equals("3")) {

                healthAnalyzer.displayHealth(
                        statistics
                );
            }


            // =================================================
            // OPTION 4 - SEARCH LOGS
            // =================================================

            else if (choice.equals("4")) {

                System.out.print(
                        "Enter keyword to search: "
                );

                String keyword =
                        scanner.nextLine();

                searcher.search(keyword);
            }


            // =================================================
            // OPTION 5 - FILTER LOGS
            // =================================================

            else if (choice.equals("5")) {

                System.out.println();

                System.out.println(
                        "Available levels:"
                );

                System.out.println(
                        "INFO"
                );

                System.out.println(
                        "WARNING"
                );

                System.out.println(
                        "ERROR"
                );

                System.out.println(
                        "EXCEPTION"
                );

                System.out.print(
                        "Enter log level: "
                );

                String level =
                        scanner.nextLine();

                searcher.filterByLevel(level);
            }


            // =================================================
            // OPTION 6 - REAL-TIME MONITORING
            // =================================================

            else if (choice.equals("6")) {

                System.out.println();

                System.out.println(
                        "Starting real-time monitoring..."
                );

                System.out.println(
                        "Press Ctrl + C to stop."
                );

                monitor.startMonitoring();
            }


            // =================================================
            // OPTION 7 - GENERATE REPORT
            // =================================================

            else if (choice.equals("7")) {

                reportGenerator.generateReport(
                        statistics,
                        healthAnalyzer
                );
            }


            // =================================================
            // OPTION 8 - EXIT
            // =================================================

            else if (choice.equals("8")) {

                running = false;

                System.out.println();

                System.out.println(
                        "Thank you for using LogWatch."
                );

                System.out.println(
                        "Application stopped."
                );
            }


            // =================================================
            // INVALID OPTION
            // =================================================

            else {

                System.out.println();

                System.out.println(
                        "Invalid choice."
                );

                System.out.println(
                        "Please enter a number between 1 and 8."
                );
            }
        }


        // =====================================================
        // CLOSE SCANNER
        // =====================================================

        scanner.close();
    }
}