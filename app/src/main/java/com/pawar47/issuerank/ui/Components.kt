package com.pawar47.issuerank.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pawar47.issuerank.data.Status

// ---------------------------------------------------------------- text

@Composable
fun IText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = IosType.body,
    color: Color = Ios.colors.label,
    maxLines: Int = Int.MAX_VALUE,
    textAlign: TextAlign = TextAlign.Start
) {
    Text(
        text = text,
        modifier = modifier,
        style = style,
        color = color,
        maxLines = maxLines,
        textAlign = textAlign,
        overflow = TextOverflow.Ellipsis
    )
}

// ---------------------------------------------------------------- interaction

/** Tap target with no Android ripple: iOS rows simply highlight. */
@Composable
fun Modifier.iosClick(enabled: Boolean = true, onClick: () -> Unit): Modifier =
    if (enabled) {
        this.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
    } else {
        this
    }

// ---------------------------------------------------------------- liquid glass

fun Modifier.glass(shape: Shape = RoundedCornerShape(30.dp), colors: IosColors): Modifier = this
    .shadow(
        elevation = 18.dp,
        shape = shape,
        clip = false,
        ambientColor = Color.Black.copy(alpha = 0.18f),
        spotColor = Color.Black.copy(alpha = 0.18f)
    )
    .clip(shape)
    .background(
        Brush.verticalGradient(listOf(colors.glass, colors.glass.copy(alpha = 0.82f)))
    )
    .border(0.7.dp, colors.glassBorder, shape)

@Composable
fun GlassCircleButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val colors = Ios.colors
    Box(
        modifier
            .size(size)
            .glass(CircleShape, colors)
            .iosClick(onClick = onClick),
        contentAlignment = Alignment.Center,
        content = content
    )
}

@Composable
fun GlassTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = Ios.colors.blue
) {
    val colors = Ios.colors
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    Box(
        modifier
            .height(30.dp)
            .glass(RoundedCornerShape(15.dp), colors)
            .graphicsLayer { alpha = if (pressed) 0.6f else 1f }
            .clickable(interactionSource = source, indication = null, onClick = onClick)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        IText(text, style = IosType.footnote.copy(fontWeight = FontWeight.SemiBold), color = tint)
    }
}

@Composable
fun FilledCapsuleButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val colors = Ios.colors
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    Box(
        modifier
            .height(34.dp)
            .graphicsLayer { alpha = if (pressed) 0.75f else 1f }
            .clip(RoundedCornerShape(17.dp))
            .background(if (enabled) colors.blue else colors.fill)
            .clickable(
                interactionSource = source,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        IText(
            text,
            style = IosType.subhead.copy(fontWeight = FontWeight.SemiBold),
            color = if (enabled) Color.White else colors.secondaryLabel
        )
    }
}

// ---------------------------------------------------------------- grouped list

@Composable
fun SectionHeader(text: String, modifier: Modifier = Modifier) {
    val colors = Ios.colors
    IText(
        text.uppercase(),
        modifier = modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 6.dp),
        style = IosType.footnote.copy(fontWeight = FontWeight.Medium),
        color = colors.secondaryLabel
    )
}

@Composable
fun GroupedCard(
    modifier: Modifier = Modifier,
    header: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = Ios.colors
    Column(modifier.fillMaxWidth()) {
        if (header != null) SectionHeader(header)
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.card),
            content = content
        )
    }
}

@Composable
fun ListRow(
    onClick: (() -> Unit)? = null,
    showDivider: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    val colors = Ios.colors
    Column(modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .iosClick(enabled = onClick != null) { onClick?.invoke() }
                .padding(contentPadding),
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
        if (showDivider) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp)
                    .height(0.5.dp)
                    .background(colors.separator)
            )
        }
    }
}

@Composable
fun Chevron() {
    ChevronGlyph(Ios.colors.tertiaryLabel)
}

// ---------------------------------------------------------------- badges

@Composable
fun StatusTag(status: Status) {
    val colors = Ios.colors
    val tint = if (status == Status.Completed) colors.green else colors.orange
    Box(
        Modifier
            .clip(RoundedCornerShape(7.dp))
            .background(tint.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        IText(status.label, style = IosType.footnote.copy(fontWeight = FontWeight.SemiBold), color = tint)
    }
}

@Composable
fun ResolutionTag(resolved: Boolean) {
    val colors = Ios.colors
    val tint = if (resolved) colors.green else colors.tertiaryLabel
    Box(
        Modifier
            .clip(RoundedCornerShape(7.dp))
            .background(if (resolved) colors.green.copy(alpha = 0.15f) else colors.fill)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        IText(
            if (resolved) "Resolved" else "Not Resolved",
            style = IosType.footnote.copy(fontWeight = FontWeight.SemiBold),
            color = if (resolved) tint else colors.secondaryLabel
        )
    }
}

@Composable
fun RankBadge(rank: Int) {
    val colors = Ios.colors
    Box(
        Modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(colors.blue.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
    ) {
        IText(
            rank.toString(),
            style = IosType.subhead.copy(fontWeight = FontWeight.SemiBold),
            color = colors.blue
        )
    }
}

private val avatarPalette = listOf(
    Color(0xFF0A84FF),
    Color(0xFF30D158),
    Color(0xFFFF9F0A),
    Color(0xFFFF453A),
    Color(0xFFAF52DE),
    Color(0xFF30B0C7),
    Color(0xFF5856D6),
    Color(0xFFFF2D55)
)

@Composable
fun InitialAvatar(text: String, seed: Long) {
    val color = avatarPalette[(seed % avatarPalette.size).toInt()]
    Box(
        Modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.18f)),
        contentAlignment = Alignment.Center
    ) {
        IText(
            text.take(2).uppercase(),
            style = IosType.footnote.copy(fontWeight = FontWeight.SemiBold),
            color = color
        )
    }
}

// ---------------------------------------------------------------- controls

@Composable
fun IosSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    val colors = Ios.colors
    val track by animateColorAsState(
        if (checked) colors.green else colors.switchOff,
        tween(180),
        label = "switchTrack"
    )
    val thumbOffset by animateDpAsState(
        if (checked) 20.dp else 0.dp,
        tween(180),
        label = "switchThumb"
    )
    val shape = RoundedCornerShape(percent = 50)
    Box(
        modifier
            .size(width = 51.dp, height = 31.dp)
            .clip(shape)
            .background(track)
            .toggleable(
                value = checked,
                role = Role.Switch,
                onValueChange = onCheckedChange
            ),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            Modifier
                .padding(start = 2.dp)
                .graphicsLayer { translationX = thumbOffset.toPx() }
                .size(27.dp)
                .shadow(3.dp, CircleShape, clip = false)
                .background(Color.White, CircleShape)
        )
    }
}

@Composable
fun SegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = Ios.colors
    val shape = RoundedCornerShape(9.dp)
    Row(
        modifier
            .height(32.dp)
            .clip(shape)
            .background(colors.fill)
            .padding(2.dp)
            .selectableGroup(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        options.forEachIndexed { index, option ->
            val selected = index == selectedIndex
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .padding(horizontal = 1.dp)
                    .then(
                        if (selected) {
                            Modifier.shadow(2.dp, RoundedCornerShape(7.dp), clip = false)
                        } else {
                            Modifier
                        }
                    )
                    .clip(RoundedCornerShape(7.dp))
                    .background(if (selected) colors.segmentSelected else Color.Transparent)
                    .selectable(
                        selected = selected,
                        role = Role.Tab,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onSelect(index) }
                    ),
                contentAlignment = Alignment.Center
            ) {
                IText(
                    option,
                    style = IosType.footnote.copy(
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
                    ),
                    color = if (selected) colors.label else colors.secondaryLabel,
                    maxLines = 1,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ---------------------------------------------------------------- text field

@Composable
fun IosTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    minHeight: Dp = 24.dp,
    textAlign: TextAlign = TextAlign.Start,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    val colors = Ios.colors
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = minHeight),
        textStyle = IosType.body.copy(color = colors.label, textAlign = textAlign),
        cursorBrush = SolidColor(colors.blue),
        singleLine = singleLine,
        keyboardOptions = keyboardOptions,
        decorationBox = { innerTextField ->
            Box(
                Modifier.fillMaxWidth(),
                contentAlignment = if (singleLine) Alignment.CenterStart else Alignment.TopStart
            ) {
                if (value.isEmpty()) {
                    IText(
                        placeholder,
                        style = IosType.body,
                        color = colors.tertiaryLabel,
                        maxLines = if (singleLine) 1 else 3,
                        textAlign = textAlign
                    )
                }
                innerTextField()
            }
        }
    )
}

// ---------------------------------------------------------------- screens

@Composable
private fun IosTopBar(
    title: String,
    collapse: () -> Float,
    onAdd: () -> Unit,
    leading: (@Composable () -> Unit)?,
    modifier: Modifier = Modifier
) {
    val colors = Ios.colors
    Box(modifier.fillMaxWidth()) {
        Box(
            Modifier
                .matchParentSize()
                .graphicsLayer { alpha = collapse() }
                .background(colors.background)
        )
        Box(
            Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .align(Alignment.BottomStart)
                .graphicsLayer { alpha = collapse() * 0.6f }
                .background(colors.separator)
        )
        Row(
            Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(54.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                leading?.invoke()
            }
            IText(
                title,
                modifier = Modifier.graphicsLayer {
                    alpha = ((collapse() - 0.35f) / 0.65f).coerceIn(0f, 1f)
                },
                style = IosType.headline,
                color = colors.label,
                maxLines = 1
            )
            Box(Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                GlassCircleButton(onClick = onAdd) {
                    PlusGlyph(colors.blue, size = 16.dp)
                }
            }
        }
    }
}

/**
 * iOS large-title screen: the big title scrolls with the content while a compact
 * translucent bar fades in above it once the list starts moving.
 */
@Composable
fun LargeTitleScreen(
    title: String,
    onAdd: () -> Unit,
    subtitle: String? = null,
    leading: (@Composable () -> Unit)? = null,
    content: LazyListScope.() -> Unit
) {
    val colors = Ios.colors
    val listState = rememberLazyListState()
    val collapseState = remember(listState) {
        derivedStateOf {
            if (listState.firstVisibleItemIndex > 0) {
                1f
            } else {
                (listState.firstVisibleItemScrollOffset / 60f).coerceIn(0f, 1f)
            }
        }
    }

    Box(Modifier.fillMaxSize().background(colors.background)) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().statusBarsPadding(),
            contentPadding = PaddingValues(top = 4.dp, bottom = 120.dp)
        ) {
            item(key = "screen-large-title") {
                Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 14.dp)) {
                    IText(title, style = IosType.largeTitle, color = colors.label, maxLines = 1)
                    if (subtitle != null) {
                        Spacer(Modifier.height(2.dp))
                        IText(subtitle, style = IosType.subhead, color = colors.secondaryLabel, maxLines = 1)
                    }
                }
            }
            content()
        }
        IosTopBar(
            title = title,
            collapse = { collapseState.value },
            onAdd = onAdd,
            leading = leading,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Composable
fun EmptyState(tab: AppTab, title: String, message: String) {
    val colors = Ios.colors
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 72.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(colors.fill),
            contentAlignment = Alignment.Center
        ) {
            TabGlyph(tab, colors.secondaryLabel)
        }
        Spacer(Modifier.height(16.dp))
        IText(title, style = IosType.headline, color = colors.label, textAlign = TextAlign.Center)
        Spacer(Modifier.height(6.dp))
        IText(message, style = IosType.subhead, color = colors.secondaryLabel, textAlign = TextAlign.Center)
    }
}

// ---------------------------------------------------------------- tab bar

@Composable
fun IosTabBar(selected: AppTab, onSelect: (AppTab) -> Unit, modifier: Modifier = Modifier) {
    val colors = Ios.colors
    val haptics = LocalHapticFeedback.current
    Row(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .glass(RoundedCornerShape(30.dp), colors)
            .padding(vertical = 7.dp, horizontal = 6.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppTab.entries.forEach { tab ->
            val isSelected = tab == selected
            val tint by animateColorAsState(
                if (isSelected) colors.blue else colors.secondaryLabel,
                tween(180),
                label = "tabTint"
            )
            Column(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .iosClick {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onSelect(tab)
                    }
                    .padding(vertical = 5.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TabGlyph(tab, tint)
                Spacer(Modifier.height(3.dp))
                IText(tab.label, style = IosType.tabLabel, color = tint, maxLines = 1)
            }
        }
    }
}

@Composable
fun FloatingTabBar(selected: AppTab, onSelect: (AppTab) -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = 6.dp)
    ) {
        IosTabBar(selected = selected, onSelect = onSelect)
    }
}
