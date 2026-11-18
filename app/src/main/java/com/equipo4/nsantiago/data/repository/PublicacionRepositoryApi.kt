package com.equipo4.nsantiago.data.repository

import android.content.Context
import com.equipo4.nsantiago.data.local.SessionManager
import com.equipo4.nsantiago.di.NetworkModule
import com.equipo4.nsantiago.domain.model.ListingType
import com.equipo4.nsantiago.domain.model.Property
import com.equipo4.nsantiago.domain.model.PropertyType
import com.equipo4.nsantiago.domain.repository.PropiedadRepository
import com.equipo4.nsantiago.network.PublicacionApi
import com.equipo4.nsantiago.network.PublicacionDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PublicacionRepositoryApi(context: Context) : PropiedadRepository {
    private val sessionManager = SessionManager(context.applicationContext)
    private val api: PublicacionApi = NetworkModule
        .provideRetrofit(NetworkModule.provideOkHttpClient(sessionManager))
        .create(PublicacionApi::class.java)

    private val properties = MutableStateFlow<List<Property>>(emptyList())

    init {
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            refresh()
        }
    }

    suspend fun refresh() {
        val response = api.listar()
        if (response.isSuccessful) {
            properties.value = response.body().orEmpty()
                .filter { it.activa && it.estado?.uppercase() == "PUBLICADA" }
                .map { it.toProperty() }
        }
    }

    override fun observeAll(): Flow<List<Property>> = properties
    override fun observeSaved(): Flow<List<Property>> = properties.map { it.filter { p -> p.isSaved } }

    override suspend fun getById(propertyId: String): Property? {
        val cached = properties.value.firstOrNull { it.id == propertyId }
        if (cached != null) return cached

        val id = propertyId.toIntOrNull() ?: return null
        val response = api.detalle(id)
        return if (response.isSuccessful) response.body()?.toProperty() else null
    }

    override suspend fun setSaved(propertyId: String, saved: Boolean) {
        properties.update { list ->
            list.map { if (it.id == propertyId) it.copy(isSaved = saved) else it }
        }
    }

    private fun PublicacionDto.toProperty(): Property {
        val propertyType = when (tipo?.uppercase()) {
            "DEPARTAMENTO" -> PropertyType.DEPARTAMENTO
            "TERRENO" -> PropertyType.TERRENO
            else -> PropertyType.CASA
        }
        val address = buildList {
            if (!calle.isNullOrBlank()) add(calle)
            if (!numero.isNullOrBlank()) add(numero)
            if (!colonia.isNullOrBlank()) add(colonia)
        }.joinToString(", ").ifBlank { "Manzanillo, Colima" }

        val photoUrls = fotos.map { photoId ->
            "${NetworkModule.BASE_URL}api/v1/publicaciones/fotos/$photoId"
        }

        return Property(
            id = id.toString(),
            title = titulo,
            price = precio,
            listingType = if (modalidad.equals("Venta", ignoreCase = true)) ListingType.VENTA else ListingType.RENTA,
            location = address,
            type = propertyType,
            areaSqM = superficie ?: 0,
            bedrooms = dormitorios,
            bathrooms = banos,
            garages = cochera,
            images = emptyList(),
            imageUrls = photoUrls,
            isSaved = guardada,
            latitude = latitud,
            longitude = longitud,
            colonia = colonia.orEmpty(),
            niveles = niveles,
            piso = piso,
            jardinPatio = patio,
            mascotasPermitidas = mascotas,
            elevador = elevador,
            amueblado = amueblado,
            usoSuelo = usoSuelo,
            superficieSqM = superficie?.toDouble()
        )
    }
}
