package com.ainalluna.michimusica.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

enum class MichiIconType {
    MUSIC, SEARCH, PLAYLIST, LYRICS, PLAY, PAUSE, MORE, BACK, SHUFFLE,
    PREVIOUS, NEXT, REPEAT, STOP, LOCK, FOLDER, REFRESH, ADD, CHECK,
}

@Composable
fun MichiIcon(
    icon: MichiIconType,
    description: String,
    modifier: Modifier = Modifier.size(24.dp),
    tint: Color = MaterialTheme.colorScheme.onSurface,
) {
    Canvas(modifier.semantics { contentDescription = description }) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = size.minDimension * .082f, cap = StrokeCap.Round)
        when (icon) {
            MichiIconType.SEARCH -> {
                drawCircle(tint, w * .27f, Offset(w * .42f, h * .4f), style = stroke)
                drawLine(tint, Offset(w * .62f, h * .61f), Offset(w * .87f, h * .86f), stroke.width, StrokeCap.Round)
            }
            MichiIconType.MUSIC -> {
                drawLine(tint, Offset(w * .62f, h * .18f), Offset(w * .62f, h * .68f), stroke.width, StrokeCap.Round)
                drawLine(tint, Offset(w * .62f, h * .2f), Offset(w * .85f, h * .29f), stroke.width, StrokeCap.Round)
                drawCircle(tint, w * .15f, Offset(w * .44f, h * .72f))
            }
            MichiIconType.PLAYLIST -> repeat(3) { index ->
                val y = h * (.25f + index * .25f)
                drawCircle(tint, w * .04f, Offset(w * .17f, y))
                drawLine(tint, Offset(w * .34f, y), Offset(w * .84f, y), stroke.width, StrokeCap.Round)
            }
            MichiIconType.LYRICS -> repeat(3) { index ->
                val y = h * (.27f + index * .23f)
                drawLine(tint, Offset(w * .2f, y), Offset(w * if (index == 1) .7f else .82f, y), stroke.width, StrokeCap.Round)
            }
            MichiIconType.PLAY -> {
                val path = Path().apply { moveTo(w * .34f, h * .2f); lineTo(w * .8f, h * .5f); lineTo(w * .34f, h * .8f); close() }
                drawPath(path, tint)
            }
            MichiIconType.PAUSE -> {
                drawRoundRect(tint, Offset(w * .27f, h * .2f), Size(w * .14f, h * .6f), CornerRadius(3f))
                drawRoundRect(tint, Offset(w * .59f, h * .2f), Size(w * .14f, h * .6f), CornerRadius(3f))
            }
            MichiIconType.MORE -> repeat(3) { drawCircle(tint, w * .06f, Offset(w * .5f, h * (.25f + it * .25f))) }
            MichiIconType.BACK -> {
                drawLine(tint, Offset(w * .8f, h * .5f), Offset(w * .22f, h * .5f), stroke.width, StrokeCap.Round)
                drawLine(tint, Offset(w * .22f, h * .5f), Offset(w * .48f, h * .24f), stroke.width, StrokeCap.Round)
                drawLine(tint, Offset(w * .22f, h * .5f), Offset(w * .48f, h * .76f), stroke.width, StrokeCap.Round)
            }
            MichiIconType.PREVIOUS, MichiIconType.NEXT -> {
                val reverse = icon == MichiIconType.PREVIOUS
                val a = if (reverse) .7f else .3f
                val b = if (reverse) .3f else .7f
                drawLine(tint, Offset(w * a, h * .22f), Offset(w * b, h * .5f), stroke.width, StrokeCap.Round)
                drawLine(tint, Offset(w * b, h * .5f), Offset(w * a, h * .78f), stroke.width, StrokeCap.Round)
                drawLine(tint, Offset(w * if (reverse) .25f else .75f, h * .22f), Offset(w * if (reverse) .25f else .75f, h * .78f), stroke.width, StrokeCap.Round)
            }
            MichiIconType.SHUFFLE -> {
                drawLine(tint, Offset(w * .16f, h * .3f), Offset(w * .38f, h * .3f), stroke.width, StrokeCap.Round)
                drawLine(tint, Offset(w * .38f, h * .3f), Offset(w * .7f, h * .7f), stroke.width, StrokeCap.Round)
                drawLine(tint, Offset(w * .7f, h * .7f), Offset(w * .86f, h * .7f), stroke.width, StrokeCap.Round)
                drawLine(tint, Offset(w * .16f, h * .7f), Offset(w * .36f, h * .7f), stroke.width, StrokeCap.Round)
                drawLine(tint, Offset(w * .36f, h * .7f), Offset(w * .68f, h * .3f), stroke.width, StrokeCap.Round)
                drawLine(tint, Offset(w * .68f, h * .3f), Offset(w * .86f, h * .3f), stroke.width, StrokeCap.Round)
            }
            MichiIconType.REPEAT, MichiIconType.REFRESH -> {
                drawArc(tint, -40f, 230f, false, Offset(w * .18f, h * .18f), Size(w * .64f, h * .64f), style = stroke)
                val path = Path().apply { moveTo(w * .73f, h * .13f); lineTo(w * .88f, h * .3f); lineTo(w * .65f, h * .31f); close() }
                drawPath(path, tint)
            }
            MichiIconType.STOP -> drawRoundRect(tint, Offset(w * .28f, h * .28f), Size(w * .44f, h * .44f), CornerRadius(3f))
            MichiIconType.LOCK -> {
                drawRoundRect(tint, Offset(w * .2f, h * .42f), Size(w * .6f, h * .45f), CornerRadius(w * .08f), style = stroke)
                drawArc(tint, 180f, 180f, false, Offset(w * .32f, h * .13f), Size(w * .36f, h * .48f), style = stroke)
            }
            MichiIconType.FOLDER -> {
                val path = Path().apply {
                    moveTo(w * .1f, h * .3f); lineTo(w * .4f, h * .3f); lineTo(w * .5f, h * .42f)
                    lineTo(w * .9f, h * .42f); lineTo(w * .9f, h * .82f); lineTo(w * .1f, h * .82f); close()
                }
                drawPath(path, tint, style = stroke)
            }
            MichiIconType.ADD -> {
                drawLine(tint, Offset(w * .22f, h * .5f), Offset(w * .78f, h * .5f), stroke.width, StrokeCap.Round)
                drawLine(tint, Offset(w * .5f, h * .22f), Offset(w * .5f, h * .78f), stroke.width, StrokeCap.Round)
            }
            MichiIconType.CHECK -> {
                drawLine(tint, Offset(w * .18f, h * .52f), Offset(w * .4f, h * .74f), stroke.width, StrokeCap.Round)
                drawLine(tint, Offset(w * .4f, h * .74f), Offset(w * .84f, h * .26f), stroke.width, StrokeCap.Round)
            }
        }
    }
}

@Composable
fun MichiFace(modifier: Modifier = Modifier) {
    val fill = MaterialTheme.colorScheme.primaryContainer
    val ink = MaterialTheme.colorScheme.onPrimaryContainer
    Canvas(modifier.semantics { contentDescription = "Michi" }) {
        val earStroke = Stroke(width = size.minDimension * .06f, cap = StrokeCap.Round)
        val ears = Path().apply {
            moveTo(size.width * .12f, size.height * .42f)
            lineTo(size.width * .24f, size.height * .08f)
            lineTo(size.width * .39f, size.height * .35f)
            moveTo(size.width * .61f, size.height * .35f)
            lineTo(size.width * .76f, size.height * .08f)
            lineTo(size.width * .88f, size.height * .42f)
        }
        drawPath(ears, fill, style = earStroke)
        drawCircle(ink, size.minDimension * .055f, Offset(size.width * .36f, size.height * .48f))
        drawCircle(ink, size.minDimension * .055f, Offset(size.width * .64f, size.height * .48f))
        val nose = Path().apply {
            moveTo(size.width * .45f, size.height * .61f)
            lineTo(size.width * .55f, size.height * .61f)
            lineTo(size.width * .5f, size.height * .7f)
            close()
        }
        drawPath(nose, ink)
    }
}

@Composable
fun MichiArtwork(modifier: Modifier = Modifier) {
    Surface(modifier, shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.primaryContainer) {
        Box(contentAlignment = Alignment.Center) { MichiFace(Modifier.size(92.dp)) }
    }
}
