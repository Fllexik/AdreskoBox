package sk.bakaj.adreskobox.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import sk.bakaj.adreskobox.model.ImportedData;
import sk.bakaj.adreskobox.model.LabelFormat;
import sk.bakaj.adreskobox.model.Parent;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImportController
{
    @FXML
    private TextField filePathField;

    @FXML
    private ComboBox<String> delimeterComboBox;

    @FXML
    private TextField widthField;

    @FXML
    private TextField heightField;

    @FXML
    private TextField columnsField;

    @FXML
    private TextField rowsField;


   private FileService fileService = new FileService();

    @FXML
    private void initialize()
    {
        delimeterComboBox.getItems().addAll(",", ";", "\t");
        delimeterComboBox.setValue(",");

        //Predvolené hodnoty pre štítky
        widthField.setText("48.3"); //105mm štandartná šírka
        heightField.setText("16.9");//42.mm
        columnsField.setText("2");
        rowsField.setText("16");

    @FXML
    private void handleFileChooser()
    {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx", "*.xls"),
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );

        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null)
        {
            filePathField.setText(selectedFile.getAbsolutePath());
        }
    }

    @FXML
    private void handleImportAndContinue()
    {
        try
        {
            File file = new File(filePathField.getText());
            String delimiter = delimeterComboBox.getValue();

            List<ImportedData> importedData = fileService.readFile(file, delimiter);

            //Vytvorenie formatu štítkov
            LabelFormat format = new LabelFormat(
                    Double.parseDouble(widthField.getText()),
                    Double.parseDouble(heightField.getText()),
                    Integer.parseInt(columnsField.getText()),
                    Integer.parseInt(rowsField.getText()),
                    7.5, //margin ľavy
                    1.2,// margin horny
                    0,//medzera medzi stĺpcami
                    0,//medzera medzi riadkami
            );

            //Presun na obrazovku náhľadu
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sk/bakaj/adreskobox/view/print-preview-view.fxml"));
            Parent root = loader.load();
            PrintPreviewController previewController = loader.getController();

            //Konverzia importedData na Parent Objekty
            List<Parent> parents = new ArrayList<>();
            for (ImportedData data : importedData)
            {
                if (data.getParent1Name() != null && !data.getParent1Name().isEmpty())
                {
                    parents.add(new Parent(data.getParent1Name(), data.getAddress1()));
                }
                if (data.getParent2Name() != null && !data.getParent2Name().isEmpty())
                {
                    parents.add(new Parent(data.getParent2Name(), data.getAddress2()));
                }
            }

            previewController.setData(parents, format);

            Scene scene = filePathField.getScene();
            scene.setRoot(root);
        } catch (Exception e)
        {
            showAlert(Alert.AlertType.ERROR, "Chyba",
                    "Nastala chyba pri importe:" + e.getMessage());
        }
        }
    }
    private void showAlert(Alert.AlertType type, String content)
    {
       Alert alert = new Alert(type);
       alert.setTitle(title);
       alert.setContentText(content);
       alert.showAndWait();
    }

}