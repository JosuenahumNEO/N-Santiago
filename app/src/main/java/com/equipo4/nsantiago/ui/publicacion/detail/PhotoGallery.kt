package com.equipo4.nsantiago.ui.publicacion.detail

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

/**
 * Galería de fotos que se desliza, con contador. Se usa en el detalle de publicación y en el
 * detalle de anuncios del administrador. [overlay] permite poner etiquetas encima.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PhotoGallery(
    images: List<Int>,
    imageUrls: List<String> = emptyList(),
    modifier: Modifier = Modifier,
    counterSeparator: String = "/",
    overlay: @Composable BoxScope.() -> Unit = {}
) {
    val pageCount = if (imageUrls.isNotEmpty()) imageUrls.size else images.size
    val pagerState = rememberPagerState(pageCount = { pageCount })
    Box(modifier = modifier) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val imageUrl = imageUrls.getOrNull(page)
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                images.getOrNull(page)?.let { imageRes ->
                    Image(
                        painter = painterResource(id = imageRes),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        if (pageCount > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${pagerState.currentPage + 1}$counterSeparator$pageCount",
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
        }
        overlay()
    }
}
