package com.phantomcode.v2.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.phantomcode.v2.vm.LinuxRuntimeController
import com.phantomcode.v2.workspace.WorkspaceFile
import com.phantomcode.v2.workspace.WorkspaceProject
import com.phantomcode.v2.workspace.WorkspaceService

private enum class Screen { PROJECTS, FILES, EDITOR, LINUX }

@Composable
fun PhantomV2App(runtime: LinuxRuntimeController) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val workspace = remember { WorkspaceService(context) }
    var screen by remember { mutableStateOf(Screen.PROJECTS) }
    var projects by remember { mutableStateOf(workspace.projects()) }
    var selectedProject by remember { mutableStateOf<WorkspaceProject?>(null) }
    var selectedFile by remember { mutableStateOf<WorkspaceFile?>(null) }
    var editorText by remember { mutableStateOf("") }
    var newProjectOpen by remember { mutableStateOf(false) }
    var newFileOpen by remember { mutableStateOf(false) }
    var nameInput by remember { mutableStateOf("") }
    var savedMessage by remember { mutableStateOf("") }
    var activityMessage by remember { mutableStateOf("") }

    LaunchedEffect(workspace) {
        workspace.events.collect { event ->
            activityMessage = "${event.actorId} · ${event.operation.name.lowercase()} · ${event.path.ifBlank { event.project }}"
        }
    }

    fun openProject(project: WorkspaceProject) {
        selectedProject = project
        screen = Screen.FILES
    }

    fun openFile(file: WorkspaceFile) {
        if (file.isDirectory) return
        selectedFile = file
        editorText = workspace.read(file.file)
        savedMessage = ""
        screen = Screen.EDITOR
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF0B0B12)),
    ) {
        Header(screen, selectedProject?.name, onBack = {
            screen = when (screen) {
                Screen.EDITOR -> Screen.FILES
                Screen.FILES -> Screen.PROJECTS
                else -> Screen.PROJECTS
            }
        }, onLinux = { screen = Screen.LINUX })
        when (screen) {
            Screen.PROJECTS -> ProjectsScreen(
                projects = projects,
                onOpen = ::openProject,
                onNew = { nameInput = ""; newProjectOpen = true },
            )
            Screen.FILES -> FilesScreen(
                project = selectedProject,
                workspace = workspace,
                onOpen = ::openFile,
                onNew = { nameInput = ""; newFileOpen = true },
                activityMessage = activityMessage,
            )
            Screen.EDITOR -> EditorScreen(
                file = selectedFile,
                text = editorText,
                savedMessage = savedMessage,
                onTextChange = { editorText = it; savedMessage = "" },
                onSave = {
                    val project = selectedProject
                    val file = selectedFile
                    if (project != null && file != null) {
                        savedMessage = if (workspace.write(project, file, editorText)) "Salvo" else "Falha ao salvar"
                    }
                },
            )
            Screen.LINUX -> LinuxScreen()
        }
    }

    if (newProjectOpen) {
        NameDialog(
            title = "Novo projeto",
            value = nameInput,
            onValueChange = { nameInput = it },
            onConfirm = {
                workspace.createProject(nameInput)?.let { project ->
                    projects = workspace.projects()
                    newProjectOpen = false
                    openProject(project)
                }
            },
            onDismiss = { newProjectOpen = false },
        )
    }
    if (newFileOpen) {
        NameDialog(
            title = "Novo arquivo",
            value = nameInput,
            onValueChange = { nameInput = it },
            onConfirm = {
                selectedProject?.let { project ->
                    if (workspace.createFile(project, nameInput)) {
                        newFileOpen = false
                        workspace.files(project).firstOrNull { it.name == nameInput.trim() }?.let(::openFile)
                    }
                }
            },
            onDismiss = { newFileOpen = false },
        )
    }
}

@Composable
private fun Header(screen: Screen, projectName: String?, onBack: () -> Unit, onLinux: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
        if (screen != Screen.PROJECTS) IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Voltar", tint = Color.White) }
        Text(projectName ?: "PHANTOM-CODE V2", color = Color.White, fontSize = 18.sp, modifier = Modifier.weight(1f))
        TextButton(onClick = onLinux) { Icon(Icons.Default.Terminal, null); Text(" Linux") }
    }
}

@Composable
private fun ProjectsScreen(projects: List<WorkspaceProject>, onOpen: (WorkspaceProject) -> Unit, onNew: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Projetos", color = Color.White, fontSize = 24.sp)
        Text("Workspace local persistente, separado do runtime Linux.", color = Color(0xFFB7B3C6))
        Button(onClick = onNew) { Icon(Icons.Default.CreateNewFolder, null); Text(" Novo projeto") }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(projects, key = { it.name }) { project ->
                Row(Modifier.fillMaxWidth().clickable { onOpen(project) }.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Folder, null, tint = Color(0xFFB794F4))
                    Spacer(Modifier.width(10.dp))
                    Text(project.name, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun FilesScreen(project: WorkspaceProject?, workspace: WorkspaceService, onOpen: (WorkspaceFile) -> Unit, onNew: () -> Unit, activityMessage: String) {
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Arquivos", color = Color.White, fontSize = 22.sp, modifier = Modifier.weight(1f))
            OutlinedButton(onClick = onNew) { Text("Novo arquivo") }
        }
        Spacer(Modifier.height(12.dp))
        if (activityMessage.isNotBlank()) Text("Atividade: $activityMessage", color = Color(0xFF68D391), fontSize = 12.sp)
        project?.let { current ->
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(workspace.files(current), key = { it.file.absolutePath }) { file ->
                    Row(Modifier.fillMaxWidth().clickable { onOpen(file) }.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(if (file.isDirectory) Icons.Default.Folder else Icons.Default.Description, null, tint = Color(0xFFB794F4))
                        Spacer(Modifier.width(10.dp))
                        Text(file.name, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun EditorScreen(file: WorkspaceFile?, text: String, savedMessage: String, onTextChange: (String) -> Unit, onSave: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Code, null, tint = Color(0xFFB794F4))
            Spacer(Modifier.width(8.dp))
            Text(file?.name ?: "Arquivo", color = Color.White, modifier = Modifier.weight(1f))
            Text(savedMessage, color = Color(0xFF68D391), fontSize = 12.sp)
            IconButton(onClick = onSave) { Icon(Icons.Default.Save, "Salvar", tint = Color.White) }
        }
        BasicTextField(
            value = text,
            onValueChange = onTextChange,
            modifier = Modifier.fillMaxSize().padding(16.dp),
            textStyle = TextStyle(color = Color(0xFFE2E8F0), fontFamily = FontFamily.Monospace, fontSize = 14.sp),
            cursorBrush = SolidColor(Color(0xFFB794F4)),
        )
    }
}

@Composable
private fun LinuxScreen() {
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(Icons.Default.Terminal, null, tint = Color(0xFFB794F4))
        Text("Linux", color = Color.White, fontSize = 24.sp)
        Text("O runtime QEMU será conectado nesta tela após o instalador transacional e a sessão de terminal passarem pelos testes. Nenhuma instalação falsa é apresentada.", color = Color(0xFFB7B3C6))
    }
}

@Composable
private fun NameDialog(title: String, value: String, onValueChange: (String) -> Unit, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            BasicTextField(value = value, onValueChange = onValueChange, singleLine = true, modifier = Modifier.fillMaxWidth())
        },
        confirmButton = { Button(onClick = onConfirm, enabled = value.isNotBlank()) { Text("Criar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}
