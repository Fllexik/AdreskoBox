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

/**
 * Služba pre prácu so súbormi - import dát z CSV a Excel súborov
 * Podporuje automatickú detekciu typu súboru, oddeľovačov a hlavičiek
 */
public class FileService
{
    // Konštanty pre názvy stĺpcov
    public static final String STUDENT_FIRSTNAME_COLUMN = "Meno";
    public static final String STUDENT_LASTNAME_COLUMN = "Priezvisko";
    public static final String PARENT1_NAME_COLUMN = "Rodič 1.";
    public static final String PARENT2_NAME_COLUMN = "Rodič 2.";
    public static final String ADDRESS1_COLUMN = "Adresa 1.";
    public static final String ADDRESS2_COLUMN = "Adresa 2.";

    // Maximálny počet riadkov na hľadanie hlavičky
    private static final int MAX_HEADER_SEARCH_ROWS = 5;

    /**
     * Detekuje typ súboru na základe prípony
     * @param file súbor na analýzu
     * @return typ súboru ako reťazec: "CSV", "XLS", "XLSX" alebo null
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
     * Detekuje oddeľovač pre CSV súbor analýzou prvých 10 riadkov
     * @param file CSV súbor na analýzu
     * @return najčastejšie používaný oddeľovač
     * @throws IOException pri chybe čítania súboru
     */
    public String detectFileDelimiter(File file) throws IOException
    {
        String fileName = file.getName().toLowerCase();

        // Ak to nie je CSV súbor, vrátime null
        if (!fileName.endsWith(".csv"))
        {
            return null;
        }

        // Definujeme potenciálne oddeľovače a ich počty v súbore
        String[] commonDelimiters = {",", ";", "\t", "|"};
        Map<String, Integer> delimiterCounts = new HashMap<>();

        for (String delimiter : commonDelimiters)
        {
            delimiterCounts.put(delimiter, 0);
        }

        // Prečítame prvých 10 riadkov (alebo menej, ak je súbor kratší)
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

        // Vyberieme oddeľovač s najväčším počtom výskytov
        String bestDelimiter = ","; // Predvolený oddeľovač je čiarka
        int maxCount = 0;

        for (Map.Entry<String, Integer> entry : delimiterCounts.entrySet())
        {
            if (entry.getValue() > maxCount)
            {
                maxCount = entry.getValue();
                bestDelimiter = entry.getKey();
            }
        }

        // Ak sme nenašli žiadny oddeľovač, vrátime predvolený
        return maxCount > 0 ? bestDelimiter : ",";
    }

    /**
     * Počíta výskyty oddeľovača v reťazci
     * @param text text na analýzu
     * @param delimiter hľadaný oddeľovač
     * @return počet výskytov oddeľovača
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
     * Nájde riadok s hlavičkou v CSV súbore
     * @param file CSV súbor na analýzu
     * @param delimiter oddeľovač používaný v súbore
     * @return HeaderInfo objekt obsahujúci informácie o hlavičke
     * @throws IOException pri chybe čítania súboru
     */
    private HeaderInfo findCSVHeaderRow(File file, String delimiter) throws IOException
    {
        List<String> allLines = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file)))
        {
            String line;
            int lineCount = 0;
            while ((line = br.readLine()) != null && lineCount < MAX_HEADER_SEARCH_ROWS)
            {
                allLines.add(line);
                lineCount++;
            }
        }

        // Hľadáme hlavičku v prvých MAX_HEADER_SEARCH_ROWS riadkoch
        for (int i = 0; i < allLines.size(); i++)
        {
            String[] headers = parseCSVLine(allLines.get(i), delimiter);

            if (containsExpectedColumns(headers))
            {
                return new HeaderInfo(true, i, headers);
            }
        }

        // Ak sa hlavička nenašla, vrátime info že nie je hlavička
        return new HeaderInfo(false, -1, null);
    }

    /**
     * Nájde riadok s hlavičkou v Excel súbore
     * @param file Excel súbor na analýzu
     * @return HeaderInfo objekt obsahujúci informácie o hlavičke
     * @throws IOException pri chybe čítania súboru
     */
    private HeaderInfo findExcelHeaderRow(File file) throws IOException
    {
        try(Workbook workbook = WorkbookFactory.create(file))
        {
            Sheet sheet = workbook.getSheetAt(0);

            // Hľadáme hlavičku v prvých MAX_HEADER_SEARCH_ROWS riadkoch
            for (int rowIndex = 0; rowIndex < Math.min(MAX_HEADER_SEARCH_ROWS, sheet.getLastRowNum() + 1); rowIndex++)
            {
                Row row = sheet.getRow(rowIndex);
                if (row != null)
                {
                    String[] headers = new String[row.getLastCellNum()];
                    for (int i = 0; i < row.getLastCellNum(); i++)
                    {
                        Cell cell = row.getCell(i);
                        headers[i] = getCellValueAsString(cell);
                    }

                    if (containsExpectedColumns(headers))
                    {
                        return new HeaderInfo(true, rowIndex, headers);
                    }
                }
            }

            return new HeaderInfo(false, -1, null);
        }
    }

    /**
     * Overí či súbor obsahuje hlavičku
     * Kontroluje, či obsahuje očakávané názvy stĺpcov
     * @param file súbor na kontrolu
     * @return true ak súbor obsahuje hlavičku, false inak
     * @throws IOException pri chybe čítania súboru
     */

    public boolean hasHeaderRow(File file) throws IOException
    {
        String fileType = detectFileType(file);

        if ("CSV".equals(fileType))
        {
            String delimiter = detectFileDelimiter(file);
            return findCSVHeaderRow(file, delimiter).hasHeader;
        }
        else if ("XLS".equals(fileType) || "XLSX".equals(fileType))
        {
            return findExcelHeaderRow(file).hasHeader;
        }
        return false;
    }

    /**
     * Kontroluje, či pole hlavičiek obsahuje aspoň dva očakávané stĺpce
     * @param headers pole názvov hlavičiek
     * @return true ak obsahuje dostatočný počet rozpoznaných stĺpcov
     */
    private boolean containsExpectedColumns(String[] headers)
    {
        if (headers == null || headers.length == 0)
        {
            return false;
        }

        int matchedColumns = 0;
        for (String header : headers)
        {
            if (header == null) continue;

            String cleanHeader = cleanValue(header);

            if (cleanHeader.equalsIgnoreCase(STUDENT_FIRSTNAME_COLUMN) ||
                    cleanHeader.equalsIgnoreCase(STUDENT_LASTNAME_COLUMN) ||
                    cleanHeader.equalsIgnoreCase(PARENT1_NAME_COLUMN) ||
                    cleanHeader.equalsIgnoreCase(PARENT2_NAME_COLUMN) ||
                    cleanHeader.equalsIgnoreCase(ADDRESS1_COLUMN) ||
                    cleanHeader.equalsIgnoreCase(ADDRESS2_COLUMN))
            {
                matchedColumns++;
            }
        }

        // Požadujeme aspoň 2 rozpoznané stĺpce pre potvrdenie hlavičky
        return matchedColumns >= 2;
    }

    /**
     * Načíta dáta zo súboru s automatickou detekciou hlavičky
     * @param file súbor na načítanie
     * @return zoznam importovaných dát
     * @throws IOException pri chybe čítania súboru
     */
    public List<ImportedData> readFile(File file) throws IOException
    {
        return readFile(file, hasHeaderRow(file));
    }

    /**
     * Načíta dáta zo súboru
     * @param file súbor na načítanie
     * @param hasHeader určuje či súbor obsahuje hlavičku
     * @return zoznam importovaných dát
     * @throws IOException pri chybe čítania súboru alebo nepodporovanom type súboru
     */
    public List<ImportedData> readFile(File file, boolean hasHeader) throws IOException
    {
        String fileType = detectFileType(file);

        if ("CSV".equals(fileType))
        {
            String delimiter = detectFileDelimiter(file);
            return readCSV(file, delimiter, hasHeader);
        }
        else if ("XLS".equals(fileType) || "XLSX".equals(fileType))
        {
            return readExcel(file, hasHeader);
        }
        else
        {
            throw new IOException("Nepodporovaný typ súboru");
        }
    }

    /**
     * Načíta dáta z CSV súboru
     * @param file CSV súbor
     * @param delimiter oddeľovač stĺpcov
     * @param hasHeader určuje či súbor obsahuje hlavičku
     * @return zoznam importovaných dát
     * @throws IOException pri chybe čítania súboru
     */
    private List<ImportedData> readCSV(File file, String delimiter, boolean hasHeader) throws IOException
    {
        List<ImportedData> dataList = new ArrayList<>();
        List<String> allLines = new ArrayList<>();

        // Načítanie všetkých riadkov
        try (BufferedReader br = new BufferedReader(new FileReader(file)))
        {
            String line;
            while ((line = br.readLine()) != null)
            {
                allLines.add(line);
            }
        }

        if (allLines.isEmpty())
        {
            throw new IOException("Súbor je prázdny.");
        }

        HeaderInfo headerInfo;
        if (hasHeader)
        {
            headerInfo = findCSVHeaderRow(file, delimiter);
        }
        else
        {
            // Ak nie je hlavička, použijeme prvý riadok ako vzor štruktúry
            String[] firstRowColumns = parseCSVLine(allLines.get(0), delimiter);
            headerInfo = new HeaderInfo(false, -1, firstRowColumns);
        }

        if (headerInfo.headers == null)
        {
            throw new IOException("Nepodarilo sa určiť štruktúru súboru.");
        }

        int[] columnIndexes = findColumnIndexes(headerInfo.headers);
        int startDataRowIndex = headerInfo.hasHeader ? headerInfo.rowIndex + 1 : 0;

        // Spracovanie dát
        for (int i = startDataRowIndex; i < allLines.size(); i++)
        {
            String line = allLines.get(i);
            if (line.trim().isEmpty()) continue;

            String[] values = parseCSVLine(line, delimiter);
            ImportedData data = createImportedData(values, columnIndexes);
            if (data != null)
            {
                dataList.add(data);
            }
        }

        return dataList;
    }

    /**
     * Načíta dáta z Excel súboru
     * @param file Excel súbor
     * @param hasHeader určuje či súbor obsahuje hlavičku
     * @return zoznam importovaných dát
     * @throws IOException pri chybe čítania súboru
     */
    private List<ImportedData> readExcel(File file, boolean hasHeader) throws IOException
    {
        List<ImportedData> dataList = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(file))
        {
            Sheet sheet = workbook.getSheetAt(0);

            HeaderInfo headerInfo;
            if (hasHeader) {
                headerInfo = findExcelHeaderRow(file);
            }
            else
            {
                // Ak nie je hlavička, použijeme prvý riadok ako vzor štruktúry
                Row firstRow = sheet.getRow(0);
                if (firstRow == null)
                {
                    throw new IOException("Súbor je prázdny alebo neobsahuje žiadne dáta.");
                }

                String[] firstRowColumns = new String[firstRow.getLastCellNum()];
                for (int i = 0; i < firstRow.getLastCellNum(); i++)
                {
                    Cell cell = firstRow.getCell(i);
                    firstRowColumns[i] = getCellValueAsString(cell);
                }
                headerInfo = new HeaderInfo(false, -1, firstRowColumns);
            }

            if (headerInfo.headers == null)
            {
                throw new IOException("Nepodarilo sa určiť štruktúru súboru.");
            }

            int[] columnIndexes = findColumnIndexes(headerInfo.headers);
            int startRowForData = headerInfo.hasHeader ? headerInfo.rowIndex + 1 : 0;

            // Spracovanie všetkých riadkov s dátami
            for (int i = startRowForData; i <= sheet.getLastRowNum(); i++)
            {
                Row row = sheet.getRow(i);
                if (row != null)
                {
                    String[] values = new String[headerInfo.headers.length];
                    for (int j = 0; j < headerInfo.headers.length; j++)
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

    /**
     * Bezpečné parsovanie CSV riadku s podporou úvodzoviek
     * @param line riadok na spracovanie
     * @param delimiter oddeľovač stĺpcov
     * @return pole hodnôt zo stĺpcov
     */
    private String[] parseCSVLine(String line, String delimiter)
    {
        List<String> result = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder current = new StringBuilder();

        for (int i = 0; i < line.length(); i++)
        {
            char c = line.charAt(i);

            if (c == '"')
            {
                inQuotes = !inQuotes;
            }
            else if (c == delimiter.charAt(0) && !inQuotes)
            {
                result.add(current.toString().trim());
                current.setLength(0);
            }
            else
            {
                current.append(c);
            }
        }
        result.add(current.toString().trim());

        return result.toArray(new String[0]);
    }

    /**
     * Nájde indexy stĺpcov pre jednotlivé typy dát
     * @param headers pole názvov hlavičiek
     * @return pole indexov pre jednotlivé stĺpce (-1 ak stĺpec nebol nájdený)
     */
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

            String cleanHeader = cleanValue(header);

            if (cleanHeader.equalsIgnoreCase(STUDENT_FIRSTNAME_COLUMN))
            {
                indexes[0] = i;
            }
            else if (cleanHeader.equalsIgnoreCase(STUDENT_LASTNAME_COLUMN))
            {
                indexes[1] = i;
            }
            else if (cleanHeader.equalsIgnoreCase(PARENT1_NAME_COLUMN))
            {
                indexes[2] = i;
            }
            else if (cleanHeader.equalsIgnoreCase(PARENT2_NAME_COLUMN))
            {
                indexes[3] = i;
            }
            else if (cleanHeader.equalsIgnoreCase(ADDRESS1_COLUMN))
            {
                indexes[4] = i;
            }
            else if (cleanHeader.equalsIgnoreCase(ADDRESS2_COLUMN))
            {
                indexes[5] = i;
            }
        }
        return indexes;
    }

    /**
     * Konvertuje hodnotu bunky na reťazec
     * @param cell bunka z Excel súboru
     * @return textová hodnota bunky
     */
    private String getCellValueAsString(Cell cell)
    {
        if (cell == null) return "";
        switch (cell.getCellType())
        {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell))
                {
                    return cell.getDateCellValue().toString();
                }
                return String.valueOf((int) cell.getNumericCellValue()).trim();
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue()).trim();
            case FORMULA:
                try
                {
                    return cell.getStringCellValue().trim();
                } catch (IllegalStateException e)
                {
                    return String.valueOf((int) cell.getNumericCellValue()).trim();
                }
            case BLANK:
                return "";
            default:
                return "";
        }
    }

    /**
     * Vytvorí objekt ImportedData z hodnôt riadku
     * @param values hodnoty zo stĺpcov
     * @param columnIndexes indexy jednotlivých typov stĺpcov
     * @return ImportedData objekt alebo null ak sú údaje neúplné
     */
    private ImportedData createImportedData(String[] values, int[] columnIndexes)
    {
        if (values == null || values.length == 0) return null;
        ImportedData data = new ImportedData();

        // Nastavenie hodnôt do objektu na základe nájdených indexov
        if (columnIndexes[0] != -1 && columnIndexes[0] < values.length)
        {
            data.setStudentFirstName(cleanValue(values[columnIndexes[0]]));
        }
        if (columnIndexes[1] != -1 && columnIndexes[1] < values.length)
        {
            data.setStudentLastName(cleanValue(values[columnIndexes[1]]));
        }
        if (columnIndexes[2] != -1 && columnIndexes[2] < values.length)
        {
            data.setParent1Name(cleanValue(values[columnIndexes[2]]));
        }
        if (columnIndexes[3] != -1 && columnIndexes[3] < values.length)
        {
            data.setParent2Name(cleanValue(values[columnIndexes[3]]));
        }
        if (columnIndexes[4] != -1 && columnIndexes[4] < values.length)
        {
            data.setAddress1(cleanValue(values[columnIndexes[4]]));
        }
        if (columnIndexes[5] != -1 && columnIndexes[5] < values.length)
        {
            data.setAddress2(cleanValue(values[columnIndexes[5]]));
        }

        // Ak sú mená študentov prázdne, vrátime null
        if ((data.getStudentFirstName() == null || data.getStudentFirstName().isEmpty()) &&
                (data.getStudentLastName() == null || data.getStudentLastName().isEmpty()))
        {
            return null;
        }

        return data;
    }

    /**
     * Vyčistí hodnotu - odstráni úvodzovky a zbytočné medzery
     * @param value hodnota na vyčistenie
     * @return vyčistená hodnota
     */
    private String cleanValue(String value)
    {
        if (value == null) return "";
        return value.replace("\"", "").trim();
    }

    /**
     * Pomocná trieda pre informácie o hlavičke súboru
     */
    private static class HeaderInfo
    {
        final boolean hasHeader;    // či súbor obsahuje hlavičku
        final int rowIndex;        // index riadku s hlavičkou
        final String[] headers;   // názvy stĺpcov

        /**
         * Konštruktor pre HeaderInfo
         * @param hasHeader určuje či súbor obsahuje hlavičku
         * @param rowIndex index riadku s hlavičkou (-1 ak nie je hlavička)
         * @param headers pole názvov stĺpcov
         */
        HeaderInfo(boolean hasHeader, int rowIndex, String[] headers)
        {
            this.hasHeader = hasHeader;
            this.rowIndex = rowIndex;
            this.headers = headers;
        }
    }
}