package com.equipo4.nsantiago.ui.createlisting

import com.equipo4.nsantiago.ui.profile.UserRole

/** Solo vendedor, administrador y superadministrador crean anuncios. Sin rol cargado, no. */
fun canCreateListings(role: UserRole?): Boolean = when (role) {
    UserRole.VENDEDOR, UserRole.ADMIN, UserRole.SUPER_ADMIN -> true
    UserRole.COMPRADOR, null -> false
}