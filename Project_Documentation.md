# 🏦 BMS Bank Management System — Comprehensive Project Documentation

This document provides an exhaustive, class-by-class, and method-by-method breakdown of the Bank Management System. It explicitly details the Object-Oriented Programming (OOP) concepts, data structures, and file-handling techniques utilized in its construction, mapped directly to the system's Course Outcomes (COs).

---

## 📌 1. Problem Statement & Objectives

To develop a robust, desktop-based Bank Management System using Java and JavaFX. The system must:
1. Allow users to register accounts securely while capturing complete KYC details.
2. Support core banking operations (Deposits, Withdrawals, Transfers) with mandatory password protection.
3. Feature an automated interest accrual mechanism (calculated differently for Savings and Current accounts).
4. Integrate a comprehensive Loan Module (apply, preview EMI, track status).
5. Provide an Administrative Dashboard for oversight (viewing full user profiles, searching accounts, and approving/rejecting loans).
6. Persist all data locally using Java Object Serialization (without an external database).

---

## 📁 2. Project Folder Structure

```text
BankManagementSystem/
├── src/main/java/              
│   ├── module-info.java        
│   └── com/bank/               
│       ├── MainApp.java        
│       ├── model/              
│       │   ├── Account.java, SavingsAccount.java, CurrentAccount.java
│       │   ├── Loan.java, LoanType.java, LoanStatus.java
│       │   └── Transaction.java, TransactionType.java
│       ├── service/            
│       │   └── BankService.java
│       ├── ui/                 
│       │   ├── AccountCreatedScreen.java, AdminDashboard.java, DashboardScreen.java
│       │   ├── LoanPanel.java, LoginScreen.java, RegisterScreen.java
│       ├── exception/          
│       │   ├── BankException.java, AuthenticationException.java, InsufficientBalanceException.java
│       └── util/               
│           ├── FileHandler.java, Validator.java, IdGenerator.java
├── src/main/resources/         
│   └── styles.css              
├── data/                       
│   └── accounts.dat            
├── run.ps1                     
└── run.bat                     
```

---

## 📖 3. Exhaustive Class & Method Blueprint

### 📦 Package: `com.bank`

#### `MainApp.java`
- **Purpose**: The entry point of the JavaFX Application.
- **Concepts Used**: Inheritance (`extends Application`), Lifecycle Management.
- **Methods**:
  - `main(String[] args)`: The standard Java entry point. Calls `launch(args)` to start the JavaFX runtime.
  - `start(Stage primaryStage)`: Overridden from `Application`. Initializes the primary `Stage` (window), applies the window title, sets the global application icon, instantiates `LoginScreen`, and calls `primaryStage.show()`.

---

### 📦 Package: `com.bank.model` (Domain Layer / Data Structures)

#### `Account.java` (Abstract Class)
- **Purpose**: The core template representing a bank account.
- **Concepts Used**: **Abstraction** (abstract class forcing child classes to implement methods), **Encapsulation** (private fields, public getters/setters), **Composition** (holding Lists of other objects), **Persistence** (`implements Serializable`).
- **Data Structures**: `List<Transaction> transactions` (ArrayList), `List<Loan> loans` (ArrayList), `LocalDate`, `LocalDateTime`.
- **Instance Variables**: `accountNumber`, `accountHolderName`, `dateOfBirth`, `mobileNumber`, `address`, `aadharNumber`, `panNumber`, `username`, `password`, `balance`, `accountType`, `createdAt`, `active`, `lastInterestCreditedDate`.
- **Key Methods**:
  - `deposit(double amount)`: Increases `balance`. Throws `IllegalArgumentException` if amount <= 0.
  - `withdraw(double amount)`: Decreases `balance`. Throws `InsufficientBalanceException` if balance drops below 0.
  - `addTransaction(Transaction t)`: Appends to the internal `transactions` list.
  - `addLoan(Loan l)`: Appends to the internal `loans` list.
  - `abstract double getInterestRate()`: The core polymorphic method every child must implement.

#### `SavingsAccount.java` & `CurrentAccount.java`
- **Purpose**: Concrete implementations of the `Account` hierarchy.
- **Concepts Used**: **Inheritance** (`extends Account`), **Polymorphism** / **Method Overriding** (`@Override`).
- **Key Methods**:
  - `SavingsAccount(String name, ...)`: Constructor calling `super(...)` and setting `AccountType.SAVINGS`.
  - `getInterestRate()` (Savings): Returns `3.5` (represents 3.5% p.a. calculated monthly).
  - `getInterestRate()` (Current): Returns `2.0` (represents 2.0% p.a. calculated quarterly).

#### `Loan.java`
- **Purpose**: Model representing a single loan application.
- **Concepts Used**: **Encapsulation**, **Math Operations** (`Math.pow`).
- **Instance Variables**: `loanId`, `accountNumber`, `loanType`, `principal`, `interestRatePA`, `tenureMonths`, `status`, `appliedAt`, `approvedAt`.
- **Key Methods**:
  - `calculateEMI()`: Uses the mathematical formula `[P x R x (1+R)^N]/[(1+R)^N-1]` where P is principal, R is monthly interest rate (`interestRatePA / 12 / 100`), and N is `tenureMonths`.
  - Getters for display: `getLoanTypeDisplay()`, `getStatusDisplay()`, returning formatted strings.

#### Enums: `LoanType.java`, `LoanStatus.java`, `TransactionType.java`
- **Purpose**: Strictly defines constant states for the system to avoid magic strings.
- **Concepts Used**: **Enumerations**.
- **Values Defined**: 
  - `LoanType`: HOME, PERSONAL, BUSINESS, EDUCATION, CAR.
  - `LoanStatus`: PENDING, APPROVED, REJECTED.
  - `TransactionType`: DEPOSIT, WITHDRAWAL, TRANSFER_IN, TRANSFER_OUT, LOAN_DISBURSEMENT, INTEREST_CREDIT.

#### `Transaction.java`
- **Purpose**: Record of a single monetary movement.
- **Instance Variables**: `transactionId`, `type`, `amount`, `timestamp`, `balanceAfter`.

---

### 📦 Package: `com.bank.service` (Business Logic Layer)

#### `BankService.java`
- **Purpose**: The central processing unit of the application. All UI screens talk to this service rather than modifying objects directly.
- **Concepts Used**: **Singleton Design Pattern** (ensures single instance to manage global list state), **Exception Throwing**, **Data Caching** (in-memory list).
- **Data Structures**: `List<Account> accounts` (ArrayList).
- **Key Methods**:
  - `getInstance()`: `static` method returning the singular instance of `BankService`.
  - `createAccount(...)`: Assembles a `SavingsAccount` or `CurrentAccount`, adds it to `accounts`, and triggers `saveData()`.
  - `login(String identifier, String password)`: Iterates through accounts matching username or account number. Validates password. If successful, calls `applyInterestIfDue()` and returns the user. Throws `AuthenticationException`.
  - `applyInterestIfDue(Account acc)`: Time-based mathematical logic. Checks `lastInterestCreditedDate` vs today. If a month/quarter has passed, calculates compound interest, updates balance seamlessly, generates an `INTEREST_CREDIT` transaction, and updates the timestamp.
  - `deposit(Account acc, double amt, String pass)` / `withdraw(...)` / `transfer(...)`: Performs the financial operation on the account object. Verifies password first. Throws `BankException` variants on failure. Generates `Transaction` objects and triggers `saveData()`.
  - `searchAccount(String query)`: Returns a specific `Account` matching the query.
  - `applyForLoan(Account acc, LoanType type, double amt, int tenure)`: Creates a new `Loan` object, attaches it to the user account, and saves data.
  - `approveLoan(String loanId)` / `rejectLoan(String loanId)`: Admin actions. Iterates through all users to find the specific pending loan. On approval, changes status to `APPROVED`, sets `approvedAt` time, and natively directly deposits the loan principal into the user's account safely using an internal logic bypass.

---

### 📦 Package: `com.bank.util` (Helper Moduels)

#### `FileHandler.java`
- **Purpose**: Handles all local filesystem storage.
- **Concepts Used**: **File Header/Stream I/O**, **Object Serialization**, **Resource Management** (Try-with-Resources).
- **Methods**:
  - `saveAccounts(List<Account> accounts)`: Opens `FileOutputStream` and `ObjectOutputStream` to write the List directly to `data/accounts.dat`.
  - `loadAccounts()`: Opens `FileInputStream` and `ObjectInputStream` to read binary data. Captures `ClassNotFoundException` and `IOException`, returning a blank `ArrayList` if the file doesn't exist.
  - `exportStatement(Account acc)`: Opens `FileWriter` and `BufferedWriter` to write a human-readable text document containing the user's ledger.

#### `Validator.java`
- **Purpose**: Form validation checking.
- **Concepts Used**: **Regular Expressions (Regex)**.
- **Methods**: `isValidAadhar(String)` (checks 12 digits), `isValidPan(String)` (checks 5 letters, 4 numbers, 1 letter format), `isValidMobile(String)` (checks 10 digits).

#### `IdGenerator.java`
- **Methods**: `generateAccountNumber()` (creates random 10-digit number strings), `generateTransactionId()` (Timestamp + Random integer base), `generateLoanId()`.

---

### 📦 Package: `com.bank.exception`

- **Purpose**: Represents application-specific failure errors to provide meaningful messages to the UI.
- **Classes**: `BankException` (extends standard `Exception`), `AuthenticationException`, `InsufficientBalanceException`. 
- **Concepts Used**: **Inheritance** and Constructor Overloading (`super(message)`).

---

### 📦 Package: `com.bank.ui` (View Layer / JavaFX)

- **Global Concepts**: **Composition**, Event-Driven Programming (Event Listeners `setOnAction()`), CSS Styling. Screens utilize Layout containers like `BorderPane`, `VBox`, `HBox`, and `GridPane`.

#### `LoginScreen.java`
- **UI Structure**: Left split (branding), Right split (login form with `TextField` and `PasswordField`).
- **Methods**: `handleLogin()` (validates strings and passes to `BankService.login()`), `handleAdminLogin()` (opens a custom Dialog UI prompting for admin credentials).

#### `RegisterScreen.java`
- **UI Structure**: Heavy `GridPane` implementation for forms. Uses `DatePicker` for DOB, `ComboBox` for Account Type.
- **Methods**: `handleRegister()` (pulls data from all fields, throws visual errors on `Label` components, delegates to `BankService.createAccount()`).

#### `DashboardScreen.java`
- **UI Structure**: Master layout. Left `sidebar` acts as a navigation pane. Center `StackPane` dynamically swaps out views.
- **Panels / Sub-Methods**:
  - `buildOverviewPanel()`: Displays `balance-card` and `interest-badge`.
  - `buildDepositPanel()` / `buildWithdrawalPanel()` / `buildTransferPanel()`: Form generation generating transaction inputs demanding password validation.
  - `buildStatementPanel()`: A `TableView` directly mapped to `Account.getTransactions()`. Connects to `FileHandler` for exports.
  - `showPasswordDialog(...)`: Reusable popup requesting a password before executing financial methods asynchronously.

#### `LoanPanel.java`
- **UI Structure**: Integrated directly into `DashboardScreen`. Uses a `TabPane`.
- **Panels**:
  - **Apply for Loan**: Dynamic layout triggering `calculateEMI()` live using `textProperty().addListener((observable, oldValue, newValue) -> ...)` updates. 
  - **My Loans**: A `TableView` uniquely mapping the active user's `List<Loan>` array displaying EMI status.

#### `AdminDashboard.java`
- **UI Structure**: Protected master layout. Includes unique `admin-nav-bar`.
- **Panels / Sub-Methods**:
  - `buildAllAccountsPanel()`: Displays high-level data of all registered users in a `TableView`. Contains a custom `TableCell` rendering a "View Profile" button to launch `showUserProfileDialog()`.
  - `showUserProfileDialog(Account)`: Opens a `GridPane` modal displaying full KYC parameters (DOB, Address, Aadhar, PAN, Active Loans).
  - `buildSearchPanel()`: Text field searching through the `BankService.searchAccount()` map in O(n) rendering an `info-grid`.
  - `buildPendingLoansPanel()`: `TableView` containing dynamic action nodes (`Approve` / `Reject` buttons) modifying underlying Loan object states on click.
  - `buildAllLoansPanel()`: Global loan ledger tracking statuses.

---

## 🔁 4. Complete Application Activity Flow

1. **Boot**: `MainApp` boots global UI. `BankService` inherently loads the `List<Account>` globally from `.dat`.
2. **Registration Cycle**: User submits data `->` `Validator` checks constraints `->` `BankService` creates `Account` wrapper `->` `Account` pushed to global List `->` `FileHandler` overwrites `.dat` file natively.
3. **Session Cycle**: User provides credentials `->` `BankService` validates `->` Iterates `applyInterestIfDue` checking chronologies `->` Updates account balance natively internally via calculations `->` Successful return swaps Scene to `DashboardScreen`.
4. **Transaction Cycle**: User provides amount + target `->` Provides password to UI Dialog `->` `BankService` executes polymorphic math (`withdraw/deposit`) `->` System binds a `Transaction` payload to the user `->` Immediately pushes overwrite via `Serialization` ensuring atomicity.
5. **Loan / Admin Cycle**: User creates `Loan` locally mapped to `List<Loan>` `->` `BankService` saves application globally `->` Admin authenticates natively parsing static keys `->` Admin approves `->` `BankService` flips enumerated state to `APPROVED`, calculates principal displacement, updates the target user's root balance directly `->` Triggers final atomic save.

---

_This architecture strictly guarantees highly decoupled operations scaling cleanly between View, Controller (Service), and Data Model layers enforcing pure Object Oriented architectures corresponding to CO1 through CO5 matrices._
