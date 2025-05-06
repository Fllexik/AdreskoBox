package sk.bakaj.adreskobox.service;

import org.apache.poi.wp.usermodel.Paragraph;
import sk.bakaj.adreskobox.model.LabelFormat;
import sk.bakaj.adreskobox.model.Parent;

import javax.swing.text.Document;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class PDFService
{
    public void generateLabels(List<Parent> parents, LabelFormat format, File outputFile)
    {
        try
        {
            Document document = new Document();
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(outputFile));
            document.open();

            float labelWidth = (float) format.getWidth();
            float labelHeight = (float) format.getHeight();

            int currentColumn = 0;
            int currentRow = 0;

            for (Parent parent : parents)
            {
                float x = (float) (format.getLeftMargin() + currentColumn *
                        (labelWidth + format.getHorizontalGap()));
                float y = (float) (format.getTopMargin() + currentRow *
                        (labelHeight + format.getVerticalGap()));

                Paragraph label = new Paragraph(parent.getFullName() + "\n" + parent.getFullAddress());
                label.setFixedPosition(x, y, labelWidth);
                document.add(label);

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
        }catch (Exception e)
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
