package client;

import gui.HomeController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class ClientSide extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Parent logIn = FXMLLoader.load(getClass().getResource("../gui/LogIn.fxml"));
        Scene scene = new Scene(logIn);
        stage.setScene(scene);
        stage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }
}
