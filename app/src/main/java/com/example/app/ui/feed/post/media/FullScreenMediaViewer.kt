package com.example.app.ui.feed.post.media

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.example.app.domain.model.Media
import com.example.app.ui.auth.components.BackIconButton
// Nhớ import VideoPlayerManager

@Composable
fun FullScreenMediaViewer(
    mediaList: List<Media>?,
    initialPage: Int = 0,
    onClose: () -> Unit
) {
    val items = mediaList ?: emptyList()
    if (items.isEmpty()) {
        onClose()
        return
    }

    val pagerState = rememberPagerState(
        initialPage = initialPage.coerceIn(0, items.lastIndex),
        pageCount = { items.size }
    )

    BackHandler {
        VideoPlayerManager.setFullScreenState(false)
        onClose()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            VerticalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                key = { items[it].url }
            ) { page ->
                val media = items[page]
                val isPageActive = (pagerState.currentPage == page)

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (media.type.equals("video", ignoreCase = true)) {
                        val savedPosition = remember(media.url) {
                            VideoPlayerManager.getSavedPosition(media.url)
                        }

                        FullScreenVideoPlayer(
                            videoUrl = media.url,
                            startPosition = savedPosition,
                            shouldPlay = isPageActive,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        AsyncImage(
                            model = media.url,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }

            BackIconButton(
                onClick = {
                    VideoPlayerManager.setFullScreenState(false)
                    onClose()
                }
            )
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
fun FullScreenVideoPlayer(
    videoUrl: String,
    startPosition: Long,
    shouldPlay: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var exoPlayer by remember { mutableStateOf<ExoPlayer?>(null) }

    // 1. Thêm State quản lý âm lượng (Mặc định là false - có tiếng)
    var isMuted by remember { mutableStateOf(false) }

    DisposableEffect(videoUrl) {
        val player = ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(videoUrl.toUri())
            setMediaItem(mediaItem)
            repeatMode = Player.REPEAT_MODE_ONE
            if (startPosition > 0) {
                seekTo(startPosition)
            }
            prepare()
        }
        exoPlayer = player

        onDispose {
            VideoPlayerManager.savePosition(videoUrl, player.currentPosition)
            player.release()
            exoPlayer = null
        }
    }

    LaunchedEffect(shouldPlay, exoPlayer) {
        if (shouldPlay) {
            exoPlayer?.play()
        } else {
            exoPlayer?.pause()
        }
    }

    // 2. Logic cập nhật âm lượng khi biến isMuted thay đổi
    LaunchedEffect(isMuted, exoPlayer) {
        exoPlayer?.volume = if (isMuted) 0f else 1f
    }

    Box(modifier = modifier) {
        if (exoPlayer != null) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        // Lưu ý: Nếu dùng controller mặc định, nút volume có thể bị che.
                        // Bạn có thể set false nếu muốn giao diện giống TikTok/Reels.
                        useController = true
                        setShutterBackgroundColor(android.graphics.Color.TRANSPARENT)
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                },
                modifier = Modifier.fillMaxSize().background(Color.Black)
            )

            // 3. Nút Bật/Tắt Âm lượng
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd) // Căn góc dưới phải
                    // Thêm padding bottom nhiều một chút để tránh thanh seekbar của Controller
                    .padding(bottom = 60.dp, end = 16.dp)
                    .size(40.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    .clip(CircleShape) // Cắt bo tròn để ripple effect đẹp
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null // Tắt hiệu ứng ripple nếu muốn, hoặc để default
                    ) {
                        isMuted = !isMuted
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.Default.VolumeUp,
                    contentDescription = "Volume Toggle",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}