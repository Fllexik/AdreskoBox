package sk.bakaj.adreskobox.model;

public class Parent
{
    private String fullName;
    private String FullAddress;

    public Parent(String fullName, String fullAddress)
    {
        this.fullName = fullName;
        FullAddress = fullAddress;
    }

    public String getFullName()
    {
        return fullName;
    }

    public void setFullName(String fullName)
    {
        this.fullName = fullName;
    }

    public String getFullAddress()
    {
        return FullAddress;
    }

    public void setFullAddress(String fullAddress)
    {
        FullAddress = fullAddress;
    }
}
