package com.mooket.app.ui.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mooket.app.ui.theme.*

/**
 * 编辑资料页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBackClick: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    val repository = remember { com.mooket.app.data.repository.ProfileRepository(com.mooket.app.data.api.RetrofitClient.apiService) }
    val viewModel: EditProfileViewModel = remember { EditProfileViewModel(repository) }
    val uiState by viewModel.uiState.collectAsState()

    // 行业身份选择弹窗
    var showIdentitySheet by remember { mutableStateOf(false) }

    // Loading overlay
    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Primary)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        EditProfileHeader(
            onBackClick = onBackClick,
            onSaveClick = { viewModel.saveProfile() }
        )

        // 基本信息 Section Title
        Text(
            text = "基本信息",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
        )

        // 头像 Section - 靠左
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(PrimaryLight)
                    .clickable { /* TODO: 选择图片 */ },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "头像",
                    tint = Primary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 基本信息 Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Surface),
            border = BorderStroke(1.dp, Border)
        ) {
            Column {
                // 昵称行
                NicknameRow(
                    nickname = uiState.nickname,
                    onNicknameChange = { viewModel.updateNickname(it) }
                )

                InfoDivider()

                // 姓名行（实名认证状态）
                RealNameRow(
                    realNameStatus = uiState.realNameStatus,
                    realName = uiState.realName,
                    onRealNameVerifyClick = { /* TODO: 跳转实名认证 */ }
                )

                InfoDivider()

                // 关联手机（只读）
                ReadOnlyInfoRow(
                    label = "关联手机",
                    value = uiState.phone?.let { "${it.substring(0, 3)}***${it.substring(it.length - 4)}" } ?: ""
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 标签 Section Title
        Text(
            text = "标签",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 行业身份 Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Surface),
            border = BorderStroke(1.dp, Border)
        ) {
            // 行业身份 - 点击弹出选择器
            IdentityTagRow(
                label = "行业身份",
                selectedTags = uiState.identityTags,
                onClick = { showIdentitySheet = true }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    // 保存成功
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            onSaveSuccess()
        }
    }

    // 行业身份选择 BottomSheet
    if (showIdentitySheet) {
        IdentitySelectionSheet(
            selectedTags = uiState.identityTags,
            availableTags = listOf("贸易商", "采购商", "供应商", "服务商", "其他"),
            onDismiss = { showIdentitySheet = false },
            onConfirm = { selected ->
                viewModel.updateIdentityTags(selected)
                showIdentitySheet = false
            }
        )
    }
}

/**
 * 行业身份选择 BottomSheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IdentitySelectionSheet(
    selectedTags: List<String>,
    availableTags: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (List<String>) -> Unit
) {
    var tempSelected by remember { mutableStateOf(selectedTags.toList()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Surface,
        windowInsets = WindowInsets(0)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "选择行业身份",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "关闭",
                    tint = TextHint,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onDismiss() }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 标签选项
            availableTags.forEach { tag ->
                val isSelected = tempSelected.contains(tag)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            tempSelected = if (isSelected) {
                                tempSelected - tag
                            } else {
                                tempSelected + tag
                            }
                        }
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = tag,
                        fontSize = 14.sp,
                        color = TextPrimary
                    )
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "已选",
                            tint = Primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                if (tag != availableTags.last()) {
                    Divider(color = Border, thickness = 0.5.dp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 确认按钮
            Button(
                onClick = { onConfirm(tempSelected) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "确认",
                    fontSize = 16.sp,
                    color = Surface
                )
            }
        }
    }
}

/**
 * 编辑资料页 Header
 */
@Composable
private fun EditProfileHeader(
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.ArrowBackIos,
            contentDescription = "返回",
            tint = TextPrimary,
            modifier = Modifier
                .size(24.dp)
                .clickable { onBackClick() }
        )
        Text(
            text = "编辑资料",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )
        Text(
            text = "保存",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Primary,
            modifier = Modifier.clickable { onSaveClick() }
        )
    }
}

/**
 * 昵称可编辑行
 */
@Composable
private fun NicknameRow(
    nickname: String,
    onNicknameChange: (String) -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var textValue by remember(nickname) { mutableStateOf(nickname) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isEditing = true }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "昵称",
            fontSize = 14.sp,
            color = Color(0xFF3C4947)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            if (isEditing) {
                BasicTextField(
                    value = textValue,
                    onValueChange = { newValue ->
                        textValue = newValue
                        onNicknameChange(newValue)
                    },
                    modifier = Modifier.widthIn(max = 200.dp),
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 14.sp,
                        color = TextPrimary,
                        textAlign = TextAlign.End
                    ),
                    decorationBox = { innerTextField ->
                        Box(contentAlignment = Alignment.CenterEnd) {
                            if (textValue.isEmpty()) {
                                Text(
                                    text = "请输入昵称",
                                    fontSize = 14.sp,
                                    color = TextHint
                                )
                            }
                            innerTextField()
                        }
                    },
                    singleLine = true,
                    cursorBrush = androidx.compose.ui.graphics.SolidColor(Primary)
                )
            } else {
                Text(
                    text = nickname.ifEmpty { "请输入昵称" },
                    fontSize = 14.sp,
                    color = if (nickname.isEmpty()) TextHint else TextPrimary
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = TextHint,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * 姓名行（实名认证状态）
 */
@Composable
private fun RealNameRow(
    realNameStatus: String?,
    realName: String?,
    onRealNameVerifyClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = realNameStatus != "approved") { onRealNameVerifyClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "姓名",
            fontSize = 14.sp,
            color = Color(0xFF3C4947)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            when (realNameStatus) {
                "approved" -> {
                    Text(
                        text = realName ?: "",
                        fontSize = 14.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFF2F8F7), RoundedCornerShape(2.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "已认证",
                            fontSize = 11.sp,
                            color = Primary
                        )
                    }
                }
                else -> {
                    Box(
                        modifier = Modifier
                            .background(PrimaryLight, RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "去实名认证",
                            fontSize = 12.sp,
                            color = Primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = TextHint,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * 只读信息行
 */
@Composable
private fun ReadOnlyInfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color(0xFF3C4947)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = TextPrimary
        )
    }
}

/**
 * 行业身份行
 */
@Composable
private fun IdentityTagRow(
    label: String,
    selectedTags: List<String>,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color(0xFF3C4947)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selectedTags.joinToString("、").ifEmpty { "请选择" },
                fontSize = 14.sp,
                color = if (selectedTags.isEmpty()) TextHint else TextPrimary
            )

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = TextHint,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * 分隔线
 */
@Composable
private fun InfoDivider() {
    Divider(
        color = Border,
        thickness = 0.5.dp,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}
