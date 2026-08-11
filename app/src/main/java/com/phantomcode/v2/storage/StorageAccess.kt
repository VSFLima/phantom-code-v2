package com.phantomcode.v2.storage

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.content.ContextCompat
import java.io.File

object StorageAccess {

    fun hasAccess(context: Context): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED
        }

    fun settingsIntent(context: Context): Intent =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Intent(
                Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                Uri.parse("package:${context.packageName}"),
            ).takeIf { it.resolveActivity(context.packageManager) != null }
                ?: Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}"))
        }

    fun externalRoot(context: Context): File? {
        if (!hasAccess(context)) return null
        val base = Environment.getExternalStorageDirectory() ?: return null
        return File(base, "Phantom-Code-V2/workspace")
    }
}
