package com.example.pawplan.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.example.pawplan.R

val LatoFontFamily = FontFamily(
    Font(R.font.latoregular, FontWeight.Normal), // Regular
    Font(R.font.latobold, FontWeight.Bold), // Bold
    Font(R.font.latoblack, FontWeight.Black), // Black
    Font(R.font.latolight, FontWeight.Light), // Light
    Font(R.font.latothin, FontWeight.Thin), // Thin

    Font(R.font.latoitalic, FontWeight.Normal, FontStyle.Italic), // Regular Italic
    Font(R.font.latobolditalic, FontWeight.Bold, FontStyle.Italic), // Bold Italic
    Font(R.font.latoblackitalic, FontWeight.Black, FontStyle.Italic), // Black Italic
    Font(R.font.latolightitalic, FontWeight.Light, FontStyle.Italic), // Light Italic
    Font(R.font.latothinitalic, FontWeight.Thin, FontStyle.Italic) // Thin Italic
)