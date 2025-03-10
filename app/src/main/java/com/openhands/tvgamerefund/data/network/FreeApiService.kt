package com.openhands.tvgamerefund.data.network

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface FreeApiService {
    @GET("account/v2/api/SI/invoice/{invoiceId}")
    suspend fun getInvoice(
        @Path("invoiceId") invoiceId: String,
        @Header("Authorization") authToken: String
    ): Response<ResponseBody>

    @GET("account/v2/api/SI/invoices")
    suspend fun getInvoicesList(
        @Header("Authorization") authToken: String
    ): Response<List<InvoiceInfo>>
}

data class InvoiceInfo(
    val id: String,
    val date: String,
    val amount: Double,
    val downloadUrl: String
)