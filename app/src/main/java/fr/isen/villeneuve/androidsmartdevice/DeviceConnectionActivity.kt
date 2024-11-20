package fr.isen.villeneuve.androidsmartdevice

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothProfile
import android.content.Intent
import android.os.Bundle
import android.util.Log
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

class DeviceConnectionActivity : ComponentActivity() {
    private var isConnected by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val deviceName = intent.getStringExtra("deviceName")
        val deviceAddress = intent.getStringExtra("deviceAddress")

        setContent {
            DeviceScreen(deviceName = deviceName, deviceAddress = deviceAddress)
        }
    }

    // Connexion au périphérique Bluetooth
    private fun connectToDevice(deviceAddress: String?) {
        val bluetoothDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress)
        BluetoothManager.bluetoothGatt = bluetoothDevice.connectGatt(this, false, object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.d("BLE", "Connected to GATT server.")
                    gatt.discoverServices()
                    BluetoothManager.isConnected = true
                    isConnected = true
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.d("BLE", "Disconnected from GATT server.")
                    BluetoothManager.isConnected = false
                    isConnected = false
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    BluetoothManager.ledCharacteristic = gatt.services[2]?.characteristics?.get(0)
                    Log.d("BLE", "Services discovered.")
                }
            }
        })
    }

    // Déconnexion du périphérique Bluetooth et retour à la page précédente
    private fun disconnectFromDevice() {
        BluetoothManager.bluetoothGatt?.disconnect()
        BluetoothManager.bluetoothGatt?.close()
        BluetoothManager.bluetoothGatt = null
        BluetoothManager.isConnected = false
        isConnected = false
        Log.d("BLE", "Disconnected from GATT server.")

        // Retourner à la page précédente
        finish()  // Cela ferme l'activité en cours et retourne à la précédente
    }

    @Composable
    fun DeviceScreen(deviceName: String?, deviceAddress: String?) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Connect to Device", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Device Name: ${deviceName ?: "Unknown"}", fontSize = 18.sp)
            Text("Device Address: ${deviceAddress ?: "Unknown"}", fontSize = 18.sp)
            Spacer(modifier = Modifier.height(32.dp))

            // Bouton de connexion
            Button(
                onClick = { connectToDevice(deviceAddress) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF00FF)),
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Text(if (isConnected) "Connected" else "Connect")
            }

            // Bouton pour accéder au contrôle des LED si connecté
            if (isConnected) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        startActivity(
                            Intent(this@DeviceConnectionActivity, LedControlActivity::class.java)
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FF00)),
                    modifier = Modifier.fillMaxWidth().padding(8.dp)
                ) {
                    Text("Go to LED Control")
                }

                // Bouton de déconnexion
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { disconnectFromDevice() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    modifier = Modifier.fillMaxWidth().padding(8.dp)
                ) {
                    Text("Disconnect from Device")
                }
            }
        }
    }
}
