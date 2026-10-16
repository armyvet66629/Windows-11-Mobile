package com.example.windows11mobile.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.windows11mobile.R
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.util.concurrent.ConcurrentHashMap

data class IconPackInfo(
    val packageName: String,
    val label: String,
    val icon: Drawable?
)

object IconPackManager {

    private val appFilterCache = ConcurrentHashMap<String, Map<String, String>>()

    fun getInstalledIconPacks(context: Context): List<IconPackInfo> {
        val pm = context.packageManager
        val iconPacks = LinkedHashMap<String, IconPackInfo>()

        // Default option
        iconPacks["default"] = IconPackInfo("default", "System Default", null)

        // Built-in Templarian WindowsIcons
        val winIconDrawable = try { ContextCompat.getDrawable(context, R.drawable.ic_win_phone) } catch (_: Exception) { null }
        iconPacks["windowsicons"] = IconPackInfo("windowsicons", "WindowsIcons (Templarian)", winIconDrawable)

        val intents = listOf(
            Intent("org.adw.launcher.THEMES"),
            Intent("com.novalauncher.THEME"),
            Intent("com.dlto.atom.launcher.THEME"),
            Intent("com.gau.go.launcherex.theme"),
            Intent("com.fede.launcher.THEME_ICONPACK"),
            Intent(Intent.ACTION_MAIN).addCategory("com.anddoes.launcher.THEME")
        )

        for (intent in intents) {
            val list = try { pm.queryIntentActivities(intent, PackageManager.GET_META_DATA) } catch (_: Exception) { emptyList() }
            for (info in list) {
                val pkgName = info.activityInfo.packageName
                if (pkgName != context.packageName && !iconPacks.containsKey(pkgName)) {
                    val label = info.loadLabel(pm).toString()
                    val icon = try { info.loadIcon(pm) } catch (_: Exception) { null }
                    iconPacks[pkgName] = IconPackInfo(pkgName, label, icon)
                }
            }
        }
        return iconPacks.values.toList()
    }

    private fun loadAppFilter(context: Context, iconPackPackage: String): Map<String, String> {
        appFilterCache[iconPackPackage]?.let { return it }
        val map = mutableMapOf<String, String>()
        try {
            val packContext = context.createPackageContext(iconPackPackage, Context.CONTEXT_IGNORE_SECURITY)
            val assetManager = packContext.assets
            
            val inputStream = try {
                assetManager.open("appfilter.xml")
            } catch (_: Exception) {
                try {
                    val res = context.packageManager.getResourcesForApplication(iconPackPackage)
                    val resId = res.getIdentifier("appfilter", "xml", iconPackPackage)
                    if (resId != 0) res.openRawResource(resId) else null
                } catch (_: Exception) { null }
            }

            if (inputStream != null) {
                val factory = XmlPullParserFactory.newInstance()
                val parser = factory.newPullParser()
                parser.setInput(inputStream, "UTF-8")
                var eventType = parser.eventType
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG && parser.name.equals("item", ignoreCase = true)) {
                        val component = parser.getAttributeValue(null, "component")
                        val drawable = parser.getAttributeValue(null, "drawable")
                        val pkg = parser.getAttributeValue(null, "package")

                        if (!drawable.isNullOrBlank()) {
                            if (!component.isNullOrBlank()) {
                                val pkgName = component.substringAfter("{").substringBefore("/").substringBefore("}")
                                if (pkgName.isNotBlank()) {
                                    map[pkgName.lowercase()] = drawable
                                }
                                map[component.lowercase()] = drawable
                            }
                            if (!pkg.isNullOrBlank()) {
                                map[pkg.lowercase()] = drawable
                            }
                        }
                    }
                    eventType = parser.next()
                }
                inputStream.close()
            }
        } catch (e: Exception) {
            Log.e("IconPackManager", "Failed to parse appfilter.xml for $iconPackPackage", e)
        }
        appFilterCache[iconPackPackage] = map
        return map
    }

    fun loadAppIcon(context: Context, appPackageName: String?, iconPackPackage: String?, label: String? = null): Drawable? {
        if (appPackageName.isNullOrBlank() && label.isNullOrBlank()) return null
        val pm = context.packageManager

        if (!iconPackPackage.isNullOrBlank() && iconPackPackage != "default") {
            if (iconPackPackage == "windowsicons") {
                val winResId = IconRepository.getIconForApp(context, appPackageName, label)
                if (winResId != null && winResId != 0) {
                    val drawable = try { ContextCompat.getDrawable(context, winResId) } catch (_: Exception) { null }
                    if (drawable != null) return drawable
                }
            } else {
                try {
                    val res = pm.getResourcesForApplication(iconPackPackage)
                    val appFilter = loadAppFilter(context, iconPackPackage)
                    
                    val pkgLower = appPackageName?.lowercase() ?: ""
                    val drawableName = appFilter[pkgLower]

                    if (!drawableName.isNullOrBlank()) {
                        val resId = res.getIdentifier(drawableName, "drawable", iconPackPackage)
                        if (resId != 0) {
                            val iconDrawable = try { res.getDrawable(resId, null) } catch (_: Exception) { null }
                            if (iconDrawable != null) return iconDrawable
                        }
                    }

                    // Fallback heuristics
                    val sanitizedPkg = pkgLower.replace('.', '_')
                    val shortName = pkgLower.substringAfterLast('.')
                    val possibleNames = listOf(
                        sanitizedPkg,
                        "ic_$sanitizedPkg",
                        "icon_$sanitizedPkg",
                        shortName,
                        "ic_$shortName"
                    )

                    for (name in possibleNames) {
                        val resId = res.getIdentifier(name, "drawable", iconPackPackage)
                        if (resId != 0) {
                            val iconDrawable = try { res.getDrawable(resId, null) } catch (_: Exception) { null }
                            if (iconDrawable != null) return iconDrawable
                        }
                    }
                } catch (e: Exception) {
                    Log.e("IconPackManager", "Error loading icon from pack $iconPackPackage", e)
                }
            }
        }

        // Fallback to default system app icon
        if (!appPackageName.isNullOrBlank()) {
            return try {
                pm.getApplicationIcon(appPackageName)
            } catch (_: Exception) {
                null
            }
        }
        return null
    }
}
