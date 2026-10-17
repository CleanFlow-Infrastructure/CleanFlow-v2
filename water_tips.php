<?php
// pages/water_tips.php
require_once '../Database.php';
if (session_status() === PHP_SESSION_NONE) { session_start(); }

$database = new Database();
$pdo = $database->getConnection();

$categories = ['Home', 'Garden', 'Community', 'Emergency'];
$isLoggedIn = isset($_SESSION['user_id']);
$userId = $isLoggedIn ? (int)$_SESSION['user_id'] : 0;

// CSRF token for favourite toggling
if (empty($_SESSION['csrf'])) { $_SESSION['csrf'] = bin2hex(random_bytes(16)); }

$message = '';

// Toggle favourite (POST)
if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['toggle_fav'])) {
    if (!$isLoggedIn) {
        header("Location: login.php");
        exit();
    }
    if (!hash_equals($_SESSION['csrf'], $_POST['csrf'] ?? '')) {
        http_response_code(403);
        exit('Invalid request');
    }
    $tipId = (int)($_POST['tip_id'] ?? 0);
    $check = $pdo->prepare("SELECT id FROM favourite_tips WHERE user_id = ? AND tip_id = ?");
    $check->execute([$userId, $tipId]);
    if ($check->fetch()) {
        $pdo->prepare("DELETE FROM favourite_tips WHERE user_id = ? AND tip_id = ?")->execute([$userId, $tipId]);
    } else {
        $pdo->prepare("INSERT IGNORE INTO favourite_tips (user_id, tip_id) VALUES (?, ?)")->execute([$userId, $tipId]);
    }
    header("Location: water_tips.php?" . http_build_query(array_filter([
        'category' => $_POST['category'] ?? '',
        'view' => $_POST['view'] ?? ''
    ])));
    exit();
}

// Filters (GET)
$category = $_GET['category'] ?? 'All';
if (!in_array($category, $categories, true)) { $category = 'All'; }
$view = ($_GET['view'] ?? '') === 'favourites' ? 'favourites' : 'all';

// Featured tip
$featured = $pdo->query("SELECT * FROM tips WHERE is_featured = 1 ORDER BY created_at DESC LIMIT 1")->fetch();

// Favourite ids for this user
$favIds = [];
if ($isLoggedIn) {
    $f = $pdo->prepare("SELECT tip_id FROM favourite_tips WHERE user_id = ?");
    $f->execute([$userId]);
    $favIds = array_map('intval', $f->fetchAll(PDO::FETCH_COLUMN));
}

// Tip list
$sql = "SELECT t.* FROM tips t";
$params = [];
$where = [];
if ($view === 'favourites' && $isLoggedIn) {
    $sql .= " INNER JOIN favourite_tips ft ON ft.tip_id = t.id AND ft.user_id = ?";
    $params[] = $userId;
}
if ($category !== 'All') {
    $where[] = "t.category = ?";
    $params[] = $category;
}
if ($where) { $sql .= " WHERE " . implode(' AND ', $where); }
$sql .= " ORDER BY t.category, t.id";
$stmt = $pdo->prepare($sql);
$stmt->execute($params);
$tips = $stmt->fetchAll();

$icons = ['Home' => '🏠', 'Garden' => '🌱', 'Community' => '👥', 'Emergency' => '🚨'];
$catImages = [
    'Home' => ['../images/pipe-leak.jpg', 'A leaking pipe joint'],
    'Garden' => ['../images/hands-water.jpg', 'Hands cupped under running water'],
    'Community' => ['../images/children-tap.jpg', 'Children washing their hands at a community tap'],
    'Emergency' => ['../images/tap-jerrycans.jpg', 'Water containers waiting at a tap on dry ground'],
];
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Water Conservation Tips - CleanFlow</title>
    <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-gradient-to-br from-blue-50 to-slate-100 min-h-screen p-6">
    <div class="max-w-5xl mx-auto">
        <a href="../index.php" class="inline-flex items-center gap-2 text-slate-600 hover:text-slate-900 mb-6">← Back to Home</a>

        <div class="relative rounded-2xl overflow-hidden mb-8 shadow-xl bg-blue-900">
            <img src="../images/child-drinking.jpg" alt="A child drinking water from a hand pump" class="absolute inset-0 w-full h-full object-cover">
            <div class="absolute inset-0 bg-gradient-to-r from-blue-950/90 via-blue-900/70 to-blue-900/20"></div>
            <div class="relative p-8 md:p-12 max-w-2xl text-white">
                <h1 class="text-3xl md:text-4xl font-bold mb-3">Water conservation tips</h1>
                <p class="text-blue-100 text-lg">Practical ways to save water at home, in the garden, in your community and during emergencies.</p>
            </div>
        </div>

        <?php if ($featured): ?>
        <div class="bg-gradient-to-br from-blue-600 to-blue-800 text-white rounded-2xl mb-8 shadow-xl overflow-hidden grid md:grid-cols-5">
            <?php $fi = $catImages[$featured['category']] ?? null; ?>
            <?php if ($fi): ?>
                <img src="<?php echo $fi[0]; ?>" alt="<?php echo htmlspecialchars($fi[1]); ?>" class="md:col-span-2 w-full h-56 md:h-full object-cover">
            <?php endif; ?>
            <div class="<?php echo $fi ? 'md:col-span-3' : 'md:col-span-5'; ?> p-8">
            <div class="text-sm uppercase tracking-wide text-blue-200 mb-2">⭐ Featured Tip · <?php echo htmlspecialchars($featured['category']); ?></div>
            <h2 class="text-2xl font-bold mb-3"><?php echo htmlspecialchars($featured['title']); ?></h2>
            <p class="text-blue-100 mb-5"><?php echo htmlspecialchars($featured['content']); ?></p>
            <button type="button" class="share-btn px-5 py-2 bg-white text-blue-700 rounded-lg font-semibold hover:bg-blue-50"
                data-title="<?php echo htmlspecialchars($featured['title'], ENT_QUOTES); ?>"
                data-text="<?php echo htmlspecialchars($featured['content'], ENT_QUOTES); ?>">🔗 Share</button>
            </div>
        </div>
        <?php endif; ?>

        <!-- Category filters -->
        <div class="flex flex-wrap gap-2 mb-4">
            <?php
            $base = $view === 'favourites' ? '&view=favourites' : '';
            $cls = fn($active) => $active ? 'bg-blue-600 text-white' : 'bg-white text-slate-700 border border-slate-200 hover:border-blue-600';
            ?>
            <a href="water_tips.php?category=All<?php echo $base; ?>" class="px-4 py-2 rounded-full text-sm font-medium <?php echo $cls($category === 'All'); ?>">All</a>
            <?php foreach ($categories as $c): ?>
                <a href="water_tips.php?category=<?php echo $c . $base; ?>" class="px-4 py-2 rounded-full text-sm font-medium <?php echo $cls($category === $c); ?>"><?php echo $c; ?></a>
            <?php endforeach; ?>
            <span class="flex-1"></span>
            <a href="water_tips.php?category=<?php echo urlencode($category); ?>" class="px-4 py-2 rounded-full text-sm font-medium <?php echo $cls($view === 'all'); ?>">All Tips</a>
            <a href="water_tips.php?category=<?php echo urlencode($category); ?>&view=favourites" class="px-4 py-2 rounded-full text-sm font-medium <?php echo $cls($view === 'favourites'); ?>">❤️ My Favourites</a>
        </div>

        <?php if ($view === 'favourites' && !$isLoggedIn): ?>
            <div class="bg-white rounded-2xl shadow p-8 text-center">
                <p class="text-slate-600 mb-4">Please sign in to save and view your favourite tips.</p>
                <a href="login.php" class="px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 font-semibold">Sign In</a>
            </div>
        <?php elseif (empty($tips)): ?>
            <div class="bg-white rounded-2xl shadow p-8 text-center text-slate-600">
                <div class="text-5xl mb-3">📭</div>
                <?php echo $view === 'favourites' ? 'You have no favourite tips yet.' : 'No tips found in this category.'; ?>
            </div>
        <?php else: ?>
            <div class="grid md:grid-cols-2 gap-6 max-h-[70vh] overflow-y-auto pr-1">
                <?php foreach ($tips as $tip): $isFav = in_array((int)$tip['id'], $favIds, true); ?>
                    <div class="bg-white rounded-2xl shadow-md overflow-hidden flex flex-col">
                        <?php if (isset($catImages[$tip['category']])): $ci = $catImages[$tip['category']]; ?>
                            <img src="<?php echo $ci[0]; ?>" alt="<?php echo htmlspecialchars($ci[1]); ?>" class="w-full h-36 object-cover">
                        <?php endif; ?>
                        <div class="p-6 flex flex-col flex-1">
                        <div class="text-xs font-semibold text-blue-600 mb-2"><?php echo htmlspecialchars($tip['category']); ?></div>
                        <h3 class="font-semibold text-slate-800 mb-2"><?php echo htmlspecialchars($tip['title']); ?></h3>
                        <p class="text-slate-600 text-sm flex-1"><?php echo htmlspecialchars($tip['content']); ?></p>
                        <div class="flex items-center gap-3 mt-4">
                            <form method="POST">
                                <input type="hidden" name="toggle_fav" value="1">
                                <input type="hidden" name="tip_id" value="<?php echo (int)$tip['id']; ?>">
                                <input type="hidden" name="csrf" value="<?php echo $_SESSION['csrf']; ?>">
                                <input type="hidden" name="category" value="<?php echo $category === 'All' ? '' : htmlspecialchars($category); ?>">
                                <input type="hidden" name="view" value="<?php echo $view === 'favourites' ? 'favourites' : ''; ?>">
                                <button type="submit" class="px-4 py-2 rounded-lg text-sm border <?php echo $isFav ? 'bg-red-50 border-red-200 text-red-600' : 'border-slate-200 text-slate-600 hover:border-blue-600'; ?>">
                                    <?php echo $isFav ? '❤️ Saved' : '🤍 Save'; ?>
                                </button>
                            </form>
                            <button type="button" class="share-btn px-4 py-2 rounded-lg text-sm border border-slate-200 text-slate-600 hover:border-blue-600"
                                data-title="<?php echo htmlspecialchars($tip['title'], ENT_QUOTES); ?>"
                                data-text="<?php echo htmlspecialchars($tip['content'], ENT_QUOTES); ?>">🔗 Share</button>
                        </div>
                        </div>
                    </div>
                <?php endforeach; ?>
            </div>
        <?php endif; ?>
    </div>

    <div id="toast" class="fixed bottom-6 left-1/2 -translate-x-1/2 bg-slate-800 text-white px-5 py-3 rounded-lg hidden">Tip copied to clipboard</div>

    <script>
        document.querySelectorAll('.share-btn').forEach(btn => {
            btn.addEventListener('click', async () => {
                const title = btn.dataset.title;
                const text = title + ' - ' + btn.dataset.text + ' (CleanFlow Water Tips)';
                try {
                    if (navigator.share) {
                        await navigator.share({ title: 'CleanFlow Water Tip', text: text, url: location.href });
                    } else {
                        await navigator.clipboard.writeText(text + ' ' + location.href);
                        const t = document.getElementById('toast');
                        t.classList.remove('hidden');
                        setTimeout(() => t.classList.add('hidden'), 2000);
                    }
                } catch (e) { /* user cancelled share */ }
            });
        });
    </script>
</body>
</html>