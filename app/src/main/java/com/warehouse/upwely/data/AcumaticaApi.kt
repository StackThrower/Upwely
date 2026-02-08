package com.warehouse.upwely.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class ApiShipmentDetail(
    val id: String,
    val inventoryId: String,
    val description: String,
    val shippedQty: Int,
    val locationId: String,
    val warehouseId: String,
)

data class ApiShipment(
    val id: String,
    val shipmentNbr: String,
    val customerId: String,
    val status: String,
    val details: List<ApiShipmentDetail>,
)

class AcumaticaApi(private val context: Context) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val baseUrl: String
        get() = SettingsRepository.getBaseUrl(context)

    private var accessToken: String?
        get() = SettingsRepository.getAccessToken(context)
        set(value) = SettingsRepository.setAccessToken(context, value)

    suspend fun getToken(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val formBody = FormBody.Builder()
                .add("client_id", SettingsRepository.getClientId(context))
                .add("username", SettingsRepository.getUsername(context))
                .add("password", SettingsRepository.getPassword(context))
                .add("scope", "api")
                .add("client_secret", SettingsRepository.getClientSecret(context))
                .add("grant_type", "password")
                .build()

            val request = Request.Builder()
                .url("$baseUrl/identity/connect/token")
                .post(formBody)
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string() ?: return@withContext Result.failure(Exception("Empty response"))
                val json = JSONObject(body)
                val token = json.getString("access_token")
                accessToken = token
                Result.success(token)
            } else {
                Result.failure(Exception("Token request failed: ${response.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getOpenShipments(): Result<List<ApiShipment>> = withContext(Dispatchers.IO) {
        try {
            val token = accessToken ?: getToken().getOrThrow()

            val request = Request.Builder()
                .url("$baseUrl/entity/Default/24.200.001/Shipment?\$top=10&\$filter=Status%20eq%20%27Open%27&\$expand=Details")
                .addHeader("Authorization", "Bearer $token")
                .get()
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string() ?: return@withContext Result.failure(Exception("Empty response"))
                val shipments = parseShipments(body)
                Result.success(shipments)
            } else if (response.code == 401) {
                accessToken = null
                val newToken = getToken().getOrThrow()
                val retryRequest = Request.Builder()
                    .url("$baseUrl/entity/Default/24.200.001/Shipment?\$top=10&\$filter=Status%20eq%20%27Open%27&\$expand=Details")
                    .addHeader("Authorization", "Bearer $newToken")
                    .get()
                    .build()
                val retryResponse = client.newCall(retryRequest).execute()
                if (retryResponse.isSuccessful) {
                    val body = retryResponse.body?.string() ?: return@withContext Result.failure(Exception("Empty response"))
                    val shipments = parseShipments(body)
                    Result.success(shipments)
                } else {
                    Result.failure(Exception("Shipments request failed: ${retryResponse.code}"))
                }
            } else {
                Result.failure(Exception("Shipments request failed: ${response.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseShipments(json: String): List<ApiShipment> {
        val arr = JSONArray(json)
        val shipments = mutableListOf<ApiShipment>()

        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val id = obj.getString("id")
            val shipmentNbr = obj.getJSONObject("ShipmentNbr").getString("value")
            val customerId = obj.getJSONObject("CustomerID").getString("value")
            val status = obj.getJSONObject("Status").getString("value")

            val detailsArr = obj.optJSONArray("Details") ?: JSONArray()
            val details = mutableListOf<ApiShipmentDetail>()

            for (j in 0 until detailsArr.length()) {
                val detailObj = detailsArr.getJSONObject(j)
                details.add(
                    ApiShipmentDetail(
                        id = detailObj.getString("id"),
                        inventoryId = detailObj.getJSONObject("InventoryID").getString("value"),
                        description = detailObj.getJSONObject("Description").getString("value"),
                        shippedQty = detailObj.getJSONObject("ShippedQty").getDouble("value").toInt(),
                        locationId = detailObj.getJSONObject("LocationID").getString("value"),
                        warehouseId = detailObj.getJSONObject("WarehouseID").getString("value"),
                    )
                )
            }

            shipments.add(
                ApiShipment(
                    id = id,
                    shipmentNbr = shipmentNbr,
                    customerId = customerId,
                    status = status,
                    details = details,
                )
            )
        }

        return shipments
    }
}
