package com.example.app.ui.auth.register.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.app.ui.auth.register.RegisterViewModel
import com.example.app.ui.auth.components.AppNotice
import com.example.app.ui.auth.components.BackIconButton
import com.example.app.ui.auth.components.NoticeType
import com.example.app.ui.auth.components.PrimaryButton
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterDateScreen(
    viewModel: RegisterViewModel = viewModel(),
    onBack: () -> Unit = {},
    onNext: () -> Unit = {}
) {
    val bg = Color(0xFF0F1B21)
    val card = Color(0xFF14232B)
    val outline = Color(0xFF2C3E48)
    val blue = Color(0xFF1877F2)
    val sheet = Color(0xFF14232B)

    val sheetBg = Color(0xFF2B3338)
    val centerBand = Color(0xFF3A4248)

    val today = remember { LocalDate.now() }
    val minDate = remember { LocalDate.of(1900, 1, 1) }

    val user by viewModel.user.collectAsState()
    val savedDateString = user.dateOfBirth
    if (savedDateString.isNotEmpty()) {
        LocalDate.parse(savedDateString)
    } else {
        today
    }

    val isAgeValid by viewModel.isAgeValid.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    val selectedDate by viewModel.selectedDate.collectAsState()
    val displayDate = selectedDate ?: today
    var showSheet by remember { mutableStateOf(false) }

    val formatter = remember {
        DateTimeFormatter.ofPattern(
            "dd 'tháng' MM, yyyy",
            Locale.Builder()
                .setLanguage("vi")
                .setRegion("VN")
                .build()
        )
    }

    LaunchedEffect(Unit) {
        viewModel.clearError()
    }

    Surface(modifier = Modifier.fillMaxSize(), color = bg) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 45.dp)
                .clip(RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp))
                .background(sheet)
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ){
                BackIconButton(onBack)
                Spacer(Modifier.weight(1f))
                PrimaryButton(
                    text = "Tiếp",
                    enabled = isAgeValid,
                    onClick = {
                        val finalDate = selectedDate ?: today
                        viewModel.updateDateOfBirth(finalDate.toString())
                        onNext()
                    }
                )
            }
            Spacer(Modifier.height(8.dp))

            Text(
                text = "Ngày sinh của bạn là khi nào?",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = "Hãy sử dụng ngày sinh của chính bạn, ngay cả khi tài khoản này dành cho doanh nghiệp, thú cưng hay chủ đề khác. " +
                        "Thông tin này sẽ không hiển thị với bất kỳ ai trừ khi bạn chọn chia sẻ.",
                color = Color(0xFFB8C7D1),
                fontSize = 15.sp,
                lineHeight = 20.sp
            )

            Text(
                text = "Tại sao tôi cần cung cấp ngày sinh của mình?",
                color = Color(0xFF4AA3FF),
                fontSize = 15.sp,
                style = TextStyle(textDecoration = TextDecoration.Underline),
                modifier = Modifier.padding(top = 6.dp)
            )

            if (!uiState.errorMessage.isNullOrBlank()) {
                AppNotice(
                    text = uiState.errorMessage,
                    type = NoticeType.ERROR,
                    onClose = { viewModel.clearError() },
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            Surface(
                color = card,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, outline),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showSheet = true }
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    val age = calcAge(displayDate, today)
                    Text(
                        text = "Ngày sinh ($age tuổi)",
                        color = Color(0xFF9FB2BD),
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = displayDate.format(formatter),
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // ===================== BottomSheet =====================
        if (showSheet) {
            // ✅ initDate chỉ set 1 lần khi mở sheet (tránh reset khi kéo)
            val initDate = remember(true) { selectedDate ?: today }
            var tempDate by remember(showSheet) { mutableStateOf(initDate) }

            fun LocalDate.coerceIn(min: LocalDate, max: LocalDate): LocalDate =
                when {
                    isBefore(min) -> min
                    isAfter(max) -> max
                    else -> this
                }

            ModalBottomSheet(
                onDismissRequest = { showSheet = false },
                containerColor = sheetBg,
                scrimColor = Color.Black.copy(alpha = 0.55f)
            ) {
                Text(
                    text = "Chọn ngày sinh",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                        .padding(horizontal = 16.dp)
                ) {
                    WheelDatePickerCompose(
                        date = initDate,                 // ✅ chỉ initial
                        onDateChange = { tempDate = it },// ✅ lưu temp
                        modifier = Modifier.fillMaxSize(),
                        minDate = minDate,
                        maxDate = today
                    )

                    Box(
                        Modifier
                            .align(Alignment.Center)
                            .fillMaxWidth()
                            .height(44.dp)
                            .background(centerBand.copy(alpha = 0.55f), RoundedCornerShape(12.dp))
                    )

                    HorizontalDivider(
                        color = Color.White.copy(alpha = 0.10f),
                        modifier = Modifier.align(Alignment.Center).offset(y = (-22).dp)
                    )
                    HorizontalDivider(
                        color = Color.White.copy(alpha = 0.10f),
                        modifier = Modifier.align(Alignment.Center).offset(y = (22).dp)
                    )
                }

                Button(
                    onClick = {
                        val picked = tempDate.coerceIn(minDate, today) // ✅ clamp tại đây
                        viewModel.setDateOfBirth(picked)
                        showSheet = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .height(58.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = blue),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text("Xong", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun WheelDatePickerCompose(
    date: LocalDate,
    onDateChange: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    minDate: LocalDate = LocalDate.of(1900, 1, 1),
    maxDate: LocalDate = LocalDate.now()
) {
    // ✅ KHÔNG remember(date) để tránh reset liên tục khi kéo
    var day by remember { mutableIntStateOf(date.dayOfMonth) }
    var month by remember { mutableIntStateOf(date.monthValue) }
    var year by remember { mutableIntStateOf(date.year) }

    // ✅ Chỉ reset khi initial date thay đổi (mở sheet lần khác)
    LaunchedEffect(date) {
        day = date.dayOfMonth
        month = date.monthValue
        year = date.year
    }

    fun daysInMonth(y: Int, m: Int): Int = LocalDate.of(y, m, 1).lengthOfMonth()

    fun normalize(y: Int, m: Int, d: Int): LocalDate {
        val safeDay = d.coerceIn(1, daysInMonth(y, m))
        return LocalDate.of(y, m, safeDay)
    }

    LaunchedEffect(day, month, year) {
        val normalized = normalize(year, month, day)
        if (normalized.dayOfMonth != day) day = normalized.dayOfMonth
        onDateChange(normalized)
    }

    val minYear = minDate.year
    val maxYear = maxDate.year

    val dayItems = remember(year, month) { (1..daysInMonth(year, month)).map { it.toString() } }
    val monthItems = remember { (1..12).map { "tháng $it" } }
    val yearItems = remember(minYear, maxYear) { (minYear..maxYear).map { it.toString() } }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        WheelColumnSimple(
            items = dayItems,
            selectedIndex = (day - 1).coerceIn(0, dayItems.lastIndex),
            modifier = Modifier.weight(1f).widthIn(min = 90.dp),
            onSelected = { idx -> day = idx + 1 }
        )
        WheelColumnSimple(
            items = monthItems,
            selectedIndex = month - 1,
            modifier = Modifier.weight(1f).widthIn(min = 130.dp),
            onSelected = { idx -> month = idx + 1 }
        )
        WheelColumnSimple(
            items = yearItems,
            selectedIndex = (year - minYear).coerceIn(0, yearItems.lastIndex),
            modifier = Modifier.weight(1f).widthIn(min = 100.dp),
            onSelected = { idx -> year = minYear + idx }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WheelColumnSimple(
    items: List<String>,
    selectedIndex: Int,
    modifier: Modifier,
    onSelected: (Int) -> Unit
) {
    val itemHeight = 44.dp
    val viewportHeight = 180.dp

    val listState = rememberLazyListState()
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val scope = rememberCoroutineScope()

    val itemHeightPx = with(LocalDensity.current) { itemHeight.toPx() }
    val safeSelected = selectedIndex.coerceIn(0, (items.size - 1).coerceAtLeast(0))

    fun currentTargetIndex(): Int {
        if (items.isEmpty()) return 0
        val idx = listState.firstVisibleItemIndex
        val offset = listState.firstVisibleItemScrollOffset
        val target = if (offset > itemHeightPx / 2f) idx + 1 else idx
        return target.coerceIn(0, items.size - 1)
    }

    // Khi items đổi (vd đổi tháng làm đổi list ngày) -> kéo về đúng selected
    LaunchedEffect(items.size) {
        if (items.isNotEmpty()) listState.scrollToItem(safeSelected)
    }

    // Khi selectedIndex đổi từ ngoài vào -> chỉ kéo nếu đang lệch thật
    LaunchedEffect(safeSelected) {
        if (items.isEmpty()) return@LaunchedEffect
        val targetNow = currentTargetIndex()
        if (!listState.isScrollInProgress && targetNow != safeSelected) {
            listState.scrollToItem(safeSelected)
        }
    }

    // Khi dừng cuộn -> snap đúng item ở giữa -> onSelected 1 lần
    LaunchedEffect(items.size) {
        snapshotFlow { listState.isScrollInProgress }
            .distinctUntilChanged()
            .filter { inProgress -> !inProgress }
            .map { currentTargetIndex() }
            .distinctUntilChanged()
            .collect { target ->
                scope.launch {
                    listState.animateScrollToItem(target)
                    onSelected(target)
                }
            }
    }

    Box(modifier = modifier.height(viewportHeight)) {
        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            contentPadding = PaddingValues(vertical = (viewportHeight - itemHeight) / 2),
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(items) { index, text ->
                val isSelected = index == safeSelected
                Text(
                    text = text,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight)
                        .wrapContentHeight(Alignment.CenterVertically),
                    textAlign = TextAlign.Center,
                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.28f),
                    fontSize = if (isSelected) 22.sp else 18.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}

private fun calcAge(birthDate: LocalDate, today: LocalDate = LocalDate.now()): Int {
    var age = today.year - birthDate.year
    if (today < birthDate.plusYears(age.toLong())) age--
    return age.coerceAtLeast(0)
}
