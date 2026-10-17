package com.cleanflow.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.cleanflow.app.models.Report
import com.cleanflow.app.models.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [User::class, Report::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun reportDao(): ReportDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cleanflow_database"
                )
                .addCallback(DatabaseCallback(context))
                .build()

                INSTANCE = instance

                // Trigger asynchronous seed check on access
                CoroutineScope(Dispatchers.IO).launch {
                    populateSampleDataIfEmpty(instance)
                }

                instance
            }
        }

        private class DatabaseCallback(private val context: Context) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateSampleDataIfEmpty(database)
                    }
                }
            }
        }

        @JvmStatic
        suspend fun populateSampleDataIfEmpty(db: AppDatabase) {
            // Check if rep_101 exists to ensure sample data is populated even on existing DB files
            val sampleReport = db.reportDao().getReportById("rep_101")
            if (sampleReport == null) {
                // Seed 11 Users
                val sampleUsers = listOf(
                    User(
                        uid = "user_demo_101",
                        fullName = "Sipho Ndlovu",
                        email = "sipho.ndlovu@cleanflow.org",
                        phone = "+27 82 123 4567",
                        role = "community",
                        createdAt = System.currentTimeMillis() - (30L * 86400 * 1000)
                    ),
                    User(
                        uid = "user_demo_102",
                        fullName = "Thandiwe Khumalo",
                        email = "thandiwe.k@cleanflow.org",
                        phone = "+27 71 234 5678",
                        role = "community",
                        createdAt = System.currentTimeMillis() - (45L * 86400 * 1000)
                    ),
                    User(
                        uid = "user_demo_103",
                        fullName = "Kagiso Molefe",
                        email = "kagiso.molefe@cleanflow.org",
                        phone = "+27 83 345 6789",
                        role = "technician",
                        createdAt = System.currentTimeMillis() - (60L * 86400 * 1000)
                    ),
                    User(
                        uid = "user_demo_104",
                        fullName = "Nompumelelo Dlamini",
                        email = "nompumelelo.d@cleanflow.org",
                        phone = "+27 72 456 7890",
                        role = "community",
                        createdAt = System.currentTimeMillis() - (15L * 86400 * 1000)
                    ),
                    User(
                        uid = "user_demo_105",
                        fullName = "Bongani Sithole",
                        email = "bongani.s@cleanflow.org",
                        phone = "+27 84 567 8901",
                        role = "technician",
                        createdAt = System.currentTimeMillis() - (90L * 86400 * 1000)
                    ),
                    User(
                        uid = "user_demo_106",
                        fullName = "Zanele Mthembu",
                        email = "zanele.m@cleanflow.org",
                        phone = "+27 73 678 9012",
                        role = "community",
                        createdAt = System.currentTimeMillis() - (20L * 86400 * 1000)
                    ),
                    User(
                        uid = "user_demo_107",
                        fullName = "Lethabo Mabena",
                        email = "lethabo.mabena@cleanflow.org",
                        phone = "+27 81 789 0123",
                        role = "community",
                        createdAt = System.currentTimeMillis() - (10L * 86400 * 1000)
                    ),
                    User(
                        uid = "user_demo_108",
                        fullName = "Siyabonga Nkosi",
                        email = "siyabonga.nkosi@cleanflow.org",
                        phone = "+27 74 890 1234",
                        role = "inspector",
                        createdAt = System.currentTimeMillis() - (120L * 86400 * 1000)
                    ),
                    User(
                        uid = "user_demo_109",
                        fullName = "Precious Naidoo",
                        email = "precious.naidoo@cleanflow.org",
                        phone = "+27 82 901 2345",
                        role = "community",
                        createdAt = System.currentTimeMillis() - (5L * 86400 * 1000)
                    ),
                    User(
                        uid = "user_demo_110",
                        fullName = "David Van Der Merwe",
                        email = "david.vdm@cleanflow.org",
                        phone = "+27 76 012 3456",
                        role = "community",
                        createdAt = System.currentTimeMillis() - (80L * 86400 * 1000)
                    ),
                    User(
                        uid = "user_demo_111",
                        fullName = "Ayanda Tshabalala",
                        email = "ayanda.t@cleanflow.org",
                        phone = "+27 83 123 9876",
                        role = "community",
                        createdAt = System.currentTimeMillis() - (25L * 86400 * 1000)
                    )
                )

                sampleUsers.forEach { db.userDao().upsertUser(it) }

                val now = System.currentTimeMillis()
                // Seed 13 Reports
                val sampleReports = listOf(
                    Report(
                        reportId = "rep_101",
                        userId = "user_demo_101",
                        issueType = "Pipe Leak",
                        description = "Main distribution line burst near Section 4 water tank. High pressure water leakage flooding nearby road.",
                        severity = "High",
                        communityName = "Mokopane Section 4",
                        latitude = -24.1833,
                        longitude = 29.0167,
                        imageUrl = "https://images.unsplash.com/photo-1585829365295-ab7cd400c167?w=600&auto=format&fit=crop",
                        status = "Pending",
                        submittedAt = now - (2 * 3600 * 1000)
                    ),
                    Report(
                        reportId = "rep_102",
                        userId = "user_demo_101",
                        issueType = "Borehole",
                        description = "Borehole pump #3 motor making severe grinding noise and yielding brown sediment in water.",
                        severity = "Medium",
                        communityName = "Mokopane Central",
                        latitude = -24.1850,
                        longitude = 29.0200,
                        imageUrl = "https://images.unsplash.com/photo-1541252260730-0412e8e2108e?w=600&auto=format&fit=crop",
                        status = "Assigned",
                        submittedAt = now - (18 * 3600 * 1000)
                    ),
                    Report(
                        reportId = "rep_103",
                        userId = "user_demo_101",
                        issueType = "Water Tank",
                        description = "Community elevated water tank outlet valve replaced and pressure tested by technical team.",
                        severity = "Low",
                        communityName = "Mokopane Section 2",
                        latitude = -24.1800,
                        longitude = 29.0120,
                        imageUrl = "https://images.unsplash.com/photo-1509316975850-ff9c5deb0cd9?w=600&auto=format&fit=crop",
                        status = "Resolved",
                        submittedAt = now - (3 * 86400 * 1000)
                    ),
                    Report(
                        reportId = "rep_104",
                        userId = "user_demo_101",
                        issueType = "Sanitation",
                        description = "Public ablution facility sewer line blockage near primary school entrance.",
                        severity = "High",
                        communityName = "Mokopane West",
                        latitude = -24.1870,
                        longitude = 29.0150,
                        status = "Pending",
                        submittedAt = now - (5 * 3600 * 1000)
                    ),
                    Report(
                        reportId = "rep_105",
                        userId = "user_demo_101",
                        issueType = "Pump Station",
                        description = "Booster pump 2 pressure regulator malfunction resolved by municipal team.",
                        severity = "Medium",
                        communityName = "Mokopane North",
                        latitude = -24.1790,
                        longitude = 29.0250,
                        status = "Resolved",
                        submittedAt = now - (6 * 86400 * 1000)
                    ),
                    Report(
                        reportId = "rep_106",
                        userId = "user_demo_101",
                        issueType = "Pipe Leak",
                        description = "Small underground drip detected near communal tapstand 12.",
                        severity = "Low",
                        communityName = "Mokopane East",
                        latitude = -24.1840,
                        longitude = 29.0280,
                        status = "Assigned",
                        submittedAt = now - (1 * 86400 * 1000)
                    ),
                    Report(
                        reportId = "rep_107",
                        userId = "user_demo_102",
                        issueType = "Borehole",
                        description = "Solar power inverter trip causing intermittent borehole shutdown.",
                        severity = "High",
                        communityName = "Lebowakgomo Zone 1",
                        latitude = -24.2000,
                        longitude = 29.5000,
                        status = "Pending",
                        submittedAt = now - (12 * 3600 * 1000)
                    ),
                    Report(
                        reportId = "rep_108",
                        userId = "user_demo_102",
                        issueType = "Water Tank",
                        description = "5000L Jojo tank crack leaking continuously near community clinic.",
                        severity = "Medium",
                        communityName = "Lebowakgomo Zone 3",
                        latitude = -24.2050,
                        longitude = 29.5100,
                        status = "Assigned",
                        submittedAt = now - (24 * 3600 * 1000)
                    ),
                    Report(
                        reportId = "rep_109",
                        userId = "user_demo_104",
                        issueType = "Sanitation",
                        description = "Stormwater drain clog causing localized overflow.",
                        severity = "Low",
                        communityName = "Giyani Section A",
                        latitude = -23.3000,
                        longitude = 30.7167,
                        status = "Resolved",
                        submittedAt = now - (4 * 86400 * 1000)
                    ),
                    Report(
                        reportId = "rep_110",
                        userId = "user_demo_106",
                        issueType = "Pump Station",
                        description = "Electrical surge tripped main intake pump breaker.",
                        severity = "High",
                        communityName = "Tzaneen Ext 5",
                        latitude = -23.8333,
                        longitude = 30.1667,
                        status = "Pending",
                        submittedAt = now - (1 * 3600 * 1000)
                    ),
                    Report(
                        reportId = "rep_111",
                        userId = "user_demo_107",
                        issueType = "Pipe Leak",
                        description = "Corroded joint connector leaking water into drainage trench.",
                        severity = "Medium",
                        communityName = "Burgersfort Village",
                        latitude = -24.6667,
                        longitude = 30.3333,
                        status = "Assigned",
                        submittedAt = now - (36 * 3600 * 1000)
                    ),
                    Report(
                        reportId = "rep_112",
                        userId = "user_demo_109",
                        issueType = "Borehole",
                        description = "Hand pump handle pin broken. Replaced with heavy duty pin.",
                        severity = "Low",
                        communityName = "Thohoyandou Unit C",
                        latitude = -22.9500,
                        longitude = 30.4833,
                        status = "Resolved",
                        submittedAt = now - (7 * 86400 * 1000)
                    ),
                    Report(
                        reportId = "rep_113",
                        userId = "user_demo_101",
                        issueType = "Water Tank",
                        description = "Chlorination dosing system refilled and water safety inspection completed.",
                        severity = "Low",
                        communityName = "Mokopane Central",
                        latitude = -24.1820,
                        longitude = 29.0190,
                        status = "Resolved",
                        submittedAt = now - (10 * 86400 * 1000)
                    )
                )

                sampleReports.forEach { db.reportDao().upsertReport(it) }
            } else if (sampleReport.imageUrl.isEmpty()) {
                // Retrofit image URLs if previously seeded without imageUrl
                db.reportDao().upsertReport(sampleReport.copy(imageUrl = "https://images.unsplash.com/photo-1585829365295-ab7cd400c167?w=600&auto=format&fit=crop"))

                val r2 = db.reportDao().getReportById("rep_102")
                if (r2 != null) db.reportDao().upsertReport(r2.copy(imageUrl = "https://images.unsplash.com/photo-1541252260730-0412e8e2108e?w=600&auto=format&fit=crop"))

                val r3 = db.reportDao().getReportById("rep_103")
                if (r3 != null) db.reportDao().upsertReport(r3.copy(imageUrl = "https://images.unsplash.com/photo-1509316975850-ff9c5deb0cd9?w=600&auto=format&fit=crop"))
            }
        }
    }
}