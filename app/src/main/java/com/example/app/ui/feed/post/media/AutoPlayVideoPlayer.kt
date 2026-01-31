package com.example.app.ui.feed.post.media

import android.graphics.Outline
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

// --- 1. SINGLETON QUẢN LÝ VIDEO ---
object VideoPlayerManager {
    // URL của video đang được phép phát
    private val _activeVideoUrl = MutableStateFlow<String?>(null)
    val activeVideoUrl = _activeVideoUrl.asStateFlow()

    // Map lưu trữ: URL -> Vị trí Y trên màn hình
    private val visibleVideos = mutableMapOf<String, Float>()

    // [MỚI] Map lưu trữ vị trí phát: URL -> mili giây
    private val playbackPositions = mutableMapOf<String, Long>()

    // [MỚI] Trạng thái FullScreen
    private var isFullScreenOpen = false

    fun updateVideoVisibility(url: String, isVisible: Boolean, yPos: Float) {
        if (isVisible) {
            visibleVideos[url] = yPos
        } else {
            visibleVideos.remove(url)
        }
        recalculateActiveVideo()
    }

    // [MỚI] Hàm lưu vị trí phát
    fun savePosition(url: String, position: Long) {
        playbackPositions[url] = position
    }

    // [MỚI] Hàm lấy vị trí phát (dùng cho FullScreen và Resume)
    fun getSavedPosition(url: String): Long {
        return playbackPositions[url] ?: 0L
    }

    // [MỚI] Hàm báo hiệu mở/đóng FullScreen (Gọi từ MediaGrid)
    fun setFullScreenState(isOpen: Boolean) {
        isFullScreenOpen = isOpen
        recalculateActiveVideo()
    }

    private fun recalculateActiveVideo() {
        // [QUAN TRỌNG] Nếu đang mở FullScreen thì dừng hết tất cả video nhỏ
        if (isFullScreenOpen) {
            _activeVideoUrl.value = null
            return
        }

        if (visibleVideos.isEmpty()) {
            _activeVideoUrl.value = null
            return
        }
        // Tìm video có Y nhỏ nhất
        val winner = visibleVideos.entries.minByOrNull { it.value }?.key

        if (_activeVideoUrl.value != winner) {
            _activeVideoUrl.value = winner
        }
    }
}

// --- 2. COMPOSABLE PLAYER ---
@OptIn(UnstableApi::class)
@Composable
fun AutoPlayVideoPlayer(
    videoUrl: String,
    thumbnailUrl: String?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val activeUrl by VideoPlayerManager.activeVideoUrl.collectAsState()
    val shouldPlay = (activeUrl == videoUrl)

    var isMuted by remember { mutableStateOf(false) } // Mặc định nên Mute
    var exoPlayer by remember { mutableStateOf<ExoPlayer?>(null) }

    // Quản lý Lifecycle & Lưu vị trí khi Dispose
    DisposableEffect(lifecycleOwner, videoUrl) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                // [MỚI] Lưu vị trí khi App ẩn xuống
                exoPlayer?.let { VideoPlayerManager.savePosition(videoUrl, it.currentPosition) }
                exoPlayer?.pause()
            } else if (event == Lifecycle.Event.ON_RESUME && shouldPlay) {
                exoPlayer?.play()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            // [MỚI] Lưu vị trí trước khi hủy player
            exoPlayer?.let { VideoPlayerManager.savePosition(videoUrl, it.currentPosition) }
            exoPlayer?.release()
            exoPlayer = null
            VideoPlayerManager.updateVideoVisibility(videoUrl, false, 0f)
        }
    }

    // Logic Player
    LaunchedEffect(shouldPlay) {
        if (shouldPlay) {
            if (exoPlayer == null) {
                exoPlayer = ExoPlayer.Builder(context).build().apply {
                    val mediaItem = MediaItem.fromUri(videoUrl.toUri())
                    setMediaItem(mediaItem)
                    repeatMode = Player.REPEAT_MODE_ONE
                    volume = if (isMuted) 0f else 1f

                    // [MỚI] Seek tới vị trí đã lưu trước đó (nếu có)
                    val savedPos = VideoPlayerManager.getSavedPosition(videoUrl)
                    if (savedPos > 0) {
                        seekTo(savedPos)
                    }

                    prepare()
                    playWhenReady = true
                }
            } else {
                exoPlayer?.play()
            }
        } else {
            // [MỚI] Khi bị dừng (do scroll hoặc do mở FullScreen), lưu vị trí lại
            exoPlayer?.let { VideoPlayerManager.savePosition(videoUrl, it.currentPosition) }
            exoPlayer?.pause()
        }
    }

    LaunchedEffect(isMuted) {
        exoPlayer?.volume = if (isMuted) 0f else 1f
    }

    Box(
        modifier = modifier
            .background(Color.Black)
            .graphicsLayer {
                clip = true
                shape = RectangleShape
            }
            .onGloballyPositioned { coordinates ->
                val windowBounds = coordinates.positionInWindow()
                val itemTop = windowBounds.y
                val itemHeight = coordinates.size.height.toFloat()
                val itemBottom = itemTop + itemHeight
                val screenHeight = context.resources.displayMetrics.heightPixels.toFloat()

                val visibleTop = maxOf(0f, itemTop)
                val visibleBottom = minOf(screenHeight, itemBottom)
                val visibleHeight = maxOf(0f, visibleBottom - visibleTop)
                val ratio = if (itemHeight > 0) visibleHeight / itemHeight else 0f
                val isVisible = ratio >= 0.6f

                VideoPlayerManager.updateVideoVisibility(
                    url = videoUrl,
                    isVisible = isVisible,
                    yPos = itemTop
                )
            },
        contentAlignment = Alignment.Center
    ) {
        if (shouldPlay && exoPlayer != null) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = false
                        clipToOutline = true
                        outlineProvider = object : ViewOutlineProvider() {
                            override fun getOutline(view: View, outline: Outline) {
                                outline.setRect(0, 0, view.width, view.height)
                            }
                        }
                        setShutterBackgroundColor(android.graphics.Color.TRANSPARENT)
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        clip = true
                        shape = RectangleShape
                    }
            )

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .size(28.dp)
                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        isMuted = !isMuted
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = "Mute",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }

        } else {
            Image(
                painter = rememberAsyncImagePainter(thumbnailUrl ?: videoUrl),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}