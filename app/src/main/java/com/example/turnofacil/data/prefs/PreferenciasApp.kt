package com.example.turnofacil.data.prefs

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import java.util.Locale

/**
 * Preferencias simples de la aplicación (por ahora el idioma).
 *
 * El idioma se guarda como etiqueta: "sistema" | "es" | "en".
 * Se aplica envolviendo el Context de la Activity en [contextConIdioma]
 * (desde [MainActivity.attachBaseContext]) y llamando a `recreate()` al cambiarlo.
 */
object PreferenciasApp {

    const val IDIOMA_SISTEMA = "sistema"
    const val IDIOMA_ES = "es"
    const val IDIOMA_EN = "en"

    private const val PREFS = "turnofacil_prefs"
    private const val CLAVE_IDIOMA = "idioma"

    fun idioma(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(CLAVE_IDIOMA, IDIOMA_SISTEMA) ?: IDIOMA_SISTEMA

    fun establecerIdioma(context: Context, idioma: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(CLAVE_IDIOMA, idioma)
            .apply()
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
