package client;

import gui.FriendsListController;
import gui.HomeController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.ArrayList;


public class ClientSide extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Parent logIn = FXMLLoader.load(getClass().getResource("../gui/LogIn.fxml"));
        Scene scene = new Scene(logIn);
        stage.setScene(scene);
        stage.show();
//        FXMLLoader loader = new FXMLLoader(getClass().getResource("../gui/FriendsList.fxml"));
//
//        // Create an instance of your controller and set the data
//        FriendsListController friendsListController = new FriendsListController();
//        ArrayList<String> friends = new ArrayList<>();
//        friends.add("Moha");
//        friends.add("aly");
//        friends.add("Mohsen");        friends.add("Moha");
//        friends.add("aly");
//        friends.add("Mohsen");        friends.add("Moha");
//        friends.add("aly");
//        friends.add("Mohsen");        friends.add("Moha");
//        friends.add("aly");
//        friends.add("Mohsen");        friends.add("Moha");
//        friends.add("aly");
//        friends.add("Mohsen");        friends.add("Moha");
//        friends.add("aly");
//        friends.add("Mohsen");
//        friendsListController.setData(friends, "Rahaf");
//
//        loader.setControllerFactory(clazz -> {
//            if (clazz == FriendsListController.class) {
//                return friendsListController;
//            } else {
//                try {
//                    return clazz.newInstance();
//                } catch (Exception e) {
//                    throw new RuntimeException(e);
//                }
//            }
//        });
//
//        Stage popupStage = new Stage();
//        Parent home = loader.load();
//        Scene scene = new Scene(home);
//        popupStage.setScene(scene);
//        popupStage.initModality(Modality.APPLICATION_MODAL);
//        popupStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }  

}
