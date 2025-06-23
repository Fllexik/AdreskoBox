package sk.bakaj.adreskobox.model;

/**
 * Trieda reprezentujúca importované údaje zo súboru alebo externého zdroja.
 * Obsahuje všetky potrebné informácie o študentovi, jeho rodičoch a adresách
 * v nespracovanom formáte pred konverziou na finálne objekty.
 */
public class ImportedData
{
    // Údaje o študentovi
    private String studentFirstName;    // Krstné meno študenta
    private String studentLastName;     // Priezvisko študenta
    // Mená rodičov
    private String parent1Name;         // Celé meno prvého rodiča
    private String parent2Name;         // Celé meno druhého rodiča
    // Adresy
    private String address1;            // Prvá adresa (prvého rodiča)
    private String address2;            // Druhá adresa (druhého rodiča)

    /**
     * Getter metóda pre získanie krstného mena študenta.
     *
     * @return krstné meno študenta alebo null ak nie je nastavené
     */
    public String getStudentFirstName()
    {
        return studentFirstName;
    }

    /**
     * Setter metóda pre nastavenie krstného mena študenta.
     *
     * @param studentFirstName krstné meno študenta
     */
    public void setStudentFirstName(String studentFirstName)
    {
        this.studentFirstName = studentFirstName;
    }

    /**
     * Getter metóda pre získanie priezviska študenta.
     *
     * @return priezvisko študenta alebo null ak nie je nastavené
     */
    public String getStudentLastName()
    {
        return studentLastName;
    }

    /**
     * Setter metóda pre nastavenie priezviska študenta.
     *
     * @param studentLastName priezvisko študenta
     */
    public void setStudentLastName(String studentLastName)
    {
        this.studentLastName = studentLastName;
    }

    /**
     * Getter metóda pre získanie mena prvého rodiča.
     *
     * @return celé meno prvého rodiča alebo null ak nie je nastavené
     */
    public String getParent1Name()
    {
        return parent1Name;
    }

    /**
     * Setter metóda pre nastavenie mena prvého rodiča.
     *
     * @param parent1Name celé meno prvého rodiča
     */
    public void setParent1Name(String parent1Name)
    {
        this.parent1Name = parent1Name;
    }

    /**
     * Getter metóda pre získanie mena druhého rodiča.
     *
     * @return celé meno druhého rodiča alebo null ak nie je nastavené
     */
    public String getParent2Name()
    {
        return parent2Name;
    }

    /**
     * Setter metóda pre nastavenie mena druhého rodiča.
     *
     * @param parent2Name celé meno druhého rodiča
     */
    public void setParent2Name(String parent2Name)
    {
        this.parent2Name = parent2Name;
    }

    /**
     * Getter metóda pre získanie prvej adresy.
     *
     * @return prvá adresa alebo null ak nie je nastavená
     */
    public String getAddress1()
    {
        return address1;
    }

    /**
     * Setter metóda pre nastavenie prvej adresy.
     *
     * @param address1 prvá adresa
     */
    public void setAddress1(String address1)
    {
        this.address1 = address1;
    }

    /**
     * Getter metóda pre získanie druhej adresy.
     *
     * @return druhá adresa alebo null ak nie je nastavená
     */
    public String getAddress2()
    {
        return address2;
    }

    /**
     * Setter metóda pre nastavenie druhej adresy.
     *
     * @param address2 druhá adresa
     */
    public void setAddress2(String address2)
    {
        this.address2 = address2;
    }
}
