package com.bto.view.interfaces;

/**
 * Interface representing a comprehensive view in the BTO Management System.
 * Combines functionality from ViewHelperInterface and ViewInterface.
 * Provides a unified interface for BTO-specific view operations.
 * 
 * @version 1.0
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
     * Prompts the user for a string input and returns it.
     * @param prompt The prompt message to display to the user.
     * @return The string inputted by the user.
     */
    String getInputString(String prompt);
    
    // Inherited methods from ViewHelperInterface and ViewInterface
    // These comments serve as placeholders for the inherited documentation.
    
    /** {@inheritDoc} */
    void printBorder(String input);
    
    /** {@inheritDoc} */
    void clearCLI();
    
    /** {@inheritDoc} */
    void delay(int sec);
    
    /** {@inheritDoc} */
    void printDoubleUnderline(String input);
    
    /** {@inheritDoc} */
    void printSingleUnderline(String input);
    
    /** {@inheritDoc} */
    void printSingleBorder(String input);
}