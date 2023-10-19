package be.hogent.sqlinjectiontester;

import static javafx.collections.FXCollections.observableArrayList;

import java.sql.SQLException;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

// Original Author: Joeri Van Herreweghe
// Translated to Java by Martijn Saelens
// An example of intentionally bad coding in order to illustrate SQL Injection attacks and their consequences.
//
// Possible SQL injections:
//  ' OR 1 --
//  '; DROP TABLE accounts --
//  '; INSERT INTO accounts(username, password) VALUES ('abc', 'test') --
//
public class SQLInjectionTester extends Application {
    Database database;
    TextField usernameTextField;
    TextField passwordTextField;
    Label outputLabel;
    TableView<Account> accountsTableView;

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws SQLException {

        // Init

        this.database = new Database();

        // Panes

        BorderPane basePane = new BorderPane();

        VBox menuPane = new VBox();
        basePane.setTop(menuPane);

        GridPane loginPane = new GridPane();
        loginPane.setHgap(10);
        loginPane.setVgap(10);
        loginPane.setPadding(new Insets(10, 10, 10, 10));
        loginPane.setAlignment(Pos.CENTER);
        basePane.setCenter(loginPane);

        VBox debugPane = new VBox();
        basePane.setBottom(debugPane);

        // menuPane

        MenuBar menuBar = new MenuBar();
        menuPane.getChildren().add(menuBar);

        Menu fileMenu = new Menu("File");
        menuBar.getMenus().add(fileMenu);

        MenuItem helpMenuItem = new MenuItem("Help");
        fileMenu.getItems().add(helpMenuItem);

        helpMenuItem.setOnAction((ActionEvent ae) -> {
            showHelp();
        });

        MenuItem aboutMenuItem = new MenuItem("About");
        fileMenu.getItems().add(aboutMenuItem);

        aboutMenuItem.setOnAction((ActionEvent ae) -> {
            showAbout();
        });

        // loginPane

        Label titleLabel = new Label("Welcome");
        titleLabel.setTextAlignment(TextAlignment.CENTER);
        titleLabel.setStyle("-fx-font-size: 30;");
        loginPane.add(titleLabel, 0, 0, 2, 1);

        Label usernameLabel = new Label("User Name:");
        loginPane.add(usernameLabel, 0, 1);

        usernameTextField = new TextField();
        loginPane.add(usernameTextField, 1, 1);

        Label passwordLabel = new Label("Password:");
        loginPane.add(passwordLabel, 0, 2);

        passwordTextField = new TextField();
        loginPane.add(passwordTextField, 1, 2);

        Button submitButton = new Button("Sign in");
        submitButton.setDefaultButton(true);
        loginPane.add(submitButton, 1, 3);

        // debugPane

        outputLabel = new Label();
        outputLabel.setTextAlignment(TextAlignment.CENTER);
        outputLabel.setAlignment(Pos.CENTER);
        outputLabel.setStyle("-fx-font-weight: bold;");
        outputLabel.setMaxWidth(Double.MAX_VALUE);
        debugPane.getChildren().add(outputLabel);

        Label accountsLabel = new Label(
                "Accounts (this information is normally not visible when logging in into a program or website):");
        debugPane.getChildren().add(accountsLabel);
        accountsTableView = new TableView<>();
        debugPane.getChildren().add(accountsTableView);

        TableColumn usernameTableColumn = new TableColumn("Username");
        usernameTableColumn.setCellValueFactory(new PropertyValueFactory<Account, String>("username"));

        TableColumn passwordTableColumn = new TableColumn("Password");
        passwordTableColumn.setCellValueFactory(new PropertyValueFactory<Account, String>("password"));

        accountsTableView.getColumns().addAll(usernameTableColumn, passwordTableColumn);
        refreshAccountsTableView();

        submitButton.setOnAction((ActionEvent ae) -> {
            onSubmit();
        });

        // Scene and Stage

        Scene scene = new Scene(basePane);
        primaryStage.setScene(scene);

        primaryStage.setTitle("SQL Injection Tester");
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);

        primaryStage.show();
    }

    private void onSubmit() {
        try {
            boolean isAccountValid = database.checkAccount(usernameTextField.getText(), passwordTextField.getText());

            if (isAccountValid) {
                showOutput("Logged in succesfully.");
            } else {
                showErrorOutput("Error: Wrong username and/or password!");
            }
        } catch (SQLException se) {
            showErrorOutput(concatenateSQLExceptionString(se));
        }

        try {
            refreshAccountsTableView();
        } catch (SQLException se) {
            showErrorOutput(concatenateSQLExceptionString(se));
            accountsTableView.getItems().clear();
        }
    }

    private void refreshAccountsTableView() throws SQLException {
        accountsTableView.setItems(observableArrayList(database.getAccounts()));
    }

    private void showOutput(String output) {
        outputLabel.setTextFill(Color.GREEN);
        outputLabel.setText(output);
    }

    private void showErrorOutput(String output) {
        outputLabel.setTextFill(Color.RED);
        outputLabel.setText(output);
    }

    private String concatenateSQLExceptionString(SQLException se) {
        StringBuilder errorText = new StringBuilder();
        errorText.append("An error has occurred!").append(System.lineSeparator())
                .append(se.getMessage()).append(System.lineSeparator());

        return errorText.toString();
    }

    private void showHelp() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Help");
        alert.setHeaderText(null);

        Label helpText = new Label("Try the following SQL injections in the password field:");

        TextArea examplesArea = new TextArea(
                "' OR 1 --" + System.lineSeparator()
                        + "'; DROP TABLE accounts --" + System.lineSeparator()
                        + "'; INSERT INTO accounts(username, password) VALUES ('abc', 'test') --");
        examplesArea.setEditable(false);
        examplesArea.setWrapText(true);

        VBox dialogPane = new VBox();
        dialogPane.getChildren().add(helpText);
        dialogPane.getChildren().add(examplesArea);

        alert.getDialogPane().setContent(dialogPane);
        alert.showAndWait();
    }

    private void showAbout() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText(null);
        alert.setContentText(
                "Original Author: Joeri Van Herreweghe." + System.lineSeparator()
                        + "Translated to Java by Martijn Saelens." + System.lineSeparator()
                        + System.lineSeparator()
                        + "Toegepaste informatica - Hogeschool Gent." + System.lineSeparator()
                        + System.lineSeparator()
                        + "An example of intentionally bad coding in order to illustrate SQL Injection attacks and their consequences."
                        + System.lineSeparator());
        alert.showAndWait();
    }
}
