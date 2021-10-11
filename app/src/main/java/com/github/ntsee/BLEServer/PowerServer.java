package com.github.ntsee.BLEServer;

import androidx.lifecycle.MutableLiveData;

public interface PowerServer {

    MutableLiveData<String> getLogger();
    MutableLiveData<Integer> getDataCounter();
    void close();

    enum Type {
        BLE,
        WIFI,
    }
}
