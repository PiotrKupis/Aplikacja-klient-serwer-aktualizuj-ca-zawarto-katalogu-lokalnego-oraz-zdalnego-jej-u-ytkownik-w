import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Klasa obsługująca klientów, którzy dołączyli do serwera.
 *
 * @author Piotr Kupis
 * @version 1.0, 15 czerwiec 2020
 */
public class Client extends SwingWorker<Void,Change> {

    private JPanel clientsPanel;
    private JScrollPane clientsScrollPane;
    private JLabel usersLabel;
    private JPanel clientPanel;
    private JLabel clientPanelBackground;
    private JLabel userLabel;
    private JLabel userFileLabel;
    private JList<String> userFileList;
    private DefaultListModel userFileListModel;
    private JScrollPane userScrollPane;

    private Socket socket;
    private DataOutputStream sendInformation;
    private InputStream getFile;
    private DataInputStream getInformation;
    private int portToGetFile;
    private int portToSendFile;
    private ExecutorService threadPool;
    private int[] ports;
    private TreeMap<Integer, String> listOfUsers;
    private SwingWorker threadWaitingForFiles, threadWatchingDirectory;
    private Thread threadWatchingListOfUsers, threadSendingFilesToUsers;

    /**
     * Konstruktor tworzący obiekt klasy obsługującej klientów, którzy dołączyli do serwera.
     *
     * @param socket główne gniazdo przeznaczone do komunikacji klienta z serwerem
     * @param portToGetFile numer portu, na którym serwer pobiera pliki od klienta
     * @param portToSendFile numer portu, na którym serwer wysyła pliki do klienta
     * @param ports zbiór portów, zawierający informacje o zajętych portach
     * @param listOfUsers lista aktywnych użytkowników na serwerze
     * @param clientsPanel panel graficzny zawierający panele wszystkich klientów
     * @param clientsScrollPane scrol panelu graficznego zawierającego panele wszystkich klientów
     * @param usersLabel etykieta zawierająca napis "Aktywni użytkownicy:"
     * @throws IOException może rzucić IOException
     */
    Client(Socket socket, int portToGetFile, int portToSendFile, int[] ports, TreeMap<Integer, String> listOfUsers, JPanel clientsPanel, JScrollPane clientsScrollPane, JLabel usersLabel) throws IOException {

        this.socket = socket;
        this.sendInformation = new DataOutputStream(socket.getOutputStream());
        this.getFile = socket.getInputStream();
        this.getInformation = new DataInputStream(getFile);
        this.portToGetFile = portToGetFile;
        this.portToSendFile = portToSendFile;
        this.threadPool = Executors.newFixedThreadPool(6);
        this.ports = ports;
        this.listOfUsers = listOfUsers;

        this.clientsPanel = clientsPanel;
        this.clientsScrollPane = clientsScrollPane;
        this.usersLabel = usersLabel;
        initComponents();
    }

    /**
     * Metoda inicjalizująca panel nowego klienta w interfejsie graficznym serwera.
     */
    private void initComponents() {
        clientPanel = new JPanel();
        clientPanel.setSize(246, 300);
        clientPanel.setLayout(null);

        userLabel = new JLabel();
        userLabel.setFont(new Font("Fira Code Medium", 1, 18));
        userLabel.setForeground(new Color(255, 255, 255));
        userLabel.setBounds(15, 10, 216, 25);
        userLabel.setHorizontalAlignment(JLabel.CENTER);
        clientPanel.add(userLabel);

        userFileLabel = new JLabel();
        userFileLabel.setFont(new Font("Fira Code Medium", 1, 18));
        userFileLabel.setForeground(new Color(255, 255, 255));
        userFileLabel.setBounds(15, 40, 216, 25);
        userFileLabel.setHorizontalAlignment(JLabel.CENTER);
        userFileLabel.setText("Lista plików:");
        clientPanel.add(userFileLabel);

        userFileListModel = new DefaultListModel<>();
        userFileList = new JList(userFileListModel);
        userFileList.setForeground(new Color(255, 255, 255));
        userFileList.setBackground(new Color(35, 141, 200));
        userFileList.setFont(new Font("Fira Code Medium", 1, 12));
        userFileList.setBounds(15, 75, 216, 220);
        DefaultListCellRenderer renderer = (DefaultListCellRenderer) userFileList.getCellRenderer();
        renderer.setHorizontalAlignment(SwingConstants.CENTER);
        clientPanel.add(userFileList);

        userScrollPane = new JScrollPane();
        userScrollPane.setViewportView(userFileList);
        userScrollPane.setBounds(15, 75, 216, 220);
        userScrollPane.setBackground(new Color(35, 141, 200));
        userScrollPane.setWheelScrollingEnabled(true);
        userScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        userScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        clientPanel.add(userScrollPane);

        clientPanelBackground = new JLabel();
        clientPanelBackground.setIcon(new ImageIcon("C:\\Users\\Admin\\IdeaProjects\\serwer\\src\\backgroundSmall.png"));
        clientPanelBackground.setBounds(0, 0, 246, 300);
        clientPanel.add(clientPanelBackground);

        clientsPanel.add(clientPanel);


        int height = clientsPanel.getComponents().length / 3;
        if (clientsPanel.getComponents().length % 3 != 0)
            height++;
        clientsPanel.setPreferredSize(new Dimension(740, height * 300));

        if (clientsPanel.getComponents().length > 3) {
            usersLabel.setBounds(100, 20, 600, 40);
            clientsScrollPane.setBounds(20, 80, 760, 520);
        } else {
            usersLabel.setBounds(100, 60, 600, 40);
            clientsScrollPane.setBounds(20, 150, 760, 300);
        }
    }

    /**
     * Metoda wysyłająca nowe pliki na serwerze do klienta, po jego dołączeniu.
     *
     * @param directory uchwyt do katologu klienta na serwerze
     * @throws IOException może rzucić IOException
     */
    private void updateClientDirectory(File directory) throws IOException {

        ArrayList<String> clientFiles = new ArrayList<String>();
        Runnable threadSendingFile;
        String receivedFile;
        boolean isFound;
        int amountOfFiles;

        amountOfFiles = getInformation.readInt();
        for (int i = 0; i < amountOfFiles; ++i) {
            receivedFile = getInformation.readUTF();
            clientFiles.add(receivedFile);
        }

        for (File serverFile : directory.listFiles()) {
            isFound = false;
            for (String clientFile : clientFiles) {
                if (serverFile.getName().equals(clientFile)) {
                    isFound = true;
                    break;
                }
            }

            if (!isFound) {
                threadSendingFile = new Thread(new SendFile("NEW", portToSendFile, directory.toString() + "\\" + serverFile.getName()));
                threadPool.execute(threadSendingFile);
            }
        }
    }

    /**
     * Metoda realizująca obsługę klienta po stronie aplikacji serwerowej.
     *
     * @throws Exception może rzucić Exception
     */
    @Override
    protected Void doInBackground() throws Exception {

        File directory;
        String userName;

        userName = getInformation.readUTF();
        publish(new Change("NewUser", userName));
        synchronized (listOfUsers) {
            listOfUsers.put(portToGetFile, userName);
            listOfUsers.notifyAll();
        }

        ArrayList<String> listOfReceivedFiles = new ArrayList<String>();
        directory = new File("Directories\\" + userName);
        if (!directory.exists())
            directory.createNewFile();

        threadWaitingForFiles = new WaitForFilesServer(threadPool, portToGetFile, directory, listOfReceivedFiles, userFileListModel);
        threadWaitingForFiles.execute();

        sendInformation.writeInt(portToGetFile);
        sendInformation.writeInt(portToSendFile);

        for (File file : Objects.requireNonNull(directory.listFiles()))
            publish(new Change("NEW", file.getName()));

        updateClientDirectory(directory);

        threadWatchingListOfUsers = new Thread(new WatchListOfUsers(sendInformation, listOfUsers));
        threadWatchingListOfUsers.start();

        threadSendingFilesToUsers = new Thread(new SendFileToUser(getInformation, userName, listOfUsers));
        threadSendingFilesToUsers.start();

        threadWatchingDirectory = new WatchDirectoryServer(threadPool, portToSendFile, directory, listOfReceivedFiles, userFileListModel);
        threadWatchingDirectory.execute();

        threadSendingFilesToUsers.join();
        return null;
    }

    /**
     * Metoda aktualizująca nazwę oraz wyświetlaną listę plików klienta w interfejsie graficznym.
     *
     * @param changesList lista zawierajaca informacje o zmianach do wprowadzenia w interfejsie graficznym aplikacji
     */
    @Override
    protected void process(List<Change> changesList) {

        for (Change change : changesList) {
            if (change.getKindOfChange().equals("NewUser")) {
                userLabel.setText(change.getValue());
            } else if (change.getKindOfChange().equals("NEW")) {
                userFileListModel.addElement(change.getValue());
            }
        }
    }

    /**
     * Metoda kończąca wątki obsługujące klienta po stronie serwera.
     */
    @Override
    protected void done() {

        try {
            synchronized (listOfUsers) {
                listOfUsers.remove(portToGetFile);
                synchronized (ports) {
                    ports[(portToGetFile - 6001) / 2] = 0;
                }
                listOfUsers.notifyAll();
            }

            threadWatchingDirectory.cancel(true);
            threadWatchingListOfUsers.interrupt();
            threadWaitingForFiles.cancel(true);

            getFile.close();
            sendInformation.close();
            socket.close();
            clientsPanel.remove(clientPanel);

            int height = clientsPanel.getComponents().length / 3;
            if (clientsPanel.getComponents().length % 3 != 0)
                height++;
            clientsPanel.setPreferredSize(new Dimension(740, height * 300));

            if (clientsPanel.getComponents().length > 3) {
                usersLabel.setBounds(100, 20, 600, 40);
                clientsScrollPane.setBounds(20, 80, 760, 520);
            } else {
                usersLabel.setBounds(100, 60, 600, 40);
                clientsScrollPane.setBounds(20, 150, 760, 300);
            }

            clientsPanel.revalidate();
            clientsPanel.repaint();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
