package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DifficultyLevel
import com.example.ui.theme.OutfitFontFamily

@Composable
fun DifficultyBadge(
    level: DifficultyLevel,
    modifier: Modifier = Modifier,
    showEnglish: Boolean = false
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(level.containerColor)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = if (showEnglish) "${level.koreanTitle} · ${level.englishTitle}" else level.koreanTitle,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = level.accentColor
        )
    }
}

@Composable
fun HighlightedChainWord(
    word: String,
    firstLetterColor: Color,
    lastLetterColor: Color,
    bodyColor: Color,
    fontSize: TextUnit = 20.sp,
    modifier: Modifier = Modifier
) {
    val clean = word.lowercase()
    val annotated = buildAnnotatedString {
        if (clean.length <= 1) {
            withStyle(SpanStyle(color = lastLetterColor, fontWeight = FontWeight.ExtraBold)) {
                append(clean)
            }
        } else {
            withStyle(SpanStyle(color = firstLetterColor, fontWeight = FontWeight.ExtraBold)) {
                append(clean.first())
            }
            if (clean.length > 2) {
                withStyle(SpanStyle(color = bodyColor, fontWeight = FontWeight.SemiBold)) {
                    append(clean.substring(1, clean.length - 1))
                }
            }
            withStyle(SpanStyle(color = lastLetterColor, fontWeight = FontWeight.ExtraBold)) {
                append(clean.last())
            }
        }
    }

    Text(
        text = annotated,
        fontFamily = OutfitFontFamily,
        fontSize = fontSize,
        letterSpacing = 0.3.sp,
        modifier = modifier
    )
}
