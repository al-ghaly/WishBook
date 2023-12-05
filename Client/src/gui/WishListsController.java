package gui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

public class WishListsController implements Initializable {

    @FXML
    private VBox tablesContainer;
    ArrayList<String> usernamesList = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //Creating tables dynamically
        List<List<String>> listOfLists = getSampleData();

        for (List<String> dataList : listOfLists) {
            TableView<String> tableView = createTableView(dataList);
            tablesContainer.getChildren().add(tableView);
        }
    }

    private TableView<String> createTableView(List<String> dataList) {
        TableView<String> tableView = new TableView<>();

        // Add your data to the table (assuming a single column for simplicity)
        dataList.forEach(item -> {
            TableColumn<String, String> column = new TableColumn<>(item);
            column.setCellValueFactory(new PropertyValueFactory<>(item));
            tableView.getColumns().add(column);
        });

        return tableView;
    }

    private List<List<String>> getSampleData() {
        // Replace this with your actual data retrieval logic
        List<List<String>> listOfLists = new ArrayList<>();
        listOfLists.add(Arrays.asList("Column1", "Column2", "Column3"));
        listOfLists.add(Arrays.asList("Data1", "Data2", "Data3"));
        listOfLists.add(Arrays.asList("Info1", "Info2", "Info3"));
        return listOfLists;
    }

    public void setData(ArrayList<String> usernamesList){
        this.usernamesList = usernamesList;
    }
}
