package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.LoveSong
import com.example.ui.theme.*
import com.example.ui.viewmodel.LoveViewModel

@Composable
fun MusicTab(
    viewModel: LoveViewModel,
    modifier: Modifier = Modifier
) {
    val songs by viewModel.songs.collectAsStateWithLifecycle()
    val letters by viewModel.letters.collectAsStateWithLifecycle()

    var showAddSongDialog by remember { mutableStateOf(false) }
    var selectedSong by remember { mutableStateOf<LoveSong?>(null) }
    var isVinylPlaying by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Set first song as default selected if none active
    LaunchedEffect(songs) {
        if (selectedSong == null && songs.isNotEmpty()) {
            selectedSong = songs.first()
        }
    }

    // Media Player state
    var isMediaBuffering by remember { mutableStateOf(false) }

    // Control MediaPlayer lifecycle & playback based on `isVinylPlaying` and `selectedSong`
    val sSong = selectedSong
    DisposableEffect(isVinylPlaying, sSong) {
        var player: android.media.MediaPlayer? = null

        if (isVinylPlaying && sSong != null) {
            val rawUrl = sSong.spotifyUrl ?: ""
            val isPlayableDirect = rawUrl.endsWith(".mp3", ignoreCase = true) ||
                                   rawUrl.endsWith(".ogg", ignoreCase = true) ||
                                   rawUrl.endsWith(".wav", ignoreCase = true) ||
                                   rawUrl.contains("soundhelix.com", ignoreCase = true)

            // If it's a direct audio file, use it. Otherwise, use a beautiful romantic piano track
            val playUrl = if (isPlayableDirect) {
                rawUrl
            } else {
                // Fallback to a sweet romantic piano/instrumental stream from a reliable server
                "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3"
            }

            try {
                isMediaBuffering = true
                val mp = android.media.MediaPlayer().apply {
                    setAudioAttributes(
                        android.media.AudioAttributes.Builder()
                            .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    setDataSource(playUrl)
                    setOnPreparedListener { player ->
                        try {
                            isMediaBuffering = false
                            player.isLooping = true
                            player.start()
                        } catch (e: Exception) {
                            isMediaBuffering = false
                        }
                    }
                    setOnErrorListener { _, _, _ ->
                        try {
                            isMediaBuffering = false
                        } catch (e: Exception) {}
                        true
                    }
                    prepareAsync()
                }
                player = mp
            } catch (e: Exception) {
                isMediaBuffering = false
            }
        }

        onDispose {
            isMediaBuffering = false
            try {
                player?.reset()
            } catch (e: Exception) {}
            try {
                player?.release()
            } catch (e: Exception) {}
        }
    }

    // Vinyl rotation angle
    val infiniteTransition = rememberInfiniteTransition(label = "vinyl_rotate")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "vinyl_angle"
    )

    val activeRotation = if (isVinylPlaying && !isMediaBuffering) rotationAngle else 0f

    // We extract any available photo URLs from letters to populate the Polaroid memory lane!
    // This connects photos cleanly to letters so they can see all their polaroids here.
    val photoUrlsList = remember(letters) {
        letters.mapNotNull { it.photoUri }.filter { it.isNotBlank() }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // HEADER TITLE
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "🎵 Nuestra Playlist y Fotos",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif
                    ),
                    color = RomanticRoseDark
                )
                Text(
                    text = "Nuestra propia música y recuerdos congelados en el tiempo",
                    style = MaterialTheme.typography.bodySmall,
                    color = RomanticRoseDark.copy(alpha = 0.6f)
                )
            }
        }

        // 1. DYNAMIC ROTATING RETRO VINYL PHONOGRAPH PLAYER
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(6.dp, RoundedCornerShape(24.dp))
                    .glassmorphicBorder(RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "🎧 Tocadiscos Retro",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif
                        ),
                        color = RomanticRoseDark
                    )

                    // Phonograph Case & Vinyl Container
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(160.dp)
                                .shadow(8.dp, CircleShape)
                                .clip(CircleShape)
                                .rotate(activeRotation)
                                .background(Color(0xFF1E1E1E))
                                .testTag("vinyl_disk"),
                            contentAlignment = Alignment.Center
                        ) {
                            // Vinyl grooves
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawCircle(
                                    color = Color.Black.copy(alpha = 0.2f),
                                    style = Stroke(width = 1.dp.toPx())
                                )
                                drawCircle(
                                    color = Color.White.copy(alpha = 0.08f),
                                    radius = size.width * 0.42f,
                                    style = Stroke(width = 1.dp.toPx())
                                )
                                drawCircle(
                                    color = Color.White.copy(alpha = 0.08f),
                                    radius = size.width * 0.35f,
                                    style = Stroke(width = 1.dp.toPx())
                                )
                                drawCircle(
                                    color = Color.White.copy(alpha = 0.12f),
                                    radius = size.width * 0.25f,
                                    style = Stroke(width = 1.dp.toPx())
                                )
                            }

                            // Center label (Color wheel)
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .background(RomanticPinkPrimary, CircleShape)
                                    .border(2.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isMediaBuffering) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .background(Color.White, CircleShape)
                                    )
                                }
                            }
                        }
                    }

                    // Playing Song Meta
                    selectedSong?.let { song ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = song.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = RomanticRoseDark,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "por ${song.artist}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = RomanticRoseDark.copy(alpha = 0.7f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (!song.thoughts.isNullOrEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "\"${song.thoughts}\"",
                                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Serif),
                                    color = RomanticRoseDark.copy(alpha = 0.8f),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }

                        // Player buttons
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Play/Pause button for local simulation flow
                            Row(
                                modifier = Modifier.clickable { isVinylPlaying = !isVinylPlaying },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                IconButton(
                                    onClick = { isVinylPlaying = !isVinylPlaying },
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(RomanticPinkPrimary, CircleShape)
                                        .shadow(4.dp, CircleShape)
                                ) {
                                    Icon(
                                        imageVector = if (isVinylPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = "Play/Pause",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Text(
                                    text = if (isVinylPlaying) "Pausar música" else "Escuchar avance",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = RomanticRoseDark.copy(alpha = 0.8f)
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Play choice buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // 1. Escuchar en Spotify
                                val spotifySearchUrl = if (!song.spotifyUrl.isNullOrEmpty() && song.spotifyUrl.contains("spotify.com", ignoreCase = true)) {
                                    song.spotifyUrl
                                } else {
                                    "https://open.spotify.com/search/${Uri.encode("${song.title} ${song.artist}")}"
                                }

                                Button(
                                    onClick = {
                                        try {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(spotifySearchUrl))
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "No se pudo abrir Spotify", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1DB954)),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MusicNote,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "Escuchar en Spotify",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                // 2. Ver en YouTube
                                val youtubeSearchUrl = if (!song.spotifyUrl.isNullOrEmpty() && (song.spotifyUrl.contains("youtube.com", ignoreCase = true) || song.spotifyUrl.contains("youtu.be", ignoreCase = true) || song.spotifyUrl.contains("shorts", ignoreCase = true))) {
                                    song.spotifyUrl
                                } else {
                                    "https://www.youtube.com/results?search_query=${Uri.encode("${song.title} ${song.artist}")}"
                                }

                                Button(
                                    onClick = {
                                        try {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(youtubeSearchUrl))
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "No se pudo abrir YouTube", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF0000)),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "Ver en YouTube",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    } ?: run {
                        Text(
                            text = "Selecciona una canción de la lista",
                            style = MaterialTheme.typography.bodyMedium,
                            color = RomanticRoseDark.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }

        // 2. LOVE LETTERS POLAROID MEMORIES LANE
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(24.dp))
                    .glassmorphicBorder(RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "📷 Recuerdos Fotográficos",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif
                        ),
                        color = RomanticRoseDark
                    )

                    if (photoUrlsList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Añade fotos polaroid creando cartas y adjuntando enlaces a imágenes. ¡Se mostrarán aquí con un marco vintage!",
                                style = MaterialTheme.typography.bodySmall,
                                color = RomanticRoseDark.copy(alpha = 0.5f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 20.dp)
                            )
                        }
                    } else {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            items(photoUrlsList) { url ->
                                PolaroidFrame(url = url)
                            }
                        }
                    }
                }
            }
        }

        // 3. PLAYLISTS TRACK RECORD LIST OPERATIONS
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🎶 Nuestra Colección Musical",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif
                    ),
                    color = RomanticRoseDark
                )

                IconButton(
                    onClick = { showAddSongDialog = true },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = RomanticPinkSecondary)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Añadir Canción", tint = Color.White)
                }
            }
        }

        if (songs.isEmpty()) {
            item {
                Text(
                    text = "Aún no hay canciones de amor agregadas.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = RomanticRoseDark.copy(alpha = 0.5f)
                )
            }
        } else {
            items(songs) { song ->
                SongTrackRow(
                    song = song,
                    isActive = selectedSong?.id == song.id,
                    onClick = {
                        selectedSong = song
                        isVinylPlaying = true
                    },
                    onDelete = { viewModel.deleteSong(song.id) }
                )
            }
        }
    }

    // ADD NEW SONG DIALOG
    if (showAddSongDialog) {
        AddSongDialog(
            onDismiss = { showAddSongDialog = false },
            onSave = { title, artist, url, thoughts ->
                viewModel.addSong(title, artist, url, thoughts)
                showAddSongDialog = false
            }
        )
    }
}

@Composable
fun PolaroidFrame(url: String) {
    Card(
        modifier = Modifier
            .width(130.dp)
            .shadow(4.dp, RoundedCornerShape(2.dp))
            .background(Color.White)
            .padding(6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(2.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .background(Color(0xFFE9E9E9))
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(url)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Recuerdo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Te amo ❤️",
                fontFamily = FontFamily.Cursive,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray
            )
        }
    }
}

@Composable
fun SongTrackRow(
    song: LoveSong,
    isActive: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .glassmorphicBorder(RoundedCornerShape(16.dp))
            .testTag("song_row_${song.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) RomanticPinkPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isActive) RomanticPinkPrimary else RomanticPinkSecondary.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isActive) Icons.Default.PlayArrow else Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = if (isActive) Color.White else RomanticRoseDark
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = RomanticRoseDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.labelSmall,
                    color = RomanticRoseDark.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Borrar canción",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun AddSongDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, artist: String, spotifyUrl: String?, thoughts: String?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var artist by remember { mutableStateOf("") }
    var link by remember { mutableStateOf("") }
    var thoughts by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Añadir Canción de Amor 🎵",
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                color = RomanticRoseDark
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título de la Canción") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = artist,
                    onValueChange = { artist = it },
                    label = { Text("Nombre del Artista") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = link,
                    onValueChange = { link = it },
                    label = { Text("Enlace (Spotify, YouTube, etc.)") },
                    placeholder = { Text("https://open.spotify.com...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = thoughts,
                    onValueChange = { thoughts = it },
                    label = { Text("¿Por qué esta canción? (Opcional)") },
                    placeholder = { Text("Ej: Me recuerda a nuestro viaje en coche...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && artist.isNotBlank()) {
                        onSave(title, artist, link.ifBlank { null }, thoughts.ifBlank { null })
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = RomanticPinkPrimary)
            ) {
                Text("Añadir")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
