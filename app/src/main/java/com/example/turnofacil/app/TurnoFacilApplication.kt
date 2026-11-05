package com.example.turnofacil.app

import android.app.Application
import com.example.turnofacil.data.sync.SyncWorker

/**
 * Clase [Application] principal de TurnoFácil.
 * Crea e inicializa el [AppContainer] global y programa la sincronización en segundo plano.
 */
class TurnoFacilApplication : Application() {

    val container: AppContainer by lazy {
        DefaultAppContainer.init(this)
        DefaultAppContainer(this)
    }

    override fun onCreate() {
        super.onCreate()
        SyncWorker.schedulePeriodicSync(this)
        SyncWorker.scheduleOneTimeSync(this)
    }
}
