# AI-Empowered Personal Finance Tracker

## Project Overview

This project is an AI-Empowered Personal Finance Tracker developed as part of the EBU6304 - Software Engineering Group Project. The application helps users track expenses, set savings goals, and analyze spending habits using AI. It integrates both automated data processing and manual validation to ensure accuracy while adapting to regional spending patterns and economic conditions.

## Features

### Manual & Automated Data Entry
- Users can manually input transactions through forms
- Supports CSV file import for bulk data entry
- Sample data is provided for demonstration purposes

### Expense Categorization
- AI automatically classifies transactions into categories
- Users can manually review and correct categorizations
- Category management with budgets and expense tracking

### Spending Insights & Predictions
- Generates financial reports and visualizations
- Provides budget progress tracking
- Includes AI-powered financial advice and predictions

### Local Financial Context
- Customizable to regional budgeting habits
- Detects seasonal spending patterns
- Adapts to local economic conditions

### User Interface
- Dashboard with financial summaries and charts
- Navigation between different financial management sections
- Settings for theme and currency preferences

## Technical Implementation

### Architecture
- Stand-alone Java application with Swing GUI
- Modular design with separate packages for model, view, and controller components
- Uses JFreeChart for data visualization
- Implements currency management and theme settings

### Key Classes
- `Main`: Application entry point
- `LoginFrame`: User authentication interface
- `MainFrame`: Main application window with navigation
- `FinanceData`: Manages financial data and calculations
- `ChineseHolidayCalendar`: Handles local holiday calculations
- `CurrencyManager`: Manages currency settings and conversions
- `ThemeManager`: Manages application theme settings
- `DashboardPanel`: Main dashboard interface
- `TransactionsPanel`: Transaction management interface
- `BudgetsPanel`: Budget management interface
- `ReportsPanel`: Financial reports interface
- `AIPanel`: AI assistant interface
- `SettingsPanel`: Application settings interface

### Data Management
- Uses in-memory data structures for demonstration
- Supports CSV import/export for data persistence
- No external database required

## Running the Application

### Prerequisites
- Java Development Kit (JDK) 11 or later
- Required libraries: FlatLaf, JFreeChart, Hutool

### Compilation and Execution
1. Compile all Java files using your preferred IDE or command-line compiler
2. Run the `Main` class to start the application

## Testing

### Test Strategy
- Unit testing for core functionality
- Integration testing for component interactions
- User acceptance testing for UI and workflow validation

### Test Cases
- Login functionality validation
- Transaction creation and management
- Budget setting and tracking
- Report generation accuracy
- CSV import/export functionality
- Currency conversion correctness
- Theme switching functionality

## User Manual

### Getting Started
1. Launch the application
2. Log in using default credentials (for demonstration purposes)
3. Navigate through the dashboard to view financial summaries
4. Use the navigation bar to access different features

### Managing Transactions
- Add new transactions manually through forms
- Import transactions from CSV files
- View and edit existing transactions
- Filter and search transactions

### Managing Budgets
- Set budgets for different categories
- Track spending against budgets
- View budget progress visualizations
- Receive budget alerts when approaching limits

### Viewing Reports
- Generate income vs expenses reports
- View category breakdowns
- Analyze spending trends
- Customize report time periods and intervals

### Using AI Features
- Get financial advice from the AI assistant
- Receive spending pattern analysis
- Obtain personalized savings recommendations

### Customizing Settings
- Switch between light and dark themes
- Change currency display preferences
- Manage application notifications
- Update security settings

## Project Management

### Agile Methodology
- Scrum framework with daily standups
- Sprint planning and retrospectives
- User stories and acceptance criteria
- Continuous integration and delivery

### Version Control
- GitHub for code repository and collaboration
- Branching strategy for feature development
- Pull requests for code reviews
- Issue tracking for bug reporting and feature requests

## Future Enhancements

- Implement machine learning for improved AI predictions
- Add multi-user support and data synchronization
- Expand financial analysis capabilities
- Develop mobile companion application
- Enhance data security and encryption