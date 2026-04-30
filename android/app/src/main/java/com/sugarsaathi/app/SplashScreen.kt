package com.sugarsaathi.app

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinished: () -> Unit) {

    val alpha = remember { Animatable(0f) }
    val loadingProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        alpha.animateTo(1f, animationSpec = tween(800))
        loadingProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 3000, easing = LinearEasing)
        )
        delay(300)
        onFinished()
    }

    val bgBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFF0A7AFF),
            Color(0xFF1D9E75),
            Color(0xFFF5A623)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = bgBrush),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(alpha = alpha.value)
                .padding(horizontal = 24.dp)
        ) {

            Spacer(modifier = Modifier.height(52.dp))

            // ── App name ──
            Text(
                text = "GlycoAI",
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = "گلائیکو اے آئی",
                fontSize = 18.sp,
                color = Color.White.copy(alpha = 0.82f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            // ── Divider ──
            Box(
                modifier = Modifier
                    .width(160.dp)
                    .height(1.dp)
                    .background(Color.White.copy(alpha = 0.25f))
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Your Daily Diabetes Companion",
                fontSize = 13.sp,
                fontStyle = FontStyle.Italic,
                color = Color.White.copy(alpha = 0.78f),
                letterSpacing = 0.5.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(30.dp))

            // ── Wellness Wheel Image ──
            // Make sure the image file in res/drawable is saved as a transparent PNG
            Image(
                painter = painterResource(id = R.drawable.splash_screen_center),
                contentDescription = "Daily Wellness Hub Wheel",
                contentScale = ContentScale.Fit, // Changed to Fit to preserve the natural shadow
                modifier = Modifier
                    .size(280.dp)
                // Removed .clip(CircleShape) so the transparent background renders perfectly
            )

            Spacer(modifier = Modifier.height(34.dp))

            // ── Loading text ──
            Text(
                text = "LOADING...",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.7f),
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = "لوڈ ہو رہا ہے...",
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ── Loading bar ──
            Box(
                modifier = Modifier
                    .width(260.dp)
                    .height(7.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        )
                )
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(loadingProgress.value)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF0A7AFF),
                                    Color(0xFF1D9E75),
                                    Color(0xFFF5A623)
                                )
                            ),
                            shape = RoundedCornerShape(4.dp)
                        )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Footer ──

            Text(
                text = "Made by Ghulam Mustafa",
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.4f),
                textAlign = TextAlign.Center
            )
        }
    }
}