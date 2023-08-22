package net.ivpn.core.common.utils

/*
 IVPN Android app
 https://github.com/ivpn/android-app

 Created by Juraj Hilje.
 Copyright (c) 2023 IVPN Limited.

 This file is part of the IVPN Android app.

 The IVPN Android app is free software: you can redistribute it and/or
 modify it under the terms of the GNU General Public License as published by the Free
 Software Foundation, either version 3 of the License, or (at your option) any later version.

 The IVPN Android app is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details.

 You should have received a copy of the GNU General Public License
 along with the IVPN Android app. If not, see <https://www.gnu.org/licenses/>.
*/

import android.util.Base64
import net.ivpn.liboqs.KeyEncapsulation
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

enum class KemAlgorithm(val value: String) {
    Kyber1024("Kyber1024")
}

class KEM {

    private val algorithms: List<KemAlgorithm> = listOf(KemAlgorithm.Kyber1024)
    private val publicKeys = mutableListOf<String>()
    private val privateKeys = mutableListOf<String>()
    private val ciphers = mutableListOf<String>()
    private val secrets = mutableListOf<String>()

    init {
        algorithms.forEach {
            val keyPair = generateKeys(it)
            publicKeys.add(keyPair.first)
            privateKeys.add(keyPair.second)
        }
    }

    fun getPublicKey(algorithm: KemAlgorithm): String {
        val index = algorithms.indexOf(algorithm)
        return publicKeys[index]
    }

    fun setCipher(algorithm: KemAlgorithm, cipher: String) {
        ciphers.add(cipher)
    }

    fun calculatePresharedKey(): String? {
        decodeCiphers(ciphers)
        return hashSecrets(secrets)
    }

    private fun generateKeys(algorithm: KemAlgorithm): Pair<String, String> {
        val client = KeyEncapsulation(algorithm.toString())
        val publicKey = client.generate_keypair()
        val secretKey = client.export_secret_key()
        val publicKeyBase64 = Base64.encode(publicKey, Base64.DEFAULT).decodeToString()
        val secretKeyBase64 = Base64.encode(secretKey, Base64.DEFAULT).decodeToString()
        return Pair(publicKeyBase64, secretKeyBase64)
    }

    private fun decodeCipher(algorithm: KemAlgorithm, privateKeyBase64: String, cipherBase64: String): String {
        val secretKey = Base64.decode(privateKeyBase64, Base64.DEFAULT)
        val client = KeyEncapsulation(algorithm.toString(), secretKey)
        val cipherText = Base64.decode(cipherBase64, Base64.DEFAULT)
        val sharedSecret = client.decap_secret(cipherText)
        return Base64.encode(sharedSecret, Base64.DEFAULT).decodeToString()
    }

    private fun decodeCiphers(ciphers: List<String>) {
        ciphers.forEachIndexed { index, cipher ->
            val algo = algorithms[index]
            val privateKey = privateKeys[index]
            secrets.add(decodeCipher(algo, privateKey, cipher))
        }
    }

    private fun hashSecrets(secrets: List<String>): String? {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            for (secret in secrets) {
                val secretBytes: ByteArray = Base64.decode(secret, Base64.DEFAULT)
                digest.update(secretBytes)
            }
            val hashBytes = digest.digest()
            Base64.encode(hashBytes, Base64.DEFAULT).decodeToString().trim()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
            null
        }
    }

}