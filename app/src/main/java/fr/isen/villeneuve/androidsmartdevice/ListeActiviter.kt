package fr.isen.villeneuve.androidsmartdevice

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.isen.villeneuve.androidsmartdevice.ui.theme.AndroidSmartDeviceTheme

// Classe `ListeActiviter` qui hérite de `ComponentActivity` pour afficher la liste des activités
class ListeActiviter : ComponentActivity() {
    // Méthode appelée lors de la création de l'activité
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Définit le contenu de l'interface utilisateur en utilisant Jetpack Compose
        setContent {
            // Applique le thème AndroidSmartDeviceTheme pour homogénéiser l'apparence
            AndroidSmartDeviceTheme {
                // Utilisation de Scaffold pour structurer l'interface avec un remplissage complet de l'écran
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Appelle la fonction composable `ActivitiesListScreen` pour afficher la liste des activités
                    ActivitiesListScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

// Fonction Composable pour afficher l'écran avec une liste des activités
@Composable
fun ActivitiesListScreen(modifier: Modifier = Modifier) {
    // Définition de la liste des activités
    val activities = listOf("Scan BLE", "Test", "Test", "Test")

    // Utilise une colonne pour empiler verticalement le texte de titre et la liste des boutons
    Column(
        modifier = modifier
            .fillMaxSize() // Prend tout l'espace de l'écran
            .padding(16.dp), // Ajoute une marge intérieure de 16 dp
        horizontalAlignment = Alignment.CenterHorizontally, // Aligne le contenu au centre horizontalement
        verticalArrangement = Arrangement.Top // Aligne le contenu verticalement vers le haut
    ) {
        // Texte de titre pour la liste des activités
        Text(
            text = "Liste des activités",
            fontSize = 24.sp, // Taille de police de 24 sp
            modifier = Modifier.padding(bottom = 16.dp) // Ajoute un espacement en dessous du titre
        )

        // Utilise un LazyColumn pour afficher les éléments de la liste de manière performante
        LazyColumn {
            // Crée un élément pour chaque activité dans la liste `activities`
            items(activities.size) { index ->
                ActivityButton(activityName = activities[index]) // Affiche un bouton pour chaque activité
            }
        }
    }
}

// Fonction Composable pour créer un bouton d'activité avec la couleur magenta
@Composable
fun ActivityButton(activityName: String) {
    val context = LocalContext.current

    Button(
        onClick = {
            if (activityName == "Scan BLE") {
                // Navigue vers l'activité ScanBle pour le scan BLE
                val intent = Intent(context, ScanBle::class.java)
                context.startActivity(intent)
            }
            // Autres actions pour les autres activités si nécessaire
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF00FF)) // Magenta
    ) {
        Text(text = activityName, fontSize = 18.sp)
    }
}

// Fonction de prévisualisation de l'écran de la liste des activités dans l'éditeur
@Preview(showBackground = true)
@Composable
fun ActivitiesListScreenPreview() {
    // Affiche la liste des activités dans un thème AndroidSmartDevice
    AndroidSmartDeviceTheme {
        ActivitiesListScreen()
    }
}
