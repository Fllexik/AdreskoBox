package sk.bakaj.adreskobox.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import sk.bakaj.adreskobox.model.LabelFormat;
import sk.bakaj.adreskobox.model.Parent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class PDFService
{
    private static final float DEFAULT_FONT_SIZE = 10f;
    private static final float LINE_HEIGHT = 12f; // Výška riadku v bodoch
    private static final float POINTS_PER_MM = 2.834645669f; // Konverzia mm na body
    private Font defaultFont;
    private BaseFont baseFont;

    public PDFService() {
        try {
            // Inicializácia fontu a BaseFont pre presné meranie
            baseFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1250, BaseFont.EMBEDDED);
            defaultFont = new Font(baseFont, DEFAULT_FONT_SIZE);
        } catch (Exception e) {
            defaultFont = new Font(Font.FontFamily.HELVETICA, DEFAULT_FONT_SIZE);
            try {
                baseFont = BaseFont.createFont();
            } catch (Exception ex) {
                // Fallback - použijeme odhad
                baseFont = null;
            }
        }
    }

    /**
     * Presné meranie šírky textu v bodoch
     */
    private float getTextWidth(String text) {
        if (text == null || text.isEmpty()) {
            return 0f;
        }

        if (baseFont != null) {
            // Presné meranie pomocí BaseFont
            return baseFont.getWidthPoint(text, DEFAULT_FONT_SIZE);
        } else {
            // Fallback - vylepšený odhad pre slovenské znaky
            float avgCharWidth = DEFAULT_FONT_SIZE * 0.55f; // Mierne znížené pre lepšiu presnosť
            return text.length() * avgCharWidth;
        }
    }
    /**
     * Generuje štítky pre zoznam rodičov - OPRAVENÁ VERZIA
     * Ak je rodičov viac ako sa zmestí na jednú stranu, vytvorí sa viac strán
     */
    public void generateLabels(List<Parent> parents, LabelFormat format, File outputFile) throws IOException, DocumentException
    {
        try
        {
            Document document = new Document(new Rectangle(595, 842)); // A4
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(outputFile));
            document.open();

            float labelWidth = (float) format.getWidth() * POINTS_PER_MM;
            float labelHeight = (float) format.getHeight() * POINTS_PER_MM;

            int currentColumn = 0;
            int currentRow = 0;

            // Získame priamy prístup k obsahu PDF
            PdfContentByte canvas = writer.getDirectContent();

            System.out.println("=== PDF GENEROVANIE ===");
            System.out.println("Počet štítkov: " + parents.size());
            System.out.println("Rozmery štítka: " + format.getWidth() + "x" + format.getHeight() + " mm");
            System.out.println("Rozmery v bodoch: " + labelWidth + "x" + labelHeight);
            System.out.println("Stĺpce x riadky: " + format.getColumns() + "x" + format.getRows());

            for (int parentIndex = 0; parentIndex < parents.size(); parentIndex++)
            {
                Parent parent = parents.get(parentIndex);

                // Výpočet pozície štítka - OPRAVENÝ
                float x = (float) (format.getLeftMargin() * POINTS_PER_MM +
                        currentColumn * (labelWidth + format.getHorizontalGap() * POINTS_PER_MM));

                // OPRAVA: Y súradnica - správny výpočet zhora dole
                float y = (float) (842 - format.getTopMargin() * POINTS_PER_MM -
                        currentRow * (labelHeight + format.getVerticalGap() * POINTS_PER_MM) - labelHeight);

                System.out.println("\nŠtítok " + (parentIndex + 1) + " (" + parent.getFullName() + "):");
                System.out.println("  Pozícia: [" + currentColumn + "," + currentRow + "]");
                System.out.println("  Súradnice: x=" + x + ", y=" + y);

                // Získanie riadkov štítka
                String[] labelLines = parent.getLabelLines();

                // Vytvorenie textu pre štítok
                StringBuilder labelText = new StringBuilder();
                int validLines = 0;

                for (String line : labelLines) {
                    if (line != null && !line.trim().isEmpty()) {
                        if (validLines > 0) {
                            labelText.append("\n");
                        }
                        labelText.append(line.trim());
                        validLines++;
                    }
                }

                if (labelText.length() == 0) {
                    System.out.println("  VAROVANIE: Prázdny štítok!");
                    labelText.append("Prázdny štítok");
                }

                System.out.println("  Text štítka (" + validLines + " riadky):");
                System.out.println("    " + labelText.toString().replace("\n", "\n    "));

                // Vytvorenie odstavca
                Paragraph label = new Paragraph(labelText.toString(), defaultFont);
                label.setAlignment(Element.ALIGN_LEFT);
                label.setLeading(LINE_HEIGHT);

                // OPRAVA: Použitie ColumnText s správnymi súradnicami
                ColumnText ct = new ColumnText(canvas);

                // Nastavenie obdĺžnika pre text - y súradnice opravené
                float textAreaX1 = x + 2; // Malý okraj zleva
                float textAreaY1 = y + 2; // Malý okraj zdola
                float textAreaX2 = x + labelWidth - 2; // Malý okraj zprava
                float textAreaY2 = y + labelHeight - 2; // Malý okraj zhora

                ct.setSimpleColumn(textAreaX1, textAreaY1, textAreaX2, textAreaY2,
                        LINE_HEIGHT, Element.ALIGN_LEFT);

                ct.addElement(label);

                // Vyrenderovanie textu
                int result = ct.go();

                if (result == ColumnText.NO_MORE_TEXT) {
                    System.out.println("  ✓ Štítok úspešne vytvorený");
                } else {
                    System.out.println("  ⚠ Štítok nemusí byť kompletný (kód: " + result + ")");
                }

                // DEBUG: Nakreslenie rámčeka okolo štítka (voliteľné)
                if (System.getProperty("debug.pdf.borders", "false").equals("true")) {
                    canvas.rectangle(x, y, labelWidth, labelHeight);
                    canvas.stroke();
                }

                // Presun na ďalší štítok
                currentColumn++;
                if (currentColumn >= format.getColumns())
                {
                    currentColumn = 0;
                    currentRow++;
                }
                if (currentRow >= format.getRows())
                {
                    currentRow = 0;
                    System.out.println("\n=== NOVÁ STRÁNKA ===");
                    document.newPage();
                }
            }

            document.close();
            System.out.println("\n=== PDF DOKONČENÉ ===");
            System.out.println("Súbor uložený: " + outputFile.getAbsolutePath());

        } catch (DocumentException | IOException e) {
            System.err.println("CHYBA pri generovaní PDF: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Chyba pri generovaní PDF: " + e.getMessage(), e);
        }
    }

    /**
     * Kontroluje, či sa text zmestí na štítok s danými rozmermi
     * OPRAVENÁ VERZIA - rozmery štítka sú už v mm, netreba ich konvertovať
     */
    public boolean checkIfTextFitsOnLabel(String line1, String line2, String line3, LabelFormat format) {
        try {
            // Rozmery štítka v mm konvertujeme na body
            float labelWidthPoints = (float) format.getWidth() * POINTS_PER_MM;
            float labelHeightPoints = (float) format.getHeight() * POINTS_PER_MM;

            // Presné meranie šírky každého riadku
            float line1Width = getTextWidth(line1 != null ? line1 : "");
            float line2Width = getTextWidth(line2 != null ? line2 : "");
            float line3Width = getTextWidth(line3 != null ? line3 : "");

            // Kontrola šírky - rezerva pre okraje
            float widthReserve = 2f; // 6 bodov rezerva pre okraje (približne 2mm)
            float maxLineWidth = Math.max(Math.max(line1Width, line2Width), line3Width);

            if (maxLineWidth > (labelWidthPoints - widthReserve)) {
                System.out.println("DEBUG: Štítok je príliš úzky");
                System.out.println("  Najširší riadok: " + maxLineWidth + " bodov");
                System.out.println("  Dostupná šírka: " + (labelWidthPoints - widthReserve) + " bodov");
                return false;
            }

            // Kontrola výšky - počítame len neprázdne riadky
            int nonEmptyLines = 0;
            if (line1 != null && !line1.trim().isEmpty()) nonEmptyLines++;
            if (line2 != null && !line2.trim().isEmpty()) nonEmptyLines++;
            if (line3 != null && !line3.trim().isEmpty()) nonEmptyLines++;

            float totalHeight = nonEmptyLines * LINE_HEIGHT;
            float heightReserve = 4f; // 4 body rezerva pre okraje

            if (totalHeight > (labelHeightPoints - heightReserve)) {
                System.out.println("DEBUG: Štítok je príliš nízky");
                System.out.println("  Potrebná výška: " + totalHeight + " bodov");
                System.out.println("  Dostupná výška: " + (labelHeightPoints - heightReserve) + " bodov");
                return false;
            }

            System.out.println("DEBUG: Štítok vyhovuje");
            System.out.println("  Rozmery štítka: " + format.getWidth() + "x" + format.getHeight() + " mm");
            System.out.println("  V bodoch: " + labelWidthPoints + "x" + labelHeightPoints);
            System.out.println("  Najširší riadok: " + maxLineWidth + " bodov");
            System.out.println("  Potrebná výška: " + totalHeight + " bodov");

            return true;

        } catch (Exception e) {
            System.err.println("Chyba pri kontrole veľkosti štítka: " + e.getMessage());
            return false;
        }
    }

    /**
     * Kontroluje, či sa celý formátovaný text zmestí na štítok
     */
    public boolean checkIfLabelFits(Parent parent, LabelFormat format) {
        // Získanie jednotlivých riadkov štítka
        String[] lines = parent.getLabelLines();

        return checkIfTextFitsOnLabel(lines[0], lines[1], lines[2], format);
    }

    /**
     * Získa najdlhší riadok z formátovaného štítka
     */
    public String getLongestLine(Parent parent) {
        String[] lines = parent.getLabelLines();

        String longest = lines[0] != null ? lines[0] : "";
        for (int i = 1; i < lines.length; i++) {
            if (lines[i] != null && getTextWidth(lines[i]) > getTextWidth(longest)) {
                longest = lines[i];
            }
        }

        return longest;
    }

    /**
     * Získa šírku najdlhšieho riadku v bodoch (pre debugging)
     */
    public float getLongestLineWidth(Parent parent) {
        String[] lines = parent.getLabelLines();

        float maxWidth = 0f;
        for (String line : lines) {
            if (line != null) {
                float width = getTextWidth(line);
                if (width > maxWidth) {
                    maxWidth = width;
                }
            }
        }

        return maxWidth;
    }

    /**
     * Metóda pre skratenie adries ak sú príliš dlhé
     */
    public String abbreviateAddressIfNeeded(String address, int maxLength)
    {
        if (address == null || address.length() <= maxLength)
        {
            return address;
        }

        // Inteligentnejšie skracovanie pre slovenské adresy
        String result = address;

        // Najprv skúsime základné skratky
        result = result.replace("ulica", "ul.")
                .replace("Ulica", "Ul.")
                .replace("námestie", "nám.")
                .replace("Námestie", "Nám.")
                .replace("trieda", "tr.")
                .replace("Trieda", "Tr.");

        // Ak je stále príliš dlhé, skúsime rozdeliť a skrátiť
        if (result.length() > maxLength) {
            String[] parts = result.split(",");
            StringBuilder shortened = new StringBuilder();

            for (String part : parts) {
                String trimmed = part.trim();
                if (shortened.length() + trimmed.length() + 2 <= maxLength) {
                    if (shortened.length() > 0) {
                        shortened.append(", ");
                    }
                    shortened.append(trimmed);
                } else {
                    break;
                }
            }
            result = shortened.toString();
        }

        return result;
    }

    /**
     * Testovacia metóda pre debugging rozmerov štítka
     */
    public void debugLabelSize(Parent parent, LabelFormat format) {
        String[] lines = parent.getLabelLines();

        float labelWidthPoints = (float) format.getWidth() * POINTS_PER_MM;
        float labelHeightPoints = (float) format.getHeight() * POINTS_PER_MM;
        float widthReserve = 6f;  // rezervy ako v checkIfTextFitsOnLabel
        float heightReserve = 4f;

        float allowedWidth = labelWidthPoints - widthReserve;
        float allowedHeight = labelHeightPoints - heightReserve;

        System.out.println("=== DEBUG INFO PRE ŠTÍTOK ===");
        System.out.println("Meno: " + parent.getFullName());
        System.out.printf("Rozmery štítka: %.2f mm x %.2f mm\n", format.getWidth(), format.getHeight());
        System.out.printf("Rozmery v bodoch: %.2f x %.2f (šírka x výška)\n", labelWidthPoints, labelHeightPoints);
        System.out.printf("Dostupná šírka (bez rezervy): %.2f bodov\n", allowedWidth);
        System.out.printf("Dostupná výška (bez rezervy): %.2f bodov\n\n", allowedHeight);

        float totalHeight = 0f;
        float maxLineWidth = 0f;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i] != null ? lines[i].trim() : "";
            if (!line.isEmpty()) {
                float width = getTextWidth(line);
                totalHeight += LINE_HEIGHT;
                maxLineWidth = Math.max(maxLineWidth, width);
                System.out.printf("Riadok %d: '%s' → %.2f bodov\n", i + 1, line, width);
            }
        }

        System.out.println();
        System.out.printf("Najširší riadok: %.2f bodov\n", maxLineWidth);
        System.out.printf("Celková výška: %.2f bodov\n", totalHeight);

        boolean fitsWidth = maxLineWidth <= allowedWidth;
        boolean fitsHeight = totalHeight <= allowedHeight;
        boolean fits = fitsWidth && fitsHeight;

        System.out.println("\nVýsledok kontroly:");
        System.out.println(" - Šírka " + (fitsWidth ? "VYHOVUJE ✅" : "NEVYHOVUJE ❌"));
        System.out.println(" - Výška " + (fitsHeight ? "VYHOVUJE ✅" : "NEVYHOVUJE ❌"));
        System.out.println("CELKOVÝ STAV: " + (fits ? "VYHOVUJE ✅" : "NEVYHOVUJE ❌"));
        System.out.println("===========================\n");
    }

}