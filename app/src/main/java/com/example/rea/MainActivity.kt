package com.example.rea


import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkAdded
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.delay



// ─────────────────────────────────────────────
// AKTİVİTE
// ─────────────────────────────────────────────
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = FileRepository(applicationContext)
        val factory    = BookViewModelFactory(repository)
        val viewModel  = ViewModelProvider(this, factory)[BookViewModel::class.java]

        setContent {
            ReaTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    AppNavigator(viewModel = viewModel)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// YÖNLENDİRİCİ + SPLASH
// ─────────────────────────────────────────────
@Composable
fun AppNavigator(viewModel: BookViewModel) {
    var showSplash by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(1800)
        showSplash = false
    }
    AnimatedContent(
        targetState = showSplash,
        transitionSpec = {
            fadeIn(tween(400)) togetherWith fadeOut(tween(400))
        },
        label = "splash_transition"
    ) { isSplash ->
        if (isSplash) SplashScreen() else MainScreen(viewModel)
    }
}

@Composable
fun SplashScreen() {
    val alpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(600),
        label = "splash_alpha"
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LocalReaColors.current.headerBg),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(LocalReaColors.current.amber.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.List,
                    contentDescription = null,
                    tint = LocalReaColors.current.amber,
                    modifier = Modifier.size(36.dp)
                )
            }
            Spacer(Modifier.height(24.dp))
            Text(
                text = "Rea Library",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = LocalReaColors.current.headerText,
                letterSpacing = (-0.5).sp
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Koleksiyonun, senin dünyanda",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.3.sp
            )
            Spacer(Modifier.height(48.dp))
            CircularProgressIndicator(
                color = LocalReaColors.current.amber,
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp
            )
        }
    }
}

// ─────────────────────────────────────────────
// ANA EKRAN İSKELETİ
// ─────────────────────────────────────────────
@Composable
fun MainScreen(viewModel: BookViewModel) {
    var currentTab by remember { mutableIntStateOf(0) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            ReaBottomBar(currentTab = currentTab, onTabChange = { currentTab = it })
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when (currentTab) {
                0 -> LibraryScreen(
                    viewModel = viewModel,
                    onNavigateToSearch = { authorName ->
                        viewModel.updateSearchQuery(authorName)
                        currentTab = 1
                    }
                )
                1 -> SearchScreen(viewModel)
                2 -> ManualAddScreen(viewModel)
            }
        }
    }
}

@Composable
fun ReaBottomBar(currentTab: Int, onTabChange: (Int) -> Unit) {
    val tabs = listOf(
        Triple(Icons.Default.List,    "Kütüphane", 0),
        Triple(Icons.Default.Explore, "Keşfet",    1),
        Triple(Icons.Default.Add,     "Ekle",      2),
    )
    NavigationBar(
        containerColor = LocalReaColors.current.headerBg,
        tonalElevation = 0.dp
    ) {
        tabs.forEach { (icon, label, idx) ->
            val selected = currentTab == idx
            NavigationBarItem(
                selected  = selected,
                onClick   = { onTabChange(idx) },
                icon      = {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (selected) LocalReaColors.current.amber.copy(alpha = 0.15f)
                                else Color.Transparent
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                },
                label     = {
                    Text(
                        text     = label,
                        fontSize = 10.sp,
                        letterSpacing = 0.3.sp
                    )
                },
                colors    = NavigationBarItemDefaults.colors(
                    selectedIconColor   = LocalReaColors.current.amber,
                    selectedTextColor   = LocalReaColors.current.amber,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor      = Color.Transparent
                )
            )
        }
    }
}

// ─────────────────────────────────────────────
// KÜTÜPHANE SEKMESİ
// ─────────────────────────────────────────────
@Composable
fun LibraryScreen(
    viewModel: BookViewModel,
    onNavigateToSearch: (String) -> Unit = {}
) {
    val myLibrary    by viewModel.myLibrary.collectAsState(initial = emptyList())
    var selectedBook by remember { mutableStateOf<Book?>(null) }

    val finishedCount = myLibrary.count { it.status == ReadingStatus.FINISHED }
    val readingCount  = myLibrary.count { it.status == ReadingStatus.READING }

    Column(modifier = Modifier.fillMaxSize()) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(LocalReaColors.current.headerBg)
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Column {
                Text(
                    text          = "Koleksiyonum",
                    fontSize      = 11.sp,
                    letterSpacing = 2.sp,
                    color         = LocalReaColors.current.amber,
                    fontWeight    = FontWeight.Medium
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text       = "Kütüphanem",
                    fontSize   = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color      = LocalReaColors.current.headerText
                )
            }
        }

        if (myLibrary.isEmpty()) {
            LibraryEmptyState()
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(value = myLibrary.size.toString(), label = "Kitap",      modifier = Modifier.weight(1f))
                StatCard(value = finishedCount.toString(),  label = "Tamamlandı", modifier = Modifier.weight(1f))
                StatCard(value = readingCount.toString(),   label = "Okunuyor",   modifier = Modifier.weight(1f))
            }

            LazyColumn(
                modifier       = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items       = myLibrary,
                    key         = { it.id },
                    contentType = { "LibraryBook" }
                ) { book ->
                    LibraryBookRow(
                        book    = book,
                        onClick = { selectedBook = book }
                    )
                }
                item { Spacer(Modifier.height(8.dp)) }
            }
        }
    }

    selectedBook?.let { book ->
        BookTrackingDialog(
            book      = book,
            onDismiss = { selectedBook = null },
            onSave    = { status, progress, totalPages, rating, notes ->
                viewModel.updateBookTracking(book, status, progress, totalPages, rating, notes)
                selectedBook = null
            },
            onAuthorClick = { authorName ->
                selectedBook = null
                onNavigateToSearch(authorName)
            }
        )
    }
}

@Composable
fun StatCard(value: String, label: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape    = RoundedCornerShape(12.dp),
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border   = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text       = value,
                fontSize   = 24.sp,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text     = label,
                fontSize = 11.sp,
                color    = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun LibraryBookRow(book: Book, onClick: () -> Unit) {
    val coverColor = CoverPalette[book.id.hashCode().and(0x7FFFFFFF) % CoverPalette.size]
    val context    = LocalContext.current

    val progress = if (book.totalPages > 0 && book.progress > 0)
        (book.progress.toFloat() / book.totalPages).coerceIn(0f, 1f)
    else 0f

    val statusColor = when (book.status) {
        ReadingStatus.READING  -> LocalReaColors.current.amber
        ReadingStatus.FINISHED -> GreenDone
        ReadingStatus.DROPPED  -> MaterialTheme.colorScheme.onSurfaceVariant
        ReadingStatus.UNREAD   -> MaterialTheme.colorScheme.outline
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape  = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column {
            Row(
                modifier          = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(52.dp)
                        .height(76.dp)
                        .clip(RoundedCornerShape(6.dp))
                ) {
                    if (book.coverUrl != null) {
                        coil.compose.AsyncImage(
                            model = coil.request.ImageRequest.Builder(context)
                                .data(book.coverUrl)
                                .size(156, 228)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Kapak",
                            contentScale       = ContentScale.Crop,
                            modifier           = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(coverColor),
                            contentAlignment = Alignment.BottomStart
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f))
                                        )
                                    )
                            )
                            Text(
                                text       = book.title.take(3).uppercase(),
                                fontSize   = 9.sp,
                                color      = Color.White.copy(alpha = 0.8f),
                                modifier   = Modifier.padding(4.dp),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text       = book.title,
                        fontSize   = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = MaterialTheme.colorScheme.onSurface,
                        maxLines   = 2,
                        overflow   = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text     = book.author,
                        fontSize = 12.sp,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(7.dp))

                    Row(
                        verticalAlignment      = Alignment.CenterVertically,
                        horizontalArrangement  = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(statusColor.copy(alpha = 0.12f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text       = book.status.label,
                                fontSize   = 10.sp,
                                color      = statusColor,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        if (book.rating > 0) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    tint     = LocalReaColors.current.amber,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(Modifier.width(2.dp))
                                Text(
                                    text     = "${book.rating}/5",
                                    fontSize = 11.sp,
                                    color    = LocalReaColors.current.amber,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                Icon(
                    imageVector        = Icons.Default.BookmarkAdded,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.outline,
                    modifier           = Modifier.size(18.dp)
                )
            }

            if (book.totalPages > 0) {
                Column(
                    modifier = Modifier.padding(start = 14.dp, end = 14.dp, bottom = 12.dp)
                ) {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text     = "Sayfa ${book.progress} / ${book.totalPages}",
                            fontSize = 10.sp,
                            color    = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text     = "${(progress * 100).toInt()}%",
                            fontSize = 10.sp,
                            color    = LocalReaColors.current.amber,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(Modifier.height(5.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(MaterialTheme.colorScheme.outline)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    if (book.status == ReadingStatus.FINISHED)
                                        GreenDone
                                    else LocalReaColors.current.amber
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GenreChip(label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(LocalReaColors.current.amberLight)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text     = label,
            fontSize = 10.sp,
            color    = LocalReaColors.current.amber,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun LibraryEmptyState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.List,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(36.dp)
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text       = "Kütüphane boş",
                fontSize   = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text     = "Keşfet sekmesinden kitap ekleyebilirsin.",
                fontSize = 13.sp,
                color    = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ─────────────────────────────────────────────
// KEŞFET / ARAMA SEKMESİ
// ─────────────────────────────────────────────

private val defaultGenreQueryMap = linkedMapOf(
    "Tümü"      to null,
    "LitRPG"    to "LitRPG",
    "Xuanhuan"  to "Xuanhuan cultivation",
    "Fantezi"   to "Fantasy magic",
    "Cyberpunk" to "Cyberpunk",
    "Sci-Fi"    to "Science Fiction",
    "Thriller"  to "Thriller suspense",
    "Adventure" to "Adventure quest"
)

@Composable
fun SearchScreen(viewModel: BookViewModel) {
    val context         = LocalContext.current
    val searchResults   by viewModel.searchResults.collectAsState()
    val discoverResults by viewModel.displayedDiscoverBooks.collectAsState()
    val pendingQuery    by viewModel.pendingSearchQuery.collectAsState()

    var searchQuery  by remember { mutableStateOf("") }
    var selectedBook by remember { mutableStateOf<ApiBook?>(null) }
    var activeGenre  by remember { mutableStateOf("Tümü") }
    var genreLoading by remember { mutableStateOf(false) }


    var customGenres by remember { mutableStateOf(linkedMapOf<String, String>()) }
    var showAddGenreDialog by remember { mutableStateOf(false) }

    // Tüm tür haritası (varsayılan + özel)
    val genreQueryMap = remember(customGenres) {
        val combined = linkedMapOf<String, String?>()
        combined.putAll(defaultGenreQueryMap)
        combined.putAll(customGenres)
        combined
    }
    val genres = genreQueryMap.keys.toList()

    val gridState  = rememberLazyGridState()
    val isAtBottom = !gridState.canScrollForward

    // Dışarıdan gelen yazar arama yönlendirmesi (kütüphaneden)
    LaunchedEffect(pendingQuery) {
        val q = viewModel.consumePendingSearchQuery()
        if (!q.isNullOrBlank()) {
            searchQuery = q
            activeGenre = "Tümü"
            viewModel.searchBooks(q)
        }
    }

    LaunchedEffect(isAtBottom) {
        if (isAtBottom && searchQuery.isBlank() && activeGenre == "Tümü") {
            viewModel.loadNextDiscoverPage()
        }
    }

    LaunchedEffect(activeGenre) {
        val query = genreQueryMap[activeGenre]
        if (query != null) {
            genreLoading = true
            viewModel.searchBooks(query)
            delay(800)
            genreLoading = false
        }
    }

    val currentList = when {
        searchQuery.isNotBlank() -> searchResults
        activeGenre != "Tümü"   -> searchResults
        else                    -> discoverResults
    }

    LaunchedEffect(activeGenre, searchQuery) {
        gridState.animateScrollToItem(0)
    }

    Column(modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(LocalReaColors.current.headerBg)
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Text(
                "Yeni Dünyalar",
                fontSize = 11.sp, letterSpacing = 2.sp,
                color = LocalReaColors.current.amber, fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(4.dp))
            Text("Keşfet", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = LocalReaColors.current.headerText)
            Spacer(Modifier.height(14.dp))

            // Arama kutusu
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .padding(horizontal = 14.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Explore,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.35f),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(10.dp))
                Box(modifier = Modifier.weight(1f)) {
                    if (searchQuery.isEmpty()) {
                        Text(
                            "Kitap, yazar, tür ara…",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.3f)
                        )
                    }
                    androidx.compose.foundation.text.BasicTextField(
                        value         = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            if (it.isNotBlank()) activeGenre = "Tümü"
                        },
                        singleLine = true,
                        textStyle  = androidx.compose.ui.text.TextStyle(
                            fontSize = 14.sp,
                            color    = LocalReaColors.current.headerText
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (searchQuery.isNotBlank()) {
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(LocalReaColors.current.amber)
                            .clickable { viewModel.searchBooks(searchQuery) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Ara", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
                    }
                }
                if (searchQuery.isNotBlank()) {
                    Spacer(Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .clickable { searchQuery = "" }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Temizle",
                            tint = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            //  Tür etiketleri + "+" butonu
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                genres.forEach { genre ->
                    val isActive = genre == activeGenre
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isActive) LocalReaColors.current.amber
                                else Color.White.copy(alpha = 0.08f)
                            )
                            .clickable {
                                searchQuery = ""
                                activeGenre = genre
                            }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text       = genre,
                            fontSize   = 12.sp,
                            color      = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            fontWeight = if (isActive) FontWeight.Medium else FontWeight.Normal
                        )
                    }
                }

                // "+" Tür Ekle butonu
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .clickable { showAddGenreDialog = true }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Tür ekle",
                            tint = LocalReaColors.current.amber.copy(alpha = 0.8f),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Tür Ekle",
                            fontSize = 12.sp,
                            color = LocalReaColors.current.amber.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        // ── Grid / Loading / Boş durum
        when {
            genreLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            color       = LocalReaColors.current.amber,
                            modifier    = Modifier.size(32.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text     = "$activeGenre kitapları yükleniyor…",
                            fontSize = 13.sp,
                            color    = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            currentList.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        color       = LocalReaColors.current.amber,
                        modifier    = Modifier.size(32.dp),
                        strokeWidth = 2.dp
                    )
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    val gridLabel = when {
                        searchQuery.isNotBlank() -> "\"$searchQuery\" için sonuçlar"
                        activeGenre != "Tümü"   -> "$activeGenre · ${currentList.size} kitap"
                        else                    -> "Öne Çıkanlar"
                    }
                    Text(
                        text          = gridLabel.uppercase(),
                        fontSize      = 10.sp,
                        letterSpacing = 2.sp,
                        color         = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier      = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
                    )

                    LazyVerticalGrid(
                        columns        = GridCells.Fixed(3),
                        state          = gridState,
                        modifier       = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement   = Arrangement.spacedBy(14.dp)
                    ) {
                        items(
                            items       = currentList,
                            key         = { it.key },
                            contentType = { "DiscoverBook" }
                        ) { apiBook ->
                            DiscoverBookCard(
                                apiBook = apiBook,
                                onClick = { selectedBook = apiBook }
                            )
                        }
                        item { Spacer(Modifier.height(8.dp)) }
                    }
                }
            }
        }
    }

    // ── Kitap detay diyaloğu
    selectedBook?.let { book ->
        BookDetailDialog(
            title        = book.title,
            author       = book.authorNames?.firstOrNull() ?: "Bilinmiyor",
            description  = "Bu kitabı kütüphanenize eklemek istiyor musunuz?",
            confirmLabel = "Kütüphaneye Ekle",
            onDismiss    = { selectedBook = null },
            onConfirm    = {
                viewModel.addBookToLibrary(book)
                selectedBook = null
                Toast.makeText(context, "Kütüphaneye eklendi!", Toast.LENGTH_SHORT).show()
            },
            onAuthorClick = { authorName ->
                selectedBook = null
                searchQuery = authorName
                activeGenre = "Tümü"
                viewModel.searchBooks(authorName)
            }
        )
    }

    // ── Özel tür ekleme diyaloğu
    if (showAddGenreDialog) {
        AddCustomGenreDialog(
            existingGenres = genres,
            onDismiss = { showAddGenreDialog = false },
            onAdd = { label, query ->
                customGenres = linkedMapOf<String, String>().apply {
                    putAll(customGenres)
                    put(label, query)
                }
                showAddGenreDialog = false
                // Yeni türü hemen seç ve ara
                searchQuery = ""
                activeGenre = label
            }
        )
    }
}

// ─────────────────────────────────────────────
// ÖZEL TÜR EKLEME DİYALOĞU
// ─────────────────────────────────────────────
@Composable
fun AddCustomGenreDialog(
    existingGenres: List<String>,
    onDismiss: () -> Unit,
    onAdd: (label: String, query: String) -> Unit
) {
    var genreInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor  = MaterialTheme.colorScheme.surface,
        shape           = RoundedCornerShape(20.dp),
        title = {
            Text(
                text       = "Tür Ekle",
                fontSize   = 18.sp,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text     = "Aramak istediğin türü yaz. Örn: \"Horror\", \"Romance\", \"Manga\"",
                    fontSize = 13.sp,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )

                // Giriş alanı
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    if (genreInput.isEmpty()) {
                        Text(
                            "Tür adı gir…",
                            fontSize = 14.sp,
                            color    = MaterialTheme.colorScheme.outline
                        )
                    }
                    BasicTextField(
                        value         = genreInput,
                        onValueChange = {
                            genreInput = it
                            errorMessage = null
                        },
                        singleLine    = true,
                        textStyle     = androidx.compose.ui.text.TextStyle(
                            fontSize = 14.sp,
                            color    = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Hata mesajı
                errorMessage?.let { err ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(MaterialTheme.colorScheme.error)
                        )
                        Text(
                            text     = err,
                            fontSize = 12.sp,
                            color    = MaterialTheme.colorScheme.error
                        )
                    }
                }

                // Bilgi notu
                Text(
                    text     = "Not: Sonuç bulunamaması durumunda uyarı alırsın.",
                    fontSize = 11.sp,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val trimmed = genreInput.trim()
                    when {
                        trimmed.isBlank() -> {
                            errorMessage = "Tür adı boş olamaz."
                        }
                        trimmed.length < 2 -> {
                            errorMessage = "En az 2 karakter gir."
                        }
                        existingGenres.any { it.equals(trimmed, ignoreCase = true) } -> {
                            errorMessage = "\"$trimmed\" zaten mevcut."
                        }
                        else -> {
                            // Türkçe karakterleri normalize et, API sorgusu olarak kullan
                            onAdd(trimmed, trimmed)
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = LocalReaColors.current.headerBg,
                    contentColor   = LocalReaColors.current.headerText
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Ekle ve Ara", fontSize = 14.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
            }
        }
    )
}

@Composable
fun DiscoverBookCard(apiBook: ApiBook, onClick: () -> Unit) {
    val coverUrl   = apiBook.coverId?.let { "https://covers.openlibrary.org/b/id/$it-M.jpg" }
    val coverColor = CoverPalette[apiBook.key.hashCode().and(0x7FFFFFFF) % CoverPalette.size]
    val context    = LocalContext.current

    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.65f)
                .clip(RoundedCornerShape(8.dp))
        ) {
            if (coverUrl != null) {
                coil.compose.AsyncImage(
                    model = coil.request.ImageRequest.Builder(context)
                        .data(coverUrl)
                        .size(300, 450)
                        .crossfade(true)
                        .build(),
                    contentDescription = apiBook.title,
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(coverColor),
                    contentAlignment = Alignment.BottomStart
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.6f)))
                            )
                    )
                    Text(
                        text     = apiBook.title,
                        fontSize = 9.sp,
                        color    = Color.White.copy(0.9f),
                        modifier = Modifier.padding(6.dp),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 13.sp
                    )
                }
            }
        }
        Spacer(Modifier.height(5.dp))
        Text(
            text     = apiBook.title,
            fontSize = 10.sp,
            color    = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 14.sp
        )
    }
}


@Composable
fun BookTrackingDialog(
    book:         Book,
    onDismiss:    () -> Unit,
    onSave:       (ReadingStatus, Int, Int, Int, String) -> Unit,
    onAuthorClick: ((String) -> Unit)? = null
) {
    var currentPage  by remember { mutableStateOf(if (book.progress > 0) book.progress.toString() else "") }
    var totalPages   by remember { mutableStateOf(if (book.totalPages > 0) book.totalPages.toString() else "") }
    var rating       by remember { mutableIntStateOf(book.rating) }
    var status       by remember { mutableStateOf(book.status) }
    var notes        by remember { mutableStateOf(book.notes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = MaterialTheme.colorScheme.surface,
        shape            = RoundedCornerShape(20.dp),
        title = {
            Column {
                Text(
                    text       = book.title,
                    fontSize   = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onSurface,
                    maxLines   = 2,
                    overflow   = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))

                Text(
                    text           = book.author,
                    fontSize       = 13.sp,
                    color          = LocalReaColors.current.amber,
                    textDecoration = if (onAuthorClick != null) TextDecoration.Underline else TextDecoration.None,
                    modifier       = if (onAuthorClick != null)
                        Modifier.clickable { onAuthorClick(book.author) }
                    else
                        Modifier
                )
            }
        },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(18.dp)) {


                item {
                    TrackingSection(title = "OKUMA DURUMU") {
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ReadingStatus.entries.forEach { s ->
                                val isSelected = status == s
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isSelected) LocalReaColors.current.headerBg else MaterialTheme.colorScheme.surfaceVariant
                                        )
                                        .clickable { status = s }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text       = s.label,
                                        fontSize   = 10.sp,
                                        color      = if (isSelected) LocalReaColors.current.headerText else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                        textAlign  = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    TrackingSection(title = "SAYFA TAKİBİ") {
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Kaldığım sayfa", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(Modifier.height(4.dp))
                                TrackingNumberField(
                                    value         = currentPage,
                                    onValueChange = { currentPage = it },
                                    placeholder   = "0"
                                )
                            }

                            Text("/", fontSize = 20.sp, color = MaterialTheme.colorScheme.outline, fontWeight = FontWeight.Light)

                            Column(modifier = Modifier.weight(1f)) {
                                Text("Toplam sayfa", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(Modifier.height(4.dp))
                                TrackingNumberField(
                                    value         = totalPages,
                                    onValueChange = { totalPages = it },
                                    placeholder   = "?"
                                )
                            }
                        }

                        val cur   = currentPage.toIntOrNull() ?: 0
                        val total = totalPages.toIntOrNull() ?: 0
                        if (total > 0 && cur > 0) {
                            val pct = ((cur.toFloat() / total) * 100).toInt().coerceIn(0, 100)
                            Spacer(Modifier.height(10.dp))
                            Row(
                                modifier              = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("İlerleme", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$pct%", fontSize = 11.sp, color = LocalReaColors.current.amber, fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(MaterialTheme.colorScheme.outline)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(pct / 100f)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(LocalReaColors.current.amber)
                                )
                            }
                        }
                    }
                }


                item {
                    TrackingSection(title = "PUANLAMA") {
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                (1..5).forEach { star ->
                                    val isFilled = star <= rating
                                    Icon(
                                        // Dolu yıldız için Star, boş için StarOutline — dark modda daha belirgin
                                        imageVector        = if (isFilled) Icons.Default.Star else Icons.Default.StarOutline,
                                        contentDescription = "$star yıldız",
                                        tint               = if (isFilled)
                                            LocalReaColors.current.amber
                                        else
                                        // Koyu modda görünürlük için sabit beyaz alfa; açık modda gri
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clickable {
                                                rating = if (rating == star) 0 else star
                                            }
                                    )
                                }
                            }
                            Text(
                                text = when (rating) {
                                    0 -> "Puansız"
                                    1 -> "Kötü"
                                    2 -> "Vasat"
                                    3 -> "İyi"
                                    4 -> "Harika"
                                    5 -> "Mükemmel"
                                    else -> ""
                                },
                                fontSize   = 13.sp,
                                color      = if (rating > 0) LocalReaColors.current.amber else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }


                item {
                    TrackingSection(title = "NOTLARIM") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(12.dp)
                        ) {
                            if (notes.isEmpty()) {
                                Text(
                                    "Kitap hakkında not ekle…",
                                    fontSize = 13.sp,
                                    color    = MaterialTheme.colorScheme.outline
                                )
                            }
                            BasicTextField(
                                value         = notes,
                                onValueChange = { notes = it },
                                textStyle     = androidx.compose.ui.text.TextStyle(
                                    fontSize = 13.sp,
                                    color    = MaterialTheme.colorScheme.onSurface
                                ),
                                minLines      = 3,
                                maxLines      = 6,
                                modifier      = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        status,
                        currentPage.toIntOrNull() ?: 0,
                        totalPages.toIntOrNull() ?: 0,
                        rating,
                        notes.trim()
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = LocalReaColors.current.headerBg,
                    contentColor   = LocalReaColors.current.headerText
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Kaydet", fontSize = 14.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Vazgeç", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
            }
        }
    )
}


@Composable
fun TrackingSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text          = title,
            fontSize      = 10.sp,
            letterSpacing = 1.5.sp,
            color         = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight    = FontWeight.Medium
        )
        content()
    }
}


@Composable
fun TrackingNumberField(value: String, onValueChange: (String) -> Unit, placeholder: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        if (value.isEmpty()) {
            Text(placeholder, fontSize = 15.sp, color = MaterialTheme.colorScheme.outline)
        }
        BasicTextField(
            value           = value,
            onValueChange   = { if (it.all { c -> c.isDigit() } && it.length <= 6) onValueChange(it) },
            textStyle       = androidx.compose.ui.text.TextStyle(
                fontSize   = 15.sp,
                color      = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            ),
            singleLine      = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier        = Modifier.fillMaxWidth()
        )
    }
}


@Composable
fun BookDetailDialog(
    title:         String,
    author:        String,
    description:   String,
    confirmLabel:  String,
    onDismiss:     () -> Unit,
    onConfirm:     () -> Unit,
    onAuthorClick: ((String) -> Unit)? = null
) {
    AlertDialog(
        onDismissRequest  = onDismiss,
        containerColor    = MaterialTheme.colorScheme.surface,
        shape             = RoundedCornerShape(20.dp),
        title = {
            Column {
                Text(
                    text       = title,
                    fontSize   = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text           = author,
                        fontSize       = 13.sp,
                        color          = LocalReaColors.current.amber,
                        textDecoration = if (onAuthorClick != null) TextDecoration.Underline else TextDecoration.None,
                        modifier       = if (onAuthorClick != null)
                            Modifier.clickable { onAuthorClick(author) }
                        else
                            Modifier
                    )
                    if (onAuthorClick != null) {
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Yazarı ara",
                            tint     = LocalReaColors.current.amber.copy(alpha = 0.6f),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        },
        text = {
            LazyColumn {
                item {
                    Text(
                        text       = description,
                        fontSize   = 14.sp,
                        color      = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 22.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors  = ButtonDefaults.buttonColors(
                    containerColor = LocalReaColors.current.headerBg,
                    contentColor   = LocalReaColors.current.headerText
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(confirmLabel, fontSize = 14.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Kapat", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
            }
        }
    )
}