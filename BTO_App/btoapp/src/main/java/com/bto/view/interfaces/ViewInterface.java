package com.bto.view.interfaces;

/**
 * A contract for views in the BTO Management System to implement.
 * 
 * @version 1.0
 */
public interface ViewInterface {
    /**
     * Renders the application based on the provided selection.
     * @param selection The selection made by the user for navigation.
     */
    void renderApp(int selection);
    
    /**
     * Renders the choices available to the user.
     * This method is used for displaying menu options.
     */
    void renderChoice();
}