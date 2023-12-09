package gui;

import java.io.DataInputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

import client.ClientSide;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class NotificationsController implements Initializable {
    String username;
    @javafx.fxml.FXML
    private Label header;
    @javafx.fxml.FXML
    private ListView<String> notificationsLst;
    JSONArray notifications;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        header.setText(username + "'s Notifications");
        getNotifications(username);
        // Make the list view of nots
        ObservableList<String> notes = FXCollections.observableArrayList(notifications);
        notificationsLst.setItems(notes);
    }

    private void getNotifications(String username) {
        try {
            JSONObject logInData = new JSONObject();
            logInData.put("Type", "notifications");
            logInData.put("username", username);

            // Send the JSON string to the server
            ClientSide.ps.println(logInData);
            ClientSide.ps.flush();

            // Read the server response
            String response = ClientSide.dis.readLine();
            // Behave according to the server response
            Object serverResponse = JSONValue.parse(response);
            JSONObject serverMessage = (JSONObject) serverResponse;
            String status
                    = (String) serverMessage.get("Status");
            if(status.equals("success")){
                notifications = (JSONArray) serverMessage.get("notifications");
            }
            else
                showAlert("An Error Happened!");
        } catch (Exception ex) {
            // Provide feedback to the user about the error
            showAlert("An Error Happened!");
        }
    }

    public void setData(String username){
        this.username = username;
    }

    public void showAlert(String message) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setContentText(message);
        a.setHeaderText("An Error Happened!");
        a.setTitle("Error!!");
        a.show();
    }
}
