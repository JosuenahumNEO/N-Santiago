package com.equipo4.nsantiago

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.equipo4.nsantiago.data.local.SessionManager
import com.equipo4.nsantiago.data.local.SettingsManager
import com.equipo4.nsantiago.ui.auth.login.LoginScreen
import com.equipo4.nsantiago.ui.auth.register.SignUpScreen
import com.equipo4.nsantiago.ui.createlisting.CreateListingPreviewScreen
import com.equipo4.nsantiago.ui.createlisting.CreateListingStep1Screen
import com.equipo4.nsantiago.ui.createlisting.CreateListingStep2Screen
import com.equipo4.nsantiago.ui.createlisting.CreateListingStep3Screen
import com.equipo4.nsantiago.ui.createlisting.CreateListingStep4Screen
import com.equipo4.nsantiago.ui.createlisting.CreateListingViewModel
import com.equipo4.nsantiago.ui.home.list.HomeScreen
import com.equipo4.nsantiago.ui.profile.seller.MyListingsScreen
import com.equipo4.nsantiago.ui.publicacion.detail.DetailPublicacionScreen
import com.equipo4.nsantiago.ui.theme.NSantiagoTheme

object AppRoutes {
    const val VISITOR = "visitor"
    const val LOGIN = "login"
    const val SIGN_UP = "signup"
    const val HOME = "home"
    const val PUBLICACION_DETAIL = "publicacion_detail"
    const val MY_LISTINGS = "my_listings"
    const val CREATE_LISTING_GRAPH = "create_listing_graph"
    const val CREATE_LISTING_STEP1 = "create_listing_step1"
    const val CREATE_LISTING_STEP2 = "create_listing_step2"
    const val CREATE_LISTING_STEP3 = "create_listing_step3"
    const val CREATE_LISTING_STEP4 = "create_listing_step4"
    const val CREATE_LISTING_PREVIEW = "create_listing_preview"
}

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            remember { SettingsManager(context) }
            val darkModeState by SettingsManager.darkModeState.collectAsState()
            val useDarkTheme = darkModeState ?: false

            NSantiagoTheme(darkTheme = useDarkTheme) {
                NSantiagoApp()
            }
        }
    }
}

@Composable
fun NSantiagoApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    NavHost(
        navController = navController,
        startDestination = AppRoutes.VISITOR
    ) {
        composable(AppRoutes.VISITOR) {
            VisitorScreen(onIngresar = { navController.navigate(AppRoutes.LOGIN) })
        }

        composable(AppRoutes.LOGIN) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(AppRoutes.SIGN_UP) },
                onLoginSuccess = {
                    sessionManager.setLoggedIn(true)
                    navController.navigate(AppRoutes.HOME) {
                        popUpTo(AppRoutes.VISITOR) { inclusive = true }
                    }
                }
            )
        }

        composable(AppRoutes.SIGN_UP) {
            SignUpScreen(
                onNavigateToLogin = { navController.popBackStack() },
                onSignUpSuccess = {
                    navController.navigate(AppRoutes.HOME) {
                        popUpTo(AppRoutes.SIGN_UP) { inclusive = true }
                    }
                }
            )
        }

        composable(AppRoutes.HOME) {
            HomeScreen(
                onPropertyClick = { propertyId ->
                    navController.navigate("${AppRoutes.PUBLICACION_DETAIL}/$propertyId")
                },
                onToggleToMap = {},
                onFilterClick = {},
                onBottomTabSelected = { index ->
                    if (index == 3) navController.navigate(AppRoutes.MY_LISTINGS)
                }
            )
        }

        composable(
            route = "${AppRoutes.PUBLICACION_DETAIL}/{publicacionId}",
            arguments = listOf(navArgument("publicacionId") { type = NavType.StringType })
        ) { backStackEntry ->
            val publicacionId = backStackEntry.arguments?.getString("publicacionId").orEmpty()
            DetailPublicacionScreen(
                publicacionId = publicacionId,
                onBackClick = { navController.popBackStack() },
                onReportClick = {},
                onEditClick = {}
            )
        }

        composable(AppRoutes.MY_LISTINGS) {
            MyListingsScreen(
                onBackClick = { navController.popBackStack() },
                onEditProfileClick = {},
                onCreateListingClick = { navController.navigate(AppRoutes.CREATE_LISTING_GRAPH) },
                onPropertyClick = { propertyId ->
                    navController.navigate("${AppRoutes.PUBLICACION_DETAIL}/$propertyId")
                },
                onBottomTabSelected = { index ->
                    if (index == 0) navController.navigate(AppRoutes.HOME)
                }
            )
        }

        navigation(
            route = AppRoutes.CREATE_LISTING_GRAPH,
            startDestination = AppRoutes.CREATE_LISTING_STEP1
        ) {
            composable(AppRoutes.CREATE_LISTING_STEP1) { entry ->
                val parent = remember(entry) { navController.getBackStackEntry(AppRoutes.CREATE_LISTING_GRAPH) }
                val viewModel: CreateListingViewModel = viewModel(parent)
                CreateListingStep1Screen(
                    viewModel = viewModel,
                    onExit = { navController.popBackStack(AppRoutes.CREATE_LISTING_GRAPH, true) },
                    onContinue = { navController.navigate(AppRoutes.CREATE_LISTING_STEP2) },
                    onBottomTabSelected = {}
                )
            }
            composable(AppRoutes.CREATE_LISTING_STEP2) { entry ->
                val parent = remember(entry) { navController.getBackStackEntry(AppRoutes.CREATE_LISTING_GRAPH) }
                val viewModel: CreateListingViewModel = viewModel(parent)
                CreateListingStep2Screen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onContinue = { navController.navigate(AppRoutes.CREATE_LISTING_STEP3) },
                    onBottomTabSelected = {}
                )
            }
            composable(AppRoutes.CREATE_LISTING_STEP3) { entry ->
                val parent = remember(entry) { navController.getBackStackEntry(AppRoutes.CREATE_LISTING_GRAPH) }
                val viewModel: CreateListingViewModel = viewModel(parent)
                CreateListingStep3Screen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onContinue = { navController.navigate(AppRoutes.CREATE_LISTING_STEP4) },
                    onBottomTabSelected = {}
                )
            }
            composable(AppRoutes.CREATE_LISTING_STEP4) { entry ->
                val parent = remember(entry) { navController.getBackStackEntry(AppRoutes.CREATE_LISTING_GRAPH) }
                val viewModel: CreateListingViewModel = viewModel(parent)
                CreateListingStep4Screen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onEditData = { navController.popBackStack(AppRoutes.CREATE_LISTING_STEP1, false) },
                    onEditContent = { navController.popBackStack(AppRoutes.CREATE_LISTING_STEP3, false) },
                    onContinue = { navController.navigate(AppRoutes.CREATE_LISTING_PREVIEW) },
                    onBottomTabSelected = {}
                )
            }
            composable(AppRoutes.CREATE_LISTING_PREVIEW) { entry ->
                val parent = remember(entry) { navController.getBackStackEntry(AppRoutes.CREATE_LISTING_GRAPH) }
                val viewModel: CreateListingViewModel = viewModel(parent)
                CreateListingPreviewScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onEdit = { navController.popBackStack(AppRoutes.CREATE_LISTING_STEP1, false) },
                    onFinished = {
                        viewModel.reset()
                        navController.navigate(AppRoutes.MY_LISTINGS) {
                            popUpTo(AppRoutes.CREATE_LISTING_GRAPH) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun VisitorScreen(onIngresar: () -> Unit) {
    val primary = 0xFF3158A8

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .background(androidx.compose.ui.graphics.Color(primary))
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("N'Santiago", color = androidx.compose.ui.graphics.Color.White, fontWeight = FontWeight.Bold)
                Button(
                    onClick = onIngresar,
                    colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color.White)
                ) {
                    Text("Ingresar", color = androidx.compose.ui.graphics.Color(primary))
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Estudiante, descubre tu nuevo cuarto N'Santiago",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    repeat(2) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(140.dp)
                                .background(androidx.compose.ui.graphics.Color(0xFFE5E7EB), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) { Text("foto", color = androidx.compose.ui.graphics.Color.Gray) }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(androidx.compose.ui.graphics.Color(0xFFE5E7EB), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) { Text("foto", color = androidx.compose.ui.graphics.Color.Gray) }

                Text(
                    "Cuartos sin amueblar en Santiago, Manzanillo, Col.",
                    textAlign = TextAlign.Center
                )

                Button(
                    onClick = onIngresar,
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) { Text("Explorar") }
            }
        }
    }
}
