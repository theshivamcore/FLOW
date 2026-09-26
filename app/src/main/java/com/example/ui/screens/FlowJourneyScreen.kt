package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MilestoneItem
import com.example.ui.theme.*
import com.example.ui.viewmodel.FlowViewModel

@Composable
fun FlowJourneyScreen(viewModel: FlowViewModel) {
    val milestones by viewModel.milestones.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text("LONG-TERM MASTERY", style = MaterialTheme.typography.labelMedium, color = FlowTeal, letterSpacing = 1.sp)
                Text("FLOW Journey", style = MaterialTheme.typography.headlineLarge, color = TextHigh)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "A visual chronicle of your compounding consistency and monumental milestones.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMedium
                )
            }
        }

        // Unlocked Count Summary Card
        item {
            val unlockedCount = milestones.count { it.isUnlocked }
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkBorder)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(FlowTeal.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = FlowTeal, modifier = Modifier.size(30.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("$unlockedCount / ${milestones.size} Milestones Conquered", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextHigh)
                        val totalProg = if (milestones.isNotEmpty()) unlockedCount.toFloat() / milestones.size.toFloat() else 0f
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { totalProg },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = FlowTeal,
                            trackColor = DarkSurfaceElevated
                        )
                    }
                }
            }
        }

        // Timeline Items
        itemsIndexed(milestones) { index, item ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Timeline Connector Column
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(36.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (item.isUnlocked) FlowTeal else DarkSurfaceElevated),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (item.isUnlocked) Icons.Default.Check else Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (item.isUnlocked) DarkBackground else TextLow,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    if (index < milestones.size - 1) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(60.dp)
                                .background(if (item.isUnlocked) FlowTeal.copy(alpha = 0.5f) else DarkBorder)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Milestone Detail Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (item.isUnlocked) DarkSurfaceElevated else DarkSurface.copy(alpha = 0.6f)
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(
                            if (item.isUnlocked) FlowTeal.copy(alpha = 0.3f) else DarkBorder
                        )
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(item.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = if (item.isUnlocked) TextHigh else TextMedium)
                            if (item.isUnlocked) {
                                Surface(color = FlowEmerald.copy(alpha = 0.15f), shape = RoundedCornerShape(6.dp)) {
                                    Text("UNLOCKED", style = MaterialTheme.typography.labelSmall, color = FlowEmerald, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(item.description, style = MaterialTheme.typography.bodySmall, color = TextMedium)

                        if (!item.isUnlocked && item.progress > 0f) {
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { item.progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = FlowTeal,
                                trackColor = DarkBorder
                            )
                        }
                    }
                }
            }
        }
    }
}
