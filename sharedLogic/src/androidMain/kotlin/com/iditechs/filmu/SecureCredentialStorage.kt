package com.iditechs.filmu

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import android.util.Base64
import kotlinx.serialization.json.Json

class SecureCredentialStorage(context: Context) : CredentialStorage {

    private val prefs = context.getSharedPreferences("filmu_secure_prefs", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    private fun getOrCreateKey(): SecretKey {
        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        ks.getEntry("filmu_credential_key", null)?.let {
            return (it as KeyStore.SecretKeyEntry).secretKey
        }
        val spec = KeyGenParameterSpec.Builder(
            "filmu_credential_key",
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            .apply { init(spec) }
            .generateKey()
    }

    private fun encrypt(plaintext: String): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val iv = cipher.iv
        val encrypted = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
        val combined = iv + encrypted
        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    private fun decrypt(encoded: String): String {
        val combined = Base64.decode(encoded, Base64.NO_WRAP)
        val iv = combined.sliceArray(0 until 12)
        val ciphertext = combined.sliceArray(12 until combined.size)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(128, iv))
        return String(cipher.doFinal(ciphertext), Charsets.UTF_8)
    }

    override suspend fun getCredentials(): ServerCredentials? {
        val stored = prefs.getString("credentials", null) ?: return null
        return try {
            json.decodeFromString<ServerCredentials>(decrypt(stored))
        } catch (_: Exception) {
            prefs.edit().remove("credentials").apply()
            null
        }
    }

    override suspend fun setCredentials(credentials: ServerCredentials?) {
        if (credentials == null) {
            prefs.edit().remove("credentials").apply()
        } else {
            val serialized = json.encodeToString(ServerCredentials.serializer(), credentials)
            prefs.edit().putString("credentials", encrypt(serialized)).apply()
        }
    }

    override suspend fun clear() {
        prefs.edit().remove("credentials").apply()
    }
}
