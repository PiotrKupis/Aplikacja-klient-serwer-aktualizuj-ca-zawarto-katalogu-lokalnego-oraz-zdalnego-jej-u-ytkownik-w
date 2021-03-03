import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Główna klasa aplikacji klienckiej, tworzy ona interfejs graficzny oraz wywołuje i obsługuje jej funkcjonalności.
 *
 * @author Piotr Kupis
 * @version 1.0, 15 czerwiec 2020
 */
public class ClientApp extends JFrame {

    private JPanel mainPanel;
    private JLabel actionLabel;
    private JLabel usersLabel;
    private JLabel filesLabel;
    private JLabel panelBackground;
    private JList<String> usersList;
    private DefaultListModel<String> usersListModel;
    private JList<String> filesList;
    private DefaultListModel<String> filesListModel;
    private JScrollPane usersListScrollPane;
    private JScrollPane filesListScrollPane;
    private JButton sendToUserButton;

    private String userName;
    private File directory;
    private Socket clientSocket;
    private OutputStream sendFile;
    private DataOutputStream sendInformation;
    private DataInputStream getInformation;
    private int portToSendFile, portToGetFile;
    private SwingWorker threadWatchingDirectory, threadWaitingForFiles, threadWaitingForListOfUsers;
    private ArrayList<String> listOfReceivedFiles;
    private ExecutorService threadPool;

    /**
     * Konstruktor tworzący obiekt głównej klasy aplikacji klienckiej.
     *
     * @param userName nazwa użytkownika
     * @param path     ścieżka do lokalnego folderu klienta
     * @throws IOException może rzucić IOException
     */
    public ClientApp(String userName, String path) throws IOException {

        super("Aplikacja kliencka: " + userName);
        this.userName = userName;
        this.directory = new File(path);
        if (!directory.exists())
            directory.createNewFile();
        initComponents();
        setVisible(true);

        clientSocket = new Socket("localhost", 6000);
        sendFile = clientSocket.getOutputStream();
        sendInformation = new DataOutputStream(sendFile);
        getInformation = new DataInputStream(clientSocket.getInputStream());
        threadPool = Executors.newFixedThreadPool(6);
        listOfReceivedFiles = new ArrayList<String>();

        sendInformation.writeUTF(userName);
        portToSendFile = getInformation.readInt();
        portToGetFile = getInformation.readInt();

        threadWaitingForFiles = new WaitForFilesClient(threadPool, portToGetFile, directory, listOfReceivedFiles, filesListModel, actionLabel);
        threadWaitingForFiles.execute();

        updateDirectory();

        threadWaitingForListOfUsers = new WaitForListOfUsers(getInformation, usersListModel, actionLabel);
        threadWaitingForListOfUsers.execute();

        threadWatchingDirectory = new WatchDirectoryClient(threadPool, portToSendFile, directory, listOfReceivedFiles, filesListModel, actionLabel);
        threadWatchingDirectory.execute();


        sendToUserButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    sendFileToUser();
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null, "Wysyłanie pliku do podanego użytkownika nie powiodło się");
                }
            }
        });

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                try {
                    sendInformation.writeUTF("END");
                    threadWaitingForFiles.cancel(true);
                    threadWatchingDirectory.cancel(true);
                    threadWaitingForListOfUsers.cancel(true);
                    threadPool.shutdown();
                } catch (IOException e) {
                }
            }
        });
    }

    /**
     * Metoda rozpoczynająca działanie programu.
     *
     * @param args zbiór przekazanych argumentów do programu
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    new ClientApp(args[0], args[1]);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Metoda inicjalizująca elementy graficzne aplikacji klienckiej.
     */
    private void initComponents() {

        mainPanel = new JPanel();
        mainPanel.setPreferredSize(new Dimension(800, 600));
        mainPanel.setLayout(null);
        add(mainPanel);

        actionLabel = new JLabel();
        actionLabel.setFont(new Font("Fira Code Medium", 1, 34));
        actionLabel.setForeground(new Color(255, 255, 255));
        actionLabel.setBounds(100, 40, 600, 40);
        actionLabel.setHorizontalAlignment(JLabel.CENTER);
        actionLabel.setText("Trwa nazwiązywanie połączenia");
        mainPanel.add(actionLabel);

        usersLabel = new JLabel();
        usersLabel.setFont(new Font("Fira Code Medium", 1, 23));
        usersLabel.setForeground(new Color(255, 255, 255));
        usersLabel.setBounds(75, 130, 300, 30);
        usersLabel.setHorizontalAlignment(JLabel.CENTER);
        usersLabel.setText("Lista użytkowników:");
        mainPanel.add(usersLabel);

        usersListModel = new DefaultListModel<>();
        usersList = new JList(usersListModel);
        usersList.setBounds(60, 180, 320, 270);
        usersList.setForeground(new Color(255, 255, 255));
        usersList.setBackground(new Color(35, 141, 200));
        usersList.setFont(new Font("Fira Code Medium", 1, 18));
        DefaultListCellRenderer renderer = (DefaultListCellRenderer) usersList.getCellRenderer();
        renderer.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(usersList);

        usersListScrollPane = new JScrollPane();
        usersListScrollPane.setViewportView(usersList);
        usersListScrollPane.setBounds(60, 180, 320, 270);
        usersListScrollPane.setWheelScrollingEnabled(true);
        usersListScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        usersListScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        mainPanel.add(usersListScrollPane);


        filesLabel = new JLabel();
        filesLabel.setFont(new Font("Fira Code Medium", 1, 23));
        filesLabel.setForeground(new Color(255, 255, 255));
        filesLabel.setBounds(435, 130, 300, 30);
        filesLabel.setHorizontalAlignment(JLabel.CENTER);
        filesLabel.setText("Lista plików w katalogu:");
        mainPanel.add(filesLabel);

        filesListModel = new DefaultListModel<>();
        for (File file : Objects.requireNonNull(directory.listFiles()))
            filesListModel.addElement(file.getName());
        filesList = new JList(filesListModel);
        filesList.setForeground(new Color(255, 255, 255));
        filesList.setBackground(new Color(35, 141, 200));
        filesList.setFont(new Font("Fira Code Medium", 1, 18));
        filesList.setBounds(425, 180, 320, 270);
        renderer = (DefaultListCellRenderer) filesList.getCellRenderer();
        renderer.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(filesList);

        filesListScrollPane = new JScrollPane();
        filesListScrollPane.setViewportView(filesList);
        filesListScrollPane.setBounds(425, 180, 320, 270);
        filesListScrollPane.setWheelScrollingEnabled(true);
        filesListScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        filesListScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        mainPanel.add(filesListScrollPane);

        sendToUserButton = new JButton();
        sendToUserButton.setBounds(150, 500, 500, 40);
        sendToUserButton.setForeground(new Color(255, 255, 255));
        sendToUserButton.setBackground(new Color(35, 141, 200));
        sendToUserButton.setFont(new Font("Fira Code Medium", 1, 20));
        sendToUserButton.setHorizontalAlignment(SwingConstants.CENTER);
        sendToUserButton.setText("Wyslij plik do wybranego użytkownika");
        mainPanel.add(sendToUserButton);

        panelBackground = new JLabel();
        panelBackground.setIcon(new ImageIcon("C:\\Users\\Admin\\IdeaProjects\\Client\\src\\background.png"));
        panelBackground.setBounds(0, 0, 800, 600);
        mainPanel.add(panelBackground);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
        setResizable(false);
    }

    /**
     * Metoda wysyła nazwy plików znajdujących się w lokalnym katalogu na serwer.
     *
     * @throws IOException może rzucić IOException
     */
    private void updateDirectory() throws IOException {

        File[] listOfFiles = directory.listFiles();
        sendInformation.writeInt(listOfFiles.length);
        for (File file : listOfFiles) {
            sendInformation.writeUTF(file.getName());
        }
    }

    /**
     * Metoda wysyła wybrany plik do wybranego użytkownika, sprawdza przy tym poprawność wykonanej akcji.
     *
     * @throws IOException może rzucić IOException
     */
    private void sendFileToUser() throws IOException {

        if (usersList.isSelectionEmpty() || filesList.isSelectionEmpty()) {
            JOptionPane.showMessageDialog(null, "Nie wybrano użytkownika lub pliku");
        } else if (usersList.getSelectedValue().equals(userName)) {
            JOptionPane.showMessageDialog(null, "Nie można wysłać pliku do samego siebie");
        } else {
            sendInformation.writeUTF("SEND");
            sendInformation.writeUTF(usersList.getSelectedValue());
            sendInformation.writeUTF(filesList.getSelectedValue());
            synchronized (listOfReceivedFiles) {
                listOfReceivedFiles.add(filesList.getSelectedValue());
            }
        }
    }
}


