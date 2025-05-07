package sk.bakaj.adreskobox;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import sk.bakaj.adreskobox.model.Parent;

public class AdreskoboxApp extends Application
{
    @Override
    public void start(Stage primaryStage) throws Exception
    {
        javafx.scene.Parent root = FXMLLoader.load(getClass().getResource("/fxml/Main.fxml"));

        Scene scene = new Scene(root);
        primaryStage.setTitle("AdreskoBox - Generátor štítkov");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args)
    {
        launch(args);
    }
}