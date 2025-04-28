package sk.bakaj.adreskobox.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ImportController
{
    @FXML
    private TextField filePathField;

    @FXML
    private ComboBox<String> delimeterComboBox;

    @FXML
    private CheckBox hasHeaderCheckBox;

    @FXML
    private ComboBox<String> parent1NameComboBox;

    @FXML
    private ComboBox<String> parent1AddressComboBox;

    @FXML
    private ComboBox<String> parent2NameComboBox;

    @FXML
    private ComboBox<String> parent2AddressComboBox;

    @FXML
    private ComboBox<String> studentNameComboBox;

    @FXML
    private ComboBox<String> predefinedFormatsComboBox;

    @FXML
    private TextField widthField;

    @FXML
    private TextField heightField;

    @FXML
    private TextField columnsField;

    @FXML
    private TextField rowsField;

    @FXML
    private TextField leftMarginField;

    @FXML
    private TextField rightMarginField;

    @FXML
    private TextField topMarginField;

    @FXML
    private TextField bottomMarginField;

    @FXML
    private TextField horizontalGapField;

    @FXML
    private TextField verticalGapField;

    private final Map<String, double[]> predefinedFormats = new HashMap<>();

    @FXML
    private void initialize()
    {
        delimeterComboBox.setItems(FXCollections.observableArrayList(
                "Čiarka (,)", "Bodkočiarka (;)", "Tabulátor (\\t)"
        ));
        delimeterComboBox.getSelectionModel().selectFirst();

        //Inincializácia preddefinovaných formátov etikiet s okrajmi
        //Formát: [širka, výška, počet stĺpcov, počet riadkov, ľavý okraj, pravý okraj, horný okraj, dolny okraj, medzera riadkov, medezra stĺpcov]
        predefinedFormats.put("48.3 x 16.9 mm (S&K Label)", new double[]{48.3, 16.9, 4, 16, 7.5, 7.5, 12, 12, 0.0, 0.0});
        predefinedFormats.put("48.3 x 16.9 mm (Multi 3)", new double[]{48.5, 16.9, 4, 17, 7.5, 7.5, 5, 4.4, 0.0, 0.0});

        predefinedFormatsComboBox.setItems(FXCollections.observableArrayList(predefinedFormats.keySet()));

        //Nastavenie predvolených hodnôt pre okraje
        leftMarginField.setText("7.5");
        rightMarginField.setText("7.5");
        topMarginField.setText("5");
        bottomMarginField.setText("4.4");
        horizontalGapField.setText("0.0");
        verticalGapField.setText("0.0");
    }

    @FXML
    private void handleSelectFile()
    {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Výberte súbor s údajmi");

        //Nastavenie filtrov pre súbory
        FileChooser.ExtensionFilter csvFilter =
                new FileChooser.ExtensionFilter("CSV súbory (*.csv)", "*.csv");
        FileChooser.ExtensionFilter excelFilter =
                new FileChooser.ExtensionFilter("Excel súbory (*.xlsx)", "*.xlsx", ".xls");
        FileChooser.ExtensionFilter allFilter =
                new FileChooser.ExtensionFilter("Všetky podporované súbory", "*.csv", "*.xlsx", "*.xls");

        fileChooser.getExtensionFilters().addAll(allFilter, csvFilter, excelFilter);

        //Výber súboru
        File selectedFile = fileChooser.showOpenDialog(filePathField.getScene().getWindow());

        if (selectedFile != null)
        {
            filePathField.setText(selectedFile.getAbsolutePath());

            //Automatická detekcia typu súboru podľa prípony
            String fileName = selectedFile.getName().toLowerCase();
            boolean isCSV = fileName.endsWith(".csv");

            //Aktivácia/deaktivácia nastavenia pre CSV
            delimeterComboBox.setDisable(!isCSV);

            // Pokusiť sa prečítať hlavičky a naplniť combobox
            loadHeadersFromFile(selectedFile);
        }
    }

    @FXML
    private void handlePredefinedFormatSelection()
    {
        String selectedFormat = predefinedFormatsComboBox.getSelectionModel().getSelectedItem();
        if (selectedFormat != null && predefinedFormats.containsKey(selectedFormat))
        {
            double[] dimensions = predefinedFormats.get(selectedFormat);
            widthField.setText(String.valueOf(dimensions[0]));
            heightField.setText(String.valueOf(dimensions[1]));
            columnsField.setText(String.valueOf(dimensions[2]));
            rowsField.setText(String.valueOf(dimensions[3]));

            //Nastavenie okrajov a medzier
            if (dimensions.length > 4)
            {
                leftMarginField.setText(String.valueOf(dimensions[4]));
                rightMarginField.setText(String.valueOf(dimensions[5]));
                topMarginField.setText(String.valueOf(dimensions[6]));
                bottomMarginField.setText(String.valueOf(dimensions[7]));
            }

            if (dimensions.length > 8)
            {
                horizontalGapField.setText(String.valueOf(dimensions[8]));
                verticalGapField.setText(String.valueOf(dimensions[9]));
            }
        }
    }

    @FXML
    private void handlePreviewData()
    {
        //Implementácia zobrazení náhľadu dát
        //Toto by malo otvoriť nové okno sos vzorkou dát
        if (filePathField.getText().isEmpty())
        {
            showAlert(Alert.AlertType.WARNING, "Chýbajucí súbor", "Najprv vyberte súbor na import");
            return;
        }

        try
        {
            //Tu by mala byť implementácia náhľadu dát zo súboru
            // Napríklad zobrazenie prvých 10 riadkov v tabuľke

            //Dočasné riešenie - len Informácia
            showAlert(Alert.AlertType.INFORMATION, "Náhľad dát",
                    "Táto funkcia zobrazí náhľad prvých 10 riadkov importovaného súboru.\n\n" +
                            "Cesta k súboru: " + filePathField.getText());
        } catch (Exception e)
        {
            showAlert(Alert.AlertType.ERROR, "Chyba pri načítaní",
                    "Nastala chyba pri načítaní súboru: " + e.getMessage());
        }
    }

    @FXML
    private void handleImportData()
    {
        //implementácia importu dát a prechod na ďalšiu záložku
        if (validateInput())
        {
            try
            {
                //Tu by mala byť skutočná implementácia dát
                //1. načitanie dát zo súboru
                //2. spracovanie podľa mapovania stĺpcov
                //3. uloženie nastavení etikiet
                //4. prechod na dalšiu záložku

                showAlert(Alert.AlertType.INFORMATION, "Import dát",
                        "Udaje boli úspešne importované. Môžete pokračovať na dalšiu záložku.");

                //Prechod na dalšiu záložku by sa mohol implementovať takto:
                //TabPane tabPane = (TabPane) filePathField.getScene().lookup("#mainTapPane");
                //tabPane.getSelectionModel().select(1);
            } catch (Exception e)
            {
                showAlert(Alert.AlertType.ERROR, "Chyba pri importe",
                        "Nastala chyba pri importe údajov: " + e.getMessage());
            }
        }
    }

    private boolean validateInput()
    {
        //Validácia všetkych vstupov
        StringBuilder errorMessage = new StringBuilder();

        if (filePathField.getText().isEmpty())
        {
            errorMessage.append("- Nieje vybraný žiaden súbor.\n");
        }

        //Validácia výberu stĺpcov
        if (parent1NameComboBox.getSelectionModel().isEmpty() && parent2NameComboBox.getSelectionModel().isEmpty())
        {
            errorMessage.append("- Musí byť vybraný aspoň jeden rodič.\n");
        }

        //Validácia rozmerov etikiet
        try
        {
            double width = validateDoubleField(widthField, "Šírka etikety");
            double height = validateDoubleField(heightField, "Výška etikeety");
            int colmnus = validateIntField(columnsField, "Počet stĺpcov");
            int rows = validateIntField(rowsField, "Počet riadkov");
            double leftMrgin = validateDoubleField(leftMarginField, "Ľavý okraj");
            double rightMrgin = validateDoubleField(rightMarginField, "Pravý okraj");
            double topMrgin = validateDoubleField(topMarginField, "Horný okraj");
            double bottomMrgin = validateDoubleField(bottomMarginField, "Dolný okraj");
            double horizontalGap = validateDoubleField(horizontalGapField, "Okraj medzi riadkami");
            double verticalGap = validateDoubleField(verticalGapField, "Okraj medzi stĺpcami");

            //Dodatočné validácie rozmerov...

        } catch (NumberFormatException e)
        {
            errorMessage.append(e.getMessage());
        }
        if (errorMessage.length() > 0)
        {
            showAlert(Alert.AlertType.ERROR, "Chyba pri importe",
                    "Opravte nasledujúce chyby:\n" + errorMessage.toString());
            return false;
        }
        return true;
    }

    private double validateDoubleField(TextField field, String fieldName) throws NumberFormatException
    {
        try
        {
            double value = Double.parseDouble(field.getText().trim());
            if (value <= 0)
            {
                throw new NumberFormatException("- " + fieldName + "musí byť kladné celé číslo.\n");
            }
            return value;
        } catch (NumberFormatException e)
        {
            throw new NumberFormatException("- " + fieldName + "musí byť platné celé číslo.\n");
        }
    }

    private void loadHeadersFromFile(File file)
    {
        //toto je len zjdnodušená implementácia
        //v skutočnej aplikácii by sa mali čitať hlavičky zo súboru podľa typu
        try
        {
            //Simuluje načítavanie hlavičiek z CSV/XLSX
            // V skutočnej aplikácii by ste použili FileService na čitanie skutočných hlavičiek
            ObservableList<String> headers;

            if (file.getName().toLowerCase().endsWith(".csv"))
            {
                //Simulácia načitania hlavičky z csv
                headers = FXCollections.observableArrayList(
                        "Meno rodiča 1", "Adresa rodiča 1",
                        "Meno rodiča 2", "Adresa rodiča 2",
                        "Meno študenta", "Trieda", "Poznámka"
                );
            } else
            {
                //Simulácia načitania hlavičky z XLSX/XLS
                headers = FXCollections.observableArrayList(
                        "Rodič1_meno", "Rodič_adresa",
                        "Rodič2_meno", "Rodič2_adresa",
                        "Študent", "trieda", "Poznámka"
                );
            }
            //Naplnenie comboboxov
            parent1NameComboBox.setItems(headers);
            parent1AddressComboBox.setItems(headers);
            parent2NameComboBox.setItems(headers);
            parent2AddressComboBox.setItems(headers);
            studentNameComboBox.setItems(headers);

            //Predvolený výber založený na názvoch stĺpcov
            if (file.getName().toLowerCase().endsWith(".csv"))
            {
                selectDefaultColumns(parent1NameComboBox, "Meno rodiča 1");
                selectDefaultColumns(parent1AddressComboBox, "Adresa rodiča 1");
                selectDefaultColumns(parent2NameComboBox, "Meno rodiča 2");
                selectDefaultColumns(parent2AddressComboBox, "Adresa rodiča 2");
                selectDefaultColumns(studentNameComboBox, "Meno študenta");
            } else
            {
                selectDefaultColumns(parent1NameComboBox, "Rodič1_meno");
                selectDefaultColumns(parent1AddressComboBox, "Rodič1_adresa");
                selectDefaultColumns(parent2NameComboBox, "Rodič2_meno");
                selectDefaultColumns(parent2AddressComboBox, "Rodič2_adresa");
                selectDefaultColumns(studentNameComboBox, "Študent");
            }
        }catch(Exception e)
        {
            showAlert(Alert.AlertType.ERROR, "Chyba pri načítaní súboru",
                    "Nastala chyba pri čítaní hlavičiek: "+e.getMessage());
        }
    }

    private void selectDefaultColumns(ComboBox<String> comboBox, String columnName)
    {
        ObservableList<String> items = comboBox.getItems();
        for (int i = 0; i < items.size(); i++)
        {
            if (items.get(i).equals(columnName))
            {
             comboBox.getSelectionModel().select(i);
             break;
            }
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String content)
    {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}