package sk.bakaj.adreskobox.service;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import sk.bakaj.adreskobox.model.LabelFormat;
import sk.bakaj.adreskobox.model.Parent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class PDFService
{
    public void generateLabels(List<Parent> parents, LabelFormat format, File outputFile)
    {
        try
        {
            Document document = new Document(new Rectangle(595, 842));
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(outputFile));
            document.open();

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
                        (labelHeight + format.getVerticalGap()));// obratenie vertikalnej pozicie povodne bolo bez cisal a + currentRow

                //vytvorenie odstavca s adresou
                Paragraph label = new Paragraph(parent.getFullName() + "\n" + parent.getFullAddress());
                /*label.setFixedPosition(x, y, labelWidth);
                document.add(label);*/

                //Použitie Columntext na presne umiestnenie textu
                ColumnText ct = new ColumnText(canvas);
                ct.setSimpleColumn(x, y, x + labelWidth, y + labelHeight, 10, Element.ALIGN_LEFT);
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

    //Metoda pre skratenie adries  ak sú príliš dlhé
    public String abbreviateAddressIfNeeded(String address, int maxLenght)
    {
        if (address == null || address.length() <= maxLenght)
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

            if (shortened.length() > maxLenght - 5)
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
