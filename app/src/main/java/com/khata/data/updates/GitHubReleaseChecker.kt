package com.khata.data.updates

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

private const val LATEST_RELEASE_API = "https://api.github.com/repos/vijayarkananduri/khata/releases/latest"

data class GitHubUpdateResult(
    val currentVersion: String,
    val latestVersion: String,
    val releaseUrl: String,
    val updateAvailable: Boolean
)

/** Called only after the user taps Check for updates; no background network access is used. */
object GitHubReleaseChecker {
    suspend fun check(context: Context): GitHubUpdateResult = withContext(Dispatchers.IO) {
        val current = context.packageManager.getPackageInfo(context.packageName, 0).versionName.orEmpty()
        val connection = (URL(LATEST_RELEASE_API).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 8_000
            readTimeout = 8_000
            setRequestProperty("Accept", "application/vnd.github+json")
            setRequestProperty("User-Agent", "KhataAndroidApp")
            setRequestProperty("X-GitHub-Api-Version", "2022-11-28")
        }
        try {
            val status = connection.responseCode
            if (status != HttpURLConnection.HTTP_OK) {
                val reason = when (status) {
                    403, 429 -> "GitHub is temporarily rate-limiting update checks. Try again later."
                    else -> "GitHub returned HTTP $status. Check your connection and try again."
                }
                throw IllegalStateException(reason)
            }
            val payload = connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            val release = JSONObject(payload)
            val latest = release.optString("tag_name").removePrefix("v").trim()
            val releaseUrl = release.optString("html_url").trim()
            require(latest.isNotBlank() && releaseUrl.startsWith("https://github.com/")) {
                "GitHub returned incomplete release information."
            }
            GitHubUpdateResult(
                currentVersion = current,
                latestVersion = latest,
                releaseUrl = releaseUrl,
                updateAvailable = compareVersions(latest, current) > 0
            )
        } finally {
            connection.disconnect()
        }
    }

    internal fun compareVersions(left: String, right: String): Int {
        val leftParts = left.substringBefore('-').split('.').map { it.toIntOrNull() ?: 0 }
        val rightParts = right.substringBefore('-').split('.').map { it.toIntOrNull() ?: 0 }
        for (index in 0 until maxOf(leftParts.size, rightParts.size)) {
            val comparison = (leftParts.getOrElse(index) { 0 }).compareTo(rightParts.getOrElse(index) { 0 })
            if (comparison != 0) return comparison
        }
        return 0
    }
}
