package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.MediaCollection
import com.example.ui.theme.MidnightCard
import com.example.ui.theme.MidnightDark
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AddMediaDialog(
    onDismiss: () -> Unit,
    onSave: (collection: MediaCollection, baslik: String, kategori: String, resimUrl: String, yayinUrl: String) -> Unit
) {
    var selectedCollection by remember { mutableStateOf(MediaCollection.CANLI_TV) }
    var baslik by remember { mutableStateOf("") }
    var kategori by remember { mutableStateOf("") }
    var resimUrl by remember { mutableStateOf("") }
    var yayinUrl by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MidnightCard,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Yeni Yayın / Belge Ekle",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Kapat",
                            tint = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Collection selector
                Text(
                    text = "Firestore Koleksiyonu Seçin:",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MediaCollection.entries.forEach { col ->
                        FilterChip(
                            selected = selectedCollection == col,
                            onClick = { selectedCollection = col },
                            label = { Text(col.title, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonCyan,
                                selectedLabelColor = MidnightDark,
                                containerColor = MidnightDark,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Field: baslik
                OutlinedTextField(
                    value = baslik,
                    onValueChange = { baslik = it },
                    label = { Text("baslik (Yayın Başlığı)") },
                    placeholder = { Text("Örn: TRT Haber, Minika Çocuk, A Spor...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_baslik"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        focusedLabelColor = NeonCyan,
                        unfocusedTextColor = TextPrimary,
                        focusedTextColor = TextPrimary
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Field: kategori
                OutlinedTextField(
                    value = kategori,
                    onValueChange = { kategori = it },
                    label = { Text("kategori (Kategori Adı)") },
                    placeholder = { Text("Örn: Ulusal, Spor, Bilim Kurgu...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_kategori"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        focusedLabelColor = NeonCyan,
                        unfocusedTextColor = TextPrimary,
                        focusedTextColor = TextPrimary
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Field: resim_url
                OutlinedTextField(
                    value = resimUrl,
                    onValueChange = { resimUrl = it },
                    label = { Text("resim_url (Afiş / Logo Linki)") },
                    placeholder = { Text("https://... (boş bırakılabilir)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_resim_url"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        focusedLabelColor = NeonCyan,
                        unfocusedTextColor = TextPrimary,
                        focusedTextColor = TextPrimary
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Field: yayin_url
                OutlinedTextField(
                    value = yayinUrl,
                    onValueChange = { yayinUrl = it },
                    label = { Text("yayin_url (.m3u8 veya .mp4)") },
                    placeholder = { Text("https://.../stream.m3u8") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_yayin_url"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        focusedLabelColor = NeonCyan,
                        unfocusedTextColor = TextPrimary,
                        focusedTextColor = TextPrimary
                    ),
                    singleLine = true
                )

                // Quick test fill button
                TextButton(
                    onClick = {
                        baslik = "Test HLS Yayını"
                        kategori = "Test"
                        resimUrl = "https://images.unsplash.com/photo-1594909122845-11baa439b7bf?w=600"
                        yayinUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Örnek Test Verisi Doldur", color = NeonCyan, fontSize = 12.sp)
                }

                if (errorText != null) {
                    Text(
                        text = errorText ?: "",
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Save button
                Button(
                    onClick = {
                        if (baslik.isBlank()) {
                            errorText = "Lütfen başlık giriniz."
                            return@Button
                        }
                        if (yayinUrl.isBlank()) {
                            errorText = "Lütfen yayın URL (.m3u8 / .mp4) giriniz."
                            return@Button
                        }
                        onSave(
                            selectedCollection,
                            baslik,
                            if (kategori.isBlank()) "Genel" else kategori,
                            resimUrl,
                            yayinUrl
                        )
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonCyan,
                        contentColor = MidnightDark
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_save_media")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Firestore'a Kaydet", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
