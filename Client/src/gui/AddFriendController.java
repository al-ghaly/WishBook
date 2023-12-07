package gui;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class AddFriendController implements Initializable {

    String username;
    @javafx.fxml.FXML
    private GridPane AddFriendscene;
    @javafx.fxml.FXML
    private TableView tableAddFriend;
    @javafx.fxml.FXML
    private TableColumn usernamefield;
    @javafx.fxml.FXML
    private TableColumn emailfield;
    @javafx.fxml.FXML
    private Button buttonAddFriend;
    @javafx.fxml.FXML
    private TextField friendsearch;
    @javafx.fxml.FXML
    private Button buttonSearch;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

    public void setData(String username){
        this.username = username;
    }
}
