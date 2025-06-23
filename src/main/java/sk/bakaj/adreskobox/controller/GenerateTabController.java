package sk.bakaj.adreskobox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import sk.bakaj.adreskobox.model.LabelFormat;
import sk.bakaj.adreskobox.model.Parent;
import sk.bakaj.adreskobox.service.ExcelService;
import sk.bakaj.adreskobox.service.PDFService;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Kontrolér pre záložku generovania výstupných súborov.
 * Spravuje proces generovania štítkov a podacích hárkov.
 */
public class GenerateTabController
{
    private static final Log log = LogFactory.getLog(GenerateTabController.class);

    @FXML
    private VBox rootVbox;

    @FXML
    private TextField outputDirField;

    @FXML
    private Label labelPagesCountLabel;

    @FXML
    private Label submissionSheetsCountLabel;

    @FXML
    private Button browseOutputDirButton;

    @FXML
    private Button generateButton;

    @FXML
    private Button startOverButton;

    @FXML
    private TextArea generationLogArea;

    // Vstupné dáta pre generovanie
    private List<Parent> selectedParents;
    private LabelFormat selectedLabelFormat;
    private String senderName;
    private String senderStreet;
    private String senderCity;
    private File templateFile;

    // Výstupné nastavenia a služby
    private File outputDirectory;
    private PDFService pdfService = new PDFService();
    private ExcelService excelService = new ExcelService();

    private List<File> generatedFiles = new ArrayList<>();

    /**
     * Inicializácia kontroléra po načítaní FXML.
     */
    @FXML
    public void initialize()
    {
        // Uloženie controllera do properties root elementu pre MainController
        if (rootVbox != null)
        {
            rootVbox.getProperties().put("controller", this);
        }

        //Predvolená cesta k výstupnému adresáru
        outputDirectory = new File(System.getProperty("user.home") + "/Documents/AdreskoBox");
        if (!outputDirectory.exists())
        {
            outputDirectory.mkdir();
        }
        outputDirField.setText(outputDirectory.getAbsolutePath());
    }

    /**
     * Obsluha tlačidla pre výber výstupného adresára.
     */
    @FXML
    private void handleBrowseOutputDir()
    {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Vyberte výstupný adresár");

        if (outputDirectory != null && outputDirectory.exists())
        {
            directoryChooser.setInitialDirectory(outputDirectory);
        }

        File dir = directoryChooser.showDialog(outputDirField.getScene().getWindow());
        if (dir != null)
        {
            outputDirectory = dir;
            outputDirField.setText(outputDirectory.getAbsolutePath());
        }
    }

    /**
     * Hlavná metóda pre generovanie výstupných súborov.
     */
    @FXML
    private void handleGenerate()
    {
        if (outputDirectory == null || !outputDirectory.exists())
        {
            showAlert(Alert.AlertType.ERROR, "Chyba", "Výstupný adresár neexistuje.");
            return;
        }

        generationLogArea.clear();
        generatedFiles.clear();

        try
        {
            log("Začínam generovanie výstupných súborov...");

            // Kontrola vstupných údajov
            if (selectedParents == null || selectedParents.isEmpty()) {
                throw new IllegalStateException("Žiadni rodičia nie sú vybraní!");
            }

            if (selectedLabelFormat == null) {
                throw new IllegalStateException("Formát štítkov nie je vybraný!");
            }

            //Generovanie štítkov
            int labelsPerPage = selectedLabelFormat.getColumns() * selectedLabelFormat.getRows();
            int totalPages = (int) Math.ceil(selectedParents.size() / (double) labelsPerPage);

            log("\nGenerujem štítky:");
            log("- Počet štítkov: " + selectedParents.size());
            log("- Štítkov na stranu: " + labelsPerPage + " (" + selectedLabelFormat.getColumns() + "x" + selectedLabelFormat.getRows() + ")");
            log("- Celkový počet strán: " + totalPages);
            log("- Rozmer štítku: " + selectedLabelFormat.getWidth() + "x" + selectedLabelFormat.getHeight() + "mm");

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            File labelsFile = new File(outputDirectory, "Stitky_" + timestamp + ".pdf");

            log("- Začínam generovanie PDF súboru: " + labelsFile.getName());

            try {
                pdfService.generateLabels(selectedParents, selectedLabelFormat, labelsFile);
                generatedFiles.add(labelsFile);
                log("- ✓ Štítky boli úspešne vygenerované do súboru: " + labelsFile.getName());
            } catch (Exception e) {
                log("- ✗ CHYBA pri generovaní štítkov: " + e.getMessage());
                throw e;
            }

            //Generovanie podacích hárkov
            log("\nGenerujem podacie hárky:");
            log("- Počet príjemcov: " + selectedParents.size());
            log("- Šablóna: " + (templateFile != null && templateFile.exists() ? "OK" : "CHÝBA"));

            // Nastavenie výstupného adresára pre ExcelService
            excelService.setOutputDirectory(outputDirectory);

            if (templateFile == null || !templateFile.exists()) {
                log("- ⚠ VAROVANIE: Šablóna podacieho hárku nebola nájdená!");
                log("- Pokúšam sa vytvoriť novú šablónu...");

                try {
                    templateFile = excelService.createNewSubmissionTemplate();
                    log("- ✓ Nová šablóna vytvorená: " + templateFile.getAbsolutePath());
                } catch (Exception e) {
                    log("- ✗ CHYBA pri vytváraní šablóny: " + e.getMessage());
                    throw new RuntimeException("Nepodarilo sa vytvoriť šablónu podacieho hárku", e);
                }
            }

            try {
                List<File> submissionSheets = excelService.createSubmissionSheets(
                        selectedParents, senderName, senderStreet, senderCity,
                        templateFile.getAbsolutePath());

                generatedFiles.addAll(submissionSheets);

                log("- ✓ Počet vygenerovaných podacích hárkov: " + submissionSheets.size());
                for (int i = 0; i < submissionSheets.size(); i++)
                {
                    log("  " + (i + 1) + ". " + submissionSheets.get(i).getName());
                }
            } catch (Exception e) {
                log("- ✗ CHYBA pri generovaní podacích hárkov: " + e.getMessage());
                e.printStackTrace();
                // Neprerušujeme proces - aspoň štítky sú vygenerované
                log("- Pokračujem bez podacích hárkov...");
            }

            log("\n✓ Generovanie dokončené. Všetky súbory boli uložené do: " + outputDirectory.getAbsolutePath());

            //Aktualizácia počítadiel
            labelPagesCountLabel.setText(String.valueOf(totalPages));

            // Bezpečný výpočet podacích hárkov
            int sheetsCount = (int) Math.ceil(selectedParents.size() / 12.0);
            submissionSheetsCountLabel.setText(String.valueOf(sheetsCount));

            showAlert(Alert.AlertType.INFORMATION, "Generovanie dokončené",
                    "Štítky boli úspešne vygenerované.\n" +
                            "Súbory sa nachádzajú v: " + outputDirectory.getAbsolutePath());

        } catch (Exception e)
        {
            log("\n✗ KRITICKÁ CHYBA pri generovaní: " + e.getMessage());

            showAlert(Alert.AlertType.ERROR, "Chyba pri generovaní",
                    "Nastala chyba pri generovaní súborov:\n\n" +
                            e.getMessage() + "\n\n" +
                            "Skontrolujte log pre viac detailov.");
        }
    }

    /**
     * Obsluha tlačidla "Začať odznova".
     */
    @FXML
    private void handleStartOver()
    {
        // TODO: Implementovať reset aplikácie
        showAlert(Alert.AlertType.INFORMATION, "Začať odznova",
                "Táto funkcionalita bude implementovaná v ďalšej verzii.");
    }

    /**
     * Otvorí výstupný adresár v systémovom správcovi súborov.
     */
    @FXML
    private void handleOpenOutputDir()
    {
        if (outputDirectory != null && outputDirectory.exists())
        {
            try
            {
                java.awt.Desktop.getDesktop().open(outputDirectory);
            } catch (IOException e)
            {
                showAlert(Alert.AlertType.ERROR, "Chyba",
                        "Nepodarilo sa otvoriť výstupný adresár: " + e.getMessage());
            }
        }
    }

    /**
     * Nastaví vstupné dáta potrebné pre generovanie.
     *
     * @param parents Zoznam rodičov
     * @param labelFormat Formát štítkov
     * @param senderName Meno odosielateľa
     * @param senderStreet Ulica odosielateľa
     * @param senderCity Mesto odosielateľa
     * @param templateFile Súbor šablóny pre podacie hárky
     */
    public void setData(List<Parent> parents, LabelFormat labelFormat,
                        String senderName, String senderStreet, String senderCity,
                        File templateFile)
    {
        this.selectedParents = parents;
        this.selectedLabelFormat = labelFormat;
        this.senderName = senderName;
        this.senderStreet = senderStreet;
        this.senderCity = senderCity;
        this.templateFile = templateFile;

        //Aktualizácia počítadiel
        if (selectedLabelFormat != null && selectedParents != null) {
            int labelsPerPage = selectedLabelFormat.getColumns() * selectedLabelFormat.getRows();
            int totalPages = (int) Math.ceil(selectedParents.size() / (double) labelsPerPage);
            labelPagesCountLabel.setText(String.valueOf(totalPages));

            int sheetsCount = (int) Math.ceil(selectedParents.size() / 12.0);
            submissionSheetsCountLabel.setText(String.valueOf(sheetsCount));
        }
    }

    /**
     * Pridá správu do logu aplikácie aj konzoly.
     */
    private void log(String message)
    {
        System.out.println(message); // Pre ladenie v konzole
        generationLogArea.appendText(message + "\n");
    }

    /**
     * Zobrazí alert dialóg.
     */
    private void showAlert(Alert.AlertType type, String title, String content)
    {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
