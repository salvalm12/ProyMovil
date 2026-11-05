package com.example.turnofacil.app

import android.content.Context
import androidx.room.Room
import com.example.turnofacil.BuildConfig
import com.example.turnofacil.data.local.TurnoDatabase
import com.example.turnofacil.data.remote.RemoteDataSource
import com.example.turnofacil.data.remote.TurnoApi
import com.example.turnofacil.data.repository.AppointmentRepository
import com.example.turnofacil.data.repository.AppointmentRepositoryImpl
import com.example.turnofacil.data.repository.CitaRepositorioRoom
import com.example.turnofacil.data.repository.LocalDataSource
import com.example.turnofacil.data.repository.RoomSeeder
import com.example.turnofacil.data.repository.ServiceRepository
import com.example.turnofacil.data.repository.ServiceRepositoryImpl
import com.example.turnofacil.data.repository.ServicioRepositorioRoom
import com.example.turnofacil.ui.viewmodel.CitaRepositorio
import com.example.turnofacil.ui.viewmodel.ServicioRepositorio

/**
 * Contenedor principal de Inyección de Dependencias Manual de la aplicación.
 */
interface AppContainer {
    val serviceRepository: ServiceRepository
    val appointmentRepository: AppointmentRepository

    val servicioRepositorio: ServicioRepositorio
    val citaRepositorio: CitaRepositorio
    val roomSeeder: RoomSeeder
}

/**
 * Implementación predeterminada de [AppContainer] que instancia los singletons de forma lazy.
 */
class DefaultAppContainer(private val context: Context = getApplicationContextPlaceholder()) : AppContainer {

    val database: TurnoDatabase by lazy {
        Room.databaseBuilder(
            context.applicationContext,
            TurnoDatabase::class.java,
            "turnofacil.db",
        ).fallbackToDestructiveMigration().build()
    }

    val localDataSource: LocalDataSource by lazy {
        LocalDataSource(database)
    }

    val turnoApi: TurnoApi by lazy {
        val okHttpClient = RemoteDataSource.createOkHttpClient(BuildConfig.DEBUG)
        RemoteDataSource.createTurnoApi(BuildConfig.BASE_URL, okHttpClient)
    }

    val remoteDataSource: RemoteDataSource by lazy {
        RemoteDataSource(turnoApi)
    }

    override val serviceRepository: ServiceRepository by lazy {
        ServiceRepositoryImpl(localDataSource)
    }

    override val appointmentRepository: AppointmentRepository by lazy {
        AppointmentRepositoryImpl(localDataSource)
    }

    override val servicioRepositorio: ServicioRepositorio by lazy {
        ServicioRepositorioRoom(database)
    }

    override val citaRepositorio: CitaRepositorio by lazy {
        CitaRepositorioRoom(database)
    }

    override val roomSeeder: RoomSeeder by lazy {
        RoomSeeder(database)
    }

    companion object {
        private var globalContext: Context? = null

        fun init(context: Context) {
            globalContext = context.applicationContext
        }

        private fun getApplicationContextPlaceholder(): Context {
            return globalContext ?: throw IllegalStateException("AppContainer no ha sido inicializado con un Context")
        }
    }
}
