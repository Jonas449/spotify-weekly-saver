package util;

public class MessageBuilder {
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String checkSymbol = "\u2713";
    private static final String crossSymbol = "\u2717";

    public static void successMessage(String message) {
        System.out.println(message + " " + ANSI_GREEN + checkSymbol + ANSI_RESET);
    }

    public static void errorMessage(String message) {
        System.err.println(message + " " + ANSI_RED + crossSymbol + ANSI_RESET);
    }
}
