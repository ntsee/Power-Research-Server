package com.github.ntsee.BLEServer;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;
import androidx.lifecycle.MutableLiveData;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Locale;

public class WifiServer implements Runnable, PowerServer {

    private final byte[] buffer;
    private final String ip;
    private final MutableLiveData<String> logger = new MutableLiveData<>();
    private final MutableLiveData<Integer> counter = new MutableLiveData<>();
    private boolean running = true;

    public WifiServer(int size, Context context) {
        this.buffer = new byte[size];
        WifiManager wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        this.ip = Formatter.formatIpAddress(wifi.getConnectionInfo().getIpAddress());
        new Thread(this).start();
    }

    @Override
    public MutableLiveData<String> getLogger() {
        return this.logger;
    }

    @Override
    public MutableLiveData<Integer> getDataCounter() {
        return this.counter;
    }

    @Override
    public synchronized void close() {
        this.running = false;
    }

    @Override
    public void run() {
        try (ServerSocket server = new ServerSocket(8078)) {
            this.logger.postValue(String.format(Locale.ENGLISH, "Server started %s:%d\n", this.ip, server.getLocalPort()));
            while (this.isRunning()) {
                try (Socket client = server.accept()) {
                    this.logger.postValue(String.format(Locale.ENGLISH, "%s connected to the server.\n", client.getInetAddress()));

                    int total = 0, read = 0;
                    while (this.isRunning() && read != -1) {
                        this.counter.postValue(total += read);
                        read = client.getInputStream().read(this.buffer);
                    }

                    this.logger.postValue(String.format(Locale.ENGLISH, "%s disconnected from the server.\n", client.getInetAddress()));

                }
            }
        } catch (IOException e) {
            this.logger.postValue(String.format(Locale.ENGLISH, "ServerSocket Exception - %s\n", e));
        }
    }

    private synchronized boolean isRunning() {
        return this.running;
    }
}
