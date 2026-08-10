package com.phantomcode.v2.vm

import android.content.Context

/** Primeiro adaptador V2: a UI conversa com este controlador, nunca com Process. */
class LinuxRuntimeController(@Suppress("UNUSED_PARAMETER") private val context: Context)

sealed interface LinuxUiState {
    data object NoDistro : LinuxUiState
    data class Ready(val distroId: String) : LinuxUiState
    data class Running(val distroId: String) : LinuxUiState
}
