package com.bto.view.interfaces;

/**
 * Interface representing a view in the BTO Management System.
 * This interface combines functionality from ViewHelperInterface and ViewInterface.
 */
public interface IBTOView extends ViewHelperInterface, ViewInterface {
    /**
     * Renders the application based on the provided selection.
     * @param selection The selection made by the user for navigation.
     */
    void renderApp(int selection);
    
    /**
     * Renders the choices available to the user.
     */
    void renderChoice();
    
    /**
     * Prompts the user for an integer input and returns it.
     * @param prompt The prompt message to display to the user.
     * @return The integer inputted by the user.
     */
    int getInputInt(String prompt);
    
    /**
     * Prompts the user for an integer within a specific range.
     * @param prompt The prompt message to display to the user.
     * @param max The maximum allowed value.
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
     * Displays a BTO-themed header with the given title.
     * @param title The title to display in the header.
     */
    void printBTOHeader(String title);
    
    /**
     * Waits for the user to press Enter to continue.
     */
    void pressEnterToContinue();
    
    // Inherited methods
    /** {@inheritDoc} */
    void printBorder(String input);
    
    /** {@inheritDoc} */
    void clearCLI();
    
    /** {@inheritDoc} */
    void delay(int sec);
    
    /** {@inheritDoc} */
    void printDoubleUnderline(String input);
    
    /** {@inheritDoc} */
    void printSingleBorder(String input);
}