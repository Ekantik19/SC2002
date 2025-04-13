package view.abstracts;

import java.util.Scanner;

/**
 * @author name
 * @version 1.0
 * The `ARenderView` class is an abstract class in Java that provides methods for rendering views with
 * borders, delays, input handling, and text formatting.
 */
public abstract class ARenderView {
    protected Scanner scanner = new Scanner(System.in);
    
    // Common rendering methods
    protected void printHeader(String title) {
        System.out.println("\n========== " + title + " ==========");
    }
    
    protected void printFooter() {
        System.out.println("================================");
    }
    
    // Input methods
    protected String getInput(String prompt) {
        System.out.print(prompt + ": ");
        return scanner.nextLine();
    }
    
    protected int getIntInput(String prompt) {
        while (true) {
            try {
                return Integer.parseInt(getInput(prompt));
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }
    
    // Abstract method for each view to implement
    public abstract void display();
}