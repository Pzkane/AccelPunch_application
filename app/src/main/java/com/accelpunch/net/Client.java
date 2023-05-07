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
    private Thread _thread;
    private Socket _socket;
    private String _host;
    private Integer _port;
    private BufferedReader _dataInputStream;
    private DataOutputStream _dataOutputStream;

    private MutableLiveData<String> _data;

    public Client(String host, Integer port)
    {
        this._host = host;
        this._port = port;
        this._thread = new Thread( this );
        this._thread.setPriority( Thread.NORM_PRIORITY );
        this._thread.start();
    }

    private void connect() {
        boolean connected = false;
        do {
            // Wait for a connection
            System.out.println( "[Client -> NodeMCU] waiting for connection on host " + _host + ":" + _port.toString() + "..." );
            try {
                this._socket = new Socket(_host, _port);
                connected = true;
            } catch (ConnectException e) {
                if (e.getMessage().contains("ETIMEDOUT")) {
                    System.out.println("[Client] " + e.getMessage());
                }
                if (e.getMessage().contains("ECONNREFUSED")) {
                    System.out.println("[Client] " + e.getMessage());
                    System.out.println("[Client] Check if NodeMCU server is started");
                }
                if (e.getMessage().contains("ENETUNREACH")) {
                    System.out.println("[Client -> NodeMCU] Network is unreachable (" + _host + ")");

                }
                if (e.getMessage().contains("ECONNABORTED")) {
                    System.out.println("[Client -> NodeMCU] Connection to "+ _host + " aborted");
                }
                if (e.getMessage().contains("Failed to connect to")) {
                    System.out.println("[Client -> NodeMCU] Failed to connect to " + _host);
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
        // Continuously wait for connection with nodes
        do {
            connect();
            System.out.println("[Client -> NodeMCU] client connected");

            // create input and output streams
            try {
                this._dataInputStream = new BufferedReader(new InputStreamReader(this._socket.getInputStream()));
                this._dataOutputStream = new DataOutputStream(new BufferedOutputStream(this._socket.getOutputStream()));
            } catch (IOException e) {
                System.out.println("[Client -> NodeMCU] failed to create streams");
                e.printStackTrace();
            }

            // send some test data
            try {
                this._dataOutputStream.writeChars("Hello from Android");
                this._dataOutputStream.flush();
            } catch (IOException e) {
                System.out.println("[Client -> NodeMCU] failed to send");
                e.printStackTrace();
            }

            // Only listen to node updates
            while (true) {
                try {
                    String dataIn = this._dataInputStream.readLine();
                    this._data.postValue(dataIn);
                    if (dataIn == null) break;
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
            System.out.println("[Client -> NodeMCU] server thread stopped");
        } while(true);
    }

    public MutableLiveData<String> getData() {
        if (_data == null) {
            _data = new MutableLiveData<String>();
        }
        return _data;
    }
}
