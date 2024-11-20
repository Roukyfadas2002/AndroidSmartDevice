package fr.isen.villeneuve.androidsmartdevice

import android.annotation.SuppressLint
import android.bluetooth.*
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
import java.util.UUID

class DeviceConnectionActivity : ComponentActivity() {
    private var isConnected by mutableStateOf(false)
    private var notificationCount by mutableStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val deviceName = intent.getStringExtra("deviceName")
        val deviceAddress = intent.getStringExtra("deviceAddress")

        setContent {
            DeviceScreen(deviceName = deviceName, deviceAddress = deviceAddress)
        }
    }

    // Connexion au périphérique Bluetooth
    @SuppressLint("MissingPermission")
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
                    // Accéder à la caractéristique LED du service 3
                    val ledCharacteristic = gatt.services[2]?.characteristics?.get(0)
                    if (ledCharacteristic != null) {
                        BluetoothManager.ledCharacteristic = ledCharacteristic
                        Log.d("BLE", "LED characteristic found.")
                    } else {
                        Log.d("BLE", "LED characteristic not found.")
                    }

                    // Abonnement à la caractéristique 2 du service 3 pour les notifications
                    val notificationCharacteristic = gatt.services[2]?.characteristics?.get(1)
                    if (notificationCharacteristic != null) {
                        gatt.setCharacteristicNotification(notificationCharacteristic, true)
                        val descriptor = notificationCharacteristic.getDescriptor(
                            BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                        )
                        descriptor?.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                        gatt.writeDescriptor(descriptor)
                        Log.d("BLE", "Subscribed to notifications for characteristic 2 of service 3.")
                    }
                }
            }

            override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
                super.onCharacteristicChanged(gatt, characteristic)
                // Incrémente le compteur à chaque notification
                notificationCount++
                Log.d("BLE", "Notification received. Count: $notificationCount")
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
        finish() // Cela ferme l'activité en cours et retourne à la précédente
    }

    // Commande pour allumer la LED
    private fun turnOnLed() {
        val ledCharacteristic = BluetoothManager.ledCharacteristic
        ledCharacteristic?.let {
            it.setValue(byteArrayOf(0x01))  // Exemple de valeur pour allumer la LED
            BluetoothManager.bluetoothGatt?.writeCharacteristic(it)
            Log.d("BLE", "LED turned ON")
        }
    }

    // Commande pour éteindre la LED
    private fun turnOffLed() {
        val ledCharacteristic = BluetoothManager.ledCharacteristic
        ledCharacteristic?.let {
            it.setValue(byteArrayOf(0x00))  // Exemple de valeur pour éteindre la LED
            BluetoothManager.bluetoothGatt?.writeCharacteristic(it)
            Log.d("BLE", "LED turned OFF")
        }
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

            // Affiche le compteur de notifications
            if (isConnected) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Notification Count: $notificationCount", fontSize = 18.sp, fontWeight = FontWeight.Bold)

                // Bouton pour accéder au contrôle des LED si connecté
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

private fun BluetoothGattCharacteristic?.getDescriptor(enableNotificationValue: ByteArray?): BluetoothGattDescriptor? {
    if (this == null) return null

    // Récupère le Client Characteristic Configuration Descriptor (CCCD)
    val descriptor = this.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
    if (descriptor != null) {
        descriptor.value = enableNotificationValue
    }
    return descriptor
}
