package com.aura.aura_ui.data.preferences

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private val Context.thakurSecurityDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "thakur_security_prefs",
)

enum class UnlockMethod(val key: String, val label: String) {
    FINGERPRINT("method_fingerprint", "Fingerprint"),
    FACE("method_face", "Face Unlock"),
    PATTERN("method_pattern", "Pattern"),
    PIN("method_pin", "PIN"),
    PASSWORD("method_password", "Password"),
}

/**
 * Persists custom Wake Word (DataStore + sync cache), Secret Code lock, and full
 * THAKUR OS Secured Access preferences + encrypted PIN / Password / Pattern secrets.
 *
 * Contract:
 * - Default wake word is `"Wake up THAKUR OS"`.
 * - PIN, Password, and Pattern secrets live in AES-256 GCM EncryptedSharedPreferences.
 * - `isWorkTheme` synchronizes the global Relax (Purple) vs Work (Orange) glow across
 *   both the main shell and the Secured Access lock screen.
 */
object ThakurSecurityStore {

    const val DEFAULT_WAKE_WORD = "Wake up THAKUR OS"

    private const val TAG = "ThakurSecurityStore"
    private const val SYNC_PREFS = "thakur_security_sync"
    private const val ENCRYPTED_PREFS = "thakur_secret_encrypted"

    private val KEY_WAKE_WORD = stringPreferencesKey("wake_word_phrase")
    private val KEY_SECRET_ENABLED = booleanPreferencesKey("secret_code_enabled")
    private val KEY_SECURED_ACCESS_ENABLED = booleanPreferencesKey("secured_access_enabled")
    private val KEY_METHOD_FINGERPRINT = booleanPreferencesKey("method_fingerprint")
    private val KEY_METHOD_FACE = booleanPreferencesKey("method_face")
    private val KEY_METHOD_PATTERN = booleanPreferencesKey("method_pattern")
    private val KEY_METHOD_PIN = booleanPreferencesKey("method_pin")
    private val KEY_METHOD_PASSWORD = booleanPreferencesKey("method_password")
    private val KEY_IS_WORK_THEME = booleanPreferencesKey("is_work_theme")

    private const val KEY_SECRET_VALUE = "secret_code_value"
    private const val KEY_PIN_VALUE = "secret_pin_value"
    private const val KEY_PATTERN_VALUE = "secret_pattern_value"

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _wakeWord = MutableStateFlow(DEFAULT_WAKE_WORD)
    val wakeWord: StateFlow<String> = _wakeWord.asStateFlow()

    private val _secretEnabled = MutableStateFlow(false)
    val secretEnabled: StateFlow<Boolean> = _secretEnabled.asStateFlow()

    private val _securedAccessEnabled = MutableStateFlow(false)
    val securedAccessEnabled: StateFlow<Boolean> = _securedAccessEnabled.asStateFlow()

    private val _hasSecretCode = MutableStateFlow(false)
    val hasSecretCode: StateFlow<Boolean> = _hasSecretCode.asStateFlow()

    private val _hasPinCode = MutableStateFlow(false)
    val hasPinCode: StateFlow<Boolean> = _hasPinCode.asStateFlow()

    private val _hasPatternCode = MutableStateFlow(false)
    val hasPatternCode: StateFlow<Boolean> = _hasPatternCode.asStateFlow()

    private val _methodFingerprint = MutableStateFlow(true)
    val methodFingerprint: StateFlow<Boolean> = _methodFingerprint.asStateFlow()

    private val _methodFace = MutableStateFlow(true)
    val methodFace: StateFlow<Boolean> = _methodFace.asStateFlow()

    private val _methodPattern = MutableStateFlow(true)
    val methodPattern: StateFlow<Boolean> = _methodPattern.asStateFlow()

    private val _methodPin = MutableStateFlow(true)
    val methodPin: StateFlow<Boolean> = _methodPin.asStateFlow()

    private val _methodPassword = MutableStateFlow(true)
    val methodPassword: StateFlow<Boolean> = _methodPassword.asStateFlow()

    private val _isWorkTheme = MutableStateFlow(false)
    val isWorkTheme: StateFlow<Boolean> = _isWorkTheme.asStateFlow()

    @Volatile
    private var initialized = false

    fun hydrate(context: Context) {
        val app = context.applicationContext
        val sync = app.getSharedPreferences(SYNC_PREFS, Context.MODE_PRIVATE)
        val cachedWake = sync.getString("wake_word", null)?.takeIf { it.isNotBlank() } ?: DEFAULT_WAKE_WORD
        val cachedSecretOn = sync.getBoolean("secret_enabled", false)
        val cachedSecuredOn = sync.getBoolean("secured_access_enabled", false)
        val fpOn = sync.getBoolean(UnlockMethod.FINGERPRINT.key, true)
        val faceOn = sync.getBoolean(UnlockMethod.FACE.key, true)
        val patOn = sync.getBoolean(UnlockMethod.PATTERN.key, true)
        val pinOn = sync.getBoolean(UnlockMethod.PIN.key, true)
        val pwdOn = sync.getBoolean(UnlockMethod.PASSWORD.key, true)
        val workTheme = sync.getBoolean("is_work_theme", false)

        val enc = encryptedPrefs(app)
        val secretSet = !enc.getString(KEY_SECRET_VALUE, null).isNullOrBlank()
        val pinSet = !enc.getString(KEY_PIN_VALUE, null).isNullOrBlank()
        val patternSet = !enc.getString(KEY_PATTERN_VALUE, null).isNullOrBlank()

        _wakeWord.value = cachedWake
        _secretEnabled.value = cachedSecretOn
        _securedAccessEnabled.value = cachedSecuredOn
        _methodFingerprint.value = fpOn
        _methodFace.value = faceOn
        _methodPattern.value = patOn
        _methodPin.value = pinOn
        _methodPassword.value = pwdOn
        _isWorkTheme.value = workTheme
        _hasSecretCode.value = secretSet
        _hasPinCode.value = pinSet
        _hasPatternCode.value = patternSet

        if (!initialized) {
            initialized = true
            scope.launch {
                runCatching {
                    val prefs = app.thakurSecurityDataStore.data.first()
                    prefs[KEY_WAKE_WORD]?.takeIf { it.isNotBlank() }?.let {
                        _wakeWord.value = it
                        sync.edit().putString("wake_word", it).apply()
                    }
                    prefs[KEY_SECRET_ENABLED]?.let {
                        _secretEnabled.value = it
                        sync.edit().putBoolean("secret_enabled", it).apply()
                    }
                    prefs[KEY_SECURED_ACCESS_ENABLED]?.let {
                        _securedAccessEnabled.value = it
                        sync.edit().putBoolean("secured_access_enabled", it).apply()
                    }
                    prefs[KEY_METHOD_FINGERPRINT]?.let {
                        _methodFingerprint.value = it
                        sync.edit().putBoolean(UnlockMethod.FINGERPRINT.key, it).apply()
                    }
                    prefs[KEY_METHOD_FACE]?.let {
                        _methodFace.value = it
                        sync.edit().putBoolean(UnlockMethod.FACE.key, it).apply()
                    }
                    prefs[KEY_METHOD_PATTERN]?.let {
                        _methodPattern.value = it
                        sync.edit().putBoolean(UnlockMethod.PATTERN.key, it).apply()
                    }
                    prefs[KEY_METHOD_PIN]?.let {
                        _methodPin.value = it
                        sync.edit().putBoolean(UnlockMethod.PIN.key, it).apply()
                    }
                    prefs[KEY_METHOD_PASSWORD]?.let {
                        _methodPassword.value = it
                        sync.edit().putBoolean(UnlockMethod.PASSWORD.key, it).apply()
                    }
                    prefs[KEY_IS_WORK_THEME]?.let {
                        _isWorkTheme.value = it
                        sync.edit().putBoolean("is_work_theme", it).apply()
                    }
                }.onFailure { Log.w(TAG, "DataStore hydrate failed: ${it.message}") }
            }
        }
    }

    fun getWakeWord(context: Context): String {
        hydrate(context)
        return _wakeWord.value
    }

    fun isSecretEnabled(context: Context): Boolean {
        hydrate(context)
        return _secretEnabled.value
    }

    fun isSecuredAccessEnabled(context: Context): Boolean {
        hydrate(context)
        return _securedAccessEnabled.value
    }

    /** True only when the user enabled Secret Code AND actually saved a non-empty secret. */
    fun isSecretLockActive(context: Context): Boolean {
        hydrate(context)
        return _secretEnabled.value && !readEncrypted(context.applicationContext, KEY_SECRET_VALUE).isNullOrBlank()
    }

    fun getSecretCode(context: Context): String? =
        readEncrypted(context.applicationContext, KEY_SECRET_VALUE)

    fun getPinCode(context: Context): String? =
        readEncrypted(context.applicationContext, KEY_PIN_VALUE)

    fun getPatternCode(context: Context): String? =
        readEncrypted(context.applicationContext, KEY_PATTERN_VALUE)

    fun verifySecret(context: Context, attempt: String): Boolean {
        val cleanAttempt = attempt.trim()
        val expectedSecret = getSecretCode(context)?.trim().orEmpty()
        val expectedPin = getPinCode(context)?.trim().orEmpty()
        if (expectedSecret.isEmpty() && expectedPin.isEmpty()) return false
        return (expectedSecret.isNotEmpty() && cleanAttempt.equals(expectedSecret, ignoreCase = true)) ||
            (expectedPin.isNotEmpty() && cleanAttempt == expectedPin)
    }

    fun verifyPin(context: Context, attempt: String): Boolean {
        val clean = attempt.trim()
        val pin = getPinCode(context)?.trim().orEmpty()
        val pwd = getSecretCode(context)?.trim().orEmpty()
        if (pin.isNotEmpty()) return clean == pin
        if (pwd.isNotEmpty()) return clean == pwd
        return false
    }

    fun verifyPassword(context: Context, attempt: String): Boolean {
        val clean = attempt.trim()
        val pwd = getSecretCode(context)?.trim().orEmpty()
        if (pwd.isEmpty()) return false
        return clean == pwd
    }

    fun verifyPattern(context: Context, sequence: String): Boolean {
        val saved = getPatternCode(context)?.trim().orEmpty()
        if (saved.isEmpty()) return false
        return sequence.trim() == saved
    }

    fun setWakeWord(context: Context, phrase: String) {
        val app = context.applicationContext
        val clean = phrase.trim().ifBlank { DEFAULT_WAKE_WORD }
        _wakeWord.value = clean
        app.getSharedPreferences(SYNC_PREFS, Context.MODE_PRIVATE)
            .edit().putString("wake_word", clean).apply()
        scope.launch {
            runCatching { app.thakurSecurityDataStore.edit { it[KEY_WAKE_WORD] = clean } }
                .onFailure { Log.w(TAG, "Failed saving wake word: ${it.message}") }
        }
    }

    fun setSecretEnabled(context: Context, enabled: Boolean) {
        val app = context.applicationContext
        _secretEnabled.value = enabled
        app.getSharedPreferences(SYNC_PREFS, Context.MODE_PRIVATE)
            .edit().putBoolean("secret_enabled", enabled).apply()
        scope.launch {
            runCatching { app.thakurSecurityDataStore.edit { it[KEY_SECRET_ENABLED] = enabled } }
                .onFailure { Log.w(TAG, "Failed saving secretEnabled: ${it.message}") }
        }
    }

    fun setSecuredAccessEnabled(context: Context, enabled: Boolean) {
        val app = context.applicationContext
        _securedAccessEnabled.value = enabled
        app.getSharedPreferences(SYNC_PREFS, Context.MODE_PRIVATE)
            .edit().putBoolean("secured_access_enabled", enabled).apply()
        scope.launch {
            runCatching { app.thakurSecurityDataStore.edit { it[KEY_SECURED_ACCESS_ENABLED] = enabled } }
                .onFailure { Log.w(TAG, "Failed saving securedAccessEnabled: ${it.message}") }
        }
    }

    fun setUnlockMethodEnabled(context: Context, method: UnlockMethod, enabled: Boolean) {
        val app = context.applicationContext
        when (method) {
            UnlockMethod.FINGERPRINT -> _methodFingerprint.value = enabled
            UnlockMethod.FACE -> _methodFace.value = enabled
            UnlockMethod.PATTERN -> _methodPattern.value = enabled
            UnlockMethod.PIN -> _methodPin.value = enabled
            UnlockMethod.PASSWORD -> _methodPassword.value = enabled
        }
        app.getSharedPreferences(SYNC_PREFS, Context.MODE_PRIVATE)
            .edit().putBoolean(method.key, enabled).apply()
        val dsKey = when (method) {
            UnlockMethod.FINGERPRINT -> KEY_METHOD_FINGERPRINT
            UnlockMethod.FACE -> KEY_METHOD_FACE
            UnlockMethod.PATTERN -> KEY_METHOD_PATTERN
            UnlockMethod.PIN -> KEY_METHOD_PIN
            UnlockMethod.PASSWORD -> KEY_METHOD_PASSWORD
        }
        scope.launch {
            runCatching { app.thakurSecurityDataStore.edit { it[dsKey] = enabled } }
                .onFailure { Log.w(TAG, "Failed saving ${method.key}: ${it.message}") }
        }
    }

    fun setWorkTheme(context: Context, isWork: Boolean) {
        val app = context.applicationContext
        _isWorkTheme.value = isWork
        app.getSharedPreferences(SYNC_PREFS, Context.MODE_PRIVATE)
            .edit().putBoolean("is_work_theme", isWork).apply()
        scope.launch {
            runCatching { app.thakurSecurityDataStore.edit { it[KEY_IS_WORK_THEME] = isWork } }
                .onFailure { Log.w(TAG, "Failed saving isWorkTheme: ${it.message}") }
        }
    }

    fun setSecretCode(context: Context, secret: String) {
        val app = context.applicationContext
        val clean = secret.trim()
        encryptedPrefs(app).edit().putString(KEY_SECRET_VALUE, clean).apply()
        _hasSecretCode.value = clean.isNotBlank()
    }

    fun setPinCode(context: Context, pin: String) {
        val app = context.applicationContext
        val clean = pin.trim()
        encryptedPrefs(app).edit().putString(KEY_PIN_VALUE, clean).apply()
        _hasPinCode.value = clean.isNotBlank()
    }

    fun setPatternCode(context: Context, sequence: String) {
        val app = context.applicationContext
        val clean = sequence.trim()
        encryptedPrefs(app).edit().putString(KEY_PATTERN_VALUE, clean).apply()
        _hasPatternCode.value = clean.isNotBlank()
    }

    fun resetToDefault(context: Context) {
        val app = context.applicationContext
        setWakeWord(app, DEFAULT_WAKE_WORD)
        setSecretEnabled(app, false)
        setSecuredAccessEnabled(app, false)
    }

    private fun readEncrypted(app: Context, key: String): String? =
        encryptedPrefs(app).getString(key, null)?.takeIf { it.isNotBlank() }

    private fun encryptedPrefs(context: Context): SharedPreferences =
        runCatching {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            EncryptedSharedPreferences.create(
                context,
                ENCRYPTED_PREFS,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
            )
        }.getOrElse { t ->
            Log.w(TAG, "EncryptedSharedPreferences fallback to private prefs: ${t.message}")
            context.getSharedPreferences(ENCRYPTED_PREFS, Context.MODE_PRIVATE)
        }
}
