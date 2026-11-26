package br.edu.iftm.gestorpendencias.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import br.edu.iftm.gestorpendencias.PendenciaApp
import br.edu.iftm.gestorpendencias.ui.auth.AuthScreen
import br.edu.iftm.gestorpendencias.ui.auth.AuthViewModel
import br.edu.iftm.gestorpendencias.ui.form.FormScreen
import br.edu.iftm.gestorpendencias.ui.form.FormViewModel
import br.edu.iftm.gestorpendencias.ui.home.HomeScreen
import br.edu.iftm.gestorpendencias.ui.home.HomeViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AppNav() {
    val nav = rememberNavController()
    val repo = (LocalContext.current.applicationContext as PendenciaApp).repository
    val auth = FirebaseAuth.getInstance()
    val inicio = if (auth.currentUser != null) "home" else "auth"

    NavHost(navController = nav, startDestination = inicio) {
        composable("auth") {
            val vm: AuthViewModel = viewModel()
            AuthScreen(vm) {
                nav.navigate("home") { popUpTo("auth") { inclusive = true } }
            }
        }
        composable("home") {
            val uid = auth.currentUser?.uid
            if (uid == null) {
                Box {}
            } else {
                val vm: HomeViewModel = viewModel(key = "home_$uid", factory = HomeViewModel.factory(repo, uid))
                HomeScreen(
                    vm = vm,
                    onNova = { nav.navigate("form?id=0") },
                    onEditar = { nav.navigate("form?id=$it") },
                    onSair = {
                        auth.signOut()
                        nav.navigate("auth") { popUpTo("home") { inclusive = true } }
                    }
                )
            }
        }
        composable(
            route = "form?id={id}",
            arguments = listOf(navArgument("id") { type = NavType.LongType; defaultValue = 0L })
        ) { entry ->
            val id = entry.arguments?.getLong("id") ?: 0L
            val uid = auth.currentUser?.uid ?: return@composable
            val vm: FormViewModel = viewModel(key = "form_${uid}_$id", factory = FormViewModel.factory(repo, uid, id))
            FormScreen(vm) { nav.popBackStack() }
        }
    }
}
