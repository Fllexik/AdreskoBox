package sk.bakaj.adreskobox.model;

/**
 * Trieda reprezentujúca študenta s jeho základnými údajmi a rodičmi.
 * Každý študent má meno, priezvisko a môže mať až dvoch rodičov.
 */
public class Student
{
    // Privátne atribúty pre uchovanie údajov o študentovi
    private String firstName;   // Krstné meno študenta
    private String lastName;    // Priezvisko študenta
    private Parent parent1;     // Prvý rodič (napr. mama)
    private  Parent parent2;    // Druhý rodič (napr. otec)

    /**
     * Konštruktor pre vytvorenie študenta so zadaným menom a priezviskom.
     * Rodičia nie sú nastavení a musia sa pridať pomocou setter metód.
     *
     * @param firstName krstné meno študenta
     * @param lastName priezvisko študenta
     */
    public Student(String firstName, String lastName)
    {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    /**
     * Getter metóda pre získanie krstného mena študenta.
     *
     * @return krstné meno študenta
     */
    public String getFirstName()
    {
        return firstName;
    }

    /**
     * Setter metóda pre nastavenie krstného mena študenta.
     *
     * @param firstName nové krstné meno študenta
     */
    public void setFirstName(String firstName)
    {
        this.firstName = firstName;
    }

    /**
     * Getter metóda pre získanie priezviska študenta.
     *
     * @return priezvisko študenta
     */
    public String getLastName()
    {
        return lastName;
    }

    /**
     * Setter metóda pre nastavenie priezviska študenta.
     *
     * @param lastName nové priezvisko študenta
     */
    public void setLastName(String lastName)
    {
        this.lastName = lastName;
    }

    /**
     * Getter metóda pre získanie prvého rodiča študenta.
     *
     * @return prvý rodič alebo null ak nie je nastavený
     */
    public Parent getParent1()
    {
        return parent1;
    }

    /**
     * Setter metóda pre nastavenie prvého rodiča študenta.
     *
     * @param parent1 prvý rodič študenta
     */
    public void setParent1(Parent parent1)
    {
        this.parent1 = parent1;
    }

    /**
     * Getter metóda pre získanie druhého rodiča študenta.
     *
     * @return druhý rodič alebo null ak nie je nastavený
     */
    public Parent getParent2()
    {
        return parent2;
    }

    /**
     * Setter metóda pre nastavenie druhého rodiča študenta.
     *
     * @param parent2 druhý rodič študenta
     */
    public void setParent2(Parent parent2)
    {
        this.parent2 = parent2;
    }
}
