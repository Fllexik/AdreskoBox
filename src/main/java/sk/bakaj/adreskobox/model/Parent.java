package sk.bakaj.adreskobox.model;

public class Parent
{
    private String fullName;
    private String fullAddress;

    public Parent(String fullName, String fullAddress)
    {
        this.fullName = fullName;
        this.fullAddress = fullAddress;
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
        return fullAddress;
    }

    public void setFullAddress(String fullAddress)
    {
        this.fullAddress = fullAddress;
    }
}
