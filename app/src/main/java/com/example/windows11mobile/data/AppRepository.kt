package com.example.windows11mobile.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.LauncherApps
import android.content.pm.ShortcutInfo
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface AppRepository {
    suspend fun getInstalledApps(): List<AppInfo>
    fun getShortcuts(packageName: String): List<ShortcutInfo>
}

class RealAppRepository(private val context: Context) : AppRepository {
    private val packageManager: PackageManager = context.packageManager
    private val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps

    override suspend fun getInstalledApps(): List<AppInfo> = withContext(Dispatchers.IO) {
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        
        packageManager.queryIntentActivities(intent, 0).map { resolveInfo ->
            AppInfo(
                name = resolveInfo.loadLabel(packageManager).toString(),
                packageName = resolveInfo.activityInfo.packageName,
                icon = resolveInfo.loadIcon(packageManager)
            )
        }.sortedBy { it.name.lowercase() }
    }

    override fun getShortcuts(packageName: String): List<ShortcutInfo> {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) return emptyList()
        
        val query = LauncherApps.ShortcutQuery().apply {
            setQueryFlags(LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC or 
                         LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST or 
                         LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED)
            setPackage(packageName)
        }
        return try {
            launcherApps.getShortcuts(query, android.os.Process.myUserHandle()) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
