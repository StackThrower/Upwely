package com.warehouse.upwely.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.warehouse.upwely.R
import com.warehouse.upwely.data.ShippingBox
import com.warehouse.upwely.ui.components.ScreenHeader
import com.warehouse.upwely.ui.theme.*
import com.warehouse.upwely.ui.viewmodels.PickingScanViewModel
import com.warehouse.upwely.ui.viewmodels.RequiredItem
import java.util.concurrent.Executors

@Composable
fun PickingScanScreen(
    requiredItems: List<RequiredItem>,
    itemIndex: Int = 0,
    onContinuePicking: () -> Unit,
    onBack: () -> Unit,
    viewModel: PickingScanViewModel = viewModel(),
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsState()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        viewModel.initialize(requiredItems)
    }

    LaunchedEffect(hasCameraPermission) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Box selector dialog
    if (uiState.showBoxSelector) {
        BoxSelectorDialog(
            boxes = uiState.shippingBoxes,
            onBoxSelected = { box -> viewModel.selectBox(box) },
            onDismiss = { viewModel.dismissBoxSelector() },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
    ) {
        ScreenHeader(
            title = stringResource(R.string.scan_items),
            subtitle = stringResource(
                R.string.scanned_of_total,
                viewModel.getTotalScanned(),
                viewModel.getTotalRequired()
            ),
            actionIcon = Icons.Outlined.QrCodeScanner,
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Progress indicator
            ProgressSection(
                scanned = viewModel.getTotalScanned(),
                total = viewModel.getTotalRequired(),
            )

            // Camera preview or scan button
            if (uiState.isScanning && hasCameraPermission) {
                // Show serial number scanning indicator
                if (uiState.isScanningSerialNumber) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Cyan.copy(alpha = 0.15f))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.QrCodeScanner,
                            contentDescription = null,
                            tint = Cyan,
                            modifier = Modifier.size(20.dp),
                        )
                        Text(
                            text = stringResource(R.string.scan_serial_number),
                            fontFamily = InterFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = Cyan,
                        )
                    }
                }

                // Use key to reset camera state when switching between scan modes
                val scanModeKey = if (uiState.isScanningSerialNumber)
                    "serial_${uiState.editingSerialNumberIndex}"
                else
                    "item"

                // Capture the index for serial number scanning
                val serialNumberIndex = uiState.editingSerialNumberIndex
                val isScanningSerial = uiState.isScanningSerialNumber

                androidx.compose.runtime.key(scanModeKey) {
                    CameraPreview(
                        onBarcodeDetected = { barcode ->
                            if (isScanningSerial && serialNumberIndex != null) {
                                viewModel.onSerialNumberScanned(barcode, serialNumberIndex)
                            } else {
                                viewModel.onBarcodeScanned(barcode)
                            }
                        },
                        onClose = { viewModel.stopScanning() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .clip(RoundedCornerShape(12.dp)),
                    )
                }
            } else {
                // Scan button
                Button(
                    onClick = {
                        if (hasCameraPermission) {
                            viewModel.startScanning()
                        } else {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CardBackground,
                        contentColor = Cyan,
                    ),
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.QrCodeScanner,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                        )
                        Text(
                            text = stringResource(R.string.tap_to_scan),
                            fontFamily = InterFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                        )
                    }
                }
            }

            // Error message
            uiState.error?.let { error ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF991B1B).copy(alpha = 0.2f))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = error,
                        fontFamily = InterFamily,
                        fontSize = 13.sp,
                        color = Color(0xFFEF4444),
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(
                        onClick = { viewModel.clearError() },
                        modifier = Modifier.size(24.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }

            // Items list
            Text(
                text = stringResource(R.string.items_to_scan),
                fontFamily = InterFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                color = TextSecondary,
                letterSpacing = 2.sp,
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardBackground)
                    .padding(4.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                items(uiState.requiredItems) { item ->
                    ItemRow(item = item)
                }
            }

            // Scanned items section
            if (uiState.scannedItems.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.scanned_items),
                    fontFamily = InterFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    letterSpacing = 2.sp,
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardBackground)
                        .padding(4.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    items(uiState.scannedItems.size) { index ->
                        ScannedItemRow(
                            item = uiState.scannedItems[index],
                            onClick = { viewModel.startScanningSerialNumber(index) },
                        )
                    }
                }
            }
        }

        // Bottom buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(
                onClick = onBack,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CardBackground,
                    contentColor = TextSecondary,
                ),
            ) {
                Text(
                    text = stringResource(R.string.back),
                    fontFamily = InterFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                )
            }

            Button(
                onClick = {
                    // Mark scan as completed
                    PickingScanResult.lastScannedItemIndex = itemIndex
                    PickingScanResult.scanCompleted = true
                    onContinuePicking()
                },
                enabled = uiState.allItemsScanned,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (uiState.allItemsScanned) Cyan else CardBackground,
                    contentColor = if (uiState.allItemsScanned) DarkBackground else TextMuted,
                    disabledContainerColor = CardBackground,
                    disabledContentColor = TextMuted,
                ),
            ) {
                Text(
                    text = stringResource(R.string.continue_picking),
                    fontFamily = InterFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                )
            }
        }
    }
}

@Composable
private fun ProgressSection(
    scanned: Int,
    total: Int,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(R.string.progress),
                fontFamily = InterFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                color = TextSecondary,
                letterSpacing = 2.sp,
            )
            Text(
                text = "$scanned / $total",
                fontFamily = JetBrainsMonoFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = if (scanned == total && total > 0) Color(0xFF16A34A) else Cyan,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(CardBackground),
        ) {
            if (total > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(scanned.toFloat() / total)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (scanned == total) Color(0xFF16A34A) else Cyan),
                )
            }
        }
    }
}

@Composable
private fun ItemRow(item: RequiredItem) {
    val isComplete = item.scannedQuantity >= item.quantity

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isComplete) Color(0xFF16A34A).copy(alpha = 0.1f) else ItemBackground)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(
                    if (isComplete) Color(0xFF16A34A).copy(alpha = 0.15f)
                    else Cyan.copy(alpha = 0.15f)
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (isComplete) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF16A34A),
                    modifier = Modifier.size(16.dp),
                )
            } else {
                Text(
                    text = "${item.scannedQuantity}",
                    fontFamily = JetBrainsMonoFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Cyan,
                )
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = item.name,
                fontFamily = InterFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = if (isComplete) Color(0xFF16A34A) else White,
            )
            Text(
                text = "${item.sku} · ${item.scannedQuantity}/${item.quantity}",
                fontFamily = JetBrainsMonoFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 10.sp,
                color = TextMuted,
            )
        }
    }
}

@Composable
private fun ScannedItemRow(
    item: com.warehouse.upwely.ui.viewmodels.ScannedItem,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF16A34A).copy(alpha = 0.1f))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.CheckCircle,
            contentDescription = null,
            tint = Color(0xFF16A34A),
            modifier = Modifier.size(20.dp),
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = item.itemName,
                fontFamily = InterFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = Color(0xFF16A34A),
            )
            item.serialNumber?.let { serial ->
                Text(
                    text = "S/N: $serial",
                    fontFamily = JetBrainsMonoFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 10.sp,
                    color = Cyan,
                )
            }
            item.selectedBox?.let { box ->
                Text(
                    text = "${box.description} (${box.boxId})",
                    fontFamily = JetBrainsMonoFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 10.sp,
                    color = TextMuted,
                )
            }
        }
        Icon(
            imageVector = Icons.Outlined.QrCodeScanner,
            contentDescription = stringResource(R.string.scan_serial_number),
            tint = Cyan,
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
private fun CameraPreview(
    onBarcodeDetected: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var lastScannedBarcode by remember { mutableStateOf("") }
    var lastScanTime by remember { mutableStateOf(0L) }

    Box(modifier = modifier) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }

                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }

                    val barcodeScanner = BarcodeScanning.getClient()
                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                        @androidx.camera.core.ExperimentalGetImage
                        val mediaImage = imageProxy.image
                        if (mediaImage != null) {
                            val image = InputImage.fromMediaImage(
                                mediaImage,
                                imageProxy.imageInfo.rotationDegrees
                            )
                            barcodeScanner.process(image)
                                .addOnSuccessListener { barcodes ->
                                    val currentTime = System.currentTimeMillis()
                                    for (barcode in barcodes) {
                                        barcode.rawValue?.let { value ->
                                            // Prevent duplicate scans within 2 seconds
                                            if (value != lastScannedBarcode || currentTime - lastScanTime > 2000) {
                                                lastScannedBarcode = value
                                                lastScanTime = currentTime
                                                onBarcodeDetected(value)
                                            }
                                        }
                                    }
                                }
                                .addOnCompleteListener {
                                    imageProxy.close()
                                }
                        } else {
                            imageProxy.close()
                        }
                    }

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageAnalysis
                        )
                    } catch (e: Exception) {
                        Log.e("CameraPreview", "Camera binding failed", e)
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            },
            modifier = Modifier.fillMaxSize(),
        )

        // Close button
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .size(36.dp)
                .clip(CircleShape)
                .background(DarkBackground.copy(alpha = 0.7f)),
        ) {
            Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = stringResource(R.string.cancel),
                tint = White,
                modifier = Modifier.size(20.dp),
            )
        }

        // Scanning overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Transparent)
            ) {
                // Corner indicators
                // Top-left
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .size(30.dp, 4.dp)
                        .background(Cyan, RoundedCornerShape(2.dp))
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .size(4.dp, 30.dp)
                        .background(Cyan, RoundedCornerShape(2.dp))
                )
                // Top-right
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(30.dp, 4.dp)
                        .background(Cyan, RoundedCornerShape(2.dp))
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(4.dp, 30.dp)
                        .background(Cyan, RoundedCornerShape(2.dp))
                )
                // Bottom-left
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .size(30.dp, 4.dp)
                        .background(Cyan, RoundedCornerShape(2.dp))
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .size(4.dp, 30.dp)
                        .background(Cyan, RoundedCornerShape(2.dp))
                )
                // Bottom-right
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(30.dp, 4.dp)
                        .background(Cyan, RoundedCornerShape(2.dp))
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(4.dp, 30.dp)
                        .background(Cyan, RoundedCornerShape(2.dp))
                )
            }
        }
    }
}

@Composable
private fun BoxSelectorDialog(
    boxes: List<ShippingBox>,
    onBoxSelected: (ShippingBox) -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CardBackground)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.select_box),
                    fontFamily = InterFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = White,
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.heightIn(max = 400.dp),
            ) {
                items(boxes.filter { it.activeByDefault }) { box ->
                    BoxOptionRow(
                        box = box,
                        onClick = { onBoxSelected(box) },
                    )
                }
                if (boxes.any { !it.activeByDefault }) {
                    item {
                        Text(
                            text = stringResource(R.string.other_boxes),
                            fontFamily = InterFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = TextMuted,
                            letterSpacing = 2.sp,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                    items(boxes.filter { !it.activeByDefault }) { box ->
                        BoxOptionRow(
                            box = box,
                            onClick = { onBoxSelected(box) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BoxOptionRow(
    box: ShippingBox,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(ItemBackground)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Cyan.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Inventory2,
                contentDescription = null,
                tint = Cyan,
                modifier = Modifier.size(22.dp),
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = box.description,
                fontFamily = InterFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = White,
            )
            Text(
                text = "${box.length.toInt()}x${box.width.toInt()}x${box.height.toInt()} ${box.linearUOM}",
                fontFamily = JetBrainsMonoFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                color = TextMuted,
            )
        }

        Text(
            text = box.boxId,
            fontFamily = JetBrainsMonoFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = Cyan,
        )
    }
}
