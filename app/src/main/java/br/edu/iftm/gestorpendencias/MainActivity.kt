package br.edu.iftm.gestorpendencias

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import br.edu.iftm.gestorpendencias.ui.AppNav
import br.edu.iftm.gestorpendencias.ui.theme.PendenciasTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { PendenciasTheme { AppNav() } }
    }
}
