package fr.isen.villeneuve.androidsmartdevice

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothProfile
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat

class DeviceConnectionActivity : ComponentActivity() {
    private var bluetoothGatt: BluetoothGatt? = null
    private var bluetoothDevice: BluetoothDevice? = null
    private var ledCharacteristic: BluetoothGattCharacteristic? = null
    private var isConnected by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val deviceName = intent.getStringExtra("deviceName")
        val deviceAddress = intent.getStringExtra("deviceAddress")

        bluetoothDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress)

        setContent {
            DeviceScreen(deviceName = deviceName, deviceAddress = deviceAddress)
        }
    }

    private fun connectToDevice() {
        bluetoothGatt = bluetoothDevice?.connectGatt(this, false, object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.d("BLE", "Connected to GATT server. Discovering services...")
                    gatt.discoverServices()
                    isConnected = true
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.d("BLE", "Disconnected from GATT server.")
                    isConnected = false
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    val services = gatt.services
                    ledCharacteristic = services?.get(2)?.characteristics?.get(0)
                    Log.d("BLE", "Services discovered: ${services.map { it.uuid }}")
                } else {
                    Log.e("BLE", "Service discovery failed with status $status")
                }
            }

            override fun onCharacteristicWrite(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                status: Int
            ) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.d("BLE", "Characteristic written successfully: ${characteristic.uuid}")
                } else {
                    Log.e("BLE", "Failed to write characteristic: ${characteristic.uuid}")
                }
            }
        })
    }

    private fun disconnectFromDevice() {
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null
        Log.d("BLE", "Disconnected from device.")
        isConnected = false
        Toast.makeText(this, "Disconnected from device", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun writeToLEDCharacteristic(state: LEDStateEnum) {
        if (ledCharacteristic != null) {
            ledCharacteristic?.value = state.hex
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            bluetoothGatt?.writeCharacteristic(ledCharacteristic)
            Log.d("BLE", "LED state set to: ${state.name}")
        } else {
            Log.e("BLE", "LED characteristic not found.")
        }
    }

    @Composable
    fun DeviceScreen(deviceName: String?, deviceAddress: String?) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text("Device Connection Status", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Device Name: ${deviceName ?: "Unknown"}", fontSize = 18.sp)
            Text("Device Address: ${deviceAddress ?: "Unknown"}", fontSize = 18.sp)
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = if (isConnected) "Connected" else "Disconnected",
                color = if (isConnected) Color.Green else Color.Red,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { connectToDevice() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF00FF)),
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Text("Connect to Device")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { writeToLEDCharacteristic(LEDStateEnum.LED_1) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF00FF)),
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Text("Turn On LED 1")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { writeToLEDCharacteristic(LEDStateEnum.LED_2) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF00FF)),
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Text("Turn On LED 2")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { writeToLEDCharacteristic(LEDStateEnum.LED_3) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF00FF)),
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Text("Turn On LED 3")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { writeToLEDCharacteristic(LEDStateEnum.NONE) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF00FF)),
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Text("Turn Off LED")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { disconnectFromDevice() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF00FF)),
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Text("Disconnect from Device")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disconnectFromDevice()
    }
}


