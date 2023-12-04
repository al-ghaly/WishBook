package gui;

import java.io.DataInputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import client.*;
import javafx.event.ActionEvent;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import javafx.stage.Stage;


public class LogInController implements Initializable {

    @FXML
    private TextField usernameTxt;
    @FXML
    private TextField passwordTxt;
    @FXML
    private Button loginButton;
    @FXML
    private Button signUpButton;

    int port = 4015;
    String ip = "127.0.0.1";
    private Stage stage;
    private Scene scene;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loginButton.setOnAction(e -> {
            // Get username and password
            String username = usernameTxt.getText();
            String password = passwordTxt.getText();

            Client client = new Client();
            client.setUsername(username);
            client.setPassword(password);

            // Perform login asynchronously in a separate thread
            if (username.isEmpty() || password.isEmpty()) {
                showAlert("Enter all required data!");
            } else {
                handleLogin(client, e);
            }
        });

        signUpButton.setOnAction(e -> {
                try {
                    switchToSignUp(e);
                } catch (Exception ex) {
                    showAlert("An Error Happened");
                }
        });

    }

    private void handleLogin(Client client, ActionEvent e) {
        try {
            Socket socket = new Socket(ip, port);

            // Create data input and output streams
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            PrintStream ps = new PrintStream(socket.getOutputStream());
            JSONObject logInData = new JSONObject();
            logInData.put("Type", "sign in");
            logInData.put("username", client.getUsername());

            // Send the JSON string to the server
            ps.println(logInData);
            ps.flush();

            // Read the server response
            String response = dis.readLine();
            // Behave according to the server response
            Object serverResponse = JSONValue.parse(response);
            JSONObject serverMessage = (JSONObject) serverResponse;

            String status
                    = (String) serverMessage.get("Status");
            switch (status) {
                case "success":
                    String password = (String) serverMessage.get("password");
                    if (!password.equals(client.getPassword())) {
                        showAlert("Wrong Password!!");
                    } else {
                        String balance = (String) serverMessage.get("balance");
                        client.setBalance(Long.parseLong(balance));
                        try {
                            switchToHome(e, client);
                        } catch (Exception ex) {
                            showAlert("An Error Happened");
                        }
                    }
                    break;
                case "username not found":
                    showAlert("Invalid Username!!");
                    break;
                case "An Error Happened":
                    showAlert("An Error Happened. Try Later.");
                    break;
            }

            dis.close();
            ps.close();
            // Close the socket
            socket.close();
        } catch (Exception ex) {
            // Provide feedback to the user about the error
            showAlert("Server is Down!\nWe may be on a break. Try Later.");
        }
    }

    public void showAlert(String message) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setContentText(message);
        a.setHeaderText("An Error Happened!");
        a.setTitle("Error!!");
        a.show();
    }

    public void switchToHome(ActionEvent event, Client client) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("../gui/Home.fxml"));

        // Create an instance of your controller and set the data
        HomeController homeController = new HomeController();
        homeController.setData(client.getUsername(), client.getBalance());

        loader.setControllerFactory(clazz -> {
            if (clazz == HomeController.class) {
                return homeController;
            } else {
                try {
                    return clazz.newInstance();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });

        Parent home = loader.load();
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
         scene = new Scene(home);
        stage.setScene(scene);
        stage.show();
    }

    public void switchToSignUp(ActionEvent event) throws Exception {
        Parent signUp = FXMLLoader.load(getClass().getResource("../gui/SignUp.fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(signUp);
        stage.setScene(scene);
        stage.show();
    }
}
