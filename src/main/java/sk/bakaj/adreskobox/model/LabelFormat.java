package sk.bakaj.adreskobox.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class LabelFormat
{
    private String name;
    private double width;
    private double height;
    private int columns;
    private int rows;
    private double leftMargin;
    private double rightMargin;
    private double topMargin;
    private double bottomMargin;
    private double horizontalGap;
    private double verticalGap;
    private int maxAddressLength;

    public LabelFormat(String name, double width, double height, int columns, int rows, double leftMargin, double rightMargin, double topMargin, double bottomMargin, double horizontalGap, double verticalGap, int maxAddressLength)
    {
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

    public String getName()
    {
        return name;
    }

    public double getWidth()
    {
        return width;
    }

    public double getHeight()
    {
        return height;
    }

    public int getColumns()
    {
        return columns;
    }

    public int getRows()
    {
        return rows;
    }

    public double getLeftMargin()
    {
        return leftMargin;
    }

    public double getRightMargin()
    {
        return rightMargin;
    }

    public double getTopMargin()
    {
        return topMargin;
    }

    public double getBottomMargin()
    {
        return bottomMargin;
    }

    public double getHorizontalGap()
    {
        return horizontalGap;
    }

    public double getVerticalGap()
    {
        return verticalGap;
    }

    public int getMaxAddressLength()
    {
        return maxAddressLength;
    }

    @Override
    public String toString()
    {
        return "LabelFormat{" +
                "name='" + name + '\'' +
                ", width=" + width +
                ", height=" + height +
                ", columns=" + columns +
                ", rows=" + rows +
                ", leftMargin=" + leftMargin +
                ", rightMargin=" + rightMargin +
                ", topMargin=" + topMargin +
                ", bottomMargin=" + bottomMargin +
                ", horizontalGap=" + horizontalGap +
                ", verticalGap=" + verticalGap +
                ", maxAddressLength=" + maxAddressLength +
                '}';
    }

    //Preddefinovane formaty štitkov
    public static ObservableList<LabelFormat> getPredefinedFormats()
    {
        return FXCollections.observableArrayList(
                new LabelFormat("A4 - 48,3 x 16,9 mm (64 ks)", 48.3, 16.9, 4, 16, 8.4,
                        8.4, 13.3, 13.3, 0, 0, 24);
    }
}
