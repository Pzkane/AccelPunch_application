package com.accelpunch.net;

import android.app.Activity;

import androidx.lifecycle.MutableLiveData;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.UUID;

public class HttpRequest implements Runnable {
    private Thread _thread;

    private MutableLiveData<String> _response;
    private String _url, _port, _method, _payload;
    private Activity _context;

    public HttpURLConnection connection;
    public boolean connected = false;

    public HttpRequest(Activity ctx, String url, String port, String method, String payload) {
        _url = url;
        _port = port;
        _method = method;
        _context = ctx;
        _payload = payload;
    }

    public void execute() {
        // Start thread on this object internally
//        _response = new MutableLiveData<String>();
        _thread = new Thread( this );
        _thread.setPriority( Thread.NORM_PRIORITY );
        _thread.start();
    }

    public void setUrl(String url) {
        _url = url.trim();
    }
    public void setPayload(String payload) {
        _payload = payload;
    }

    @Override
    public void run() {
        _response.postValue("");
        try {
            URL url = new URL("http://" + _url.trim() + ":" + _port);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(_method);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            if (_method == "POST") {
                final String boundary = UUID.randomUUID().toString();
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                OutputStream os = connection.getOutputStream();
                os.write(_payload.getBytes());
                os.flush();
                os.close();
            } else {
                connection.setRequestProperty("Content-Type", "application/json");
                connection.connect();
            }
            connected = true;
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String line, appended = "";
                    while ((line = bufferedReader.readLine()) != null) {
                        appended += line + "\n";
                    }
                    _response.postValue(appended);
                }
            } else {
                System.out.println("[HttpRequest] " + connection.getResponseCode());
            }

        } catch (ConnectException e) {
            if (e.getMessage().contains("ENETUNREACH")) {
                _response.postValue("Network is unreachable (" + _url + ")");
            }
            if (e.getMessage().contains("ECONNABORTED")) {
                _response.postValue("Connection to "+ _url + " aborted");
            }
            if (e.getMessage().contains("Failed to connect to")) {
                _response.postValue("Failed to connect to " + _url);
            }
        } catch (UnknownHostException e) {
            if (e.getMessage().contains("Unable to resolve host")) {
                _response.postValue("Unable to resolve host " + _url);
            }
        } catch (SocketTimeoutException e) {
            if (e.getMessage().contains("failed to connect to")) {
                _response.postValue("Failed to connect to " + _url);
            }
        } catch (Exception  e) {
            e.printStackTrace();
        }
    }

    public MutableLiveData<String> getResponse() {
        if (_response == null) {
            _response = new MutableLiveData<String>();
        }
        return _response;
    }
}
