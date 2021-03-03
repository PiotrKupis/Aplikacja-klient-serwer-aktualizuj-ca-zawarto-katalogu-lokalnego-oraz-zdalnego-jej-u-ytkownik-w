import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * Główna klasa aplikacji serwerowej, tworzy ona interfejs graficzny oraz wywołuje wątek odpowiedzialny za funkcjonalnosć serwera.
 *
 * @author Piotr Kupis
 * @version 1.0, 15 czerwiec 2020
 */
public class ServerApp extends JFrame {

    private JPanel mainPanel;
    private JLabel usersLabel;
    private JLabel serverPanelBackground;
    private JPanel clientsPanel;
    private JScrollPane clientsScrollPane;
    private SwingWorker server;

    /**
     * Konstruktor tworzący obiekt głównej klasy aplikacji serwerowej.
     *
     * @throws IOException może rzucić IOException
     */
    ServerApp() throws IOException {
        super("Aplikacja serwerowa");
        initComponents();
        setVisible(true);

        server = new Server(clientsPanel, clientsScrollPane, usersLabel);
        server.execute();
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
                    new ServerApp();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Metoda inicjalizująca elementy graficzne aplikacji serwerowej.
     */
    private void initComponents() {
        mainPanel = new JPanel();
        mainPanel.setPreferredSize(new Dimension(800, 600));
        mainPanel.setLayout(null);
        add(mainPanel);

        usersLabel = new JLabel();
        usersLabel.setFont(new Font("Fira Code Medium", 1, 33));
        usersLabel.setForeground(new Color(255, 255, 255));
        usersLabel.setBounds(100, 60, 600, 40);
        usersLabel.setHorizontalAlignment(JLabel.CENTER);
        usersLabel.setText("Aktywni użytkownicy:");
        mainPanel.add(usersLabel);

        clientsPanel = new JPanel();
        clientsPanel.setLayout(new GridLayout(0, 3, 5, 5));
        clientsPanel.setBackground(new Color(35, 141, 200));
        clientsPanel.setPreferredSize(new Dimension(760, 300));
        mainPanel.add(clientsPanel);

        clientsScrollPane = new JScrollPane();
        clientsScrollPane.setViewportView(clientsPanel);
        clientsScrollPane.setBounds(20, 150, 760, 300);
        clientsScrollPane.setWheelScrollingEnabled(true);
        clientsScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        clientsScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        mainPanel.add(clientsScrollPane);

        serverPanelBackground = new JLabel();
        serverPanelBackground.setIcon(new ImageIcon("C:\\Users\\Admin\\IdeaProjects\\serwer\\src\\background.png"));
        serverPanelBackground.setBounds(0, 0, 800, 600);
        mainPanel.add(serverPanelBackground);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
        setResizable(false);
    }
}
