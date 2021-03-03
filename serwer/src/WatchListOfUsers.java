import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.TreeMap;

/**
 * Klasa aktualizująca informacje klienta o liście aktywnych użytkowników na serwerze.
 *
 * @author Piotr Kupis
 * @version 1.0, 15 czerwiec 2020
 */
public class WatchListOfUsers implements Runnable {

    private DataOutputStream sendInformation;
    private TreeMap<Integer, String> listOfUsers;

    /**
     * Konstruktor tworzący obiekt klasy aktualizującej informacje klienta o liście aktywnych użytkowników na serwerze.
     *
     * @param sendInformation strumień wyjściowy, zawierający informację przekazywane do klienta
     * @param listOfUsers     lista aktywnych użytkowników na serwerze
     */
    WatchListOfUsers(DataOutputStream sendInformation, TreeMap<Integer, String> listOfUsers) {
        this.sendInformation = sendInformation;
        this.listOfUsers = listOfUsers;
    }

    /**
     * Metoda sprawdzająca oraz aktualizująca informacje klienta o liście aktywnych użytkowników na serwerze.
     */
    @Override
    public void run() {
        boolean isFound;
        ArrayList<String> clientListOfUsers = new ArrayList<String>();

        try {
            while (true) {

                synchronized (listOfUsers) {

                    if (clientListOfUsers.size() == 0) {
                        for (String userName : listOfUsers.values()) {
                            sendInformation.writeUTF("NEW");
                            sendInformation.writeUTF(userName);
                            clientListOfUsers.add(userName);
                        }
                    } else {
                        for (String userName : listOfUsers.values()) {
                            isFound = false;
                            for (String userName2 : clientListOfUsers) {
                                if (userName.equals(userName2)) {
                                    isFound = true;
                                    break;
                                }
                            }

                            if (!isFound) {
                                sendInformation.writeUTF("NEW");
                                sendInformation.writeUTF(userName);
                                clientListOfUsers.add(userName);
                            }
                        }


                        for (String userName : clientListOfUsers) {
                            isFound = false;
                            for (String userName2 : listOfUsers.values()) {
                                if (userName.equals(userName2)) {
                                    isFound = true;
                                    break;
                                }
                            }

                            if (!isFound) {
                                sendInformation.writeUTF("DELETED");
                                sendInformation.writeUTF(userName);
                                clientListOfUsers.remove(userName);
                            }
                        }
                    }
                    listOfUsers.wait();
                }
            }
        } catch (IOException | InterruptedException | ConcurrentModificationException e) {
        }
    }
}