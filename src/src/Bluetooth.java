package src;

import lejos.hardware.LocalBTDevice;

public class Bluetooth {
    private static LocalBTDevice bt;
    public Bluetooth() {
        bt = lejos.hardware.Bluetooth.getLocalDevice();
        System.out.println(bt.getFriendlyName());
        System.out.println(bt.getBluetoothAddress());
    }
}
