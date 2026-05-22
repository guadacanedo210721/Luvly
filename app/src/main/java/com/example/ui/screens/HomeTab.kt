package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.LoveReason
import com.example.data.RelationshipProfile
import com.example.ui.theme.*
import com.example.ui.viewmodel.LoveViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun HomeTab(
    viewModel: LoveViewModel,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val countdown by viewModel.countdownState.collectAsStateWithLifecycle()
    val reasons by viewModel.reasons.collectAsStateWithLifecycle()

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showReasonsListDialog by remember { mutableStateOf(false) }
    var showAddReasonDialog by remember { mutableStateOf(false) }

    // Active reason state pulled from jar
    var activeReasonText by remember { mutableStateOf<String?>(null) }
    var activeReasonId by remember { mutableStateOf<Int?>(null) }
    var jarShakeTrigger by remember { mutableStateOf(0) }

    // Shake animation for jar
    val jarRotation by animateFloatAsState(
        targetValue = if (jarShakeTrigger % 2 == 1) 15f else if (jarShakeTrigger > 0) -15f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = Spring.StiffnessMedium),
        finishedListener = {
            if (jarShakeTrigger > 0 && jarShakeTrigger < 4) {
                jarShakeTrigger++
            } else {
                jarShakeTrigger = 0
            }
        }
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. GREETING CARD & PROFILE
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(24.dp))
                    .glassmorphicBorder(RoundedCornerShape(24.dp))
                    .testTag("greeting_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    IconButton(
                        onClick = { showEditProfileDialog = true },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .testTag("edit_profile_button"),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar Perfil",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 36.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "❤️ Nuestro Diario",
                            style = MaterialTheme.typography.titleMedium,
                            color = RomanticRoseDark.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        val userName = profile?.userName ?: "Tú"
                        val partnerName = profile?.partnerName ?: "Mi Amor"

                        Text(
                            text = "$userName & $partnerName",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif
                            ),
                            color = RomanticRoseDark
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale("es", "ES"))
                        val anniversaryDateStr = profile?.let {
                            sdf.format(Date(it.anniversaryTimestamp))
                        } ?: "14 de Octubre"

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CalendarToday,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = RomanticRoseDark.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Desde el $anniversaryDateStr",
                                style = MaterialTheme.typography.bodyMedium,
                                color = RomanticRoseDark.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }

        // 2. LIVE COUNTDOWN SECTION
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(28.dp))
                    .glassmorphicBorder(RoundedCornerShape(28.dp)),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Tiempo de Amor Recíproco ✨",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = RomanticRoseDark,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CountdownUnit(value = countdown.days, label = "Días")
                        CountdownDivider()
                        CountdownUnit(value = countdown.hours, label = "Hrs")
                        CountdownDivider()
                        CountdownUnit(value = countdown.minutes, label = "Min")
                        CountdownDivider()
                        CountdownUnit(value = countdown.seconds, label = "Seg", highlight = true)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "¡Coleccionando risas y momentos inolvidables!",
                        style = MaterialTheme.typography.bodySmall,
                        color = RomanticRoseDark.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // 3. HEART JAR (COFRE DE RAZONES)
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
                        .padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "🏺 El Cofre de las Razones",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif
                            ),
                            color = RomanticRoseDark
                        )
                        Text(
                            text = "Toca el cofre para descubrir una razón de por qué te amo",
                            style = MaterialTheme.typography.bodySmall,
                            color = RomanticRoseDark.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                    }

                    // Interactive shaking glass jar illustration
                    Box(
                        modifier = Modifier
                            .size(170.dp)
                            .rotate(jarRotation)
                            .clip(RoundedCornerShape(24.dp))
                            .clickable {
                                // Shaking action
                                if (reasons.isNotEmpty()) {
                                    jarShakeTrigger = 1
                                    val randomReason = reasons.random()
                                    activeReasonText = randomReason.reasonText
                                    activeReasonId = randomReason.id
                                } else {
                                    activeReasonText = "Aún no hay razones escritas. ¡Escribe algunas increíbles juntos!"
                                }
                            }
                            .testTag("shake_jar_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        // Drawing retro heart jar
                        val outlineColor = MaterialTheme.colorScheme.primary
                        Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                            // Jar Lid
                            drawRoundRect(
                                color = outlineColor,
                                topLeft = androidx.compose.ui.geometry.Offset(size.width * 0.3f, 0f),
                                size = androidx.compose.ui.geometry.Size(size.width * 0.4f, size.height * 0.08f),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx()),
                                style = Stroke(width = 4.dp.toPx())
                            )
                            // Jar Neck
                            drawRect(
                                color = outlineColor,
                                topLeft = androidx.compose.ui.geometry.Offset(size.width * 0.35f, size.height * 0.08f),
                                size = androidx.compose.ui.geometry.Size(size.width * 0.3f, size.height * 0.08f),
                                style = Stroke(width = 4.dp.toPx())
                            )
                            // Jar Body
                            val jarPath = Path().apply {
                                moveTo(size.width * 0.35f, size.height * 0.16f)
                                cubicTo(
                                    size.width * 0.1f, size.height * 0.2f,
                                    size.width * 0.05f, size.height * 0.5f,
                                    size.width * 0.12f, size.height * 0.9f
                                )
                                cubicTo(
                                    size.width * 0.15f, size.height * 0.98f,
                                    size.width * 0.85f, size.height * 0.98f,
                                    size.width * 0.88f, size.height * 0.9f
                                )
                                cubicTo(
                                    size.width * 0.95f, size.height * 0.5f,
                                    size.width * 0.9f, size.height * 0.2f,
                                    size.width * 0.65f, size.height * 0.16f
                                )
                            }
                            drawPath(
                                path = jarPath,
                                color = outlineColor,
                                style = Stroke(width = 4.dp.toPx())
                            )
                        }

                        // Hearts inside jar that hover/float!
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            modifier = Modifier
                                .size(54.dp)
                                .offset(y = 15.dp),
                            tint = CoralPink
                        )

                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            modifier = Modifier
                                .size(28.dp)
                                .offset(x = (-30).dp, y = (-10).dp),
                            tint = RomanticPinkSecondary
                        )

                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            modifier = Modifier
                                .size(24.dp)
                                .offset(x = 35.dp, y = (-5).dp),
                            tint = WaxSealRed.copy(alpha = 0.8f)
                        )
                    }

                    // Floating/Displaying opened paper note from jar
                    AnimatedVisibility(
                        visible = activeReasonText != null,
                        enter = fadeIn() + expandVertically() + scaleIn(),
                        exit = fadeOut() + shrinkVertically() + scaleOut()
                    ) {
                        activeReasonText?.let { reasonText ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(4.dp, RoundedCornerShape(16.dp))
                                    .testTag("pushed_reason_card"),
                                colors = CardDefaults.cardColors(
                                    containerColor = LightPeach
                                ),
                                shape = RoundedCornerShape(16.dp),
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        IconButton(
                                            onClick = {
                                                activeReasonText = null
                                                activeReasonId = null
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Cerrar nota",
                                                tint = RomanticRoseDark
                                            )
                                        }
                                    }

                                    Icon(
                                        imageVector = Icons.Default.Favorite,
                                        contentDescription = null,
                                        tint = WaxSealRed,
                                        modifier = Modifier.size(26.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = reasonText,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontFamily = FontFamily.Serif,
                                            fontWeight = FontWeight.Medium,
                                            lineHeight = 22.sp
                                        ),
                                        color = RomanticRoseDark,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    // Mini jar operations row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { showAddReasonDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("add_reason_trigger"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Añadir", maxLines = 1)
                        }

                        OutlinedButton(
                            onClick = { showReasonsListDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("view_reasons_trigger"),
                            colors = ButtonDefaults.outlinedButtonColors()
                        ) {
                            Icon(Icons.Default.FavoriteBorder, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Todas(${reasons.size})", maxLines = 1)
                        }
                    }
                }
            }
        }
    }

    // DIALOGS & SHEET IMPLEMENTATION

    // 1. EDIT PROFILE DIALOG
    if (showEditProfileDialog) {
        var tempUser by remember { mutableStateOf(profile?.userName ?: "") }
        var tempPartner by remember { mutableStateOf(profile?.partnerName ?: "") }
        var tempTimestamp by remember { mutableStateOf(profile?.anniversaryTimestamp ?: System.currentTimeMillis()) }
        val context = LocalContext.current

        val cal = Calendar.getInstance().apply {
            timeInMillis = tempTimestamp
        }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("Configurar Vínculo ❤️", fontFamily = FontFamily.Serif, color = RomanticRoseDark) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = tempUser,
                        onValueChange = { tempUser = it },
                        label = { Text("Tu Nombre") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = tempPartner,
                        onValueChange = { tempPartner = it },
                        label = { Text("Tu Pareja") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    val dateSdf = SimpleDateFormat("dd / MM / yyyy", Locale.getDefault())
                    OutlinedButton(
                        onClick = {
                            DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    val newCal = Calendar.getInstance().apply {
                                        set(Calendar.YEAR, year)
                                        set(Calendar.MONTH, month)
                                        set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                    }
                                    tempTimestamp = newCal.timeInMillis
                                },
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Event, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Aniversario: ${dateSdf.format(Date(tempTimestamp))}")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateProfile(
                            userName = tempUser.ifEmpty { "Tú" },
                            partnerName = tempPartner.ifEmpty { "Mi Amor" },
                            anniversaryTimestamp = tempTimestamp
                        )
                        showEditProfileDialog = false
                    }
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // 2. ADD REASON DIALOG
    if (showAddReasonDialog) {
        var reasonFormText by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddReasonDialog = false },
            title = { Text("Escribir nueva razón ✨", fontFamily = FontFamily.Serif) },
            text = {
                OutlinedTextField(
                    value = reasonFormText,
                    onValueChange = { reasonFormText = it },
                    label = { Text("Escribe aquí por qué le amas...") },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    maxLines = 4
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (reasonFormText.isNotBlank()) {
                            viewModel.addReason(reasonFormText)
                            reasonFormText = ""
                            showAddReasonDialog = false
                        }
                    }
                ) {
                    Text("Añadir Razón")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddReasonDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // 3. ALL REASONS LIST SHEET
    if (showReasonsListDialog) {
        AlertDialog(
            onDismissRequest = { showReasonsListDialog = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Todas las razones (${reasons.size}) 🏺", fontFamily = FontFamily.Serif, fontSize = 20.sp, color = RomanticRoseDark)
                    IconButton(onClick = { showReasonsListDialog = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }
            },
            text = {
                Box(modifier = Modifier.sizeIn(maxHeight = 400.dp)) {
                    if (reasons.isEmpty()) {
                        Text("No hay razones actualmente. ¡Escribe algunas increíbles!", textAlign = TextAlign.Center)
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(reasons) { item ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Favorite,
                                            contentDescription = null,
                                            tint = CoralPink,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = item.reasonText,
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.weight(1f),
                                            color = RomanticRoseDark
                                        )
                                        IconButton(
                                            onClick = { viewModel.deleteReason(item.id) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Borrar",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showReasonsListDialog = false }) {
                    Text("Cerrar")
                }
            }
        )
    }
}

@Composable
fun CountdownUnit(value: Long, label: String, highlight: Boolean = false) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = String.format("%02d", kotlin.math.abs(value)),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            ),
            color = if (highlight) WaxSealRed else RomanticRoseDark
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = RomanticRoseDark.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun CountdownDivider() {
    Text(
        text = ":",
        style = MaterialTheme.typography.headlineMedium.copy(
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        ),
        color = RomanticRoseDark.copy(alpha = 0.5f),
        modifier = Modifier.offset(y = (-4).dp)
    )
}
