package com.khata

import com.khata.data.updates.GitHubReleaseChecker
import org.junit.Assert.assertEquals
import org.junit.Test

class GitHubReleaseCheckerTest {
    @Test fun comparesNumericVersionComponentsRatherThanText() {
        assertEquals(1, GitHubReleaseChecker.compareVersions("2.10.0", "2.9.9"))
        assertEquals(-1, GitHubReleaseChecker.compareVersions("2.0.9", "2.1.0"))
        assertEquals(0, GitHubReleaseChecker.compareVersions("v2.1.0".removePrefix("v"), "2.1.0"))
    }
}
