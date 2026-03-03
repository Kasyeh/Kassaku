package com.example.kassaku.ui.components.skeleton

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Basic rectangular skeleton placeholder with shimmer effect.
 * 
 * @param width Width of the skeleton box
 * @param height Height of the skeleton box
 * @param modifier Additional modifiers
 * @param cornerRadius Corner radius for rounded corners
 */
@Composable
fun SkeletonBox(
    width: Dp,
    height: Dp,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = SkeletonDefaults.TextCornerRadius,
    shimmerBrush: Brush = rememberShimmerBrush()
) {
    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .clip(RoundedCornerShape(cornerRadius))
            .background(shimmerBrush)
            .clearAndSetSemantics { } // Hide from accessibility tree
    )
}

/**
 * Full-width skeleton placeholder with shimmer effect.
 * 
 * @param height Height of the skeleton
 * @param modifier Additional modifiers
 * @param cornerRadius Corner radius for rounded corners
 */
@Composable
fun SkeletonBoxFullWidth(
    height: Dp,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = SkeletonDefaults.TextCornerRadius,
    shimmerBrush: Brush = rememberShimmerBrush()
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(cornerRadius))
            .background(shimmerBrush)
            .clearAndSetSemantics { }
    )
}

/**
 * Circular skeleton placeholder for avatars and icons.
 * 
 * @param size Diameter of the circle
 * @param modifier Additional modifiers
 */
@Composable
fun SkeletonCircle(
    size: Dp,
    modifier: Modifier = Modifier,
    shimmerBrush: Brush = rememberShimmerBrush()
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(shimmerBrush)
            .clearAndSetSemantics { }
    )
}

/**
 * Text-line skeleton placeholder for simulating text content.
 * Uses standard text heights from SkeletonDefaults.
 * 
 * @param width Width of the text skeleton
 * @param textSize Size category (small, medium, large)
 * @param modifier Additional modifiers
 */
@Composable
fun SkeletonText(
    width: Dp,
    modifier: Modifier = Modifier,
    height: Dp = SkeletonDefaults.TextHeightSmall,
    shimmerBrush: Brush = rememberShimmerBrush()
) {
    SkeletonBox(
        width = width,
        height = height,
        modifier = modifier,
        cornerRadius = SkeletonDefaults.TextCornerRadius,
        shimmerBrush = shimmerBrush
    )
}

/**
 * Card-shaped skeleton with rounded corners.
 * Useful for cards, dialogs, and containers.
 * 
 * @param width Width of the card
 * @param height Height of the card
 * @param modifier Additional modifiers
 */
@Composable
fun SkeletonCard(
    modifier: Modifier = Modifier,
    width: Dp? = null,
    height: Dp,
    shimmerBrush: Brush = rememberShimmerBrush()
) {
    Box(
        modifier = modifier
            .then(if (width != null) Modifier.width(width) else Modifier.fillMaxWidth())
            .height(height)
            .clip(RoundedCornerShape(SkeletonDefaults.CardCornerRadius))
            .background(shimmerBrush)
            .clearAndSetSemantics { }
    )
}
