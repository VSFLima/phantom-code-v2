package com.phantomcode.v2.vm

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** Primeiro adaptador V2: a UI conversa com este controlador, nunca com Process. */
class LinuxRuntimeController(private val context: Context) {
    var state by mutableStateOf(LinuxUiState.NoDistro)
        private set

    fun installPlaceholder() {
        // O instalador real entra no próximo milestone, depois do contrato de UI.
        state = LinuxUiState.Ready("phantom")
    }

    fun start() {
        if (state is LinuxUiState.Ready) state = LinuxUiState.Running("phantom")
    }

    fun stop() {
        if (state is LinuxUiState.Running) state = LinuxUiState.Ready("phantom")
    }
}

sealed interface LinuxUiState {
    data object NoDistro : LinuxUiState
    data class Ready(val distroId: String) : LinuxUiState
    data class Running(val distroId: String) : LinuxUiState
}
