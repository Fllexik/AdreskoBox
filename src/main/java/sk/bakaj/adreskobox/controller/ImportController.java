package sk.bakaj.adreskobox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import sk.bakaj.adreskobox.service.FileService;
import java.io.File;

public class ImportController
{
    @FXML
    private TextField filePathField;

    private FileService fileService = new FileService();
    private File selectedFile;
    private String detectedDelimiter;

    @FXML
    private void handleSelectFile()
    {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"),
                new FileChooser.ExtensionFilter("Excel Files", "*.xls", "*.xlsx")
        );

        selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null)
        {
            filePathField.setText(selectedFile.getAbsolutePath());
            detectDelimiterAndType(selectedFile);
        }
    }
    private void  detectDelimiterAndType(File file)
    {
        try
        {
            detectedDelimiter = fileService.detectFileDelimiter(file);
        }
        catch (Exception e)
        {
            showAlert(Alert.AlertType.ERROR, "Chyba detekcie",
                    "Nepodarilo sa detekovať formát alebo oddeľovač: " + e.getMessage());
            detectedDelimiter = null;
        }
    }

    public File getSelectedFile()
    {
        return selectedFile;
    }

    public String getDetectedDelimiter ()
    {
        return detectedDelimiter;
    }

    private void showAlert(Alert.AlertType type, String title, String content)
        {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setContentText(content);
            alert.showAndWait();
        }

}