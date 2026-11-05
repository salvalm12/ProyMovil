package com.example.turnofacil.data.remote

import com.example.turnofacil.data.remote.dto.AppointmentDto
import com.example.turnofacil.data.remote.dto.ServiceDto
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Fuente de datos remota que interactúa con [TurnoApi].
 */
class RemoteDataSource(
    private val api: TurnoApi,
) {
    suspend fun fetchServices(): List<ServiceDto> {
        val response = api.getServices()
        if (response.isSuccessful) {
            return response.body() ?: emptyList()
        } else {
            throw Exception("Error fetching services: ${response.code()}")
        }
    }

    suspend fun syncService(service: ServiceDto): ServiceDto {
        val response = api.syncService(service)
        if (response.isSuccessful && response.body() != null) {
            return response.body()!!
        } else {
            throw Exception("Error syncing service: ${response.code()}")
        }
    }

    suspend fun fetchAppointments(since: Long? = null): List<AppointmentDto> {
        val response = api.getAppointments(since)
        if (response.isSuccessful) {
            return response.body() ?: emptyList()
        } else {
            throw Exception("Error fetching appointments: ${response.code()}")
        }
    }

    suspend fun syncAppointment(appointment: AppointmentDto): AppointmentDto {
        val response = api.syncAppointment(appointment)
        if (response.isSuccessful && response.body() != null) {
            return response.body()!!
        } else {
            throw Exception("Error syncing appointment: ${response.code()}")
        }
    }

    companion object {
        fun createOkHttpClient(isDebug: Boolean): OkHttpClient {
            val builder = OkHttpClient.Builder()
            if (isDebug) {
                val logging = HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
                builder.addInterceptor(logging)
            }
            return builder.build()
        }

        fun createTurnoApi(baseUrl: String, okHttpClient: OkHttpClient): TurnoApi {
            return Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(TurnoApi::class.java)
        }
    }
}
