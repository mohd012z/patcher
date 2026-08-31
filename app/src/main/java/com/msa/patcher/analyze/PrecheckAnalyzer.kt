package com.msa.patcher.analyze

import android.content.Context
import android.net.Uri
import com.msa.patcher.model.ApkSummary
import java.security.MessageDigest
import java.util.zip.ZipInputStream

object PrecheckAnalyzer {
    fun analyze(context: Context, uri: Uri, displayName: String): ApkSummary {
        val resolver = context.contentResolver
        val digest = MessageDigest.getInstance("SHA-256")
        var size = 0L
        resolver.openInputStream(uri)!!.use { input ->
            val buffer = ByteArray(64 * 1024)
            while (true) {
                val n = input.read(buffer)
                if (n <= 0) break
                digest.update(buffer, 0, n)
                size += n
            }
        }
        var dex = 0
        var native = 0
        var embeddedDex = 0
        var embeddedApk = 0
        var signing = false
        val abis = linkedSetOf<String>()
        resolver.openInputStream(uri)!!.use { input ->
            ZipInputStream(input).use { zip ->
                while (true) {
                    val entry = zip.nextEntry ?: break
                    val n = entry.name
                    if (Regex("^classes\\d*\\.dex$").matches(n)) dex++
                    else if (n.endsWith(".dex", true)) embeddedDex++
                    if (n.endsWith(".apk", true)) embeddedApk++
                    if (n.endsWith(".so", true)) {
                        native++
                        val parts = n.split('/')
                        if (parts.size >= 3 && parts[0] == "lib") abis += parts[1]
                    }
                    if (n.startsWith("META-INF/") && (n.endsWith(".RSA", true) || n.endsWith(".DSA", true) || n.endsWith(".EC", true))) signing = true
                }
            }
        }
        return ApkSummary(
            displayName, size, digest.digest().joinToString("") { "%02x".format(it) },
            dex, native, abis, embeddedDex, embeddedApk, signing
        )
    }
}
