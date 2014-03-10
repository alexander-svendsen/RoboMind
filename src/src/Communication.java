package src;

import lejos.remote.nxt.BTConnector;
import lejos.remote.nxt.NXTConnection;
import src.lib.BroadcastThread;
import src.util.HostName;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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
    BTConnector btConnector;
    TcpThread tcpThread;
    BTThread btThread;
    NXTConnection bc;

    private final Object lock = new Object();
    private boolean connectionEstablished = true;

    public Communication(){
        try{
            serverSocket = new ServerSocket(9200);
            btConnector = new BTConnector();
            btConnector.setUpSocket();

            tcpThread = new TcpThread();
            tcpThread.start();
            btThread = new BTThread();
            btThread.start();

            //starts a udp discovery protocol
            String hostname = HostName.getHostName();
            BroadcastThread bt = new BroadcastThread(hostname);
            bt.start();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    class TcpThread extends Thread{
        @Override
        public void run() {
            try{
                client = serverSocket.accept();
                synchronized (lock){
                    if (connectionEstablished){
                        return;
                    }
                    bufferedReader = new BufferedReader(new InputStreamReader(client.getInputStream()));
                    printWriter = new PrintWriter(client.getOutputStream());
                    connectionEstablished = true;
                    lock.notifyAll();
                }

            }catch(IOException e){
                e.printStackTrace(); //ignore
            }

        }
    }

    class BTThread extends  Thread{
        @Override
        public void run() {
            bc = btConnector.waitForConnection();
            synchronized (lock){
                if (connectionEstablished){
                    return;
                }
                bufferedReader = new BufferedReader(new InputStreamReader(bc.openInputStream()));
                printWriter = new PrintWriter(bc.openOutputStream());
                connectionEstablished = true;
                lock.notifyAll();
            }

        }
    }

    public void setUpConnection(){
        connectionEstablished = false;

        if (!tcpThread.isAlive()){
            tcpThread = new TcpThread();
            tcpThread.start();
        }
        if(!btThread.isAlive()){
            btThread = new BTThread();
            btThread.start();
        }

        try {
            synchronized (lock){
                lock.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public String receive() throws IOException{
        return bufferedReader.readLine();  //must contain a \n to be a valid line to receive
    }

    public void send(String data){
        printWriter.write(data + '\n');
        printWriter.flush();
    }

    public void closeStreams() throws IOException {
        bufferedReader.close();
        printWriter.close();
    }

    public void close(){
        try{
            if (client != null){
                client.close();
                closeStreams();
            }
            if( bc != null){
                bc.close();
                closeStreams();
            }

        }catch (IOException e){
            e.printStackTrace(); //ignore
        }
    }

    public void shutdown(){
        this.close();
        try {
            serverSocket.close();
            btConnector.cancel();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
