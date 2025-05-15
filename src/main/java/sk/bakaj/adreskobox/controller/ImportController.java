package sk.bakaj.adreskobox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import sk.bakaj.adreskobox.model.LabelFormat;
import sk.bakaj.adreskobox.service.FileService;
import java.io.File;

public class ImportController
{
    @FXML
    private TextField filePathField;

    @FXML
    private Label fileTypeLabel;

    @FXML
    private Label delimiterLabel;

    @FXML
    private Label headerStatusLabel;

    @FXML
    private CheckBox hasHeaderCheckBox;

    @FXML
    private ComboBox<LabelFormat> predefinedFormatsComboBox;

    @FXML
    private Label selectedFormatLabel;

    private FileService fileService = new FileService();
    private File selectedFile;
    private String detectedFileType;
    private String detectedDelimiter;
    private boolean hasHeader = true;
    private LabelFormat selectedFormat;

    @FXML
    public void initialize()
    {
        //naplnenie Comboboxu predefinovanymi formatami
        predefinedFormatsComboBox.setItems(LabelFormat.getPredefinedFormats());

        //Na začiatku predpokladame, že subor ma hlavičku
        hasHeaderCheckBox.setSelected(true);

        //Listener pre zmenu stavu checkboxu
        hasHeaderCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> hasHeader = newValue;
        if (selectedFile != null)
        {
            updateHeaderStatusLabel();
        };
    }

    @FXML
    private void handleSelectFile()
    {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Vyber zdrojový suboru");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Všetky podporované súbory", "*.csv", "*.xls", "*.xlsx"),
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"),
                new FileChooser.ExtensionFilter("Excel Files", "*.xls", "*.xlsx")
        );

        File file = fileChooser.showOpenDialog(filePathField.getScene().getWindow());
        if (file != null)
        {
            selectedFile = file;
            filePathField.setText(selectedFile.getAbsolutePath());
            detectedFileTypeAndDelimiter(file);
            detectHeaderRow(file);
        }

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