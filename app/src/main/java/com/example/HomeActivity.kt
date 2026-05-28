package com.example

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.BackHandler
import coil.compose.AsyncImage
import com.example.api.RetrofitClient
import com.example.api.XtreamApi
import com.example.data.SessionManager
import com.example.model.Category
import com.example.ui.theme.*
import kotlinx.coroutines.launch

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                HomeScreen(
                    onLogout = {
                        SessionManager(this).logout()
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    },
                    onPlay = { url ->
                        val intent = Intent(this, PlayerActivity::class.java)
                        intent.putExtra("STREAM_URL", url)
                        startActivity(intent)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onLogout: () -> Unit, onPlay: (String) -> Unit) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val serverUrl = sessionManager.getServerUrl() ?: ""
    val user = sessionManager.getUsername() ?: ""
    val pass = sessionManager.getPassword() ?: ""
    val api = remember { RetrofitClient.getApi(serverUrl) }

    var selectedTab by remember { mutableStateOf(0) } // 0: Live, 1: Movies, 2: Series
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("XtreamPro", fontWeight = FontWeight.Bold, color = Color.White) },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundDark)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = BottomNavDark) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Tv, contentDescription = "Live") },
                    label = { Text("Live TV") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = PrimaryRed, unselectedIconColor = Color.White)
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Movie, contentDescription = "Movies") },
                    label = { Text("Movies") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = PrimaryRed, unselectedIconColor = Color.White)
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.VideoLibrary, contentDescription = "Series") },
                    label = { Text("Series") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = PrimaryRed, unselectedIconColor = Color.White)
                )
            }
        },
        containerColor = BackgroundDark
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            when (selectedTab) {
                0 -> LiveTvScreen(api, user, pass, serverUrl, onPlay)
                1 -> VodScreen(api, user, pass, serverUrl, onPlay)
                2 -> SeriesScreen(api, user, pass, serverUrl, onPlay)
            }
        }
    }
}

@Composable
fun LiveTvScreen(api: XtreamApi, user: String, pass: String, serverUrl: String, onPlay: (String) -> Unit) {
    var categories by remember { mutableStateOf<List<Category>>(emptyList()) }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var streams by remember { mutableStateOf<List<com.example.model.LiveStream>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isLoading = true
        try {
            categories = api.getLiveCategories(user, pass)
            if (categories.isNotEmpty()) {
                selectedCategory = categories.first()
            }
        } catch (e: Exception) { }
        finally { isLoading = false }
    }

    LaunchedEffect(selectedCategory) {
        selectedCategory?.let { category ->
            isLoading = true
            try {
                streams = api.getLiveStreams(user, pass, categoryId = category.categoryId)
            } catch (e: Exception) { }
            finally { isLoading = false }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                val isSelected = selectedCategory == category
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) PrimaryRed else Color.White.copy(alpha = 0.1f))
                        .clickable { selectedCategory = category }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(category.categoryName, color = if (isSelected) Color.White else TextGray, fontSize = 14.sp)
                }
            }
        }

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = PrimaryRed) }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(streams) { stream ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.clickable {
                            val url = "$serverUrl/live/$user/$pass/${stream.streamId}.m3u8"
                            onPlay(url)
                        }
                    ) {
                        Column {
                            Box(modifier = Modifier.fillMaxWidth().aspectRatio(16f/9f)) {
                                AsyncImage(
                                    model = stream.streamIcon ?: "https://via.placeholder.com/400x225/3b82f6/ffffff?text=Stream",
                                    contentDescription = stream.name,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                Box(
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(PrimaryRed)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("LIVE", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                                }
                                Box(
                                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.White, modifier = Modifier.size(48.dp))
                                }
                            }
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(stream.name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                                Text("Live TV", color = TextGray, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VodScreen(api: XtreamApi, user: String, pass: String, serverUrl: String, onPlay: (String) -> Unit) {
    var categories by remember { mutableStateOf<List<Category>>(emptyList()) }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var streams by remember { mutableStateOf<List<com.example.model.VodStream>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isLoading = true
        try {
            categories = api.getVodCategories(user, pass)
            if (categories.isNotEmpty()) {
                selectedCategory = categories.first()
            }
        } catch (e: Exception) { }
        finally { isLoading = false }
    }

    LaunchedEffect(selectedCategory) {
        selectedCategory?.let { category ->
            isLoading = true
            try {
                streams = api.getVodStreams(user, pass, categoryId = category.categoryId)
            } catch (e: Exception) { }
            finally { isLoading = false }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                val isSelected = selectedCategory == category
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) PrimaryRed else Color.White.copy(alpha = 0.1f))
                        .clickable { selectedCategory = category }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(category.categoryName, color = if (isSelected) Color.White else TextGray, fontSize = 14.sp)
                }
            }
        }

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = PrimaryRed) }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(streams) { stream ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.clickable {
                            val ext = stream.containerExtension ?: "mp4"
                            val url = "$serverUrl/movie/$user/$pass/${stream.streamId}.$ext"
                            onPlay(url)
                        }
                    ) {
                        Column {
                            Box(modifier = Modifier.fillMaxWidth().aspectRatio(2f/3f)) {
                                AsyncImage(
                                    model = stream.streamIcon ?: "https://via.placeholder.com/300x450/e94560/ffffff?text=VOD",
                                    contentDescription = stream.name,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                Box(
                                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.White, modifier = Modifier.size(48.dp))
                                }
                            }
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(stream.name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SeriesScreen(api: XtreamApi, user: String, pass: String, serverUrl: String, onPlay: (String) -> Unit) {
    var categories by remember { mutableStateOf<List<Category>>(emptyList()) }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var seriesList by remember { mutableStateOf<List<com.example.model.SeriesStream>>(emptyList()) }
    var selectedSeries by remember { mutableStateOf<com.example.model.SeriesStream?>(null) }
    var seriesInfo by remember { mutableStateOf<com.example.model.SeriesInfoResponse?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isLoading = true
        try {
            categories = api.getSeriesCategories(user, pass)
            if (categories.isNotEmpty()) {
                selectedCategory = categories.first()
            }
        } catch (e: Exception) { }
        finally { isLoading = false }
    }

    LaunchedEffect(selectedCategory) {
        selectedCategory?.let { category ->
            isLoading = true
            try {
                seriesList = api.getSeries(user, pass, categoryId = category.categoryId)
            } catch (e: Exception) { }
            finally { isLoading = false }
        }
    }

    LaunchedEffect(selectedSeries) {
        selectedSeries?.let { series ->
            isLoading = true
            try {
                seriesInfo = api.getSeriesInfo(user, pass, seriesId = series.seriesId)
            } catch (e: Exception) { }
            finally { isLoading = false }
        }
    }

    if (selectedSeries != null) {
        BackHandler {
            selectedSeries = null
            seriesInfo = null
        }
        
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = PrimaryRed) }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    Row(modifier = Modifier.padding(16.dp)) {
                         AsyncImage(
                              model = seriesInfo?.info?.cover ?: selectedSeries?.cover ?: "https://via.placeholder.com/300x450/e94560/ffffff?text=Series",
                              contentDescription = selectedSeries?.name,
                              modifier = Modifier.width(120.dp).aspectRatio(2f/3f).clip(RoundedCornerShape(8.dp)),
                              contentScale = ContentScale.Crop
                         )
                         Spacer(modifier = Modifier.width(16.dp))
                         Column {
                              Text(selectedSeries?.name ?: "", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                              Spacer(modifier = Modifier.height(8.dp))
                              Text(seriesInfo?.info?.plot ?: selectedSeries?.plot ?: "", color = TextGray, fontSize = 14.sp)
                         }
                    }
                }
                
                seriesInfo?.episodes?.let { episodesMap ->
                    episodesMap.forEach { (season, episodes) ->
                        item {
                            Text("Season $season", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp))
                        }
                        items(episodes) { episode ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
                                    .clickable {
                                        val ext = episode.containerExtension ?: "mp4"
                                        val url = "$serverUrl/series/$user/$pass/${episode.id}.$ext"
                                        onPlay(url)
                                    }
                            ) {
                                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.PlayCircle, contentDescription = "Play", tint = PrimaryRed)
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(episode.title.takeIf { !it.isNullOrBlank() } ?: "Episode ${episode.episodeNum}", color = Color.White, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    val isSelected = selectedCategory == category
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) PrimaryRed else Color.White.copy(alpha = 0.1f))
                            .clickable { selectedCategory = category }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(category.categoryName, color = if (isSelected) Color.White else TextGray, fontSize = 14.sp)
                    }
                }
            }

            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = PrimaryRed) }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(seriesList) { series ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.clickable {
                                selectedSeries = series
                            }
                        ) {
                            Column {
                                Box(modifier = Modifier.fillMaxWidth().aspectRatio(2f/3f)) {
                                    AsyncImage(
                                        model = series.cover ?: "https://via.placeholder.com/300x450/e94560/ffffff?text=Series",
                                        contentDescription = series.name,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(series.name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
