package fr.isen.villeneuve.androidsmartdevice

import android.bluetooth.BluetoothGatt
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import fr.isen.villeneuve.androidsmartdevice.ui.theme.AndroidSmartDeviceTheme

class LedControle : ComponentActivity() {

    private var bluetoothGatt: BluetoothGatt? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Récupérer l'objet BluetoothGatt passé par l'intent
        bluetoothGatt = intent.getParcelableExtra("bluetoothGatt")

        setContent {
            AndroidSmartDeviceTheme {
                ControlPanel(
                    bluetoothGatt = bluetoothGatt,
                    onDisconnectClick = { disconnect() }
                )
            }
        }
    }

    // Fonction pour déconnecter le Bluetooth
    private fun disconnect() {
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        finish() // Retour à la page précédente
    }
}

@Composable
fun ControlPanel(
    bluetoothGatt: BluetoothGatt?,
    onDisconnectClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Vérifie si BluetoothGatt est null ou non pour déterminer la connexion
        val connectionStatus = if (bluetoothGatt != null) "Connected" else "Not Connected"

        // Affichage du statut de la connexion
        Text(
            text = "Status: $connectionStatus",
            style = TextStyle(fontSize = 18.sp),
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Autres boutons pour contrôler les LEDs
        Button(onClick = { /* Code pour contrôler la LED */ }) {
            Text("Turn LED On")
        }

        Button(onClick = { /* Code pour contrôler la LED */ }) {
            Text("Turn LED Off")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onDisconnectClick) {
            Text("Disconnect")
        }
    }
}
