package util;

/**
 * This class is used to build log messages
 * @version 03 March 2022
 */
public class MessageBuilder {
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String checkSymbol = "\u2713";
    private static final String crossSymbol = "\u2717";

    /**
     * Generates a log message with a green check mark at the end.
     * @param message Log message
     */
    public static void successMessage(String message) {
        System.out.println(message + " " + ANSI_GREEN + checkSymbol + ANSI_RESET);
    }

    /**
     * Generates an error message with a red cross at the end.
     * @param message Error message
     */
    public static void errorMessage(String message) {
        System.err.println(message + " " + ANSI_RED + crossSymbol + ANSI_RESET);
    }
}
