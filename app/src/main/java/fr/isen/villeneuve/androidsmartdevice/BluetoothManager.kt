package fr.isen.villeneuve.androidsmartdevice

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic

object BluetoothManager {
    var bluetoothGatt: BluetoothGatt? = null
    var ledCharacteristic: BluetoothGattCharacteristic? = null
    var isConnected: Boolean = false

}
