package com.phantomcode.v2.workspace

import android.content.Context
import java.io.File

data class WorkspaceProject(val name: String, val directory: File)
data class WorkspaceFile(val name: String, val file: File, val isDirectory: Boolean)

class WorkspaceController(context: Context, rootDir: File? = null) {
    private val root = (rootDir ?: File(context.filesDir, "workspace")).apply { mkdirs() }

    fun projects(): List<WorkspaceProject> = root.listFiles()
        ?.filter { it.isDirectory }
        ?.sortedBy { it.name.lowercase() }
        ?.map { WorkspaceProject(it.name, it) }
        ?: emptyList()

    fun createProject(name: String): WorkspaceProject? {
        val clean = cleanName(name) ?: return null
        val directory = File(root, clean)
        if (!directory.mkdirs()) return null
        return WorkspaceProject(clean, directory)
    }

    fun files(project: WorkspaceProject, relativePath: String = ""): List<WorkspaceFile> {
        val directory = resolve(project, relativePath) ?: return emptyList()
        return directory.listFiles()
            ?.sortedWith(compareBy<File> { !it.isDirectory }.thenBy { it.name.lowercase() })
            ?.map { WorkspaceFile(it.name, it, it.isDirectory) }
            ?: emptyList()
    }

    fun createFile(project: WorkspaceProject, relativePath: String): Boolean {
        val target = resolve(project, relativePath) ?: return false
        if (target.exists()) return false
        target.parentFile?.mkdirs()
        return target.createNewFile()
    }

    fun read(file: File): String = runCatching { file.readText() }.getOrDefault("")

    fun write(file: File, content: String): Boolean = runCatching {
        file.parentFile?.mkdirs()
        file.writeText(content)
    }.isSuccess

    private fun resolve(project: WorkspaceProject, relativePath: String): File? {
        val candidate = File(project.directory, relativePath.trimStart('/')).canonicalFile
        return candidate.takeIf {
            it.path == project.directory.canonicalPath || it.path.startsWith(project.directory.canonicalPath + File.separator)
        }
    }

    private fun cleanName(value: String): String? = value.trim()
        .replace(Regex("[^A-Za-z0-9._-]"), "-")
        .trim('-')
        .takeIf { it.isNotBlank() && it != "." && it != ".." }
}
