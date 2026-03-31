package com.bank.ui;

import com.bank.exception.BankException;
import com.bank.model.Account;
import com.bank.model.Loan;
import com.bank.model.Transaction;
import com.bank.service.BankService;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.List;

// CO4: JavaFX - Main Dashboard Screen
public class DashboardScreen {

    private final BorderPane root;
    private final Stage stage;
    private Account account;
    private final BankService bankService = BankService.getInstance();

    private Label balanceLabel;
    private Label messageLabel;

    public DashboardScreen(Stage stage, Account account) {
        this.stage = stage;
        this.account = account;
        this.root = new BorderPane();
        buildUI();
    }

    private void buildUI() {
        root.getStyleClass().add("screen-bg");

        // ── Top Nav Bar ──
        HBox navBar = new HBox();
        navBar.getStyleClass().add("nav-bar");
        navBar.setAlignment(Pos.CENTER_LEFT);
        navBar.setPadding(new Insets(14, 24, 14, 24));
        navBar.setSpacing(16);

        Label logo = new Label("🏦 BMS Bank");
        logo.getStyleClass().add("nav-logo");
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label userLabel = new Label("👤  " + account.getAccountHolderName());
        userLabel.getStyleClass().add("nav-user");

        Button logoutBtn = new Button("Logout");
        logoutBtn.getStyleClass().add("logout-btn");
        logoutBtn.setOnAction(e -> {
            LoginScreen loginScreen = new LoginScreen(stage);
            stage.getScene().setRoot(loginScreen.getRoot());
        });

        navBar.getChildren().addAll(logo, spacer, userLabel, logoutBtn);

        // ── Left Sidebar ──
        VBox sidebar = new VBox(8);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPadding(new Insets(24, 16, 24, 16));
        sidebar.setPrefWidth(200);

        Label menuTitle = new Label("MENU");
        menuTitle.getStyleClass().add("sidebar-section-label");

        String[] menuItems = {
                "🏠  Overview", "💰  Deposit", "💸  Withdraw",
                "🔁  Transfer", "📋  Transactions", "📄  Statement",
                "🏦  My Loans", "❌  Close Account"
        };

        ToggleGroup toggleGroup = new ToggleGroup();

        // Content area
        StackPane contentArea = new StackPane();
        contentArea.getStyleClass().add("content-area");

        // Message label (global, shown at top of content)
        messageLabel = new Label();
        messageLabel.setWrapText(true);
        messageLabel.setVisible(false);

        sidebar.getChildren().add(menuTitle);

        for (int i = 0; i < menuItems.length; i++) {
            ToggleButton btn = new ToggleButton(menuItems[i]);
            btn.setToggleGroup(toggleGroup);
            btn.getStyleClass().add("sidebar-btn");
            btn.setMaxWidth(Double.MAX_VALUE);
            final int index = i;
            btn.setOnAction(e -> switchPanel(index, contentArea));
            if (i == 0) {
                btn.setSelected(true);
            }
            sidebar.getChildren().add(btn);
        }

        // Default: show overview
        contentArea.getChildren().add(buildOverviewPanel());

        // ── Layout ──
        BorderPane mainLayout = new BorderPane();
        mainLayout.setLeft(sidebar);
        mainLayout.setCenter(contentArea);

        root.setTop(navBar);
        root.setCenter(mainLayout);
    }

    private void switchPanel(int index, StackPane contentArea) {
        contentArea.getChildren().clear();
        switch (index) {
            case 0 -> contentArea.getChildren().add(buildOverviewPanel());
            case 1 -> contentArea.getChildren().add(buildDepositPanel());
            case 2 -> contentArea.getChildren().add(buildWithdrawPanel());
            case 3 -> contentArea.getChildren().add(buildTransferPanel());
            case 4 -> contentArea.getChildren().add(buildTransactionsPanel());
            case 5 -> contentArea.getChildren().add(buildStatementPanel());
            case 6 -> contentArea.getChildren().add(new LoanPanel(account, bankService).getRoot());
            case 7 -> contentArea.getChildren().add(buildCloseAccountPanel());
        }
    }

    // ─────────────────────────────────────────────
    // Overview Panel
    // ─────────────────────────────────────────────
    private VBox buildOverviewPanel() {
        refreshAccount();
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(30));

        Label title = new Label("Account Overview");
        title.getStyleClass().add("panel-title");

        // Balance card
        VBox balanceCard = new VBox(8);
        balanceCard.getStyleClass().add("balance-card");
        balanceCard.setPadding(new Insets(24));
        balanceCard.setAlignment(Pos.CENTER_LEFT);

        Label balanceTitle = new Label("Current Balance");
        balanceTitle.getStyleClass().add("balance-card-label");
        balanceLabel = new Label("₹ " + String.format("%.2f", account.getBalance()));
        balanceLabel.getStyleClass().add("balance-amount");
        Label minNote = new Label("Minimum balance: ₹" + String.format("%.2f", account.getMinimumBalance()));
        minNote.getStyleClass().add("mini-note");

        // Interest rate badge
        Label interestBadge = new Label("📈 " + String.format("%.1f", account.getInterestRate()) + "% p.a. interest");
        interestBadge.getStyleClass().add("interest-badge");

        balanceCard.getChildren().addAll(balanceTitle, balanceLabel, minNote, interestBadge);

        // Info grid
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(40);
        infoGrid.setVgap(12);
        infoGrid.getStyleClass().add("info-grid");
        infoGrid.setPadding(new Insets(20));

        addInfoRow(infoGrid, "Account Number", account.getAccountNumber(), 0, 0);
        addInfoRow(infoGrid, "Account Type", account.getAccountType().getDisplayName(), 0, 1);
        addInfoRow(infoGrid, "Account Holder", account.getAccountHolderName(), 1, 0);
        addInfoRow(infoGrid, "Mobile", account.getMobileNumber(), 1, 1);
        addInfoRow(infoGrid, "Username", account.getUsername(), 2, 0);
        addInfoRow(infoGrid, "Opened On",
                account.getCreatedAt().toLocalDate().toString(), 2, 1);
        addInfoRow(infoGrid, "Daily Withdrawal Limit",
                "₹" + String.format("%.2f", account.getDailyWithdrawalLimit()), 3, 0);
        String lastInterest = account.getLastInterestCreditedDate() != null
                ? account.getLastInterestCreditedDate().toString() : "Not yet credited";
        addInfoRow(infoGrid, "Last Interest Credited", lastInterest, 3, 1);

        panel.getChildren().addAll(title, balanceCard, infoGrid);
        return panel;
    }

    private void addInfoRow(GridPane grid, String key, String value, int row, int col) {
        VBox box = new VBox(2);
        Label k = new Label(key);
        k.getStyleClass().add("info-key");
        Label v = new Label(value);
        v.getStyleClass().add("info-value");
        box.getChildren().addAll(k, v);
        grid.add(box, col, row);
    }

    // ─────────────────────────────────────────────
    // Deposit Panel — password protected
    // ─────────────────────────────────────────────
    private VBox buildDepositPanel() {
        VBox panel = buildOperationPanel("Deposit Money", "💰");

        TextField amountField = new TextField();
        amountField.setPromptText("Enter amount to deposit");
        amountField.getStyleClass().add("text-field-custom");
        amountField.setMaxWidth(320);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Confirm your account password");
        passwordField.getStyleClass().add("text-field-custom");
        passwordField.setMaxWidth(320);

        Label resultLabel = new Label();
        resultLabel.setWrapText(true);

        Button depositBtn = new Button("Deposit →");
        depositBtn.getStyleClass().add("primary-btn");

        depositBtn.setOnAction(e -> {
            try {
                double amount = Double.parseDouble(amountField.getText().trim());
                String password = passwordField.getText();
                bankService.deposit(account.getAccountNumber(), amount, password);
                refreshAccount();
                showSuccess(resultLabel, "✓ Deposited ₹" + String.format("%.2f", amount) +
                        " successfully! New Balance: ₹" + String.format("%.2f", account.getBalance()));
                amountField.clear();
                passwordField.clear();
            } catch (NumberFormatException ex) {
                showError(resultLabel, "✗ Please enter a valid amount.");
            } catch (BankException ex) {
                showError(resultLabel, "✗ " + ex.getMessage());
            }
        });

        panel.getChildren().addAll(
                styledNote("Minimum deposit: ₹1"),
                amountField,
                labeledNode("Account Password", passwordField),
                depositBtn, resultLabel
        );
        return panel;
    }

    // ─────────────────────────────────────────────
    // Withdraw Panel — password protected
    // ─────────────────────────────────────────────
    private VBox buildWithdrawPanel() {
        VBox panel = buildOperationPanel("Withdraw Money", "💸");

        TextField amountField = new TextField();
        amountField.setPromptText("Enter amount to withdraw");
        amountField.getStyleClass().add("text-field-custom");
        amountField.setMaxWidth(320);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Confirm your account password");
        passwordField.getStyleClass().add("text-field-custom");
        passwordField.setMaxWidth(320);

        Label resultLabel = new Label();
        resultLabel.setWrapText(true);

        Button withdrawBtn = new Button("Withdraw →");
        withdrawBtn.getStyleClass().add("primary-btn");

        withdrawBtn.setOnAction(e -> {
            try {
                double amount = Double.parseDouble(amountField.getText().trim());
                String password = passwordField.getText();
                bankService.withdraw(account.getAccountNumber(), amount, password);
                refreshAccount();
                showSuccess(resultLabel, "✓ Withdrawn ₹" + String.format("%.2f", amount) +
                        " successfully! New Balance: ₹" + String.format("%.2f", account.getBalance()));
                amountField.clear();
                passwordField.clear();
            } catch (NumberFormatException ex) {
                showError(resultLabel, "✗ Please enter a valid amount.");
            } catch (BankException ex) {
                showError(resultLabel, "✗ " + ex.getMessage());
            }
        });

        panel.getChildren().addAll(
                styledNote("Daily limit: ₹" + String.format("%.2f", account.getDailyWithdrawalLimit())
                        + "  |  Min balance: ₹" + String.format("%.2f", account.getMinimumBalance())),
                amountField,
                labeledNode("Account Password", passwordField),
                withdrawBtn, resultLabel
        );
        return panel;
    }

    // ─────────────────────────────────────────────
    // Transfer Panel — password protected
    // ─────────────────────────────────────────────
    private VBox buildTransferPanel() {
        VBox panel = buildOperationPanel("Transfer Funds", "🔁");

        TextField toAccField = new TextField();
        toAccField.setPromptText("Recipient Account Number (e.g. BMS1234567890)");
        toAccField.getStyleClass().add("text-field-custom");
        toAccField.setMaxWidth(360);

        TextField amountField = new TextField();
        amountField.setPromptText("Enter amount to transfer");
        amountField.getStyleClass().add("text-field-custom");
        amountField.setMaxWidth(360);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Confirm your account password");
        passwordField.getStyleClass().add("text-field-custom");
        passwordField.setMaxWidth(360);

        Label resultLabel = new Label();
        resultLabel.setWrapText(true);

        Button transferBtn = new Button("Transfer →");
        transferBtn.getStyleClass().add("primary-btn");

        transferBtn.setOnAction(e -> {
            try {
                String toAcc = toAccField.getText().trim();
                double amount = Double.parseDouble(amountField.getText().trim());
                String password = passwordField.getText();
                bankService.transfer(account.getAccountNumber(), toAcc, amount, password);
                refreshAccount();
                showSuccess(resultLabel, "✓ Transferred ₹" + String.format("%.2f", amount) +
                        " to " + toAcc + ". New Balance: ₹" + String.format("%.2f", account.getBalance()));
                toAccField.clear();
                amountField.clear();
                passwordField.clear();
            } catch (NumberFormatException ex) {
                showError(resultLabel, "✗ Please enter a valid amount.");
            } catch (BankException ex) {
                showError(resultLabel, "✗ " + ex.getMessage());
            }
        });

        VBox fields = new VBox(12,
                labeledNode("Recipient Account Number", toAccField),
                labeledNode("Amount (₹)", amountField),
                labeledNode("Account Password", passwordField)
        );

        panel.getChildren().addAll(
                styledNote("Ensure the recipient account number is correct before transferring."),
                fields, transferBtn, resultLabel
        );
        return panel;
    }

    // ─────────────────────────────────────────────
    // Transactions Panel
    // ─────────────────────────────────────────────
    @SuppressWarnings("unchecked")
    private VBox buildTransactionsPanel() {
        VBox panel = new VBox(16);
        panel.setPadding(new Insets(30));

        Label title = new Label("Transaction History");
        title.getStyleClass().add("panel-title");

        refreshAccount();
        List<Transaction> transactions = account.getTransactionHistory();

        if (transactions.isEmpty()) {
            Label empty = new Label("No transactions found.");
            empty.getStyleClass().add("empty-label");
            panel.getChildren().addAll(title, empty);
            return panel;
        }

        // CO5: TableView with generics
        TableView<Transaction> table = new TableView<>();
        table.getStyleClass().add("table-custom");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Transaction, String> idCol = new TableColumn<>("Transaction ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("transactionId"));

        TableColumn<Transaction, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));

        TableColumn<Transaction, Double> amountCol = new TableColumn<>("Amount (₹)");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        amountCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); } 
                else { setText(String.format("%.2f", item)); }
            }
        });

        TableColumn<Transaction, Double> balAfterCol = new TableColumn<>("Balance After (₹)");
        balAfterCol.setCellValueFactory(new PropertyValueFactory<>("balanceAfter"));
        balAfterCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); }
                else { setText(String.format("%.2f", item)); }
            }
        });

        TableColumn<Transaction, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));

        TableColumn<Transaction, String> dateCol = new TableColumn<>("Date & Time");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("formattedTimestamp"));

        table.getColumns().addAll(idCol, typeCol, amountCol, balAfterCol, descCol, dateCol);

        // Reverse to show newest first
        List<Transaction> reversed = new java.util.ArrayList<>(transactions);
        java.util.Collections.reverse(reversed);
        table.setItems(FXCollections.observableArrayList(reversed));
        VBox.setVgrow(table, Priority.ALWAYS);

        panel.getChildren().addAll(title, table);
        return panel;
    }

    // ─────────────────────────────────────────────
    // Statement Panel
    // ─────────────────────────────────────────────
    private VBox buildStatementPanel() {
        VBox panel = buildOperationPanel("Download Bank Statement", "📄");

        Label infoLabel = new Label(
                "Your bank statement will be exported as a text file in the /data/ folder. " +
                "It will contain your complete transaction history and account details."
        );
        infoLabel.setWrapText(true);
        infoLabel.getStyleClass().add("info-text");
        infoLabel.setMaxWidth(440);

        Label resultLabel = new Label();
        resultLabel.setWrapText(true);

        Button exportBtn = new Button("📄  Export Statement");
        exportBtn.getStyleClass().add("primary-btn");

        exportBtn.setOnAction(e -> {
            try {
                String filePath = bankService.exportStatement(account.getAccountNumber());
                showSuccess(resultLabel, "✓ Statement saved to: " + filePath);
            } catch (BankException ex) {
                showError(resultLabel, "✗ " + ex.getMessage());
            }
        });

        panel.getChildren().addAll(infoLabel, exportBtn, resultLabel);
        return panel;
    }

    // ─────────────────────────────────────────────
    // Close Account Panel
    // ─────────────────────────────────────────────
    private VBox buildCloseAccountPanel() {
        VBox panel = buildOperationPanel("Close Account", "❌");

        Label warningLabel = new Label(
                "⚠ Warning: Closing your account is permanent and cannot be undone. " +
                "Please ensure you have withdrawn all your funds before proceeding."
        );
        warningLabel.setWrapText(true);
        warningLabel.getStyleClass().add("warning-label");
        warningLabel.setMaxWidth(480);

        Label resultLabel = new Label();
        resultLabel.setWrapText(true);

        Button closeBtn = new Button("❌  Close My Account");
        closeBtn.getStyleClass().add("danger-btn");

        closeBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirm Account Closure");
            confirm.setHeaderText("Are you sure you want to close your account?");
            confirm.setContentText("This action is permanent and cannot be reversed.");
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        bankService.closeAccount(account.getAccountNumber());
                        LoginScreen loginScreen = new LoginScreen(stage);
                        stage.getScene().setRoot(loginScreen.getRoot());
                    } catch (BankException ex) {
                        showError(resultLabel, "✗ " + ex.getMessage());
                    }
                }
            });
        });

        panel.getChildren().addAll(warningLabel, closeBtn, resultLabel);
        return panel;
    }

    // ─────────────────────────────────────────────
    // Helper Builders
    // ─────────────────────────────────────────────
    private VBox buildOperationPanel(String title, String icon) {
        VBox panel = new VBox(18);
        panel.setPadding(new Insets(30));

        HBox titleRow = new HBox(10);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 24px;");
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("panel-title");
        titleRow.getChildren().addAll(iconLabel, titleLabel);

        panel.getChildren().add(titleRow);
        return panel;
    }

    private Label styledNote(String text) {
        Label note = new Label(text);
        note.getStyleClass().add("note-label");
        note.setWrapText(true);
        return note;
    }

    private VBox labeledNode(String labelText, javafx.scene.Node field) {
        VBox box = new VBox(5);
        Label label = new Label(labelText);
        label.getStyleClass().add("field-label");
        box.getChildren().addAll(label, field);
        return box;
    }

    private void showSuccess(Label label, String message) {
        label.getStyleClass().removeAll("error-label");
        if (!label.getStyleClass().contains("success-label"))
            label.getStyleClass().add("success-label");
        label.setText(message);
    }

    private void showError(Label label, String message) {
        label.getStyleClass().removeAll("success-label");
        if (!label.getStyleClass().contains("error-label"))
            label.getStyleClass().add("error-label");
        label.setText(message);
    }

    private void refreshAccount() {
        try {
            account = bankService.getAccount(account.getAccountNumber());
            if (balanceLabel != null) {
                balanceLabel.setText("₹ " + String.format("%.2f", account.getBalance()));
            }
        } catch (BankException ignored) {}
    }

    public BorderPane getRoot() { return root; }
}
