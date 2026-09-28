/* ============================================
   KOZZY — script.js (dipakai bersama semua halaman)
   Data disimpan di localStorage, jadi login,
   booking, dan kamar tetap keingatan walau
   pindah halaman atau refresh.
============================================ */

// ====== 1. VARIABEL GLOBAL ======
var pengguna = [];      // daftar akun
var kamarList = [];     // daftar kamar
var bookingList = [];   // daftar booking
var userLogin = null;   // akun yang sedang login
var filterTipe = "semua";
var kamarDipilih = null;
var metodeDipilih = null;
var buktiBayar = null;
var idDitolak = null;
var yakinReset = false;

// supaya warna & tulisan status seragam di semua halaman
var INFO_STATUS = {
  tersedia:     ["hijau",  "Tersedia"],
  terisi:       ["merah",  "Terisi"],
  pemeliharaan: ["kuning", "Perbaikan"],
  pending:      ["kuning", "Menunggu Verifikasi"],
  approved:     ["hijau",  "Disetujui"],
  rejected:     ["merah",  "Ditolak"]
};

// ====== 2. PENYIMPANAN (localStorage) ======
function simpanData() {
  localStorage.setItem("kozzyData", JSON.stringify({
    pengguna: pengguna, kamar: kamarList, booking: bookingList
  }));
}

function ambilData() {
  var mentah = localStorage.getItem("kozzyData");
  if (mentah == null) {
    isiDataContoh(); // pertama kali dibuka → isi data contoh
  } else {
    var d = JSON.parse(mentah);
    pengguna = d.pengguna;
    kamarList = d.kamar;
    bookingList = d.booking;
  }
  cekMasaSewa(); // kamar yang masa sewanya habis jadi kosong lagi
  simpanData();
  // cek sesi login (id-nya disimpan di kozzyLogin)
  var idLogin = localStorage.getItem("kozzyLogin");
  userLogin = null;
  if (idLogin != null) {
    for (var i = 0; i < pengguna.length; i++) {
      if (pengguna[i].id == idLogin) userLogin = pengguna[i];
    }
  }
}

// mengembalikan semua data ke kondisi awal (tombol di halaman admin)
function resetData() {
  if (!yakinReset) {
    yakinReset = true;
    document.getElementById("tombolReset").innerText = "Yakin? Klik sekali lagi";
    return;
  }
  localStorage.removeItem("kozzyData");
  localStorage.removeItem("kozzyLogin");
  window.location.href = "index.html";
}

function isiDataContoh() {
  pengguna = [
    { id: 1, nama: "Admin Kozzy (Akiel)",  email: "admin@kozzy.id", sandi: "admin123", peran: "admin" },
    { id: 2, nama: "Farah", email: "farah@kozzy.id",  sandi: "123456",   peran: "pencari" },
    { id: 3, nama: "Fildza",   email: "fildza@kozzy.id",  sandi: "123456",   peran: "pencari" }
  ];
  kamarList = [
    { kode: "A-01", tipe: "putra", harga: 850000,  status: "tersedia",     terisiHingga: null,             deskripsi: "Kamar lantai 1, dekat parkiran.",       fasilitas: "Wi-Fi, Kasur, Lemari, AC" },
    { kode: "A-02", tipe: "putra", harga: 950000,  status: "tersedia",     terisiHingga: null,             deskripsi: "Kamar lantai 1 menghadap halaman.",      fasilitas: "Wi-Fi, Kasur, Lemari, AC, Meja" },
    { kode: "A-03", tipe: "putra", harga: 1100000, status: "tersedia",     terisiHingga: null,             deskripsi: "Kamar lantai 2 dengan lemari besar.", fasilitas: "Wi-Fi, Kasur, AC, K. Mandi Dalam" },
    { kode: "A-04", tipe: "putra", harga: 900000,  status: "terisi",       terisiHingga: tanggalGeser(20), deskripsi: "Kamar lantai 2 dengan meja kerja.",           fasilitas: "Wi-Fi, Kasur, Lemari, Meja" },
    { kode: "B-01", tipe: "putri", harga: 950000,  status: "tersedia",     terisiHingga: null,             deskripsi: "Kamar lantai 1 dekat parkiran.",                   fasilitas: "Wi-Fi, Kasur, Lemari, AC" },
    { kode: "B-02", tipe: "putri", harga: 1150000, status: "terisi",       terisiHingga: tanggalGeser(10), deskripsi: "Kamar lantai 1 dekat taman.",        fasilitas: "Wi-Fi, Kasur, AC, K. Mandi Dalam" },
    { kode: "B-03", tipe: "putri", harga: 1050000, status: "tersedia",     terisiHingga: null,             deskripsi: "Kamar lantai 2 dengan lemari besar.",        fasilitas: "Wi-Fi, Kasur, Lemari, Meja" },
    { kode: "B-04", tipe: "putri", harga: 1350000, status: "pemeliharaan", terisiHingga: null,             deskripsi: "Sedang renovasi, siap huni bulan depan.",    fasilitas: "Wi-Fi, Kasur, AC" }
  ];
  bookingList = [
    { id: "KZ-0117", userId: 3, kamar: "B-02", mulai: tanggalGeser(-20), durasi: 1, status: "approved", metode: "Transfer BCA", bukti: null, alasan: null },
    { id: "KZ-0118", userId: 2, kamar: "A-03", mulai: tanggalGeser(4),   durasi: 1, status: "pending",  metode: "QRIS",        bukti: null, alasan: null }
  ];
}

// ====== 3. FUNGSI KECIL (helper) ======
function rupiah(n) { return "Rp " + n.toLocaleString("id-ID"); }

var NAMA_BULAN = ["Jan","Feb","Mar","Apr","Mei","Jun","Jul","Agu","Sep","Okt","Nov","Des"];
function formatTanggal(iso) {
  var b = iso.split("-"); // "2026-02-12" -> ["2026","02","12"]
  return Number(b[2]) + " " + NAMA_BULAN[Number(b[1]) - 1] + " " + b[0];
}
function keIso(d) {
  var bulan = d.getMonth() + 1, tgl = d.getDate();
  return d.getFullYear() + "-" + (bulan < 10 ? "0" + bulan : bulan) + "-" + (tgl < 10 ? "0" + tgl : tgl);
}
function tanggalGeser(hari) { // hari ini + N hari (boleh minus)
  var d = new Date();
  d.setDate(d.getDate() + hari);
  return keIso(d);
}
function tambahBulan(iso, n) {
  var d = new Date(iso);
  d.setMonth(d.getMonth() + Number(n));
  return keIso(d);
}
function cariKamar(kode) {
  for (var i = 0; i < kamarList.length; i++) if (kamarList[i].kode == kode) return kamarList[i];
  return null;
}
function cariBooking(id) {
  for (var i = 0; i < bookingList.length; i++) if (bookingList[i].id == id) return bookingList[i];
  return null;
}
function cariPengguna(id) {
  for (var i = 0; i < pengguna.length; i++) if (pengguna[i].id == id) return pengguna[i];
  return null;
}
function acakKode() { return "KZ-" + Math.random().toString(36).substr(2, 4).toUpperCase(); }

// baca parameter di URL, contoh: booking.html?kode=A-01 -> ambilParam("kode") = "A-01"
function ambilParam(nama) {
  var pasangan = window.location.search.substring(1).split("&");
  for (var i = 0; i < pasangan.length; i++) {
    var bagian = pasangan[i].split("=");
    if (bagian[0] == nama) return decodeURIComponent(bagian[1]);
  }
  return null;
}

// ====== 4. ATURAN BISNIS ======
// satu akun hanya boleh punya satu sewa aktif
// (pending = masih diproses, approved + kamarnya masih terisi = masih menghuni)
function punyaSewaAktif() {
  if (userLogin == null) return null;
  for (var i = 0; i < bookingList.length; i++) {
    var b = bookingList[i];
    if (b.userId != userLogin.id) continue;
    if (b.status == "pending") return b;
    if (b.status == "approved") {
      var k = cariKamar(b.kamar);
      if (k != null && k.status == "terisi") return b;
    }
  }
  return null;
}
// apakah sudah ada pengajuan pending di kamar tersebut?
function adaPendingDiKamar(kode) {
  for (var i = 0; i < bookingList.length; i++) {
    if (bookingList[i].kamar == kode && bookingList[i].status == "pending") return true;
  }
  return false;
}
// kamar otomatis kosong lagi kalau masa sewanya sudah lewat
function cekMasaSewa() {
  var hariIni = tanggalGeser(0);
  for (var i = 0; i < kamarList.length; i++) {
    var k = kamarList[i];
    if (k.status == "terisi" && k.terisiHingga != null && k.terisiHingga < hariIni) {
      k.status = "tersedia";
      k.terisiHingga = null;
    }
  }
}

// ====== 5. NAVBAR ======
function perbaruiNavbar() {
  var kotak = document.getElementById("navUser");
  if (kotak == null) return;
  if (userLogin == null) {
    kotak.innerHTML = "<a class='tombol kecil' href='login.html'>Masuk</a>";
  } else {
    kotak.innerHTML =
      "<a class='profil-chip' href='profil.html' title='Lihat profil'>" +
        "<span class='avatar'>" + userLogin.nama.charAt(0).toUpperCase() + "</span>" +
        "<span class='nama-chip'>" + userLogin.nama.split(" ")[0] + "</span>" +
      "</a>" +
      "<button class='tombol kecil' onclick='tanyaLogout()'>Keluar</button>";
  }
}

// konfirmasi sebelum keluar — modalnya dibuat lewat JS sekali saja,
// jadi tidak perlu di-copy ke tiap halaman HTML
function tanyaLogout() {
  var modal = document.getElementById("modalKeluar");
  if (modal == null) {
    modal = document.createElement("div");
    modal.className = "modal";
    modal.id = "modalKeluar";
    modal.innerHTML = "<div class='modal-kotak tengah'>" +
      "<div class='ikon-tanya'>?</div>" +
      "<h3>Keluar dari Kozzy?</h3>" +
      "<p class='sub-tengah'>Kamu perlu masuk lagi nanti untuk booking atau cek status sewa.</p>" +
      "<button class='tombol merah penuh' onclick='logout()'>Ya, Keluar</button>" +
      "<button class='tombol penuh' style='margin-top:10px' onclick=\"tutupModal('modalKeluar')\">Batal</button>" +
    "</div>";
    // klik area gelap di luar kotak = batal juga
    modal.onclick = function (e) { if (e.target == modal) tutupModal("modalKeluar"); };
    document.body.appendChild(modal);
  }
  modal.classList.add("tampil");
}

// menandai menu yang sedang dibuka di navbar
function tandaiMenu() {
  var sekarang = window.location.pathname.split("/").pop(); // mis. "kamar.html"
  if (sekarang == "") sekarang = "index.html";
  var semua = document.querySelectorAll(".menu a");
  for (var i = 0; i < semua.length; i++) {
    if (semua[i].getAttribute("href") == sekarang) semua[i].classList.add("aktif");
  }
}

function logout() {
  localStorage.removeItem("kozzyLogin");
  userLogin = null;
  window.location.href = "index.html";
}

// pergi ke login, dan ingat mau kembali ke mana setelah berhasil
function pergiLogin(tujuan) {
  if (tujuan != null) localStorage.setItem("kozzyBalik", tujuan);
  window.location.href = "login.html";
}

// ====== 6. HALAMAN LOGIN / DAFTAR ======
function gantiTab(tab) {
  var lagiMasuk = tab == "masuk";
  document.getElementById("formMasuk").style.display = lagiMasuk ? "" : "none";
  document.getElementById("formDaftar").style.display = lagiMasuk ? "none" : "";
  document.getElementById("tabMasuk").className = lagiMasuk ? "aktif" : "";
  document.getElementById("tabDaftar").className = lagiMasuk ? "" : "aktif";
  document.getElementById("judulAuth").innerText = lagiMasuk ? "Masuk ke Kozzy" : "Buat akun baru";
  document.getElementById("loginSalah").innerText = "";
  document.getElementById("daftarSalah").innerText = "";
}

function prosesLogin() {
  var email = document.getElementById("loginEmail").value.trim().toLowerCase();
  var sandi = document.getElementById("loginPass").value.trim();
  var pesan = document.getElementById("loginSalah");

  if (email == "" || sandi == "") {
    pesan.innerText = "Email dan password wajib diisi.";
    return false; // false = form tidak me-reload halaman
  }
  for (var i = 0; i < pengguna.length; i++) {
    if (pengguna[i].email == email && pengguna[i].sandi == sandi) {
      loginBerhasil(pengguna[i]);
      return false;
    }
  }
  pesan.innerText = "Email atau password salah (password semua huruf kecil). Atau klik tombol demo di bawah.";
  return false;
}

function prosesDaftar() {
  var nama  = document.getElementById("daftarNama").value.trim();
  var email = document.getElementById("daftarEmail").value.trim().toLowerCase();
  var telp  = document.getElementById("daftarTelp").value.trim();
  var sandi = document.getElementById("daftarSandi").value.trim();
  var pesan = document.getElementById("daftarSalah");

  // validasi satu-satu supaya jelas salahnya di mana
  if (nama.length < 3) { pesan.innerText = "Nama minimal 3 karakter."; return false; }
  if (email.indexOf("@") < 1 || email.indexOf(".") < 0) { pesan.innerText = "Format email belum benar."; return false; }
  for (var i = 0; i < pengguna.length; i++) {
    if (pengguna[i].email == email) { pesan.innerText = "Email sudah terdaftar — coba masuk saja."; return false; }
  }
  if (telp.indexOf("08") != 0 || telp.length < 10) { pesan.innerText = "No. WhatsApp harus diawali 08 dan minimal 10 digit."; return false; }
  if (sandi.length < 6) { pesan.innerText = "Password minimal 6 karakter."; return false; }

  // daftar berhasil: simpan user baru, langsung login
  var userBaru = { id: pengguna.length + 1, nama: nama, email: email, sandi: sandi, telepon: telp, peran: "pencari" };
  pengguna.push(userBaru);
  simpanData();
  loginBerhasil(userBaru);
  return false;
}

function loginBerhasil(u) {
  userLogin = u;
  localStorage.setItem("kozzyLogin", u.id); // ingat sesi login
  // kalau tadi diminta login dari halaman lain, balik ke sana
  var tujuan = localStorage.getItem("kozzyBalik");
  if (tujuan != null) {
    localStorage.removeItem("kozzyBalik");
    window.location.href = tujuan;
  } else {
    window.location.href = "index.html";
  }
}

// ====== 7. HALAMAN KAMAR ======
function tampilkanKamar() {
  var html = "";
  for (var i = 0; i < kamarList.length; i++) {
    var k = kamarList[i];
    if (filterTipe != "semua" && k.tipe != filterTipe) continue; // kena filter, lewati
    var info = INFO_STATUS[k.status];
    html += "<div class='kartu-kamar" + (k.status == "terisi" ? " abu" : "") + "'>" +
      "<img src='kamar/" + k.kode + ".jpg' alt='Kamar " + k.kode + "'>" +
      "<div>" +
        "<div class='kartu-atas'>" +
          "<h3>" + k.kode + "</h3>" +
          "<span class='tipe " + (k.tipe == "putra" ? "tipe-putra" : "tipe-putri") + "'>" + k.tipe + "</span>" +
          "<span class='lencana " + info[0] + "'>" + info[1] + "</span>" +
        "</div>" +
        "<p>" + k.deskripsi + "</p>" +
        "<p class='fasilitas'>" + k.fasilitas + "</p>" +
        (k.status == "terisi" && k.terisiHingga != null ? "<p class='ket'>terisi s.d. " + formatTanggal(k.terisiHingga) + "</p>" : "") +
      "</div>" +
      "<div class='kartu-kanan'>" +
        "<div class='harga'>" + rupiah(k.harga) + " <small>/bulan</small></div>" +
        "<button class='tombol kecil' onclick=\"bukaDetail('" + k.kode + "')\">Detail</button>" +
      "</div>" +
    "</div>";
  }
  document.getElementById("daftarKamar").innerHTML =
    html == "" ? "<p>Waduh, belum ada kamar di kategori ini.</p>" : html;

  // tandai chip filter yang sedang aktif
  document.getElementById("chipSemua").className = "chip" + (filterTipe == "semua" ? " aktif" : "");
  document.getElementById("chipPutra").className = "chip" + (filterTipe == "putra" ? " aktif" : "");
  document.getElementById("chipPutri").className = "chip" + (filterTipe == "putri" ? " aktif" : "");
}

function bukaDetail(kode) {
  var k = cariKamar(kode);
  var info = INFO_STATUS[k.status];
  document.getElementById("dFoto").src = "kamar/" + k.kode + ".jpg";
  document.getElementById("dKode").innerText = k.kode + " — Kos " + (k.tipe == "putra" ? "Putra" : "Putri");
  document.getElementById("dHarga").innerText = rupiah(k.harga) + " / bulan";
  document.getElementById("dDeskripsi").innerText = k.deskripsi + " Harga sudah termasuk listrik & Wi-Fi.";
  document.getElementById("dFasilitas").innerText = k.fasilitas;
  var lencana = document.getElementById("dStatus");
  lencana.className = "lencana " + info[0];
  lencana.innerText = info[1];

  // Admin hanya dapat melihat detail kamar.
  // Tombol booking tidak ditampilkan untuk akun admin.
  if (userLogin != null && userLogin.peran == "admin") {
    document.getElementById("dAksi").innerHTML = "";
    document.getElementById("modalDetail").classList.add("tampil");
    return;
  }

  // tentukan tombol aksinya
  var aksi = "";
  if (k.status == "terisi") {
    aksi = "<button class='tombol penuh' disabled>Kamar Terisi</button>" +
      (k.terisiHingga != null ? "<p class='ket'>Kosong mulai " + formatTanggal(k.terisiHingga) + ".</p>" : "");
  } else if (k.status == "pemeliharaan") {
    aksi = "<button class='tombol penuh' disabled>Sedang Perbaikan</button>";
  } else if (punyaSewaAktif() != null) {
    aksi = "<button class='tombol penuh' disabled>Punya Sewa Aktif</button><p class='ket'>Satu akun hanya bisa menyewa satu kamar.</p>";
  } else if (adaPendingDiKamar(kode)) {
    aksi = "<button class='tombol penuh' disabled>Sedang Diproses</button><p class='ket'>Ada pengajuan lain untuk kamar ini.</p>";
  } else {
    aksi = "<button class='tombol hijau penuh' onclick=\"mulaiBooking('" + k.kode + "')\">Booking Kamar Ini →</button>";
  }
  document.getElementById("dAksi").innerHTML = aksi;
  document.getElementById("modalDetail").classList.add("tampil");
}

function mulaiBooking(kode) {
  tutupModal("modalDetail");
  // kalau belum login, ke halaman login dulu (nanti balik ke sini lagi)
  if (userLogin == null) {
    pergiLogin("booking.html?kode=" + kode);
    return;
  }
  window.location.href = "booking.html?kode=" + kode;
}

// ====== 8. HALAMAN BOOKING ======
function siapkanBooking() {
  var kode = ambilParam("kode");
  var blok = document.getElementById("blokBooking");
  var k = cariKamar(kode);

  // Admin tidak memiliki akses untuk melakukan booking kamar.
  if (userLogin != null && userLogin.peran == "admin") {
    blok.innerHTML = panelPesan("Booking tidak tersedia untuk admin",
      "Akun admin hanya dapat melihat detail kamar dan mengelola data kos.",
      "<a class='tombol hijau' href='kamar.html'>Kembali ke Kamar →</a>");
    return;
  }

  // semua pengecekan dikumpulkan di sini
  if (userLogin == null) {
    blok.innerHTML = panelPesan("Masuk dulu ya", "Halaman ini untuk pengguna yang sudah masuk.",
      "<button class='tombol hijau' onclick=\"pergiLogin('booking.html?kode=" + kode + "')\">Masuk →</button>");
    return;
  }
  if (k == null) {
    blok.innerHTML = panelPesan("Kamar tidak ditemukan", "Pilih dulu kamar yang mau kamu sewa.",
      "<a class='tombol hijau' href='kamar.html'>Lihat Kamar →</a>");
    return;
  }
  if (k.status != "tersedia") {
    blok.innerHTML = panelPesan("Kamar tidak tersedia", "Kamar " + k.kode + " sedang " +
      (k.status == "terisi" ? "terisi" : "diperbaiki") + ". Cari yang lain ya.",
      "<a class='tombol hijau' href='kamar.html'>Lihat Kamar Lain →</a>");
    return;
  }
  if (adaPendingDiKamar(kode)) {
    blok.innerHTML = panelPesan("Sedang diproses", "Ada pengajuan lain yang menunggu verifikasi untuk kamar ini.",
      "<a class='tombol hijau' href='kamar.html'>Lihat Kamar Lain →</a>");
    return;
  }
  var sewa = punyaSewaAktif();
  if (sewa != null) {
    blok.innerHTML = panelPesan("Kamu sudah punya sewa aktif",
      "Booking " + sewa.id + " masih berjalan. Satu akun hanya bisa menyewa satu kamar.",
      "<a class='tombol hijau' href='sewaku.html'>Lihat Sewaku →</a>");
    return;
  }

  // aman → tampilkan form
  kamarDipilih = kode;
  document.getElementById("infoKamarBooking").innerHTML =
    "<img src='kamar" + k.kode + ".jpg'>" +
    "<div><b style='font-size:18px'>Kamar " + k.kode + "</b>" +
    "<div class='ket'>Kos " + (k.tipe == "putra" ? "Putra" : "Putri") + " · " + k.fasilitas + "</div>" +
    "<div class='harga'>" + rupiah(k.harga) + " <small>/bulan</small></div></div>";
  var tgl = document.getElementById("fTanggal");
  tgl.value = tanggalGeser(0);
  tgl.min = tanggalGeser(0);
  document.getElementById("fDurasi").value = "1";
  hitungTotal();
}

function hitungTotal() {
  var k = cariKamar(kamarDipilih);
  var durasi = Number(document.getElementById("fDurasi").value);
  document.getElementById("totalHarga").innerText = rupiah(k.harga * durasi);
}

function pilihMetode(nama) {
  metodeDipilih = nama;
  var info = document.getElementById("infoBayar");
  info.style.display = "";
  info.innerText = "Kamu memilih " + nama + ". Bayar tepat sebesar total tagihan ke nomor di atas, lalu unggah buktinya (opsional di mode demo).";
}

function pilihBukti(input) {
  if (input.files.length == 0) { buktiBayar = null; return; }
  var file = input.files[0];
  var pembaca = new FileReader();
  pembaca.onload = function () { buktiBayar = pembaca.result; }; // disimpan sebagai gambar
  pembaca.readAsDataURL(file);
  document.getElementById("namaBukti").innerText = "Terpilih: " + file.name;
}

function kirimBooking() {
  var k = cariKamar(kamarDipilih);
  var tanggal = document.getElementById("fTanggal").value;
  var durasi = Number(document.getElementById("fDurasi").value);

  // validasi dulu
  if (tanggal == "" || tanggal < tanggalGeser(0)) { toast("Pilih tanggal mulai yang benar."); return; }
  if (metodeDipilih == null)                     { toast("Pilih metode pembayaran dulu."); return; }
  if (!document.getElementById("fSetuju").checked) { toast("Centang persetujuannya dulu ya."); return; }
  if (punyaSewaAktif() != null) { toast("Kamu sudah punya sewa aktif."); location.href = "sewaku.html"; return; }

  // simpan booking baru, status pending = menunggu verifikasi admin
  var idBaru = acakKode();
  bookingList.push({
    id: idBaru, userId: userLogin.id, kamar: k.kode, mulai: tanggal,
    durasi: durasi, status: "pending", metode: metodeDipilih, bukti: buktiBayar, alasan: null
  });
  simpanData(); // penting: simpan ke localStorage

  document.getElementById("sKode").innerText = idBaru;
  document.getElementById("sInfo").innerText =
    "Kamar " + k.kode + " · mulai " + formatTanggal(tanggal) + " · " + durasi +
    " bulan · " + rupiah(k.harga * durasi) + " via " + metodeDipilih;
  document.getElementById("modalSukses").classList.add("tampil");
}

// ====== 9. HALAMAN SEWAKU ======
function tampilkanSewaku() {
  var kotak = document.getElementById("isiSewaku");
  if (userLogin == null) {
    kotak.innerHTML = panelPesan("Masuk dulu ya", "Halaman ini untuk pengguna yang sudah masuk.",
      "<button class='tombol hijau' onclick=\"pergiLogin('sewaku.html')\">Masuk →</button>");
    return;
  }
  var html = "<span class='label-bagian'>SEWAKU</span>" +
    "<h1>Hai, " + userLogin.nama.split(" ")[0] + "</h1><p class='sub'>Status sewamu yang terbaru.</p>";
  var ada = false;
  for (var i = 0; i < bookingList.length; i++) {
    var b = bookingList[i];
    if (b.userId != userLogin.id) continue;
    ada = true;
    var k = cariKamar(b.kamar);
    var info = INFO_STATUS[b.status];
    html += "<div class='panel kartu-sewa'>" +
      "<div class='kartu-atas'><span class='lencana " + info[0] + "'>" + info[1] + "</span>" +
      "<span class='ket kode'>" + b.id + "</span></div>" +
      "<h2>Kamar " + k.kode + " — Kos " + (k.tipe == "putra" ? "Putra" : "Putri") + "</h2>" +
      "<div class='info-grid'>" +
        "<div><small>Mulai sewa</small><b>" + formatTanggal(b.mulai) + "</b></div>" +
        "<div><small>Durasi</small><b>" + b.durasi + " bulan</b></div>" +
        "<div><small>Total</small><b>" + rupiah(k.harga * b.durasi) + "</b></div>" +
        "<div><small>Metode</small><b>" + b.metode + "</b></div>" +
        "<div><small>Bukti bayar</small>" + (b.bukti != null
          ? "<button class='tombol kecil' onclick=\"lihatBukti('" + b.id + "')\">Lihat</button>"
          : "<b>—</b>") + "</div>" +
      "</div>";
    if (b.status == "pending")
      html += "<div class='pesan tunggu'><b>Menunggu verifikasi admin.</b> Pembayaranmu sudah masuk antrean — biasanya kurang dari 1×24 jam.</div>";
    if (b.status == "approved")
      html += "<div class='pesan sukses'><b>Selamat, kamarmu aktif!</b> Terisi atas nama kamu sampai " + formatTanggal(k.terisiHingga) + ".</div>";
    if (b.status == "rejected")
      html += "<div class='pesan gagal'><b>Ditolak admin.</b> Alasan: " + b.alasan + "</div>" +
        "<a class='tombol kecil' href='kamar.html'>Cari Kamar Lain →</a>";
    html += "</div>";
  }
  if (!ada) {
    html += panelPesan("Belum ada sewa", "Kamarnya masih banyak yang kosong, nih.",
      "<a class='tombol hijau' href='kamar.html'>Cari Kamar →</a>");
  }
  kotak.innerHTML = html;
}

// ====== 9.5 HALAMAN PROFIL ======
function tampilkanProfil() {
  var kotak = document.getElementById("isiProfil");
  if (userLogin == null) {
    kotak.innerHTML = panelPesan("Masuk dulu ya", "Halaman ini untuk pengguna yang sudah masuk.",
      "<a class='tombol hijau' href='login.html'>Masuk →</a>");
    return;
  }

  // hitung statistik akun ini dulu
  var totalBooking = 0, totalDibayar = 0;
  for (var i = 0; i < bookingList.length; i++) {
    var b = bookingList[i];
    if (b.userId != userLogin.id) continue;
    totalBooking++;
    if (b.status == "approved") { // baru dihitung kalau sudah disetujui admin
      var k = cariKamar(b.kamar);
      totalDibayar += k.harga * b.durasi;
    }
  }
  var sewa = punyaSewaAktif();
  var admin = userLogin.peran == "admin";

  var html = "<span class='label-bagian'>AKUN SAYA</span>" +
    "<h1>Profilku</h1><p class='sub'>Data akun dan riwayat sewamu.</p>";

  // kartu identitas
  html += "<div class='panel profil-kartu'>" +
    "<span class='avatar-besar'>" + userLogin.nama.charAt(0).toUpperCase() + "</span>" +
    "<div>" +
      "<h2 style='font-size:24px'>" + userLogin.nama + "</h2>" +
      "<p class='ket'>" + userLogin.email + "</p>" +
      "<p class='ket'>No. WhatsApp: " + (userLogin.telepon != null ? userLogin.telepon : "—") + "</p><br>" +
      "<span class='lencana " + (admin ? "merah" : "hijau") + "'>" + (admin ? "Admin Kozzy" : "Pencari Kos") + "</span>" +
    "</div></div>";

  // statistik singkat
  html += "<div class='stat-baris'>" +
    "<div><b>" + totalBooking + "</b><span>total booking</span></div>" +
    "<div><b>" + rupiah(totalDibayar) + "</b><span>sudah dibayar</span></div>" +
    "<div><b>" + (sewa != null ? "Ada" : "Tidak") + "</b><span>sewa aktif</span></div>" +
  "</div>";

  // sewa aktif sekarang
  html += "<h2 class='judul-admin'>Sewa Aktif</h2>";
  if (sewa != null) {
    var ks = cariKamar(sewa.kamar);
    var info = INFO_STATUS[sewa.status];
    html += "<div class='panel kartu-sewa'>" +
      "<div class='kartu-atas'><span class='lencana " + info[0] + "'>" + info[1] + "</span>" +
      "<span class='ket kode'>" + sewa.id + "</span></div>" +
      "<h2 style='font-size:20px;margin:8px 0'>Kamar " + ks.kode + "</h2>" +
      (sewa.status == "approved"
        ? "<p class='ket'>Terisi atas nama kamu sampai " + formatTanggal(ks.terisiHingga) + ".</p>"
        : "<p class='ket'>Pengajuanmu masih menunggu verifikasi admin.</p>") +
      "<a class='tombol kecil' href='sewaku.html'>Buka Sewaku →</a>" +
    "</div>";
  } else {
    html += panelPesan("Belum ada sewa aktif", "Mau cari kamar? Masih ada yang kosong, nih.",
      "<a class='tombol hijau' href='kamar.html'>Cari Kamar →</a>");
  }

  // riwayat semua booking (loop dari belakang = yang terbaru di atas)
  html += "<h2 class='judul-admin'>Riwayat Booking</h2>";
  var adaRiwayat = false;
  for (var r = bookingList.length - 1; r >= 0; r--) {
    var b2 = bookingList[r];
    if (b2.userId != userLogin.id) continue;
    adaRiwayat = true;
    var k2 = cariKamar(b2.kamar);
    var st = INFO_STATUS[b2.status];
    html += "<div class='riwayat-baris'>" +
      "<div><b>" + b2.id + "</b> · Kamar " + k2.kode +
      " <span class='ket'>· " + formatTanggal(b2.mulai) + " · " + b2.durasi + " bln · " +
      rupiah(k2.harga * b2.durasi) + "</span></div>" +
      "<span class='lencana " + st[0] + "'>" + st[1] + "</span>" +
    "</div>";
  }
  if (!adaRiwayat) html += "<p class='sub'>Belum pernah booking sama sekali.</p>";

  // tombol keluar (dengan konfirmasi juga)
  html += "<h2 class='judul-admin'>Akun</h2>" +
    "<button class='tombol merah' onclick='tanyaLogout()'>Keluar dari akun</button>";

  kotak.innerHTML = html;
}

// ====== 10. HALAMAN ADMIN ======
function tampilkanAdmin() {
  var kotak = document.getElementById("isiAdmin");
  if (userLogin == null) {
    kotak.innerHTML = panelPesan("Masuk dulu ya", "Halaman ini khusus akun admin.",
      "<a class='tombol hijau' href='login.html'>Masuk</a>");
    return;
  }
  if (userLogin.peran != "admin") {
    kotak.innerHTML = panelPesan("Khusus admin",
      "Halaman ini hanya untuk akun admin. Coba masuk dengan admin@kozzy.id / admin123.",
      "<a class='tombol hijau' href='login.html'>Ganti Akun</a>");
    return;
  }

  // hitung statistik dulu
  var jTersedia = 0, jTerisi = 0, jPending = 0;
  for (var i = 0; i < kamarList.length; i++) {
    if (kamarList[i].status == "tersedia") jTersedia++;
    if (kamarList[i].status == "terisi") jTerisi++;
  }
  for (var j = 0; j < bookingList.length; j++) if (bookingList[j].status == "pending") jPending++;

  var html = "<span class='label-bagian'>ADMIN</span><h1>Panel Admin</h1>" +
    "<p class='sub'>Setujui / tolak pengajuan booking, dan atur status kamar.</p>" +
    "<div class='stat-baris'>" +
      "<div><b>" + kamarList.length + "</b><span>total kamar</span></div>" +
      "<div><b>" + jTersedia + "</b><span>tersedia</span></div>" +
      "<div><b>" + jTerisi + "</b><span>terisi</span></div>" +
      "<div><b>" + jPending + "</b><span>menunggu</span></div>" +
    "</div>" +
    "<h2 class='judul-admin'>Pengajuan Menunggu Verifikasi</h2>";

  // kartu pengajuan yang pending
  var adaPending = false;
  for (var p = 0; p < bookingList.length; p++) {
    var b = bookingList[p];
    if (b.status != "pending") continue;
    adaPending = true;
    var penghuni = cariPengguna(b.userId);
    var kamarB = cariKamar(b.kamar);
    html += "<div class='admin-kartu'>" +
      "<div><b>" + penghuni.nama + "</b>" +
        "<div class='muted'>" + penghuni.email + " · kamar " + b.kamar + " (" + rupiah(kamarB.harga) + "/bln)</div>" +
        "<div class='muted'>" + b.id + " · mulai " + formatTanggal(b.mulai) + " · " + b.durasi + " bln · via " + b.metode +
        (b.bukti != null
          ? " · <button class='tombol kecil' onclick=\"lihatBukti('" + b.id + "')\">lihat bukti</button>"
          : " · tanpa bukti") + "</div>" +
      "</div>" +
      "<div class='admin-aksi'>" +
        "<button class='tombol kecil hijau' onclick=\"setujui('" + b.id + "')\">✓ Setujui</button>" +
        "<button class='tombol kecil merah' onclick=\"tolak('" + b.id + "')\">× Tolak</button>" +
      "</div></div>";
  }
  if (!adaPending) html += "<p class='sub'>Tidak ada pengajuan menunggu. Mantap!</p>";

  // riwayat keputusan
  html += "<h2 class='judul-admin'>Riwayat Keputusan</h2>";
  var adaRiwayat = false;
  for (var r = 0; r < bookingList.length; r++) {
    var b2 = bookingList[r];
    if (b2.status == "pending") continue;
    adaRiwayat = true;
    var info = INFO_STATUS[b2.status];
    var penghuni2 = cariPengguna(b2.userId);
    html += "<div class='admin-kartu'>" +
      "<div><b>" + b2.id + " · Kamar " + b2.kamar + "</b>" +
      "<div class='muted'>" + penghuni2.nama + " · " + formatTanggal(b2.mulai) + " · " + b2.durasi + " bln" +
      (b2.alasan != null ? " · alasan tolak: " + b2.alasan : "") + "</div></div>" +
      "<span class='lencana " + info[0] + "'>" + info[1] + "</span></div>";
  }
  if (!adaRiwayat) html += "<p class='sub'>Belum ada keputusan.</p>";

  // pengaturan status tiap kamar
  html += "<h2 class='judul-admin'>Status Kamar</h2>";
  for (var s = 0; s < kamarList.length; s++) {
    var k = kamarList[s];
    html += "<div class='baris-kamar'><b>" + k.kode + "</b>" +
      "<span class='tipe " + (k.tipe == "putra" ? "tipe-putra" : "tipe-putri") + "'>" + k.tipe + "</span>" +
      "<span class='ket'>" + rupiah(k.harga) + "/bln</span>" +
      (k.terisiHingga != null ? "<span class='ket'>terisi s.d. " + formatTanggal(k.terisiHingga) + "</span>" : "") +
      "<select class='masukan' onchange=\"ubahStatusKamar('" + k.kode + "', this.value)\">" +
        "<option value='tersedia'"     + (k.status == "tersedia"     ? " selected" : "") + ">Tersedia</option>" +
        "<option value='terisi'"       + (k.status == "terisi"       ? " selected" : "") + ">Terisi</option>" +
        "<option value='pemeliharaan'" + (k.status == "pemeliharaan" ? " selected" : "") + ">Perbaikan</option>" +
      "</select></div>";
  }

  // tombol reset (berguna sebelum presentasi)
  html += "<h2 class='judul-admin'>Lainnya</h2>" +
    "<button class='tombol kecil' id='tombolReset' onclick='resetData()'>Reset Data Demo</button>" +
    "<p class='ket'>Mengembalikan semua data ke kondisi awal.</p>";
  kotak.innerHTML = html;
}

// ====== 11. AKSI ADMIN ======
function setujui(id) {
  var b = cariBooking(id);
  var k = cariKamar(b.kamar);
  if (k.status != "tersedia") { toast("Kamar sudah tidak tersedia — tolak pengajuannya."); return; }
  b.status = "approved";
  k.status = "terisi";
  k.terisiHingga = tambahBulan(b.mulai, b.durasi); // kamar terisi selama masa sewa
  simpanData();
  toast("Disetujui! Kamar " + k.kode + " terisi sampai " + formatTanggal(k.terisiHingga) + ".");
  tampilkanAdmin();
}
function tolak(id) {
  idDitolak = id;
  document.getElementById("tolakAlasan").value = "";
  document.getElementById("modalTolak").classList.add("tampil");
}
function konfirmasiTolak() {
  var alasan = document.getElementById("tolakAlasan").value;
  if (alasan.length < 5) { toast("Tulis alasan penolakannya dulu (min. 5 karakter)."); return; }
  var b = cariBooking(idDitolak);
  b.status = "rejected";
  b.alasan = alasan;
  simpanData();
  tutupModal("modalTolak");
  toast("Booking " + b.id + " ditolak. Kamar tetap tersedia.");
  tampilkanAdmin();
}
function ubahStatusKamar(kode, status) {
  var k = cariKamar(kode);
  k.status = status;
  if (status == "terisi" && k.terisiHingga == null) k.terisiHingga = tambahBulan(tanggalGeser(0), 1);
  if (status != "terisi") k.terisiHingga = null;
  simpanData();
  toast("Kamar " + kode + " sekarang berstatus: " + status + ".");
}
function lihatBukti(id) {
  var b = cariBooking(id);
  document.getElementById("buktiImg").src = b.bukti;
  document.getElementById("modalBukti").classList.add("tampil");
}

// ====== 12. TOAST, MODAL & PANEL PESAN ======
function toast(pesan) {
  var el = document.createElement("div");
  el.className = "toast";
  el.innerText = pesan;
  document.getElementById("toastWrap").appendChild(el);
  setTimeout(function () { el.remove(); }, 3200);
}
function tutupModal(id) {
  var el = document.getElementById(id);
  if (el != null) el.classList.remove("tampil");
}
function panelPesan(judul, teks, tombolHtml) {
  return "<div class='panel-pesan'><h2>" + judul + "</h2><p>" + teks + "</p>" + tombolHtml + "</div>";
}

// wadah toast harus ada di semua halaman — dibuat otomatis kalau belum ada
(function pasangToast() {
  if (document.getElementById("toastWrap") == null) {
    var d = document.createElement("div");
    d.id = "toastWrap";
    document.body.appendChild(d);
  }
})();

// ====== 13. JALANKAN SAAT HALAMAN DIBUKA ======
ambilData();
perbaruiNavbar();
tandaiMenu();
if (document.getElementById("isiProfil") != null) tampilkanProfil();

// setiap halaman punya elemen berbeda — jalankan fungsinya hanya kalau elemennya ada
if (document.getElementById("daftarKamar") != null) {
  var t = ambilParam("tipe");
  filterTipe = (t == "putra" || t == "putri") ? t : "semua";
  tampilkanKamar();
}
if (document.getElementById("blokBooking") != null) siapkanBooking();
if (document.getElementById("isiSewaku") != null) tampilkanSewaku();
if (document.getElementById("isiAdmin") != null) tampilkanAdmin();
if (document.getElementById("formMasuk") != null && userLogin != null) {
  window.location.href = "index.html"; // sudah login, tidak perlu ke halaman login lagi
}