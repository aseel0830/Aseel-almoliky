package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.ItemEntity
import com.example.ui.DisplayLayoutMode
import com.example.util.WireColor

@Composable
fun ItemCard(
    item: ItemEntity,
    onSetExactSingle: (Int) -> Unit,
    onSetExactTriple: (Int, Int) -> Unit,
    onUpdateBoxColor: ((boxIndex: Int, newColor: String) -> Unit)? = null,
    onEdit: () -> Unit,
    onOpenImage: (ItemEntity) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    displayMode: DisplayLayoutMode = DisplayLayoutMode.STANDARD
) {
    var showExactInputDialog by remember { mutableStateOf(false) }
    var selectedTripleIndex by remember { mutableIntStateOf(1) }
    var currentEditingInitialValue by remember { mutableIntStateOf(0) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showThreeDotsMenu by remember { mutableStateOf(false) }

    var showColorPickerForBox by remember { mutableStateOf<Int?>(null) }

    val hasQty = item.hasQuantity()
    val borderColor = if (hasQty) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
    }

    // Dynamic sizing based on displayLayoutMode
    val cardPadding = when (displayMode) {
        DisplayLayoutMode.COMPACT -> 8.dp
        DisplayLayoutMode.COMFORTABLE -> 18.dp
        DisplayLayoutMode.GRID_2COL -> 10.dp
        else -> 14.dp
    }
    val imageSize = when (displayMode) {
        DisplayLayoutMode.COMPACT -> 44.dp
        DisplayLayoutMode.COMFORTABLE -> 64.dp
        DisplayLayoutMode.GRID_2COL -> 48.dp
        else -> 54.dp
    }
    val titleFontSize = when (displayMode) {
        DisplayLayoutMode.COMPACT -> 14.sp
        DisplayLayoutMode.COMFORTABLE -> 18.sp
        DisplayLayoutMode.GRID_2COL -> 13.sp
        else -> 16.sp
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("item_card_${item.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (hasQty) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(listOf(borderColor, borderColor))
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (hasQty) 3.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(cardPadding)
        ) {
            // Header: Image, Title, Unit badge, Three-dots menu (for Edit/Delete)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Item Image (clickable to view large image)
                Box(
                    modifier = Modifier
                        .size(imageSize)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .clickable { onOpenImage(item) }
                        .testTag("open_item_image_${item.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    if (!item.imageUri.isNullOrBlank()) {
                        AsyncImage(
                            model = item.imageUri,
                            contentDescription = "عرض صورة ${item.name}",
                            modifier = Modifier
                                .size(imageSize)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.ElectricBolt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(imageSize * 0.5f)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Name and details
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = titleFontSize,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = if (displayMode == DisplayLayoutMode.GRID_2COL) 2 else 3,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        // Unit badge
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                        ) {
                            Text(
                                text = "الوحدة: ${item.unit.ifBlank { "حبة" }}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        // Single Mode Wire Color badge if present
                        if (item.quantityMode != "TRIPLE_COLOR" && !item.wireColor.isNullOrBlank()) {
                            val wireEnum = WireColor.fromLabel(item.wireColor)
                            WireColorBadge(wireEnum = wireEnum, label = item.wireColor)
                        }
                    }
                }

                // Three Dots Menu for Item Edit and Delete (Requirement: عبر الضغط على ثلاث نقاط في النافذة وخروج قائمة منسدلة حذف وتعديل)
                Box {
                    IconButton(
                        onClick = { showThreeDotsMenu = true },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("item_menu_btn_${item.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "خيارات الصنف",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showThreeDotsMenu,
                        onDismissRequest = { showThreeDotsMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("تعديل الصنف", fontWeight = FontWeight.SemiBold)
                                }
                            },
                            onClick = {
                                showThreeDotsMenu = false
                                onEdit()
                            }
                        )

                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("حذف الصنف", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                                }
                            },
                            onClick = {
                                showThreeDotsMenu = false
                                showDeleteConfirmDialog = true
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(if (displayMode == DisplayLayoutMode.COMPACT) 8.dp else 12.dp))

            // Manual Quantity Input Section (No - and + buttons!)
            if (item.quantityMode == "TRIPLE_COLOR") {
                // Triple Mode: 3 manual input boxes with dedicated colors
                Text(
                    text = "٣ خانات للكمية مع لون مخصص (اضغط لتسجيل الرقم يدوياً):",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                val box1Color = item.c1 ?: item.wireColor ?: "أحمر"
                val box2Color = item.c2 ?: item.wireColor ?: "أزرق"
                val box3Color = item.c3 ?: item.wireColor ?: "أخضر مخطط بأصفر"

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ManualCounterBox(
                        title = "خانة 1",
                        colorName = box1Color,
                        value = item.q1,
                        unit = item.unit.ifBlank { "لفة" },
                        onClick = {
                            selectedTripleIndex = 1
                            currentEditingInitialValue = item.q1
                            showExactInputDialog = true
                        },
                        onColorClick = { showColorPickerForBox = 1 },
                        modifier = Modifier.weight(1f)
                    )

                    ManualCounterBox(
                        title = "خانة 2",
                        colorName = box2Color,
                        value = item.q2,
                        unit = item.unit.ifBlank { "لفة" },
                        onClick = {
                            selectedTripleIndex = 2
                            currentEditingInitialValue = item.q2
                            showExactInputDialog = true
                        },
                        onColorClick = { showColorPickerForBox = 2 },
                        modifier = Modifier.weight(1f)
                    )

                    ManualCounterBox(
                        title = "خانة 3",
                        colorName = box3Color,
                        value = item.q3,
                        unit = item.unit.ifBlank { "لفة" },
                        onClick = {
                            selectedTripleIndex = 3
                            currentEditingInitialValue = item.q3
                            showExactInputDialog = true
                        },
                        onColorClick = { showColorPickerForBox = 3 },
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                // Single Mode: Clean, prominent manual entry card without - and + stepper buttons
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (item.quantity > 0) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.linearGradient(
                            listOf(
                                if (item.quantity > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                if (item.quantity > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                        )
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            currentEditingInitialValue = item.quantity
                            showExactInputDialog = true
                        }
                        .testTag("quantity_manual_entry_${item.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = if (displayMode == DisplayLayoutMode.COMPACT) 10.dp else 14.dp,
                                vertical = if (displayMode == DisplayLayoutMode.COMPACT) 6.dp else 10.dp
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.EditNote,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(if (displayMode == DisplayLayoutMode.COMPACT) 20.dp else 24.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "الكمية (يدوي):",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (item.quantity > 0) "${item.quantity} ${item.unit.ifBlank { "حبة" }}" else "انقر لكتابة الرقم",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (item.quantity > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Big prominent numeric badge
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (item.quantity > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(
                                    horizontal = if (displayMode == DisplayLayoutMode.COMPACT) 10.dp else 14.dp,
                                    vertical = if (displayMode == DisplayLayoutMode.COMPACT) 4.dp else 6.dp
                                )
                            ) {
                                Text(
                                    text = "${item.quantity}",
                                    style = if (displayMode == DisplayLayoutMode.COMPACT) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (item.quantity > 0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = item.unit.ifBlank { "حبة" },
                                    fontSize = if (displayMode == DisplayLayoutMode.COMPACT) 10.sp else 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (item.quantity > 0) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showExactInputDialog) {
        QuantityInputDialog(
            itemName = item.name,
            initialQuantity = currentEditingInitialValue,
            label = if (item.quantityMode == "TRIPLE_COLOR") "الكمية للخانة رقم $selectedTripleIndex" else "الكمية المطلوبة يدويّاً",
            onDismiss = { showExactInputDialog = false },
            onConfirm = { newQty ->
                if (item.quantityMode == "TRIPLE_COLOR") {
                    onSetExactTriple(selectedTripleIndex, newQty)
                } else {
                    onSetExactSingle(newQty)
                }
                showExactInputDialog = false
            }
        )
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = {
                Text(
                    text = "تأكيد حذف العنصر",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Text(
                    text = "هل أنت متأكد من حذف \"${item.name}\" من هذه النافذة؟",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDelete()
                    }
                ) {
                    Text(
                        text = "حذف",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Color picker dialog for specific box
    showColorPickerForBox?.let { boxIdx ->
        BoxColorPickerDialog(
            boxNumber = boxIdx,
            onDismiss = { showColorPickerForBox = null },
            onColorChosen = { chosenColor ->
                onUpdateBoxColor?.invoke(boxIdx, chosenColor.labelAr)
                showColorPickerForBox = null
            }
        )
    }
}

@Composable
fun ManualCounterBox(
    title: String,
    colorName: String? = null,
    value: Int,
    unit: String,
    onClick: () -> Unit,
    onColorClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val wireEnum = WireColor.fromLabel(colorName)

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (value > 0) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = CardDefaults.outlinedCardBorder(),
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Box title
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(3.dp))

            // Color badge for this box (Clickable to change color)
            if (!colorName.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .then(
                            if (onColorClick != null) Modifier.clickable { onColorClick() } else Modifier
                        )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        if (wireEnum != null) {
                            ColorCircleIndicator(color = wireEnum, size = 9)
                        }
                        Text(
                            text = colorName,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Number display badge (tappable to type manually, without - and + buttons!)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (value > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                    )
                    .border(
                        width = 1.dp,
                        color = if (value > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$value",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = if (value > 0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = unit,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Normal,
                        color = if (value > 0) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BoxColorPickerDialog(
    boxNumber: Int,
    onDismiss: () -> Unit,
    onColorChosen: (WireColor) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "اختيار لون السلك للخانة ($boxNumber):",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Column {
                Text(
                    text = "اختر اللون المطلوب من الألوان الستة المعتمدة:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    WireColor.entries.forEach { wc ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = CardDefaults.outlinedCardBorder(),
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { onColorChosen(wc) }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                ColorCircleIndicator(color = wc, size = 14)
                                Text(
                                    text = wc.labelAr,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

@Composable
fun WireColorBadge(
    wireEnum: WireColor?,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = CardDefaults.outlinedCardBorder(),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (wireEnum != null) {
                ColorCircleIndicator(color = wireEnum, size = 12)
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
