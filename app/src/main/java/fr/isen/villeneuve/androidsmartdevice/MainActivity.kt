package fr.isen.villeneuve.androidsmartdevice

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import fr.isen.villeneuve.androidsmartdevice.ui.theme.AndroidSmartDeviceTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidSmartDeviceTheme {
                PresentationScreen(onStartClicked = {
                    checkPermissionsAndStart() // Vérifie les permissions et lance ListeActiviter si accordées
                })
            }
        }
    }

    private fun checkPermissionsAndStart() {
        // Vérification de la version Android et des permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val permissionsNeeded = mutableListOf<String>()

            // Vérifiez chaque permission et ajoutez-la à la liste si elle n'est pas encore accordée
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_SCAN)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_CONNECT)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION)
            }

            // Si des permissions sont manquantes, les demander
            if (permissionsNeeded.isNotEmpty()) {
                ActivityCompat.requestPermissions(
                    this,
                    permissionsNeeded.toTypedArray(),
                    1
                )
            } else {
                // Si toutes les permissions sont déjà accordées, lancez l'activité
                startListeActiviter()
            }
        } else {
            // Si la version est inférieure à Marshmallow, lancez directement l'activité
            startListeActiviter()
        }
    }

    private fun startListeActiviter() {
        val intent = Intent(this, ListeActiviter::class.java)
        startActivity(intent) // Lance l'activité ListeActiviter
    }

    // Gérer la réponse de la demande de permission
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // Si toutes les permissions sont accordées, lancez l'activité ListeActiviter
                startListeActiviter()
            } else {
                // Si des permissions sont refusées, affichez un message d'erreur
                Toast.makeText(this, "Les permissions sont nécessaires pour scanner les appareils Bluetooth", Toast.LENGTH_LONG).show()
            }
        }
    }
}

@Composable
fun PresentationScreen(onStartClicked: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .padding(16.dp)
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
        ) {
            Image(
                painter = painterResource(id = R.drawable.welcome),
                contentDescription = "Logo de l'application",
                modifier = Modifier.fillMaxSize()
            )
        }
        Text(
            text = "Android Smart Device",
            fontSize = 24.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Cette application vous permet de scanner les appareils Bluetooth Low Energy à proximité.",
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onStartClicked,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Magenta, // couleur de fond du bouton
                contentColor = Color.White // couleur du texte sur le bouton
            )
        ) {
            Text(text = "Commencer")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PresentationScreenPreview() {
    AndroidSmartDeviceTheme {
        PresentationScreen(onStartClicked = {})
    }
}
