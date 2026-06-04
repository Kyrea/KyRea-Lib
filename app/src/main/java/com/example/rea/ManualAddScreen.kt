package com.example.rea

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import java.io.File
import java.io.FileOutputStream

@Composable
fun ManualAddScreen(viewModel: BookViewModel) {
    val context = LocalContext.current

    var title       by remember { mutableStateOf("") }
    var author      by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isbn        by remember { mutableStateOf("") }

    var imageUri        by remember { mutableStateOf<Uri?>(null) }
    var tempCameraUri   by remember { mutableStateOf<Uri?>(null) }
    var showImageDialog by remember { mutableStateOf(false) }

    val scanner = remember { GmsBarcodeScanning.getClient(context) }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) imageUri = uri
    }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) imageUri = tempCameraUri
    }

    Column(modifier = Modifier.fillMaxSize()) {

        // ── Koyu header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(LocalReaColors.current.headerBg)
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Column {
                Text(
                    text          = "Koleksiyona Ekle",
                    fontSize      = 11.sp,
                    letterSpacing = 2.sp,
                    color         = LocalReaColors.current.amber,
                    fontWeight    = FontWeight.Medium
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text       = "Kitap Ekle",
                    fontSize   = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color      = LocalReaColors.current.headerText
                )
            }
        }

        // ── Kaydırılabilir form alanı
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // ── Kapak görseli seçme alanı
            CoverPickerBox(
                imageUri       = imageUri,
                onPickRequested = { showImageDialog = true }
            )

            // ── Form alanları
            ReaTextField(
                value       = title,
                onValueChange = { title = it },
                label       = "Kitap Adı",
                placeholder = "Örn. The Name of the Wind",
                icon        = Icons.Default.MenuBook
            )

            ReaTextField(
                value         = author,
                onValueChange = { author = it },
                label         = "Yazar",
                placeholder   = "Örn. Patrick Rothfuss",
                icon          = Icons.Default.Person
            )

            ReaTextField(
                value         = description,
                onValueChange = { description = it },
                label         = "Açıklama",
                placeholder   = "Kitap hakkında notlarınız…",
                icon          = Icons.Default.Notes,
                minLines      = 3,
                maxLines      = 6
            )

            // ── ISBN + Tara
            IsbnRow(
                isbn      = isbn,
                onIsbnChange = { isbn = it },
                onScanClick  = {
                    scanner.startScan()
                        .addOnSuccessListener { barcode ->
                            barcode.rawValue?.let { isbn = it }
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Tarama iptal edildi.", Toast.LENGTH_SHORT).show()
                        }
                }
            )

            Spacer(Modifier.height(4.dp))

            // ── Alt aksiyonlar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Bul butonu
                OutlinedButton(
                    onClick = {
                        if (isbn.isNotBlank()) {
                            val existing = viewModel.checkLocalLibraryByIsbn(isbn)
                            if (existing != null) {
                                Toast.makeText(context, "Bulundu! \"${existing.title}\" kütüphanende var.", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "Bu ISBN kütüphanende yok.", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "Önce ISBN tarayın.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape  = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Bul", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }

                // Kaydet butonu
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            val savedPath = if (imageUri != null) saveImageToInternalStorage(context, imageUri!!) else null
                            viewModel.saveManualBook(
                                title       = title,
                                author      = author.ifBlank { "Bilinmiyor" },
                                description = description.ifBlank { "Açıklama yok." },
                                isbn        = isbn,
                                coverUrl    = savedPath
                            )
                            Toast.makeText(context, "\"$title\" kütüphaneye eklendi!", Toast.LENGTH_SHORT).show()
                            title       = ""
                            author      = ""
                            description = ""
                            isbn        = ""
                            imageUri    = null
                        } else {
                            Toast.makeText(context, "Kitap adı zorunludur.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .weight(2f)
                        .height(50.dp),
                    shape  = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LocalReaColors.current.headerBg,
                        contentColor   = LocalReaColors.current.headerText
                    )
                ) {
                    Icon(
                        Icons.Default.BookmarkAdd,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Kütüphaneye Kaydet", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    // ── Görsel seçim dialogu
    if (showImageDialog) {
        ImageSourceDialog(
            onDismiss     = { showImageDialog = false },
            onCameraClick = {
                showImageDialog = false
                val tempFile = File(context.cacheDir, "temp_camera_${System.currentTimeMillis()}.jpg")
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", tempFile)
                tempCameraUri = uri
                cameraLauncher.launch(uri)
            },
            onGalleryClick = {
                showImageDialog = false
                galleryLauncher.launch("image/*")
            }
        )
    }
}

// ─────────────────────────────────────────────
// KAPAK SEÇİCİ BOX
// ─────────────────────────────────────────────
@Composable
fun CoverPickerBox(imageUri: Uri?, onPickRequested: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(
                    listOf(MaterialTheme.colorScheme.outline, MaterialTheme.colorScheme.surfaceVariant)
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onPickRequested() },
        contentAlignment = Alignment.Center
    ) {
        if (imageUri != null) {
            AsyncImage(
                model              = imageUri,
                contentDescription = "Seçilen Kapak",
                contentScale       = ContentScale.Crop,
                modifier           = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp))
            )
            // Üstüne değiştir overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black.copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null,
                        tint     = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text     = "Görseli Değiştir",
                        fontSize = 13.sp,
                        color    = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.AddAPhoto,
                        contentDescription = null,
                        tint     = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    text       = "Kapak Görseli Ekle",
                    fontSize   = 14.sp,
                    color      = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text     = "Kamera veya galerinizden seçin",
                    fontSize = 12.sp,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ─────────────────────────────────────────────
// ÖZEL METİN ALANI
// ─────────────────────────────────────────────
@Composable
fun ReaTextField(
    value:         String,
    onValueChange: (String) -> Unit,
    label:         String,
    placeholder:   String,
    icon:          ImageVector,
    minLines:      Int = 1,
    maxLines:      Int = 1,
    keyboardType:  KeyboardType = KeyboardType.Text
) {
    Column {
        Text(
            text          = label.uppercase(),
            fontSize      = 10.sp,
            letterSpacing = 1.5.sp,
            color         = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight    = FontWeight.Medium,
            modifier      = Modifier.padding(bottom = 6.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(0.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp)
                .animateContentSize(),
            verticalAlignment = if (minLines > 1) Alignment.Top else Alignment.CenterVertically
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.outline,
                modifier           = Modifier
                    .size(18.dp)
                    .padding(top = if (minLines > 1) 2.dp else 0.dp)
            )
            Spacer(Modifier.width(10.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    Text(
                        text     = placeholder,
                        fontSize = 14.sp,
                        color    = MaterialTheme.colorScheme.outline
                    )
                }
                BasicTextField(
                    value         = value,
                    onValueChange = onValueChange,
                    textStyle     = TextStyle(
                        fontSize = 14.sp,
                        color    = MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush   = SolidColor(LocalReaColors.current.amber),
                    minLines      = minLines,
                    maxLines      = maxLines,
                    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                    modifier      = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// ─────────────────────────────────────────────
// ISBN SATIRI
// ─────────────────────────────────────────────
@Composable
fun IsbnRow(
    isbn:         String,
    onIsbnChange: (String) -> Unit,
    onScanClick:  () -> Unit
) {
    Column {
        Text(
            text          = "ISBN",
            fontSize      = 10.sp,
            letterSpacing = 1.5.sp,
            color         = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight    = FontWeight.Medium,
            modifier      = Modifier.padding(bottom = 6.dp)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ISBN input
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(0.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.QrCode,
                    contentDescription = null,
                    tint     = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(10.dp))
                Box(modifier = Modifier.weight(1f)) {
                    if (isbn.isEmpty()) {
                        Text("978-0-…", fontSize = 14.sp, color = MaterialTheme.colorScheme.outline)
                    }
                    BasicTextField(
                        value           = isbn,
                        onValueChange   = onIsbnChange,
                        textStyle       = TextStyle(fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface),
                        cursorBrush     = SolidColor(LocalReaColors.current.amber),
                        singleLine      = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier        = Modifier.fillMaxWidth()
                    )
                }
            }

            // Tara butonu
            Button(
                onClick = onScanClick,
                modifier = Modifier.height(48.dp),
                shape  = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LocalReaColors.current.headerBg,
                    contentColor   = LocalReaColors.current.headerText
                ),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                Icon(
                    Icons.Default.QrCodeScanner,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text("Tara", fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

// ─────────────────────────────────────────────
// GÖRSEL KAYNAK DİYALOĞU
// ─────────────────────────────────────────────
@Composable
fun ImageSourceDialog(
    onDismiss:     () -> Unit,
    onCameraClick: () -> Unit,
    onGalleryClick:() -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = MaterialTheme.colorScheme.surface,
        shape            = RoundedCornerShape(20.dp),
        title = {
            Text(
                text       = "Kapak Görseli",
                fontSize   = 20.sp,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text     = "Görseli nereden eklemek istersiniz?",
                    fontSize = 14.sp,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Kamera seçeneği
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { onCameraClick() }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CameraAlt, null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Text("Kamera", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        Text("Fotoğraf çek", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Galeri seçeneği
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { onGalleryClick() }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.PhotoLibrary, null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Text("Galeri", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        Text("Var olan bir görsel seç", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
            }
        }
    )
}

// ─────────────────────────────────────────────
// YARDIMCI: Görseli kalıcı depoya kaydet
// ─────────────────────────────────────────────
fun saveImageToInternalStorage(context: Context, uri: Uri): String? {
    return try {
        val inputStream  = context.contentResolver.openInputStream(uri)
        val file         = File(context.filesDir, "cover_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()
        file.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}