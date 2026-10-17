<?php
// pages/help.php - built-in help and FAQ for CleanFlow
$faqs = [
    'Getting started' => [
        ['How do I create an account?', 'Select "Create Account" on the login page, enter your name, email address and a password, then submit the form. You can also sign in with one click using "Sign in with Google", which creates your account automatically the first time.'],
        ['How do I sign in with Google?', 'On the login page, click the Google button, choose your Google account in the pop-up window and confirm. You will be taken to the home page, already signed in.'],
        ['I entered the wrong password. What happens?', 'You will see the message "Invalid email or password" and can try again. Check that your email address is spelled correctly and that Caps Lock is off.'],
    ],
    'Reporting problems' => [
        ['How do I report damaged infrastructure?', 'Open "Report an Issue" from the home page, choose the type of infrastructure (for example borehole, pipeline or toilet facility), enter the community name, describe the problem and submit. Add a photo and location if you can, because this helps repair teams find the problem faster.'],
        ['What counts as an emergency?', 'For urgent problems that affect a community water supply, call the 24/7 emergency hotline on 0800 CLEAN FLOW instead of waiting for a report to be processed.'],
    ],
    'Donations and volunteering' => [
        ['How do I donate?', 'Open "Donate Now", pick an amount or enter your own, fill in your name and email, choose a payment method (card, EFT or mobile payment) and click "Donate Securely". Tick "Make this donation anonymous" if you do not want your name shown in the recent donors list.'],
        ['How do I become a volunteer?', 'Open "Become a Volunteer" from the home page and complete the form with your skills, availability and preferred community. Coordinators use this information to assign tasks.'],
    ],
    'Water tips' => [
        ['Where can I find water saving tips?', 'Open "Water Tips" from the home page or menu. Tips are grouped into Home, Garden, Community and Emergency, and you can use the category buttons to filter them.'],
        ['How do I save or share a tip?', 'Sign in, then click "Save" on a tip to add it to your favourites. Click "My Favourites" to see only your saved tips. The "Share" button opens your device\'s share menu, or copies the tip so you can paste it into a message.'],
    ],
    'Contact and support' => [
        ['How can I contact the CleanFlow team?', 'Use the Contact page to send a message. We are open Monday to Friday 8:00 AM - 5:00 PM and Saturday 9:00 AM - 1:00 PM, and closed on Sundays.'],
    ],
];
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Help &amp; FAQ - CleanFlow</title>
    <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="min-h-screen p-6" style="background: radial-gradient(circle at 8% 15%, rgba(59,130,246,.20), transparent 40%), radial-gradient(circle at 92% 8%, rgba(14,165,233,.20), transparent 35%), radial-gradient(circle at 85% 92%, rgba(37,99,235,.16), transparent 40%), linear-gradient(135deg, #eff6ff, #f1f5f9); background-attachment: fixed;">
    <div class="max-w-3xl mx-auto">
        <a href="../index.php" class="inline-flex items-center gap-2 text-slate-600 hover:text-slate-900 mb-6">← Back to Home</a>

        <div class="relative rounded-2xl overflow-hidden shadow-xl mb-8 bg-blue-900">
            <img src="../images/children-tap.jpg" alt="Smiling children washing their hands at a community tap" class="absolute inset-0 w-full h-full object-cover">
            <div class="absolute inset-0 bg-gradient-to-r from-blue-950/90 via-blue-900/75 to-blue-900/40"></div>
            <div class="relative p-8 md:p-12 text-white">
                <h1 class="text-3xl md:text-4xl font-bold mb-3">How can we help?</h1>
                <p class="text-blue-100 text-lg mb-6 max-w-xl">Find quick answers on how to use CleanFlow. Type a word to filter the questions.</p>
                <input type="search" id="faqSearch" placeholder="Search help, e.g. donate, password, report" aria-label="Search help"
                       class="w-full max-w-xl px-5 py-3 rounded-lg text-slate-800 focus:outline-none focus:ring-4 focus:ring-blue-300">
            </div>
        </div>

        <?php foreach ($faqs as $section => $items): ?>
            <div class="faq-section mb-6">
                <h2 class="text-lg font-semibold text-slate-800 mb-3"><?php echo htmlspecialchars($section); ?></h2>
                <div class="space-y-3">
                    <?php foreach ($items as $item): ?>
                        <details class="faq-item bg-white rounded-xl shadow p-5 border-l-4 border-blue-500 hover:shadow-lg transition-shadow">
                            <summary class="font-medium text-slate-800 cursor-pointer"><?php echo htmlspecialchars($item[0]); ?></summary>
                            <p class="text-slate-600 mt-3"><?php echo htmlspecialchars($item[1]); ?></p>
                        </details>
                    <?php endforeach; ?>
                </div>
            </div>
        <?php endforeach; ?>

        <p id="noResults" class="hidden text-center text-slate-600 py-6">No matching help topics. Try a different word or contact us.</p>

        <div class="bg-gradient-to-br from-blue-600 to-blue-800 rounded-2xl p-6 text-white">
            <h2 class="font-semibold mb-2">Still need help?</h2>
            <p class="text-blue-100 text-sm mb-4">Our team is happy to assist you.</p>
            <a href="contact.php" class="inline-block px-6 py-3 bg-white text-blue-700 rounded-lg font-semibold hover:bg-blue-50">Contact Us</a>
        </div>
    </div>

    <script>
        document.getElementById('faqSearch').addEventListener('input', function () {
            var q = this.value.toLowerCase().trim();
            var any = false;
            document.querySelectorAll('.faq-section').forEach(function (sec) {
                var visible = 0;
                sec.querySelectorAll('.faq-item').forEach(function (item) {
                    var show = item.textContent.toLowerCase().indexOf(q) !== -1;
                    item.style.display = show ? '' : 'none';
                    if (show) { visible++; }
                });
                sec.style.display = visible ? '' : 'none';
                if (visible) { any = true; }
            });
            document.getElementById('noResults').classList.toggle('hidden', any);
        });
    </script>
</body>
</html>