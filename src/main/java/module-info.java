module com.bank {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.base;

    opens com.bank to javafx.graphics;
    opens com.bank.ui to javafx.graphics;
    opens com.bank.model to javafx.base;
    opens com.bank.service to javafx.base;


    exports com.bank;
    exports com.bank.model;
    exports com.bank.service;
    exports com.bank.exception;
    exports com.bank.util;
    exports com.bank.ui;
}
