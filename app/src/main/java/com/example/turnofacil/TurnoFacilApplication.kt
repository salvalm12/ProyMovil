package com.example.turnofacil

import android.app.Application
import com.example.turnofacil.data.AppContainer

class TurnoFacilApplication : Application() {

    val container: AppContainer by lazy { AppContainer(this) }
}
