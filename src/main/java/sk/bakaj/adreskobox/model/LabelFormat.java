package sk.bakaj.adreskobox.model;

public class LabelFormat
{
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

    public LabelFormat(double width, double height, int columns, int rows, double leftMargin, double rightMargin, double topMargin, double bottomMargin, double horizontalGap, double verticalGap)
    {
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
}
