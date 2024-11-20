package fr.isen.villeneuve.androidsmartdevice

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.isen.villeneuve.androidsmartdevice.ui.theme.AndroidSmartDeviceTheme
import java.util.*

class LedControle : ComponentActivity() {

    private var bluetoothGatt: BluetoothGatt? = null
    private var ledCharacteristic: BluetoothGattCharacteristic? = null
    private var clickCountCharacteristic1: BluetoothGattCharacteristic? = null
    private var clickCountCharacteristic2: BluetoothGattCharacteristic? = null

    private var status: String? = null
    private var serviceUuid: UUID? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Récupération des données passées par DeviceConnectionActivity
        bluetoothGatt = intent.getParcelableExtra("bluetoothGatt")
        status = intent.getStringExtra("connectionStatus")
        serviceUuid = UUID.fromString(intent.getStringExtra("serviceUuid"))

        setContent {
            AndroidSmartDeviceTheme {
                ControlPanel(
                    connectionStatus = status ?: "Unknown",
                    onLedOn = { toggleLed(true) },
                    onLedOff = { toggleLed(false) },
                    onSubscribeClickCount = { subscribeToNotifications() },
                    onDisconnectClick = { disconnect() }
                )
            }
        }
    }

    private fun toggleLed(turnOn: Boolean) {
        val command = if (turnOn) byteArrayOf(0x01) else byteArrayOf(0x00)
        ledCharacteristic?.let {
            it.value = command
            bluetoothGatt?.writeCharacteristic(it)
        } ?: Log.e("LED", "LED characteristic not initialized")
    }

    private fun subscribeToNotifications() {
        bluetoothGatt?.let { gatt ->
            clickCountCharacteristic1?.let { characteristic ->
                enableNotifications(gatt, characteristic)
            }
            clickCountCharacteristic2?.let { characteristic ->
                enableNotifications(gatt, characteristic)
            }
        } ?: Log.e("Bluetooth", "BluetoothGatt not initialized")
    }

    private fun enableNotifications(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
        gatt.setCharacteristicNotification(characteristic, true)
        val descriptor = characteristic.getDescriptor(UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG))
        descriptor?.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
        gatt.writeDescriptor(descriptor)
    }

    private fun disconnect() {
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        finish()
    }

    companion object {
        const val CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb"
    }
}

@Composable
fun ControlPanel(
    connectionStatus: String,
    onLedOn: () -> Unit,
    onLedOff: () -> Unit,
    onSubscribeClickCount: () -> Unit,
    onDisconnectClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Affichage du statut de connexion
        Text(text = "Status: $connectionStatus", fontSize = 18.sp, modifier = Modifier.padding(bottom = 16.dp))

        // Boutons pour contrôler la LED
        Button(onClick = onLedOn, modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Text("Turn LED On")
        }
        Button(onClick = onLedOff, modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Text("Turn LED Off")
        }

        // Bouton pour s'abonner aux notifications
        Button(onClick = onSubscribeClickCount, modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Text("Subscribe to Button Click Notifications")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bouton pour se déconnecter
        Button(onClick = onDisconnectClick, modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Text("Disconnect")
        }
    }
}
