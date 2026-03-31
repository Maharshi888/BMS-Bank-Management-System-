package com.bank.ui;

import com.bank.MainApp;
import com.bank.exception.BankException;
import com.bank.model.Account;
import com.bank.service.BankService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;

// CO4: JavaFX GUI - Login Screen
public class LoginScreen {

    private final BorderPane root;
    private final Stage stage;
    private final BankService bankService = BankService.getInstance();

    public LoginScreen(Stage stage) {
        this.stage = stage;
        this.root = new BorderPane();
        buildUI();
    }

    private void buildUI() {
        root.getStyleClass().add("screen-bg");

        // Left panel - branding
        VBox leftPanel = new VBox(16);
        leftPanel.getStyleClass().add("left-panel");
        leftPanel.setAlignment(Pos.CENTER);
        leftPanel.setPrefWidth(340);

        Label bankIcon = new Label("🏦");
        bankIcon.setStyle("-fx-font-size: 56px;");

        Label bankName = new Label("BMS Bank");
        bankName.getStyleClass().add("brand-title");

        Label tagline = new Label("Secure · Simple · Smart");
        tagline.getStyleClass().add("brand-tagline");

        Label desc = new Label("Your trusted partner\nfor all banking needs.");
        desc.getStyleClass().add("brand-desc");
        desc.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        leftPanel.getChildren().addAll(bankIcon, bankName, tagline, new Separator(), desc);

        // Right panel - login form
        VBox rightPanel = new VBox(18);
        rightPanel.getStyleClass().add("right-panel");
        rightPanel.setAlignment(Pos.CENTER);
        rightPanel.setPadding(new Insets(50, 60, 50, 60));

        Label loginTitle = new Label("Welcome Back");
        loginTitle.getStyleClass().add("form-title");

        Label loginSubtitle = new Label("Sign in to your account");
        loginSubtitle.getStyleClass().add("form-subtitle");

        // Identifier field
        VBox identifierBox = new VBox(6);
        Label identifierLabel = new Label("Username or Account Number");
        identifierLabel.getStyleClass().add("field-label");
        TextField identifierField = new TextField();
        identifierField.setPromptText("Enter username or account number");
        identifierField.getStyleClass().add("text-field-custom");
        identifierBox.getChildren().addAll(identifierLabel, identifierField);

        // Password field
        VBox passwordBox = new VBox(6);
        Label passwordLabel = new Label("Password");
        passwordLabel.getStyleClass().add("field-label");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        passwordField.getStyleClass().add("text-field-custom");
        passwordBox.getChildren().addAll(passwordLabel, passwordField);

        // Error label
        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("error-label");
        errorLabel.setWrapText(true);

        // Login button
        Button loginBtn = new Button("Login →");
        loginBtn.getStyleClass().add("primary-btn");
        loginBtn.setMaxWidth(Double.MAX_VALUE);

        // Separator
        HBox orBox = new HBox(10);
        orBox.setAlignment(Pos.CENTER);
        Separator s1 = new Separator(); HBox.setHgrow(s1, Priority.ALWAYS);
        Separator s2 = new Separator(); HBox.setHgrow(s2, Priority.ALWAYS);
        Label orLabel = new Label("OR");
        orLabel.getStyleClass().add("or-label");
        orBox.getChildren().addAll(s1, orLabel, s2);

        // Register button
        Button registerBtn = new Button("Create New Account");
        registerBtn.getStyleClass().add("secondary-btn");
        registerBtn.setMaxWidth(Double.MAX_VALUE);

        // Admin login button
        Button adminBtn = new Button("🔐  Login as Administrator");
        adminBtn.getStyleClass().add("admin-login-btn");
        adminBtn.setMaxWidth(Double.MAX_VALUE);

        rightPanel.getChildren().addAll(
                loginTitle, loginSubtitle,
                identifierBox, passwordBox,
                errorLabel,
                loginBtn, orBox, registerBtn, adminBtn
        );

        // Actions
        loginBtn.setOnAction(e -> handleLogin(
                identifierField.getText().trim(),
                passwordField.getText(),
                errorLabel
        ));

        passwordField.setOnAction(e -> loginBtn.fire());

        registerBtn.setOnAction(e -> {
            RegisterScreen registerScreen = new RegisterScreen(stage);
            stage.getScene().setRoot(registerScreen.getRoot());
        });

        adminBtn.setOnAction(e -> handleAdminLogin());

        root.setLeft(leftPanel);
        root.setCenter(rightPanel);
    }

    private void handleLogin(String identifier, String password, Label errorLabel) {
        errorLabel.setText("");
        if (identifier.isEmpty() || password.isEmpty()) {
            errorLabel.setText("⚠ Please fill in all fields.");
            return;
        }
        try {
            Account account = bankService.login(identifier, password);
            DashboardScreen dashboard = new DashboardScreen(stage, account);
            stage.getScene().setRoot(dashboard.getRoot());
        } catch (BankException ex) {
            errorLabel.setText("✗ " + ex.getMessage());
        }
    }

    private void handleAdminLogin() {
        // Credentials dialog
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Administrator Login");
        dialog.setHeaderText("Enter admin credentials");

        ButtonType loginButtonType = new ButtonType("Login", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField adminUser = new TextField();
        adminUser.setPromptText("Admin username");
        PasswordField adminPass = new PasswordField();
        adminPass.setPromptText("Admin password");

        grid.add(new Label("Username:"), 0, 0);
        grid.add(adminUser, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(adminPass, 1, 1);

        Label adminError = new Label();
        adminError.setStyle("-fx-text-fill: #e74c3c;");
        grid.add(adminError, 0, 2, 2, 1);

        dialog.getDialogPane().setContent(grid);

        javafx.scene.Node loginNode = dialog.getDialogPane().lookupButton(loginButtonType);
        loginNode.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String user = adminUser.getText().trim();
            String pass = adminPass.getText();
            // Hardcoded admin credentials (demo)
            if ("admin".equals(user) && "admin123".equals(pass)) {
                dialog.close();
                AdminDashboard adminDash = new AdminDashboard(stage);
                stage.getScene().setRoot(adminDash.getRoot());
            } else {
                adminError.setText("✗ Invalid admin credentials.");
                event.consume(); // keep dialog open
            }
        });

        dialog.showAndWait();
    }

    public BorderPane getRoot() { return root; }
}

