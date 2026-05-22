package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.LoveLetter
import com.example.ui.theme.*
import com.example.ui.viewmodel.LoveViewModel
import androidx.compose.foundation.border
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun LettersTab(
    viewModel: LoveViewModel,
    modifier: Modifier = Modifier
) {
    val letters by viewModel.letters.collectAsStateWithLifecycle()
    val profile by viewModel.profile.collectAsStateWithLifecycle()

    var showComposeDialog by remember { mutableStateOf(false) }
    var selectedLetterToShow by remember { mutableStateOf<LoveLetter?>(null) }

    val partnerName = profile?.partnerName ?: "Mi Amor"

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // HEADER ROW WITH COMPOSE TRIGGER
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "✉️ Cartas Selladas",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif
                    ),
                    color = RomanticRoseDark
                )
                Text(
                    text = "Abre los sellos de cera para leer tus mensajes",
                    style = MaterialTheme.typography.bodySmall,
                    color = RomanticRoseDark.copy(alpha = 0.6f)
                )
            }

            Button(
                onClick = { showComposeDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = RomanticPinkPrimary),
                modifier = Modifier
                    .shadow(4.dp, RoundedCornerShape(12.dp))
                    .testTag("write_letter_action")
            ) {
                Icon(imageVector = Icons.Default.Brush, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Escribir")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (letters.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.Mail,
                        contentDescription = null,
                        modifier = Modifier.size(72.dp),
                        tint = RomanticPinkSecondary.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "¡Aún no hay cartas selladas!",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = RomanticRoseDark.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "Presiona el botón 'Escribir' para mandarle un hermoso mensaje sorpresa a $partnerName.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = RomanticRoseDark.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 80.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(letters.size) { index ->
                    val letter = letters[index]
                    EnvelopeCard(
                        letter = letter,
                        onClick = {
                            viewModel.toggleLetterOpened(letter)
                            selectedLetterToShow = letter
                        }
                    )
                }
            }
        }
    }

    // 1. IMMERSIVE LETTER VIEW DIALOG
    selectedLetterToShow?.let { letter ->
        val updatedLetter = letters.find { it.id == letter.id } ?: letter
        LetterImmersiveDialog(
            letter = updatedLetter,
            onClose = {
                selectedLetterToShow = null
            },
            onDelete = {
                viewModel.deleteLetter(letter.id)
                selectedLetterToShow = null
            }
        )
    }

    // 2. CREATE / COMPOSE NEW LETTER DIALOG
    if (showComposeDialog) {
        ComposeLetterDialog(
            onDismiss = { showComposeDialog = false },
            onSend = { title, content, paper, seal, photo ->
                viewModel.addLetter(
                    title = title,
                    content = content,
                    paperStyle = paper,
                    waxSeal = seal,
                    photoUri = photo
                )
                showComposeDialog = false
            }
        )
    }
}

@Composable
fun EnvelopeCard(
    letter: LoveLetter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val paperConfig = getPaperConfiguration(letter.paperStyle)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(170.dp)
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .glassmorphicBorder(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("envelope_card_${letter.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = paperConfig.primaryColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // Draw retro letter creases/folds
            Canvas(modifier = Modifier.fillMaxSize()) {
                val path = Path().apply {
                    // Top Fold Triangle
                    moveTo(0f, 0f)
                    lineTo(size.width / 2f, size.height * 0.4f)
                    lineTo(size.width, 0f)

                    // Side Fold Creases
                    moveTo(0f, size.height)
                    lineTo(size.width * 0.35f, size.height * 0.5f)
                    moveTo(size.width, size.height)
                    lineTo(size.width * 0.65f, size.height * 0.5f)
                }
                drawPath(
                    path = path,
                    color = paperConfig.accentTextColor.copy(alpha = 0.15f),
                    style = Stroke(width = 2.dp.toPx())
                )
            }

            // Wax Seal Stamp centered
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .align(Alignment.Center)
                    .shadow(4.dp, CircleShape)
                    .background(getSealColor(letter.waxSeal), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // Dimpled wax texture
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = Color.Black.copy(alpha = 0.1f),
                        radius = size.width * 0.45f,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }

                Icon(
                    imageVector = getSealIcon(letter.waxSeal),
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.size(20.dp)
                )
            }

            // Small text info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomStart)
            ) {
                Text(
                    text = letter.title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = paperConfig.accentTextColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val formatter = SimpleDateFormat("dd MMM yyyy", Locale("es", "ES"))
                Text(
                    text = formatter.format(Date(letter.timestamp)),
                    style = MaterialTheme.typography.labelSmall,
                    color = paperConfig.accentTextColor.copy(alpha = 0.7f)
                )
            }
        }
    }
}

// IMMERSIVE EXPERIENTIAL READING BOOK
@Composable
fun LetterImmersiveDialog(
    letter: LoveLetter,
    onClose: () -> Unit,
    onDelete: () -> Unit
) {
    val paperConfig = getPaperConfiguration(letter.paperStyle)
    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.75f))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            // Stationery Paper Sheet
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .shadow(12.dp, RoundedCornerShape(24.dp))
                    .glassmorphicBorder(RoundedCornerShape(24.dp))
                    .testTag("letter_detail_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = paperConfig.primaryColor)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    // Header operations
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onDelete) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar Carta",
                                tint = paperConfig.accentTextColor.copy(alpha = 0.6f)
                            )
                        }

                        IconButton(
                            onClick = onClose,
                            modifier = Modifier
                                .background(paperConfig.accentTextColor.copy(alpha = 0.1f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = paperConfig.accentTextColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Letter Details (Scrollable paper sheet)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(scrollState),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = letter.title,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold
                            ),
                            color = paperConfig.accentTextColor,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        val formattedDate = SimpleDateFormat("dd MMMM, yyyy - hh:mm a", Locale("es", "ES"))
                            .format(Date(letter.timestamp))

                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.bodySmall,
                            color = paperConfig.accentTextColor.copy(alpha = 0.6f)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Optional polaroid visual inside letter
                        if (!letter.photoUri.isNullOrEmpty()) {
                            PolaroidContainer(photoUri = letter.photoUri)
                            Spacer(modifier = Modifier.height(24.dp))
                        }

                        // Writing contents with stationary spacing rules
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(paperConfig.accentTextColor.copy(alpha = 0.15f))
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = letter.content,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = FontFamily.Serif,
                                lineHeight = 26.sp,
                                letterSpacing = 0.5.sp
                            ),
                            color = paperConfig.accentTextColor,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(30.dp))

                        // Wax seal stamp signature
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .shadow(4.dp, CircleShape)
                                .background(getSealColor(letter.waxSeal), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getSealIcon(letter.waxSeal),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Sellado con Amor",
                            style = MaterialTheme.typography.labelSmall,
                            color = paperConfig.accentTextColor.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }
        }
    }
}

// STYLISH RETRO POLAROID CONTAINER
@Composable
fun PolaroidContainer(photoUri: String) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .shadow(6.dp, RoundedCornerShape(2.dp))
            .background(Color.White)
            .padding(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(2.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(Color(0xFFE9E9E9))
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(photoUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Recuerdo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "✨ Nuestro Instante",
                fontFamily = FontFamily.Cursive,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

// 2. COMPOSE LETTER DIALOG
@Composable
fun ComposeLetterDialog(
    onDismiss: () -> Unit,
    onSend: (title: String, content: String, paperStyle: String, waxSeal: String, photoUri: String?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedPaper by remember { mutableStateOf("romance_pink") }
    var selectedSeal by remember { mutableStateOf("heart") }
    var photoUriInput by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Escribir Nueva Carta de Amor ✏️",
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                color = RomanticRoseDark
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .sizeIn(maxHeight = 420.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título de la Carta") },
                    placeholder = { Text("Ej: Para mi amor, Para los días de lluvia...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Cuerpo del Mensaje") },
                    placeholder = { Text("Escribe tus sentimientos más profundos aquí...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    maxLines = 10
                )

                // Choose stationery style
                Column {
                    Text(
                        "Tipo de Papel de Carta",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = RomanticRoseDark
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        PaperOptionButton(
                            styleName = "romance_pink",
                            color = Color(0xFFFFECEF),
                            isSelected = selectedPaper == "romance_pink",
                            onClick = { selectedPaper = "romance_pink" }
                        )
                        PaperOptionButton(
                            styleName = "vintage_beige",
                            color = Color(0xFFFAF2E3),
                            isSelected = selectedPaper == "vintage_beige",
                            onClick = { selectedPaper = "vintage_beige" }
                        )
                        PaperOptionButton(
                            styleName = "sweet_lavender",
                            color = Color(0xFFF1EAFF),
                            isSelected = selectedPaper == "sweet_lavender",
                            onClick = { selectedPaper = "sweet_lavender" }
                        )
                        PaperOptionButton(
                            styleName = "midnight_dark",
                            color = Color(0xFF352D30),
                            isSelected = selectedPaper == "midnight_dark",
                            onClick = { selectedPaper = "midnight_dark" }
                        )
                    }
                }

                // Choose wax seal stamp
                Column {
                    Text(
                        "Estilo de Sello de Cera",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = RomanticRoseDark
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SealOptionButton(
                            seal = "heart",
                            icon = Icons.Default.Favorite,
                            isSelected = selectedSeal == "heart",
                            onClick = { selectedSeal = "heart" }
                        )
                        SealOptionButton(
                            seal = "rose",
                            icon = Icons.Default.FilterVintage,
                            isSelected = selectedSeal == "rose",
                            onClick = { selectedSeal = "rose" }
                        )
                        SealOptionButton(
                            seal = "forever",
                            icon = Icons.Default.AllInclusive,
                            isSelected = selectedSeal == "forever",
                            onClick = { selectedSeal = "forever" }
                        )
                        SealOptionButton(
                            seal = "butterfly",
                            icon = Icons.Default.Spa,
                            isSelected = selectedSeal == "butterfly",
                            onClick = { selectedSeal = "butterfly" }
                        )
                    }
                }

                // Polaroid attachment link (URL/Path)
                OutlinedTextField(
                    value = photoUriInput,
                    onValueChange = { photoUriInput = it },
                    label = { Text("Enlace de Foto Polaroid (Opcional)") },
                    placeholder = { Text("Escribe/copia la URL de un recuerdo") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && content.isNotBlank()) {
                        onSend(
                            title,
                            content,
                            selectedPaper,
                            selectedSeal,
                            photoUriInput.ifBlank { null }
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = RomanticPinkPrimary)
            ) {
                Text("Sellar y Enviar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun PaperOptionButton(
    styleName: String,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .shadow(2.dp, CircleShape)
            .background(color, CircleShape)
            .clickable { onClick() }
            .border(
                width = if (isSelected) 3.dp else 0.dp,
                color = if (isSelected) RomanticRoseDark else Color.Transparent,
                shape = CircleShape
            )
    )
}

@Composable
fun SealOptionButton(
    seal: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .shadow(2.dp, CircleShape)
            .background(getSealColor(seal), CircleShape)
            .clickable { onClick() }
            .border(
                width = if (isSelected) 3.dp else 0.dp,
                color = if (isSelected) Color.White else Color.Transparent,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.9f),
            modifier = Modifier.size(16.dp)
        )
    }
}


// PAPER CONFIGS MATCHING STYLES
data class PaperConfiguration(
    val primaryColor: Color,
    val accentTextColor: Color
)

fun getPaperConfiguration(style: String): PaperConfiguration {
    return when (style) {
        "romance_pink" -> PaperConfiguration(
            primaryColor = Color(0xCCFFECEF), // 80% translucent rose glass
            accentTextColor = Color(0xFF8C304B)
        )
        "vintage_beige" -> PaperConfiguration(
            primaryColor = Color(0xCCFAF2E3), // 80% translucent vintage glass
            accentTextColor = Color(0xFF5D4037)
        )
        "sweet_lavender" -> PaperConfiguration(
            primaryColor = Color(0xCCF1EAFF), // 80% translucent lavender glass
            accentTextColor = Color(0xFF4A148C)
        )
        "midnight_dark" -> PaperConfiguration(
            primaryColor = Color(0xCC352D30), // 80% translucent charcoal glass
            accentTextColor = Color(0xFFFFF0F2)
        )
        else -> PaperConfiguration(
            primaryColor = Color(0xCCFFECEF),
            accentTextColor = Color(0xFF8C304B)
        )
    }
}

// SEAL ICON HELPERS
fun getSealIcon(seal: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (seal) {
        "heart" -> Icons.Default.Favorite
        "rose" -> Icons.Default.FilterVintage
        "forever" -> Icons.Default.AllInclusive
        "butterfly" -> Icons.Default.Spa
        else -> Icons.Default.Favorite
    }
}

fun getSealColor(seal: String): Color {
    return when (seal) {
        "heart" -> WaxSealRed
        "rose" -> WaxSealRose
        "forever" -> WaxSealGold
        "butterfly" -> Color(0xFF537166)
        else -> WaxSealRed
    }
}
