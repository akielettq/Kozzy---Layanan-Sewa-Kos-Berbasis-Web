<?php
// includes/nav.php — Navigasi Atas Responsif Sesuai Status Login & Peran
$u = userLogin();
$sekarang = basename($_SERVER['PHP_SELF']);
?>
<header class="nav">
  <div class="nav-dalam">
    <a class="logo" href="index.php">
      <img src="logo kos.png" alt="Logo">
      Kozzy<b>.</b>
    </a>
    <nav class="menu">
      <a href="index.php" class="<?= $sekarang == 'index.php' ? 'aktif' : '' ?>">Beranda</a>
      <a href="kamar.php" class="<?= $sekarang == 'kamar.php' ? 'aktif' : '' ?>">Kamar</a>
      <?php if (!$u || $u['peran'] !== 'admin'): ?>
        <a href="sewaku.php" class="<?= $sekarang == 'sewaku.php' ? 'aktif' : '' ?>">Sewaku</a>
      <?php endif; ?>
      <?php if ($u && $u['peran'] === 'admin'): ?>
        <a href="admin.php" class="<?= $sekarang == 'admin.php' ? 'aktif' : '' ?>">Admin</a>
      <?php endif; ?>
    </nav>
    <div class="nav-user" id="navUser">
      <?php if (!$u): ?>
        <a class="tombol kecil" href="login.php">Masuk</a>
      <?php else: ?>
        <a class="profil-chip" href="profil.php" title="Lihat profil">
          <span class="avatar"><?= htmlspecialchars(strtoupper(substr($u['nama'], 0, 1))) ?></span>
          <span class="nama-chip"><?= htmlspecialchars(explode(' ', $u['nama'])[0]) ?></span>
        </a>
        <button type="button" class="tombol kecil" onclick="tanyaLogout()">Keluar</button>
      <?php endif; ?>
    </div>
  </div>
</header>
