package com.example.turnofacil.app

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * CompositionLocal para exponer [AppContainer] a todo el árbol de Compose.
 */
val LocalAppContainer = staticCompositionLocalOf<AppContainer> {
    error("No se ha provisto un AppContainer en LocalAppContainer")
}
