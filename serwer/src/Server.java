import javax.swing.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Klasa przyjmująca nowych klientów i wywoływująca wątki ich obsługujące.
 *
 * @author Piotr Kupis
 * @version 1.0, 15 czerwiec 2020
 */
public class Server extends SwingWorker<Void,Change> {

    private JPanel clientsPanel;
    private JScrollPane clientsScrollPane;
    private JLabel usersLabel;

    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    private TreeMap<Integer,String> listOfUsers;
    private int[] ports = new int[40];

    /**
     * Konstruktor tworzący obiekt klasy przyjmującej nowych klientów i wywoływującej wątki ich obsługujące.
     *
     * @param clientsPanel panel graficzny zawierający panele wszystkich klientów
     * @param clientsScrollPane scrol panelu graficznego zawierającego panele wszystkich klientów
     * @param usersLabel etykieta zawierająca napis "Aktywni użytkownicy:"
     * @throws IOException może rzucić IOException
     */
    Server(JPanel clientsPanel,JScrollPane clientsScrollPane,JLabel usersLabel) throws IOException {
        this.clientsPanel=clientsPanel;
        this.clientsScrollPane=clientsScrollPane;
        this.usersLabel=usersLabel;

        serverSocket = new ServerSocket(6000);
        threadPool = Executors.newFixedThreadPool(9);
        listOfUsers=new TreeMap<Integer, String>();
    }

    /**
     * Metoda oczekująca na nowych klientów i wywoływująca wątki ich obsługujące.
     *
     * @throws Exception może rzucić Exception
     */
    @Override
    protected Void doInBackground() throws Exception {

        int i;
        Socket socket;
        SwingWorker clientThread;

        while (true) {

            socket = serverSocket.accept();
            for (i = 0; i < ports.length; ++i) {
                if (ports[i] == 0)
                    break;
            }
            ports[i]=1;

            clientThread= new Client(socket, 6001 + i * 2, 6001 + i * 2 + 1,ports,listOfUsers,clientsPanel,clientsScrollPane,usersLabel);
            threadPool.execute(clientThread);
        }
    }

    /**
     * Metoda zamykająca gniazdo oczekujące na nowych klientów, po zakończeniu działania programu.
     */
    @Override
    protected void done() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
