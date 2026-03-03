package com.example.kassaku.ui.components.form

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kassaku.ui.theme.StitchPrimary
import com.example.kassaku.ui.theme.StitchPrimaryLight
import com.example.kassaku.ui.theme.StitchAccentRed
import com.example.kassaku.ui.theme.StitchTextSecondary

/**
 * Data class representing a category option.
 */
data class CategoryOption(
    val id: String,
    val label: String,
    val icon: ImageVector? = null
)

/**
 * Horizontal scrollable row of category chips.
 * Single selection - only one chip can be selected at a time.
 */
@Composable
fun CategoryChipRow(
    categories: List<CategoryOption>,
    selectedCategory: String?,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    isExpense: Boolean = false,
    enabled: Boolean = true,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    val accentColor = if (isExpense) StitchAccentRed else StitchPrimary
    val selectedContainerColor = if (isExpense) Color(0xFFFEE2E2) else StitchPrimaryLight
    
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { category ->
                val isSelected = selectedCategory?.equals(category.label, ignoreCase = true) == true
                
                FilterChip(
                    selected = isSelected,
                    onClick = { 
                        if (enabled) onCategorySelected(category.label) 
                    },
                    label = {
                        Text(
                            text = category.label,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    },
                    leadingIcon = category.icon?.let { icon ->
                        {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    enabled = enabled,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = selectedContainerColor,
                        selectedLabelColor = accentColor,
                        selectedLeadingIconColor = accentColor
                    )
                )
            }
        }
        
        // Error message if applicable
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = StitchAccentRed,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

/**
 * Predefined income categories.
 */
object IncomeCategories {
    val list = listOf(
        CategoryOption("gaji", "Gaji"),
        CategoryOption("bonus", "Bonus"),
        CategoryOption("transfer", "Transfer"),
        CategoryOption("investasi", "Investasi"),
        CategoryOption("lainnya", "Lainnya")
    )
}

/**
 * Predefined expense categories.
 */
object ExpenseCategories {
    val list = listOf(
        CategoryOption("makanan", "Makanan"),
        CategoryOption("transport", "Transport"),
        CategoryOption("belanja", "Belanja"),
        CategoryOption("tagihan", "Tagihan"),
        CategoryOption("hiburan", "Hiburan"),
        CategoryOption("kesehatan", "Kesehatan"),
        CategoryOption("lainnya", "Lainnya")
    )
}
