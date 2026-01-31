package com.example.app.ui.feed.post.media

import android.R.attr.spacing
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.VideoFrameDecoder
import com.example.app.domain.model.Media


    @Composable
    fun MediaGrid(
        mediaList: List<Media>?,
    ) {
        val items = mediaList ?: emptyList()
        if (items.isEmpty()) return

        var isFullScreenVisible by remember { mutableStateOf(false) }
        var initialPage by remember { mutableIntStateOf(0) }   // ← thêm state này

        val onMediaClick = { index: Int ->
            VideoPlayerManager.setFullScreenState(true)
            initialPage = index
            isFullScreenVisible = true
        }

        val spacing = 4.dp
        val modifier = Modifier.fillMaxWidth()


        when (items.size) {
            1 -> {
                MediaImage(
                    media = items[0],
                    modifier = modifier
                        .heightIn(min = 220.dp, max = 600.dp)
                        .clickable { onMediaClick(0) }   // ← truyền index
                )
            }

            2 -> {
                Row(modifier = modifier.aspectRatio(2f)) {
                    MediaImage(
                        media = items[0],
                        modifier = Modifier.weight(1f).fillMaxHeight()
                            .clickable { onMediaClick(0) })
                    Spacer(modifier = Modifier.width(spacing))
                    MediaImage(
                        media = items[1],
                        modifier = Modifier.weight(1f).fillMaxHeight()
                            .clickable { onMediaClick(1) })
                }
            }

            3 -> {
                Column(modifier = modifier.aspectRatio(1f)) {
                    MediaImage(
                        media = items[0],
                        modifier = Modifier.weight(1f).fillMaxWidth()
                            .clickable { onMediaClick(0) })
                    Spacer(modifier = Modifier.height(spacing))
                    Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        MediaImage(
                            media = items[1],
                            modifier = Modifier.weight(1f).fillMaxHeight()
                                .clickable { onMediaClick(1) })
                        Spacer(modifier = Modifier.width(spacing))
                        MediaImage(
                            media = items[2],
                            modifier = Modifier.weight(1f).fillMaxHeight()
                                .clickable { onMediaClick(2) })
                    }
                }
            }


            4 -> {
                Column(modifier = modifier.aspectRatio(1f)) {
                    // Dòng 1
                    Row(modifier = Modifier.weight(1f)) {
                        MediaImage(
                            media = items[0],
                            modifier = Modifier.weight(1f).fillMaxHeight()
                                .clickable { onMediaClick(0) })
                        Spacer(modifier = Modifier.width(spacing))
                        MediaImage(
                            media = items[1],
                            modifier = Modifier.weight(1f).fillMaxHeight()
                                .clickable { onMediaClick(1) })
                    }
                    Spacer(modifier = Modifier.height(spacing))
                    // Dòng 2
                    Row(modifier = Modifier.weight(1f)) {
                        MediaImage(
                            media = items[2],
                            modifier = Modifier.weight(1f).fillMaxHeight()
                                .clickable { onMediaClick(2) })
                        Spacer(modifier = Modifier.width(spacing))
                        MediaImage(
                            media = items[3],
                            modifier = Modifier.weight(1f).fillMaxHeight()
                                .clickable { onMediaClick(3) })
                    }
                }
            }

            else -> {
                val remainingCount = items.size - 4
                Column(modifier = modifier.aspectRatio(1f)) {
                    Row(modifier = Modifier.weight(1f)) {
                        MediaImage(
                            media = items[0],
                            modifier = Modifier.weight(1f).fillMaxHeight()
                                .clickable { onMediaClick(0) })
                        Spacer(modifier = Modifier.width(spacing))
                        MediaImage(
                            media = items[1],
                            modifier = Modifier.weight(1f).fillMaxHeight()
                                .clickable { onMediaClick(1) })
                    }
                    Spacer(modifier = Modifier.height(spacing))
                    Row(modifier = Modifier.weight(1f)) {
                        MediaImage(
                            media = items[2],
                            modifier = Modifier.weight(1f).fillMaxHeight()
                                .clickable { onMediaClick(2) })
                        Spacer(modifier = Modifier.width(spacing))

                        // Ô cuối cùng chứa số đếm
                        Box(
                            modifier = Modifier.weight(1f).fillMaxHeight()
                                .clickable { onMediaClick(3) }) {
                            MediaImage(media = items[3], modifier = Modifier.fillMaxSize())
                            Surface(
                                modifier = Modifier.fillMaxSize(),
                                color = Color.Black.copy(alpha = 0.3f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "+$remainingCount",
                                        color = Color.White,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (isFullScreenVisible) {
            Dialog(
                onDismissRequest = {
                    isFullScreenVisible = false
                    VideoPlayerManager.setFullScreenState(false) },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                FullScreenMediaViewer(
                    mediaList = items,
                    initialPage = initialPage,
                    onClose = { isFullScreenVisible = false
                        VideoPlayerManager.setFullScreenState(false)}
                )
            }
        }
    }
@Composable
fun MediaImage(
    media: Media,
    modifier: Modifier
) {
    val context = LocalContext.current

    // Nếu là Video -> Dùng AutoPlayVideoPlayer
    if (media.type == "video") {
        AutoPlayVideoPlayer(
            videoUrl = media.url,
            thumbnailUrl = media.url,
            modifier = modifier
        )
    } else {
        // Nếu là Ảnh -> Giữ nguyên logic cũ
        val imageLoader = remember {
            ImageLoader.Builder(context)
                .components {
                    // VideoFrameDecoder chỉ dùng làm thumbnail backup
                    add(VideoFrameDecoder.Factory())
                }
                .build()
        }

        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = media.url,
                imageLoader = imageLoader,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alignment = Alignment.Center
            )
        }
    }
}