// ====== MODAL HELPERS ======
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

// ====== TOAST NOTIFICATION ======
function toast(pesan) {
  var wrap = document.getElementById("toastWrap");
  if (wrap == null) {
    wrap = document.createElement("div");
    wrap.id = "toastWrap";
    document.body.appendChild(wrap);
  }
  var el = document.createElement("div");
  el.className = "toast";
  el.textContent = pesan;
  wrap.appendChild(el);
  setTimeout(function () {
    el.remove();
  }, 3200);
}

// ====== HELPER FORM BOOKING ======
function hitungTotal() {
  var elHarga = document.getElementById("hargaKamarPerBulan");
  var elDurasi = document.getElementById("fDurasi");
  var elTotal = document.getElementById("totalHarga");

  if (elHarga != null && elDurasi != null) {
    var harga = Number(elHarga.value);
    var durasi = Number(elDurasi.value);
    var total = harga * durasi;
    var teks = "Rp " + total.toLocaleString("id-ID");
    if (elTotal != null) elTotal.textContent = teks;

    var daftarNominal = document.querySelectorAll(".nominal-bayar");
    for (var i = 0; i < daftarNominal.length; i++) {
      daftarNominal[i].textContent = teks;
    }
  }
  perbaruiRingkasanBooking();
}

var BULAN_ID = ["Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Agu", "Sep", "Okt", "Nov", "Des"];

function formatTglId(d) {
  return d.getDate() + " " + BULAN_ID[d.getMonth()] + " " + d.getFullYear();
}

function tambahBulanJs(d, n) {
  var hari = d.getDate();
  var h = new Date(d.getFullYear(), d.getMonth() + n, 1);
  var maks = new Date(h.getFullYear(), h.getMonth() + 1, 0).getDate();
  h.setDate(Math.min(hari, maks));
  return h;
}

function perbaruiRingkasanBooking() {
  var rb = document.getElementById("infoKamarBooking");
  if (!rb) return;
  var tgl = document.getElementById("fTanggal");
  var dur = document.getElementById("fDurasi");
  var elMulai = document.getElementById("rbMulai");
  var elDurasi = document.getElementById("rbDurasi");
  var elSampai = document.getElementById("rbSampai");
  var elMetode = document.getElementById("rbMetode");

  var n = dur ? Number(dur.value) : 1;
  if (elDurasi) elDurasi.textContent = n + " bulan";

  var bagian = tgl && tgl.value ? tgl.value.split("-") : null;
  if (bagian && bagian.length === 3) {
    var mulai = new Date(Number(bagian[0]), Number(bagian[1]) - 1, Number(bagian[2]));
    if (elMulai) elMulai.textContent = formatTglId(mulai);
    if (elSampai) elSampai.textContent = formatTglId(tambahBulanJs(mulai, n));
  }

  if (elMetode) {
    var kat = document.querySelector('input[name="kategori_bayar"]:checked');
    var label = "Belum dipilih";
    if (kat && kat.value === "qris") {
      label = "QRIS";
    } else if (kat) {
      var sel = document.getElementById(kat.value === "bank" ? "selBank" : "selEwallet");
      if (sel && sel.value !== "") label = sel.options[sel.selectedIndex].getAttribute("data-nama") || sel.options[sel.selectedIndex].text;
    }
    elMetode.textContent = label;
  }
}

// Kategori pembayaran: bank / ewallet / qris
var PANEL_BAYAR = { bank: "panelBank", ewallet: "panelEwallet", qris: "panelQris" };
var KONTROL_BAYAR = { bank: "selBank", ewallet: "selEwallet", qris: "inQris" };

function pilihKategoriBayar(kategori) {
  for (var k in PANEL_BAYAR) {
    var aktif = (k === kategori);
    var panel = document.getElementById(PANEL_BAYAR[k]);
    var kontrol = document.getElementById(KONTROL_BAYAR[k]);
    if (panel != null) panel.style.display = aktif ? "" : "none";
    if (kontrol != null) kontrol.disabled = !aktif;
  }
  var kartu = document.querySelectorAll(".kat-kartu");
  for (var i = 0; i < kartu.length; i++) {
    var radio = kartu[i].querySelector("input");
    kartu[i].classList.toggle("aktif", radio != null && radio.checked);
  }
}

// Menampilkan rincian tujuan pembayaran untuk bank / e-wallet yang dipilih di dropdown
function pilihRekening(select, idKotak) {
  var kotak = document.getElementById(idKotak);
  if (kotak == null) return;
  kotak.innerHTML = "";

  var opsi = select.options[select.selectedIndex];
  if (select.value === "" || opsi == null) {
    kotak.style.display = "none";
    return;
  }

  function baris(kelas, teks) {
    var el = document.createElement("div");
    el.className = kelas;
    el.textContent = teks;
    kotak.appendChild(el);
    return el;
  }

  baris("judul", opsi.getAttribute("data-nama"));
  baris("nomor", opsi.getAttribute("data-tujuan"));
  var nominal = baris("catatan", "Transfer tepat sebesar ");
  var tebal = document.createElement("b");
  tebal.className = "nominal-bayar";
  nominal.appendChild(tebal);
  nominal.appendChild(document.createTextNode(", lalu unggah bukti pembayarannya di bawah."));

  kotak.style.display = "";
  hitungTotal();
}

function pilihBukti(input) {
  var namaEl = document.getElementById("namaBukti");
  if (input.files && input.files.length > 0) {
    namaEl.textContent = "Terpilih: " + input.files[0].name;
  } else {
    namaEl.textContent = "";
  }
}

// ====== HELPER DETAIL KAMAR ======
var galeriFoto = [];
var galeriIdx = 0;
var galeriFallback = "putra.jpg";

function tampilFotoGaleri() {
  var img = document.getElementById("dFoto");
  if (!img) return;
  var jml = galeriFoto.length;


  var ganti = img.getAttribute("src") !== galeriFoto[galeriIdx];
  if (ganti) img.classList.add("muat");
  img.onload = function () { this.classList.remove("muat"); };
  img.onerror = function () { this.onerror = null; this.classList.remove("muat"); this.src = galeriFallback; };
  img.src = galeriFoto[galeriIdx];
  setTimeout(function () { img.classList.remove("muat"); }, 1200);

  var banyak = jml > 1;
  var prev = document.getElementById("dPrev"), next = document.getElementById("dNext");
  var hitung = document.getElementById("dHitung"), titik = document.getElementById("dTitik");
  if (prev) prev.style.display = banyak ? "" : "none";
  if (next) next.style.display = banyak ? "" : "none";
  if (hitung) { hitung.style.display = banyak ? "" : "none"; hitung.textContent = (galeriIdx + 1) + " / " + jml; }
  if (titik) {
    titik.innerHTML = "";
    if (banyak) {
      for (var i = 0; i < jml; i++) {
        var b = document.createElement("button");
        b.type = "button";
        b.setAttribute("aria-label", "Foto " + (i + 1));
        if (i === galeriIdx) b.className = "aktif";
        b.onclick = (function (n) { return function () { galeriIdx = n; tampilFotoGaleri(); }; })(i);
        titik.appendChild(b);
      }
    }
  }
}

function geserFoto(arah) {
  if (galeriFoto.length < 2) return;
  galeriIdx = (galeriIdx + arah + galeriFoto.length) % galeriFoto.length;
  tampilFotoGaleri();
}

(function () {
  var mulaiX = null;
  document.addEventListener("touchstart", function (e) {
    var g = document.getElementById("dGaleri");
    mulaiX = (g && g.contains(e.target)) ? e.touches[0].clientX : null;
  }, { passive: true });
  document.addEventListener("touchend", function (e) {
    if (mulaiX === null) return;
    var selisih = e.changedTouches[0].clientX - mulaiX;
    if (Math.abs(selisih) > 40) geserFoto(selisih < 0 ? 1 : -1);
    mulaiX = null;
  }, { passive: true });
  document.addEventListener("keydown", function (e) {
    var m = document.getElementById("modalDetail");
    if (!m || !m.classList.contains("tampil")) return;
    if (e.key === "ArrowLeft") geserFoto(-1);
    else if (e.key === "ArrowRight") geserFoto(1);
    else if (e.key === "Escape") tutupModal("modalDetail");
  });
})();

function bukaDetailKamar(k) {
  var modal = document.getElementById("modalDetail");
  if (!modal) return;

  galeriFoto = (k.foto && k.foto.length) ? k.foto : ["kamar/" + k.kode_kamar + ".jpg"];
  galeriIdx = 0;
  galeriFallback = k.tipe === "putra" ? "putra.jpg" : "putri.jpg";
  tampilFotoGaleri();
  document.getElementById("dKode").textContent = k.kode_kamar + " — Kos " + (k.tipe == "putra" ? "Putra" : "Putri");
  document.getElementById("dHarga").textContent = "Rp " + Number(k.harga).toLocaleString("id-ID") + " / bulan";
  document.getElementById("dDeskripsi").textContent = k.deskripsi + " Harga sudah termasuk listrik & Wi-Fi.";
  document.getElementById("dFasilitas").textContent = k.daftar_fasilitas || "—";

  var infoStatus = {
    tersedia:     ["hijau",  "Tersedia"],
    terisi:       ["merah",  "Terisi"],
    pemeliharaan: ["kuning", "Perbaikan"]
  };
  var info = infoStatus[k.status] || ["abu", k.status];
  if (k.pending) info = ["kuning", "Menunggu verifikasi"];
  var lencana = document.getElementById("dStatus");
  lencana.className = "lencana " + info[0];
  lencana.textContent = info[1];

  // Keterangan jika kamar sedang menunggu verifikasi pengajuan
  var infoEl = document.getElementById("dInfo");
  if (infoEl) {
    infoEl.textContent = k.info_pending || "";
    infoEl.style.display = k.info_pending ? "" : "none";
  }

  var tingkatEl = document.getElementById("dTingkat");
  if (tingkatEl) {
    tingkatEl.className = "tingkat t-" + (k.tingkat_kode || "ekonomi");
    tingkatEl.textContent = k.tingkat || "";
    tingkatEl.style.display = k.tingkat ? "" : "none";
  }

  var aksiEl = document.getElementById("dAksi");
  aksiEl.innerHTML = k.tombol_aksi || "";

  modal.classList.add("tampil");
}

// ====== HELPER TAB LOGIN ======
function gantiTab(tab) {
  var lagiMasuk = tab == "masuk";
  var fMasuk = document.getElementById("formMasuk");
  var fDaftar = document.getElementById("formDaftar");
  var tMasuk = document.getElementById("tabMasuk");
  var tDaftar = document.getElementById("tabDaftar");
  var jAuth = document.getElementById("judulAuth");
  var sAuth = document.getElementById("subAuth");

  if (fMasuk) fMasuk.style.display = lagiMasuk ? "" : "none";
  if (fDaftar) fDaftar.style.display = lagiMasuk ? "none" : "";
  if (tMasuk) tMasuk.className = lagiMasuk ? "aktif" : "";
  if (tDaftar) tDaftar.className = lagiMasuk ? "" : "aktif";
  if (jAuth) jAuth.textContent = lagiMasuk ? "Masuk ke Kozzy" : "Buat akun baru";
  if (sAuth) sAuth.textContent = lagiMasuk ? "Masuk dulu untuk bisa booking kamar." : "Daftar sekali, lalu booking kamar kapan saja.";

  var kartu = document.getElementById("kartuAuth");
  if (kartu) kartu.style.maxWidth = lagiMasuk ? "480px" : "600px";
}

// ====== HELPER FORM DATA DIRI ======
function gantiLabelInstansi() {

  var radio = document.querySelector('input[name="status_penghuni"]:checked');
  var select = document.getElementById("profStatus");
  var nilai = radio ? radio.value : (select ? select.value : "");

  var label = document.getElementById("labelInstansi");
  var input = document.getElementById("daftarInstansi") || document.getElementById("profInstansi");
  if (!label || !input) return;

  var teks = {
    mahasiswa: ["Nama kampus", "cth. Universitas Riau"],
    pekerja:   ["Tempat kerja", "cth. PT Riau Makmur"],
    lainnya:   ["Instansi (opsional)", "boleh dikosongkan"]
  };
  var pilih = teks[nilai] || ["Kampus / tempat kerja", "cth. Universitas Riau"];
  label.textContent = pilih[0];
  input.placeholder = pilih[1];
}

function tampilSandi(tampil) {
  var ids = ["daftarSandi", "daftarSandi2"];
  for (var i = 0; i < ids.length; i++) {
    var el = document.getElementById(ids[i]);
    if (el) el.type = tampil ? "text" : "password";
  }
}


// ====== GERAK HALUS ======
function kurangiGerak() {
  return !!(window.matchMedia && window.matchMedia("(prefers-reduced-motion: reduce)").matches);
}

function aktifkanReveal() {
  if (!("IntersectionObserver" in window) || kurangiGerak()) return;
  var daftar = document.querySelectorAll(".kartu-kamar, .langkah, .pilih-kartu, .kartu-keluhan, .kartu-sewa");
  var pengamat = new IntersectionObserver(function (entri) {
    entri.forEach(function (e) {
      if (e.isIntersecting) { e.target.classList.add("masuk"); pengamat.unobserve(e.target); }
    });
  }, { threshold: 0.1, rootMargin: "0px 0px -30px 0px" });

  for (var i = 0; i < daftar.length; i++) {
    var el = daftar[i];
    if (el.getBoundingClientRect().top < window.innerHeight - 30) continue;
    var urutan = Array.prototype.indexOf.call(el.parentNode.children, el);
    el.style.setProperty("--tunda", ((urutan % 3) * 80) + "ms");
    el.classList.add("reveal");
    pengamat.observe(el);
  }
}

// Angka statistik di hero menghitung naik dari 0
function hitungNaikHero() {
  if (!window.requestAnimationFrame || kurangiGerak()) return;
  var daftar = document.querySelectorAll(".hero-stat b");
  Array.prototype.forEach.call(daftar, function (el) {
    var m = el.textContent.trim().match(/^(Rp\s*)?([\d.]+)$/);
    if (!m) return;
    var akhir = parseInt(m[2].replace(/\./g, ""), 10), awalan = m[1] || "";
    if (!akhir) return;
    var lama = 900, mulai = null;
    function langkah(waktu) {
      if (mulai === null) mulai = waktu;
      var p = Math.min((waktu - mulai) / lama, 1);
      var nilai = Math.round(akhir * (1 - Math.pow(1 - p, 3)));
      el.textContent = awalan + nilai.toLocaleString("id-ID");
      if (p < 1) window.requestAnimationFrame(langkah);
    }
    el.textContent = awalan + "0";
    window.requestAnimationFrame(langkah);
  });
}

document.addEventListener("DOMContentLoaded", function () {
  aktifkanReveal();
  hitungNaikHero();

  // Ringkasan booking mengikuti isian formulir
  var ids = ["fTanggal", "fDurasi", "selBank", "selEwallet"];
  ids.forEach(function (id) {
    var el = document.getElementById(id);
    if (el) { el.addEventListener("change", perbaruiRingkasanBooking); el.addEventListener("input", perbaruiRingkasanBooking); }
  });
  Array.prototype.forEach.call(document.querySelectorAll('input[name="kategori_bayar"]'), function (r) {
    r.addEventListener("change", perbaruiRingkasanBooking);
  });
  perbaruiRingkasanBooking();
});
