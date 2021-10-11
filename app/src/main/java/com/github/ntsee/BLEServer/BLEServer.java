package com.github.ntsee.BLEServer;

import android.bluetooth.*;
import android.content.Context;
import android.util.Log;
import androidx.lifecycle.MutableLiveData;

import java.util.UUID;

public class BLEServer implements PowerServer {

    public static final String TAG = "BLEServer";
    private static final UUID SERVICE_UUID = UUID.fromString("0000b81d-0000-1000-8000-00805f9b34fb");
    private static final UUID MESSAGE_UUID = UUID.fromString("7db3e235-3608-41f3-a03c-955fcbd2ea4b");
    private static final BluetoothGattCharacteristic MESSAGE_CHARACTERISTIC = new BluetoothGattCharacteristic(
            MESSAGE_UUID,
            BluetoothGattCharacteristic.PROPERTY_WRITE,
            BluetoothGattCharacteristic.PERMISSION_WRITE
    );

    static {
        MESSAGE_CHARACTERISTIC.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
    }

    private final MutableLiveData<String> logger = new MutableLiveData<>();
    private final MutableLiveData<Integer> counter = new MutableLiveData<>();
    private final BluetoothGattServer server;

    public BLEServer(boolean responses, Context context) {
        BluetoothManager manager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        MESSAGE_CHARACTERISTIC.setWriteType(responses ? BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT : BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        this.server = manager.openGattServer(context, this.new BLEServerGattServerCallback());
        this.server.addService(this.createService());
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
    public void close() {
        this.server.close();
    }

    private BluetoothGattService createService() {
        BluetoothGattService service = new BluetoothGattService(SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY);
        service.addCharacteristic(MESSAGE_CHARACTERISTIC);
        return service;
    }

    class BLEServerGattServerCallback extends BluetoothGattServerCallback {

        private int received = 0;

        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            super.onConnectionStateChange(device, status, newState);
            boolean success = status == BluetoothGatt.GATT_SUCCESS;
            boolean connected = newState == BluetoothProfile.STATE_CONNECTED;
            if (success && connected) {
                Log.d(TAG, String.format("%s connected to the server.\n", device.getAddress()));
                this.received = 0;
            } else {
                Log.d(TAG, String.format("%s disconnected to the server.\n", device.getAddress()));
            }
        }

        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);
            if (characteristic.getUuid().equals(MESSAGE_UUID)) {
                if (responseNeeded) {
                    server.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null);
                }
                if (value != null) {
                    counter.postValue(this.received += value.length);
                }
            }
        }
    }
}