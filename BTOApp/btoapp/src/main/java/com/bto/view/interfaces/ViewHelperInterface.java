package com.bto.view.interfaces;

/**
 * Defines methods for interacting with the view in the BTO Management System user interface.
 * Provides utility methods for rendering, input handling, and display formatting.
 * 
 * @version 1.0
 */
public interface ViewHelperInterface {
    /**
     * Prints a border with the given input string.
     * @param input The input string used to generate the border.
     */
    void printBorder(String input);
    
    /**
     * Clears the command-line interface (CLI).
     * This method is used to clear the output on the CLI.
     */
    void clearCLI();
    
    /**
     * Delays execution for the specified number of seconds.
     * @param sec The number of seconds to delay execution.
     */
    void delay(int sec);
    
    /**
     * Delays execution with a custom prompt message.
     * @param sec The number of seconds to delay execution.
     * @param prompt The message to display during the delay.
     */
    void delay(int sec, String prompt);
    
    /**
     * Prints a double underline with the given input string.
     * @param input The input string to underline.
     */
    void printDoubleUnderline(String input);
    
    /**
     * Prints a single underline with the given input string.
     * @param input The input string to underline.
     */
    void printSingleUnderline(String input);
    
    /**
     * Prints a single border around the given input string.
     * @param input The input string to enclose in a border.
     */
    void printSingleBorder(String input);

    /**
     * Prompts the user for an integer input and returns it.
     * @param prompt The prompt message to display to the user.
     * @return The integer inputted by the user.
     */
    int getInputInt(String prompt);
    
    /**
     * Prompts the user for an integer input within a specified range.
     * @param prompt The prompt message to display to the user.
     * @param max The maximum allowed value for the input.
     * @return The integer inputted by the user.
     */
    int getInputInt(String prompt, int max);
    
    /**
     * Prompts the user for a string input and returns it.
     * @param prompt The prompt message to display to the user.
     * @return The string inputted by the user.
     */
    String getInputString(String prompt);
    
    /**
     * Prompts the user for a double input and returns it.
     * @param prompt The prompt message to display to the user.
     * @return The double inputted by the user.
     */
    double getInputDouble(String prompt);
    
    /**
     * Displays an exit prompt to the user.
     */
    void exitPrompt();
}