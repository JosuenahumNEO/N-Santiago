package com.equipo4.nsantiago.ui.profile.seller

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.equipo4.nsantiago.R
import com.equipo4.nsantiago.ui.common.NSantiagoBottomBar
import com.equipo4.nsantiago.ui.profile.UserRole
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage

// Si tu componente de tarjeta en 'home' tiene otro nombre, cambia este import:
// import com.equipo4.nsantiago.ui.home.NOMBRE_DE_TU_TARJETA

private val TextDark = Color(0xFF1F2937)
private val SubtitleGray = Color(0xFF6B7280)
private val AvatarBg = Color(0xFFE0F2FE)
private val PrimaryButtonBg = Color(0xFF6C8DAE)

@Composable
fun MyListingsScreen(
    viewModel: MyListingsViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onEditProfileClick: () -> Unit = {},
    onCreateListingClick: () -> Unit = {},
    onPropertyClick: (String) -> Unit = {},
    onBottomTabSelected: (Int) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    // Regla de control de acceso: Si el usuario en sesión es COMPRADOR, regresa a Cuenta.
    LaunchedEffect(uiState.role) {
        if (uiState.role == UserRole.COMPRADOR) {
            onBackClick()
        }
    }

    Scaffold(
        bottomBar = { NSantiagoBottomBar(selectedTab = 3, onTabSelected = onBottomTabSelected) },
        containerColor = Color.White
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Sección Encabezado
            item {
                HeaderSection(
                    uiState = uiState,
                    onBackClick = onBackClick,
                    onEditProfileClick = onEditProfileClick,
                    onCreateListingClick = onCreateListingClick
                )
            }

            // Sección Lista o Estado Vacío
            if (uiState.listings.isEmpty() && !uiState.isLoading) {
                item {
                    EmptyListingsMessage()
                }
            } else {
                items(
                    items = uiState.listings,
                    key = { it.property.id }
                ) { item ->
                    ListingCard(
                        item = item,
                        onView = { onPropertyClick(item.property.id) },
                        onDelete = { viewModel.deleteListing(item.property.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HeaderSection(
    uiState: MyListingsUiState,
    onBackClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onCreateListingClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Barra Superior
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Regresar",
                    tint = TextDark
                )
            }
            IconButton(onClick = onEditProfileClick) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = "Editar perfil",
                    tint = TextDark
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // Avatar: foto de perfil o ícono por defecto
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(AvatarBg),
            contentAlignment = Alignment.Center
        ) {
            if (uiState.userPhotoUrl != null) {
                AsyncImage(
                    model = uiState.userPhotoUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = null,
                    tint = Color(0xFF0284C7),
                    modifier = Modifier.size(50.dp)
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Nombre completo
        Text(
            text = uiState.userName.ifBlank { stringResource(R.string.profile_default_name) },
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )

        Spacer(Modifier.height(6.dp))

        // Correo electrónico
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.Email,
                contentDescription = null,
                tint = SubtitleGray,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = uiState.userEmail,
                fontSize = 13.sp,
                color = SubtitleGray
            )
        }

        Spacer(Modifier.height(4.dp))

        // Teléfono
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.Phone,
                contentDescription = null,
                tint = SubtitleGray,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = uiState.userPhone,
                fontSize = 13.sp,
                color = SubtitleGray
            )
        }

        Spacer(Modifier.height(16.dp))

        // Botón "Crear anuncio +"
        Button(
            onClick = onCreateListingClick,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryButtonBg,
                contentColor = Color.White
            ),
            modifier = Modifier
                .width(180.dp)
                .height(42.dp)
        ) {
            Text(
                text = "Crear anuncio +",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun StatusChip(status: ListingStatus, modifier: Modifier = Modifier) {
    Surface(
        color = Color(status.colorHex),
        shape = RoundedCornerShape(6.dp),
        modifier = modifier
    ) {
        Text(
            text = status.label,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun EmptyListingsMessage() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 40.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Aún no tienes anuncios. Crea tu primer anuncio para que los compradores lo vean.",
            fontSize = 14.sp,
            color = SubtitleGray,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
    }
}

@Composable
private fun ListingCard(
    item: MyListingItem,
    onView: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(92.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE5E7EB)),
                    contentAlignment = Alignment.Center
                ) {
                    val photoUrl = item.property.imageUrls.firstOrNull()
                    if (photoUrl != null) {
                        AsyncImage(
                            model = photoUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text("foto", color = SubtitleGray)
                    }
                }

                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.property.location,
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        maxLines = 2
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "$${item.property.price.toInt()} /mensual",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6C8DAE)
                    )
                    Spacer(Modifier.height(6.dp))
                    StatusChip(item.status)
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onView, modifier = Modifier.weight(1f)) {
                    Text("Ver")
                }
                Button(
                    onClick = { showDeleteDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF111827)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Borrar")
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("¿Estás seguro?") },
            text = { Text("La publicación se eliminará de forma permanente.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    }
                ) { Text("Sí, eliminar publicación", color = Color(0xFFB91C1C)) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("No, mantener publicación") }
            }
        )
    }
}