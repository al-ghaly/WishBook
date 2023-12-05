package gui;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class HomeController implements Initializable {

    @FXML
    private Button profileButton;
    @FXML
    private Button addItemButton;
    @FXML
    private Button addFriendButton;
    @FXML
    private Button friendListButton;
    @FXML
    private Button notificationButton;
    @FXML
    private Button logOutButton;
    @FXML
    private Button aboutButton;
    @FXML
    private Label usernameTxt;
    @FXML
    private Label balanceTxt;

    private String username;
    private long balance;
    @FXML
    private Button requestButton;
    @FXML
    private GridPane homeContent;

    ArrayList<String> friendsList = new ArrayList<>();
    int port = 4015;
    String ip = "127.0.0.1";


    public void setData(String data, long balance) {
        this.username = data;
        this.balance = balance;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
       usernameTxt.setText(username);
       balanceTxt.setText(balance + " $");

       aboutButton.setOnAction(e -> {
          popUpAbout();
       });

        logOutButton.setOnAction(e -> {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        switchToLogIn(e);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        showAlert("An Error Happened");
                    }
                }
            });
        });

        // Load the home page content
        try {
            // Create the home page content.
            FXMLLoader loader = new FXMLLoader(getClass().getResource("./WishLists.fxml"));
            // Create an instance of your controller and set the data
            WishListsController wishListsController = new WishListsController();
            // Get the user's friends list here and pass an arraylist of usernames to the custom page
            if(getFriends(username).equals("success"))
                wishListsController.setData(friendsList);
            else
                showAlert("An Error Happened loading your Home Page");

            loader.setControllerFactory(clazz -> {
                if (clazz == WishListsController.class) {
                    return wishListsController;
                } else {
                    try {
                        return clazz.newInstance();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            Parent testPage = loader.load();
            // Add the loaded page to the homeContent grid
            homeContent.getChildren().add(testPage);

        } catch (IOException e) {
            showAlert("An Error Happened Loading your Home Page!!");
        }
    }

    public String getFriends(String username){
        try {
            Socket socket = new Socket(ip, port);
            // Create data input and output streams
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            PrintStream ps = new PrintStream(socket.getOutputStream());

            // Send data to the server
            JSONObject signUpData = new JSONObject();
            signUpData.put("Type", "friends list");
            signUpData.put("username", username);
            // Send the JSON string to the server
            ps.println(signUpData);
            ps.flush();

            // Read the server response
            String response = dis.readLine();
            // Behave according to the server response
            Object serverResponse = JSONValue.parse(response);
            JSONObject serverMessage = (JSONObject) serverResponse;
            String status
                    = (String) serverMessage.get("Status");

            dis.close();
            ps.close();
            // Close the socket
            socket.close();

            if(status.equals("success")){
                JSONArray friends = (JSONArray) serverMessage.get("friends");
                for (Object friend : friends) {
                    friendsList.add((String) friend);
                }
                return "success";
            }
            else
                return "failed";

        } catch (Exception ex) {
            return "failed";
        }
    }

    public void popUpAbout(){
        Stage popupStage = new Stage();
        About root = new About();
        Scene aboutScene = new Scene(root);
        popupStage.setScene(aboutScene);
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.show();
    }

    public void switchToLogIn(ActionEvent event) throws Exception{
        Parent logIn = FXMLLoader.load(getClass().getResource("../gui/LogIn.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(logIn);
        stage.setScene(scene);
        stage.show();
    }

    public void showAlert(String message) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setContentText(message);
        a.setHeaderText("An Error Happened!");
        a.setTitle("Error!!");
        a.show();
    }
}
