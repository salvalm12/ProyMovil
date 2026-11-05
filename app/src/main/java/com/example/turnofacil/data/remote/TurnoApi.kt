package com.example.turnofacil.data.remote

import com.example.turnofacil.data.remote.dto.AppointmentDto
import com.example.turnofacil.data.remote.dto.ServiceDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Firma de endpoints de Retrofit para TurnoFácil API REST (solo HTTPS).
 */
interface TurnoApi {

    @GET("services")
    suspend fun getServices(): Response<List<ServiceDto>>

    @POST("services")
    suspend fun syncService(@Body service: ServiceDto): Response<ServiceDto>

    @GET("appointments")
    suspend fun getAppointments(@Query("since") since: Long? = null): Response<List<AppointmentDto>>

    @POST("appointments")
    suspend fun syncAppointment(@Body appointment: AppointmentDto): Response<AppointmentDto>

    @PUT("appointments/{id}/status")
    suspend fun updateAppointmentStatus(
        @Path("id") id: String,
        @Query("status") status: String,
        @Query("updated_at") updatedAt: Long,
    ): Response<AppointmentDto>
}
