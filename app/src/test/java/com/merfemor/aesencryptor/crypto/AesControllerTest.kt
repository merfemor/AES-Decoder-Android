package com.merfemor.aesencryptor.crypto

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import javax.crypto.BadPaddingException


@RunWith(RobolectricTestRunner::class)
class AesControllerTest {
    private val forTest = OpenSSLAesController()

    @Test
    fun `Decode AES 128 CBC, password is ok`() {
        val decoded = forTest.decode(
            ENCODED_AES_128_CBC_BASE64,
            PASSWORD, EncodeAlgorithm.AES_128_CBC)
        assertEquals(ORIGINAL_TEXT, decoded.getLeftSure())
    }

    @Test
    fun `Decode AES 256 CBC, password is ok`() {
        val decoded = forTest.decode(
            ENCODED_AES_256_CBC_BASE64,
            PASSWORD, EncodeAlgorithm.AES_256_CBC)
        assertEquals(ORIGINAL_TEXT, decoded.getLeftSure())
    }

    @Test
    fun `Decode AES 256 CBC, password is bad`() {
        val decoded = forTest.decode(
            ENCODED_AES_256_CBC_BASE64,
            BAD_PASSWORD, EncodeAlgorithm.AES_256_CBC)
        assertEquals(BadPaddingException::class, decoded.getRightSure()::class)
    }

    @Test
    fun `Decode AES 256 CBC, data is bad`() {
        val decoded = forTest.decode(
            ENCODED_AES_256_CBC_BASE64_BAD,
            BAD_PASSWORD, EncodeAlgorithm.AES_256_CBC)
        assertEquals(BadPaddingException::class, decoded.getRightSure()::class)
    }

    private companion object {
        private const val ORIGINAL_TEXT = "Hello, world!\n"
        private const val PASSWORD = "123456"
        private const val BAD_PASSWORD = "1234567"

        /**
         * Encoded with such cmd:
         * openssl enc -salt -aes-256-cbc -a -in <file with original text> -pass pass:<password text>
         * using LibreSSL 2.6.5
         */
        private const val ENCODED_AES_256_CBC_BASE64 = "U2FsdGVkX188/tAGTgbokFMhBDMtVA9ceqXyc4pPqes="
        private const val ENCODED_AES_256_CBC_BASE64_BAD = "U2FsdGVkX188/tBGTgbokFMhBDMtVA9ceqXyc4pPqes="
        private const val ENCODED_AES_128_CBC_BASE64 = "U2FsdGVkX18FptbSF8KokdhuIvqvQHZOMhKlCq6QjSg="
    }
}