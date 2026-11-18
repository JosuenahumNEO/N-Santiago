package com.equipo4.nsantiago.domain.model

data class Country(
    val name: String,
    val code: String,      // Ej. "+52"
    val flag: String,      // Emoji de bandera, Ej. "🇲🇽"
    val isoCode: String
)