package com.example.ui.screens.barcode

import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.data.local.entity.Product
import com.example.data.repository.BusinessRepository
import com.example.ui.theme.EmeraldGreen
import com.example.util.CurrencyUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarcodeScannerScreen(
    repository: BusinessRepository,
    onNavigateBack: () -> Unit,
    onProductFound: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    var manualBarcodeInput by remember { mutableStateOf("") }
    var detectedProduct by remember { mutableStateOf<Product?>(null) }
    var searchError by remember { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = modifier.testTag("barcode_scanner_screen"),
        topBar = {
            TopAppBar(
                title = { Text("Barcode & SKU Scanner", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.Black)
        ) {
            // Camera Preview View
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }

                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        try {
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }
                            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview)
                        } catch (e: Exception) {
                            // In case camera permission or emulator without camera
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                }
            )

            // Scanning Reticle Overlay
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(280.dp, 180.dp)
                        .border(BorderStroke(2.dp, EmeraldGreen), RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.05f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        tint = EmeraldGreen.copy(alpha = 0.6f),
                        modifier = Modifier.size(48.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Align barcode within rectangle to scan",
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Bottom Manual Entry Card & Result
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Scan or Enter Code Manually",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = manualBarcodeInput,
                            onValueChange = {
                                manualBarcodeInput = it
                                searchError = null
                            },
                            placeholder = { Text("e.g. 890123456789 or EL-BC-01") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        Button(
                            onClick = {
                                scope.launch {
                                    val prod = repository.findProductByBarcode(manualBarcodeInput.trim())
                                    if (prod != null) {
                                        detectedProduct = prod
                                        searchError = null
                                    } else {
                                        detectedProduct = null
                                        searchError = "No product matching '${manualBarcodeInput.trim()}'"
                                    }
                                }
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Search, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Find")
                        }
                    }

                    if (searchError != null) {
                        Text(
                            text = searchError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    if (detectedProduct != null) {
                        val prod = detectedProduct!!
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(prod.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Text("SKU: ${prod.sku} • Stock: ${prod.currentStock} ${prod.unit}", style = MaterialTheme.typography.bodySmall)
                                Text("Price: ${CurrencyUtils.formatCents(prod.sellingPriceCents)}", color = EmeraldGreen, fontWeight = FontWeight.Bold)

                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { onProductFound(prod.id) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Open Product Details")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
