package com.example.ui

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.local.ItemEntity
import com.example.data.local.ProjectEntity
import com.example.data.local.StageEntity
import com.example.ui.components.AddItemDialog
import com.example.ui.components.AddStageDialog
import com.example.ui.components.CloudSyncDialog
import com.example.ui.components.EditItemDialog
import com.example.ui.components.EditStageDialog
import com.example.ui.components.ItemCard
import com.example.ui.components.PinLockScreen
import com.example.ui.components.ProjectsDialog
import com.example.ui.components.SettingsDialog
import com.example.ui.components.SummaryBottomSheet
import com.example.ui.components.ViewImageDialog
import com.example.util.ShareHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val isUnlocked by viewModel.isUnlocked.collectAsStateWithLifecycle()
    val isPinEnabled by viewModel.isPinEnabled.collectAsStateWithLifecycle()
    val stages by viewModel.stages.collectAsStateWithLifecycle()
    val projects by viewModel.projects.collectAsStateWithLifecycle()
    val currentProject by viewModel.currentProject.collectAsStateWithLifecycle()
    val selectedStageName by viewModel.selectedStageName.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val filterMode by viewModel.filterMode.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val displayLayoutMode by viewModel.displayLayoutMode.collectAsStateWithLifecycle()
    val isSearchBarVisible by viewModel.isSearchBarVisible.collectAsStateWithLifecycle()
    val displayedItems by viewModel.displayedItems.collectAsStateWithLifecycle()
    val totalSelectedCount by viewModel.totalSelectedCount.collectAsStateWithLifecycle()
    val technicianName by viewModel.technicianName.collectAsStateWithLifecycle()
    val technicianPhone by viewModel.technicianPhone.collectAsStateWithLifecycle()

    var showThreeDotsMenu by remember { mutableStateOf(false) }
    var showAddItemDialog by remember { mutableStateOf(false) }
    var showAddStageDialog by remember { mutableStateOf(false) }
    var showSummarySheet by remember { mutableStateOf(false) }
    var showProjectsDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var showCloudSyncDialog by remember { mutableStateOf(false) }

    var itemToEdit by remember { mutableStateOf<ItemEntity?>(null) }
    var itemForImageView by remember { mutableStateOf<ItemEntity?>(null) }
    var stageToEdit by remember { mutableStateOf<StageEntity?>(null) }

    // File picker launcher for importing/uploading a shared project file (.json)
    val importFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val success = viewModel.importProjectFromFile(context, uri)
                if (success) {
                    Toast.makeText(context, "تم رفع واستيراد المشروع بنجاح!", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "فشل قراءة ملف المشروع، يرجى التأكد من صحة الملف", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // File picker launcher specifically for Manual Restore from Google Drive (Requirement)
    val restoreFromDriveLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val success = viewModel.importProjectFromFile(context, uri)
                if (success) {
                    Toast.makeText(context, "تمت استعادة بيانات المشروع من Google Drive بنجاح! 📥", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "فشل استعادة النسخة الاحتياطية من Google Drive، يرجى فحص الملف", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // 4-Digit PIN Security Screen Check (Real lock)
    if (isPinEnabled && !isUnlocked) {
        PinLockScreen(
            technicianName = technicianName,
            onVerifyPin = { enteredPin ->
                viewModel.unlockWithPin(enteredPin)
            }
        )
        return
    }

    // Sizing factors driven by displayLayoutMode across entire UI
    val topBarIconSize = when (displayLayoutMode) {
        DisplayLayoutMode.COMPACT -> 32.dp
        DisplayLayoutMode.COMFORTABLE -> 44.dp
        else -> 38.dp
    }
    val topBarTitleSize = when (displayLayoutMode) {
        DisplayLayoutMode.COMPACT -> 14.sp
        DisplayLayoutMode.COMFORTABLE -> 18.sp
        else -> 16.sp
    }
    val topBarSubtitleSize = when (displayLayoutMode) {
        DisplayLayoutMode.COMPACT -> 10.sp
        DisplayLayoutMode.COMFORTABLE -> 13.sp
        else -> 11.sp
    }
    val searchBarHorizontalPadding = when (displayLayoutMode) {
        DisplayLayoutMode.COMPACT -> 10.dp
        DisplayLayoutMode.COMFORTABLE -> 20.dp
        else -> 16.dp
    }
    val bottomTabFontSize = when (displayLayoutMode) {
        DisplayLayoutMode.COMPACT -> 11.sp
        DisplayLayoutMode.COMFORTABLE -> 15.sp
        else -> 13.sp
    }
    val bottomStageIconSize = when (displayLayoutMode) {
        DisplayLayoutMode.COMPACT -> 16.dp
        DisplayLayoutMode.COMFORTABLE -> 24.dp
        else -> 20.dp
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface)) {
                // Top App Bar: Strictly the three dots menu in the action area as requested
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(topBarIconSize)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.ElectricBolt,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(topBarIconSize * 0.58f)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = technicianName,
                                    fontSize = topBarTitleSize,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "حصر وتوريد الكهرباء",
                                    fontSize = topBarSubtitleSize,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    },
                    actions = {
                        // The requested 3-dots overflow menu containing:
                        // الكل - المحدد فقط - غير المحدد - زر تصفير الحصر - ادارة المشاريع - بار البحث عن العناصر
                        Box {
                            IconButton(
                                onClick = { showThreeDotsMenu = true },
                                modifier = Modifier.testTag("top_overflow_menu_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "القائمة",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            DropdownMenu(
                                expanded = showThreeDotsMenu,
                                onDismissRequest = { showThreeDotsMenu = false },
                                modifier = Modifier.testTag("dropdown_menu_options")
                            ) {
                                // 1. الكل
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("الكل", fontWeight = if (filterMode == FilterMode.ALL) FontWeight.Bold else FontWeight.Normal)
                                            if (filterMode == FilterMode.ALL) {
                                                Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    },
                                    onClick = {
                                        viewModel.setFilterMode(FilterMode.ALL)
                                        showThreeDotsMenu = false
                                    }
                                )

                                // 2. المحدد فقط
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                "المحدد فقط ($totalSelectedCount)",
                                                fontWeight = if (filterMode == FilterMode.SELECTED_ONLY) FontWeight.Bold else FontWeight.Normal,
                                                color = if (totalSelectedCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                            )
                                            if (filterMode == FilterMode.SELECTED_ONLY) {
                                                Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    },
                                    onClick = {
                                        viewModel.setFilterMode(FilterMode.SELECTED_ONLY)
                                        showThreeDotsMenu = false
                                    }
                                )

                                // 3. غير المحدد
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("غير المحدد", fontWeight = if (filterMode == FilterMode.UNSELECTED_ONLY) FontWeight.Bold else FontWeight.Normal)
                                            if (filterMode == FilterMode.UNSELECTED_ONLY) {
                                                Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    },
                                    onClick = {
                                        viewModel.setFilterMode(FilterMode.UNSELECTED_ONLY)
                                        showThreeDotsMenu = false
                                    }
                                )

                                HorizontalDivider()

                                // 4. زر تصفير الحصر
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.RestartAlt, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("تصفير الحصر", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                                        }
                                    },
                                    onClick = {
                                        showThreeDotsMenu = false
                                        showResetConfirmDialog = true
                                    }
                                )

                                HorizontalDivider()

                                // 5. إدارة المشاريع
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Folder, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("إدارة المشاريع", fontWeight = FontWeight.SemiBold)
                                        }
                                    },
                                    onClick = {
                                        showThreeDotsMenu = false
                                        showProjectsDialog = true
                                    }
                                )

                                // 6. بار البحث عن العناصر
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(if (isSearchBarVisible) "إخفاء شريط البحث" else "بار البحث عن العناصر", fontWeight = FontWeight.SemiBold)
                                        }
                                    },
                                    onClick = {
                                        viewModel.toggleSearchBar()
                                        showThreeDotsMenu = false
                                    }
                                )

                                HorizontalDivider()

                                // 7. الإعدادات
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("الإعدادات (حساب جوجل، الأبعاد والأمان)", fontWeight = FontWeight.SemiBold)
                                        }
                                    },
                                    onClick = {
                                        showThreeDotsMenu = false
                                        showSettingsDialog = true
                                    }
                                )

                                // 8. قفل التطبيق إذا كان الرمز مفعلاً
                                if (isPinEnabled) {
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("قفل التطبيق برمز سري", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                                            }
                                        },
                                        onClick = {
                                            showThreeDotsMenu = false
                                            viewModel.lockApp()
                                        }
                                    )
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )

                // Optional Search Bar (opened from the 3 dots menu or if text is present)
                if (isSearchBarVisible || searchQuery.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = searchBarHorizontalPadding, vertical = 4.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            placeholder = { Text("بحث سريع عن صنف أو مادة كهربائية...") },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            },
                            trailingIcon = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (searchQuery.isNotBlank()) {
                                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                            Icon(Icons.Default.Clear, contentDescription = "مسح البحث")
                                        }
                                    }
                                    IconButton(onClick = {
                                        viewModel.setSearchQuery("")
                                        viewModel.setSearchBarVisible(false)
                                    }) {
                                        Icon(Icons.Default.Close, contentDescription = "إغلاق شريط البحث")
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("fast_search_bar")
                        )
                    }
                }

                // Active filter status indicator if filtering by selected/unselected
                if (filterMode != FilterMode.ALL) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.setFilterMode(FilterMode.ALL) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.FilterList, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "تصفية: ${filterMode.label} (${displayedItems.size} صنف)",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Text("عرض الكل ✕", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        bottomBar = {
            // Windows/Stages Navigation Bar with Fixed Pinned "كشف الحصر" Button on the Right (Requirement)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .navigationBarsPadding()
            ) {
                // Stage actions bar: Current Stage Name & Edit button
                val currentStageEntity = stages.firstOrNull { it.name == selectedStageName }
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = searchBarHorizontalPadding,
                                vertical = if (displayLayoutMode == DisplayLayoutMode.COMPACT) 2.dp else 4.dp
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (!currentStageEntity?.imageUri.isNullOrBlank()) {
                                AsyncImage(
                                    model = currentStageEntity!!.imageUri,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(if (displayLayoutMode == DisplayLayoutMode.COMPACT) 20.dp else 24.dp)
                                        .clip(RoundedCornerShape(6.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(
                                text = "النافذة: $selectedStageName",
                                style = MaterialTheme.typography.titleSmall,
                                fontSize = if (displayLayoutMode == DisplayLayoutMode.COMPACT) 12.sp else 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "(${displayedItems.size} صنف)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (currentStageEntity != null) {
                                TextButton(
                                    onClick = { stageToEdit = currentStageEntity },
                                    modifier = Modifier.testTag("open_stage_edit_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("تعديل / حذف النافذة", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Row with PINNED "كشف الحصر" Button on the right (RTL start), and horizontally scrollable stages on the left
                val selectedIndex = stages.indexOfFirst { it.name == selectedStageName }.coerceAtLeast(0)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Pinned Fixed "كشف الحصر" Button (Requirement: ثابت على اليمين بجانب النوافذ ولا يتحرك)
                    Surface(
                        onClick = { showSummarySheet = true },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primary,
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .padding(start = 8.dp, end = 4.dp, top = 4.dp, bottom = 4.dp)
                            .testTag("pinned_summary_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(
                                horizontal = if (displayLayoutMode == DisplayLayoutMode.COMPACT) 10.dp else 12.dp,
                                vertical = if (displayLayoutMode == DisplayLayoutMode.COMPACT) 7.dp else 9.dp
                            ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.ListAlt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "كشف الحصر ($totalSelectedCount)",
                                fontWeight = FontWeight.Bold,
                                fontSize = if (displayLayoutMode == DisplayLayoutMode.COMPACT) 11.sp else 13.sp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }

                    // Vertical Separator
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(28.dp)
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                    )

                    // Horizontally Scrollable Stage Navigation Tabs
                    ScrollableTabRow(
                        selectedTabIndex = selectedIndex,
                        edgePadding = 4.dp,
                        containerColor = MaterialTheme.colorScheme.surface,
                        indicator = { tabPositions ->
                            if (selectedIndex < tabPositions.size) {
                                TabRowDefaults.SecondaryIndicator(
                                    Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        stages.forEach { stage ->
                            val isSelected = stage.name == selectedStageName
                            Tab(
                                selected = isSelected,
                                onClick = { viewModel.setSelectedStage(stage.name) },
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        if (!stage.imageUri.isNullOrBlank()) {
                                            AsyncImage(
                                                model = stage.imageUri,
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .size(bottomStageIconSize)
                                                    .clip(RoundedCornerShape(4.dp)),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                        Text(
                                            text = stage.name,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = bottomTabFontSize,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                modifier = Modifier.testTag("stage_tab_${stage.name}")
                            )
                        }

                        // Button to add another stage/window ("إضافة نافذة")
                        Tab(
                            selected = false,
                            onClick = { showAddStageDialog = true },
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "إضافة نافذة",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = bottomTabFontSize,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
                            modifier = Modifier.testTag("add_stage_tab_button")
                        )
                    }
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        // Single unified, continuous scroll area adapting to displayLayoutMode
        if (displayLayoutMode == DisplayLayoutMode.GRID_2COL) {
            // 2-Column Grid Layout Mode
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item(span = { GridItemSpan(2) }) {
                    ActiveProjectBanner(
                        projectName = currentProject?.name,
                        displayMode = displayLayoutMode,
                        onClick = { showProjectsDialog = true }
                    )
                }

                if (displayedItems.isEmpty()) {
                    item(span = { GridItemSpan(2) }) {
                        EmptyItemsView(isSearching = searchQuery.isNotBlank())
                    }
                } else {
                    items(displayedItems, key = { it.id }) { item ->
                        ItemCard(
                            item = item,
                            displayMode = displayLayoutMode,
                            onSetExactSingle = { newQty -> viewModel.setExactSingleQuantity(item, newQty) },
                            onSetExactTriple = { idx, newQty -> viewModel.setExactTripleQuantity(item, idx, newQty) },
                            onUpdateBoxColor = { boxIdx, newColor -> viewModel.updateBoxColor(item, boxIdx, newColor) },
                            onEdit = { itemToEdit = item },
                            onOpenImage = { itemForImageView = it },
                            onDelete = { viewModel.deleteItem(item) }
                        )
                    }
                }

                item(span = { GridItemSpan(2) }) {
                    AddItemBottomSection(
                        stageName = selectedStageName,
                        displayMode = displayLayoutMode,
                        onAddClick = { showAddItemDialog = true }
                    )
                }
            }
        } else {
            // Standard / Compact / Comfortable List Layout Modes
            val horizontalPadding = when (displayLayoutMode) {
                DisplayLayoutMode.COMPACT -> 10.dp
                DisplayLayoutMode.COMFORTABLE -> 20.dp
                else -> 16.dp
            }
            val spacing = when (displayLayoutMode) {
                DisplayLayoutMode.COMPACT -> 6.dp
                DisplayLayoutMode.COMFORTABLE -> 14.dp
                else -> 10.dp
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(horizontal = horizontalPadding, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(spacing)
            ) {
                // Active Project Banner
                item {
                    ActiveProjectBanner(
                        projectName = currentProject?.name,
                        displayMode = displayLayoutMode,
                        onClick = { showProjectsDialog = true }
                    )
                }

                if (displayedItems.isEmpty()) {
                    item {
                        EmptyItemsView(isSearching = searchQuery.isNotBlank())
                    }
                } else {
                    items(displayedItems, key = { it.id }) { item ->
                        ItemCard(
                            item = item,
                            displayMode = displayLayoutMode,
                            onSetExactSingle = { newQty -> viewModel.setExactSingleQuantity(item, newQty) },
                            onSetExactTriple = { idx, newQty -> viewModel.setExactTripleQuantity(item, idx, newQty) },
                            onUpdateBoxColor = { boxIdx, newColor -> viewModel.updateBoxColor(item, boxIdx, newColor) },
                            onEdit = { itemToEdit = item },
                            onOpenImage = { itemForImageView = it },
                            onDelete = { viewModel.deleteItem(item) }
                        )
                    }
                }

                // Add Item button at the bottom of the list
                item {
                    AddItemBottomSection(
                        stageName = selectedStageName,
                        displayMode = displayLayoutMode,
                        onAddClick = { showAddItemDialog = true }
                    )
                }
            }
        }
    }

    // Add Item Dialog
    if (showAddItemDialog) {
        AddItemDialog(
            stageName = selectedStageName,
            onDismiss = { showAddItemDialog = false },
            onConfirm = { name, imageUri, quantityMode, quantity, q1, q2, q3, wireColor, c1, c2, c3, unit ->
                viewModel.addNewItem(
                    name = name,
                    stageName = selectedStageName,
                    imageUri = imageUri,
                    quantityMode = quantityMode,
                    quantity = quantity,
                    q1 = q1,
                    q2 = q2,
                    q3 = q3,
                    wireColor = wireColor,
                    c1 = c1,
                    c2 = c2,
                    c3 = c3,
                    unit = unit
                )
                showAddItemDialog = false
            }
        )
    }

    // Edit Item Dialog
    if (itemToEdit != null) {
        EditItemDialog(
            item = itemToEdit!!,
            onDismiss = { itemToEdit = null },
            onConfirm = { updated ->
                viewModel.updateItem(updated)
                itemToEdit = null
            }
        )
    }

    // View Large Image Dialog
    if (itemForImageView != null) {
        ViewImageDialog(
            itemName = itemForImageView!!.name,
            imageUri = itemForImageView!!.imageUri,
            stageName = itemForImageView!!.stageName,
            onDismiss = { itemForImageView = null }
        )
    }

    // Edit Stage Dialog
    if (stageToEdit != null) {
        EditStageDialog(
            stage = stageToEdit!!,
            itemCount = displayedItems.size,
            onDismiss = { stageToEdit = null },
            onUpdate = { newName, newImageUri ->
                viewModel.updateStage(stageToEdit!!, newName, newImageUri)
                stageToEdit = null
            },
            onDelete = {
                viewModel.deleteStage(stageToEdit!!)
                stageToEdit = null
            }
        )
    }

    // Add Stage Dialog
    if (showAddStageDialog) {
        AddStageDialog(
            onDismiss = { showAddStageDialog = false },
            onConfirm = { stageName, imageUri ->
                viewModel.addNewStage(stageName, imageUri)
                showAddStageDialog = false
            }
        )
    }

    // Settings Dialog with Screen Layout Modes, Google Account Sign-In, Manual Restore from Drive, and Instant Lock
    if (showSettingsDialog) {
        SettingsDialog(
            technicianName = technicianName,
            technicianPhone = technicianPhone,
            isDarkMode = isDarkMode,
            isPinEnabled = isPinEnabled,
            displayLayoutMode = displayLayoutMode,
            onUpdateTechnicianName = { viewModel.setTechnicianName(it) },
            onUpdateTechnicianPhone = { viewModel.setTechnicianPhone(it) },
            onToggleDarkMode = { viewModel.toggleDarkMode() },
            onSelectDisplayLayoutMode = { viewModel.setDisplayLayoutMode(it) },
            onOpenCloudSync = { showCloudSyncDialog = true },
            onRestoreFromGoogleDriveFile = {
                // Open file picker to select a backup file from Google Drive (Requirement)
                restoreFromDriveLauncher.launch("*/*")
            },
            onSetPin = { newPin -> viewModel.setAppPin(newPin) },
            onDisablePin = { viewModel.disablePin() },
            onLockAppNow = { viewModel.lockApp() },
            onDismiss = { showSettingsDialog = false }
        )
    }

    // Summary Bottom Sheet
    if (showSummarySheet) {
        SummaryBottomSheet(
            project = currentProject,
            technicianName = technicianName,
            technicianPhone = technicianPhone,
            onUpdateClientName = { viewModel.updateClientName(it) },
            itemsByStage = viewModel.getItemsByStageMap(),
            onDismiss = { showSummarySheet = false }
        )
    }

    // Projects Dialog with Export / Download & Import / Upload
    if (showProjectsDialog) {
        ProjectsDialog(
            projects = projects,
            currentProject = currentProject,
            onDismiss = { showProjectsDialog = false },
            onSaveNewProject = { name, client, notes, preserveQuantities ->
                viewModel.saveAsNewProject(name, client, notes, preserveQuantities)
                showProjectsDialog = false
            },
            onSwitchProject = { id ->
                viewModel.switchProject(id)
                showProjectsDialog = false
            },
            onDeleteProject = { proj ->
                viewModel.deleteProject(proj)
            },
            onExportProjectFile = { proj ->
                scope.launch {
                    val file = viewModel.exportProjectFile(context, proj)
                    if (file != null) {
                        ShareHelper.shareFile(
                            context = context,
                            file = file,
                            mimeType = "application/json",
                            title = "مشاركة ملف مشروع ${proj.name}"
                        )
                    } else {
                        Toast.makeText(context, "فشل تصدير ملف المشروع", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onImportProjectFile = {
                importFileLauncher.launch("*/*")
            },
            onExportBackup = {
                showCloudSyncDialog = true
            }
        )
    }

    // Google Cloud Sync Dialog (Opened through Settings or Project backup)
    if (showCloudSyncDialog) {
        CloudSyncDialog(
            cloudSyncService = viewModel.cloudSyncService,
            onDismiss = { showCloudSyncDialog = false }
        )
    }

    // Reset Quantities Confirmation Dialog
    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = { Text("تصفير الكميات", fontWeight = FontWeight.Bold) },
            text = { Text("هل أنت متأكد من تصفير كافة الكميات المحددة لهذا المشروع؟ لن يتم حذف الأصناف أو النوافذ، فقط سيتم إعادة الكمية إلى 0.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetCurrentQuantities()
                        showResetConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("نعم، تصفير", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
private fun ActiveProjectBanner(
    projectName: String?,
    displayMode: DisplayLayoutMode,
    onClick: () -> Unit
) {
    val verticalPadding = when (displayMode) {
        DisplayLayoutMode.COMPACT -> 4.dp
        DisplayLayoutMode.COMFORTABLE -> 12.dp
        else -> 8.dp
    }
    val titleSize = when (displayMode) {
        DisplayLayoutMode.COMPACT -> 12.sp
        DisplayLayoutMode.COMFORTABLE -> 16.sp
        else -> 14.sp
    }

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .testTag("project_banner")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = verticalPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(if (displayMode == DisplayLayoutMode.COMPACT) 18.dp else 20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "المشروع: ${projectName ?: "مشروع جديد"}",
                    fontSize = titleSize,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            ) {
                Text(
                    text = "إدارة المشاريع 📂",
                    fontSize = if (displayMode == DisplayLayoutMode.COMPACT) 10.sp else 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyItemsView(isSearching: Boolean) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.ElectricBolt,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = if (isSearching) "لم يتم العثور على عناصر تطابق البحث" else "لا توجد عناصر في هذه النافذة بعد",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "اضغط على زر (+ إضافة عنصر) بالأسفل لتسجيل صنف جديد",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun AddItemBottomSection(
    stageName: String,
    displayMode: DisplayLayoutMode,
    onAddClick: () -> Unit
) {
    val buttonHeight = when (displayMode) {
        DisplayLayoutMode.COMPACT -> 42.dp
        DisplayLayoutMode.COMFORTABLE -> 56.dp
        else -> 50.dp
    }
    val buttonFontSize = when (displayMode) {
        DisplayLayoutMode.COMPACT -> 12.sp
        DisplayLayoutMode.COMFORTABLE -> 16.sp
        else -> 14.sp
    }

    Column {
        Spacer(modifier = Modifier.height(6.dp))
        Button(
            onClick = onAddClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(buttonHeight)
                .testTag("add_item_bottom_button")
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "+ إضافة عنصر إلى نافذة ($stageName)",
                fontWeight = FontWeight.Bold,
                fontSize = buttonFontSize
            )
        }
        Spacer(modifier = Modifier.height(60.dp))
    }
}
