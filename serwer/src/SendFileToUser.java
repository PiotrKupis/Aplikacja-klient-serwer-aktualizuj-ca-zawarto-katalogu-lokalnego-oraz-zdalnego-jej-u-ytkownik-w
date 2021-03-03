import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.util.TreeMap;

/**
 * Klasa oczekująca na komunikat końca pracy od klienta oraz realizująca wysyłanie plików do innych użytkowników.
 *
 * @author Piotr Kupis
 * @version 1.0, 15 czerwiec 2020
 */
public class SendFileToUser implements Runnable {

    private DataInputStream getInformation;
    private String userName;
    private TreeMap<Integer, String> listOfUsers;

    /**
     * Konstruktor tworzacy klasę oczekującą na komunikat końca pracy od klienta oraz realizującą wysyłanie plików do innych użytkowników.
     *
     * @param getInformation strumień wejściowy, zawierający informację od klienta
     * @param userName       nazwa użytkownika
     * @param listOfUsers    lista aktywnych użytkowników na serwerze
     */
    SendFileToUser(DataInputStream getInformation, String userName, TreeMap<Integer, String> listOfUsers) {
        this.getInformation = getInformation;
        this.userName = userName;
        this.listOfUsers = listOfUsers;
    }

    /**
     * Metoda oczekująca na komunikat końca pracy od klienta oraz realizująca wysyłanie plików do innych użytkowników.
     */
    @Override
    public void run() {
        String destinationUser, fileName, action;
        int portNumber = 0;
        boolean isFound;
        File directory;
        File[] listOfFiles;
        Thread threadSendingFile;

        try {
            while (true) {

                action = getInformation.readUTF();
                if (!action.equals("END")) {

                    destinationUser = getInformation.readUTF();
                    fileName = getInformation.readUTF();

                    directory = new File("Directories\\" + destinationUser);
                    listOfFiles = directory.listFiles();

                    isFound = false;
                    for (File files : listOfFiles) {

                        if (files.getName().equals(fileName)) {
                            isFound = true;
                            break;
                        }
                    }

                    if (!isFound) {

                        synchronized (listOfUsers) {
                            for (Integer key : listOfUsers.keySet()) {
                                if (listOfUsers.get(key).equals(destinationUser)) {
                                    portNumber = key;
                                    break;
                                }
                            }
                        }

                        threadSendingFile = new Thread(new SendFile("NEW", portNumber, "Directories\\" + userName + "\\" + fileName));
                        threadSendingFile.start();
                        threadSendingFile.join();

                        threadSendingFile = new Thread(new SendFile("NEW", portNumber + 1, "Directories\\" + userName + "\\" + fileName));
                        threadSendingFile.start();
                        threadSendingFile.join();
                    }
                } else {
                    break;
                }
            }
        } catch (SocketException e) {
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}