package com.example.turnofacil.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO para la transferencia de servicios con la API REST.
 */
data class ServiceDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("price") val price: Double,
    @SerializedName("duration_min") val durationMin: Int,
    @SerializedName("active") val active: Boolean = true,
    @SerializedName("updated_at") val updatedAt: Long = System.currentTimeMillis(),
)
