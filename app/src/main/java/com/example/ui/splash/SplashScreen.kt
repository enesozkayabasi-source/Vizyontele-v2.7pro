package com.example.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonRed
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    var progress by remember { mutableFloatStateOf(0f) }
    var statusText by remember { mutableStateOf("VİZYON TV Başlatılıyor...") }
    val entranceScale = remember { Animatable(0.7f) }
    val entranceAlpha = remember { Animatable(0f) }

    // Breathing glow animation
    val infiniteTransition = rememberInfiniteTransition(label = "splash_glow_anim")
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_pulse"
    )

    // Subtle ambient rotation for background accent
    val subtleRotate by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(28000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "subtle_rotate"
    )

    // Snappy, fast opening sequence (~0.8s total)
    LaunchedEffect(Unit) {
        entranceAlpha.animateTo(1f, animationSpec = tween(200, easing = FastOutSlowInEasing))
        entranceScale.animateTo(1f, animationSpec = spring(dampingRatio = 0.85f, stiffness = 600f))

        progress = 0.35f
        statusText = "Türksat & Canlı TV Bağlanıyor..."
        delay(250)

        progress = 0.85f
        statusText = "Kanallar Hazırlanıyor..."
        delay(300)

        progress = 1.0f
        statusText = "Hazır! Keyifli Seyirler..."
        delay(200)

        onFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF070A12))
            .testTag("splash_screen")
    ) {
        // Atmospheric cinematic background image (Gold Luxury Art)
        Image(
            painter = painterResource(id = R.drawable.vizyontv_gold_splash_1791394550229),
            contentDescription = "VİZYON TV Splash Arka Planı",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Vignette & Darkness Gradients
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.50f),
                            Color(0xFF070A12).copy(alpha = 0.70f),
                            Color(0xFF070A12).copy(alpha = 0.96f)
                        )
                    )
                )
        )

        // Main Center Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(1.1f))

            // VİZYON TV Altın Kanatlı Logo
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .scale(entranceScale.value)
                    .size(160.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color(0xFF10141E))
                    .border(
                        width = 2.5.dp,
                        color = Color(0xFFD4AF37).copy(alpha = glowPulse),
                        shape = RoundedCornerShape(32.dp)
                    )
                    .testTag("splash_logo_box")
            ) {
                Image(
                    painter = painterResource(id = R.drawable.vizyontv_gold_icon_1791394537825),
                    contentDescription = "VİZYON TV Logo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Brand Title and Logo
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "VİZYON",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 4.sp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "TV",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFD4AF37),
                    letterSpacing = 4.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Subtitle Tagline
            Text(
                text = "Keşfetmeye Hazır Mısınız?",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.95f),
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = "En İyi İçerikler, Premium Deneyim",
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFFD4AF37).copy(alpha = 0.90f),
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.weight(1f))

            // Bottom Progress & Stepper Animation Container
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .padding(bottom = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Status Text & Progress Percentage
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFD4AF37))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = statusText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.95f),
                            maxLines = 1
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "${(progress * 100).toInt()}%",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFD4AF37)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Smooth Progress Bar
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = Color(0xFFD4AF37),
                    trackColor = Color.White.copy(alpha = 0.15f)
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Quick Skip / Enter Button
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White.copy(alpha = 0.10f),
                    border = BorderStroke(1.dp, Color(0xFFD4AF37).copy(alpha = 0.4f)),
                    modifier = Modifier
                        .clickable { onFinished() }
                        .testTag("splash_skip_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Hemen Başla",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Hemen Başla",
                            tint = Color(0xFFD4AF37),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
