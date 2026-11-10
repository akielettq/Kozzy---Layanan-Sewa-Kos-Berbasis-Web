<?php
// includes/footer.php — Footer, Modal Logout Global, Toast Container & Flash Script Trigger
$flash = getFlash();
?>
<footer class="footer">
  <div><b style="font-size:26px">Kozzy<b>.</b></b><br><small>Kos bulanan · Putra &amp; Putri · Pekanbaru</small></div>
  <div class="ket-footer"><small>0896-6132-7050 (WA) · kozzy@gmail.com<br>Jl. Bangau Perkasa, Pekanbaru</small></div>
</footer>

<!-- Modal Konfirmasi Keluar (Logout) -->
<div class="modal" id="modalKeluar" onclick="if (event.target == this) tutupModal('modalKeluar')">
  <div class="modal-kotak tengah">
    <div class="ikon-tanya">?</div>
    <h3>Keluar dari Kozzy?</h3>
    <p class="sub-tengah">Kamu perlu masuk lagi nanti untuk booking atau cek status sewa.</p>
    <a class="tombol merah penuh" href="logout.php" style="display:block;text-align:center;text-decoration:none;">Ya, Keluar</a>
    <button type="button" class="tombol penuh" style="margin-top:10px" onclick="tutupModal('modalKeluar')">Batal</button>
  </div>
</div>

<div id="toastWrap"></div>

<script src="script.js"></script>
<?php if ($flash): ?>
<script>
  document.addEventListener('DOMContentLoaded', function() {
    toast(<?= json_encode($flash['pesan']) ?>);
  });
</script>
<?php endif; ?>
</body>
</html>
