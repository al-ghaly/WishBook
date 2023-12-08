package gui;

import java.io.DataInputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class FriendsRequestController implements Initializable {
    
     @FXML
    private BorderPane borderPane;

    @FXML
    private ListView<String> listView;

    @FXML
    private Label label;

    String username;
    ArrayList<String> friendsList = new ArrayList<>();
    int port = 4015;
    String ip = "127.0.0.1";
    ArrayList<String> friends;
    ObservableList<String> items;

    @Override
public void initialize(URL url, ResourceBundle rb) {
        label.setText(username + "'s Friends Requests");
    // Add items to the ListView
    getRequests(username);
     items = FXCollections.observableArrayList(friendsList);
    
    // Set the cell factory to customize the rendering of each item
        listView.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
            @Override
            public ListCell<String> call(ListView<String> param) {
                return new FriendListCell();
            }
        });
        listView.setItems(items);
    }

    private void getRequests(String username) {
        try {
            Socket socket = new Socket(ip, port);
            // Create data input and output streams
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            PrintStream ps = new PrintStream(socket.getOutputStream());

            // Send data to the server
            JSONObject signUpData = new JSONObject();
            signUpData.put("Type", "get requests");
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
                JSONArray friends = (JSONArray) serverMessage.get("requests");
                for (Object friend : friends) {
                    friendsList.add((String) friend);
                }
            }
            else
                showAlert("Failed to retrieve the requests!");

        } catch (Exception ex) {
            showAlert("Failed to retrieve the requests!");
        }
    }

    public void setData(String username, ArrayList<String> friends) {
        this.username = username;
        this.friends = friends;
    }

    public class FriendListCell extends ListCell<String> {
    private HBox hbox = new HBox();
    private Button acceptButton = new Button("Accept");
    private Button rejectButton = new Button("Reject");
    private Label label = new Label();

    public FriendListCell() {
        // Handle accept button action
        acceptButton.setOnAction(e -> {
            boolean status = informServer(username, getItem(), true);
            if (status){
                updateList(getItem());
                friends.add(getItem());
            }
        });

        // Handle reject button action
        rejectButton.setOnAction(e -> {
            boolean status = informServer(username, getItem(), false);
            if (status){
                updateList(getItem());
            }
        });

        // Set HBox properties
        hbox.getChildren().addAll(label, acceptButton, rejectButton);
        hbox.setSpacing(10);  // Adjust spacing between components
        hbox.setAlignment(Pos.CENTER_LEFT);  // Align components to the left
    }

    private boolean informServer(String username, String item, boolean accepted) {
            try {
                Socket socket = new Socket(ip, port);
                // Create data input and output streams
                DataInputStream dis = new DataInputStream(socket.getInputStream());
                PrintStream ps = new PrintStream(socket.getOutputStream());

                // Send data to the server
                JSONObject signUpData = new JSONObject();
                signUpData.put("Type", "reply to requests");
                signUpData.put("username", username);
                signUpData.put("friend name", item);
                signUpData.put("status", accepted);
                // Send the JSON string to the server
                ps.println(signUpData);
                ps.flush();

                // Read the server response
                String response = dis.readLine();
                dis.close();
                ps.close();
                // Close the socket
                socket.close();

                if(response.equals("success")){
                    return true;
                }
                else{
                    showAlert("Action Failed!");
                    return false;
                }

            } catch (Exception ex) {
                showAlert("ActionFailed!");
                return false;
            }
        }

        @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setGraphic(null);
        } else {
            label.setText(item);
            setGraphic(hbox);
        }
    }

    public void updateList(String item){
        items.remove(item);
        listView.refresh();
    }
}

    public void showAlert(String message) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setContentText(message);
        a.setHeaderText("An Error Happened!");
        a.setTitle("Error!!");
        a.show();
    }

}
