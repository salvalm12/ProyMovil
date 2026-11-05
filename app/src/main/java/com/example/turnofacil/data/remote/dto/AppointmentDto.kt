package com.example.turnofacil.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO para la transferencia de citas con la API REST.
 */
data class AppointmentDto(
    @SerializedName("id") val id: String,
    @SerializedName("service_id") val serviceId: String,
    @SerializedName("customer_name") val customerName: String,
    @SerializedName("attention_type") val attentionType: String,
    @SerializedName("addons") val addons: List<String> = emptyList(),
    @SerializedName("date") val date: String,
    @SerializedName("time") val time: String,
    @SerializedName("status") val status: String,
    @SerializedName("total_price") val totalPrice: Double,
    @SerializedName("updated_at") val updatedAt: Long = System.currentTimeMillis(),
)
