package com.merfemor.aesencryptor.crypto

import android.util.Base64
import com.merfemor.aesencryptor.util.Either
import java.nio.charset.Charset
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class OpenSSLAesController {

    private fun getKeyLengthBytes(algorithm: EncodeAlgorithm): Int {
        val keySizeBits = when (algorithm) {
            EncodeAlgorithm.AES_128_CBC -> 128
            EncodeAlgorithm.AES_256_CBC -> 256
        }
        return keySizeBits / Byte.SIZE_BITS
    }

    fun decode(
        base64Text: String,
        password: String,
        algorithm: EncodeAlgorithm
    ): Either<String, Throwable> {
        val saltAndCipherText = Base64.decode(base64Text, Base64.DEFAULT)

        // header is "Salted__", ASCII encoded, if salt is being used (the default)
        val salt = saltAndCipherText.copyOfRange(SALT_OFFSET, SALT_OFFSET + SALT_SIZE)
        // following bytes is cipher text
        val cipherText = saltAndCipherText.copyOfRange(CIPHER_TEXT_OFFSET, saltAndCipherText.size)

        val keyLengthBytes = getKeyLengthBytes(algorithm)

        // the IV is useless, OpenSSL might as well have use zero's
        val (keyBytes, ivBytes) = openSslEvpBytesToKeyAndIv(
            keyLengthBytes,
            cipherAesCbc.blockSize,
            md5MessageDigest,
            salt,
            password.toByteArray(asciiCharset),
            ITERATIONS
        )
        val key = SecretKeySpec(keyBytes, "AES")
        val iv = IvParameterSpec(ivBytes)

        cipherAesCbc.init(Cipher.DECRYPT_MODE, key, iv)
        return try {
            val decrypted = cipherAesCbc.doFinal(cipherText)
            Either.left(String(decrypted))
        } catch (e: Exception) {
            Either.right(e)
        }
    }

    /*
    val textBytes = Base64.decode(base64Text, Base64.DEFAULT)
        val passwordBytes = password.toByteArray()

        val keyLengthBits = getKeyLengthBits(algorithm)

        // first 8 bytes is "Salted__",  next 8 bytes is the salt
        val salt = Arrays.copyOfRange(textBytes, 8, AES_SALT_OFFSET_BYTES)
        val paramsGenerator = OpenSSLPBEParametersGenerator()
        paramsGenerator.init(passwordBytes, salt)
        val cipherParams = paramsGenerator.generateDerivedParameters(keyLengthBits, AES_INIT_VECTOR_BITS)
        val cipher = PaddedBufferedBlockCipher(CBCBlockCipher(AESEngine())) // , PKCS7Padding() ??
        cipher.reset()
        cipher.init(false, cipherParams)

        val bufferLen = cipher.getOutputSize(textBytes.size - AES_SALT_OFFSET_BYTES)
        val buffer = ByteArray(bufferLen)
        var len = cipher.processBytes(textBytes, AES_SALT_OFFSET_BYTES, textBytes.size - AES_SALT_OFFSET_BYTES, buffer, 0)
        len += cipher.doFinal(buffer, len)

        val bytesDesc = buffer.copyOf()
     */

    /**
     * This kotlin impl of is OpenSSL proprietary algorithm for deriving key and init vector bytes
     * from salt and password.
     * https://www.openssl.org/docs/manmaster/man3/EVP_BytesToKey.html
     *
     * Thanks go to Ola Bini for releasing this source on his blog.
     * The source was obtained from http://olabini.com/blog/tag/evp_bytestokey/.
     */
    private fun openSslEvpBytesToKeyAndIv(
        keyLengthBytes: Int, ivLengthBytes: Int, messageDigest: MessageDigest,
        salt: ByteArray?, data: ByteArray, count: Int
    ): Pair<ByteArray, ByteArray> {
        val key = ByteArray(keyLengthBytes)
        var key_ix = 0
        val iv = ByteArray(ivLengthBytes)
        var iv_ix = 0
        val result = key to iv
        var md_buf: ByteArray? = null
        var nkey = keyLengthBytes
        var niv = ivLengthBytes
        var i = 0
        var addmd = 0
        while (true) {
            messageDigest.reset()
            if (addmd++ > 0) {
                messageDigest.update(md_buf)
            }
            messageDigest.update(data)
            if (null != salt) {
                messageDigest.update(salt, 0, 8)
            }
            md_buf = messageDigest.digest()
            i = 1
            while (i < count) {
                messageDigest.reset()
                messageDigest.update(md_buf)
                md_buf = messageDigest.digest()
                i++
            }
            i = 0
            if (nkey > 0) {
                while (true) {
                    if (nkey == 0) break
                    if (i == md_buf!!.size) break
                    key[key_ix++] = md_buf[i]
                    nkey--
                    i++
                }
            }
            if (niv > 0 && i != md_buf!!.size) {
                while (true) {
                    if (niv == 0) break
                    if (i == md_buf.size) break
                    iv[iv_ix++] = md_buf[i]
                    niv--
                    i++
                }
            }
            if (nkey == 0 && niv == 0) {
                break
            }
        }
        i = 0
        while (i < md_buf!!.size) {
            md_buf[i] = 0
            i++
        }
        return result
    }

    private companion object {
        private const val ITERATIONS = 1
        private const val SALT_OFFSET = 8
        private const val SALT_SIZE = 8
        private const val CIPHER_TEXT_OFFSET = SALT_OFFSET + SALT_SIZE

        private val cipherAesCbc = Cipher.getInstance("AES/CBC/PKCS5Padding")
        private val md5MessageDigest = MessageDigest.getInstance("MD5")
        private val asciiCharset = Charset.forName("ASCII")
    }
}

