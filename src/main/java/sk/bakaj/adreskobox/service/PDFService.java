package sk.bakaj.adreskobox.service;

import com.itextpdf.text.*;
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
    private Font defaultFont;

    public PDFService() {
        try {
            // Inicializácia fontu
            defaultFont = new Font(Font.FontFamily.HELVETICA, DEFAULT_FONT_SIZE);
        } catch (Exception e) {
            defaultFont = new Font(Font.FontFamily.HELVETICA, DEFAULT_FONT_SIZE);
        }
    }
    /**
     * Generuje štítky pre zoznam rodičov
     * Ak je rodičov viac ako sa zmestí na jednú stranu, vytvorí sa viac strán
     */
    public void generateLabels(List<Parent> parents, LabelFormat format, File outputFile) throws IOException, DocumentException
    {
        try
        {
            Document document = new Document(new Rectangle(595, 842));
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(outputFile));
            document.open();

            // Vždy pridaj prvú stránku na začiatku, aby dokument nebol prázdny
            document.newPage();

            float labelWidth = (float) format.getWidth();
            float labelHeight = (float) format.getHeight();

            int currentColumn = 0;
            int currentRow = 0;

            // Ziskame priamy prístup k obsahu PDF
            PdfContentByte canvas = writer.getDirectContent();

            for (Parent parent : parents)
            {
              // Výpočet pozicie štítka
              float x = (float) (format.getLeftMargin() + currentColumn *
                      (labelWidth + format.getHorizontalGap()));
              float y = (float) (842 - format.getTopMargin() - currentRow *
                      (labelHeight + format.getVerticalGap()) - labelHeight);

              //vytvorenie odstavca s adresou
                Paragraph label = new Paragraph(parent.getFormattedLabel(), defaultFont);

              //Použitie Columntext na presne umiestnenie textu
              ColumnText ct = new ColumnText(canvas);
              ct.setSimpleColumn(x, y, x + labelWidth, y + labelHeight, LINE_HEIGHT, Element.ALIGN_LEFT);
              ct.addElement(label);
              ct.go();
              //Presun na dalši štitok
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
        }catch (DocumentException | IOException e) //(Exception e)
        {
            throw  new RuntimeException("Chyba pri generovaní PDF" + e.getMessage(), e);
        }
    }

    /**
     * Kontroluje, či sa text zmestí na štítok s danými rozmermi
     * Berie do úvahy skutočné rozmery textu, nie len počet znakov
     */
    public boolean checkIfTextFitsOnLabel(String line1, String line2, String line3, LabelFormat format) {
        try {
            // Konverzia mm na body (1 mm = 2.834645 bodov)
            float labelWidthPoints = (float) (format.getWidth() * 2.834645);
            float labelHeightPoints = (float) (format.getHeight() * 2.834645);

            // Odhad šírky znakov (pre Helvetica, veľkosť 10)
            float avgCharWidth = DEFAULT_FONT_SIZE * 0.6f; // Priemerná šírka znaku

            // Kontrola šírky každého riadku
            float line1Width = (line1 != null ? line1.length() : 0) * avgCharWidth;
            float line2Width = (line2 != null ? line2.length() : 0) * avgCharWidth;
            float line3Width = (line3 != null ? line3.length() : 0) * avgCharWidth;

            // Kontrola, či sa riadky zmestia na šírku
            if (line1Width > labelWidthPoints || line2Width > labelWidthPoints || line3Width > labelWidthPoints) {
                return false;
            }

            // Kontrola výšky (3 riadky + medzery)
            float totalHeight = 3 * LINE_HEIGHT + 2; // 3 riadky + malá medzera

            return totalHeight <= labelHeightPoints;

        } catch (Exception e) {
            // V prípade chyby vrátime false
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

        // Nájdi najdlhší riadok
        String longest = lines[0];
        for (int i = 1; i < lines.length; i++) {
            if (lines[i].length() > longest.length()) {
                longest = lines[i];
            }
        }

        return longest;
    }

    //Metoda pre skratenie adries  ak sú príliš dlhé
    public String abbreviateAddressIfNeeded(String address, int maxLength)
    {
        if (address == null || address.length() <= maxLength)
        {
            return address;
        }

        //Tu implementujte logiku skracovania adries
        String[] parts = address.split(",");
        StringBuilder shortened = new StringBuilder();

        for (String part : parts)
        {
            String trimmed = part.trim();
            // skratiť slova podľa potreby
            // napr. "ulica" -> "ul." ....
            trimmed = trimmed.replace("ulica", "ul.")
                    .replace("námestie", "nám.");
            shortened.append(trimmed).append(", ");

            if (shortened.length() > maxLength - 5)
            {
                break;
            }
        }

        if (shortened.length() > 2)
        {
            shortened.setLength(shortened.length() - 2); //Odstranenie poslednej čiarky a medzery
        }
        return shortened.toString();
    }
}