package com.example.turnofacil.data.local

import androidx.room.TypeConverter
import com.example.turnofacil.domain.model.EstadoCita
import com.example.turnofacil.domain.model.SyncState
import com.example.turnofacil.domain.model.TipoAtencion
import com.example.turnofacil.domain.model.TipoNotificacion
import com.example.turnofacil.domain.model.TipoPropietarioMedia
import java.time.LocalDate
import java.time.LocalTime

/**
 * Conversores de Room para los tipos de dominio.
 *
 * Las fechas se guardan en ISO-8601 (`yyyy-MM-dd`) y las horas en ISO-8601
 * (`HH:mm`), de modo que el orden lexicográfico coincide con el orden temporal.
 * Los enums se guardan por su `name`, lo que mantiene el esquema legible.
 */
class Converters {

    @TypeConverter
    fun fromLocalDate(value: LocalDate?): String? = value?.toString()

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? = value?.let(LocalDate::parse)

    @TypeConverter
    fun fromLocalTime(value: LocalTime?): String? = value?.toString()

    @TypeConverter
    fun toLocalTime(value: String?): LocalTime? = value?.let(LocalTime::parse)

    @TypeConverter
    fun fromEstadoCita(value: EstadoCita?): String? = value?.name

    @TypeConverter
    fun toEstadoCita(value: String?): EstadoCita? = value?.let(EstadoCita::valueOf)

    @TypeConverter
    fun fromSyncState(value: SyncState?): String? = value?.name

    @TypeConverter
    fun toSyncState(value: String?): SyncState? = value?.let(SyncState::valueOf)

    @TypeConverter
    fun fromTipoAtencion(value: TipoAtencion?): String? = value?.name

    @TypeConverter
    fun toTipoAtencion(value: String?): TipoAtencion? = value?.let(TipoAtencion::valueOf)

    @TypeConverter
    fun fromTipoNotificacion(value: TipoNotificacion?): String? = value?.name

    @TypeConverter
    fun toTipoNotificacion(value: String?): TipoNotificacion? =
        value?.let(TipoNotificacion::valueOf)

    @TypeConverter
    fun fromTipoPropietarioMedia(value: TipoPropietarioMedia?): String? = value?.name

    @TypeConverter
    fun toTipoPropietarioMedia(value: String?): TipoPropietarioMedia? =
        value?.let(TipoPropietarioMedia::valueOf)
}
