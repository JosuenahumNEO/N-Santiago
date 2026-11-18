package com.equipo4.nsantiago.ui.profile.seller

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.equipo4.nsantiago.data.session.CurrentUserStore
import com.equipo4.nsantiago.ui.profile.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.equipo4.nsantiago.di.NetworkModule
import com.equipo4.nsantiago.data.local.SessionManager
import com.equipo4.nsantiago.network.PublicacionApi
import com.equipo4.nsantiago.network.PublicacionDto
import com.equipo4.nsantiago.domain.model.ListingType
import com.equipo4.nsantiago.domain.model.Property
import com.equipo4.nsantiago.domain.model.PropertyType

class MyListingsViewModel(application: Application) : AndroidViewModel(application) {
    private val sessionManager = SessionManager(application)
    private val api = NetworkModule
        .provideRetrofit(NetworkModule.provideOkHttpClient(sessionManager))
        .create(PublicacionApi::class.java)

    private val _uiState = MutableStateFlow(MyListingsUiState())
    val uiState: StateFlow<MyListingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            CurrentUserStore.user.collect { user ->
                _uiState.update {
                    it.copy(
                        userName = user?.let { u ->
                            listOfNotNull(u.nombre, u.primerApellido, u.segundoApellido).joinToString(" ")
                        }.orEmpty(),
                        userEmail = user?.correo.orEmpty(),
                        userPhone = user?.telefono?.takeIf { it.isNotBlank() } ?: "",
                        userPhotoUrl = user?.takeIf { it.tieneFoto && it.fotoVersion != null }
                            ?.let { NetworkModule.fotoPerfilUrl(it.fotoVersion!!) },
                        role = UserRole.fromDb(user?.rol)
                    )
                }
            }
        }
        loadMyListings()
    }

    fun loadMyListings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val response = api.misPublicaciones()
                if (response.isSuccessful) {
                    val listings = response.body().orEmpty().map { dto ->
                        MyListingItem(
                            property = dto.toProperty(),
                            status = when (dto.estado?.uppercase()) {
                                "PUBLICADA" -> ListingStatus.PUBLISHED
                                "RECHAZADA" -> ListingStatus.REJECTED
                                else -> ListingStatus.IN_REVIEW
                            }
                        )
                    }
                    _uiState.update { it.copy(listings = listings) }
                }
            } catch (_: Exception) {
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun deleteListing(propertyId: String) {
        val id = propertyId.toIntOrNull() ?: return
        viewModelScope.launch {
            try {
                val response = api.eliminar(id)
                if (response.isSuccessful) loadMyListings()
            } catch (_: Exception) {
            }
        }
    }

    private fun PublicacionDto.toProperty(): Property {
        val type = when (tipo?.uppercase()) {
            "DEPARTAMENTO" -> PropertyType.DEPARTAMENTO
            "TERRENO" -> PropertyType.TERRENO
            else -> PropertyType.CASA
        }
        val address = listOfNotNull(calle, numero, colonia)
            .joinToString(", ").ifBlank { "Manzanillo, Colima" }
        return Property(
            id = id.toString(),
            title = titulo,
            price = precio,
            listingType = if (modalidad.equals("Venta", ignoreCase = true)) ListingType.VENTA else ListingType.RENTA,
            location = address,
            type = type,
            areaSqM = superficie ?: 0,
            bedrooms = dormitorios,
            bathrooms = banos,
            garages = cochera,
            imageUrls = fotos.map { "${NetworkModule.BASE_URL}api/v1/publicaciones/fotos/$it" },
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
            usoSuelo = usoSuelo
        )
    }
}