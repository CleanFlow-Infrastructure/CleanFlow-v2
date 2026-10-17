<?php
require_once 'Database.php';
require_once 'includes/functions.php';

$database = new Database();
$pdo = $database->getConnection();

include 'includes/header.php';

// Statistics
try {
    $resolvedIssues = $pdo->query("SELECT COUNT(*) as total FROM reports WHERE status = 'resolved'")->fetch()['total'];
    $communities = $pdo->query("SELECT COUNT(DISTINCT community_name) as total FROM reports")->fetch()['total'];
    $activeUsers = $pdo->query("SELECT COUNT(*) as total FROM users")->fetch()['total'];
    $donationsTotal = $pdo->query("SELECT SUM(amount) as total FROM donations WHERE status = 'completed'")->fetch()['total'] ?? 0;
} catch (PDOException $e) {
    $resolvedIssues = 250; $communities = 50; $activeUsers = 1000; $donationsTotal = 500000;
}

// Water tips for the slider (featured tip first, then random)
try {
    $slideTips = $pdo->query("SELECT category, title, content FROM tips ORDER BY is_featured DESC, RAND() LIMIT 8")->fetchAll();
} catch (PDOException $e) {
    $slideTips = [];
}
$tipIcons = ['Home' => '🏠', 'Garden' => '🌱', 'Community' => '👥', 'Emergency' => '🚨'];
$heroImages = [
    ['images/child-drinking.jpg', 'A child drinking water from a hand pump'],
    ['images/tap-jerrycans.jpg', 'Water containers queuing at a rural tap on dry ground'],
    ['images/children-tap.jpg', 'Smiling children washing their hands at a community tap'],
];
?>

<!-- Hero with background slideshow -->
<section class="relative text-white overflow-hidden bg-blue-900">
    <?php foreach ($heroImages as $i => $img): ?>
        <img src="<?php echo $img[0]; ?>" alt="<?php echo htmlspecialchars($img[1]); ?>"
             class="hero-slide absolute inset-0 w-full h-full object-cover transition-opacity duration-1000 <?php echo $i === 0 ? 'opacity-100' : 'opacity-0'; ?>">
    <?php endforeach; ?>
    <div class="absolute inset-0 bg-gradient-to-r from-blue-950/90 via-blue-900/70 to-blue-900/30"></div>

    <div class="relative max-w-7xl mx-auto px-6 py-28 lg:py-40">
        <div class="max-w-3xl">
            <h1 class="text-4xl md:text-6xl font-bold mb-6 leading-tight">Clean water and working infrastructure for every rural community</h1>
            <p class="text-xl text-blue-100 mb-8 max-w-2xl">Report damaged infrastructure, support community projects, and help improve water and sanitation services through the CleanFlow Digital Platform.</p>
            <div class="flex flex-wrap gap-4">
                <a href="pages/report_issue.php" class="px-8 py-4 bg-white text-blue-700 rounded-lg hover:bg-blue-50 transition-colors font-semibold inline-flex items-center gap-2">📍 Report an Issue</a>
                <a href="pages/donate.php" class="px-8 py-4 border-2 border-white text-white rounded-lg hover:bg-white hover:text-blue-700 transition-colors font-semibold inline-flex items-center gap-2">❤️ Donate Now</a>
                <a href="pages/volunteer.php" class="px-8 py-4 border-2 border-white text-white rounded-lg hover:bg-white hover:text-blue-700 transition-colors font-semibold inline-flex items-center gap-2">🤝 Become a Volunteer</a>
                <a href="pages/water_tips.php" class="px-8 py-4 border-2 border-white text-white rounded-lg hover:bg-white hover:text-blue-700 transition-colors font-semibold inline-flex items-center gap-2">💧 Water Tips</a>
            </div>
        </div>
    </div>

    <div class="absolute bottom-6 left-1/2 -translate-x-1/2 flex gap-2" id="heroDots">
        <?php foreach ($heroImages as $i => $img): ?>
            <button type="button" aria-label="Show image <?php echo $i + 1; ?>" data-i="<?php echo $i; ?>"
                    class="hero-dot h-2.5 rounded-full bg-white transition-all <?php echo $i === 0 ? 'w-8' : 'w-2.5 opacity-50'; ?>"></button>
        <?php endforeach; ?>
    </div>
</section>

<!-- Statistics -->
<section class="bg-slate-50 border-b border-slate-200 py-14">
    <div class="max-w-7xl mx-auto px-6">
        <div class="grid grid-cols-2 lg:grid-cols-4 gap-8 text-center">
            <div><div class="text-4xl font-bold text-blue-600"><?php echo $resolvedIssues ?: '250'; ?>+</div><div class="text-slate-600">Issues Resolved</div></div>
            <div><div class="text-4xl font-bold text-blue-600"><?php echo $communities ?: '50'; ?>+</div><div class="text-slate-600">Communities Supported</div></div>
            <div><div class="text-4xl font-bold text-blue-600"><?php echo $activeUsers ?: '1000'; ?>+</div><div class="text-slate-600">Active Users</div></div>
            <div><div class="text-4xl font-bold text-blue-600">R<?php echo number_format($donationsTotal ?: 500000); ?></div><div class="text-slate-600">Donations Raised</div></div>
        </div>
    </div>
</section>

<!-- Sliding water tips -->
<?php if (!empty($slideTips)): ?>
<section class="py-20 bg-white" aria-label="Water conservation tips">
    <div class="max-w-7xl mx-auto px-6">
        <div class="flex flex-wrap items-end justify-between gap-4 mb-10">
            <div>
                <h2 class="text-3xl font-bold text-slate-900 mb-2">Water conservation tips</h2>
                <p class="text-lg text-slate-600">Small habits that save water at home, in the garden and in your community.</p>
            </div>
            <div class="flex items-center gap-3">
                <button type="button" id="tipPrev" aria-label="Previous tips" class="w-11 h-11 rounded-full border border-slate-300 hover:border-blue-600 hover:text-blue-600">←</button>
                <button type="button" id="tipNext" aria-label="Next tips" class="w-11 h-11 rounded-full border border-slate-300 hover:border-blue-600 hover:text-blue-600">→</button>
            </div>
        </div>

        <div class="overflow-hidden" id="tipViewport">
            <div class="flex transition-transform duration-700 ease-out" id="tipTrack">
                <?php foreach ($slideTips as $tip): ?>
                    <div class="tip-card flex-none px-3" style="width:100%">
                        <article class="h-full bg-gradient-to-br from-blue-50 to-white border border-blue-100 rounded-2xl p-7 flex flex-col">
                            <div class="text-sm font-semibold text-blue-700 mb-3"><?php echo ($tipIcons[$tip['category']] ?? '💧') . ' ' . htmlspecialchars($tip['category']); ?></div>
                            <h3 class="text-xl font-semibold text-slate-900 mb-3"><?php echo htmlspecialchars($tip['title']); ?></h3>
                            <p class="text-slate-600 flex-1"><?php echo htmlspecialchars($tip['content']); ?></p>
                        </article>
                    </div>
                <?php endforeach; ?>
            </div>
        </div>

        <div class="flex items-center justify-between mt-8">
            <div class="flex gap-2" id="tipDots"></div>
            <a href="pages/water_tips.php" class="px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 font-semibold">View all tips</a>
        </div>
    </div>
</section>
<?php endif; ?>

<!-- Features -->
<section class="py-20 bg-slate-50">
    <div class="max-w-7xl mx-auto px-6">
        <div class="text-center mb-14">
            <h2 class="text-3xl font-bold text-slate-900 mb-4">Our platform features</h2>
            <p class="text-xl text-slate-600 max-w-2xl mx-auto">Digital tools that help rural communities manage water and sanitation infrastructure</p>
        </div>
        <div class="grid md:grid-cols-2 lg:grid-cols-4 gap-8">
            <div class="bg-white p-6 rounded-2xl border border-slate-200 hover:shadow-lg transition-shadow">
                <div class="w-14 h-14 bg-blue-100 text-blue-600 rounded-xl flex items-center justify-center mb-4 text-2xl">📍</div>
                <h3 class="font-semibold text-slate-900 mb-3">Real-Time Infrastructure Reporting</h3>
                <p class="text-slate-600">Report damaged infrastructure instantly with GPS location tracking</p>
            </div>
            <div class="bg-white p-6 rounded-2xl border border-slate-200 hover:shadow-lg transition-shadow">
                <div class="w-14 h-14 bg-blue-100 text-blue-600 rounded-xl flex items-center justify-center mb-4 text-2xl">💰</div>
                <h3 class="font-semibold text-slate-900 mb-3">Secure Online Donations</h3>
                <p class="text-slate-600">Support community projects through our secure payment platform</p>
            </div>
            <div class="bg-white p-6 rounded-2xl border border-slate-200 hover:shadow-lg transition-shadow">
                <div class="w-14 h-14 bg-blue-100 text-blue-600 rounded-xl flex items-center justify-center mb-4 text-2xl">👥</div>
                <h3 class="font-semibold text-slate-900 mb-3">Volunteer Coordination</h3>
                <p class="text-slate-600">Connect with volunteers and coordinate community improvement activities</p>
            </div>
            <div class="bg-white p-6 rounded-2xl border border-slate-200 hover:shadow-lg transition-shadow">
                <div class="w-14 h-14 bg-blue-100 text-blue-600 rounded-xl flex items-center justify-center mb-4 text-2xl">📊</div>
                <h3 class="font-semibold text-slate-900 mb-3">Community Project Monitoring</h3>
                <p class="text-slate-600">Track progress of infrastructure projects in real-time</p>
            </div>
        </div>
    </div>
</section>

<!-- Report a problem split section -->
<section class="py-20 bg-white">
    <div class="max-w-7xl mx-auto px-6 grid lg:grid-cols-2 gap-12 items-center">
        <img src="images/pipe-leak.jpg" alt="A leaking pipe joint spilling water into mud" class="rounded-2xl shadow-xl w-full h-80 object-cover">
        <div>
            <h2 class="text-3xl font-bold text-slate-900 mb-4">Seen a leak or a broken tap?</h2>
            <p class="text-lg text-slate-600 mb-6">Every report helps repair teams find and fix problems sooner. Tell us what is damaged and where it is, and follow the repair from your account.</p>
            <a href="pages/report_issue.php" class="px-8 py-4 bg-blue-600 text-white rounded-lg hover:bg-blue-700 font-semibold inline-block">Report an issue</a>
        </div>
    </div>
</section>

<!-- Help strip -->
<section class="py-14 bg-slate-50 border-y border-slate-200">
    <div class="max-w-4xl mx-auto px-6 flex flex-wrap items-center justify-between gap-6">
        <div>
            <h2 class="text-2xl font-bold text-slate-900 mb-1">New here or need a hand?</h2>
            <p class="text-slate-600">Our Help &amp; FAQ explains how to sign in, report a problem, donate, volunteer and use water tips.</p>
        </div>
        <a href="pages/help.php" class="px-8 py-4 bg-blue-600 text-white rounded-lg hover:bg-blue-700 font-semibold">Open Help &amp; FAQ</a>
    </div>
</section>

<!-- Call to action with background image -->
<section class="relative text-white py-24 overflow-hidden bg-blue-900">
    <img src="images/hands-water.jpg" alt="Hands cupped under running water from a tap" class="absolute inset-0 w-full h-full object-cover">
    <div class="absolute inset-0 bg-blue-900/80"></div>
    <div class="relative max-w-4xl mx-auto px-6 text-center">
        <h2 class="text-3xl md:text-4xl font-bold mb-6">Join us in making a difference</h2>
        <p class="text-xl text-blue-100 mb-8">Whether you report issues, donate or volunteer, your contribution helps bring clean water and better sanitation to rural communities.</p>
        <div class="flex flex-wrap gap-4 justify-center">
            <a href="pages/register.php" class="px-8 py-4 bg-white text-blue-700 rounded-lg hover:bg-blue-50 font-semibold">Get started today</a>
            <a href="pages/contact.php" class="px-8 py-4 border-2 border-white text-white rounded-lg hover:bg-white hover:text-blue-700 font-semibold">Contact us</a>
        </div>
    </div>
</section>

<script>
(function () {
    var reduce = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

    // Hero background slideshow
    var slides = document.querySelectorAll('.hero-slide');
    var dots = document.querySelectorAll('.hero-dot');
    var cur = 0, heroTimer;
    function showHero(n) {
        cur = n;
        slides.forEach(function (s, i) { s.classList.toggle('opacity-100', i === n); s.classList.toggle('opacity-0', i !== n); });
        dots.forEach(function (d, i) {
            d.classList.toggle('w-8', i === n);
            d.classList.toggle('w-2.5', i !== n);
            d.classList.toggle('opacity-50', i !== n);
        });
    }
    function startHero() { if (!reduce && slides.length > 1) heroTimer = setInterval(function () { showHero((cur + 1) % slides.length); }, 6000); }
    dots.forEach(function (d) { d.addEventListener('click', function () { clearInterval(heroTimer); showHero(+d.dataset.i); startHero(); }); });
    startHero();

    // Sliding water tips
    var track = document.getElementById('tipTrack');
    if (!track) return;
    var cards = track.querySelectorAll('.tip-card');
    var dotWrap = document.getElementById('tipDots');
    var idx = 0, perView = 1, tipTimer;

    function calc() {
        perView = window.innerWidth >= 1024 ? 3 : (window.innerWidth >= 768 ? 2 : 1);
        cards.forEach(function (c) { c.style.width = (100 / perView) + '%'; });
        buildDots();
        go(Math.min(idx, maxIdx()));
    }
    function maxIdx() { return Math.max(0, cards.length - perView); }
    function buildDots() {
        dotWrap.innerHTML = '';
        for (var i = 0; i <= maxIdx(); i++) {
            var b = document.createElement('button');
            b.type = 'button';
            b.setAttribute('aria-label', 'Go to tip group ' + (i + 1));
            b.className = 'h-2.5 rounded-full bg-blue-600 transition-all';
            (function (n) { b.addEventListener('click', function () { go(n); restart(); }); })(i);
            dotWrap.appendChild(b);
        }
    }
    function go(n) {
        idx = n;
        track.style.transform = 'translateX(-' + (idx * (100 / perView)) + '%)';
        Array.prototype.forEach.call(dotWrap.children, function (d, i) {
            d.classList.toggle('w-8', i === idx);
            d.classList.toggle('w-2.5', i !== idx);
            d.classList.toggle('opacity-30', i !== idx);
        });
    }
    function next() { go(idx >= maxIdx() ? 0 : idx + 1); }
    function prev() { go(idx <= 0 ? maxIdx() : idx - 1); }
    function restart() { clearInterval(tipTimer); if (!reduce) tipTimer = setInterval(next, 5000); }

    document.getElementById('tipNext').addEventListener('click', function () { next(); restart(); });
    document.getElementById('tipPrev').addEventListener('click', function () { prev(); restart(); });
    var vp = document.getElementById('tipViewport');
    vp.addEventListener('mouseenter', function () { clearInterval(tipTimer); });
    vp.addEventListener('mouseleave', restart);
    window.addEventListener('resize', calc);
    calc();
    restart();
})();
</script>

<!-- Floating help button -->
<a href="pages/help.php" aria-label="Help and FAQ" class="fixed bottom-6 right-6 z-50 px-5 py-3 bg-blue-600 text-white rounded-full shadow-lg hover:bg-blue-700 font-semibold">Help</a>

<?php include 'includes/footer.php'; ?>