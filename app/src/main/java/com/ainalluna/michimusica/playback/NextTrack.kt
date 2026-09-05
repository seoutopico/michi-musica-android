package com.ainalluna.michimusica.playback

import androidx.media3.common.Player

/** Continue the current permutation; only wrap once its final item has been reached. */
internal fun nextTrackIndex(current: Int, next: Int, firstShuffled: Int, count: Int, shuffle: Boolean): Int? {
    if (count <= 1) return null
    if (next in 0 until count && next != current) return next
    return firstShuffled.takeIf { shuffle && it in 0 until count && it != current }
}

fun Player.canAdvanceTrack(): Boolean = nextTrackIndex(currentMediaItemIndex, nextMediaItemIndex,
    currentTimeline.getFirstWindowIndex(true), mediaItemCount, shuffleModeEnabled) != null

fun Player.advanceTrack() {
    val next = nextTrackIndex(currentMediaItemIndex, nextMediaItemIndex, currentTimeline.getFirstWindowIndex(true), mediaItemCount, shuffleModeEnabled)
    if (next != null) seekToDefaultPosition(next)
}
