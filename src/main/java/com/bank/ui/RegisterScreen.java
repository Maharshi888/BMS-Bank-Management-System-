package com.bank.ui;

import com.bank.exception.BankException;
import com.bank.model.Account;
import com.bank.model.AccountType;
import com.bank.service.BankService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.time.LocalDate;

// CO4: JavaFX - Register / Create Account Screen
public class RegisterScreen {

    private final ScrollPane root;
    private final Stage stage;
    private final BankService bankService = BankService.getInstance();

    public RegisterScreen(Stage stage) {
        this.stage = stage;
        this.root = new ScrollPane();
        buildUI();
    }

    private void buildUI() {
        root.setFitToWidth(true);
        root.getStyleClass().add("scroll-pane-custom");

        VBox mainBox = new VBox(0);
        mainBox.getStyleClass().add("screen-bg");

        // Header
        HBox header = new HBox();
        header.getStyleClass().add("form-header");
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(20, 30, 20, 30));

        Button backBtn = new Button("← Back to Login");
        backBtn.getStyleClass().add("back-btn");
        backBtn.setOnAction(e -> {
            LoginScreen loginScreen = new LoginScreen(stage);
            stage.getScene().setRoot(loginScreen.getRoot());
        });

        Label title = new Label("🏦  Open a New Bank Account");
        title.getStyleClass().add("header-title");
        HBox.setMargin(title, new Insets(0, 0, 0, 20));

        header.getChildren().addAll(backBtn, title);

        // Form
        GridPane form = new GridPane();
        form.getStyleClass().add("register-form");
        form.setHgap(20);
        form.setVgap(16);
        form.setPadding(new Insets(30, 60, 30, 60));

        // Column constraints
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        form.getColumnConstraints().addAll(col1, col2);

        // --- Section: Personal Info ---
        Label personalSection = new Label("Personal Information");
        personalSection.getStyleClass().add("section-label");
        form.add(personalSection, 0, 0, 2, 1);

        // Full Name
        TextField fullNameField = createField("Full Name *", "Enter your full name");
        form.add(labeledField("Full Name *", fullNameField), 0, 1);

        // Username
        TextField usernameField = createField("Username *", "Choose a username");
        form.add(labeledField("Username *", usernameField), 1, 1);

        // Date of Birth
        DatePicker dobPicker = new DatePicker();
        dobPicker.setPromptText("dd/mm/yyyy");
        dobPicker.getStyleClass().add("date-picker-custom");
        dobPicker.setMaxWidth(Double.MAX_VALUE);
        form.add(labeledField("Date of Birth *", dobPicker), 0, 2);

        // Mobile Number
        TextField mobileField = createField("Mobile Number *", "10-digit mobile number");
        form.add(labeledField("Mobile Number *", mobileField), 1, 2);

        // --- Section: Identity ---
        Label identitySection = new Label("Identity Details");
        identitySection.getStyleClass().add("section-label");
        form.add(identitySection, 0, 3, 2, 1);

        // Aadhar Number
        TextField aadharField = createField("Aadhar Number *", "12-digit Aadhar number");
        form.add(labeledField("Aadhar Number *", aadharField), 0, 4);

        // PAN Number
        TextField panField = createField("PAN Number *", "e.g. ABCDE1234F");
        form.add(labeledField("PAN Number *", panField), 1, 4);

        // Address
        TextArea addressField = new TextArea();
        addressField.setPromptText("Enter full address");
        addressField.getStyleClass().add("text-area-custom");
        addressField.setPrefRowCount(2);
        form.add(labeledField("Address *", addressField), 0, 5, 2, 1);

        // --- Section: Professional ---
        Label professionalSection = new Label("Other Details");
        professionalSection.getStyleClass().add("section-label");
        form.add(professionalSection, 0, 6, 2, 1);

        // Educational Qualification
        ComboBox<String> eduCombo = new ComboBox<>();
        eduCombo.getItems().addAll(
                "Below 10th", "10th Pass", "12th Pass",
                "Diploma", "Graduate", "Post Graduate", "Doctorate"
        );
        eduCombo.setPromptText("Select qualification");
        eduCombo.getStyleClass().add("combo-box-custom");
        eduCombo.setMaxWidth(Double.MAX_VALUE);
        form.add(labeledField("Educational Qualification *", eduCombo), 0, 7);

        // Account Type
        ComboBox<String> accountTypeCombo = new ComboBox<>();
        accountTypeCombo.getItems().addAll("Savings Account", "Current Account");
        accountTypeCombo.setPromptText("Select account type");
        accountTypeCombo.getStyleClass().add("combo-box-custom");
        accountTypeCombo.setMaxWidth(Double.MAX_VALUE);
        form.add(labeledField("Account Type *", accountTypeCombo), 1, 7);

        // --- Section: Security ---
        Label securitySection = new Label("Security");
        securitySection.getStyleClass().add("section-label");
        form.add(securitySection, 0, 8, 2, 1);

        // Password
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Minimum 6 characters");
        passwordField.getStyleClass().add("text-field-custom");
        form.add(labeledField("Password *", passwordField), 0, 9);

        // Confirm Password
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Re-enter password");
        confirmPasswordField.getStyleClass().add("text-field-custom");
        form.add(labeledField("Confirm Password *", confirmPasswordField), 1, 9);

        // Error / Success message
        Label messageLabel = new Label();
        messageLabel.getStyleClass().add("error-label");
        messageLabel.setWrapText(true);
        form.add(messageLabel, 0, 10, 2, 1);

        // Submit button
        Button createBtn = new Button("Create Account →");
        createBtn.getStyleClass().add("primary-btn");
        createBtn.setPrefWidth(260);
        HBox btnBox = new HBox(createBtn);
        btnBox.setAlignment(Pos.CENTER_RIGHT);
        form.add(btnBox, 0, 11, 2, 1);

        // Action
        createBtn.setOnAction(e -> handleRegister(
                fullNameField, usernameField, dobPicker,
                mobileField, aadharField, panField,
                addressField, eduCombo, accountTypeCombo,
                passwordField, confirmPasswordField, messageLabel
        ));

        mainBox.getChildren().addAll(header, form);
        root.setContent(mainBox);
    }

    private void handleRegister(TextField fullName, TextField username,
                                 DatePicker dob, TextField mobile,
                                 TextField aadhar, TextField pan,
                                 TextArea address, ComboBox<String> edu,
                                 ComboBox<String> accountType,
                                 PasswordField password, PasswordField confirmPassword,
                                 Label messageLabel) {
        messageLabel.getStyleClass().remove("success-label");
        messageLabel.getStyleClass().add("error-label");

        // Basic blank checks
        if (fullName.getText().isBlank() || username.getText().isBlank()
                || dob.getValue() == null || mobile.getText().isBlank()
                || aadhar.getText().isBlank() || pan.getText().isBlank()
                || address.getText().isBlank() || edu.getValue() == null
                || accountType.getValue() == null || password.getText().isBlank()) {
            messageLabel.setText("✗ Please fill in all required fields.");
            return;
        }

        if (!password.getText().equals(confirmPassword.getText())) {
            messageLabel.setText("✗ Passwords do not match.");
            return;
        }

        AccountType type = accountType.getValue().startsWith("Savings")
                ? AccountType.SAVINGS : AccountType.CURRENT;

        try {
            Account account = bankService.createAccount(
                    fullName.getText().trim(),
                    username.getText().trim(),
                    password.getText(),
                    dob.getValue(),
                    aadhar.getText().trim(),
                    pan.getText().trim().toUpperCase(),
                    address.getText().trim(),
                    mobile.getText().trim(),
                    edu.getValue(),
                    type
            );

            // Show success and navigate to dashboard
            AccountCreatedScreen createdScreen = new AccountCreatedScreen(stage, account);
            stage.getScene().setRoot(createdScreen.getRoot());

        } catch (BankException ex) {
            messageLabel.setText("✗ " + ex.getMessage());
        }
    }

    private TextField createField(String label, String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.getStyleClass().add("text-field-custom");
        return field;
    }

    private VBox labeledField(String labelText, javafx.scene.Node field) {
        VBox box = new VBox(5);
        Label label = new Label(labelText);
        label.getStyleClass().add("field-label");
        box.getChildren().addAll(label, field);
        VBox.setVgrow(field, Priority.ALWAYS);
        if (field instanceof Region r) r.setMaxWidth(Double.MAX_VALUE);
        return box;
    }

    public ScrollPane getRoot() { return root; }
}
