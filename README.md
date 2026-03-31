# 🏦 BMS Bank Management System

A full-featured **JavaFX desktop banking application** built with Java 17+ and JavaFX 21.

## ✨ Features

### User Features
- **Account Registration** — Full KYC (Name, DOB, Aadhar, PAN, Mobile, Address)
- **Secure Login** — by username or account number
- **Password-Protected Transactions** — Deposit, Withdraw, Transfer all require password confirmation
- **Fund Transfer** — Between any two active accounts
- **Transaction History** — Full ledger with timestamps
- **Bank Statement Export** — Saved as a `.txt` file
- **Loan Module** — Apply for Home, Personal, Business, Education, or Car loans
  - Real-time EMI preview while filling the form
  - View all your loans with status and next due date
- **Automatic Interest Accrual**
  - Savings: 3.5% p.a., credited monthly
  - Current: 2.0% p.a., credited quarterly

### Admin Features
- **Admin Login** — Hardcoded credentials (`admin` / `admin123`) for demo
- **All Accounts** — View every account with balance and status
- **Search Account** — O(1) lookup by account number or username
- **Pending Loans** — Approve or Reject loan applications in one click
- **All Loans** — Full loan history across all accounts

## 🎨 UI Theme
Clean, professional **Crimson & Amber** theme inspired by ICICI / Kotak Mahindra banking UIs:
- White surfaces with neutral charcoal text
- Deep red → orange gradient balance card
- Crimson active states and action buttons

## 🛠️ Tech Stack

| Tech | Version |
|------|---------|
| Java | 17+ |
| JavaFX | 21 |
| Persistence | Java Object Serialization |
| Build | Manual `javac` + PowerShell script |

## 🚀 How to Run

### Prerequisites
- Java 17 or newer installed
- JavaFX SDK 21 downloaded and placed at `C:\Javafx\javafx-sdk-21.0.10\`
  - Download from: https://gluonhq.com/products/javafx/

### Run
```powershell
# Windows (PowerShell)
.\run.ps1

# Windows (Command Prompt)
run.bat
```

The script compiles all `.java` sources and launches the application automatically.

## 📁 Project Structure

```
BankManagementSystem/
├── src/main/java/
│   ├── module-info.java
│   └── com/bank/
│       ├── MainApp.java
│       ├── model/          # Account, Loan, Transaction, enums
│       ├── service/        # BankService (business logic)
│       ├── ui/             # JavaFX screens
│       ├── exception/      # Custom exceptions
│       └── util/           # FileHandler, Validator
├── src/main/resources/
│   └── styles.css          # Full UI theme
├── data/                   # Serialized accounts (auto-created, git-ignored)
├── run.ps1                 # PowerShell build & launch
└── run.bat                 # Batch build & launch
```

## 💡 Account Types

| Type    | Min Balance | Daily Limit | Interest |
|---------|------------|-------------|---------|
| Savings | ₹1,000     | ₹25,000     | 3.5% p.a. (monthly) |
| Current | ₹5,000     | ₹1,00,000   | 2.0% p.a. (quarterly) |

## 🏦 Loan Types

| Loan Type  | Interest Rate | Max Tenure |
|------------|---------------|------------|
| Home       | 8.5% p.a.     | 240 months |
| Personal   | 14.0% p.a.    | 60 months  |
| Business   | 11.5% p.a.    | 120 months |
| Education  | 9.0% p.a.     | 96 months  |
| Car        | 10.0% p.a.    | 84 months  |

EMI formula: `EMI = P × r × (1+r)ⁿ / ((1+r)ⁿ - 1)`

---

> **Note:** This is a demo/educational project. Admin credentials are hardcoded. Do not use for real banking purposes.
