package sk.bakaj.adreskobox;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
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
        try
        {
            // Načítanie FXML súboru pre hlavné okno
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Main.fxml"));
            Parent root = loader.load();

            // Vytvorenie scény
            Scene scene = new Scene(root, 1024, 700);

            // Načítanie CSS súboru
            String cssPath = getClass().getResource("/css/style.css").toExternalForm();
            scene.getStylesheets().add(cssPath);

            // Nastavenie okna
            primaryStage.setTitle("AdreskoBox - Generátor štítkov");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);

            // PRIDANIE IKONY
            try {
                Image icon = new Image(getClass().getResourceAsStream("/images/adreskobox_icon_64x64.png"));
                primaryStage.getIcons().add(icon);
            } catch (Exception e) {
                System.err.println("Nepodarilo sa načítať ikonu: " + e.getMessage());
            }

            // Zobrazenie okna
            primaryStage.show();
        }
        catch (Exception e)
        {
            System.err.println("Chyba pri spustení aplikácie: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
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