package com.phantomcode.v2.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.phantomcode.v2.storage.StorageAccess
import com.phantomcode.v2.vm.LinuxRuntimeController
import com.phantomcode.v2.workspace.WorkspaceFile
import com.phantomcode.v2.workspace.WorkspaceProject
import com.phantomcode.v2.workspace.WorkspaceService

private enum class Screen { PROJECTS, FILES, EDITOR, LINUX }

@Composable
fun PhantomV2App(runtime: LinuxRuntimeController) {
    val context = LocalContext.current
    var storageGranted by remember { mutableStateOf(StorageAccess.hasAccess(context)) }
    val workspace = remember(storageGranted) { WorkspaceService(context) }
    var screen by remember { mutableStateOf(Screen.PROJECTS) }
    var projects by remember(storageGranted) { mutableStateOf(workspace.projects()) }
    var selectedProject by remember { mutableStateOf<WorkspaceProject?>(null) }
    var selectedFile by remember { mutableStateOf<WorkspaceFile?>(null) }
    var editorText by remember { mutableStateOf("") }
    var newProjectOpen by remember { mutableStateOf(false) }
    var newFileOpen by remember { mutableStateOf(false) }
    var nameInput by remember { mutableStateOf("") }
    var savedMessage by remember { mutableStateOf("") }
    var activityMessage by remember { mutableStateOf("") }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        storageGranted = StorageAccess.hasAccess(context)
        if (storageGranted) projects = workspace.projects()
    }

    fun requestStorage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            context.startActivity(StorageAccess.settingsIntent(context))
        } else {
            val permission = if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            } else {
                Manifest.permission.READ_EXTERNAL_STORAGE
            }
            permissionLauncher.launch(permission)
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val granted = StorageAccess.hasAccess(context)
                if (granted != storageGranted) {
                    storageGranted = granted
                    projects = workspace.projects()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

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
                storageGranted = storageGranted,
                onRequestStorage = ::requestStorage,
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
            Screen.LINUX -> LinuxScreen(runtime)
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
private fun ProjectsScreen(
    projects: List<WorkspaceProject>,
    storageGranted: Boolean,
    onRequestStorage: () -> Unit,
    onOpen: (WorkspaceProject) -> Unit,
    onNew: () -> Unit,
) {
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Projetos", color = Color.White, fontSize = 24.sp)
        Text("Workspace local persistente, separado do runtime Linux.", color = Color(0xFFB7B3C6))
        if (!storageGranted) {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF2A1E3A))) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Acesso às pastas necessário", color = Color(0xFFFFD66B), fontSize = 15.sp)
                    Text(
                        "Permita o acesso a todos os arquivos para salvar os projetos em /storage/emulated/0/Phantom-Code-V2 e reutilizar arquivos existentes.",
                        color = Color(0xFFB7B3C6),
                        fontSize = 12.sp,
                    )
                    Button(onClick = onRequestStorage) { Text("Permitir acesso") }
                }
            }
        }
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
private fun LinuxScreen(runtime: LinuxRuntimeController) {
    var command by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()
    val installScroll = rememberScrollState()
    val clipboard = LocalClipboardManager.current
    val focusRequester = remember { FocusRequester() }
    val keyboard = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current
    val installing = runtime.state is com.phantomcode.v2.vm.LinuxUiState.Installing

    LaunchedEffect(runtime.output) {
        if (!installing) scrollState.scrollTo(Int.MAX_VALUE)
    }
    LaunchedEffect(runtime.installLog) {
        if (installing) installScroll.scrollTo(Int.MAX_VALUE)
    }

    val canSend = runtime.state is com.phantomcode.v2.vm.LinuxUiState.Running ||
        runtime.state is com.phantomcode.v2.vm.LinuxUiState.Starting

    Column(Modifier.fillMaxSize().background(Color(0xFF0B0B12)).imePadding()) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Terminal, null, tint = Color(0xFFB794F4))
            Spacer(Modifier.width(8.dp))
            Text("Linux", color = Color.White, fontSize = 20.sp, modifier = Modifier.weight(1f))
            Text("Estado: ${runtime.state.label()}", color = Color(0xFFB7B3C6), fontSize = 12.sp)
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = runtime::install, enabled = runtime.state is com.phantomcode.v2.vm.LinuxUiState.NoDistro) { Text("Instalar") }
            Button(onClick = runtime::start, enabled = runtime.state is com.phantomcode.v2.vm.LinuxUiState.Ready) { Text("Iniciar") }
            OutlinedButton(onClick = runtime::stop, enabled = runtime.state is com.phantomcode.v2.vm.LinuxUiState.Running) { Text("Parar") }
        }
        runtime.progress?.let { value -> Text("Download: ${(value * 100).toInt()}%", color = Color(0xFFB794F4), modifier = Modifier.padding(horizontal = 16.dp)) }
        runtime.error?.let { Text(it, color = Color(0xFFF56565), fontSize = 12.sp, modifier = Modifier.padding(horizontal = 16.dp)) }

        Box(
            Modifier.weight(1f).fillMaxWidth()
                .clickable { focusRequester.requestFocus(); keyboard?.show() }
        ) {
            if (installing) {
                Box(
                    Modifier.fillMaxSize().verticalScroll(installScroll).padding(12.dp),
                ) {
                    Text(
                        runtime.installLog.ifBlank { "Baixando distro…" },
                        color = Color(0xFF68D391),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                    )
                }
            } else {
                SelectionContainer {
                    Box(Modifier.fillMaxSize().verticalScroll(scrollState).padding(12.dp)) {
                        Text(
                            runtime.output.ifBlank { "O console Linux aparecerá aqui.\n\nUse a barra abaixo para enviar comandos." },
                            color = Color(0xFFE2E8F0),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                        )
                    }
                }
            }
        }
        Row(
            Modifier.fillMaxWidth().background(Color(0xFF171522)).padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { runCatching { clipboard.setText(AnnotatedString(runtime.output)) } }) {
                Icon(Icons.Default.ContentCopy, "Copiar saída", tint = Color(0xFFB794F4))
            }
            IconButton(onClick = { runCatching { clipboard.getText()?.text?.let { command = it } } }) {
                Icon(Icons.Default.ContentPaste, "Colar", tint = Color(0xFFB794F4))
            }
            IconButton(onClick = { runtime.clearOutput() }) {
                Icon(Icons.Default.Delete, "Limpar", tint = Color(0xFFB794F4))
            }
            BasicTextField(
                value = command,
                onValueChange = { command = it },
                modifier = Modifier.weight(1f).background(Color(0xFF0D0D14)).padding(horizontal = 10.dp, vertical = 8.dp)
                    .focusRequester(focusRequester),
                textStyle = TextStyle(color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 14.sp),
                cursorBrush = SolidColor(Color(0xFFB794F4)),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (canSend && command.isNotBlank()) {
                        runtime.sendInput(command)
                        command = ""
                    }
                }),
            )
            Spacer(Modifier.width(6.dp))
            Button(
                onClick = { runtime.sendInput(command); command = "" },
                enabled = canSend && command.isNotBlank(),
            ) { Text("Enviar") }
        }
    }
}

private fun com.phantomcode.v2.vm.LinuxUiState.label(): String = when (this) {
    com.phantomcode.v2.vm.LinuxUiState.NoDistro -> "Nenhuma distro"
    com.phantomcode.v2.vm.LinuxUiState.Installing -> "Instalando"
    com.phantomcode.v2.vm.LinuxUiState.Ready -> "Pronto"
    com.phantomcode.v2.vm.LinuxUiState.Starting -> "Iniciando"
    com.phantomcode.v2.vm.LinuxUiState.Running -> "Linux ativo"
    com.phantomcode.v2.vm.LinuxUiState.Error -> "Erro"
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
