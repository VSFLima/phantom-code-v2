package com.phantomcode.v2.workspace

import android.content.Context
import com.phantomcode.v2.storage.StorageAccess
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

enum class WorkspaceChangeSource { EDITOR, LINUX, AI, GIT, RESTORE }
enum class WorkspaceOperation { CREATE, MODIFY, RENAME, DELETE }

data class WorkspaceChangeEvent(
    val project: String,
    val path: String,
    val operation: WorkspaceOperation,
    val source: WorkspaceChangeSource,
    val actorId: String,
    val revision: Long,
)

/** Fonte única de alterações do workspace para editor, Linux, Git e IA. */
class WorkspaceService(context: Context) {
    private val storage = WorkspaceController(context, StorageAccess.externalRoot(context))
    private val _events = MutableSharedFlow<WorkspaceChangeEvent>(extraBufferCapacity = 32)
    private var revision = 0L

    val events: SharedFlow<WorkspaceChangeEvent> = _events.asSharedFlow()

    fun projects(): List<WorkspaceProject> = storage.projects()
    fun files(project: WorkspaceProject): List<WorkspaceFile> = storage.files(project)
    fun read(file: java.io.File): String = storage.read(file)
    fun createProject(name: String): WorkspaceProject? = storage.createProject(name)?.also {
        emit(it.name, "", WorkspaceOperation.CREATE, WorkspaceChangeSource.EDITOR, "user")
    }

    fun createFile(project: WorkspaceProject, name: String): Boolean =
        storage.createFile(project, name).also { ok ->
            if (ok) emit(project.name, name, WorkspaceOperation.CREATE, WorkspaceChangeSource.EDITOR, "user")
        }

    fun write(project: WorkspaceProject, file: WorkspaceFile, content: String, source: WorkspaceChangeSource = WorkspaceChangeSource.EDITOR, actorId: String = "user"): Boolean =
        storage.write(file.file, content).also { ok ->
            if (ok) emit(project.name, file.name, WorkspaceOperation.MODIFY, source, actorId)
        }

    private fun emit(project: String, path: String, operation: WorkspaceOperation, source: WorkspaceChangeSource, actorId: String) {
        revision += 1
        _events.tryEmit(WorkspaceChangeEvent(project, path, operation, source, actorId, revision))
    }
}
