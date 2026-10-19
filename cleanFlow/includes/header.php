<?php
if (session_status() === PHP_SESSION_NONE) {
    session_start();
}
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>CleanFlow NPO - Clean Water & Infrastructure for Rural Communities</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <style>
        @import url('https://fonts.googleapis.com/css2?family=Inter:opsz,wght@14..32,300;14..32,400;14..32,500;14..32,600;14..32,700&display=swap');
        * { font-family: 'Inter', sans-serif; }
        .nav-link:hover { color: #2563eb; }
    </style>
</head>
<body class="bg-slate-50">
    <header class="bg-white border-b border-slate-200 sticky top-0 z-50 shadow-sm">
        <div class="max-w-7xl mx-auto px-6 py-4">
            <div class="flex items-center justify-between">
                <div class="flex items-center gap-3 cursor-pointer" onclick="window.location.href='index.php'">
                    <img src="images/logo.png" alt="CleanFlow" class="h-12 w-auto" onerror="this.src='https://via.placeholder.com/48?text=CF'">
                    <div>
                        <div class="font-bold text-xl text-slate-800">CleanFlow</div>
                        <div class="text-xs text-blue-600">Community Infrastructure</div>
                    </div>
                </div>
                <nav class="hidden md:flex items-center gap-6">
                    <a href="index.php" class="nav-link text-slate-700 hover:text-blue-600">Home</a>
                    <a href="pages/report_issue.php" class="nav-link text-slate-700 hover:text-blue-600">Report Issue</a>
                    <a href="pages/donate.php" class="nav-link text-slate-700 hover:text-blue-600">Donate</a>
                    <a href="pages/volunteer.php" class="nav-link text-slate-700 hover:text-blue-600">Volunteer</a>
                    <a href="pages/contact.php" class="nav-link text-slate-700 hover:text-blue-600">Contact</a>
                    <?php if(isset($_SESSION['user_id'])): ?>
                        <div class="relative group">
                            <button class="flex items-center gap-2 px-4 py-2 bg-slate-100 rounded-full">
                                👤 <?php echo htmlspecialchars($_SESSION['user_name'] ?? 'User'); ?>
                            </button>
                            <div class="absolute right-0 mt-2 w-48 bg-white rounded-lg shadow-lg border hidden group-hover:block">
    <a href="pages/notifications.php" class="block px-4 py-2 hover:bg-slate-50">🔔 Notifications</a>
    <?php if(isset($_SESSION['user_role']) && $_SESSION['user_role'] == 'admin'): ?>
        <a href="pages/admin_dashboard.php" class="block px-4 py-2 hover:bg-slate-50">📊 Admin Dashboard</a>
    <?php endif; ?>
    <a href="pages/logout.php" class="block px-4 py-2 text-red-600 hover:bg-slate-50">🚪 Logout</a>
</div>
                        </div>
                    <?php else: ?>
                        <a href="pages/login.php" class="px-5 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700">Login</a>
                        <a href="pages/register.php" class="px-5 py-2 border border-blue-600 text-blue-600 rounded-lg hover:bg-blue-50">Register</a>
                    <?php endif; ?>
                </nav>
            </div>
        </div>
    </header>
    <main>