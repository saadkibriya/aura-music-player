/*
 * MIT License
 * Copyright (c) 2025 Md Golam Kibriya
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND.
 */

package com.kibriya.aura.audio

import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.AudioProcessor.AudioFormat
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.*

/**
 * AuraAudioProcessor — 10-band biquad EQ + ReplayGain volume scaling.
 * Plugged into Media3's audio pipeline via AudioSink.
 */
class AuraAudioProcessor : AudioProcessor {

    // 10 EQ band centre frequencies in Hz
    private val bandFrequencies = floatArrayOf(
        31f, 62f, 125f, 250f, 500f, 1000f, 2000f, 4000f, 8000f, 16000f
    )

    private val bandGains = FloatArray(10) { 0f }   // dB, -12 to +12
    private var replayGain = 0f                      // linear scale, applied as multiplier
    private var sampleRate = 44100
    private var channelCount = 2

    private var inputFormat = AudioFormat.NOT_SET
    private var outputFormat = AudioFormat.NOT_SET
    private var isActive = false

    // Biquad coefficients [band][coeff]: b0, b1, b2, a1, a2
    private val coeffs = Array(10) { DoubleArray(5) }
    // Filter states per band per channel: [band][channel][z1, z2]
    private var states = Array(10) { Array(2) { DoubleArray(2) } }

    private var outputBuffer: ByteBuffer = ByteBuffer.allocate(0)
    private var inputEnded = false

    // ── Public API ────────────────────────────────────────────────────────────

    /** Set gain for a single EQ band. gain must be in [-12, 12] dB. */
    fun setBandGain(band: Int, gainDb: Float) {
        require(band in 0..9)
        bandGains[band] = gainDb.coerceIn(-12f, 12f)
        computeCoefficients()
    }

    /** Set all 10 band gains at once (List size must be 10). */
    fun setAllBandGains(gains: List<Float>) {
        require(gains.size == 10)
        gains.forEachIndexed { i, g -> bandGains[i] = g.coerceIn(-12f, 12f) }
        computeCoefficients()
    }

    /**
     * Set ReplayGain as a linear multiplier.
     * Caller should convert dB -> linear before passing:
     *   linear = 10f.pow(dB / 20f)
     */
    fun setReplayGain(gain: Float) {
        replayGain = gain.coerceIn(0f, 4f)
    }

    // ── AudioProcessor impl ───────────────────────────────────────────────────

    override fun configure(inputAudioFormat: AudioFormat): AudioFormat {
        // Only handle PCM_16BIT (signed 16-bit little-endian)
        if (inputAudioFormat.encoding != android.media.AudioFormat.ENCODING_PCM_16BIT) {
            return AudioFormat.NOT_SET
        }
        inputFormat = inputAudioFormat
        outputFormat = inputAudioFormat
        sampleRate = inputAudioFormat.sampleRate
        channelCount = inputAudioFormat.channelCount
        resetStates()
        computeCoefficients()
        isActive = true
        return outputFormat
    }

    override fun isActive(): Boolean = isActive

    override fun queueInput(inputBuffer: ByteBuffer) {
        val remaining = inputBuffer.remaining()
        if (remaining == 0) return

        if (outputBuffer.capacity() < remaining) {
            outputBuffer = ByteBuffer.allocate(remaining).order(ByteOrder.nativeOrder())
        } else {
            outputBuffer.clear()
        }

        // Process each 16-bit sample
        while (inputBuffer.remaining() >= 2) {
            val channelIdx = ((inputBuffer.position() / 2) % channelCount)
            val raw = inputBuffer.short.toInt()
            var sample = raw / 32768.0

            // Apply biquad EQ filters in series
            for (band in 0..9) {
                sample = applyBiquad(band, channelIdx, sample)
            }

            // Apply ReplayGain (0f means no change since default = 0, treat 0 as bypass)
            if (replayGain != 0f) {
                sample *= replayGain.toDouble()
            }

            // Clip and write back
            val out = (sample * 32768.0).toInt().coerceIn(-32768, 32767).toShort()
            outputBuffer.putShort(out)
        }

        outputBuffer.flip()
    }

    override fun queueEndOfStream() {
        inputEnded = true
    }

    override fun getOutput(): ByteBuffer {
        val out = outputBuffer
        outputBuffer = ByteBuffer.allocate(0)
        return out
    }

    override fun isEnded(): Boolean = inputEnded && !outputBuffer.hasRemaining()

    override fun flush() {
        resetStates()
        inputEnded = false
        outputBuffer = ByteBuffer.allocate(0)
    }

    override fun reset() {
        flush()
        isActive = false
        inputFormat = AudioFormat.NOT_SET
        outputFormat = AudioFormat.NOT_SET
    }

    // ── Biquad DSP ────────────────────────────────────────────────────────────

    /**
     * Compute peaking EQ biquad coefficients for all 10 bands.
     * Uses Audio EQ Cookbook (Robert Bristow-Johnson) peaking filter formula.
     */
    private fun computeCoefficients() {
        for (i in 0..9) {
            val f0 = bandFrequencies[i].toDouble()
            val gainDb = bandGains[i].toDouble()
            val A = 10.0.pow(gainDb / 40.0)          // sqrt of linear gain
            val w0 = 2.0 * PI * f0 / sampleRate
            val cosW = cos(w0)
            val sinW = sin(w0)
            // Q fixed at ~1.41 (bandwidth ~1 octave per band)
            val Q = 1.41
            val alpha = sinW / (2.0 * Q)

            val b0 = 1.0 + alpha * A
            val b1 = -2.0 * cosW
            val b2 = 1.0 - alpha * A
            val a0 = 1.0 + alpha / A
            val a1 = -2.0 * cosW
            val a2 = 1.0 - alpha / A

            // Normalise by a0
            coeffs[i][0] = b0 / a0
            coeffs[i][1] = b1 / a0
            coeffs[i][2] = b2 / a0
            coeffs[i][3] = a1 / a0
            coeffs[i][4] = a2 / a0
        }
    }

    /** Apply biquad filter for one band/channel using transposed Direct Form II. */
    private fun applyBiquad(band: Int, ch: Int, x: Double): Double {
        val c = coeffs[band]
        val s = states[band][ch]
        val y = c[0] * x + s[0]
        s[0] = c[1] * x - c[3] * y + s[1]
        s[1] = c[2] * x - c[4] * y
        return y
    }

    private fun resetStates() {
        states = Array(10) { Array(channelCount.coerceAtLeast(2)) { DoubleArray(2) } }
    }
}