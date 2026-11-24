package com.aura.aura_ui.voice

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.core.content.ContextCompat
import com.aura.aura_ui.data.preferences.ThakurSecurityStore
import com.k2fsa.sherpa.onnx.FeatureConfig
import com.k2fsa.sherpa.onnx.KeywordSpotter
import com.k2fsa.sherpa.onnx.KeywordSpotterConfig
import com.k2fsa.sherpa.onnx.OnlineModelConfig
import com.k2fsa.sherpa.onnx.OnlineStream
import com.k2fsa.sherpa.onnx.OnlineTransducerModelConfig
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Free, fully-offline wake-word detection powered by sherpa-onnx keyword spotting
 * (Apache-2.0).
 *
 * Reads the active wake phrase from [ThakurSecurityStore.getWakeWord] on startup and
 * whenever reloaded from Settings, deterministically tokenizing custom phrases against
 * `assets/kws/tokens.txt` so edits apply immediately without app reinstall.
 */
class SherpaKwsWakeWordDetector(
    private val context: Context,
    /** Per-keyword boosting score; higher = easier to trigger. */
    private val keywordsScore: Float = 1.5f,
    /** Trigger threshold 0..1; lower = easier trigger. 0.20 balances recall vs false-accepts. */
    private val keywordsThreshold: Float = 0.20f,
) : WakeWordDetector {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var listeningJob: Job? = null
    private var spotter: KeywordSpotter? = null

    private val _isListening = MutableStateFlow(false)
    override val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val configuredWakeWord: String = ThakurSecurityStore.getWakeWord(context)
    override val availableKeywords: List<String> = listOf(configuredWakeWord)

    private var onWakeWordCallback: ((String) -> Unit)? = null
    private var initError: Exception? = null

    init {
        initializeSpotter()
    }

    private fun initializeSpotter() {
        try {
            val phrase = configuredWakeWord
            val isDefaultPhrase = phrase.equals(ThakurSecurityStore.DEFAULT_WAKE_WORD, ignoreCase = true)
            val vocab = loadVocab()

            if (!isDefaultPhrase) {
                val customTokensLine = tokenizePhraseForKws(phrase, vocab)
                if (customTokensLine != null) {
                    val kwsDir = ensureKwsFilesOnDisk()
                    val customKeywordsFile = File(kwsDir, "custom_keywords.txt")
                    customKeywordsFile.writeText("$customTokensLine\n")
                    validateKeywordLines(customKeywordsFile.readLines(), vocab, "custom_keywords.txt")

                    val modelConfig = OnlineModelConfig(
                        transducer = OnlineTransducerModelConfig(
                            encoder = File(kwsDir, "encoder.int8.onnx").absolutePath,
                            decoder = File(kwsDir, "decoder.int8.onnx").absolutePath,
                            joiner = File(kwsDir, "joiner.int8.onnx").absolutePath,
                        ),
                        tokens = File(kwsDir, "tokens.txt").absolutePath,
                        numThreads = 1,
                        provider = "cpu",
                        modelType = "zipformer2",
                    )
                    val config = KeywordSpotterConfig(
                        featConfig = FeatureConfig(sampleRate = SAMPLE_RATE, featureDim = 80),
                        modelConfig = modelConfig,
                        keywordsFile = customKeywordsFile.absolutePath,
                        keywordsScore = keywordsScore,
                        keywordsThreshold = keywordsThreshold,
                    )
                    spotter = KeywordSpotter(assetManager = null, config = config)
                    initError = null
                    Log.i(TAG, "✅ sherpa-onnx initialized with custom wake phrase \"$phrase\" ($customTokensLine)")
                    return
                }
            }

            Log.i(TAG, "🔧 Initializing sherpa-onnx keyword spotter (assets/$ASSET_DIR, phrase=\"$phrase\")…")
            val modelConfig = OnlineModelConfig(
                transducer = OnlineTransducerModelConfig(
                    encoder = "$ASSET_DIR/encoder.int8.onnx",
                    decoder = "$ASSET_DIR/decoder.int8.onnx",
                    joiner = "$ASSET_DIR/joiner.int8.onnx",
                ),
                tokens = "$ASSET_DIR/tokens.txt",
                numThreads = 1,
                provider = "cpu",
                modelType = "zipformer2",
            )
            val config = KeywordSpotterConfig(
                featConfig = FeatureConfig(sampleRate = SAMPLE_RATE, featureDim = 80),
                modelConfig = modelConfig,
                keywordsFile = "$ASSET_DIR/keywords.txt",
                keywordsScore = keywordsScore,
                keywordsThreshold = keywordsThreshold,
            )
            validateKeywordAssets(vocab)

            spotter = KeywordSpotter(assetManager = context.assets, config = config)
            initError = null
            Log.i(TAG, "✅ sherpa-onnx keyword spotter initialized (listening for \"$phrase\")")
        } catch (e: Exception) {
            initError = e
            Log.e(TAG, "❌ Failed to initialize sherpa-onnx keyword spotter: ${e.message}", e)
            throw e
        }
    }

    private fun loadVocab(): HashSet<String> =
        context.assets.open("$ASSET_DIR/tokens.txt")
            .bufferedReader().useLines { lines ->
                lines.mapNotNull { it.trim().substringBefore(' ').takeIf(String::isNotEmpty) }
                    .toHashSet()
            }

    private fun ensureKwsFilesOnDisk(): File {
        val dir = File(context.filesDir, ASSET_DIR).apply { mkdirs() }
        val assetFiles = listOf("encoder.int8.onnx", "decoder.int8.onnx", "joiner.int8.onnx", "tokens.txt")
        for (name in assetFiles) {
            val target = File(dir, name)
            if (!target.exists() || target.length() == 0L) {
                context.assets.open("$ASSET_DIR/$name").use { input ->
                    target.outputStream().use { output -> input.copyTo(output) }
                }
            }
        }
        return dir
    }

    /**
     * Deterministically encode any spoken phrase into valid `tokens.txt` BPE tokens via
     * greedy longest-match, preventing native `EncodeBase` SIGABRT on unknown tokens.
     */
    private fun tokenizePhraseForKws(phrase: String, vocab: Set<String>): String? {
        val digitWords = mapOf(
            '0' to " ZERO ", '1' to " ONE ", '2' to " TWO ", '3' to " THREE ", '4' to " FOUR ",
            '5' to " FIVE ", '6' to " SIX ", '7' to " SEVEN ", '8' to " EIGHT ", '9' to " NINE ",
        )
        val expanded = buildString {
            for (ch in phrase.uppercase()) {
                append(digitWords[ch] ?: ch)
            }
        }
        val words = expanded
            .replace(Regex("[^A-Z'\\s]"), " ")
            .trim()
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }

        if (words.isEmpty()) return null

        val outTokens = mutableListOf<String>()
        for (word in words) {
            var remaining = "\u2581$word"
            while (remaining.isNotEmpty()) {
                var matched: String? = null
                for (len in remaining.length downTo 1) {
                    val candidate = remaining.substring(0, len)
                    if (candidate in vocab && candidate != "<blk>" && candidate != "<unk>" && candidate != "<sos/eos>") {
                        matched = candidate
                        break
                    }
                }
                if (matched == null) return null
                outTokens.add(matched)
                remaining = remaining.substring(matched.length)
            }
        }
        return outTokens.joinToString(" ").takeIf { it.isNotBlank() }
    }

    private fun validateKeywordAssets(vocab: Set<String>) {
        val lines = context.assets.open("$ASSET_DIR/keywords.txt").bufferedReader().use { it.readLines() }
        validateKeywordLines(lines, vocab, "keywords.txt")
    }

    private fun validateKeywordLines(lines: List<String>, vocab: Set<String>, label: String) {
        lines.filter { it.isNotBlank() }.forEach { line ->
            val tokens = line.substringBefore(" :").substringBefore(" #").substringBefore(" @")
                .trim().split(Regex("\\s+"))
            val missing = tokens.filter { it.isNotEmpty() && it !in vocab }
            check(missing.isEmpty()) {
                "$label token(s) $missing not in tokens.txt (line: \"$line\"). " +
                    "sherpa would SIGABRT on this — refusing to initialize."
            }
        }
    }

    override fun start() {
        // Reconcile a stale "listening" flag whose job already died.
        if (_isListening.value) {
            if (listeningJob?.isActive == true) {
                Log.d(TAG, "Already listening (job active)")
                return
            }
            Log.w(TAG, "⚠️ isListening=true but job not active — resetting state")
            _isListening.value = false
        }

        val activeSpotter = spotter
        if (activeSpotter == null) {
            Log.e(TAG, "❌ Cannot start — spotter not initialized (initError: ${initError?.message})")
            return
        }

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(TAG, "❌ Cannot start — RECORD_AUDIO permission not granted")
            return
        }

        Log.i(TAG, "🎤 Starting wake word detection…")
        _isListening.value = true

        // A stopped loop can still be inside read()/decode() for ~100 ms; wait it out so two
        // loops never share the mic.
        val previous = listeningJob
        listeningJob = scope.launch {
            previous?.join()
            try {
                runDetectionLoop(activeSpotter)
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error in wake word detection loop: ${e.message}", e)
            } finally {
                withContext(Dispatchers.Main) {
                    _isListening.value = false
                    Log.d(TAG, "Listening job completed, isListening=false")
                }
            }
        }
    }

    private suspend fun runDetectionLoop(spotter: KeywordSpotter) {
        val minBuffer = AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
        )
        if (minBuffer == AudioRecord.ERROR || minBuffer == AudioRecord.ERROR_BAD_VALUE) {
            Log.e(TAG, "❌ Invalid AudioRecord buffer size: $minBuffer")
            withContext(Dispatchers.Main) { _isListening.value = false }
            return
        }

        val audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            maxOf(minBuffer, CHUNK_SAMPLES * 2) * 2,
        )
        if (audioRecord.state != AudioRecord.STATE_INITIALIZED) {
            Log.e(TAG, "❌ AudioRecord failed to initialize (state=${audioRecord.state})")
            audioRecord.release()
            withContext(Dispatchers.Main) { _isListening.value = false }
            return
        }

        val stream: OnlineStream = spotter.createStream()
        val shortBuf = ShortArray(CHUNK_SAMPLES)
        val floatBuf = FloatArray(CHUNK_SAMPLES)

        try {
            audioRecord.startRecording()
            Log.i(TAG, "✅ Wake word detection active — listening for \"$configuredWakeWord\"…")

            while (coroutineContext.isActive && _isListening.value) {
                val n = audioRecord.read(shortBuf, 0, CHUNK_SAMPLES)
                if (n <= 0) {
                    if (n < 0) {
                        Log.e(TAG, "AudioRecord read error: $n")
                        break
                    }
                    continue
                }

                // PCM16 → normalized float [-1, 1], which sherpa expects.
                for (i in 0 until n) floatBuf[i] = shortBuf[i] / 32768.0f
                if (com.aura.aura_ui.BuildConfig.DEBUG) logLevel(shortBuf, n)
                stream.acceptWaveform(if (n == CHUNK_SAMPLES) floatBuf else floatBuf.copyOf(n), SAMPLE_RATE)

                while (spotter.isReady(stream)) {
                    spotter.decode(stream)
                    val keyword = spotter.getResult(stream).keyword
                    if (keyword.isNotBlank()) {
                        Log.i(TAG, "🔊 Wake word detected: \"$keyword\"")
                        spotter.reset(stream)
                        // Release the mic BEFORE handing off to STT (single audio consumer).
                        withContext(Dispatchers.Main) {
                            stopInternal()
                            onWakeWordCallback?.invoke(keyword)
                        }
                        return
                    }
                }
            }
        } finally {
            stream.release()
            audioRecord.stop()
            audioRecord.release()
            Log.d(TAG, "AudioRecord + stream released")
        }
    }

    private var levelChunks = 0
    private var levelPeak = 0
    private var levelSumSq = 0.0
    private var levelSamples = 0

    /** Debug builds only: mic peak and RMS every ~2 s, to tell a silenced mic from a model miss. */
    private fun logLevel(buf: ShortArray, n: Int) {
        for (i in 0 until n) {
            val v = buf[i].toInt()
            levelPeak = maxOf(levelPeak, kotlin.math.abs(v))
            levelSumSq += v.toDouble() * v
        }
        levelSamples += n
        if (++levelChunks < 20) return
        Log.d(TAG, "mic level: peak=$levelPeak rms=${kotlin.math.sqrt(levelSumSq / levelSamples).toInt()}")
        levelChunks = 0; levelPeak = 0; levelSumSq = 0.0; levelSamples = 0
    }

    override fun stop() {
        if (!_isListening.value) return
        Log.i(TAG, "⏹️ Stopping wake word detection…")
        stopInternal()
    }

    private fun stopInternal() {
        _isListening.value = false
        // Cancel only: release() and the next start() must still be able to join this job.
        listeningJob?.cancel()
        // AudioRecord + stream cleanup happens in the loop's finally block.
    }

    override fun setOnWakeWordDetected(callback: (String) -> Unit) {
        onWakeWordCallback = callback
    }

    override fun release() {
        Log.i(TAG, "Releasing SherpaKwsWakeWordDetector")
        stop()
        onWakeWordCallback = null
        val loop = listeningJob
        val native = spotter ?: return
        spotter = null
        // Cancellation is cooperative: the loop may still be inside decode(). Freeing the spotter
        // under it was a null-deref SIGSEGV that killed the whole app (2026-09-14). Free it only
        // once the loop, and its finally that releases the stream, has finished.
        scope.launch {
            val finished = loop == null || withTimeoutOrNull(RELEASE_WAIT_MS) { loop.join() } != null
            if (finished) {
                runCatching { native.release() }.onFailure { Log.e(TAG, "Error releasing spotter", it) }
            } else {
                Log.w(TAG, "Detection loop still running after ${RELEASE_WAIT_MS}ms, leaking the spotter instead of crashing")
            }
        }
    }

    companion object {
        private const val TAG = "SherpaKwsWakeWord"
        private const val ASSET_DIR = "kws"
        private const val SAMPLE_RATE = 16000

        // ~100 ms of audio per read — low latency without over-polling the mic.
        private const val CHUNK_SAMPLES = 1600
        private const val RELEASE_WAIT_MS = 2_000L
    }
}
