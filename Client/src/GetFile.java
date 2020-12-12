import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

/**
 * Klasa pobierająca plik z przekazanego gniazda.
 *
 * @author Piotr Kupis
 * @version 1.0, 15 czerwiec 2020
 */
public class GetFile implements Runnable {

    private Socket socket;
    private InputStream getFile;
    private DataInputStream getInformation;
    private File directory;
    private String fileName;
    private String kindOfChange;
    private ArrayList<String> listOfReceivedFiles;

    /**
     * Konstruktor tworzący obiekt klasy pobierającej plik z przekazanego gniazda.
     *
     * @param socket gniazdo z którego pobierany jest plik
     * @param fileName nazwa pobieranego pliku
     * @param kindOfChange rodzaj zmiany
     * @param directory uchwyt do lokalnego katologu użytkownika
     * @param listOfReceivedFiles lista odebranych plików
     * @throws IOException może rzucić IOException
     */
    GetFile(Socket socket,String fileName,String kindOfChange,File directory,ArrayList<String> listOfReceivedFiles) throws IOException {
        this.socket=socket;
        getFile=socket.getInputStream();
        getInformation=new DataInputStream(getFile);
        this.fileName=fileName;
        this.kindOfChange=kindOfChange;
        this.directory=directory;
        this.listOfReceivedFiles=listOfReceivedFiles;
    }

    /**
     * Metoda pobierająca plik z przekazanego gniazda.
     */
    @Override
    public void run() {
        long fileLength;
        File file=null;
        FileOutputStream fileOutput=null;

        try {
            file=new File(directory+"//"+fileName);

            if(kindOfChange.equals("NEW")){

                synchronized (listOfReceivedFiles){
                    listOfReceivedFiles.add(fileName);
                }
                fileLength=getInformation.readLong();
                if(!file.exists())
                    file.createNewFile();
                fileOutput=new FileOutputStream(file);

                for(int i=0;i<fileLength;++i){
                    fileOutput.write(getFile.read());
                }
                fileOutput.close();
            }
        }
        catch(SocketException e){
            try {
                if(fileOutput!=null){
                    fileOutput.close();
                    file.delete();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                getFile.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}