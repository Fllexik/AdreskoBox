package sk.bakaj.adreskobox.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import sk.bakaj.adreskobox.model.LabelFormat;
import sk.bakaj.adreskobox.model.Parent;
import sk.bakaj.adreskobox.service.PDFService;

import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Label;
import java.util.List;
import java.io.File;

/**
 * Controller pre okno náhľadu a tlače štítkov.
 * Zobrazuje tabuľku s rodičmi a ich adresami, umožňuje export do PDF
 * a priamu tlač štítkov v zvolenom formáte.
 */
public class PrintPreviewController
{
    // FXML komponenty pre zobrazenie údajov
    @FXML
    private TableView<Parent> labelsTable;  // Tabuľka so zoznamom štítkov

    @FXML
    private TableColumn<Parent, String> nameColumn; // Stĺpеc s menami rodičov

    @FXML
    private TableColumn<Parent, String> addressColumn;  // Stĺpec s adresami

    @FXML
    private Label totalLabelsLabel; // Label zobrazujúci celkový počet štítkov

    // Dáta pre generovanie štítkov
    private  List<Parent> parents;                      // Zoznam rodičov na tlač
    private LabelFormat format;                         // Formát štítkov
    private PDFService pdfService = new PDFService();   // Služba pre generovanie PDF

    @FXML
    public void initialize()
    {
        nameColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getFullName()));
        addressColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getFullAddress()));
    }

    public void setData(List<Parent> parents, LabelFormat format)
    {
        this.parents = parents;
        this.format = format;
        labelsTable.getItems().setAll(parents);
        totalLabelsLabel.setText(String.valueOf(parents.size()));
    }

    @FXML
    private void handleSavePDF()
    {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Uložiť PDF");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

        File file = fileChooser.showSaveDialog(labelsTable.getScene().getWindow());
        if (file != null)
        {
            try
            {
                pdfService.generateLabels(parents,format, file);
                showAlert(Alert.AlertType.INFORMATION, "Úspech",
                        "PDF bolo úspešne vytvorené.");
            } catch (Exception e)
            {
                showAlert(Alert.AlertType.ERROR, "Chyba",
                        "Nastala chyba pri vytváraní PDF:" + e.getMessage());
            }
        }
    }

    @FXML
    private void handlePrint()
    {
        // TODO: Implementovať priamu tlač
        showAlert(Alert.AlertType.INFORMATION, "info",
                "Funkcionalita tlače bude implementovana v dalšej verzií.");
    }

    private void showAlert(Alert.AlertType type, String title, String content)
    {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

}
