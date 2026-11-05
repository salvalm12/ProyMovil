package com.example.turnofacil.data

import android.content.Context
import androidx.room.Room
import com.example.turnofacil.data.local.TurnoDatabase
import com.example.turnofacil.data.repository.CitaRepositorioRoom
import com.example.turnofacil.data.repository.RoomSeeder
import com.example.turnofacil.data.repository.ServicioRepositorioRoom

/**
 * Contenedor de dependencias de la aplicación.
 *
 * Mantiene una sola instancia de Room y de los repositorios durante toda la
 * vida del proceso. La UI no construye la base directamente.
 */
class AppContainer(context: Context) {

    val database: TurnoDatabase = Room.databaseBuilder(
        context.applicationContext,
        TurnoDatabase::class.java,
        "turnofacil.db",
    ).build()

    val servicioRepositorio: ServicioRepositorioRoom = ServicioRepositorioRoom(database)

    val citaRepositorio: CitaRepositorioRoom = CitaRepositorioRoom(database)

    val roomSeeder: RoomSeeder = RoomSeeder(database)
}
