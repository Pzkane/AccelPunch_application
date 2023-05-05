package com.accelpunch.net;

import androidx.lifecycle.MutableLiveData;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.Socket;

public class Client implements Runnable
{
    private Thread thread;
    private Socket socket;
    private String host;
    private Integer port;
    private BufferedReader dataInputStream;
    private DataOutputStream dataOutputStream;

    private MutableLiveData<String> data;

    public Client(String host, Integer port)
    {
        this.host = host;
        this.port = port;
        this.thread = new Thread( this );
        this.thread.setPriority( Thread.NORM_PRIORITY );
        this.thread.start();
    }

    private void connect() {
        boolean connected = false;
        do {
            // Wait for a connection
            System.out.println( "[Client -> NodeMCU] waiting for connection on host " + host + ":" + port.toString() + "..." );
            try {
                this.socket = new Socket(host, port);
                connected = true;
            } catch (ConnectException e) {
                if (e.getMessage().contains("ETIMEDOUT")) {
                    System.out.println("[Client] " + e.getMessage());
                } else if (e.getMessage().contains("ECONNREFUSED")) {
                    System.out.println("[Client] " + e.getMessage());
                    System.out.println("[Client] Check if NodeMCU server is started");
                } else {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            } catch (IOException e) {
                System.out.println("[Client -> NodeMCU] failed to accept");
                e.printStackTrace();
            }
        } while(!connected);
    }

    @Override
    public void run()
    {
        do {
            connect();
            System.out.println("[Client -> NodeMCU] client connected");

            // create input and output streams
            try {
                this.dataInputStream = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
                this.dataOutputStream = new DataOutputStream(new BufferedOutputStream(this.socket.getOutputStream()));
            } catch (IOException e) {
                System.out.println("[Client -> NodeMCU] failed to create streams");
                e.printStackTrace();
            }

            // send some test data
            try {
                this.dataOutputStream.writeChars("Hello from Android");
                this.dataOutputStream.flush();
            } catch (IOException e) {
                System.out.println("[Client -> NodeMCU] failed to send");
                e.printStackTrace();
            }

            // placeholder recv loop
            while (true) {
                try {
                    String test = this.dataInputStream.readLine();
//                System.out.println("bytes read: " + "\"" + test + "\"");
                    this.data.postValue(test);
                    if (test == null) break;
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
            System.out.println("[Client -> NodeMCU] server thread stopped");
        }while(true);
    }

    public MutableLiveData<String> getData() {
        if (data == null) {
            data = new MutableLiveData<String>();
        }
        return data;
    }
}
