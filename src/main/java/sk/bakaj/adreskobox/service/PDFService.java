package sk.bakaj.adreskobox.service;

import org.apache.poi.wp.usermodel.Paragraph;
import sk.bakaj.adreskobox.model.LabelFormat;
import sk.bakaj.adreskobox.model.Parent;

import javax.swing.text.Document;
import java.io.File;
import java.util.List;

public class PDFService
{
    public void generatelabels(List<Parent> parents, LabelFormat format, File outputFile)
    {
        try
        {
            PdfWriter writer = new PdfWriter(outputFile);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

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

            }
        }
    }
}
