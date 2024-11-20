import android.app.Service
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import java.util.*

class BluetoothService : Service() {

    private var bluetoothGatt: BluetoothGatt? = null
    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): BluetoothService = this@BluetoothService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    fun connectToDevice(device: BluetoothDevice) {
        bluetoothGatt = device.connectGatt(this, false, object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                super.onConnectionStateChange(gatt, status, newState)
                when (newState) {
                    BluetoothGatt.STATE_CONNECTED -> {
                        Log.d("BluetoothService", "Connected to GATT server")
                        gatt.discoverServices()
                    }
                    BluetoothGatt.STATE_DISCONNECTED -> {
                        Log.d("BluetoothService", "Disconnected from GATT server")
                    }
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                super.onServicesDiscovered(gatt, status)
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.d("BluetoothService", "Services discovered")
                }
            }
        })
    }

    fun writeToLedCharacteristic(uuidService: UUID, uuidCharacteristic: UUID, value: ByteArray) {
        val service: BluetoothGattService? = bluetoothGatt?.getService(uuidService)
        val characteristic: BluetoothGattCharacteristic? = service?.getCharacteristic(uuidCharacteristic)

        if (characteristic != null) {
            characteristic.value = value
            bluetoothGatt?.writeCharacteristic(characteristic)
        } else {
            Log.e("BluetoothService", "Characteristic not found")
        }
    }

    fun disconnect() {
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null
    }
}
