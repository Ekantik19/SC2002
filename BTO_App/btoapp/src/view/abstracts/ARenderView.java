package view.abstracts;

import java.util.Scanner;

/**
 * The {@code ARenderView} class is an abstract class in Java that provides methods for rendering views with
 * borders, delays, input handling, and text formatting.
 * 
 * @author name
 * @version 1.0
 */
public abstract class ARenderView {
    /**
     * Scanner object used to read user input from the console.
     * This scanner is initialized to read from System.in and is
     * shared among all methods in the class that need to capture
     * user input.
     */
    protected Scanner scanner = new Scanner(System.in);
    // Common rendering methods

    /**
     * Prints a formatted header with the specified title.
     *
     * @param title the header title to display
     */
    protected void printHeader(String title) {
        System.out.println("\n========== " + title + " ==========");
    }
    
    /**
     * Prints a footer separator line.
     */
    protected void printFooter() {
        System.out.println("================================");
    }
    
    // Input methods

    /**
     * Prompts the user with the specified message and reads a line of input.
     *
     * @param prompt the message to display as a prompt
     * @return the user's input as a string
     */
    protected String getInput(String prompt) {
        System.out.print(prompt + ": ");
        return scanner.nextLine();
    }
    
    /**
     * Prompts the user for an integer input with the given prompt.
     * Continues to prompt until a valid integer is entered.
     *
     * @param prompt the message to display as a prompt
     * @return the user's input as an integer
     */
    protected int getIntInput(String prompt) {
        while (true) {
            try {
                return Integer.parseInt(getInput(prompt));
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }
}