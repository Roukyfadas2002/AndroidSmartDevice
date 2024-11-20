package fr.isen.villeneuve.androidsmartdevice

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.isen.villeneuve.androidsmartdevice.ui.theme.AndroidSmartDeviceTheme
import java.util.*

class DeviceConnectionActivity : ComponentActivity() {

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var device: BluetoothDevice? = null
    private var deviceUUID: UUID? = null
    private var bluetoothGatt: BluetoothGatt? = null

    private var connectionStatus by mutableStateOf("Disconnected")
    private var deviceInfo by mutableStateOf<DeviceInfo?>(null)

    private val handler = Handler(Handler.Callback { msg ->
        when (msg.what) {
            CONNECTION_SUCCESS -> {
                // Connection successful
                connectionStatus = "Connected"
                showToast("Connection successful")
                // Redirect to LedControle activity
                navigateToLedControle()
            }
            CONNECTION_FAILED -> {
                // Connection failed
                connectionStatus = "Connection Failed"
                showToast("Connection failed")
            }
            else -> {
                // Handle other cases
            }
        }
        true
    })

    companion object {
        const val CONNECTION_SUCCESS = 1
        const val CONNECTION_FAILED = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Retrieve the device name, address, and UUIDs passed from the scan screen
        val deviceName = intent.getStringExtra("deviceName")
        val deviceAddress = intent.getStringExtra("deviceAddress")
        val uuidList = intent.getStringArrayListExtra("uuidList")

        device = bluetoothAdapter?.getRemoteDevice(deviceAddress)
        deviceUUID = UUID.fromString(uuidList?.firstOrNull())

        // Store device information
        deviceInfo = DeviceInfo(
            name = deviceName ?: "Unknown Device",
            address = device?.address ?: "Unknown Address",
            type = device?.type.toString(),
            uuids = uuidList ?: listOf("No UUIDs found")
        )

        setContent {
            AndroidSmartDeviceTheme {
                DeviceConnectionScreen(
                    deviceName = deviceInfo?.name ?: "Unknown Device",
                    deviceInfo = deviceInfo,
                    connectionStatus = connectionStatus,
                    onConnectClick = { connectToDevice() },
                    onDisconnectClick = { disconnectDevice() }
                )
            }
        }
    }

    private fun connectToDevice() {
        connectionStatus = "Connecting..."
        device?.let {
            // Use connectGatt to connect to the device
            bluetoothGatt = it.connectGatt(this, false, gattCallback)
        }
    }

    private fun disconnectDevice() {
        connectionStatus = "Disconnected"
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()

        // Return to the previous page after disconnecting
        finish()
    }

    private fun showToast(message: String) {
        // Show a Toast message for connection status
    }

    private fun navigateToLedControle() {
        // Navigate to LedControle activity when connected
        val intent = Intent(this, LedControle::class.java)
        startActivity(intent)
        finish() // Optionally, call finish() to close the current activity
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            when (newState) {
                BluetoothGatt.STATE_CONNECTED -> {
                    Log.d("Bluetooth", "Connected to GATT server.")
                    // Discover services after connection
                    gatt.discoverServices()
                    handler.sendMessage(handler.obtainMessage(CONNECTION_SUCCESS))
                }
                BluetoothGatt.STATE_DISCONNECTED -> {
                    Log.d("Bluetooth", "Disconnected from GATT server.")
                    handler.sendMessage(handler.obtainMessage(CONNECTION_FAILED))
                }
                else -> {
                    Log.d("Bluetooth", "Connection state changed: $newState")
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            super.onServicesDiscovered(gatt, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("Bluetooth", "Services discovered.")
                // You can interact with the device services here
            } else {
                Log.w("Bluetooth", "Service discovery failed with status $status")
            }
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            super.onCharacteristicRead(gatt, characteristic, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("Bluetooth", "Characteristic read: ${characteristic.value}")
                // Handle the read characteristic value
            }
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("Bluetooth", "Characteristic written: ${characteristic.value}")
                // Handle the written characteristic value
            }
        }
    }

    @Composable
    fun DeviceConnectionScreen(
        deviceName: String,
        deviceInfo: DeviceInfo?,
        connectionStatus: String,
        onConnectClick: () -> Unit,
        onDisconnectClick: () -> Unit
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Title with large text
            Text(
                text = "Connecting to $deviceName",
                style = TextStyle(fontSize = 24.sp, color = Color.Black),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Connection status with visual feedback
            Text(
                text = "Status: $connectionStatus",
                style = TextStyle(fontSize = 18.sp, color = when (connectionStatus) {
                    "Connecting..." -> Color.Yellow
                    "Connected" -> Color.Green
                    "Disconnected" -> Color.Red
                    else -> Color.Gray
                }),
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Device info
            deviceInfo?.let {
                Text("Device Name: ${it.name}", style = TextStyle(fontSize = 16.sp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Device Address: ${it.address}", style = TextStyle(fontSize = 16.sp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Device Type: ${it.type}", style = TextStyle(fontSize = 16.sp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("UUIDs: ${it.uuids.joinToString()}", style = TextStyle(fontSize = 16.sp))
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Connect and Disconnect Buttons with styling
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = onConnectClick,
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .height(48.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)
                ) {
                    Text("Connect", color = Color.White)
                }

                Button(
                    onClick = onDisconnectClick,
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .height(48.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) {
                    Text("Disconnect", color = Color.White)
                }
            }
        }
    }
}

// Data class to hold device information
data class DeviceInfo(
    val name: String,
    val address: String,
    val type: String,
    val uuids: List<String>
)
