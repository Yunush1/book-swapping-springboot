package com.book.swap.utils.books;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Logger {

    // ANSI color codes
    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";
    private static final String CYAN = "\u001B[36m";

    // INFO (blue)
    public static void info(String message) {
//        System.out.println(BLUE + "ℹ️ [INFO] " + message + RESET);
        log.info(BLUE + "✅ [SUCCESS] {}" + RESET, message);
    }

    // SUCCESS (green)
    public static void success(String message) {
        System.out.println(GREEN + "✅ [SUCCESS] " + message + RESET);
    }

    // WARNING (yellow)
    public static void warn(String message) {
        log.warn(YELLOW + "⚠ [WARNING] {}" + RESET, message);
    }

    // ERROR (red)
    public static void error(String message) {
        log.error(RED + "❌ [ERROR] {}" + RESET, message);
    }

    // DEBUG (cyan)
    public static void debug(String message) {
        log.debug(CYAN + "\uD83D\uDC1E [DEBUG] {}" + RESET, message);
    }

    // SEPARATOR (for better readability in console)
    public static void separator() {
        System.out.println(CYAN + "--------------------------------------------------------" + RESET);
    }
}
