package com.bank.ui;

import com.bank.model.Account;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

// CO4: JavaFX - Account Created Confirmation Screen
public class AccountCreatedScreen {

    private final BorderPane root;
    private final Stage stage;
    private final Account account;

    public AccountCreatedScreen(Stage stage, Account account) {
        this.stage = stage;
        this.account = account;
        this.root = new BorderPane();
        buildUI();
    }

    private void buildUI() {
        root.getStyleClass().add("screen-bg");

        VBox centerBox = new VBox(22);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setPadding(new Insets(50));

        // Success icon
        Label successIcon = new Label("✅");
        successIcon.setStyle("-fx-font-size: 52px;");

        Label successTitle = new Label("Account Created Successfully!");
        successTitle.getStyleClass().add("form-title");

        Label successMsg = new Label("Welcome to BMS Bank! Your account details are below.");
        successMsg.getStyleClass().add("form-subtitle");

        // Account details card
        VBox card = new VBox(14);
        card.getStyleClass().add("info-card");
        card.setPadding(new Insets(28, 36, 28, 36));
        card.setMaxWidth(480);

        Label cardTitle = new Label("Account Information");
        cardTitle.getStyleClass().add("card-title");

        GridPane details = new GridPane();
        details.setHgap(16);
        details.setVgap(10);

        addDetailRow(details, "Account Holder", account.getAccountHolderName(), 0);
        addDetailRow(details, "Account Number", account.getAccountNumber(), 1);
        addDetailRow(details, "Account Type", account.getAccountType().getDisplayName(), 2);
        addDetailRow(details, "Username", account.getUsername(), 3);
        addDetailRow(details, "Opening Balance",
                String.format("₹%.2f (Minimum Balance)", account.getBalance()), 4);

        Label noteLabel = new Label("⚠ Please note your Account Number for future logins.");
        noteLabel.getStyleClass().add("note-label");
        noteLabel.setWrapText(true);

        card.getChildren().addAll(cardTitle, details, new Separator(), noteLabel);

        Button continueBtn = new Button("Proceed to Dashboard →");
        continueBtn.getStyleClass().add("primary-btn");
        continueBtn.setPrefWidth(260);

        continueBtn.setOnAction(e -> {
            DashboardScreen dashboard = new DashboardScreen(stage, account);
            stage.getScene().setRoot(dashboard.getRoot());
        });

        centerBox.getChildren().addAll(successIcon, successTitle, successMsg, card, continueBtn);
        root.setCenter(centerBox);
    }

    private void addDetailRow(GridPane grid, String label, String value, int row) {
        Label labelNode = new Label(label + ":");
        labelNode.getStyleClass().add("detail-key");
        Label valueNode = new Label(value);
        valueNode.getStyleClass().add("detail-value");
        grid.add(labelNode, 0, row);
        grid.add(valueNode, 1, row);
    }

    public BorderPane getRoot() { return root; }
}
