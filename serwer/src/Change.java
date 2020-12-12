/**
 * Klasa służąca do przekazywania zmiany do wprowadzenia w interfejsie graficznym.
 *
 * @author Piotr Kupis
 * @version 1.0, 15 czerwiec 2020
 */
class Change{
    private String kindOfChange;
    private String value;

    /**
     * Konstruktor tworzący obiekt klasy służącej do przekazywania zmiany do wprowadzenia w interfejsie graficznym.
     *
     * @param kindOfChange rodzaj zmiany do wprowadzenia w interfejsie graficznym
     * @param value wartość wprowadzonej zmiany
     */
    Change(String kindOfChange, String value){
        this.kindOfChange=kindOfChange;
        this.value=value;
    }

    /**
     * @return rodzaj zmiany
     */
    public String getKindOfChange(){
        return kindOfChange;
    }

    /**
     * @return wartość zmiany
     */
    public String getValue() {
        return value;
    }
}