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
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
                connectionStatus = "Connected"
                showToast("Connection successful")
                navigateToLedControle()
            }
            CONNECTION_FAILED -> {
                connectionStatus = "Connection Failed"
                showToast("Connection failed")
            }
            else -> Unit
        }
        true
    })

    companion object {
        const val CONNECTION_SUCCESS = 1
        const val CONNECTION_FAILED = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Récupération des données envoyées depuis l'activité précédente
        val deviceName = intent.getStringExtra("deviceName")
        val deviceAddress = intent.getStringExtra("deviceAddress")
        val uuidList = intent.getStringArrayListExtra("uuidList")

        device = bluetoothAdapter?.getRemoteDevice(deviceAddress)
        deviceUUID = uuidList?.firstOrNull()?.let { UUID.fromString(it) }

        deviceInfo = DeviceInfo(
            name = deviceName ?: "Unknown Device",
            address = device?.address ?: "Unknown Address",
            type = when (device?.type) {
                BluetoothDevice.DEVICE_TYPE_CLASSIC -> "Classic"
                BluetoothDevice.DEVICE_TYPE_LE -> "Low Energy (LE)"
                BluetoothDevice.DEVICE_TYPE_DUAL -> "Dual Mode"
                else -> "Unknown"
            },
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
        if (device == null || deviceUUID == null) {
            showToast("Invalid device or UUID")
            return
        }
        connectionStatus = "Connecting..."
        bluetoothGatt = device?.connectGatt(this, false, gattCallback)
    }

    private fun disconnectDevice() {
        connectionStatus = "Disconnected"
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun navigateToLedControle() {
        val intent = Intent(this, LedControle::class.java)
        startActivity(intent)
        finish()
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            when (newState) {
                BluetoothGatt.STATE_CONNECTED -> {
                    Log.d("Bluetooth", "Connected to GATT server.")
                    gatt.discoverServices()
                    handler.sendMessage(handler.obtainMessage(CONNECTION_SUCCESS))
                }
                BluetoothGatt.STATE_DISCONNECTED -> {
                    Log.d("Bluetooth", "Disconnected from GATT server.")
                    handler.sendMessage(handler.obtainMessage(CONNECTION_FAILED))
                }
                else -> Log.d("Bluetooth", "Connection state changed: $newState")
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("Bluetooth", "Services discovered.")
            } else {
                Log.w("Bluetooth", "Service discovery failed with status $status")
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
            Text(
                text = "Connecting to $deviceName",
                style = TextStyle(fontSize = 24.sp, color = Color.Black),
                modifier = Modifier.padding(bottom = 16.dp)
            )
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
            deviceInfo?.let {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text("Name: ${it.name}", style = TextStyle(fontSize = 16.sp))
                    Text("Address: ${it.address}", style = TextStyle(fontSize = 16.sp))
                    Text("Type: ${it.type}", style = TextStyle(fontSize = 16.sp))
                    Text("UUIDs: ${it.uuids.joinToString()}", style = TextStyle(fontSize = 16.sp))
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(onClick = onConnectClick) {
                    Text("Connect")
                }
                Button(onClick = onDisconnectClick) {
                    Text("Disconnect")
                }
            }
        }
    }
}

data class DeviceInfo(
    val name: String,
    val address: String,
    val type: String,
    val uuids: List<String>
)
