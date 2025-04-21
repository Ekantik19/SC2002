# BTO Management System

## Project Overview

This is a command-line based Build-To-Order (BTO) Management System developed for the SC2002 Object-Oriented Design & Programming course at Nanyang Technological University. The system provides a centralized platform for applicants to view and apply for BTO housing projects, and for HDB staff (officers and managers) to manage these projects and applications.

## System Architecture

The application follows the Model-View-Controller (MVC) architecture:

- **Model**: Classes that represent business entities such as User, Applicant, Project, Application
- **View**: Classes that handle user interface and display information to users
- **Controller**: Classes that contain business logic and coordinate between models and views

The system also adheres to key object-oriented principles:
- **Inheritance**: Class hierarchies (e.g., User → Applicant → HDBOfficer) that promote code reuse
- **Abstraction**: Abstract classes and interfaces that define common behaviors
- **Encapsulation**: Data hiding and exposure only through getters and setters
- **Polymorphism**: Method overriding and interface implementations

## Key Components

### Models
- **User**: Base class for all users in the system
- **Applicant**: Represents BTO applicants
- **HDBOfficer**: Officers who handle flat bookings and enquiries
- **HDBManager**: Managers who oversee projects and applications
- **Project**: Represents a BTO housing project
- **Application**: Represents an application for a BTO flat
- **Enquiry**: Represents a question about a BTO project

### Views
- **LoginView**: Handles the login interface
- **MainMenuView**: Displays the main menu based on user role
- **ProjectView**: Displays project information and management options
- **ApplicationView**: Displays application information and management options
- **EnquiryView**: Displays enquiry information and management options

### Controllers
- **AuthenticationController**: Handles user authentication and session management
- **ProjectController**: Manages project operations
- **ApplicationController**: Manages application operations
- **BookingController**: Manages flat booking operations
- **EnquiryController**: Manages enquiry operations
- **ManagerController**: Manages HDBManager-specific operations

### Data Managers
- **ApplicantDataManager**: Handles loading and saving applicant data
- **OfficerDataManager**: Handles loading and saving officer data
- **ManagerDataManager**: Handles loading and saving manager data
- **ProjectDataManager**: Handles loading and saving project data
- **ApplicationDataManager**: Handles loading and saving application data
- **EnquiryDataManager**: Handles loading and saving enquiry data

## Features

### For Applicants
- View available BTO projects based on eligibility criteria
- Apply for BTO projects
- View application status
- Request withdrawal of applications
- Create, view, edit, and delete enquiries

### For HDB Officers
- Register to handle BTO projects
- Process flat bookings for successful applicants
- Generate booking receipts
- View and reply to enquiries
- Access project details regardless of visibility settings

### For HDB Managers
- Create, edit, and delete BTO projects
- Toggle project visibility
- Approve/reject officer registrations
- Approve/reject applications
- Approve/reject withdrawal requests
- Generate reports with filtering options
- View and reply to enquiries

## Setup and Installation

1. Clone the repository
2. Ensure you have Java Development Kit (JDK) 8 or higher installed
3. Configure the file paths in `FilePathConfig.java` if necessary
4. Compile the source code
5. Run the application with `java App`

## Data Storage

The system uses text files to store data:
- `ApplicantList.txt`: Stores applicant information
- `OfficerList.txt`: Stores officer information
- `ManagerList.txt`: Stores manager information
- `ProjectList.txt`: Stores project information
- `ApplicationList.txt`: Stores application information
- `EnquiryList.txt`: Stores enquiry information

## Project Structure

```
src/
│
├── App.java                           # Main application class and entry point
│
├── controller/                        # Controllers for business logic
│   ├── abstracts/                     # Abstract controller classes
│   │   ├── AAuthenticationController.java
│   │   └── ABaseController.java
│   │
│   ├── interfaces/                    # Controller interfaces
│   │   ├── IApplicationController.java
│   │   ├── IAuthenticationController.java
│   │   ├── IBookingController.java
│   │   ├── IEnquiryController.java
│   │   ├── IManagerController.java
│   │   └── IProjectController.java
│   │
│   ├── ApplicationController.java     # Concrete controller implementations
│   ├── AuthenticationController.java
│   ├── BookingController.java
│   ├── EnquiryController.java
│   ├── ManagerController.java
│   └── ProjectController.java
│
├── datamanager/                       # Data managers for file I/O
│   ├── ApplicantDataManager.java
│   ├── ApplicationDataManager.java
│   ├── DataManager.java               # Base data manager class
│   ├── EnquiryDataManager.java
│   ├── ManagerDataManager.java
│   ├── OfficerDataManager.java
│   └── ProjectDataManager.java
│
├── enquiry/                           # Enquiry-related classes
│   ├── Enquiry.java
│   ├── EnquiryEditor.java
│   ├── Repliable.java
│   └── RepliableEditorInterface.java
│
├── model/                             # Domain model classes
│   ├── abstracts/                     # Abstract model classes
│   │   ├── AApplication.java
│   │   ├── AProject.java
│   │   └── AUser.java
│   │
│   ├── enums/                         # Enumeration types
│   │   ├── ApplicationStatus.java
│   │   ├── FlatType.java
│   │   └── UserRole.java
│   │
│   ├── interfaces/                    # Model interfaces
│   │   ├── IApplicationManagement.java
│   │   ├── IEnquiryManagement.java
│   │   ├── IProjectManagement.java
│   │   ├── IReportGeneration.java
│   │   └── IUserManagement.java
│   │
│   ├── Applicant.java                 # Concrete model implementations
│   ├── Application.java
│   ├── HDBManager.java
│   ├── HDBOfficer.java
│   ├── Project.java
│   ├── Receipt.java
│   ├── Report.java
│   └── User.java
│
├── service/                           # Service classes
│   └── EligibilityCheckerService.java
│
├── utils/                             # Utility classes
│   └── FilePathConfig.java
│
└── view/                              # View classes
    ├── abstracts/                     # Abstract view classes
    │   └── ARenderView.java
    │
    ├── interfaces/                    # View interfaces
    │   └── ViewInterface.java
    │
    ├── menu/                          # Menu-related classes
    │   ├── ApplicantMenuBuilder.java
    │   ├── ManagerMenuBuilder.java
    │   ├── MenuAction.java
    │   ├── MenuBuilder.java
    │   ├── MenuBuilderFactory.java
    │   └── OfficerMenuBuilder.java
    │
    ├── ApplicationView.java          # Concrete view implementations
    ├── EnquiryView.java
    ├── LoginView.java
    ├── MainMenuView.java
    ├── MenuNavigator.java
    ├── PasswordChangeView.java
    ├── ProjectView.java
    └── ReportView.java

resources/                            # Resource files
    ├── ApplicantList.txt             # Data storage files
    ├── ApplicationList.txt
    ├── EnquiryList.txt
    ├── ManagerList.txt
    ├── OfficerList.txt
    └── ProjectList.txt
```

## Design Patterns Used

The system implements several design patterns:
- **Singleton**: Used for data managers to ensure only one instance handles file I/O
- **Factory**: Used in MenuBuilderFactory to create different menu builders based on user role
- **Strategy**: Different algorithms are encapsulated in various controller implementations
- **MVC**: Separation of concerns between model, view, and controller classes
- **Template Method**: Abstract classes define algorithms with steps implemented by concrete subclasses

## Class Inheritance and Interface Hierarchy

- User (Abstract)
  - Applicant
    - HDBOfficer
  - HDBManager

- ARenderView (Abstract)
  - LoginView, MainMenuView, ProjectView, etc.

- ABaseController (Abstract)
  - AAuthenticationController (Abstract)
    - AuthenticationController
  - ApplicationController, ProjectController, etc.

## Contributors

- Chong Jia Cheng Aldric (U2421359L)
- Chew Yee Chen (U2423456H)
- Ong Seng Kiat (U2422708K)
- Tiwari Ekantik (U2423226J)
- Guevarra Luisa Ira Ang (U2322791D)

## License

This project was developed for educational purposes as part of the SC2002 Object-Oriented Design & Programming course at Nanyang Technological University.
