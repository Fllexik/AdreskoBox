package sk.bakaj.adreskobox.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import sk.bakaj.adreskobox.model.Parent;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ExcelService
{
    //Konštanty pre umiestnenia v šablóne
    public static final int SENDER_NAME_ROW = 9;
    public static final int SENDER_STREET_ROW = 10;
    public static final int SENDER_CITY_ROW = 11;
    public static final String MERGED_CELLS_SENDER_ADDRESS = "B10:F10";
    public static final String MERGED_CELLS_SENDER_CITY = "B11:F11";
    public static final String MERGED_CELLS_SENDER_NAME = "B9:F9";

    public static final int MAIL_TYPE_OFFICIAL_ROW = 12;
    public static final int MAIL_TYPE_COLUMN = 6; // stĺpec G

    public static final int RECIPIENTS_START_ROW = 22;
    public static final int RECIPIENTS_NAME_START_COLUMN = 2; // Stĺpec C
    public static final int RECIPIENTS_ADDRESS_START_COLUMN = 4; // Stĺpec E
    public static final int RECIPIENTS_CITY_START_COLUMN = 7; // Stĺpec H
    public static final int MAX_RECIPIENTS_PER_PAGE = 12;

    /**
     * Vytvorí podací hárok pre zadaných rodičov
     *
     * @param parents      Zoznam rodičov
     * @param senderName   Meno odosielateľa
     * @param senderStreet Ulica odosielateľa
     * @param senderCity   PČS a Mesto odosielateľa
     * @param mailType     Typ zásielky
     * @param templatePath Cesta k šablóne podacieho hárku
     * @return Zoznam vytvorených súborov
     */
    public List<File> createSubmissionSheets(List<Parent> parents, String senderName,
                                             String senderStreet, String senderCity,
                                             MailType mailType, String templatePath) throws IOException
    {
        //Kontrola existencie šablony
        File templateFile = new File(templatePath);
        if (!templateFile.exists())
        {
            throw new FileNotFoundException("Šablóna podacieho hárku nebola nájdená" + templatePath);
        }

        List<File> createdFiles = new ArrayList<>();
        //Rozdelenie rodičov do skupín po 12 (maximálny počet na jeden hárok)
        int totalGroups = (int) Math.ceil(parents.size() / (double) MAX_RECIPIENTS_PER_PAGE);

        for (int groupIndex = 0; groupIndex < totalGroups; groupIndex++)
        {
            //Určenie začiatku a konca indexov pre túto skupinu
            int startIndex = groupIndex * MAX_RECIPIENTS_PER_PAGE;
            int endIndex = Math.min(startIndex + MAX_RECIPIENTS_PER_PAGE, parents.size());

            //Vytvorenie súboru podacieho hárku
            File sheetFile = createSubmissionSheets(parents.subList(startIndex, endIndex),
                    senderName, senderStreet, senderCity, mailType, templateFile, groupIndex + 1);

            createdFiles.add(sheetFile);
        }
        return createdFiles;
    }

    /**
     * Vytvorí jeden podací hárok pre zadanú skupinu rodičov
     */
    private File createSingleSubmissionSheet(List<Parent> groupParents, String senderName,
                                             String senderStreet, String senderCity,
                                             MailType mailType, File templateFile, int groupNumber) throws IOException
    {
        //Vytvorenie výstupného súboru
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        File outputFile = new File("Podaci_harok_" + groupNumber + "_" + timestamp + ".xlsx");

        //Kopirovanie šablóny
        try (FileInputStream fis = new FileInputStream(templateFile);
             Workbook templateWorkbook = WorkbookFactory.create(fis);
             FileOutputStream fos = new FileOutputStream(outputFile))
        {
            Sheet sheet = templateWorkbook.getSheetAt(0);

            //Vyplnenie údajov odosieľateľa
            fillSenderInfo(sheet, senderName, senderStreet, senderCity);

            //Označenie typu zásielky
            markMailType(sheet, mailType);

            //Vyplnenie údajov o prijemcoch
            fillRecipients(sheet, groupParents);

            //Uloženie workbooku
            templateWorkbook.write(fos);
        }
        return outputFile;
    }

    /**
     * Vyplní údaje odosielateľa
     */
    private void fillSenderInfo(Sheet sheet, String senderName, String senderStreet, String senderCity)
    {
        //Meno odosielateľa(B10-F10)
        Row nameRow = getOrCreateRow(sheet, SENDER_NAME_ROW);
        Cell nameCell = getOrCreateCell(nameRow, 1);//Stĺpec B
        nameCell.setCellValue(senderName);

        //Ulica odosielateľa (B11-F11)
        Row streetRow = getOrCreateRow(sheet, SENDER_STREET_ROW);
        Cell streetCell = getOrCreateCell(streetRow, 1);//Stĺpec B
        streetCell.setCellValue(senderStreet);

        //PČS a Mesto odosielateľa (B12-F12)
        Row cityRow = getOrCreateRow(sheet, SENDER_CITY_ROW);
        Cell cityCell = getOrCreateCell(cityRow, 1);//Stĺpec B
        cityCell.setCellValue(senderCity);
    }

    /**
     * Označí typ zásielky v hárkoch
     */
    private void markMailType(Sheet sheet, MailType mailType)
    {
        int rowIndex;
        switch (mailType)
        {
            case REGISTERED:
                rowIndex = 10;//Riadok pre "Doporučený list"
                break;
            case INSURED:
                rowIndex = 11;//Riadok pre "Poistený list"
                break;
            case OFFICIAL:
                rowIndex = 12;//Riadok pre "Úradná zasielka"
                break;
            case PACKAGE:
                rowIndex = 13;//Riadok pre "Balík"
                break;
            case EXPRESS:
                rowIndex = 14;//Riadok pre "Expresná zasielka"
                break;
            case POSTAL_ORDER:
                rowIndex = 15;//Riadok pre "Poštový poukaz"
                break;
            default:
                rowIndex = 12;//Predvolené"úradná zasielka"
                break;
        }
        Row row = getOrCreateRow(sheet, rowIndex);
        Cell cell = getOrCreateCell(row, MAIL_TYPE_COLUMN);
        cell.setCellValue("X"); //Označenie "X" pre vybraný typ
    }

    /**
     * Vyplní údaje o prijemcoch
     */
    private void fillRecipients(Sheet sheet, List<Parent> parents)
    {
        for (int i = 0; i < parents.size(); i++)
        {
            Parent parent = parents.get(i);
            int rowIndex = RECIPIENTS_START_ROW + i;

            //Rozdelenie adresy na časti (ulica, mesto s PČS)
            String[] addressParts = splitAddress(parent.getFullAddress());
            String street = addressParts[0];
            String city = addressParts[1];

            //Meno príjemcu(C-D)
            Row row = getOrCreateRow(sheet, rowIndex);
            Cell nameCell = getOrCreateCell(row, RECIPIENTS_NAME_START_COLUMN);
            nameCell.setCellValue(parent.getFullName());

            //Ulica príjemcu(E-F)
            Cell streetCell = getOrCreateCell(row, RECIPIENTS_ADDRESS_START_COLUMN);
            streetCell.setCellValue(street);

            //PČS a Mesto príjemcu(H)
            Cell cityCell = getOrCreateCell(row, RECIPIENTS_CITY_START_COLUMN);
            cityCell.setCellValue(city);
        }
    }

    /**
     * Rozdelí adresu na ulicu a mesto s PSČ
     */
    private String[] splitAddress(String fullAddress)
    {
        String street = fullAddress;
        String city = "";

        //Skusíme nájsť oddeľovač medzi ulicou a mestom
        int commaIndex = fullAddress.lastIndexOf(",");
        if (commaIndex > 0)
        {
            street = fullAddress.substring(0, commaIndex).trim();
            city = fullAddress.substring(commaIndex + 1).trim();
        } else
        {
            //Skusime detekciu PSČ (5 čislíc)
            String[] parts = fullAddress.split(" ");
            for (int i = 0; i < parts.length; i++)
            {
                if (parts[i].matches("\\d{5}") || parts[i].matches("\\d{3}\\s?\\d{2}"))
                {
                    // Našli sme PSČ, rozdelíme adresu
                    StringBuilder cityPart = new StringBuilder();
                    for (int j = 0; j < parts.length; j++)
                    {
                        cityPart.append(parts[j]).append(" ");
                    }
                    city = cityPart.toString().trim();

                    StringBuilder streetPart = new StringBuilder();
                    for (int j = 0; j < i; j++)
                    {
                        streetPart.append(parts[j]).append(" ");
                    }
                    street = streetPart.toString().trim();

                    break;
                }
            }
        }
        return new String[]{street, city};
    }

    /**
     * Pomocná metóda pre ziskanie alebo vytvorenie riadku
     */
    private Row getOrCreateRow(Sheet sheet, int rowIndex)
    {
        Row row = sheet.getRow(rowIndex);
        if (row == null)
        {
            row = sheet.createRow(rowIndex);
        }
        return row;
    }

    /**
     * Pomocná metoda pre získanie alebo vytvorenie bunky
     */
    private Cell getOrCreateCell(Row row, int columnIndex)
    {
        Cell cell = row.getCell(columnIndex);
        if (cell == null)
        {
            cell = row.createCell(columnIndex);
        }
        return cell;
    }

    /**
     * Vytvorí novú šablonu podacieho hárku
     *
     * @return Výtvorený súbor šablony
     */
    public File createNewSubmissionTemplate() throws IOException
    {
        File templateDir = new File("templates");
        if (!templateDir.exists())
        {
            templateDir.mkdir();
        }

        File templateFile = new File(templateDir, "Podaci_harok_template.xlsx");

        try(Workbook workbook = new XSSFWorkbook();
        FileOutputStream fos = new FileOutputStream(templateFile))
        {
            Sheet sheet = workbook.createSheet("Podaci_harok");

            //Nastavenie zlučených budniek pre adresu odosielateľa
            sheet.addMergedRegion(CellRangeAddress.valueOf(MERGED_CELLS_SENDER_NAME));
            sheet.addMergedRegion(CellRangeAddress.valueOf(MERGED_CELLS_SENDER_ADDRESS));
            sheet.addMergedRegion(CellRangeAddress.valueOf(MERGED_CELLS_SENDER_CITY));

            //Vytvorenie hlavičky
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("PODACÍ HÁROK");

            //Vytvorenie hlavičky pre typ Zasielky
            Row mailTypeHeaderRow = sheet.createRow(8);
            Cell mailTypeHeaderCell = mailTypeHeaderRow.createCell(6);
            mailTypeHeaderCell.setCellValue("Druh zásielky");

            //Vytvorenie možnosti pre typ zasielky
            String[] mailTypes = {"Doporučený list", "Poistený list", "Úradná zasielka", "Balík", "Expresná zasielka", "Poštový poukaz"};
            for (int i = 0; i < mailTypes.length; i++)
            {
                Row row = sheet.createRow(10 + i);
               Cell labelCell = row.createCell(5);
               labelCell.setCellValue(mailTypes[i]);
               Cell checkCell = row.createCell(6);
               checkCell.setCellValue("");
            }

            //Vytvorenie hlavičky pre príjemcov
            Row recipientsHeaderRow = sheet.createRow(21);
            Cell nameHeaderCell = recipientsHeaderRow.createCell(2);
            nameHeaderCell.setCellValue("Meno príjemcu");
            Cell addressHeaderCell = recipientsHeaderRow.createCell(4);
            addressHeaderCell.setCellValue("Ulica a číslo");
            Cell cityHeaderCell = recipientsHeaderRow.createCell(7);
            cityHeaderCell.setCellValue("PSČ a mesto");

            //Uloženie workbooku
            workbook.write(fos);
        }

        /**
         * Enum pre typy zásielok
         */
        public enum MailType
        {
            REGISTERED("Doporučený list"),
            INSURED("Poistený list"),
            OFFICIAL("Úradná zasielka"),
            PACKAGE("Balík"),
            EXPRESS("Expresná zásielka"),
            POSTAL_ORDER("Poštový poukaz");

            private final String displayName;

            MailType(String displayName)
            {
                this.displayName = displayName;
            }

            public String getDisplayName()
            {
                return displayName;
            }

            @Override
            public String toString()
            {
                return displayName;
            }
        }
    }
}
