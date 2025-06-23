package sk.bakaj.adreskobox;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


/**
 * Hlavná aplikačná trieda pre AdreskoBox - generátor štítkov
 *
 * Táto aplikácia umožňuje import dát, výber rodičov, kontrolu adries,
 * nastavenie výstupu a generovanie adresných štítkov.
 */
public class AdreskoboxApp extends Application
{
    /**
     * Inicializuje a zobrazuje hlavné okno aplikácie
     *
     * @param primaryStage hlavné okno aplikácie
     * @throws Exception ak sa nepodarí načítať FXML súbor
     */
    @Override
    public void start(Stage primaryStage) throws Exception
    {
        // Načítanie FXML súboru pre hlavné okno
        javafx.scene.Parent root = FXMLLoader.load(getClass().getResource("/fxml/Main.fxml"));

        // Vytvorenie scény a nastavenie okna
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

        primaryStage.setTitle("AdreskoBox - Generátor štítkov");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Hlavná metóda pre spustenie aplikácie
     *
     * @param args argumenty z príkazového riadku
     */
    public static void main(String[] args)
    {
        launch(args);
    }
}