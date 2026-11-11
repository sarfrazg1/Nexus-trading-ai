package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

/**
 * Licensed Google Font 'Noto Nastaliq Urdu' (OFL License - Open Font License).
 * Bundled locally in app/src/main/res/font/noto_nastaliq_urdu.ttf.
 * Fully readable offline, renders authentic Urdu Nastaleeq script with correct joining,
 * RTL alignment, proper diacritics, and mixed Urdu/English trading text.
 */
val NotoNastaliqUrduFontFamily = FontFamily(
    Font(R.font.noto_nastaliq_urdu, FontWeight.Normal),
    Font(R.font.noto_nastaliq_urdu, FontWeight.Medium),
    Font(R.font.noto_nastaliq_urdu, FontWeight.Bold)
)

val LatinFontFamily = FontFamily.Default

val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = LatinFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = LatinFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    titleMedium = TextStyle(
        fontFamily = LatinFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    titleLarge = TextStyle(
        fontFamily = LatinFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 26.sp
    ),
    labelSmall = TextStyle(
        fontFamily = LatinFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)
