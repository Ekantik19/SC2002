package com.bto.view;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bto.controller.ProjectController;
import com.bto.model.Project;
import com.bto.model.User;
import com.bto.view.abstracts.ARenderView;

/**
 * ProjectView class is responsible for rendering project-related views
 * in the BTO Management System.
 * 
 * This class extends the abstract base view class {@link ARenderView}.
 */
public class ProjectView extends ARenderView {
    private User currentUser;
    private ProjectController projectController;

    /**
     * Constructs a new ProjectView with the necessary dependencies.
     * 
     * @param currentUser The user currently interacting with the system
     * @param projectController Controller for project-related operations
     */
    public ProjectView(User currentUser, ProjectController projectController) {
        this.currentUser = currentUser;
        this.projectController = projectController;
    }

    /**
     * Renders the application based on the user's selection.
     * 
     * @param selection The selected menu option
     *                  <ul>
     *                      <li>0: Main menu</li>
     *                      <li>1: View All Projects</li>
     *                      <li>2: Filter Projects by Neighborhood</li>
     *                      <li>3: Filter Projects by Flat Type</li>
     *                      <li>4: View Project Details</li>
     *                  </ul>
     */
    @Override
    public void renderApp(int selection) {
        clearCLI();
        switch (selection) {
            case 0:
                renderChoice();
                break;
            case 1:
                viewAllProjects();
                break;
            case 2:
                filterProjectsByNeighborhood();
                break;
            case 3:
                filterProjectsByFlatType();
                break;
            case 4:
                viewProjectDetails();
                break;
            default:
                System.out.println("Invalid option. Please try again.");
                delay(2);
                renderApp(0);
        }
    }

    /**
     * Renders the main choice menu for project-related operations.
     */
    @Override
    public void renderChoice() {
        printBorder("Project Management");
        System.out.println("Welcome, " + currentUser.getUserID());
        System.out.println("Select an option:");
        System.out.println("(1) View All Projects");
        System.out.println("(2) Filter Projects by Neighborhood");
        System.out.println("(3) Filter Projects by Flat Type");
        System.out.println("(4) View Project Details");
        System.out.println("(0) Exit");
    }

    /**
     * View all projects available to the current user.
     */
    private void viewAllProjects() {
        // Create empty filters to get all available projects
        Map<String, Object> filters = new HashMap<>();
        List<Project> projects = projectController.getAvailableProjects(currentUser);
        
        displayProjects(projects);
        
        pressEnterToContinue();
        renderApp(0);
    }

    /**
     * Filter projects by neighborhood.
     */
    private void filterProjectsByNeighborhood() {
        String neighborhood = getInputString("Enter neighborhood to filter: ");
        
        List<Project> filteredProjects = projectController.getProjectsByNeighborhood(
            neighborhood, 
            currentUser
        );
        
        displayProjects(filteredProjects);
        
        pressEnterToContinue();
        renderApp(0);
    }

    /**
     * Filter projects by flat type.
     */
    private void filterProjectsByFlatType() {
        String flatType = getInputString("Enter flat type (2-Room/3-Room): ");
        
        List<Project> filteredProjects = projectController.getProjectsByFlatType(
            flatType, 
            currentUser
        );
        
        displayProjects(filteredProjects);
        
        pressEnterToContinue();
        renderApp(0);
    }

    /**
     * View details of a specific project.
     */
    private void viewProjectDetails() {
        String projectName = getInputString("Enter project name: ");
        
        Project project = projectController.getProjectByName(projectName);
        
        if (project != null) {
            displayProjectDetails(project);
        } else {
            System.out.println("Project not found.");
        }
        
        pressEnterToContinue();
        renderApp(0);
    }

    /**
     * Display a list of projects.
     * 
     * @param projects List of projects to display
     */
    private void displayProjects(List<Project> projects) {
        if (projects.isEmpty()) {
            System.out.println("No projects found.");
            return;
        }
        
        System.out.println("Projects:");
        for (Project project : projects) {
            System.out.printf("Project: %s, Neighborhood: %s, Visibility: %s\n", 
                project.getProjectName(), 
                project.getNeighborhood(), 
                project.isVisible() ? "Visible" : "Hidden"
            );
        }
    }

    /**
     * Display detailed information about a specific project.
     * 
     * @param project The project to display details for
     */
    private void displayProjectDetails(Project project) {
        System.out.println("Project Details:");
        System.out.println("Name: " + project.getProjectName());
        System.out.println("Neighborhood: " + project.getNeighborhood());
        System.out.println("Opening Date: " + project.getOpeningDate());
        System.out.println("Closing Date: " + project.getClosingDate());
        System.out.println("Visibility: " + (project.isVisible() ? "Visible" : "Hidden"));
        
        System.out.println("\nFlat Types:");
        project.getFlatTypes().forEach((type, units) -> 
            System.out.printf("%s: %d units\n", type, units)
        );
    }
}