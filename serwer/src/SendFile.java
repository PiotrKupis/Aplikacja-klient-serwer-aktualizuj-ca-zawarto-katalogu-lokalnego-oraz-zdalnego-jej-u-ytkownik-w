import java.io.*;
import java.net.Socket;

/**
 * Klasa wysyłająca nowy plik na przekazny port.
 *
 * @author Piotr Kupis
 * @version 1.0, 15 czerwiec 2020
 */
public class SendFile implements Runnable {

    private Socket clientSocket;
    private OutputStream sendFile;
    private DataOutputStream sendInformation;
    private File file;
    private String kindOfChange;

    /**
     * Konstruktor tworzący obiekt klasy wysyłającej nowy plik na przekazny port.
     *
     * @param kindOfChange rodzaj zmiany
     * @param portNumber   numer portu, na którym aplikacja serwerowa wysyła nowe pliki
     * @param fileName     nazwa wysyłanego pliku
     * @throws IOException może rzucić IOException
     */
    SendFile(String kindOfChange, int portNumber, String fileName) throws IOException {
        clientSocket = new Socket("localhost", portNumber);
        sendFile = clientSocket.getOutputStream();
        sendInformation = new DataOutputStream(sendFile);
        file = new File(fileName);
        this.kindOfChange = kindOfChange;
    }

    /**
     * Metoda wysyłająca nowy plik na przekazny port.
     */
    @Override
    public void run() {
        int theByte = 0;
        FileInputStream fileInput = null;

        try {
            if (kindOfChange.equals("NEW")) {

                Thread.sleep(3000);
                fileInput = new FileInputStream(file);
                sendInformation.writeUTF(file.getName());
                sendInformation.writeUTF("NEW");
                sendInformation.writeLong(file.length());

                while ((theByte = fileInput.read()) != -1) {
                    sendFile.write(theByte);
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileInput != null)
                    fileInput.close();
                sendFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}