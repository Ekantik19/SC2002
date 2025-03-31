/**
 * ARenderView - Abstract base class for all views in the BTO Management System.
 * Provides methods for rendering views with borders, delays, input handling, and text formatting.
 * 
 * @version 1.0
 */
package com.bto.view.abstracts;

import java.util.Locale;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import com.bto.view.interfaces.IBTOView;

//public abstract class ARenderView implements ViewInterface, ViewHelperInterface {
public abstract class ARenderView implements IBTOView {
    protected Scanner sc;

    /**
     * Constructor initializes a new Scanner object for user input
     */
    public ARenderView() {
        sc = new Scanner(System.in);
    }

    /**
     * Prints a string surrounded by a double-lined border
     * 
     * @param input The input string displayed in the middle of the border.
     */
    @Override
    public void printBorder(String input) {
        clearCLI();
        String space = String.format("%" + (99 - input.length()) + "s", "");
        String halfSpace = String.format("%" + (99 - input.length()) / 2 + "s", "");
        System.out.println(
                "╔════════════════════════════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║ " + halfSpace + input + halfSpace + "║");
        System.out.println(
                "╚════════════════════════════════════════════════════════════════════════════════════════════════════╝");
    }

    /**
     * Uses a ProcessBuilder to clear the command line interface screen
     * by executing the "cls" command (Windows) or "clear" command (Unix/Linux)
     */
    @Override
    public void clearCLI() {
        try {
            String operatingSystem = System.getProperty("os.name").toLowerCase();
            
            if (operatingSystem.contains("windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                // For Unix/Linux/MacOS
                new ProcessBuilder("clear").inheritIO().start().waitFor();
            }
        } catch (Exception err) {
            // Fall back to printing newlines if clearing fails
            System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
        }
    }

    /**
     * Prints a countdown message for the specified number of seconds
     * 
     * @param sec The number of seconds to delay.
     */
    public void delay(int sec) {
        for (int i = 0; i < sec; i++) {
            System.out.printf("Returning in %d second%s\n", sec - i, (sec - i != 1) ? "s" : "");
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Delays execution with a custom prompt
     * 
     * @param sec    The number of seconds to delay.
     * @param prompt The prompt to be printed before the delay.
     */
    public void delay(int sec, String prompt) {
        System.out.println(prompt);
        try {
            TimeUnit.SECONDS.sleep(sec);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Prints a string with a double underline below it.
     * 
     * @param input The string to be printed above the double underlines
     */
    @Override
    public void printDoubleUnderline(String input) {
        String space = String.format("%" + (99 - input.length()) + "s", "");
        System.out.println(input + space);
        System.out.println(
                "════════════════════════════════════════════════════════════════════════════════════════════════════");
    }

    /**
     * Prints a string with a single underline below it.
     * 
     * @param input The string to be printed above the single underline
     */
    public void printSingleUnderline(String input) {
        String space = String.format("%" + (99 - input.length()) + "s", "");
        System.out.println(input + space);
        System.out.println(
                "_____________________________________________________________________________________________________");
    }

    /**
     * Prints a string with a single-lined border
     * 
     * @param input The string to be displayed in the border
     */
    @Override
    public void printSingleBorder(String input) {
        String space = String.format("%" + (99 - input.length()) + "s", "");
        System.out.println(
                "┌────────────────────────────────────────────────────────────────────────────────────────────────────┐");
        System.out.println("│ " + input + space + "│");
        System.out.println(
                "└────────────────────────────────────────────────────────────────────────────────────────────────────┘");
    }

    /**
     * Reads an integer input from the user
     * 
     * @param prompt A prompt message to display to the user
     * @return The integer input by the user, or -1 if invalid
     */
    public int getInputInt(String prompt) {
        if (!prompt.isEmpty()) {
            System.out.print(prompt);
        }
        try {
            int input = sc.nextInt();
            sc.nextLine();
            return input;
        } catch (Exception e) {
            System.out.println("Please enter a valid integer.");
            sc.nextLine(); // Consume invalid input
            return -1;
        }
    }

    /**
     * Reads an integer input from the user with a maximum bound
     * 
     * @param prompt A prompt message to display to the user
     * @param max The maximum allowed value
     * @return The integer input by the user, or -1 if invalid
     */
    public int getInputInt(String prompt, int max) {
        if (!prompt.isEmpty()) {
            System.out.print(prompt);
        }
        try {
            int input = sc.nextInt();
            sc.nextLine();
            while (input > max || input < 0) {
                System.out.println("Selection out of range. Please enter a number between 0 and " + max);
                input = sc.nextInt();
                sc.nextLine();
            }
            return input;
        } catch (Exception e) {
            System.out.println("Please enter a valid integer.");
            sc.nextLine(); // Consume invalid input
            return -1;
        }
    }

    /**
     * Reads a string input from the user
     * 
     * @param prompt A prompt message to display to the user
     * @return The string input by the user
     */
    public String getInputString(String prompt) {
        if (!prompt.isEmpty()) {
            System.out.print(prompt);
        }
        try {
            return sc.nextLine();
        } catch (Exception e) {
            System.out.println("Error receiving input.");
            sc.next();
            return "";
        }
    }

    /**
     * Reads a double input from the user
     * 
     * @param prompt A prompt message to display to the user
     * @return The double input by the user, or -1 if invalid
     */    
    public double getInputDouble(String prompt) {
        if (!prompt.isEmpty()) {
            System.out.print(prompt);
        }
        try {
            double input = sc.nextDouble();
            sc.nextLine();
            return input;
        } catch (Exception e) {
            System.out.println("Please enter a valid number.");
            sc.nextLine(); // Consume invalid input
            return -1;
        }
    }

    /**
     * Formats a name by capitalizing the first letter of each word
     * 
     * @param name The string to be formatted
     * @return The formatted string
     */
    protected String formatName(String name) {
        // Split the name into words
        String[] words = name.split(" ");

        // Capitalize the first letter of each word
        StringBuilder formattedName = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                formattedName.append(word.substring(0, 1).toUpperCase(Locale.ENGLISH));
                if (word.length() > 1) {
                    formattedName.append(word.substring(1).toLowerCase(Locale.ENGLISH));
                }
                formattedName.append(" ");
            }
        }

        // Trim trailing space and return
        return formattedName.toString().trim();
    }

    /**
     * Waits for the user to press Enter before continuing
     */    
    public void pressEnterToContinue() {
        System.out.println("\nPress Enter to continue...");
        sc.nextLine();
    }

    /**
     * Method for rendering the view
     *
     * @param selection Selects the page to render from the switch case
     */
    @Override
    public abstract void renderApp(int selection);

    /**
     * Method for rendering the choice menu
     */
    @Override
    public abstract void renderChoice();
    
    /**
     * Prints a BTO-themed header
     * 
     * @param title The title to display in the header
     */
    public void printBTOHeader(String title) {
        System.out.println(
                "╔════════════════════════════════════════════════════════════════════════════════════════╗");
        System.out.println(
                "║  ██████╗ ████████╗ ██████╗     ███╗   ███╗ █████╗ ███╗   ██╗ █████╗  ██████╗ ███████╗  ║");
        System.out.println(
                "║  ██╔══██╗╚══██╔══╝██╔═══██╗    ████╗ ████║██╔══██╗████╗  ██║██╔══██╗██╔════╝ ██╔════╝  ║");
        System.out.println(
                "║  ██████╔╝   ██║   ██║   ██║    ██╔████╔██║███████║██╔██╗ ██║███████║██║  ███╗█████╗    ║");
        System.out.println(
                "║  ██╔══██╗   ██║   ██║   ██║    ██║╚██╔╝██║██╔══██║██║╚██╗██║██╔══██║██║   ██║██╔══╝    ║");
        System.out.println(
                "║  ██████╔╝   ██║   ╚██████╔╝    ██║ ╚═╝ ██║██║  ██║██║ ╚████║██║  ██║╚██████╔╝███████╗  ║");
        System.out.println(
                "║  ╚═════╝    ╚═╝    ╚═════╝     ╚═╝     ╚═╝╚═╝  ╚═╝╚═╝  ╚═══╝╚═╝  ╚═╝ ╚═════╝ ╚══════╝  ║");
        System.out.println(
                "╚════════════════════════════════════════════════════════════════════════════════════════╝");
        System.out.println("                              " + title);
    }
}