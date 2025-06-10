package sk.bakaj.adreskobox.controller;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import sk.bakaj.adreskobox.model.LabelFormat;

/**
 * Controller pre vytvorenie a úpravu vlastného formátu štítkov.
 * Umožňuje nastavenie všetkých parametrov štítkov s náhľadom v reálnom čase.
 */
public class CustomLabelFormatController
{
    // Konštanty pre rozmer A4 papiera v milimetroch
    private static final double A4_WIDTH_MM = 210.0;
    private static final double A4_HEIGHT_MM = 297.0;

    // FXML komponenty - základné parametre
    @FXML
    private TextField nameField;
    @FXML
    private TextField widthField;
    @FXML
    private TextField heightField;
    @FXML
    private TextField columnsField;
    @FXML
    private TextField rowsField;

    // FXML komponenty - okraje
    @FXML
    private TextField leftMarginField;
    @FXML
    private TextField rightMarginField;
    @FXML
    private TextField topMarginField;
    @FXML
    private TextField bottomMarginField;

    // FXML komponenty - medzery a ostatné
    @FXML
    private TextField horizontalGapField;
    @FXML
    private TextField verticalGapField;
    @FXML
    private TextField maxAddressLengthField;

    // FXML komponenty - UI prvky
    @FXML
    private Canvas previewCanvas;
    @FXML
    private Label errorLabel;

    // Výsledný formát štítkov
    private LabelFormat customLabelFormat;

    /**
     * Inicializácia controllera - nastavenie predvolených hodnôt a listenerov.
     */
    @FXML
    public void initialize()
    {
        initializeDefaultValues();
        attachUpdateListeners();
        updatePreview();
    }

    /**
     * Nastavenie predvolených hodnôt do formulára.
     */
    private void initializeDefaultValues()
    {
        nameField.setText("Vlastný formát");
        widthField.setText("48.3");
        heightField.setText("16.9");
        columnsField.setText("4");
        rowsField.setText("16");
        leftMarginField.setText("8.4");
        rightMarginField.setText("8.4");
        topMarginField.setText("13.3");
        bottomMarginField.setText("13.3");
        horizontalGapField.setText("0");
        verticalGapField.setText("0");
        maxAddressLengthField.setText("24");
    }
    /**
     * Pripojenie listenerov pre automatickú aktualizáciu náhľadu.
     */
    private void attachUpdateListeners()
    {
        // Pridanie listenerov pre všetky polia ovlivňujúce náhľad
        widthField.textProperty().addListener((obs, oldVal, newVal) -> updatePreview());
        heightField.textProperty().addListener((obs, oldVal, newVal) -> updatePreview());
        columnsField.textProperty().addListener((obs, oldVal, newVal) -> updatePreview());
        rowsField.textProperty().addListener((obs, oldVal, newVal) -> updatePreview());
        leftMarginField.textProperty().addListener((obs, oldVal, newVal) -> updatePreview());
        rightMarginField.textProperty().addListener((obs, oldVal, newVal) -> updatePreview());
        topMarginField.textProperty().addListener((obs, oldVal, newVal) -> updatePreview());
        bottomMarginField.textProperty().addListener((obs, oldVal, newVal) -> updatePreview());
        horizontalGapField.textProperty().addListener((obs, oldVal, newVal) -> updatePreview());
        verticalGapField.textProperty().addListener((obs, oldVal, newVal) -> updatePreview());
    }

    /**
     * Inicializácia formulára s existujúcim formátom štítkov.
     *
     * @param format Formát štítkov na načítanie do formulára
     */
    public void initWithFormat(LabelFormat format)
    {
        if (format != null)
        {
            return;
        }
            nameField.setText(format.getName() != null ? format.getName() : "Vlastný formát");
            widthField.setText(String.valueOf(format.getWidth()));
            heightField.setText(String.valueOf(format.getHeight()));
            columnsField.setText(String.valueOf(format.getColumns()));
            rowsField.setText(String.valueOf(format.getRows()));
            leftMarginField.setText(String.valueOf(format.getLeftMargin()));
            rightMarginField.setText(String.valueOf(format.getRightMargin()));
            topMarginField.setText(String.valueOf(format.getTopMargin()));
            bottomMarginField.setText(String.valueOf(format.getBottomMargin()));
            horizontalGapField.setText(String.valueOf(format.getHorizontalGap()));
            verticalGapField.setText(String.valueOf(format.getVerticalGap()));
            maxAddressLengthField.setText(String.valueOf(format.getMaxAddressLength()));

            updatePreview();
        }
    /**
     * Spracovanie uloženia nového formátu štítkov.
     * Validuje vstupné údaje a vytvorí nový LabelFormat objekt.
     */
    @FXML
    private void handleSave()
    {
        try
        {
            // Načítanie hodnôt z formulára
            String name = nameField.getText().trim();
            double width = Double.parseDouble(widthField.getText());
            double height = Double.parseDouble(heightField.getText());
            int columns = Integer.parseInt(columnsField.getText());
            int rows = Integer.parseInt(rowsField.getText());
            double leftMargin = Double.parseDouble(leftMarginField.getText());
            double rightMargin = Double.parseDouble(rightMarginField.getText());
            double topMargin = Double.parseDouble(topMarginField.getText());
            double bottomMargin = Double.parseDouble(bottomMarginField.getText());
            double horizontalGap = Double.parseDouble(horizontalGapField.getText());
            double verticalGap = Double.parseDouble(verticalGapField.getText());
            int maxAddressLength = Integer.parseInt(maxAddressLengthField.getText());

            // Validácia základných hodnôt
            if (!validatePositiveValues(width, height, columns, rows, maxAddressLength))
            {
                return;
            }

            // Validácia okrajov a medzier
            if (!validateNonNegativeValues(leftMargin, rightMargin, topMargin, bottomMargin, horizontalGap, verticalGap))
            {
                return;
            }

            // Kontrola zmestenia na A4 papier
            if (!validateA4Size(width, height, columns, rows, leftMargin, rightMargin, topMargin, bottomMargin, horizontalGap, verticalGap))
            {
                return;
            }

            // Vytvorenie názvu ak nie je zadaný
            if (name.isEmpty())
            {
                name = String.format("Vlastný - %.1f x %.1f mm (%dx%d ks)", width, height, columns, rows);
            }

            // Vytvorenie nového formátu štítkov
            customLabelFormat = new LabelFormat(name, width, height, columns, rows,
                    leftMargin, rightMargin, topMargin, bottomMargin, horizontalGap,
                    verticalGap, maxAddressLength);

            closeDialog();
        }
        catch (NumberFormatException e)
        {
            showError("Neplatný formát čísla. Skontrolujte, či sú všetky hodnoty správne zadané.");
        }
    }

    /**
     * Validácia kladných hodnôt.
     */
    private boolean validatePositiveValues(double width, double height, int columns, int rows, int maxAddressLength)
    {
        if (width <= 0 || height <= 0 || columns <= 0 || rows <= 0 || maxAddressLength <= 0)
        {
            showError("Rozmery, počty stĺpcov/riadkov a maximálna dĺžka adresy musia byť kladné čísla.");
            return false;
        }
        return true;
    }

    /**
     * Validácia nezáporných hodnôt.
     */
    private boolean validateNonNegativeValues(double... values)
    {
        for (double value : values)
        {
            if (value < 0)
            {
                showError("Okraje a medzery nemôžu byť záporné hodnoty.");
                return false;
            }
        }
        return true;
    }

    /**
     * Kontrola, či sa štítky zmestia na A4 papier.
     */
    private boolean validateA4Size(double width, double height, int columns, int rows,
                                   double leftMargin, double rightMargin, double topMargin, double bottomMargin,
                                   double horizontalGap, double verticalGap)
    {

        double totalWidth = leftMargin + (width * columns) + (horizontalGap * (columns - 1)) + rightMargin;
        double totalHeight = topMargin + (height * rows) + (verticalGap * (rows - 1)) + bottomMargin;

        if (totalWidth > A4_WIDTH_MM)
        {
            showError(String.format("Štítky presahujú šírku strany A4 (%.1f mm). Aktuálna šírka: %.1f mm",
                    A4_WIDTH_MM, totalWidth));
            return false;
        }

        if (totalHeight > A4_HEIGHT_MM)
        {
            showError(String.format("Štítky presahujú výšku strany A4 (%.1f mm). Aktuálna výška: %.1f mm",
                    A4_HEIGHT_MM, totalHeight));
            return false;
        }

        return true;
    }

    /**
     * Spracovanie zrušenia úprav.
     */
    @FXML
    private void handleCancel()
    {
        closeDialog();
    }

    /**
     * Aktualizácia náhľadu štítkov na plátno.
     * Vykreslí rozmiestnenie štítkov podľa aktuálnych nastavení.
     */
    private void updatePreview()
    {
        try
        {
            // Načítanie hodnôt z polí
            double width = Double.parseDouble(widthField.getText());
            double height = Double.parseDouble(heightField.getText());
            int columns = Integer.parseInt(columnsField.getText());
            int rows = Integer.parseInt(rowsField.getText());
            double leftMargin = Double.parseDouble(leftMarginField.getText());
            double rightMargin = Double.parseDouble(rightMarginField.getText());
            double topMargin = Double.parseDouble(topMarginField.getText());
            double bottomMargin = Double.parseDouble(bottomMarginField.getText());
            double horizontalGap = Double.parseDouble(horizontalGapField.getText());
            double verticalGap = Double.parseDouble(verticalGapField.getText());

            // Vykreslenie náhľadu
            drawPreview(width, height, columns, rows, leftMargin, rightMargin,
                    topMargin, bottomMargin, horizontalGap, verticalGap);

            // Skrytie chybovej správy ak je všetko v poriadku
            hideError();

        }
        catch (NumberFormatException e)
        {
            // Ignorujeme chyby počas písania - polia môžu byť dočasne prázdne
        }
    }

    /**
     * Vykreslenie náhľadu štítkov na plátno.
     */
    private void drawPreview(double width, double height, int columns, int rows,
                             double leftMargin, double rightMargin, double topMargin, double bottomMargin,
                             double horizontalGap, double verticalGap)
    {

        GraphicsContext gc = previewCanvas.getGraphicsContext2D();

        // Vyčistenie plátna
        gc.clearRect(0, 0, previewCanvas.getWidth(), previewCanvas.getHeight());

        // Vykreslenie okraja strany A4
        gc.setStroke(Color.DARKGRAY);
        gc.setLineWidth(2);
        gc.strokeRect(2, 2, previewCanvas.getWidth() - 4, previewCanvas.getHeight() - 4);

        // Nastavenie pre štítky
        gc.setFill(Color.LIGHTBLUE);
        gc.setStroke(Color.DARKBLUE);
        gc.setLineWidth(1);

        // Výpočet mierky pre náhľad
        double scaleX = (previewCanvas.getWidth() - 10) / A4_WIDTH_MM;
        double scaleY = (previewCanvas.getHeight() - 10) / A4_HEIGHT_MM;
        double scale = Math.min(scaleX, scaleY);

        // Vykreslenie jednotlivých štítkov
        for (int row = 0; row < rows; row++)
        {
            for (int col = 0; col < columns; col++)
            {
                double x = (leftMargin + col * (width + horizontalGap)) * scale + 5;
                double y = (topMargin + row * (height + verticalGap)) * scale + 5;
                double labelWidth = width * scale;
                double labelHeight = height * scale;

                // Vyplnenie a obrys štítka
                gc.fillRect(x, y, labelWidth, labelHeight);
                gc.strokeRect(x, y, labelWidth, labelHeight);
            }
        }
    }

    /**
     * Zobrazenie chybovej správy.
     *
     * @param message Text chybovej správy
     */
    private void showError(String message)
    {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    /**
     * Skrytie chybovej správy.
     */
    private void hideError()
    {
        errorLabel.setVisible(false);
    }

    /**
     * Zatvorenie dialógového okna.
     */
    private void closeDialog()
    {
        Stage stage = (Stage) widthField.getScene().getWindow();
        stage.close();
    }

    /**
     * Získanie vytvoreného formátu štítkov.
     *
     * @return Vytvorený formát štítkov alebo null ak nebol vytvorený
     */
    public LabelFormat getCustomLabelFormat()
    {
        return customLabelFormat;
    }
}
