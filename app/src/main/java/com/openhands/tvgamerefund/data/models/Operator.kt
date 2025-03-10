package com.openhands.tvgamerefund.data.models

enum class Operator(val displayName: String, val baseUrl: String) {
    FREE("Free Mobile", "https://mobile.free.fr"),
    ORANGE("Orange", "https://orange.fr"),
    SFR("SFR", "https://sfr.fr"),
    BOUYGUES("Bouygues Telecom", "https://bouyguestelecom.fr")
}