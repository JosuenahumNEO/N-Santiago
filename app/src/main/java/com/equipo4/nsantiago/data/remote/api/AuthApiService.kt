package com.equipo4.nsantiago.data.remote.api

import com.equipo4.nsantiago.data.remote.dto.GoogleAuthRequestDto
import com.equipo4.nsantiago.data.remote.dto.GoogleAuthResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    @POST("auth/google")
    suspend fun loginWithGoogle(
        @Body request: GoogleAuthRequestDto
    ): Response<GoogleAuthResponseDto>
}