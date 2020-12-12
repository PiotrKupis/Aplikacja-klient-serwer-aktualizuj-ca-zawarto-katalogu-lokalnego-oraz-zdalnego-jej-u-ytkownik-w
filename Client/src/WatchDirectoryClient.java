import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Klasa obserwująca katalog lokalny klienta i wywoływująca wątki obsługujące zmiany.
 *
 * @author Piotr Kupis
 * @version 1.0, 15 czerwiec 2020
 */
public class WatchDirectoryClient extends SwingWorker<Void, Change> {

    private int portNumber;
    private File directory;
    private ExecutorService threadPool;
    private ArrayList<String> listOfReceivedFiles;
    private DefaultListModel<String> filesListModel;
    private JLabel actionLabel;

    /**
     * Konstruktor tworzący obiekt klasy obserwującej katalog lokalny klienta i wywoływującej wątki obsługujące zmiany.
     *
     * @param threadPool pula wątków przeznaczona do odbierania i wysłania plików
     * @param portNumber numer portu, na którym aplikacja kliencka wysyła informacje o zmiannach w katalogu
     * @param directory uchwyt do lokalnego katologu użytkownika
     * @param listOfReceivedFiles lista odebranych plików
     * @param filesListModel model listy plików katalogu użytkownika w interfejsie graficznym
     * @param actionLabel etykieta zawierająca informację czym aktualnie zajmuje się aplikacja kliencka
     */
    WatchDirectoryClient(ExecutorService threadPool,int portNumber,File directory,ArrayList<String> listOfReceivedFiles,DefaultListModel<String> filesListModel,JLabel actionLabel){
        this.portNumber=portNumber;
        this.directory=directory;
        this.threadPool=threadPool;
        this.listOfReceivedFiles=listOfReceivedFiles;
        this.filesListModel=filesListModel;
        this.actionLabel=actionLabel;
    }

    /**
     * Metoda obserwująca katalog lokalny użytkownika i wywoływująca wątki obsługujące zmiany.
     *
     * @throws Exception może rzucić Exception
     */
    @Override
    protected Void doInBackground() throws Exception {

        Runnable threadSendingFile;
        File[] listOfFiles = directory.listFiles();
        File[] newListOfFiles;
        boolean isFound;

        while (true){

            publish(new Change("ACTION","Sprawdzam"));
            newListOfFiles=directory.listFiles();

            if(!newListOfFiles.equals(listOfFiles)){

                for(File newFiles:newListOfFiles){
                    isFound=false;
                    for(File oldFiles:listOfFiles){
                        if(newFiles.getName().equals(oldFiles.getName())){
                            isFound=true;
                            break;
                        }
                    }

                    if(!isFound){
                        synchronized (listOfReceivedFiles) {
                            if(!listOfReceivedFiles.contains(newFiles.getName())){
                                listOfReceivedFiles.add(newFiles.getName());
                                threadSendingFile=new Thread(new SendFile("NEW",portNumber,directory.toString()+"\\"+newFiles.getName()));
                                publish(new Change("NEW",newFiles.getName()));
                                publish(new Change("ACTION","Wysyłam"));
                                threadPool.execute(threadSendingFile);
                            }
                        }
                    }
                }


                for(File oldFiles:listOfFiles){
                    isFound=false;
                    for(File newFiles:newListOfFiles){
                        if(oldFiles.getName().equals(newFiles.getName())){
                            isFound=true;
                            break;
                        }
                    }

                    if(!isFound){
                        synchronized (listOfReceivedFiles) {
                            if(listOfReceivedFiles.contains(oldFiles.getName()))
                                listOfReceivedFiles.remove(oldFiles.getName());
                        }
                        threadSendingFile=new Thread(new SendFile("DELETED",portNumber,directory.toString()+"\\"+oldFiles.getName()));
                        publish(new Change("DELETED",oldFiles.getName()));
                        threadPool.execute(threadSendingFile);
                    }
                }
                listOfFiles=newListOfFiles;
            }
            Thread.sleep(3000);
        }
    }

    /**
     * Metoda aktualizująca infomacje o aktualnym działaniu aplikacji oraz wyświetlaną listę plików w interfejsie graficznym klienta
     *
     * @param changesList lista zawierajaca informacje o zmianach do wprowadzenia w interfejsie graficznym aplikacji
     */
    @Override
    protected void process(List<Change> changesList) {

        for(Change change:changesList){
            if(change.getKindOfChange().equals("NEW")){
                filesListModel.addElement(change.getValue());
            }
            else if(change.getKindOfChange().equals("DELETED")){
                filesListModel.removeElement(change.getValue());
            }
            else{
                actionLabel.setText(change.getValue());
            }
        }
    }
}