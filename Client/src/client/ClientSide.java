package client;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.DataInputStream;
import java.io.PrintStream;
import java.net.Socket;


public class ClientSide extends Application {
    public static DataInputStream dis;
    public static PrintStream ps;
    Socket socket;

    @Override
    public void start(Stage stage) throws Exception {
        startConnection();
        Parent logIn = FXMLLoader.load(getClass().getResource("../gui/LogIn.fxml"));
        Scene scene = new Scene(logIn);
        stage.setScene(scene);
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                try {
                    ps.println("close connection");
                    ps.flush();
                    socket.close();
                    dis.close();
                    ps.close();
                }
                catch (Exception ignored){

                }
            }
        });

        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public void startConnection(){
        try {
            socket = new Socket("127.0.0.1", 4015);
            // Create data input and output streams
            dis = new DataInputStream(socket.getInputStream());
            ps = new PrintStream(socket.getOutputStream());
        }
        catch (Exception ignored){
        }
    }
}
