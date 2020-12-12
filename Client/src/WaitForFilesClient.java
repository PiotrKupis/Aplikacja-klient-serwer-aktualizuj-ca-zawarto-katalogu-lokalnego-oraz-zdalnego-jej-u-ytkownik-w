import javax.swing.*;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Klasa oczekująca na nowe pliki od serwera oraz wywoływująca wątki je pobierające.
 *
 * @author Piotr Kupis
 * @version 1.0, 15 czerwiec 2020
 */
public class WaitForFilesClient extends SwingWorker<Void,Change> {

    private ServerSocket waitForFiles;
    private File directory;
    private ExecutorService threadPool;
    private ArrayList<String> listOfReceivedFiles;
    private DefaultListModel<String> filesListModel;
    private JLabel actionLabel;

    /**
     * Konstruktor tworzący obiekt klasy oczekującej na nowe pliki od serwera.
     *
     * @param threadPool pula wątków przeznaczona do odbierania i wysłania plików
     * @param portNumber numer portu, na którym klient pobiera pliki od serwera
     * @param directory uchwyt do lokalnego katologu użytkownika
     * @param listOfReceivedFiles lista odebranych plików
     * @param filesListModel model listy plików katalogu użytkownika w interfejsie graficznym
     * @param actionLabel etykieta zawierająca informację czym aktualnie zajmuje się aplikacja kliencka
     * @throws IOException może rzucić IOException
     */
    WaitForFilesClient(ExecutorService threadPool,int portNumber,File directory,ArrayList<String> listOfReceivedFiles,DefaultListModel<String> filesListModel,JLabel actionLabel) throws IOException {
        this.waitForFiles=new ServerSocket(portNumber);
        this.directory=directory;
        this.threadPool=threadPool;
        this.listOfReceivedFiles=listOfReceivedFiles;
        this.filesListModel=filesListModel;
        this.actionLabel=actionLabel;
    }

    /**
     * Metoda oczekująca na nowe pliki oraz wywoływująca wątki pobierające przychodzące pliki.
     *
     * @throws Exception może rzucić Exception
     */
    @Override
    protected Void doInBackground() throws Exception {

        Socket socket;
        DataInputStream getInformation;
        Runnable threadGettingFile;
        String fileName,kindOfChange;

        while(true) {
            socket = waitForFiles.accept();
            getInformation=new DataInputStream(socket.getInputStream());
            fileName=getInformation.readUTF();
            kindOfChange=getInformation.readUTF();

            publish(new Change("NEW",fileName));
            publish(new Change("ACTION","Pobieram"));

            threadGettingFile=new Thread(new GetFile(socket,fileName,kindOfChange,directory,listOfReceivedFiles));
            threadPool.execute(threadGettingFile);
        }
    }

    /**
     * Metoda aktualizująca infomację o aktualnym działaniu aplikacji oraz wyświetlaną listę plików w interfejsie graficznym klienta.
     *
     * @param changesList lista zawierajaca informacje o zmianach do wprowadzenia w interfejsie graficznym aplikacji
     */
    @Override
    protected void process(List<Change> changesList) {

        for(Change change:changesList){
            if(change.getKindOfChange().equals("NEW")){
                filesListModel.addElement(change.getValue());
            }
            else if(change.getKindOfChange().equals("ACTION")){
                actionLabel.setText(change.getValue());
            }
        }
    }

    /**
     * Metoda zamykająca gniazdo oczekujące na nowe pliki, po zakończeniu działania programu.
     */
    @Override
    protected void done() {
        try {
            waitForFiles.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
