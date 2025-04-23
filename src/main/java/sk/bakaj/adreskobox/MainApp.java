package sk.bakaj.adreskobox;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application
{
    @Override
    public void start(Stage primaryStage) throws Exception
    {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/Main.fxml"));
        Parent root = loader.load();

        primaryStage.setTitle("AdreskoBOX");
        primaryStage.setScene(new Scene(root, 800,600));
        primaryStage.show();
    }

    public static void main(String[] args)
    {
        launch(args);
    }
}
