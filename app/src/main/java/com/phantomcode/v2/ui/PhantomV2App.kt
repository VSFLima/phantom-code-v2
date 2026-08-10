package com.phantomcode.v2.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.phantomcode.v2.vm.LinuxRuntimeController
import com.phantomcode.v2.vm.LinuxUiState

@Composable
fun PhantomV2App(runtime: LinuxRuntimeController) {
    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF0B0B12)).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text("PHANTOM-CODE V2", color = Color.White, fontSize = 22.sp)
        Text("Editor mobile + Linux, com um núcleo simples e previsível.", color = Color(0xFFB7B3C6))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Terminal, contentDescription = null)
                    Text("Linux", modifier = Modifier.weight(1f))
                    Text(runtime.state.label(), fontSize = 12.sp)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { runtime.installPlaceholder() },
                        enabled = runtime.state is LinuxUiState.NoDistro,
                    ) { Text("Preparar distro") }
                    Button(
                        onClick = { runtime.start() },
                        enabled = runtime.state is LinuxUiState.Ready,
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Text(" Iniciar")
                    }
                    OutlinedButton(
                        onClick = { runtime.stop() },
                        enabled = runtime.state is LinuxUiState.Running,
                    ) { Text("Parar") }
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth().weight(1f)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Code, contentDescription = null)
                    Text("Editor")
                }
                Text("O editor V2 será adicionado aqui com arquivos reais do workspace.", color = Color(0xFFB7B3C6))
                Spacer(Modifier.height(4.dp))
                Text("Primeiro validamos o contrato do Linux; depois conectamos o editor sem misturar processos e UI.", color = Color(0xFFB7B3C6), fontSize = 12.sp)
            }
        }
    }
}

private fun LinuxUiState.label(): String = when (this) {
    LinuxUiState.NoDistro -> "Nenhuma distro"
    is LinuxUiState.Ready -> "Pronto"
    is LinuxUiState.Running -> "Linux ativo"
}
