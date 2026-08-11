package com.phantomcode.v2.vm

import android.content.Context
import java.io.File
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.util.zip.GZIPInputStream

private const val PHANTOM_URL = "https://github.com/VSFLima/phantom-code-v2/releases/download/distro-phantom/phantom.tar.gz"
private const val PHANTOM_SHA_URL = "https://github.com/VSFLima/phantom-code-v2/releases/download/distro-phantom/phantom.sha256"
private const val DISTRO_VERSION = 5 // Debian 12 (bookworm) arm64, com apt + systemd

class PhantomDistroInstaller(context: Context) {
    private val root = File(context.filesDir, "linux/phantom")
    private val temp = File(context.filesDir, "linux/phantom.installing")

    fun isInstalled(): Boolean = File(root, "rootfs.img").isFile && File(root, "kernel").isFile && File(root, "initrd.img").isFile && File(root, "qemu-system-aarch64").isFile && installedVersion() == DISTRO_VERSION

    private fun installedVersion(): Int? = runCatching { File(root, "VERSION").readText().trim().toInt() }.getOrNull()

    fun install(onProgress: (Float) -> Unit = {}, onLog: (String) -> Unit = {}): Result<Unit> = runCatching {
        temp.deleteRecursively()
        temp.mkdirs()
        val archive = File(temp, "phantom.tar.gz")
        val connection = URL(PHANTOM_URL).openConnection() as HttpURLConnection
        connection.connectTimeout = 20_000
        connection.readTimeout = 120_000
        connection.instanceFollowRedirects = true
        connection.setRequestProperty("User-Agent", "Phantom-Code-V2")
        check(connection.responseCode in 200..299) { "Download HTTP ${connection.responseCode}" }
        onLog("Baixando distro…")
        val digest = MessageDigest.getInstance("SHA-256")
        connection.inputStream.use { input ->
            archive.outputStream().use { output ->
                val buffer = ByteArray(64 * 1024)
                var total = 0L
                val length = connection.contentLengthLong
                var count: Int
                var lastLogged = -1L
                while (input.read(buffer).also { count = it } >= 0) {
                    if (count == 0) continue
                    output.write(buffer, 0, count)
                    digest.update(buffer, 0, count)
                    total += count
                    if (length > 0) {
                        onProgress(total.toFloat() / length.toFloat())
                        val mb = total / (1000 * 1000)
                        if (mb != lastLogged) {
                            lastLogged = mb
                            if (mb % 25 == 0L || mb == length / (1000 * 1000)) onLog("Baixado $mb MB")
                        }
                    }
                }
            }
        }
        onLog("Download concluído. Verificando SHA-256…")
        val actual = digest.digest().joinToString("") { "%02x".format(it) }
        val expectedSha = downloadSha()
        check(actual.equals(expectedSha, ignoreCase = true)) { "SHA-256 da distro inválido (esperado $expectedSha)" }
        onLog("SHA-256 OK. Extraindo…")
        GZIPInputStream(archive.inputStream()).use { TarArchive.extract(it, temp, onLog) }
        check(File(temp, "rootfs.img").isFile && File(temp, "kernel").isFile && File(temp, "initrd.img").isFile) {
            "Pacote da distro incompleto"
        }
        onLog("Aplicando permissões…")
        makeExecutable(File(temp, "qemu-system-aarch64"))
        File(temp, "lib").listFiles()?.forEach { makeExecutable(it) }
        onLog("Finalizando instalação…")
        root.deleteRecursively()
        check(temp.renameTo(root)) { "Não foi possível finalizar a instalação" }
        onLog("Distro instalada (v$DISTRO_VERSION).")
    }

    private fun downloadSha(): String {
        val connection = URL(PHANTOM_SHA_URL).openConnection() as HttpURLConnection
        connection.connectTimeout = 20_000
        connection.readTimeout = 30_000
        connection.instanceFollowRedirects = true
        connection.setRequestProperty("User-Agent", "Phantom-Code-V2")
        check(connection.responseCode in 200..299) { "Falha ao obter o checksum da distro (HTTP ${connection.responseCode})" }
        return connection.inputStream.bufferedReader().use { it.readText().trim() }
    }

    private fun makeExecutable(file: File) {
        if (!file.isFile) return
        file.setExecutable(true, false)
        file.setReadable(true)
        file.setWritable(false)
    }
}

private object TarArchive {
    fun extract(input: InputStream, destination: File, onFile: (String) -> Unit = {}) {
        val root = destination.canonicalFile
        val header = ByteArray(512)
        val buffer = ByteArray(64 * 1024)
        var index = 0
        while (readFully(input, header)) {
            if (header.all { it == 0.toByte() }) break
            val name = String(header, 0, 100, Charsets.UTF_8).trimEnd('\u0000').trimStart('/', '.')
            if (name.isBlank()) continue
            val size = String(header, 124, 12, Charsets.UTF_8).trim().trimEnd('\u0000').toLongOrNull(8) ?: 0L
            val target = File(destination, name).canonicalFile
            check(target.path.startsWith(root.path + File.separator)) { "Entrada TAR inválida" }
            index++
            onFile("Extraindo $index: $name")
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
