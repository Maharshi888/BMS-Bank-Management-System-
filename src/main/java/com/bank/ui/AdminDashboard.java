package com.bank.ui;

import com.bank.exception.BankException;
import com.bank.model.Account;
import com.bank.model.Loan;
import com.bank.service.BankService;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.List;

/**
 * Admin Dashboard — accessible via the admin login on LoginScreen.
 * Panels: All Accounts | Search Account | Pending Loans | All Loans
 */
public class AdminDashboard {

    private final BorderPane root;
    private final Stage stage;
    private final BankService bankService = BankService.getInstance();

    public AdminDashboard(Stage stage) {
        this.stage = stage;
        this.root = new BorderPane();
        buildUI();
    }

    private void buildUI() {
        root.getStyleClass().add("screen-bg");

        // ── Admin Nav Bar ──
        HBox navBar = new HBox();
        navBar.getStyleClass().addAll("nav-bar", "admin-nav-bar");
        navBar.setAlignment(Pos.CENTER_LEFT);
        navBar.setPadding(new Insets(14, 24, 14, 24));
        navBar.setSpacing(16);

        Label logo = new Label("🏦 BMS Bank");
        logo.getStyleClass().add("nav-logo");

        Label adminBadge = new Label("🔑 ADMINISTRATOR");
        adminBadge.getStyleClass().add("admin-badge");

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button logoutBtn = new Button("Logout");
        logoutBtn.getStyleClass().add("logout-btn");
        logoutBtn.setOnAction(e -> {
            LoginScreen loginScreen = new LoginScreen(stage);
            stage.getScene().setRoot(loginScreen.getRoot());
        });

        navBar.getChildren().addAll(logo, adminBadge, spacer, logoutBtn);

        // ── Sidebar ──
        VBox sidebar = new VBox(8);
        sidebar.getStyleClass().addAll("sidebar", "admin-sidebar");
        sidebar.setPadding(new Insets(24, 16, 24, 16));
        sidebar.setPrefWidth(210);

        Label menuTitle = new Label("ADMIN MENU");
        menuTitle.getStyleClass().add("sidebar-section-label");

        String[] menuItems = {
                "👥  All Accounts",
                "🔍  Search Account",
                "⏳  Pending Loans",
                "📋  All Loans"
        };

        ToggleGroup toggleGroup = new ToggleGroup();
        StackPane contentArea = new StackPane();
        contentArea.getStyleClass().add("content-area");

        sidebar.getChildren().add(menuTitle);

        for (int i = 0; i < menuItems.length; i++) {
            ToggleButton btn = new ToggleButton(menuItems[i]);
            btn.setToggleGroup(toggleGroup);
            btn.getStyleClass().add("sidebar-btn");
            btn.setMaxWidth(Double.MAX_VALUE);
            final int idx = i;
            btn.setOnAction(e -> switchPanel(idx, contentArea));
            if (i == 0) btn.setSelected(true);
            sidebar.getChildren().add(btn);
        }

        contentArea.getChildren().add(buildAllAccountsPanel());

        BorderPane mainLayout = new BorderPane();
        mainLayout.setLeft(sidebar);
        mainLayout.setCenter(contentArea);

        root.setTop(navBar);
        root.setCenter(mainLayout);
    }

    private void switchPanel(int idx, StackPane contentArea) {
        contentArea.getChildren().clear();
        switch (idx) {
            case 0 -> contentArea.getChildren().add(buildAllAccountsPanel());
            case 1 -> contentArea.getChildren().add(buildSearchPanel());
            case 2 -> contentArea.getChildren().add(buildPendingLoansPanel());
            case 3 -> contentArea.getChildren().add(buildAllLoansPanel());
        }
    }

    // ─────────────────────────────────────────────
    // All Accounts Panel
    // ─────────────────────────────────────────────
    @SuppressWarnings("unchecked")
    private VBox buildAllAccountsPanel() {
        VBox panel = new VBox(16);
        panel.setPadding(new Insets(30));

        Label title = new Label("👥  All Accounts");
        title.getStyleClass().add("panel-title");

        List<Account> accounts = bankService.getAllAccounts();

        TableView<Account> table = new TableView<>();
        table.getStyleClass().add("table-custom");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Account, String> numCol = new TableColumn<>("Account No.");
        numCol.setCellValueFactory(new PropertyValueFactory<>("accountNumber"));

        TableColumn<Account, String> nameCol = new TableColumn<>("Account Holder");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("accountHolderName"));

        TableColumn<Account, String> userCol = new TableColumn<>("Username");
        userCol.setCellValueFactory(new PropertyValueFactory<>("username"));

        TableColumn<Account, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("accountType"));

        TableColumn<Account, Double> balCol = new TableColumn<>("Balance (₹)");
        balCol.setCellValueFactory(new PropertyValueFactory<>("balance"));
        balCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : String.format("%.2f", item));
            }
        });

        TableColumn<Account, Boolean> activeCol = new TableColumn<>("Status");
        activeCol.setCellValueFactory(new PropertyValueFactory<>("active"));
        activeCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); return; }
                setText(item ? "Active" : "Closed");
                setStyle(item ? "-fx-text-fill: #2ecc71;" : "-fx-text-fill: #e74c3c;");
            }
        });

        table.getColumns().addAll(numCol, nameCol, userCol, typeCol, balCol, activeCol);
        table.setItems(FXCollections.observableArrayList(accounts));

        Label countLabel = new Label("Total accounts: " + accounts.size());
        countLabel.getStyleClass().add("note-label");

        panel.getChildren().addAll(title, countLabel, table);
        return panel;
    }

    // ─────────────────────────────────────────────
    // Search Account Panel
    // ─────────────────────────────────────────────
    private VBox buildSearchPanel() {
        VBox panel = new VBox(16);
        panel.setPadding(new Insets(30));

        Label title = new Label("🔍  Search Account");
        title.getStyleClass().add("panel-title");

        TextField searchField = new TextField();
        searchField.setPromptText("Enter account number or username...");
        searchField.getStyleClass().add("text-field-custom");
        searchField.setMaxWidth(420);

        Button searchBtn = new Button("Search →");
        searchBtn.getStyleClass().add("primary-btn");

        VBox resultBox = new VBox(12);
        resultBox.getStyleClass().add("info-grid");
        resultBox.setPadding(new Insets(16));
        resultBox.setVisible(false);

        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("error-label");
        errorLabel.setWrapText(true);

        searchBtn.setOnAction(e -> {
            String query = searchField.getText().trim();
            errorLabel.setText("");
            resultBox.setVisible(false);
            resultBox.getChildren().clear();

            if (query.isEmpty()) {
                errorLabel.setText("✗ Please enter a search query.");
                return;
            }

            Account found = bankService.searchAccount(query);
            if (found == null) {
                errorLabel.setText("✗ No account found matching: " + query);
                return;
            }

            // Show account card
            GridPane grid = new GridPane();
            grid.setHgap(32);
            grid.setVgap(10);

            addRow(grid, "Account Number", found.getAccountNumber(), 0, 0);
            addRow(grid, "Account Holder", found.getAccountHolderName(), 0, 1);
            addRow(grid, "Username", found.getUsername(), 1, 0);
            addRow(grid, "Account Type", found.getAccountType().getDisplayName(), 1, 1);
            addRow(grid, "Balance (₹)", String.format("%.2f", found.getBalance()), 2, 0);
            addRow(grid, "Status", found.isActive() ? "Active" : "Closed", 2, 1);
            addRow(grid, "Mobile", found.getMobileNumber(), 3, 0);
            addRow(grid, "Opened On", found.getCreatedAt().toLocalDate().toString(), 3, 1);
            addRow(grid, "Aadhar", found.getAadharNumber(), 4, 0);
            addRow(grid, "PAN", found.getPanNumber(), 4, 1);
            addRow(grid, "Number of Loans", String.valueOf(found.getLoans().size()), 5, 0);
            addRow(grid, "Interest Rate", found.getInterestRate() + "% p.a.", 5, 1);

            resultBox.getChildren().add(grid);
            resultBox.setVisible(true);
        });

        searchField.setOnAction(e -> searchBtn.fire());

        panel.getChildren().addAll(title,
                new HBox(12, searchField, searchBtn),
                errorLabel, resultBox);
        return panel;
    }

    private void addRow(GridPane grid, String key, String value, int row, int col) {
        VBox box = new VBox(2);
        Label k = new Label(key);
        k.getStyleClass().add("info-key");
        Label v = new Label(value);
        v.getStyleClass().add("info-value");
        box.getChildren().addAll(k, v);
        grid.add(box, col, row);
    }

    // ─────────────────────────────────────────────
    // Pending Loans Panel
    // ─────────────────────────────────────────────
    @SuppressWarnings("unchecked")
    private VBox buildPendingLoansPanel() {
        VBox panel = new VBox(16);
        panel.setPadding(new Insets(30));

        Label title = new Label("⏳  Pending Loan Applications");
        title.getStyleClass().add("panel-title");

        Label statusMsg = new Label();
        statusMsg.setWrapText(true);

        List<Loan> pending = bankService.getPendingLoans();

        if (pending.isEmpty()) {
            Label empty = new Label("✓ No pending loan applications.");
            empty.getStyleClass().add("success-label");
            panel.getChildren().addAll(title, empty);
            return panel;
        }

        // Mutable list backed by observable so approve/reject can remove
        javafx.collections.ObservableList<Loan> pendingObs = FXCollections.observableArrayList(pending);

        TableView<Loan> table = new TableView<>(pendingObs);
        table.getStyleClass().add("table-custom");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Loan, String> idCol = new TableColumn<>("Loan ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("loanId"));

        TableColumn<Loan, String> accCol = new TableColumn<>("Account No.");
        accCol.setCellValueFactory(new PropertyValueFactory<>("accountNumber"));

        TableColumn<Loan, String> typeCol = new TableColumn<>("Loan Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("loanTypeDisplay"));

        TableColumn<Loan, Double> amtCol = new TableColumn<>("Amount (₹)");
        amtCol.setCellValueFactory(new PropertyValueFactory<>("principal"));
        amtCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : String.format("%.2f", item));
            }
        });

        TableColumn<Loan, Double> rateCol = new TableColumn<>("Rate");
        rateCol.setCellValueFactory(new PropertyValueFactory<>("interestRatePA"));
        rateCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : item + "%");
            }
        });

        TableColumn<Loan, Integer> tenureCol = new TableColumn<>("Tenure");
        tenureCol.setCellValueFactory(new PropertyValueFactory<>("tenureMonths"));

        TableColumn<Loan, String> appliedCol = new TableColumn<>("Applied On");
        appliedCol.setCellValueFactory(new PropertyValueFactory<>("appliedAtDisplay"));

        // Approve / Reject action column
        TableColumn<Loan, Void> actionCol = new TableColumn<>("Action");
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button approveBtn = new Button("✓ Approve");
            private final Button rejectBtn = new Button("✗ Reject");
            private final HBox btnBox = new HBox(8, approveBtn, rejectBtn);

            {
                approveBtn.getStyleClass().add("approve-btn");
                rejectBtn.getStyleClass().add("reject-btn");
                btnBox.setAlignment(Pos.CENTER);

                approveBtn.setOnAction(e -> {
                    Loan loan = getTableRow().getItem();
                    if (loan == null) return;
                    try {
                        bankService.approveLoan(loan.getLoanId());
                        pendingObs.remove(loan);
                        showStatus(statusMsg, "✓ Loan " + loan.getLoanId() + " approved and ₹"
                                + String.format("%.2f", loan.getPrincipal()) + " disbursed.", true);
                    } catch (BankException ex) {
                        showStatus(statusMsg, "✗ " + ex.getMessage(), false);
                    }
                });

                rejectBtn.setOnAction(e -> {
                    Loan loan = getTableRow().getItem();
                    if (loan == null) return;
                    try {
                        bankService.rejectLoan(loan.getLoanId());
                        pendingObs.remove(loan);
                        showStatus(statusMsg, "Loan " + loan.getLoanId() + " rejected.", true);
                    } catch (BankException ex) {
                        showStatus(statusMsg, "✗ " + ex.getMessage(), false);
                    }
                });
            }

            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnBox);
            }
        });

        table.getColumns().addAll(idCol, accCol, typeCol, amtCol, rateCol, tenureCol, appliedCol, actionCol);

        panel.getChildren().addAll(title, statusMsg, table);
        return panel;
    }

    private void showStatus(Label label, String msg, boolean success) {
        if (success) {
            label.getStyleClass().removeAll("error-label");
            if (!label.getStyleClass().contains("success-label"))
                label.getStyleClass().add("success-label");
        } else {
            label.getStyleClass().removeAll("success-label");
            if (!label.getStyleClass().contains("error-label"))
                label.getStyleClass().add("error-label");
        }
        label.setText(msg);
    }

    // ─────────────────────────────────────────────
    // All Loans Panel
    // ─────────────────────────────────────────────
    @SuppressWarnings("unchecked")
    private VBox buildAllLoansPanel() {
        VBox panel = new VBox(16);
        panel.setPadding(new Insets(30));

        Label title = new Label("📋  All Loan Applications");
        title.getStyleClass().add("panel-title");

        List<Loan> allLoans = bankService.getAllLoans();

        if (allLoans.isEmpty()) {
            Label empty = new Label("No loan applications found.");
            empty.getStyleClass().add("empty-label");
            panel.getChildren().addAll(title, empty);
            return panel;
        }

        TableView<Loan> table = new TableView<>();
        table.getStyleClass().add("table-custom");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Loan, String> idCol = new TableColumn<>("Loan ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("loanId"));

        TableColumn<Loan, String> accCol = new TableColumn<>("Account No.");
        accCol.setCellValueFactory(new PropertyValueFactory<>("accountNumber"));

        TableColumn<Loan, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("loanTypeDisplay"));

        TableColumn<Loan, Double> amtCol = new TableColumn<>("Amount (₹)");
        amtCol.setCellValueFactory(new PropertyValueFactory<>("principal"));
        amtCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : String.format("%.2f", item));
            }
        });

        TableColumn<Loan, Double> emiCol = new TableColumn<>("EMI (₹)");
        emiCol.setCellValueFactory(new PropertyValueFactory<>("emiAmount"));
        emiCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null || item == 0) ? "-" : String.format("%.2f", item));
            }
        });

        TableColumn<Loan, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("statusDisplay"));
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); getStyleClass().removeAll("loan-pending","loan-approved","loan-rejected"); return; }
                setText(item);
                getStyleClass().removeAll("loan-pending","loan-approved","loan-rejected");
                switch (item) {
                    case "Pending"  -> getStyleClass().add("loan-pending");
                    case "Approved" -> getStyleClass().add("loan-approved");
                    case "Rejected" -> getStyleClass().add("loan-rejected");
                }
            }
        });

        TableColumn<Loan, String> appliedCol = new TableColumn<>("Applied On");
        appliedCol.setCellValueFactory(new PropertyValueFactory<>("appliedAtDisplay"));

        TableColumn<Loan, String> approvedCol = new TableColumn<>("Approved On");
        approvedCol.setCellValueFactory(new PropertyValueFactory<>("approvedAtDisplay"));

        table.getColumns().addAll(idCol, accCol, typeCol, amtCol, emiCol, statusCol, appliedCol, approvedCol);
        table.setItems(FXCollections.observableArrayList(allLoans));

        Label countLabel = new Label("Total loans: " + allLoans.size());
        countLabel.getStyleClass().add("note-label");

        panel.getChildren().addAll(title, countLabel, table);
        return panel;
    }

    public BorderPane getRoot() { return root; }
}
