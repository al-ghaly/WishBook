package gui;


import java.io.DataInputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;

import client.ClientSide;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class FriendsListController implements Initializable {

    @javafx.fxml.FXML
    private Label usernameLbl;
    @javafx.fxml.FXML
    private TextField searchBar;
    @javafx.fxml.FXML
    private Button removeBtn;

    @javafx.fxml.FXML
    private ListView<String> friendsList;
     String username;

    ArrayList<String> friendList = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        usernameLbl.setText(username);
        getFriends(username);
        ObservableList<String> friendsNames = FXCollections.observableArrayList(friendList);
        friendsList.setItems(friendsNames);
        searchBar.textProperty().addListener(
                (observable, oldValue, newValue) -> filterFriendsList(newValue));
        // Disable the remove button if no items are selected
        removeBtn.disableProperty().bind(
                friendsList.getSelectionModel().selectedItemProperty().isNull());
        // Get The remove Button to Work
        removeBtn.setOnAction(e -> {
            String selectedFriend= friendsList.getSelectionModel().getSelectedItem();
            if (selectedFriend != null) {
                if (showConfirm("Are you sure you want to remove "
                        + selectedFriend).equals("Ok")) {
                    // Remove the selected item from the database
                    String status = deleteFriend(selectedFriend, username);
                    if (status.equals("success")) {
                        friendsList.getItems().remove(selectedFriend);
                        friendList.remove(selectedFriend);
                    }
                    else
                        showAlert("Couldn't remove " + selectedFriend + " !");
                }
            }
        });

        // Get the mouse double click to work
        friendsList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                // Double-click detected
                int selectedIndex = friendsList.getSelectionModel().getSelectedIndex();
                if (selectedIndex >= 0 && selectedIndex < friendsList.getItems().size()) {
                    String selectedItem = friendsList.getItems().get(selectedIndex);
                    try {
                        openUserProfile(selectedItem);
                    } catch (Exception ex) {
                        showAlert("An Error Happened");
                    }
                }
            }
        });
    }

    public void getFriends(String username){
        try {
            // Send data to the server
            JSONObject signUpData = new JSONObject();
            signUpData.put("Type", "friends list");
            signUpData.put("username", username);
            // Send the JSON string to the server
            ClientSide.ps.println(signUpData);
            ClientSide.ps.flush();

            // Read the server response
            String response = ClientSide.dis.readLine();
            // Behave according to the server response
            Object serverResponse = JSONValue.parse(response);
            JSONObject serverMessage = (JSONObject) serverResponse;
            String status
                    = (String) serverMessage.get("Status");

            if(status.equals("success")){
                JSONArray friends = (JSONArray) serverMessage.get("friends");
                for (Object friend : friends) {
                    friendList.add((String) friend);
                }
            }
            else
                showAlert("Error");

        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Error");
        }
    }

    private void openUserProfile(String selectedItem) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("../gui/FriendProfile.fxml"));

        // Create an instance of your controller and set the data
        FriendProfileController friendProfileController = new FriendProfileController();
        friendProfileController.setData(selectedItem, username);

        loader.setControllerFactory(clazz -> {
            if (clazz == FriendProfileController.class) {
                return friendProfileController;
            } else {
                try {
                    return clazz.newInstance();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });

        Stage popupStage = new Stage();
        Parent home = loader.load();
        Scene scene = new Scene(home);
        popupStage.setScene(scene);
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.show();
    }

    private void filterFriendsList(String searchText) {
        // Filter the friends list based on the entered text
        ObservableList<String> filteredFriends = FXCollections.observableArrayList();
        for (String friend : friendList) {
            if (friend.toLowerCase().contains(searchText.toLowerCase())) {
                filteredFriends.add(friend);
            }
        }
        friendsList.setItems(filteredFriends);
    }

    public void setData(ArrayList<String> friends, String username) {
        this.username = username;
    }

    public void showAlert(String message) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setContentText(message);
        a.setHeaderText("An Error Happened!");
        a.setTitle("Error");
        a.show();
    }

    public String showConfirm(String message) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setContentText(message);
        a.setHeaderText("Action is irreversible!");
        a.setTitle("Confirm");

        Optional<ButtonType> result = a.showAndWait();
        // Check the user's choice
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // User clicked OK
            return "Ok";
        } else {
            return "Cancel";
        }
    }

    public String deleteFriend(String friendName, String username){
        try {
            // Send data to the server
            JSONObject itemData = new JSONObject();
            itemData.put("Type", "delete friend");
            itemData.put("username", username);
            itemData.put("friend name", friendName);
            // Send the JSON string to the server
            ClientSide.ps.println(itemData);
            ClientSide.ps.flush();

            // Read the server response
            String response = ClientSide.dis.readLine();
            // Behave according to the server response
            return response;
        } catch (Exception ex) {
            return "failed";
        }
    }
}
