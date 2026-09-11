package com.lumen.app.core.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

object PermissionUtils {
    fun hasCamera(context: Context) = granted(context, Manifest.permission.CAMERA)
    fun hasContacts(context: Context) = granted(context, Manifest.permission.WRITE_CONTACTS)
    fun hasCalendar(context: Context) = granted(context, Manifest.permission.WRITE_CALENDAR)
    fun hasNotifications(context: Context) =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) granted(context, Manifest.permission.POST_NOTIFICATIONS) else true

    private fun granted(context: Context, permission: String) =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    fun requiredRuntimePermissions(): Array<String> {
        val perms = mutableListOf(Manifest.permission.CAMERA)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) perms += Manifest.permission.POST_NOTIFICATIONS
        return perms.toTypedArray()
    }
}
