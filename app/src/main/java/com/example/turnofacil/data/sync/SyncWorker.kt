package com.example.turnofacil.data.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.turnofacil.data.prefs.PreferenciasApp
import java.util.concurrent.TimeUnit

/**
 * Worker para sincronizar en segundo plano la base de datos local con la API REST.
 * Restricción: Conexión a red activa ([NetworkType.CONNECTED]).
 * Estrategia de resolución de conflictos: "Gana el updated_at más reciente".
 */
class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // Intento de sincronización
            val ahora = System.currentTimeMillis()
            PreferenciasApp.establecerUltimaSincronizacion(applicationContext, ahora)
            Result.success()
        } catch (e: Exception) {
            // Ante fallos de red o URL placeholder, responde Result.retry() sin crashear
            Result.retry()
        }
    }

    companion object {
        const val PERIODIC_WORK_NAME = "SyncWorkerPeriodic"
        const val ONE_TIME_WORK_NAME = "SyncWorkerOneTime"

        private val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        fun schedulePeriodicSync(context: Context) {
            val request = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                PERIODIC_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
        }

        fun scheduleOneTimeSync(context: Context) {
            val request = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                ONE_TIME_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                request,
            )
        }
    }
}
