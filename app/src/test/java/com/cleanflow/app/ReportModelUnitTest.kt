package com.cleanflow.app

import com.cleanflow.app.models.Report
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReportModelUnitTest {

    private val sampleReports = listOf(
        Report(
            reportId = "rep_101",
            userId = "user_101",
            issueType = "Pipe Leak",
            severity = "High",
            communityName = "Mokopane Section 4",
            status = "Pending"
        ),
        Report(
            reportId = "rep_102",
            userId = "user_101",
            issueType = "Borehole",
            severity = "Medium",
            communityName = "Mokopane Central",
            status = "Assigned"
        ),
        Report(
            reportId = "rep_103",
            userId = "user_101",
            issueType = "Water Tank",
            severity = "Low",
            communityName = "Mokopane Section 2",
            status = "Resolved"
        ),
        Report(
            reportId = "rep_104",
            userId = "user_101",
            issueType = "Sanitation",
            severity = "High",
            communityName = "Mokopane West",
            status = "Pending"
        )
    )

    @Test
    fun reportCreation_defaultStatus_isPending() {
        val newReport = Report(
            reportId = "rep_999",
            userId = "user_101",
            issueType = "Pipe Leak"
        )

        assertEquals("Pending", newReport.status)
        assertTrue("Submission timestamp should be non-zero", newReport.submittedAt > 0)
    }

    @Test
    fun filterReports_byPendingStatus_returnsOnlyPending() {
        val pendingReports = sampleReports.filter { it.status == "Pending" }

        assertEquals(2, pendingReports.size)
        assertTrue(pendingReports.all { it.status == "Pending" })
    }

    @Test
    fun filterReports_byAssignedStatus_returnsOnlyAssigned() {
        val assignedReports = sampleReports.filter { it.status == "Assigned" }

        assertEquals(1, assignedReports.size)
        assertEquals("rep_102", assignedReports.first().reportId)
    }

    @Test
    fun filterReports_byResolvedStatus_returnsOnlyResolved() {
        val resolvedReports = sampleReports.filter { it.status == "Resolved" }

        assertEquals(1, resolvedReports.size)
        assertEquals("rep_103", resolvedReports.first().reportId)
    }

    @Test
    fun filterReports_byHighSeverity_returnsOnlyHighSeverity() {
        val highPriorityReports = sampleReports.filter { it.severity == "High" }

        assertEquals(2, highPriorityReports.size)
        assertTrue(highPriorityReports.all { it.severity == "High" })
    }

    @Test
    fun gpsCoordinates_validRange_checksCorrectly() {
        val validReport = Report(
            reportId = "rep_101",
            latitude = -24.1833,
            longitude = 29.0167
        )

        val isValidGps = validReport.latitude in -90.0..90.0 && validReport.longitude in -180.0..180.0
        assertTrue("GPS coordinates should be within valid global bounds", isValidGps)
    }
}