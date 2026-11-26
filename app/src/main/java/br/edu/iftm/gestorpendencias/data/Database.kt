package br.edu.iftm.gestorpendencias.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface PendenciaDao {
    @Query("SELECT * FROM pendencias WHERE userId = :uid")
    fun observarPendencias(uid: String): Flow<List<Pendencia>>

    @Query("SELECT * FROM pendencias WHERE id = :id")
    suspend fun buscar(id: Long): Pendencia?

    @Insert
    suspend fun inserir(p: Pendencia)

    @Update
    suspend fun atualizar(p: Pendencia)

    @Delete
    suspend fun excluir(p: Pendencia)

    @Query(
        "SELECT COUNT(*) FROM pendencias WHERE userId = :uid AND contexto = :ctx " +
            "AND status != 'CONCLUIDA' AND id != :ignorarId"
    )
    suspend fun contarAtivas(uid: String, ctx: Contexto, ignorarId: Long): Int

    @Query("SELECT * FROM capacidades WHERE userId = :uid")
    fun observarCapacidades(uid: String): Flow<List<CapacidadeContexto>>

    @Query("SELECT limite FROM capacidades WHERE userId = :uid AND contexto = :ctx")
    suspend fun buscarLimite(uid: String, ctx: Contexto): Int?

    @Upsert
    suspend fun salvarCapacidade(c: CapacidadeContexto)
}

@Database(
    entities = [Pendencia::class, CapacidadeContexto::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): PendenciaDao
}
