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

    public PDFService()
    {
        try {
            // Inicializácia fontu a BaseFont pre presné meranie
            baseFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1250, BaseFont.EMBEDDED);
            defaultFont = new Font(baseFont, DEFAULT_FONT_SIZE);
        }
        catch (Exception e)
        {
            defaultFont = new Font(Font.FontFamily.HELVETICA, DEFAULT_FONT_SIZE);
            try
            {
                baseFont = BaseFont.createFont();
            }
            catch (Exception ex)
            {
                // Fallback - použijeme odhad
                baseFont = null;
            }
        }
    }

    /**
     * Presné meranie šírky textu v bodoch
     */
    private float getTextWidth(String text)
    {
        if (text == null || text.isEmpty())
        {
            return 0f;
        }

        if (baseFont != null)
        {
            // Presné meranie pomocí BaseFont
            return baseFont.getWidthPoint(text, DEFAULT_FONT_SIZE);
        }
        else
        {
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

            for (int parentIndex = 0; parentIndex < parents.size(); parentIndex++)
            {
                Parent parent = parents.get(parentIndex);

                // Výpočet pozície štítka
                float x = (float) (format.getLeftMargin() * POINTS_PER_MM +
                        currentColumn * (labelWidth + format.getHorizontalGap() * POINTS_PER_MM));

                // Y súradnica - správny výpočet zhora dole
                float y = (float) (842 - format.getTopMargin() * POINTS_PER_MM -
                        currentRow * (labelHeight + format.getVerticalGap() * POINTS_PER_MM) - labelHeight);


                // Získanie riadkov štítka
                String[] labelLines = parent.getLabelLines();

                // Vytvorenie textu pre štítok
                StringBuilder labelText = new StringBuilder();
                int validLines = 0;

                for (String line : labelLines)
                {
                    if (line != null && !line.trim().isEmpty())
                    {
                        if (validLines > 0)
                        {
                            labelText.append("\n");
                        }
                        labelText.append(line.trim());
                        validLines++;
                    }
                }

                if (labelText.length() == 0)
                {
                    labelText.append("Prázdny štítok");
                }

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
                    document.newPage();
                }
            }

            document.close();

        }
        catch (DocumentException | IOException e)
        {
            throw new RuntimeException("Chyba pri generovaní PDF: " + e.getMessage(), e);
        }
    }

    /**
     * Kontroluje, či sa text zmestí na štítok s danými rozmermi
     * OPRAVENÁ VERZIA - rozmery štítka sú už v mm, netreba ich konvertovať
     */
    public boolean checkIfTextFitsOnLabel(String line1, String line2, String line3, LabelFormat format)
    {
        try
        {
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

            if (maxLineWidth > (labelWidthPoints - widthReserve))
            {
                return false;
            }

            // Kontrola výšky - počítame len neprázdne riadky
            int nonEmptyLines = 0;
            if (line1 != null && !line1.trim().isEmpty()) nonEmptyLines++;
            if (line2 != null && !line2.trim().isEmpty()) nonEmptyLines++;
            if (line3 != null && !line3.trim().isEmpty()) nonEmptyLines++;

            float totalHeight = nonEmptyLines * LINE_HEIGHT;
            float heightReserve = 4f; // 4 body rezerva pre okraje

            if (totalHeight > (labelHeightPoints - heightReserve))
            {
                return false;
            }

            return true;

        }
        catch (Exception e)
        {
            return false;
        }
    }

    /**
     * Kontroluje, či sa celý formátovaný text zmestí na štítok
     */
    public boolean checkIfLabelFits(Parent parent, LabelFormat format)
    {
        // Získanie jednotlivých riadkov štítka
        String[] lines = parent.getLabelLines();

        return checkIfTextFitsOnLabel(lines[0], lines[1], lines[2], format);
    }

    /**
     * Získa najdlhší riadok z formátovaného štítka
     */
    public String getLongestLine(Parent parent)
    {
        String[] lines = parent.getLabelLines();

        String longest = lines[0] != null ? lines[0] : "";
        for (int i = 1; i < lines.length; i++) {
            if (lines[i] != null && getTextWidth(lines[i]) > getTextWidth(longest))
            {
                longest = lines[i];
            }
        }

        return longest;
    }

    /**
     * Získa šírku najdlhšieho riadku v bodoch (pre debugging)
     */
    public float getLongestLineWidth(Parent parent)
    {
        String[] lines = parent.getLabelLines();

        float maxWidth = 0f;
        for (String line : lines)
        {
            if (line != null)
            {
                float width = getTextWidth(line);
                if (width > maxWidth)
                {
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
        if (result.length() > maxLength)
        {
            String[] parts = result.split(",");
            StringBuilder shortened = new StringBuilder();

            for (String part : parts)
            {
                String trimmed = part.trim();
                if (shortened.length() + trimmed.length() + 2 <= maxLength)
                {
                    if (shortened.length() > 0)
                    {
                        shortened.append(", ");
                    }
                    shortened.append(trimmed);
                }
                else
                {
                    break;
                }
            }
            result = shortened.toString();
        }

        return result;
    }
}