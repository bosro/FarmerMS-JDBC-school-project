module com.example.jdbc_assignment {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.fontawesome5;
    requires java.sql;
    requires mysql.connector.j;
    requires org.json;

    opens com.example.jdbc_assignment to javafx.fxml;
    opens com.example.jdbc_assignment.controllers to javafx.fxml;
    exports com.example.jdbc_assignment;
    exports com.example.jdbc_assignment.controllers;
    exports com.example.jdbc_assignment.models;
}