package com.chimera.ui.screens.threed

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.sceneview.SceneView
import io.github.sceneview.math.Position
import io.github.sceneview.math.Size
import io.github.sceneview.node.CubeNode
import io.github.sceneview.rememberCameraManipulator
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberMaterialLoader
import io.github.sceneview.rememberModelLoader

/**
 * G2 renderer proof.
 *
 * This screen intentionally contains no Chimera simulation dependency.
 * It proves that SceneView/Filament can coexist with the Compose shell.
 */
@Composable
fun ThreeDLabScreen(onBack: () -> Unit) {
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val materialLoader = rememberMaterialLoader(engine)
    val material = remember(materialLoader) {
        materialLoader.createColorInstance(
            color = io.github.sceneview.math.Color(0.25f, 0.55f, 0.85f, 1.0f)
        )
    }

    Box(Modifier.fillMaxSize()) {
        SceneView(
            modifier = Modifier.fillMaxSize(),
            engine = engine,
            modelLoader = modelLoader,
            materialLoader = materialLoader,
            cameraManipulator = rememberCameraManipulator()
        ) {
            CubeNode(
                size = Size(1f, 1f, 1f),
                materialInstance = material,
                position = Position(z = -2f)
            )
        }

        Button(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Text("Back")
        }
    }
}
