package com.phantomcode.v2.vm

import android.content.Context
import java.io.File
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.util.zip.GZIPInputStream

private const val PHANTOM_URL = "https://github.com/VSFLima/phantom-releases/releases/download/distro-phantom/phantom.tar.gz"
private const val PHANTOM_SHA256 = "79d591f67913a33edd22abfa0ac0ff9bf37c053b427d425f155767fb60304d74"

class PhantomDistroInstaller(context: Context) {
    private val root = File(context.filesDir, "linux/phantom")
    private val temp = File(context.filesDir, "linux/phantom.installing")

    fun isInstalled(): Boolean = File(root, "rootfs.img").isFile && File(root, "kernel").isFile && File(root, "initrd.img").isFile

    fun install(onProgress: (Float) -> Unit = {}): Result<Unit> = runCatching {
        temp.deleteRecursively()
        temp.mkdirs()
        val archive = File(temp, "phantom.tar.gz")
        val connection = URL(PHANTOM_URL).openConnection() as HttpURLConnection
        connection.connectTimeout = 20_000
        connection.readTimeout = 120_000
        connection.instanceFollowRedirects = true
        connection.setRequestProperty("User-Agent", "Phantom-Code-V2")
        check(connection.responseCode in 200..299) { "Download HTTP ${connection.responseCode}" }
        val digest = MessageDigest.getInstance("SHA-256")
        connection.inputStream.use { input ->
            archive.outputStream().use { output ->
                val buffer = ByteArray(64 * 1024)
                var total = 0L
                val length = connection.contentLengthLong
                var count: Int
                while (input.read(buffer).also { count = it } >= 0) {
                    if (count == 0) continue
                    output.write(buffer, 0, count)
                    digest.update(buffer, 0, count)
                    total += count
                    if (length > 0) onProgress(total.toFloat() / length.toFloat())
                }
            }
        }
        val actual = digest.digest().joinToString("") { "%02x".format(it) }
        check(actual.equals(PHANTOM_SHA256, ignoreCase = true)) { "SHA-256 da distro inválido" }
        GZIPInputStream(archive.inputStream()).use { TarArchive.extract(it, temp) }
        check(File(temp, "rootfs.img").isFile && File(temp, "kernel").isFile && File(temp, "initrd.img").isFile) {
            "Pacote da distro incompleto"
        }
        root.deleteRecursively()
        check(temp.renameTo(root)) { "Não foi possível finalizar a instalação" }
    }
}

private object TarArchive {
    fun extract(input: InputStream, destination: File) {
        val root = destination.canonicalFile
        val header = ByteArray(512)
        val buffer = ByteArray(64 * 1024)
        while (readFully(input, header)) {
            if (header.all { it == 0.toByte() }) break
            val name = String(header, 0, 100, Charsets.UTF_8).trimEnd('\u0000').trimStart('/', '.')
            if (name.isBlank()) break
            val size = String(header, 124, 12, Charsets.UTF_8).trim().trimEnd('\u0000').toLongOrNull(8) ?: 0L
            val target = File(destination, name).canonicalFile
            check(target.path.startsWith(root.path + File.separator)) { "Entrada TAR inválida" }
            if (header[156].toInt().toChar() == '5' || name.endsWith('/')) {
                target.mkdirs()
            } else {
                target.parentFile?.mkdirs()
                target.outputStream().use { output ->
                    var remaining = size
                    while (remaining > 0) {
                        val read = input.read(buffer, 0, minOf(buffer.size.toLong(), remaining).toInt())
                        check(read > 0) { "TAR truncado" }
                        output.write(buffer, 0, read)
                        remaining -= read
                    }
                }
            }
            val padding = ((512 - (size % 512)) % 512).toInt()
            var skipped = 0
            while (skipped < padding) {
                val read = input.read(buffer, 0, minOf(buffer.size, padding - skipped))
                check(read > 0) { "TAR truncado" }
                skipped += read
            }
        }
    }

    private fun readFully(input: InputStream, buffer: ByteArray): Boolean {
        var offset = 0
        while (offset < buffer.size) {
            val read = input.read(buffer, offset, buffer.size - offset)
            if (read < 0) return false
            offset += read
        }
        return true
    }
}
