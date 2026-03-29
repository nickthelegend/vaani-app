package com.vaani.app.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppResolver @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun getPackageNameForApp(appName: String): String? {
        val packages = context.packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        for (info in packages) {
            val label = info.loadLabel(context.packageManager).toString()
            if (label.equals(appName, ignoreCase = true)) {
                return info.packageName
            }
        }
        return null
    }

    fun isAppInstalled(packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: Exception) {
            false
        }
    }
}
