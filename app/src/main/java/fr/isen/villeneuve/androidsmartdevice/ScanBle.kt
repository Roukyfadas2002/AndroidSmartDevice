package fr.isen.villeneuve.androidsmartdevice

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import fr.isen.villeneuve.androidsmartdevice.ui.theme.AndroidSmartDeviceTheme

class ScanBle : ComponentActivity() {
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val bluetoothLeScanner: BluetoothLeScanner? = bluetoothAdapter?.bluetoothLeScanner
    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidSmartDeviceTheme {
                ScanBleScreen(bluetoothAdapter, bluetoothLeScanner, handler, onBackPressed = { finish() })
            }
        }
    }
}

@Composable
fun ScanBleScreen(
    bluetoothAdapter: BluetoothAdapter?,
    bluetoothLeScanner: BluetoothLeScanner?,
    handler: Handler,
    onBackPressed: () -> Unit,
) {
    var isScanning by remember { mutableStateOf(false) }
    var detectedDevices = remember { mutableStateListOf<ScanResult>() }
    var showBluetoothDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val permissionsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.BLUETOOTH] == true &&
                permissions[Manifest.permission.BLUETOOTH_ADMIN] == true &&
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        if (granted && bluetoothAdapter?.isEnabled == true) {
            startBleScan(bluetoothLeScanner, detectedDevices)
        } else {
            showBluetoothDialog = true
        }
    }

    fun onScanButtonClick() {
        if (bluetoothAdapter?.isEnabled == true) {
            isScanning = !isScanning
            if (isScanning) {
                startBleScan(bluetoothLeScanner, detectedDevices)
            } else {
                stopBleScan(bluetoothLeScanner)
            }
        } else {
            showBluetoothDialog = true
        }
    }

    val infiniteTransition = rememberInfiniteTransition()
    val animatedSize by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isScanning) 1.2f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    if (showBluetoothDialog) {
        BluetoothAlertDialog(
            onDismiss = { showBluetoothDialog = false },
            onEnableBluetooth = {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                context.startActivity(enableBtIntent)
                showBluetoothDialog = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Bouton retour
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Retour",
                modifier = Modifier
                    .size(32.dp)
                    .clickable { onBackPressed() }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Scan BLE", fontSize = 24.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(150.dp)
                .padding(16.dp)
                .graphicsLayer(scaleX = animatedSize, scaleY = animatedSize)
        ) {
            Button(
                onClick = { onScanButtonClick() },
                modifier = Modifier.size(150.dp)
            ) {
                Image(
                    painter = painterResource(
                        id = if (isScanning) R.drawable.stop else R.drawable.start
                    ),
                    contentDescription = if (isScanning) "Arrêter le scan" else "Démarrer le scan",
                    modifier = Modifier.size(50.dp)
                )
            }
        }

        Text(
            text = if (isScanning) "Scan en cours..." else "Appuyez pour scanner",
            fontSize = 16.sp,
            color = if (isScanning) Color.Blue else Color.Gray
        )

        Spacer(modifier = Modifier.height(32.dp))

        LazyColumn {
            items(detectedDevices) { result ->
                val signalStrength = result.rssi
                val deviceName = result.device.name ?: "Appareil inconnu"
                val deviceAddress = result.device.address
                val signalColor = when {
                    signalStrength > -50 -> Color.Green
                    signalStrength > -70 -> Color.Yellow
                    else -> Color.Red
                }

                // Récupérer les UUIDs de l'appareil
                val uuidList = getDeviceUuid(result)

                DeviceItem(
                    deviceName = deviceName,
                    deviceAddress = deviceAddress,
                    signalStrength = signalStrength,
                    signalColor = signalColor,
                    uuidList = uuidList,
                    onClick = {
                        val intent = Intent(context, DeviceConnectionActivity::class.java)
                        intent.putExtra("deviceName", deviceName)
                        intent.putExtra("deviceAddress", deviceAddress)
                        intent.putStringArrayListExtra("uuidList", ArrayList(uuidList))
                        context.startActivity(intent)
                    }
                )
            }
        }
    }
}

@Composable
fun DeviceItem(
    deviceName: String,
    deviceAddress: String,
    signalStrength: Int,
    signalColor: Color,
    uuidList: List<String>,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() }
            .background(signalColor.copy(alpha = 0.2f))
            .padding(16.dp)
    ) {
        Text(
            text = "Nom : $deviceName",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(text = "Adresse : $deviceAddress", fontSize = 14.sp, color = Color.Gray)
        Text(text = "Signal : $signalStrength dBm", fontSize = 14.sp, color = Color.Gray)

        // Affichage des UUIDs
        if (uuidList.isNotEmpty()) {
            Text(
                text = "UUID(s) : ${uuidList.joinToString(", ")}",
                fontSize = 14.sp,
                color = Color.Gray
            )
        } else {
            Text(
                text = "Aucun UUID trouvé",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}

@SuppressLint("MissingPermission")
fun startBleScan(bluetoothLeScanner: BluetoothLeScanner?, detectedDevices: MutableList<ScanResult>) {
    bluetoothLeScanner?.startScan(object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            if (result.device.name != null && detectedDevices.none { it.device.address == result.device.address }) {
                detectedDevices.add(result)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e("BLE", "Scan failed with error code $errorCode")
        }
    })
}

fun stopBleScan(bluetoothLeScanner: BluetoothLeScanner?) {
    bluetoothLeScanner?.stopScan(object : ScanCallback() {})
}

@SuppressLint("MissingPermission")
fun getDeviceUuid(scanResult: ScanResult): List<String> {
    // Extraire les UUID des services de l'appareil
    val uuids = scanResult.scanRecord?.serviceUuids
    return uuids?.map { it.toString() } ?: emptyList()
}

@Composable
fun BluetoothAlertDialog(
    onDismiss: () -> Unit,
    onEnableBluetooth: () -> Unit
) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(color = Color.White)
        ) {
            Text(text = "Le Bluetooth est désactivé. Souhaitez-vous l'activer ?", fontSize = 18.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(onClick = onDismiss) {
                    Text(text = "Annuler")
                }
                Button(onClick = onEnableBluetooth) {
                    Text(text = "Activer")
                }
            }
        }
    }
}
