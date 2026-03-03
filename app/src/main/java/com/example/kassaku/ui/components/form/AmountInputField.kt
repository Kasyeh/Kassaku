package com.example.kassaku.ui.components.form

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kassaku.ui.theme.StitchPrimary
import com.example.kassaku.ui.theme.StitchAccentRed
import com.example.kassaku.ui.theme.StitchTextSecondary
import java.text.NumberFormat
import java.util.Locale

/**
 * Currency input field with automatic thousand separator formatting.
 * Stores raw integer string, displays formatted value.
 */
@Composable
fun AmountInputField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isExpense: Boolean = false,
    isError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true
) {
    val accentColor = if (isExpense) StitchAccentRed else StitchPrimary
    
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            // Filter to digits only, limit to 12 characters
            val filtered = newValue.filter { it.isDigit() }.take(12)
            // Remove leading zeros (except for empty or single "0")
            val cleaned = if (filtered.length > 1) filtered.trimStart('0').ifEmpty { "0" } else filtered
            onValueChange(cleaned)
        },
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        textStyle = TextStyle(
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        ),
        prefix = {
            Text(
                text = "Rp ",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
        },
        placeholder = {
            Text(
                text = "0",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = StitchTextSecondary.copy(alpha = 0.5f)
            )
        },
        visualTransformation = ThousandSeparatorTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        isError = isError,
        supportingText = if (errorMessage != null) {
            { Text(errorMessage, color = StitchAccentRed) }
        } else null,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = accentColor,
            cursorColor = accentColor,
            focusedLabelColor = accentColor
        )
    )
}

/**
 * Visual transformation that adds thousand separators (.) to numbers.
 * Example: 50000 -> 50.000
 */
class ThousandSeparatorTransformation : VisualTransformation {
    private val formatter = NumberFormat.getNumberInstance(Locale("id", "ID")).apply {
        maximumFractionDigits = 0
    }
    
    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text
        
        if (originalText.isEmpty()) {
            return TransformedText(text, OffsetMapping.Identity)
        }
        
        val number = originalText.toLongOrNull() ?: return TransformedText(text, OffsetMapping.Identity)
        val formatted = formatter.format(number)
        
        return TransformedText(
            AnnotatedString(formatted),
            ThousandOffsetMapping(originalText, formatted)
        )
    }
}

/**
 * Offset mapping for thousand separator transformation.
 * Maps cursor positions between original (12345) and formatted (12.345) text.
 */
private class ThousandOffsetMapping(
    private val original: String,
    private val formatted: String
) : OffsetMapping {
    
    override fun originalToTransformed(offset: Int): Int {
        // Count how many separators appear before this position
        var transformedOffset = 0
        var originalCount = 0
        
        for (char in formatted) {
            if (originalCount >= offset) break
            transformedOffset++
            if (char.isDigit()) originalCount++
        }
        
        return transformedOffset.coerceIn(0, formatted.length)
    }
    
    override fun transformedToOriginal(offset: Int): Int {
        // Count only digits up to this position
        var originalOffset = 0
        var transformedCount = 0
        
        for (char in formatted) {
            if (transformedCount >= offset) break
            transformedCount++
            if (char.isDigit()) originalOffset++
        }
        
        return originalOffset.coerceIn(0, original.length)
    }
}
