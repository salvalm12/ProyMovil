package com.example.turnofacil.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.turnofacil.data.local.dao.AddonDao
import com.example.turnofacil.data.local.dao.AppointmentDao
import com.example.turnofacil.data.local.dao.MediaDao
import com.example.turnofacil.data.local.dao.NotificationDao
import com.example.turnofacil.data.local.dao.ServiceDao
import com.example.turnofacil.data.local.entity.AddonEntity
import com.example.turnofacil.data.local.entity.AppointmentAddonCrossRef
import com.example.turnofacil.data.local.entity.AppointmentEntity
import com.example.turnofacil.data.local.entity.MediaEntity
import com.example.turnofacil.data.local.entity.NotificationEntity
import com.example.turnofacil.data.local.entity.ServiceEntity

/**
 * Base de datos local de TurnoFácil (versión 1).
 *
 * No se crea ninguna instancia/singleton aquí: el `AppContainer` (Integrante 2)
 * es quien construye la base con `Room.databaseBuilder(...)`. Esto mantiene la
 * capa de datos testeable y evita estado global.
 */
@Database(
    entities = [
        ServiceEntity::class,
        AddonEntity::class,
        AppointmentEntity::class,
        AppointmentAddonCrossRef::class,
        NotificationEntity::class,
        MediaEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class TurnoDatabase : RoomDatabase() {

    abstract fun serviceDao(): ServiceDao

    abstract fun addonDao(): AddonDao

    abstract fun appointmentDao(): AppointmentDao

    abstract fun notificationDao(): NotificationDao

    abstract fun mediaDao(): MediaDao
}
