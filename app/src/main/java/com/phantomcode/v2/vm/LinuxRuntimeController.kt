package com.phantomcode.v2.vm

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class LinuxRuntimeController(context: Context) {
    private val appContext = context.applicationContext
    private val installer = PhantomDistroInstaller(appContext)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var process: Process? = null

    var state by mutableStateOf(if (installer.isInstalled()) LinuxUiState.Ready else LinuxUiState.NoDistro)
        private set
    var output by mutableStateOf("")
        private set
    var progress by mutableStateOf<Float?>(null)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    fun install() {
        if (state is LinuxUiState.Installing || installer.isInstalled()) return
        state = LinuxUiState.Installing
        error = null
        progress = 0f
        scope.launch {
            val result = installer.install { value -> scope.launch(Dispatchers.Main) { progress = value } }
            withContext(Dispatchers.Main) {
                progress = null
                result.onSuccess { state = LinuxUiState.Ready }.onFailure {
                    state = LinuxUiState.Error
                    error = it.message ?: "Falha ao instalar a distro"
                }
            }
        }
    }

    fun start() {
        if (state !is LinuxUiState.Ready || process != null) return
        state = LinuxUiState.Starting
        error = null
        output = ""
        scope.launch {
            val result = runCatching {
                val distro = File(appContext.filesDir, "linux/phantom")
                val qemu = File(distro, "qemu-system-aarch64")
                val libDir = File(distro, "lib")
                check(qemu.isFile && qemu.canExecute()) { "QEMU não está disponível na distro instalada" }
                check(libDir.isDirectory) { "Bibliotecas do QEMU não encontradas na distro instalada" }
                val command = listOf(
                    qemu.absolutePath, "-M", "virt,accel=tcg", "-cpu", "cortex-a72",
                    "-smp", "2", "-m", "1024", "-L", distro.absolutePath,
                    "-kernel", File(distro, "kernel").absolutePath,
                    "-initrd", File(distro, "initrd.img").absolutePath,
                    "-append", "root=/dev/vda rw console=hvc0 console=ttyAMA0",
                    "-drive", "if=none,format=raw,file=${File(distro, "rootfs.img").absolutePath},id=hd0",
                    "-device", "virtio-blk-device,drive=hd0", "-nographic",
                )
                val builder = ProcessBuilder(command).directory(distro).redirectErrorStream(true)
                builder.environment()["LD_LIBRARY_PATH"] = libDir.absolutePath
                val started = builder.start()
                process = started
                withContext(Dispatchers.Main) { state = LinuxUiState.Running }
                started.inputStream.bufferedReader().useLines { lines ->
                    lines.forEach { line ->
                        scope.launch(Dispatchers.Main) { output = (output + line + "\n").takeLast(32_000) }
                    }
                }
                val code = started.waitFor()
                process = null
                withContext(Dispatchers.Main) {
                    if (state is LinuxUiState.Running) state = LinuxUiState.Ready
                    if (code != 0) error = "QEMU encerrou com código $code"
                }
            }
            result.onFailure {
                process = null
                withContext(Dispatchers.Main) { state = LinuxUiState.Error; error = it.message ?: "Falha ao iniciar Linux" }
            }
        }
    }

    fun stop() {
        process?.let { runCatching { it.destroy() } }
        process = null
        if (state is LinuxUiState.Running || state is LinuxUiState.Starting) state = LinuxUiState.Ready
    }

    fun sendInput(input: String) {
        val active = process ?: return
        scope.launch { runCatching { active.outputStream.write((input + "\n").toByteArray()); active.outputStream.flush() } }
    }
}

sealed interface LinuxUiState {
    data object NoDistro : LinuxUiState
    data object Installing : LinuxUiState
    data object Ready : LinuxUiState
    data object Starting : LinuxUiState
    data object Running : LinuxUiState
    data object Error : LinuxUiState
}
