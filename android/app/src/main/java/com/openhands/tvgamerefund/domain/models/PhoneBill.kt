package com.openhands.tvgamerefund.domain.models

import java.util.Date

data class PhoneBill(
    val id: String,
    val operator: PhoneOperator,
    val billDate: Date,
    val downloadUrl: String,
    val totalAmount: Double,
    val billPeriod: BillPeriod,
    val localPath: String? = null,
    val processedPath: String? = null
)

data class BillPeriod(
    val startDate: Date,
    val endDate: Date
)

enum class PhoneOperator(val apiBaseUrl: String) {
    FREE("https://mobile.free.fr"),
    ORANGE("https://espaceclient.orange.fr"),
    SFR("https://espace-client.sfr.fr"),
    BOUYGUES("https://www.bouyguestelecom.fr")
}

data class BillLine(
    val date: Date,
    val time: String,
    val type: String,
    val number: String,
    val duration: String? = null,
    val amount: Double
)