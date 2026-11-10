/* ============================================
   KOZZY — script.js
   Frontend UI Helpers (Modal, Toast, Booking Form)
   Semua data dan sesi kini dikelola oleh PHP & MySQL.
============================================ */

// ====== 1. MODAL HELPERS ======
function tutupModal(id) {
  var el = document.getElementById(id);
  if (el != null) el.classList.remove("tampil");
}

function tanyaLogout() {
  var modal = document.getElementById("modalKeluar");
  if (modal != null) {
    modal.classList.add("tampil");
  }
}

function lihatBukti(srcGambar) {
  var img = document.getElementById("buktiImg");
  var modal = document.getElementById("modalBukti");
  if (img != null && modal != null) {
    img.src = srcGambar;
    modal.classList.add("tampil");
  }
}

// ====== 2. TOAST NOTIFICATION ======
function toast(pesan) {
  var wrap = document.getElementById("toastWrap");
  if (wrap == null) {
    wrap = document.createElement("div");
    wrap.id = "toastWrap";
    document.body.appendChild(wrap);
  }
  var el = document.createElement("div");
  el.className = "toast";
  el.innerText = pesan;
  wrap.appendChild(el);
  setTimeout(function () {
    el.remove();
  }, 3200);
}

// ====== 3. HELPER FORM BOOKING ======
function hitungTotal() {
  var elHarga = document.getElementById("hargaKamarPerBulan");
  var elDurasi = document.getElementById("fDurasi");
  var elTotal = document.getElementById("totalHarga");

  if (elHarga != null && elDurasi != null && elTotal != null) {
    var harga = Number(elHarga.value);
    var durasi = Number(elDurasi.value);
    var total = harga * durasi;
    elTotal.innerText = "Rp " + total.toLocaleString("id-ID");
  }
}

function pilihMetode(nama) {
  var info = document.getElementById("infoBayar");
  if (info != null) {
    info.style.display = "";
    info.innerText = "Kamu memilih " + nama + ". Bayar tepat sebesar total tagihan ke nomor di atas, lalu unggah buktinya.";
  }
}

function pilihBukti(input) {
  var namaEl = document.getElementById("namaBukti");
  if (input.files && input.files.length > 0) {
    namaEl.innerText = "Terpilih: " + input.files[0].name;
  } else {
    namaEl.innerText = "";
  }
}

// ====== 4. HELPER DETAIL KAMAR ======
function bukaDetailKamar(k) {
  var modal = document.getElementById("modalDetail");
  if (!modal) return;

  var dFoto = document.getElementById("dFoto");
  dFoto.src = "kamar/" + k.kode_kamar + ".jpg";
  dFoto.onerror = function() {
    this.onerror = null;
    this.src = k.tipe === "putra" ? "putra.jpg" : "putri.jpg";
  };
  document.getElementById("dKode").innerText = k.kode_kamar + " — Kos " + (k.tipe == "putra" ? "Putra" : "Putri");
  document.getElementById("dHarga").innerText = "Rp " + Number(k.harga).toLocaleString("id-ID") + " / bulan";
  document.getElementById("dDeskripsi").innerText = k.deskripsi + " Harga sudah termasuk listrik & Wi-Fi.";
  document.getElementById("dFasilitas").innerText = k.daftar_fasilitas || "—";

  var infoStatus = {
    tersedia:     ["hijau",  "Tersedia"],
    terisi:       ["merah",  "Terisi"],
    pemeliharaan: ["kuning", "Perbaikan"]
  };
  var info = infoStatus[k.status] || ["abu", k.status];
  var lencana = document.getElementById("dStatus");
  lencana.className = "lencana " + info[0];
  lencana.innerText = info[1];

  var aksiEl = document.getElementById("dAksi");
  aksiEl.innerHTML = k.tombol_aksi || "";

  modal.classList.add("tampil");
}

// ====== 5. HELPER TAB LOGIN ======
function gantiTab(tab) {
  var lagiMasuk = tab == "masuk";
  var fMasuk = document.getElementById("formMasuk");
  var fDaftar = document.getElementById("formDaftar");
  var tMasuk = document.getElementById("tabMasuk");
  var tDaftar = document.getElementById("tabDaftar");
  var jAuth = document.getElementById("judulAuth");

  if (fMasuk) fMasuk.style.display = lagiMasuk ? "" : "none";
  if (fDaftar) fDaftar.style.display = lagiMasuk ? "none" : "";
  if (tMasuk) tMasuk.className = lagiMasuk ? "aktif" : "";
  if (tDaftar) tDaftar.className = lagiMasuk ? "" : "aktif";
  if (jAuth) jAuth.innerText = lagiMasuk ? "Masuk ke Kozzy" : "Buat akun baru";
}
