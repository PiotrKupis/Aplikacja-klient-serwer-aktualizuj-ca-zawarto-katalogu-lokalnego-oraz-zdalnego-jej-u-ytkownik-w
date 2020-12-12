import javax.swing.*;
import java.io.DataInputStream;
import java.util.List;

/**
 * Klasa oczekująca na informację o zmianach w liście aktualnych użytkowników na serwerze.
 *
 * @author Piotr Kupis
 * @version 1.0, 15 czerwiec 2020
 */
public class WaitForListOfUsers extends SwingWorker<Void,Change>{

    private DataInputStream getInformation;
    private DefaultListModel<String> usersListModel;
    private JLabel actionLabel;

    /**
     * Konstruktor tworzący obiekt klasy oczekującej na informacje o zmianach w liście aktualnych użytkowników.
     *
     * @param getInformation strumień wejściowy, zawierający informację od serwera
     * @param usersListModel model listy aktualnych użytkowników w interfejsie graficznym
     * @param actionLabel etykieta zawierająca informację czym aktualnie zajmuje się aplikacja kliencka
     */
    WaitForListOfUsers(DataInputStream getInformation,DefaultListModel<String> usersListModel, JLabel actionLabel){
        this.getInformation=getInformation;
        this.usersListModel=usersListModel;
        this.actionLabel=actionLabel;
    }

    /**
     * Metoda oczekująca na informacje o zmianach w liście aktualnych użytkowników.
     *
     * @throws Exception może rzucić Exception
     */
    @Override
    protected Void doInBackground() throws Exception {

        String userName,action;
        while(true){

                action=getInformation.readUTF();
                userName=getInformation.readUTF();
                publish(new Change("ACTION","Pobieram"));

                if(action.equals("NEW")){
                    publish(new Change("NEW",userName));
                }
                else{
                    publish(new Change("DELETED",userName));
                }
                publish(new Change("ACTION","Sprawdzam"));
        }
    }

    /**
     * Metoda aktualizująca infomację o aktualnym działaniu aplikacji oraz wyświetlaną listę aktualnych użytkowników w interfejsie graficznym klienta
     *
     * @param changesList lista zawierajaca informacje o zmianach do wprowadzenia w interfejsie graficznym aplikacji
     */
    @Override
    protected void process(List<Change> changesList) {

        for(Change change:changesList){
            if(change.getKindOfChange().equals("NEW")){
                usersListModel.addElement(change.getValue());
            }
            else if(change.getKindOfChange().equals("DELETED")){
                usersListModel.removeElement(change.getValue());
            }
            else{
                actionLabel.setText(change.getValue());
            }
        }
    }
}