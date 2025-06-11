package sk.bakaj.adreskobox.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Model reprezentujúci formát štítkov pre tlač adries.
 *
 * Obsahuje všetky potrebné parametre pre definovanie rozloženia štítkov
 * na stránke, vrátane rozmerov, okrajov a medzier medzi štítkami.
 *
 * Všetky rozmery sú v milimetroch.
 */
public class LabelFormat
{
    /** Názov formátu štítkov */
    private String name;

    /** Výška jedného štítka v mm */
    private double width;

    /** Výška jedného štítka v mm */
    private double height;

    /** Počet stĺpcov štítkov na stránke */
    private int columns;

    /** Počet riadkov štítkov na stránke */
    private int rows;

    /** Ľavý okraj stránky v mm */
    private double leftMargin;

    /** Pravý okraj stránky v mm */
    private double rightMargin;

    /** Horný okraj stránky v mm */
    private double topMargin;

    /** Dolný okraj stránky v mm */
    private double bottomMargin;

    /** Horizontálna medzera medzi štítkami v mm */
    private double horizontalGap;

    /** Vertikálna medzera medzi štítkami v mm */
    private double verticalGap;

    /** Maximálna dĺžka adresy v znakoch */
    private int maxAddressLength;

    /**
     * Vytvorí nový formát štítkov so zadanými parametrami.
     *
     * @param name názov formátu
     * @param width šírka štítka v mm
     * @param height výška štítka v mm
     * @param columns počet stĺpcov na stránke
     * @param rows počet riadkov na stránke
     * @param leftMargin ľavý okraj v mm
     * @param rightMargin pravý okraj v mm
     * @param topMargin horný okraj v mm
     * @param bottomMargin dolný okraj v mm
     * @param horizontalGap horizontálna medzera v mm
     * @param verticalGap vertikálna medzera v mm
     * @param maxAddressLength maximálna dĺžka adresy
     * @throws IllegalArgumentException ak sú zadané neplatné hodnoty
     */
    public LabelFormat(String name, double width, double height, int columns, int rows, double leftMargin, double rightMargin, double topMargin, double bottomMargin, double horizontalGap, double verticalGap, int maxAddressLength)
    {
        // Validácia vstupných parametrov
        validateParameters(name, width, height, columns, rows, leftMargin, rightMargin,
                topMargin, bottomMargin, horizontalGap, verticalGap, maxAddressLength);

        this.name = name;
        this.width = width;
        this.height = height;
        this.columns = columns;
        this.rows = rows;
        this.leftMargin = leftMargin;
        this.rightMargin = rightMargin;
        this.topMargin = topMargin;
        this.bottomMargin = bottomMargin;
        this.horizontalGap = horizontalGap;
        this.verticalGap = verticalGap;
        this.maxAddressLength = maxAddressLength;
    }

    /**
     * Validuje vstupné parametre konštruktora.
     *
     * @throws IllegalArgumentException ak sú parametre neplatné
     */
    private void validateParameters(String name, double width, double height, int columns, int rows,
                                    double leftMargin, double rightMargin, double topMargin, double bottomMargin,
                                    double horizontalGap, double verticalGap, int maxAddressLength) {

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Názov formátu nemôže byť prázdny");
        }

        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Rozmery štítka musia byť kladné hodnoty");
        }

        if (columns <= 0 || rows <= 0) {
            throw new IllegalArgumentException("Počet stĺpcov a riadkov musí byť kladný");
        }

        if (leftMargin < 0 || rightMargin < 0 || topMargin < 0 || bottomMargin < 0) {
            throw new IllegalArgumentException("Okraje nemôžu byť záporné");
        }

        if (horizontalGap < 0 || verticalGap < 0) {
            throw new IllegalArgumentException("Medzery nemôžu byť záporné");
        }

        if (maxAddressLength <= 0) {
            throw new IllegalArgumentException("Maximálna dĺžka adresy musí byť kladná");
        }
    }

    // Getter metódy s dokumentáciou

    /** @return názov formátu štítkov */
    public String getName()
    {
        return name;
    }

    /** @return šírka štítka v mm */
    public double getWidth()
    {
        return width;
    }

    /** @return výška štítka v mm */
    public double getHeight()
    {
        return height;
    }

    /** @return počet stĺpcov štítkov na stránke */
    public int getColumns()
    {
        return columns;
    }

    /** @return počet riadkov štítkov na stránke */
    public int getRows()
    {
        return rows;
    }

    /** @return ľavý okraj stránky v mm */
    public double getLeftMargin()
    {
        return leftMargin;
    }

    /** @return pravý okraj stránky v mm */
    public double getRightMargin()
    {
        return rightMargin;
    }

    /** @return horný okraj stránky v mm */
    public double getTopMargin()
    {
        return topMargin;
    }

    /** @return dolný okraj stránky v mm */
    public double getBottomMargin()
    {
        return bottomMargin;
    }

    /** @return horizontálna medzera medzi štítkami v mm */
    public double getHorizontalGap()
    {
        return horizontalGap;
    }

    /** @return vertikálna medzera medzi štítkami v mm */
    public double getVerticalGap()
    {
        return verticalGap;
    }

    /** @return maximálna dĺžka adresy v znakoch */
    public int getMaxAddressLength()
    {
        return maxAddressLength;
    }

    /**
     * Vypočíta celkový počet štítkov na jednej stránke.
     *
     * @return počet štítkov na stránke
     */
    public int getTotalLabelsPerPage()
    {
        return columns * rows;
    }

    /**
     * Vypočíta celkovú šírku všetkých štítkov vrátane medzier.
     *
     * @return celková šírka v mm
     */
    public double getTotalWidth()
    {
        return leftMargin + rightMargin + (columns * width) + ((columns - 1) * horizontalGap);
    }

    /**
     * Vypočíta celkovú výšku všetkých štítkov vrátane medzier.
     *
     * @return celková výška v mm
     */
    public double getTotalHeight()
    {
        return topMargin + bottomMargin + (rows * height) + ((rows - 1) * verticalGap);
    }

    /**
     * Overí, či formát štítkov sedí na štandardnú A4 stránku (210 x 297 mm).
     *
     * @return true ak sa zmestí na A4, inak false
     */
    public boolean fitsOnA4()
    {
        return getTotalWidth() <= 210.0 && getTotalHeight() <= 297.0;
    }

    @Override
    public String toString()
    {
        return name; // Pre zobrazenie v ComboBox a podobných komponentoch
    }
    /**
     * Poskytuje detailný popis formátu pre debugging.
     *
     * @return detailný reťazec s všetkými parametrami
     */
    public String toDetailedString() {
        return String.format("LabelFormat{name='%s', width=%.1f, height=%.1f, " +
                        "columns=%d, rows=%d, margins=[%.1f,%.1f,%.1f,%.1f], " +
                        "gaps=[%.1f,%.1f], maxLength=%d, total=%dx%d}",
                name, width, height, columns, rows,
                leftMargin, rightMargin, topMargin, bottomMargin,
                horizontalGap, verticalGap, maxAddressLength,
                getTotalLabelsPerPage(), (int)getTotalWidth());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        LabelFormat other = (LabelFormat) obj;
        return name.equals(other.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    /**
     * Poskytuje zoznam preddefinovaných formátov štítkov.
     *
     * @return observable zoznam dostupných formátov
     */
    public static ObservableList<LabelFormat> getPredefinedFormats()
    {
        return FXCollections.observableArrayList(
                // Avery štandardné formáty
                new LabelFormat("A4 - 48,3 x 16,9 mm (64 ks)",
                        48.3, 16.9, 4, 16,
                        8.4, 8.4, 13.3,
                        13.3, 0, 0, 24),

                new LabelFormat("A4 - 70 × 37 mm (24 ks)",
                        70.0, 37.0, 3, 8,
                        5.0, 5.0, 15.0, 15.0,
                        0, 0, 50),

                new LabelFormat("A4 - 105 × 148 mm (4 ks)",
                        105.0, 148.0, 2, 2,
                        0, 0, 0.5, 0.5,
                        0, 0, 100),

                new LabelFormat("A4 - 99,1 × 67,7 mm (8 ks)",
                        99.1, 67.7, 2, 4,
                        5.95, 5.95, 21.15, 21.15,
                        0, 0, 80)
        );
    }
}
