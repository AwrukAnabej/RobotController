package com.main.robotcontroller;

/**
 * Created by Rafał on 2016-01-23.
 */
public interface BluetoothSPPConnectionListener {
    public void bluetoothWrite(int bytes, byte[] buffer);
    public void onConnecting();
    public void onConnected();
    public void onConnectionFailed();
    public void onConnectionLost();
}
