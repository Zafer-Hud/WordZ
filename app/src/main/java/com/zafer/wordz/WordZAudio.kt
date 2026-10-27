package com.zafer.wordz

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import java.io.File
import java.io.FileOutputStream
import kotlin.math.PI
import kotlin.math.sin

class WordZAudio(private val context: Context) {
    enum class Fx { SELECT, CORRECT, BONUS, WRONG, COMPLETE, REWARD, LEVEL_UP }

    private val soundPool = SoundPool.Builder()
        .setMaxStreams(6)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val ids = mutableMapOf<Fx, Int>()
    private var effectsEnabled = true
    private var musicEnabled = false
    private var musicPlayer: MediaPlayer? = null

    init {
        ids[Fx.SELECT] = load("wordz_select.wav", listOf(660.0), listOf(0.06), 0.20)
        ids[Fx.CORRECT] = load("wordz_correct.wav", listOf(660.0, 880.0), listOf(0.08, 0.12), 0.30)
        ids[Fx.BONUS] = load("wordz_bonus.wav", listOf(784.0, 988.0, 1175.0), listOf(0.07, 0.07, 0.12), 0.30)
        ids[Fx.WRONG] = load("wordz_wrong.wav", listOf(220.0, 180.0), listOf(0.10, 0.12), 0.22)
        ids[Fx.COMPLETE] = load("wordz_complete.wav", listOf(523.0, 659.0, 784.0, 1047.0), listOf(0.09, 0.09, 0.09, 0.18), 0.32)
        ids[Fx.REWARD] = load("wordz_reward.wav", listOf(880.0, 1047.0, 1319.0), listOf(0.08, 0.08, 0.16), 0.28)
        ids[Fx.LEVEL_UP] = load("wordz_level_up.wav", listOf(523.0, 659.0, 784.0, 988.0, 1319.0), listOf(0.07, 0.07, 0.07, 0.09, 0.20), 0.33)
    }

    fun setEffectsEnabled(enabled: Boolean) { effectsEnabled = enabled }

    fun setMusicEnabled(enabled: Boolean) {
        musicEnabled = enabled
        if (enabled) startMusic() else stopMusic()
    }

    fun play(fx: Fx, volume: Float = 1f) {
        if (!effectsEnabled) return
        ids[fx]?.let { soundPool.play(it, volume, volume, 1, 0, 1f) }
    }

    fun release() {
        stopMusic()
        soundPool.release()
    }

    private fun load(name: String, freqs: List<Double>, durations: List<Double>, volume: Double): Int {
        val file = File(context.cacheDir, name)
        if (!file.exists()) writeWav(file, freqs, durations, volume)
        return soundPool.load(file.absolutePath, 1)
    }

    private fun startMusic() {
        if (!musicEnabled || musicPlayer?.isPlaying == true) return
        val file = File(context.cacheDir, "wordz_ambient.wav")
        if (!file.exists()) writeAmbient(file)
        musicPlayer = MediaPlayer().apply {
            setDataSource(file.absolutePath)
            isLooping = true
            setVolume(0.16f, 0.16f)
            prepare()
            start()
        }
    }

    private fun stopMusic() {
        musicPlayer?.runCatching { stop() }
        musicPlayer?.release()
        musicPlayer = null
    }

    private fun writeWav(file: File, freqs: List<Double>, durations: List<Double>, volume: Double) {
        val sampleRate = 22050
        val pcm = ArrayList<Short>()
        freqs.zip(durations).forEach { (freq, duration) ->
            val count = (sampleRate * duration).toInt().coerceAtLeast(1)
            repeat(count) { index ->
                val t = index.toDouble() / sampleRate
                val attack = (index / (sampleRate * 0.01)).coerceIn(0.0, 1.0)
                val decay = 1.0 - index.toDouble() / count
                val sample = sin(2.0 * PI * freq * t) * volume * attack * decay
                pcm += (sample.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
            }
        }
        writePcmWav(file, pcm.toShortArray(), sampleRate)
    }

    private fun writeAmbient(file: File) {
        val sampleRate = 22050
        val seconds = 8
        val notes = doubleArrayOf(261.63, 329.63, 392.00, 493.88)
        val pcm = ShortArray(sampleRate * seconds)
        pcm.indices.forEach { index ->
            val t = index.toDouble() / sampleRate
            val freq = notes[((t / 2.0).toInt()) % notes.size]
            val phase = t % 2.0
            val edge = minOf(1.0, phase / 0.30, (2.0 - phase) / 0.30).coerceAtLeast(0.0)
            val sample = (0.10 * sin(2.0 * PI * freq * t) + 0.035 * sin(2.0 * PI * (freq / 2.0) * t)) * edge
            pcm[index] = (sample.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
        }
        writePcmWav(file, pcm, sampleRate)
    }

    private fun writePcmWav(file: File, pcm: ShortArray, sampleRate: Int) {
        FileOutputStream(file).use { out ->
            val dataSize = pcm.size * 2
            out.write("RIFF".toByteArray())
            writeLeInt(out, 36 + dataSize)
            out.write("WAVEfmt ".toByteArray())
            writeLeInt(out, 16)
            writeLeShort(out, 1)
            writeLeShort(out, 1)
            writeLeInt(out, sampleRate)
            writeLeInt(out, sampleRate * 2)
            writeLeShort(out, 2)
            writeLeShort(out, 16)
            out.write("data".toByteArray())
            writeLeInt(out, dataSize)
            pcm.forEach { writeLeShort(out, it.toInt()) }
        }
    }

    private fun writeLeInt(out: FileOutputStream, value: Int) {
        out.write(value and 0xff)
        out.write(value shr 8 and 0xff)
        out.write(value shr 16 and 0xff)
        out.write(value shr 24 and 0xff)
    }

    private fun writeLeShort(out: FileOutputStream, value: Int) {
        out.write(value and 0xff)
        out.write(value shr 8 and 0xff)
    }
}
