package com.example.turnofacil.data.prefs

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import android.content.res.Configuration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.Locale

/**
 * Preferencias globales de la aplicación (idioma, horario de atención y sincronización).
 *
 * Mantiene compatibilidad total con el selector de idioma existente y expone
 * los valores configurables como [Flow] para la UI (Configuración, ViewModel).
 */
object PreferenciasApp {

    const val IDIOMA_SISTEMA = "sistema"
    const val IDIOMA_ES = "es"
    const val IDIOMA_EN = "en"

    private const val PREFS = "turnofacil_prefs"
    private const val CLAVE_IDIOMA = "idioma"
    private const val CLAVE_HORA_APERTURA = "hora_apertura"
    private const val CLAVE_HORA_CIERRE = "hora_cierre"
    private const val CLAVE_ULTIMA_SYNC = "ultima_sync"

    const val DEFAULT_HORA_APERTURA = "09:00"
    const val DEFAULT_HORA_CIERRE = "18:00"

    fun idioma(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(CLAVE_IDIOMA, IDIOMA_SISTEMA) ?: IDIOMA_SISTEMA

    fun establecerIdioma(context: Context, idioma: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(CLAVE_IDIOMA, idioma)
            .apply()
    }

    /** Observa los cambios de idioma como Flow. */
    fun observarIdioma(context: Context): Flow<String> = callbackFlow {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == CLAVE_IDIOMA) {
                trySend(idioma(context))
            }
        }
        trySend(idioma(context))
        prefs.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    fun horaApertura(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(CLAVE_HORA_APERTURA, DEFAULT_HORA_APERTURA) ?: DEFAULT_HORA_APERTURA

    fun horaCierre(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(CLAVE_HORA_CIERRE, DEFAULT_HORA_CIERRE) ?: DEFAULT_HORA_CIERRE

    fun establecerHorarioAtencion(context: Context, apertura: String, cierre: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(CLAVE_HORA_APERTURA, apertura)
            .putString(CLAVE_HORA_CIERRE, cierre)
            .apply()
    }

    /** Observa el horario de atención (Apertura, Cierre) como Flow. */
    fun observarHorarioAtencion(context: Context): Flow<Pair<String, String>> = callbackFlow {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == CLAVE_HORA_APERTURA || key == CLAVE_HORA_CIERRE) {
                trySend(Pair(horaApertura(context), horaCierre(context)))
            }
        }
        trySend(Pair(horaApertura(context), horaCierre(context)))
        prefs.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    fun ultimaSincronizacion(context: Context): Long =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getLong(CLAVE_ULTIMA_SYNC, 0L)

    fun establecerUltimaSincronizacion(context: Context, timestamp: Long) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putLong(CLAVE_ULTIMA_SYNC, timestamp)
            .apply()
    }

    /** Observa el timestamp de la última sincronización exitosa como Flow. */
    fun observarUltimaSincronizacion(context: Context): Flow<Long> = callbackFlow {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == CLAVE_ULTIMA_SYNC) {
                trySend(ultimaSincronizacion(context))
            }
        }
        trySend(ultimaSincronizacion(context))
        prefs.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    /** Devuelve un Context con el idioma guardado aplicado (o el original si es "sistema"). */
    fun contextConIdioma(base: Context): Context {
        val idioma = idioma(base)
        if (idioma == IDIOMA_SISTEMA) return base

        val locale = Locale(idioma)
        Locale.setDefault(locale)
        val config = Configuration(base.resources.configuration)
        config.setLocale(locale)
        return base.createConfigurationContext(config)
    }
}

/** Obtiene la Activity a la que pertenece un Context (para poder llamar a recreate()). */
fun Context.buscarActivity(): Activity? {
    var actual: Context? = this
    while (actual is ContextWrapper) {
        if (actual is Activity) return actual
        actual = actual.baseContext
    }
    return null
}
