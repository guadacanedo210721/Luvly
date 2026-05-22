package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Milestone
import com.example.ui.theme.*
import com.example.ui.viewmodel.LoveViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StoryTab(
    viewModel: LoveViewModel,
    modifier: Modifier = Modifier
) {
    val milestones by viewModel.milestones.collectAsStateWithLifecycle()
    var showAddMilestoneDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // HEADER BLOCK
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "📜 Nuestra Historia",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif
                        ),
                        color = RomanticRoseDark
                    )
                    Text(
                        text = "El mapa de aventuras, risas y fechas importantes",
                        style = MaterialTheme.typography.bodySmall,
                        color = RomanticRoseDark.copy(alpha = 0.6f)
                    )
                }

                IconButton(
                    onClick = { showAddMilestoneDialog = true },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = RomanticPinkPrimary),
                    modifier = Modifier
                        .shadow(4.dp, CircleShape)
                        .testTag("add_milestone_button")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Añadir Hito", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (milestones.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Timeline,
                            contentDescription = null,
                            modifier = Modifier.size(72.dp),
                            tint = RomanticPinkPrimary.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Nuestra línea de tiempo está vacía",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = RomanticRoseDark
                        )
                        Text(
                            text = "Añade hitos importantes como vuestra primera cita o vuestro primer viaje juntos.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = RomanticRoseDark.copy(alpha = 0.6f),
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            } else {
                // VERTICAL TIMELINE LAYOUT
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    // Vertical Connection Line
                    val lineColor = RomanticPinkPrimary.copy(alpha = 0.4f)
                    Canvas(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(44.dp)
                            .padding(start = 20.dp)
                    ) {
                        drawLine(
                            color = lineColor,
                            start = Offset(0f, 0f),
                            end = Offset(0f, size.height),
                            strokeWidth = 3.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f)
                        )
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 90.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        itemsIndexed(milestones) { index, milestone ->
                            TimelineItem(
                                milestone = milestone,
                                index = index,
                                onDelete = { viewModel.deleteMilestone(milestone.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    // ADD NEW MILESTONE DIALOG
    if (showAddMilestoneDialog) {
        AddMilestoneDialog(
            onDismiss = { showAddMilestoneDialog = false },
            onSave = { title, dateText, desc, iconName, timestamp ->
                viewModel.addMilestone(title, dateText, desc, iconName, timestamp)
                showAddMilestoneDialog = false
            }
        )
    }
}

@Composable
fun TimelineItem(
    milestone: Milestone,
    index: Int,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag("timeline_item_${milestone.id}"),
        verticalAlignment = Alignment.Top
    ) {
        // Connected Icon Node Circle
        Box(
            modifier = Modifier
                .size(40.dp)
                .shadow(2.dp, CircleShape)
                .background(RomanticPinkPrimary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = getMilestoneIcon(milestone.iconName),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Info Card
        Card(
            modifier = Modifier
                .weight(1f)
                .shadow(3.dp, RoundedCornerShape(16.dp))
                .glassmorphicBorder(RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = milestone.dateText,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = RomanticPinkPrimary
                    )

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Borrar",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = milestone.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold
                    ),
                    color = RomanticRoseDark
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = milestone.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = RomanticRoseDark.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun AddMilestoneDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, dateText: String, desc: String, iconName: String, timestamp: Long) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf("favorite") }
    
    var timestampState by remember { mutableStateOf(System.currentTimeMillis()) }
    val localContext = LocalContext.current
    val calendarInstance = Calendar.getInstance().apply { timeInMillis = timestampState }

    val formatter = SimpleDateFormat("dd MMM yyyy", Locale("es", "ES"))
    var dateText by remember { mutableStateOf(formatter.format(Date(timestampState))) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Registrar Hito de Amor 📜",
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
                    label = { Text("Título de la Aventura") },
                    placeholder = { Text("Ej: Nuestra primera cita...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Date Picker trigger button
                OutlinedButton(
                    onClick = {
                        DatePickerDialog(
                            localContext,
                            { _, year, month, dayOfMonth ->
                                val selectedCal = Calendar.getInstance().apply {
                                    set(Calendar.YEAR, year)
                                    set(Calendar.MONTH, month)
                                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                }
                                timestampState = selectedCal.timeInMillis
                                dateText = formatter.format(Date(timestampState))
                            },
                            calendarInstance.get(Calendar.YEAR),
                            calendarInstance.get(Calendar.MONTH),
                            calendarInstance.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Fecha: $dateText")
                }

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Pequeña reseña de lo que pasó") },
                    placeholder = { Text("Escribe una breve memoria dulce...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    maxLines = 3
                )

                // Choose Icon Row
                Column {
                    Text(
                        "Icono del Hito",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = RomanticRoseDark
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CategoryIconButton(
                            iconName = "favorite",
                            icon = Icons.Default.Favorite,
                            isSelected = selectedIcon == "favorite",
                            onClick = { selectedIcon = "favorite" }
                        )
                        CategoryIconButton(
                            iconName = "flight",
                            icon = Icons.Default.Flight,
                            isSelected = selectedIcon == "flight",
                            onClick = { selectedIcon = "flight" }
                        )
                        CategoryIconButton(
                            iconName = "cafe",
                            icon = Icons.Default.LocalCafe,
                            isSelected = selectedIcon == "cafe",
                            onClick = { selectedIcon = "cafe" }
                        )
                        CategoryIconButton(
                            iconName = "explore",
                            icon = Icons.Default.Explore,
                            isSelected = selectedIcon == "explore",
                            onClick = { selectedIcon = "explore" }
                        )
                        CategoryIconButton(
                            iconName = "event",
                            icon = Icons.Default.Event,
                            isSelected = selectedIcon == "event",
                            onClick = { selectedIcon = "event" }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && desc.isNotBlank()) {
                        onSave(title, dateText, desc, selectedIcon, timestampState)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = RomanticPinkPrimary)
            ) {
                Text("Guardar Hito")
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
fun CategoryIconButton(
    iconName: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .shadow(2.dp, CircleShape)
            .background(
                if (isSelected) RomanticPinkPrimary else MaterialTheme.colorScheme.surfaceVariant,
                CircleShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) Color.White else RomanticRoseDark.copy(alpha = 0.6f),
            modifier = Modifier.size(18.dp)
        )
    }
}

fun getMilestoneIcon(name: String): ImageVector {
    return when (name) {
        "favorite" -> Icons.Default.Favorite
        "flight" -> Icons.Default.Flight
        "cafe" -> Icons.Default.LocalCafe
        "explore" -> Icons.Default.Explore
        "event" -> Icons.Default.Event
        else -> Icons.Default.Favorite
    }
}
