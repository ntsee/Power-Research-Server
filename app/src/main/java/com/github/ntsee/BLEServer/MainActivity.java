package com.github.ntsee.BLEServer;

import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    private static final PowerServer.Type POWER_SERVER_TYPE = PowerServer.Type.WIFI;
    private static final boolean BLE_USE_ACKNOWLEDGEMENTS = false;
    private static final int WIFI_BUFFER_SIZE = 500;

    private TextView tv_log;
    private TextView tv_counter;
    private PowerServer server;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.tv_log = this.findViewById(R.id.tv_log);
        this.tv_counter = this.findViewById(R.id.tv_counter);
        switch (POWER_SERVER_TYPE) {
            case BLE: this.server = new BLEServer(BLE_USE_ACKNOWLEDGEMENTS, this); break;
            case WIFI: this.server = new WifiServer(WIFI_BUFFER_SIZE, this); break;
        }

        this.server.getDataCounter().observe(this, newValue -> this.tv_counter.setText(String.valueOf(newValue)));
        this.server.getLogger().observe(this, newValue -> this.tv_log.append(newValue));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.server.close();
    }
}