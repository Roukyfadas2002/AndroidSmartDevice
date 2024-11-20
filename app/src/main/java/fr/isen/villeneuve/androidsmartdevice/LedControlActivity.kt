package fr.isen.villeneuve.androidsmartdevice

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class LedControlActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LedControlScreen()
        }
    }

    private fun writeToLEDCharacteristic(state: LEDStateEnum) {
        BluetoothManager.ledCharacteristic?.let { characteristic ->
            characteristic.value = state.hex
            BluetoothManager.bluetoothGatt?.writeCharacteristic(characteristic)
            Log.d("BLE", "LED state set to: ${state.name}")
        } ?: Log.e("BLE", "LED characteristic not found.")
    }

    @Composable
    fun LedControlScreen() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("LED Control", fontSize = 24.sp)
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { writeToLEDCharacteristic(LEDStateEnum.LED_1) },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Blue),
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Text("Turn On LED 1")
            }

            Button(
                onClick = { writeToLEDCharacteristic(LEDStateEnum.LED_2) },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Green),
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Text("Turn On LED 2")
            }

            Button(
                onClick = { writeToLEDCharacteristic(LEDStateEnum.LED_3) },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Text("Turn On LED 3")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Nouveau bouton pour éteindre les LED
            Button(
                onClick = { writeToLEDCharacteristic(LEDStateEnum.OFF) },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Text("Turn Off LED")
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { finish() },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Text("Back")
            }
        }
    }
}
