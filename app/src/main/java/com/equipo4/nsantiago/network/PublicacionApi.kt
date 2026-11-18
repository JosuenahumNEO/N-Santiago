package com.equipo4.nsantiago.network

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

data class PublicacionVendedorDto(
    val id: Int,
    val nombre: String?,
    val correo: String?,
    val telefono: String?,
    val tieneFoto: Boolean = false,
    val fotoVersion: String? = null
)

data class PublicacionDto(
    val id: Int,
    val titulo: String,
    val descripcion: String?,
    val reglas: String?,
    val precio: Double,
    val modalidad: String?,
    val tipo: String?,
    val estado: String?,
    val activa: Boolean = true,
    val calle: String?,
    val numero: String?,
    val colonia: String?,
    val codigoPostal: String?,
    val latitud: Double?,
    val longitud: Double?,
    val superficie: Int?,
    val usoSuelo: String?,
    val dormitorios: Int?,
    val banos: Int?,
    val cochera: Int?,
    val piso: Int?,
    val niveles: Int?,
    val amueblado: Boolean = false,
    val mascotas: Boolean = false,
    val elevador: Boolean = false,
    val patio: Boolean = false,
    val fotos: List<Int> = emptyList(),
    val vendedor: PublicacionVendedorDto?,
    val guardada: Boolean = false
)

data class CrearPublicacionRequest(
    val titulo: String,
    val descripcion: String,
    val reglas: String?,
    val precio: Double,
    val modalidad: String,
    val tipo: String,
    val calle: String,
    val numero: String,
    val colonia: String,
    val codigoPostal: String?,
    val latitud: Double?,
    val longitud: Double?,
    val superficie: Int?,
    val usoSuelo: String?,
    val dormitorios: Int?,
    val banos: Int?,
    val cochera: Int?,
    val piso: Int?,
    val niveles: Int?,
    val amueblado: Boolean,
    val mascotas: Boolean,
    val elevador: Boolean,
    val patio: Boolean
)

interface PublicacionApi {
    @GET("api/v1/publicaciones")
    suspend fun listar(): Response<List<PublicacionDto>>

    @GET("api/v1/publicaciones/mias")
    suspend fun misPublicaciones(): Response<List<PublicacionDto>>

    @GET("api/v1/publicaciones/{id}")
    suspend fun detalle(@Path("id") id: Int): Response<PublicacionDto>

    @POST("api/v1/publicaciones")
    suspend fun crear(@Body request: CrearPublicacionRequest): Response<PublicacionDto>

    @DELETE("api/v1/publicaciones/{id}")
    suspend fun eliminar(@Path("id") id: Int): Response<Unit>

    @Multipart
    @POST("api/v1/publicaciones/{id}/fotos")
    suspend fun subirFoto(
        @Path("id") id: Int,
        @Part foto: MultipartBody.Part
    ): Response<PublicacionDto>
}
