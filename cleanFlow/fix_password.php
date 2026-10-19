<?php
require_once 'Database.php';
$database = new Database();
$pdo = $database->getConnection();

echo "<h1>Password Fix Tool</h1>";

// Create a NEW password hash for 'admin123'
$new_password = 'admin123';
$new_hash = password_hash($new_password, PASSWORD_DEFAULT);

echo "New hash created: <code>" . $new_hash . "</code><br><br>";

// Update ALL admin users with the new hash
$stmt = $pdo->prepare("UPDATE users SET password = ? WHERE email = 'admin@cleanflow.org' OR email = 'newadmin@cleanflow.org'");
$stmt->execute([$new_hash]);

echo "✅ Updated passwords for admin@cleanflow.org and newadmin@cleanflow.org<br><br>";

// Verify the update worked
$check = $pdo->prepare("SELECT email, password FROM users WHERE email = 'newadmin@cleanflow.org'");
$check->execute();
$user = $check->fetch();

if ($user) {
    echo "Verification for newadmin@cleanflow.org:<br>";
    echo "Stored hash: " . $user['password'] . "<br>";
    
    if (password_verify('admin123', $user['password'])) {
        echo "✅✅✅ PASSWORD VERIFIED! 'admin123' works! ✅✅✅<br>";
    } else {
        echo "❌ Still not working<br>";
    }
}

echo "<br><hr>";
echo "<h3>Now try logging in:</h3>";
echo "<a href='pages/login.php' style='background:blue; color:white; padding:10px 20px; text-decoration:none; border-radius:5px;'>Go to Login Page</a>";
?>