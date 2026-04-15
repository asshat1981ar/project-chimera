package com.chimera.ui.screens.map

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chimera.ui.theme.AshBlack
import com.chimera.ui.theme.DimAsh
import com.chimera.ui.theme.EmberGold
import com.chimera.ui.theme.FadedBone
import com.chimera.ui.theme.HollowCrimson
import com.chimera.ui.theme.VoidGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onEnterScene: (String) -> Unit = {},
    viewModel: MapViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberStandardBottomSheetState(initialValue = SheetValue.Hidden, skipHiddenState = false)
    val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = sheetState)

    LaunchedEffect(uiState.selectedNode) {
        if (uiState.selectedNode != null) {
            sheetState.expand()
        } else {
            sheetState.hide()
        }
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetContainerColor = MaterialTheme.colorScheme.surface,
        sheetContentColor = MaterialTheme.colorScheme.onSurface,
        sheetPeekHeight = 0.dp,
        sheetContent = {
            uiState.selectedNode?.let { node ->
                NodeDetailSheet(
                    node = node,
                    onEnterScene = {
                        node.sceneId?.let { onEnterScene(it) }
                        viewModel.clearSelection()
                    },
                    onDismiss = { viewModel.clearSelection() }
                )
            } ?: Spacer(modifier = Modifier.height(1.dp))
        },
        containerColor = AshBlack
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(AshBlack)
        ) {
            // Title
            Text(
                text = "The Hollow",
                style = MaterialTheme.typography.headlineMedium,
                color = EmberGold,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
            )

            // Connection lines
            MapConnections(nodes = uiState.nodes)

            // Map nodes
            uiState.nodes.forEach { node ->
                MapNodeMarker(
                    node = node,
                    isSelected = uiState.selectedNode?.id == node.id,
                    onClick = { viewModel.selectNode(node) },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun MapConnections(nodes: List<MapNode>) {
    val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        nodes.forEach { node ->
            val start = Offset(node.xFraction * w, node.yFraction * h)
            node.connectedTo.forEach { targetId ->
                val target = nodes.find { it.id == targetId } ?: return@forEach
                val end = Offset(target.xFraction * w, target.yFraction * h)
                val lineColor = if (node.isUnlocked && target.isUnlocked) {
                    FadedBone.copy(alpha = 0.3f)
                } else {
                    DimAsh.copy(alpha = 0.15f)
                }
                drawLine(
                    color = lineColor,
                    start = start,
                    end = end,
                    strokeWidth = 2f,
                    pathEffect = if (!node.isUnlocked || !target.isUnlocked) pathEffect else null
                )
            }
        }
    }
}

@Composable
private fun MapNodeMarker(
    node: MapNode,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        val density = LocalDensity.current

        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset {
                    IntOffset(
                        x = (node.xFraction * constraints.maxWidth - 24 * density.density).toInt(),
                        y = (node.yFraction * constraints.maxHeight - 24 * density.density).toInt()
                    )
                }
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable(enabled = node.isUnlocked) { onClick() }
                    .width(80.dp)
            ) {
                Box {
                    Surface(
                        shape = CircleShape,
                        color = when {
                            isSelected -> EmberGold
                            node.isCompleted -> VoidGreen.copy(alpha = 0.7f)
                            node.isUnlocked -> HollowCrimson.copy(alpha = 0.8f)
                            else -> DimAsh.copy(alpha = 0.4f)
                        },
                        border = BorderStroke(
                            2.dp,
                            if (isSelected) EmberGold else Color.Transparent
                        ),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = if (node.isUnlocked) node.name.first().toString() else "?",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (node.isUnlocked) MaterialTheme.colorScheme.onSurface else DimAsh
                            )
                        }
                    }

                    // Rumor heat badge
                    if (node.rumorCount > 0) {
                        Badge(
                            containerColor = EmberGold,
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            Text("${node.rumorCount}")
                        }
                    }
                }

                Text(
                    text = if (node.isUnlocked) node.name else "???",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (node.isUnlocked) FadedBone else DimAsh,
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
private fun NodeDetailSheet(
    node: MapNode,
    onEnterScene: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(node.name, style = MaterialTheme.typography.headlineSmall, color = EmberGold)
            if (node.isCompleted) {
                Text("Explored", style = MaterialTheme.typography.labelMedium, color = VoidGreen)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(node.description, style = MaterialTheme.typography.bodyMedium, color = FadedBone)

        if (node.faction != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Controlled by: ${node.faction}",
                style = MaterialTheme.typography.bodySmall,
                color = HollowCrimson
            )
        }

        if (node.rumorCount > 0) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "${node.rumorCount} active rumor${if (node.rumorCount > 1) "s" else ""}",
                style = MaterialTheme.typography.bodySmall,
                color = EmberGold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (node.sceneId != null && node.isUnlocked) {
            Button(
                onClick = onEnterScene,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = HollowCrimson)
            ) {
                Text(if (node.isCompleted) "Return" else "Enter")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
