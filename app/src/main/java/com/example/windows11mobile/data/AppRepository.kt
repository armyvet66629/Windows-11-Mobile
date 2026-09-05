package com.example.windows11mobile.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.LauncherApps
import android.content.pm.ShortcutInfo
import android.os.Build
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface AppRepository {
    suspend fun getInstalledApps(): List<AppInfo>
    fun observeApps(): Flow<List<AppInfo>>
    fun getShortcuts(packageName: String): List<ShortcutInfo>
}

class RealAppRepository(private val context: Context) : AppRepository {
    private val packageManager: PackageManager = context.packageManager
    private val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps

    override suspend fun getInstalledApps(): List<AppInfo> = withContext(Dispatchers.IO) {
        queryApps()
    }

    private fun queryApps(): List<AppInfo> {
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        
        return packageManager.queryIntentActivities(intent, 0).map { resolveInfo ->
            AppInfo(
                name = resolveInfo.loadLabel(packageManager).toString(),
                packageName = resolveInfo.activityInfo.packageName,
                icon = resolveInfo.loadIcon(packageManager)
            )
        }.sortedBy { it.name.lowercase() }
    }

    override fun observeApps(): Flow<List<AppInfo>> = callbackFlow {
        val callback = object : LauncherApps.Callback() {
            override fun onPackageRemoved(packageName: String?, user: android.os.UserHandle?) {
                trySend(queryApps())
            }

            override fun onPackageAdded(packageName: String?, user: android.os.UserHandle?) {
                trySend(queryApps())
            }

            override fun onPackageChanged(packageName: String?, user: android.os.UserHandle?) {
                trySend(queryApps())
            }

            override fun onPackagesAvailable(packageNames: Array<out String>?, user: android.os.UserHandle?, replacing: Boolean) {
                trySend(queryApps())
            }

            override fun onPackagesUnavailable(packageNames: Array<out String>?, user: android.os.UserHandle?, replacing: Boolean) {
                trySend(queryApps())
            }
        }

        launcherApps.registerCallback(callback, Handler(Looper.getMainLooper()))
        
        // Initial emit
        trySend(queryApps())

        awaitClose {
            launcherApps.unregisterCallback(callback)
        }
    }.onStart { emit(queryApps()) }

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
