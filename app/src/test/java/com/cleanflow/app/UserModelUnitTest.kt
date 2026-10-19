package com.cleanflow.app

import com.cleanflow.app.models.User
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UserModelUnitTest {

    @Test
    fun userCreation_defaultValues_areCorrect() {
        val user = User(
            uid = "user_101",
            fullName = "Sipho Ndlovu",
            email = "sipho.ndlovu@cleanflow.org"
        )

        assertEquals("user_101", user.uid)
        assertEquals("Sipho Ndlovu", user.fullName)
        assertEquals("sipho.ndlovu@cleanflow.org", user.email)
        assertEquals("community", user.role) // Default role
        assertTrue("Creation timestamp should be non-zero", user.createdAt > 0)
    }

    @Test
    fun userCreation_customRole_assignsCorrectly() {
        val technician = User(
            uid = "tech_101",
            fullName = "Kagiso Molefe",
            email = "kagiso.molefe@cleanflow.org",
            role = "technician"
        )

        val inspector = User(
            uid = "insp_101",
            fullName = "Siyabonga Nkosi",
            email = "siyabonga.nkosi@cleanflow.org",
            role = "inspector"
        )

        assertEquals("technician", technician.role)
        assertEquals("inspector", inspector.role)
    }

    @Test
    fun userCopy_updatesFieldsCorrectly() {
        val originalUser = User(
            uid = "user_101",
            fullName = "Sipho Ndlovu",
            email = "sipho.ndlovu@cleanflow.org",
            phone = "+27 82 000 0000"
        )

        val updatedUser = originalUser.copy(
            fullName = "Sipho M. Ndlovu",
            phone = "+27 82 123 4567"
        )

        assertEquals("user_101", updatedUser.uid)
        assertEquals("Sipho M. Ndlovu", updatedUser.fullName)
        assertEquals("sipho.ndlovu@cleanflow.org", updatedUser.email)
        assertEquals("+27 82 123 4567", updatedUser.phone)
    }
}