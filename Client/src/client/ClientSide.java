package client;

import gui.*;
import java.io.DataInputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.json.simple.JSONObject;

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
