package com.bank.ui;

import com.bank.exception.BankException;
import com.bank.model.Account;
import com.bank.model.Loan;
import com.bank.model.LoanType;
import com.bank.service.BankService;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

/**
 * User-facing Loan panel with two tabs:
 *   Tab 1 — Apply for a new loan
 *   Tab 2 — View existing loans
 */
public class LoanPanel {

    private final VBox root;
    private final Account account;
    private final BankService bankService;

    public LoanPanel(Account account, BankService bankService) {
        this.account = account;
        this.bankService = bankService;
        this.root = new VBox();
        buildUI();
    }

    private void buildUI() {
        root.setPadding(new Insets(30));
        root.setSpacing(16);

        Label title = new Label("🏦  My Loans");
        title.getStyleClass().add("panel-title");

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.getStyleClass().add("loan-tab-pane");
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        Tab applyTab = new Tab("Apply for Loan");
        applyTab.setContent(buildApplyTab());

        Tab myLoansTab = new Tab("My Loans");
        myLoansTab.setContent(buildMyLoansTab());

        tabPane.getTabs().addAll(applyTab, myLoansTab);
        root.getChildren().addAll(title, tabPane);
    }

    // ── Apply for Loan tab ────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private VBox buildApplyTab() {
        VBox tab = new VBox(16);
        tab.setPadding(new Insets(24));

        // Loan type combo
        ComboBox<LoanType> loanTypeCombo = new ComboBox<>(
                FXCollections.observableArrayList(LoanType.values()));
        loanTypeCombo.setPromptText("Select loan type");
        loanTypeCombo.getStyleClass().add("combo-field");
        loanTypeCombo.setMaxWidth(360);

        // Info label showing rate & max tenure
        Label loanInfoLabel = new Label();
        loanInfoLabel.getStyleClass().add("note-label");
        loanInfoLabel.setWrapText(true);

        loanTypeCombo.setOnAction(e -> {
            LoanType selected = loanTypeCombo.getValue();
            if (selected != null) {
                loanInfoLabel.setText(String.format(
                        "Rate: %.1f%% p.a.  |  Max tenure: %d months",
                        selected.getInterestRatePA(), selected.getMaxTenureMonths()
                ));
            }
        });

        // Amount field
        TextField amountField = new TextField();
        amountField.setPromptText("Loan amount (₹)");
        amountField.getStyleClass().add("text-field-custom");
        amountField.setMaxWidth(360);

        // Tenure field
        TextField tenureField = new TextField();
        tenureField.setPromptText("Tenure in months");
        tenureField.getStyleClass().add("text-field-custom");
        tenureField.setMaxWidth(360);

        // EMI preview
        Label emiPreview = new Label("EMI will be shown here after you fill in the fields.");
        emiPreview.getStyleClass().add("note-label");
        emiPreview.setWrapText(true);

        // Live EMI computation on field changes
        Runnable updateEMI = () -> updateEmiPreview(emiPreview, loanTypeCombo, amountField, tenureField);
        amountField.textProperty().addListener((obs, o, n) -> updateEMI.run());
        tenureField.textProperty().addListener((obs, o, n) -> updateEMI.run());
        loanTypeCombo.valueProperty().addListener((obs, o, n) -> updateEMI.run());

        Label resultLabel = new Label();
        resultLabel.setWrapText(true);

        Button applyBtn = new Button("Apply for Loan →");
        applyBtn.getStyleClass().add("primary-btn");

        applyBtn.setOnAction(e -> {
            resultLabel.setText("");
            LoanType selectedType = loanTypeCombo.getValue();
            if (selectedType == null) {
                showError(resultLabel, "✗ Please select a loan type.");
                return;
            }
            try {
                double principal = Double.parseDouble(amountField.getText().trim());
                int tenure = Integer.parseInt(tenureField.getText().trim());
                Loan loan = bankService.applyForLoan(account.getAccountNumber(), selectedType, principal, tenure);
                showSuccess(resultLabel, "✓ Loan application submitted! Loan ID: " + loan.getLoanId()
                        + ". Pending admin approval.");
                amountField.clear();
                tenureField.clear();
                loanTypeCombo.setValue(null);
                loanInfoLabel.setText("");
                emiPreview.setText("EMI will be shown here after you fill in the fields.");
            } catch (NumberFormatException ex) {
                showError(resultLabel, "✗ Please enter valid numeric values for amount and tenure.");
            } catch (BankException ex) {
                showError(resultLabel, "✗ " + ex.getMessage());
            }
        });

        VBox fields = new VBox(12,
                labeled("Loan Type", loanTypeCombo),
                loanInfoLabel,
                labeled("Loan Amount (₹)", amountField),
                labeled("Tenure (months)", tenureField),
                emiPreview
        );

        tab.getChildren().addAll(fields, applyBtn, resultLabel);
        return tab;
    }

    private void updateEmiPreview(Label emiPreview, ComboBox<LoanType> loanTypeCombo,
                                  TextField amountField, TextField tenureField) {
        try {
            LoanType type = loanTypeCombo.getValue();
            if (type == null) return;
            double principal = Double.parseDouble(amountField.getText().trim());
            int tenure = Integer.parseInt(tenureField.getText().trim());
            if (principal <= 0 || tenure <= 0) return;
            double emi = Loan.computeEMI(principal, type.getInterestRatePA(), tenure);
            emiPreview.setText(String.format("📊 Estimated Monthly EMI: ₹%.2f", emi));
            emiPreview.getStyleClass().removeAll("error-label");
            if (!emiPreview.getStyleClass().contains("success-label"))
                emiPreview.getStyleClass().add("success-label");
        } catch (NumberFormatException ignored) {
            emiPreview.setText("EMI will be calculated once you enter valid values.");
            emiPreview.getStyleClass().removeAll("success-label");
        }
    }

    // ── My Loans tab ──────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private VBox buildMyLoansTab() {
        VBox tab = new VBox(16);
        tab.setPadding(new Insets(24));

        // Reload the latest account data
        Account freshAccount;
        try {
            freshAccount = bankService.getAccount(account.getAccountNumber());
        } catch (BankException e) {
            freshAccount = account;
        }

        java.util.List<Loan> loans = freshAccount.getLoans();

        if (loans.isEmpty()) {
            Label empty = new Label("You have no loans yet. Apply from the 'Apply for Loan' tab.");
            empty.getStyleClass().add("empty-label");
            tab.getChildren().add(empty);
            return tab;
        }

        TableView<Loan> table = new TableView<>();
        table.getStyleClass().add("table-custom");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Loan, String> idCol = new TableColumn<>("Loan ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("loanId"));

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

        TableColumn<Loan, Double> rateCol = new TableColumn<>("Rate (p.a.)");
        rateCol.setCellValueFactory(new PropertyValueFactory<>("interestRatePA"));
        rateCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : item + "%");
            }
        });

        TableColumn<Loan, Integer> tenureCol = new TableColumn<>("Tenure (mo)");
        tenureCol.setCellValueFactory(new PropertyValueFactory<>("tenureMonths"));

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

        TableColumn<Loan, String> dueCol = new TableColumn<>("Next Due");
        dueCol.setCellValueFactory(new PropertyValueFactory<>("nextDueDateDisplay"));

        table.getColumns().addAll(idCol, typeCol, amtCol, rateCol, tenureCol, emiCol,
                statusCol, appliedCol, approvedCol, dueCol);
        table.setItems(FXCollections.observableArrayList(loans));

        tab.getChildren().add(table);
        return tab;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private VBox labeled(String text, javafx.scene.Node node) {
        VBox box = new VBox(5);
        Label lbl = new Label(text);
        lbl.getStyleClass().add("field-label");
        box.getChildren().addAll(lbl, node);
        return box;
    }

    private void showSuccess(Label label, String msg) {
        label.getStyleClass().removeAll("error-label");
        if (!label.getStyleClass().contains("success-label"))
            label.getStyleClass().add("success-label");
        label.setText(msg);
    }

    private void showError(Label label, String msg) {
        label.getStyleClass().removeAll("success-label");
        if (!label.getStyleClass().contains("error-label"))
            label.getStyleClass().add("error-label");
        label.setText(msg);
    }

    public VBox getRoot() { return root; }
}
