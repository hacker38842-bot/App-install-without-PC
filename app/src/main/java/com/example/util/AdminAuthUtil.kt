package com.example.util

import java.security.MessageDigest
import java.security.SecureRandom
import java.text.Normalizer
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object AdminAuthUtil {

    private const val ITERATIONS = 10_000
    private const val KEY_LENGTH_BITS = 256
    private val secureRandom = SecureRandom()

    const val ADMIN_SALT_HEX = "57f85a95ccfd74afd40b8875dee2d0be"
    const val ADMIN_PASSWORD_HASH_HEX = "1cddcaac279274eca616c9e40bdb5f01437e2680600b76ce9065f262471411b1"
    const val ADMIN_DISPLAY_NAME = "𝑴𝒖𝒔𝒌𝒂𝒏 𝑷𝒂𝒏𝒆𝒍 𝑺𝒕𝒐𝒓𝒆"
    const val ADMIN_USERNAME_NORMALIZED = "muskan panel store"

    /**
     * Normalizes Unicode characters (such as mathematical bold/italic characters)
     * and trims whitespace to allow robust matching.
     */
    fun normalizeUsername(input: String): String {
        val nfkd = Normalizer.normalize(input.trim(), Normalizer.Form.NFKD)
        // Strip accents and diacritics and convert to lowercase for comparison
        val ascii = nfkd.replace("\\p{M}".toRegex(), "")
        return ascii.lowercase().replace("\\s+".toRegex(), " ")
    }

    /**
     * Generates a 16-byte cryptographic random salt.
     */
    fun generateSalt(): ByteArray {
        val salt = ByteArray(16)
        secureRandom.nextBytes(salt)
        return salt
    }

    /**
     * Hashes password using PBKDF2WithHmacSHA256 with a salt and 10,000 iterations.
     */
    fun hashPassword(password: String, salt: ByteArray): String {
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BITS)
        val skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val hash = skf.generateSecret(spec).encoded
        return bytesToHex(hash)
    }

    /**
     * Constant-time comparison to prevent timing attacks.
     */
    fun verifyPassword(password: String, saltHex: String, expectedHashHex: String): Boolean {
        return try {
            val salt = hexToBytes(saltHex)
            val computedHashHex = hashPassword(password, salt)
            val expectedBytes = hexToBytes(expectedHashHex)
            val computedBytes = hexToBytes(computedHashHex)
            MessageDigest.isEqual(expectedBytes, computedBytes)
        } catch (e: Exception) {
            false
        }
    }

    fun bytesToHex(bytes: ByteArray): String {
        val sb = StringBuilder(bytes.size * 2)
        for (b in bytes) {
            sb.append(String.format("%02x", b))
        }
        return sb.toString()
    }

    fun hexToBytes(hex: String): ByteArray {
        val len = hex.length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] = ((Character.digit(hex[i], 16) shl 4) + Character.digit(hex[i + 1], 16)).toByte()
            i += 2
        }
        return data
    }
}
