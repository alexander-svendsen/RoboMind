package src;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Note:
 *  - Thread or not ?
 *  - Don't think i need more then one open socket connection
 */
public class Communication {
    private ServerSocket serverSocket;
    private Socket client;
    private BufferedReader bufferedReader;
    private PrintWriter printWriter;


    public Communication(){
        try{
            serverSocket = new ServerSocket(9200);
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public void setUpConnection(){
        try{
            client = serverSocket.accept();
            InputStream inputStream = client.getInputStream();
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            printWriter = new PrintWriter(client.getOutputStream());
        }catch(IOException e){
            e.printStackTrace(); //ignore
        }
    }

    public String receive() throws IOException{
        return bufferedReader.readLine();  //must contain a \n to be a valid line to receive
    }

    public void send(String data){
        printWriter.write(data + '\n');
        printWriter.flush();
    }

    public void close(){
        try{
            client.close();
        }catch (IOException e){
            e.printStackTrace(); //ignore
        }
    }
}
