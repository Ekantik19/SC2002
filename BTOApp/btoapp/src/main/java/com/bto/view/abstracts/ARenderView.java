package com.bto.view.abstracts;

import java.util.Locale;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import com.bto.view.interfaces.ViewHelperInterface;
import com.bto.view.interfaces.ViewInterface;

/**
 * @author Loo Si Hui
 * @version 1.0
 * The `ARenderView` class is an abstract class in Java that provides methods for rendering views with
 * borders, delays, input handling, and text formatting.
 */
public abstract class ARenderView implements ViewInterface, ViewHelperInterface {
    protected Scanner sc;

    /** 
     * The `public ARenderView()` constructor in the `ARenderView` class initializes a new `Scanner`
     * object `sc` 
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
     * Clears the command line interface screen in a cross-platform manner
     */
    @Override
    public void clearCLI() {
        try {
            final String os = System.getProperty("os.name").toLowerCase();
            
            if (os.contains("win")) {
                // Windows
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                // Unix-like systems (macOS, Linux)
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception err) {
            System.out.println("Error clearing screen: " + err.getMessage());
        }
    }

    // Rest of the methods remain the same as in the original implementation

    /**
     * Method for rendering the view
     *
     * @param selection Selects the page to render from the switch case
     */
    @Override
    public void renderApp(int selection) {
        switch(selection) {
            case 0: // Render the option
                renderChoice();
                break;
            default:
                System.out.println("Invalid selection");
        }
    }

    /**
     * Method for rendering the choice
     */
    @Override
    public void renderChoice() {
        printBorder("");
    }

    // All other methods from the original implementation remain the same
}