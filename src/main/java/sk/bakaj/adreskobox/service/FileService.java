package sk.bakaj.adreskobox.service;

import org.apache.poi.ss.usermodel.*;
import sk.bakaj.adreskobox.model.ImportedData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileService
{
    public static final String STUDENT_FIRSTNAME_COLUMN = "Meno";
    public static final String STUDENT_LASTNAME_COLUMN = "Priezvisko";
    public static final String PARENT1_NAME_COLUMN = "Rodič 1.";
    public static final String PARENT2_NAME_COLUMN = "Rodič 2.";
    public static final String ADDRESS1_COLUMN = "Adresa 1.";
    public static final String ADDRESS2_COLUMN = "Adresa 2.";

    /**
     * Detekuje typ a vrati ho ako reťazec: "CSV", "XLS", XLXS" alebo null
     */
    public String detectFileType(File file)
    {
        String fileName = file.getName().toLowerCase();
        if (fileName.endsWith(".csv"))
        {
            return "CSV";
        }
        else if (fileName.endsWith(".xls"))
        {
            return "XLS";
        }
        else if (fileName.endsWith(".xlsx"))
        {
            return "XLSX";
        }
        else
        {
            return null;
        }
    }

    /**
     * Detekuje oddeľovač pre CSV súbor
     */

    public String detectFileDelimiter(File file) throws IOException
    {
        String fileName = file.getName().toLowerCase();

        //Ak to nie je CSV súbor, vratime null
        if (!fileName.endsWith(".csv"))
        {
            return null;
        }

        //Definujeme potencialne oddeľovače a ich počty v súbore
        String[] commonDelimiters = {",", ";", "\t", "|"};
        Map<String, Integer> delimiterCounts = new HashMap<>();

        for (String delimiter : commonDelimiters)
        {
            delimiterCounts.put(delimiter, 0);
        }

        //Prečítame prvých 10 riadkov(alebo menej, ak je súbor kratši)
        try(BufferedReader br = new BufferedReader(new FileReader(file)))
        {
            String line;
            int lineCount = 0;
            while ((line = br.readLine()) != null && lineCount < 10)
            {
                for (String delimiter : commonDelimiters)
                {
                    int count = countOccurrences(line, delimiter);
                    delimiterCounts.put(delimiter, delimiterCounts.get(delimiter) + count);
                }
                lineCount++;
            }
        }
        //Vyberiem oddeľovač s najväčším počtom vyskytov
        String bestDelimiter = ","; //Predvolený oddeľovač je čiarka
        int maxCount = 0;

        for (Map.Entry<String, Integer> entry : delimiterCounts.entrySet())
        {
            if (entry.getValue() > maxCount)
            {
                maxCount = entry.getValue();
                bestDelimiter = entry.getKey();
            }
        }
        // AK sme nenašli žiadený oddeľovač, vratime predvolený
        return maxCount > 0 ? bestDelimiter : ",";
    }

    /**
     * Počíta výskyty oddeľovača v reťazci
     */

    private int countOccurrences(String text, String delimiter)
    {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(delimiter, index)) != -1)
        {
            count++;
            index += delimiter.length();
        }
        return count;
    }

    /**
     * Overí či prvý riadok v súbore je hlavička
     * Kontroluje, či obsahuje očakávané hlavičky stlpcov
     */

    public boolean hasHeaderRow(File file) throws IOException
    {
        String fileType = detectFileType(file);

        if ("CSV".equals(fileType))
        {
            String delimiter = detectFileDelimiter(file);
            return checkCSVHeaderRow(file, delimiter);
        } else if ("XLS".equals(fileType) || "XLSX".equals(fileType))
        {
            return checkExcelHeaderRow(file).getKey().booleanValue(); //return checkExcelHeaderRow(file);
        }
        return false;
    }

    /**
     * Kontroluje, či prvý riadok CSV súboru obsahuje očakavané stlpce
     */

    private boolean checkCSVHeaderRow(File file, String delimiter) throws IOException
    {
        try(BufferedReader br = new BufferedReader(new FileReader(file)))
        {
            String headerLine1 = br.readLine();//Prvý riadok
            String headerLine2 = br.readLine();//Druhý riadok

            //Skusíme najprv druhý riadok, či existuju a obsahuju relevantné hlavičky:
            if (headerLine2 == null)
            {
                String[] headers2 = headerLine2.split(delimiter);
                if (containsExpectedColumns(headers2))
                {
                    return true;
                }
            }
            //Ak druhý riadok neobsahuje, alebo neexistuje, skusím prvý riadok
            if (headerLine1 != null)
            {
                String[] headers1 = headerLine1.split(delimiter);
                return containsExpectedColumns(headers1);
            }
            return false;
        }
    }

    /**
     * Kontroluje , či prvý riadok alebo druhý riadok Excel suboru obsahuje očakávane stlpce
     * Vracia pár (boolean isHeaderPresent, int headerRowIndex).
     */

    private Map.Entry<Boolean, Integer> checkExcelHeaderRow(File file) throws IOException
    {
        try(Workbook workbook = WorkbookFactory.create(file))
        {
            Sheet sheet = workbook.getSheetAt(0);

            // Skúsime riadok s indexom 0 (prvý riadok)
            Row headerRow0 = sheet.getRow(0);
            if (headerRow0 != null)
            {
                String[] headers0 = new String[headerRow0.getLastCellNum()];
                for (int i = 0; i < headerRow0.getLastCellNum(); i++)
                {
                    Cell cell = headerRow0.getCell(i);
                    headers0[i] = getCellValueAsString(cell);
                }
                if (containsExpectedColumns(headers0))
                {
                    return new java.util.AbstractMap.SimpleEntry<>(true, 0); // Hlavička nájdená na riadku 0
                }
            }

            // Ak neboli nájdené na riadku 0, skúsime riadok s indexom 1 (druhý riadok)
            Row headerRow1 = sheet.getRow(1);
            if (headerRow1 != null)
            {
                String[] headers1 = new String[headerRow1.getLastCellNum()];
                for (int i = 0; i < headerRow1.getLastCellNum(); i++)
                {
                    Cell cell = headerRow1.getCell(i);
                    headers1[i] = getCellValueAsString(cell);
                }
                if (containsExpectedColumns(headers1))
                {
                    return new java.util.AbstractMap.SimpleEntry<>(true, 1); // Hlavička nájdená na riadku 1
                }
            }
            return new java.util.AbstractMap.SimpleEntry<>(false, -1); // Hlavička nebola nájdená
        }
    }

    /**
     * Kontroluje, či pole hlavičiek obsahuje aspoň jeden očakávany stlpec
     */
    private boolean containsExpectedColumns(String[] headers)
    {
        if (headers == null || headers.length == 0)
        {
            return false;
        }
        //Kontroluje ci  hlavička obsahuje aspoň jeden z očakávanych stlpcov
        for (String header : headers)
        {
            if (header == null) continue;

            if (header.equalsIgnoreCase(STUDENT_FIRSTNAME_COLUMN) ||
                    header.equalsIgnoreCase(STUDENT_LASTNAME_COLUMN) ||
                    header.equalsIgnoreCase(PARENT1_NAME_COLUMN) ||
                    header.equalsIgnoreCase(PARENT2_NAME_COLUMN) ||
                    header.equalsIgnoreCase(ADDRESS1_COLUMN) ||
                    header.equalsIgnoreCase(ADDRESS2_COLUMN))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Načíta data zo súboru, automaticky detekuje typ a oddeľovač
     *  Ak prvý riadok nie je hlavička, može volajúci špecifikovať hasHeader=false
     *
     * @param file Súbor na načítanie.
     * @param hasHeader True, ak sa očakáva hlavička a ma sa preskočit/použiť  False, ak sú dáta od prvého riadku.
     * @return Zoznam načítaných dát.
     * @throws IOException Ak nastane chyba pri čítaní súboru.
     */
    public List<ImportedData> readFile(File file, boolean hasHeader) throws IOException
    {
        String fileType = detectFileType(file);

        if ("CSV".equals(fileType))
        {
            String delimiter = detectFileDelimiter(file);
            return readCSV(file, delimiter, hasHeader);
        } else if ("XLS".equals(fileType) || "XLSX".equals(fileType))
        {
            // Potrebujeme vedieť, kde sa hlavička nachádza (ak vôbec)
            Map.Entry<Boolean, Integer> headerInfo = checkExcelHeaderRow(file);
            // Ak 'hasHeader' z UI je false, ale detekujeme hlavičku, tak sa riadime detekciou.
            // Ak 'hasHeader' z UI je true, a detekujeme ju, tak super.
            // Ak 'hasHeader' z UI je true, ale detekujeme, že nie je, tak je problém.
            // Zjednodušíme to: 'hasHeader' z UI bude len indikátorom pre používateľa,
            // ale pre čítanie sa budeme riadiť tým, čo detekuje 'checkExcelHeaderRow'.
            return readExcel(file, headerInfo.getValue()); // Posielame index riadku s hlavičkou, nie boolean
        } else
        {
            throw new IOException("Nepodporovaný typ súboru");
        }
    }

    /**
     * Načíta data zo súboru s automatickou detekciou hlavičky
     */
    public List<ImportedData> readFile(File file) throws IOException
    {
        // Táto metóda už len deleguje na metódu s parametrom 'hasHeader',
        // kde 'hasHeader' je získané z detekcie.
        return readFile(file, hasHeaderRow(file));
    }

    private List<ImportedData> readCSV(File file, String delimiter, boolean hasHeader) throws IOException
    {
        List<ImportedData> dataList = new ArrayList<>();
        List<String> allLines = new ArrayList<>();

        // Načítanie všetkých riadkov do zoznamu
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                allLines.add(line);
            }
        }

        if (allLines.isEmpty()) {
            throw new IOException("Súbor je prázdny.");
        }

        String[] headersToUse = null;
        int startDataRowIndex = 0; // Index v zozname allLines, odkiaľ začínajú skutočné dáta

        // Určenie riadku s hlavičkami a odkiaľ začínajú dáta
        if (hasHeader) { // Ak UI/detekcia naznačuje, že by mala byť hlavička
            // Skúsime nájsť hlavičku na druhom riadku (index 1 v allLines)
            if (allLines.size() > 1) { // Kontrola, či existuje druhý riadok
                String[] headers2 = allLines.get(1).split(delimiter);
                if (containsExpectedColumns(headers2)) {
                    headersToUse = headers2;
                    startDataRowIndex = 2; // Dáta začínajú po dvoch riadkoch hlavičiek (index 2)
                }
            }
            // Ak druhý riadok neobsahoval hlavičky, alebo súbor má len jeden riadok, skúsime prvý riadok (index 0)
            if (headersToUse == null && allLines.size() > 0) {
                String[] headers1 = allLines.get(0).split(delimiter);
                if (containsExpectedColumns(headers1)) {
                    headersToUse = headers1;
                    startDataRowIndex = 1; // Dáta začínajú po jednom riadku hlavičky (index 1)
                }
            }
        }

        // Ak sa ani po hasHeader=true nenašli rozpoznateľné hlavičky, alebo hasHeader bolo false,
        // predpokladáme, že prvý riadok obsahuje potenciálne informácie o stĺpcoch, ale je zároveň dátami.
        if (headersToUse == null && !allLines.isEmpty()) {
            headersToUse = allLines.get(0).split(delimiter); // Použijeme štruktúru prvého riadku
            startDataRowIndex = 0; // Dáta začínajú od prvého riadku
        } else if (headersToUse == null && allLines.isEmpty()) {
            // Tento prípad by už mal byť ošetrený počiatočnou kontrolou allLines.isEmpty()
            return dataList; // Vrátiť prázdny zoznam
        }

        int[] columnIndexes = findColumnIndexes(headersToUse);

        // Spracovanie skutočných riadkov s dátami
        for (int i = startDataRowIndex; i < allLines.size(); i++) {
            String line = allLines.get(i);
            String[] values = line.split(delimiter);
            ImportedData data = createImportedData(values, columnIndexes);
            if (data != null) {
                dataList.add(data);
            }
        }
        return dataList;
    }

    /**
     * Načíta dáta z Excel súboru s dynamickým určením riadku hlavičky.
     * @param file Súbor na načítanie.
     * @param headerRowIndex Index riadku, na ktorom bola nájdená platná hlavička. Ak -1, hlavička nebola nájdená.
     * @return Zoznam načítaných dát.
     * @throws IOException Ak nastane chyba pri čítaní súboru.
     */
    private List<ImportedData> readExcel(File file, int headerRowIndex) throws IOException
    {
        List<ImportedData> dataList = new ArrayList<>();
        try (Workbook workbook = WorkbookFactory.create(file))
        {
            Sheet sheet = workbook.getSheetAt(0);

            String[] headers;
            int startRowForData;

            if (headerRowIndex != -1) // Hlavička bola nájdená
            {
                Row actualHeaderRow = sheet.getRow(headerRowIndex);
                if (actualHeaderRow == null) {
                    throw new IOException("Nájdená hlavička na riadku " + (headerRowIndex + 1) + " sa nedá načítať.");
                }

                headers = new String[actualHeaderRow.getLastCellNum()];
                for (int i = 0; i < actualHeaderRow.getLastCellNum(); i++)
                {
                    Cell cell = actualHeaderRow.getCell(i);
                    headers[i] = getCellValueAsString(cell);
                }
                startRowForData = headerRowIndex + 1; // Dáta začínajú hneď po riadku s hlavičkou
            }
            else // Hlavička nebola nájdená - predpokladáme dáta od prvého riadku
            {
                Row firstRow = sheet.getRow(0);
                if (firstRow == null) {
                    throw new IOException("Súbor je prázdny alebo neobsahuje žiadne dáta.");
                }
                headers = new String[firstRow.getLastCellNum()];
                for (int i = 0; i < firstRow.getLastCellNum(); i++)
                {
                    Cell cell = firstRow.getCell(i);
                    headers[i] = getCellValueAsString(cell);
                }
                startRowForData = 0; // Dáta začínajú od prvého riadku (bude sa snažiť parsovať prvý riadok ako dáta)
            }

            int[] columnIndexes = findColumnIndexes(headers);

            for (int i = startRowForData; i <= sheet.getLastRowNum(); i++)
            {
                Row row = sheet.getRow(i);
                if (row != null)
                {
                    String[] values = new String[headers.length];
                    for (int j = 0; j < headers.length; j++)
                    {
                        Cell cell = row.getCell(j);
                        values[j] = getCellValueAsString(cell);
                    }
                    ImportedData data = createImportedData(values, columnIndexes);
                    if (data != null)
                    {
                        dataList.add(data);
                    }
                }
            }
        }
        return dataList;
    }

    private int[] findColumnIndexes(String[] headers)
    {
        int[] indexes = new int[6];
        for (int i = 0; i < indexes.length; i++)
        {
            indexes[i] = -1;
        }

        for (int i = 0; i < headers.length; i++)
        {
            String header = headers[i];
            if (header == null) continue;

            if (header.equalsIgnoreCase(STUDENT_FIRSTNAME_COLUMN))
            {
                indexes[0] = i;
            } else if (header.equalsIgnoreCase(STUDENT_LASTNAME_COLUMN))
            {
                indexes[1] = i;
            } else if (header.equalsIgnoreCase(PARENT1_NAME_COLUMN))
            {
                indexes[2] = i;
            } else if (header.equalsIgnoreCase(PARENT2_NAME_COLUMN))
            {
                indexes[3] = i;
            } else if (header.equalsIgnoreCase(ADDRESS1_COLUMN))
            {
                indexes[4] = i;
            } else if (header.equalsIgnoreCase(ADDRESS2_COLUMN))
            {
                indexes[5] = i;
            }
        }
        return indexes;
    }

    private String getCellValueAsString(Cell cell)
    {
        if (cell == null) return "";
        switch (cell.getCellType())
        {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                // Pozor na dátumy, ak sa čísla používajú aj na dátumy
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString(); // Ak sú dátumy, môžete to formátovať inak
                }
                return String.valueOf((int) cell.getNumericCellValue()).trim();
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue()).trim();
            case FORMULA:
                try {
                    return cell.getStringCellValue().trim();
                } catch (IllegalStateException e) {
                    // Ak formula vráti číslo
                    return String.valueOf((int) cell.getNumericCellValue()).trim();
                }
            case BLANK:
                return "";
            default:
                return "";
        }
    }

    private ImportedData createImportedData(String[] values, int[] columnIndexes)
    {
        if (values == null || values.length == 0) return null;
        ImportedData data = new ImportedData();

        // Bezpečné získanie hodnoty, ak je index platný a hodnota existuje
        if (columnIndexes[0] != -1 && columnIndexes[0] < values.length) {
            data.setStudentFirstName(values[columnIndexes[0]].trim());
        }
        if (columnIndexes[1] != -1 && columnIndexes[1] < values.length) {
            data.setStudentLastName(values[columnIndexes[1]].trim());
        }
        if (columnIndexes[2] != -1 && columnIndexes[2] < values.length) {
            data.setParent1Name(values[columnIndexes[2]].trim());
        }
        if (columnIndexes[3] != -1 && columnIndexes[3] < values.length) {
            data.setParent2Name(values[columnIndexes[3]].trim());
        }
        if (columnIndexes[4] != -1 && columnIndexes[4] < values.length) {
            data.setAddress1(values[columnIndexes[4]].trim());
        }
        if (columnIndexes[5] != -1 && columnIndexes[5] < values.length) {
            data.setAddress2(values[columnIndexes[5]].trim());
        }

        // Ak sú mená študentov prázdne, vráťte null, aby sa riadok ignoroval
        if ((data.getStudentFirstName() == null || data.getStudentFirstName().isEmpty()) &&
                (data.getStudentLastName() == null || data.getStudentLastName().isEmpty())) {
            return null; // Alebo môžete vrátiť data, ale potom je na volajúcom, aby to odfiltroval
        }

        return data;
    }
}