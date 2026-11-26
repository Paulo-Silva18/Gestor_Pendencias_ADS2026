package br.edu.iftm.gestorpendencias

import android.app.Application
import androidx.room.Room
import br.edu.iftm.gestorpendencias.data.AppDatabase
import br.edu.iftm.gestorpendencias.data.PendenciaRepository

class PendenciaApp : Application() {
    private val db by lazy {
        Room.databaseBuilder(this, AppDatabase::class.java, "pendencias.db").build()
    }
    val repository by lazy { PendenciaRepository(db.dao()) }
}
