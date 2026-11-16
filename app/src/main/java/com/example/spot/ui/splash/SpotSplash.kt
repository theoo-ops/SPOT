package com.example.spot.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.example.spot.R
import com.example.spot.ui.theme.Burgundy
import com.example.spot.ui.theme.Inter
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * In-app splash. Plays once, then calls [onFinished] as soon as the animation is done
 * AND [dataReady] is true. No artificial hold.
 *
 * @param animate false when the system's animations are off: everything starts in its final state.
 */
@Composable
fun SpotSplash(
    animate: Boolean,
    dataReady: Boolean,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val p = remember { SplashProgress(start = if (animate) 0f else 1f) }
    val ready by rememberUpdatedState(dataReady)
    val finish by rememberUpdatedState(onFinished)

    LaunchedEffect(Unit) {
        if (animate) {
            withFrameNanos { }  // start the clock on the first drawn frame, not at composition
            p.play()
        }
        snapshotFlow { ready }.first { it }
        finish()
    }

    val description = stringResource(R.string.splash_content_description)
    val m = SplashMotion
    val l = SplashLayout

    BoxWithConstraints(
        modifier
            .fillMaxSize()
            .background(Burgundy)
            .clearAndSetSemantics { contentDescription = description }
            // Swallow touches so nothing reaches Home underneath while the splash is up.
            .pointerInput(Unit) {
                awaitPointerEventScope { while (true) awaitPointerEvent().changes.forEach { it.consume() } }
            },
    ) {
        val unit = minOf(maxWidth, maxHeight * l.WideScreenRatio)
        val taglineSize = with(LocalDensity.current) { maxOf(unit * l.TaglineSize, l.TaglineMinDp.dp).toSp() }

        // Waves: blush behind, pink in front (the pink export is shorter, so behind it would be hidden);
        // each slides up by its own height. Both are exported at the same width, so the pink keeps
        // its height ratio to the blush.
        val blushPainter = painterResource(R.drawable.ic_wave_blush)
        val pinkPainter = painterResource(R.drawable.ic_wave_pink)
        val blushHeight = maxHeight * l.WaveHeight
        val pinkHeight = blushHeight * (pinkPainter.intrinsicSize.height / blushPainter.intrinsicSize.height)
        WaveLayer(blushPainter, p.waveBlush, Modifier.align(Alignment.BottomCenter).height(blushHeight))
        WaveLayer(pinkPainter, p.wavePink, Modifier.align(Alignment.BottomCenter).height(pinkHeight))

        // Doodles: faint, one after another.
        l.Doodles.forEachIndexed { i, d ->
            val a = p.doodles[i]
            Image(
                painter = painterResource(d.res),
                contentScale = ContentScale.FillWidth, // scale up to the given width, not the exported size
                contentDescription = null,
                modifier = Modifier
                    .centerAt(d.x, d.y)
                    .width(unit * d.width)
                    .graphicsLayer {
                        alpha = a.value * l.DoodleAlpha
                        rotationZ = d.rotation
                    },
            )
        }

        // Sparkles: twinkle once.
        l.Sparkles.forEachIndexed { i, s ->
            val a = p.sparkles[i]
            Image(
                painter = painterResource(R.drawable.ic_sparkle),
                contentScale = ContentScale.FillWidth, // scale up to the given width, not the exported size
                contentDescription = null,
                modifier = Modifier
                    .centerAt(s.x, s.y)
                    .width(unit * s.width)
                    .graphicsLayer {
                        val t = a.value
                        alpha = sparkleAlpha(t)
                        val sc = sparkleScale(t)
                        scaleX = sc
                        scaleY = sc
                    },
            )
        }

        // Logo lockup: pin, wordmark, tagline as one stacked group, centred in the free area
        // between the status bar and the wave, so the pieces never collide on short screens.
        Box(
            Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(bottom = blushHeight),
        ) {
            Column(
                modifier = Modifier.centerAt(0.5f, l.LockupY),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(Modifier.width(unit * l.PinWidth)) {
                    Image(
                        painter = painterResource(R.drawable.ic_spot_flicks),
                        contentScale = ContentScale.FillWidth, // scale up to the given width, not the exported size
                        contentDescription = null,
                        modifier = Modifier
                            .width(unit * l.PinWidth * l.FlicksWidthRatio)
                            .align(Alignment.TopCenter)
                            .graphicsLayer {
                                val v = p.flicks.value
                                alpha = v
                                val sc = lerp(m.FlickStartScale, 1f, v)
                                scaleX = sc
                                scaleY = sc
                                translationY = size.height * l.FlicksOffsetYRatio
                                transformOrigin = TransformOrigin(0.5f, 1f)
                            },
                    )
                    Box(
                        Modifier.graphicsLayer {
                            val v = p.pinDrop.value  // spring: briefly > 1 = overshoot
                            alpha = p.pinAlpha.value
                            val sc = lerp(m.PinStartScale, 1f, v)
                            scaleX = sc
                            scaleY = sc
                            translationY = (1f - v) * -m.PinDropDp.dp.toPx()
                            transformOrigin = TransformOrigin(0.5f, l.PinTipY)
                        },
                    ) {
                        Image(painterResource(R.drawable.ic_spot_pin), null, Modifier.fillMaxWidth(), contentScale = ContentScale.FillWidth)
                        Image(
                            painter = painterResource(R.drawable.ic_spot_utensils),
                            contentScale = ContentScale.FillWidth, // scale up to the given width, not the exported size
                            contentDescription = null,
                            modifier = Modifier
                                .width(unit * l.PinWidth * l.UtensilsWidthRatio)
                                .align(Alignment.TopCenter)
                                .graphicsLayer {
                                    alpha = p.utensils.value
                                    translationY = size.height * l.UtensilsOffsetYRatio
                                },
                        )
                    }
                }

                // Wordmark, then tagline: rise and fade in.
                Spacer(Modifier.height(unit * l.PinToWordmarkGap))
                Image(
                    painter = painterResource(R.drawable.ic_spot_wordmark),
                    contentScale = ContentScale.FillWidth, // scale up to the given width, not the exported size
                    contentDescription = null,
                    modifier = Modifier
                        .width(unit * l.WordmarkWidth)
                        .riseIn(p.wordmark),
                )
                Spacer(Modifier.height(unit * l.WordmarkToTaglineGap))
                Text(
                    text = stringResource(R.string.splash_tagline),
                    modifier = Modifier
                        .widthIn(max = unit * l.TaglineMaxWidth)
                        .riseIn(p.tagline),
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    style = TextStyle(
                        fontFamily = Inter,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Normal,
                        fontSize = taglineSize,
                        lineHeight = taglineSize * 1.3f,
                    ),
                )
            }
        }
    }
}

@Composable
private fun WaveLayer(painter: Painter, progress: Animatable<Float, *>, modifier: Modifier) {
    Image(
        painter = painter,
        contentDescription = null,
        contentScale = ContentScale.FillBounds,
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { translationY = (1f - progress.value) * size.height },
    )
}

/** Holds every animated value; each runs 0 → 1. */
private class SplashProgress(start: Float) {
    val waveBlush = Animatable(start)
    val wavePink = Animatable(start)
    val pinAlpha = Animatable(start)
    val pinDrop = Animatable(start)
    val utensils = Animatable(start)
    val flicks = Animatable(start)
    val wordmark = Animatable(start)
    val tagline = Animatable(start)
    val doodles = List(SplashLayout.Doodles.size) { Animatable(start) }
    val sparkles = List(SplashLayout.Sparkles.size) { Animatable(start) }

    suspend fun play(): Unit = coroutineScope {
        val m = SplashMotion
        fun t(durationMs: Int, delayMs: Int, easing: Easing = m.EaseOut) = tween<Float>(
            durationMillis = (durationMs * m.SlowMotion).roundToInt(),
            delayMillis = (delayMs * m.SlowMotion).roundToInt(),
            easing = easing,
        )
        launch { waveBlush.animateTo(1f, t(m.WaveMs, m.WaveBlushDelayMs)) }
        launch { wavePink.animateTo(1f, t(m.WaveMs, m.WaveBlushDelayMs + m.WavePinkLagMs)) }
        launch { pinAlpha.animateTo(1f, t(m.PinFadeMs, 0)) }
        launch { pinDrop.animateTo(1f, m.PinSpring) }
        launch { utensils.animateTo(1f, t(m.UtensilsMs, m.UtensilsDelayMs)) }
        launch { flicks.animateTo(1f, t(m.FlicksMs, m.FlicksDelayMs)) }
        launch { wordmark.animateTo(1f, t(m.WordmarkMs, m.WordmarkDelayMs)) }
        launch { tagline.animateTo(1f, t(m.TaglineMs, m.WordmarkDelayMs + m.TaglineLagMs)) }
        doodles.forEachIndexed { i, a ->
            launch { a.animateTo(1f, t(m.DoodleMs, m.DoodlesDelayMs + i * m.DoodleStaggerMs)) }
        }
        sparkles.forEachIndexed { i, a ->
            launch { a.animateTo(1f, t(m.SparkleMs, m.SparklesDelayMs + i * m.SparkleStaggerMs, LinearEasing)) }
        }
    }
}

/** Rise [SplashMotion.TextRiseDp] and fade in, driven in the graphics layer (no relayout). */
private fun Modifier.riseIn(progress: Animatable<Float, *>) = graphicsLayer {
    val v = progress.value
    alpha = v
    translationY = (1f - v) * SplashMotion.TextRiseDp.dp.toPx()
}

/** Places the element so its centre sits at (x, y) as fractions of the parent's size. */
private fun Modifier.centerAt(x: Float, y: Float) = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints.copy(minWidth = 0, minHeight = 0))
    val w = constraints.maxWidth
    val h = constraints.maxHeight
    layout(w, h) {
        placeable.place(
            (w * x - placeable.width / 2f).roundToInt(),
            (h * y - placeable.height / 2f).roundToInt(),
        )
    }
}

private fun sparkleAlpha(t: Float): Float {
    val peak = SplashMotion.SparklePeakAt
    val l = SplashLayout
    return if (t < peak) {
        l.SparklePeakAlpha * SplashMotion.EaseOut.transform(t / peak)
    } else {
        lerp(l.SparklePeakAlpha, l.SparkleRestAlpha, SplashMotion.EaseInOut.transform((t - peak) / (1f - peak)))
    }
}

private fun sparkleScale(t: Float): Float {
    val m = SplashMotion
    val peak = m.SparklePeakAt
    return if (t < peak) {
        lerp(m.SparkleStartScale, m.SparklePeakScale, m.EaseOut.transform(t / peak))
    } else {
        lerp(m.SparklePeakScale, 1f, m.EaseInOut.transform((t - peak) / (1f - peak)))
    }
}

@Preview(name = "Final state", widthDp = 360, heightDp = 760)
@Preview(name = "Short phone", widthDp = 360, heightDp = 640)
@Preview(name = "Tablet", widthDp = 800, heightDp = 1280)
@Composable
private fun SpotSplashPreview() {
    SpotSplash(animate = false, dataReady = false, onFinished = {})
}
