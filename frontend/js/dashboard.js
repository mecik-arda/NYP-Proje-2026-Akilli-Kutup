document.addEventListener('DOMContentLoaded', () => {
  initSidebar();
  initTheme();
  initSearch();
  initNotifications();
  initFullscreen();
  initMobileMenu();
  animateStats();
  renderDonutChart();
  renderBarChart();
  renderRecentBooks();
  renderPopularBooks();
  renderMembersTable();
  renderBookGrid();
  renderAssetGrid();
  initAddBookModal();
  initAIChat();
});

const sampleBooks = [
  { id: 1, baslik: 'Kürk Mantolu Madonna', yazar: 'Sabahattin Ali', kategori: 'Roman', yil: 1943, durum: 'mevcut', puan: 4.8, odunc: 142 },
  { id: 2, baslik: 'Masumiyet Müzesi', yazar: 'Orhan Pamuk', kategori: 'Roman', yil: 2008, durum: 'mevcut', puan: 4.5, odunc: 118 },
  { id: 3, baslik: 'İstanbul Hatırası', yazar: 'Ahmet Ümit', kategori: 'Polisiye', yil: 2010, durum: 'odunc', puan: 4.3, odunc: 95 },
  { id: 4, baslik: '10 Minutes 38 Seconds in This Strange World', yazar: 'Elif Şafak', kategori: 'Roman', yil: 2019, durum: 'mevcut', puan: 4.6, odunc: 130 },
  { id: 5, baslik: 'Tutunamayanlar', yazar: 'Oğuz Atay', kategori: 'Roman', yil: 1972, durum: 'mevcut', puan: 4.9, odunc: 160 },
  { id: 6, baslik: 'Beyaz Kale', yazar: 'Orhan Pamuk', kategori: 'Roman', yil: 1985, durum: 'odunc', puan: 4.2, odunc: 87 },
  { id: 7, baslik: 'Aylak Adam', yazar: 'Yusuf Atılgan', kategori: 'Roman', yil: 1959, durum: 'mevcut', puan: 4.4, odunc: 76 },
  { id: 8, baslik: 'Çalıkuşu', yazar: 'Reşat Nuri Güntekin', kategori: 'Roman', yil: 1922, durum: 'mevcut', puan: 4.7, odunc: 155 },
  { id: 9, baslik: 'Huzur', yazar: 'Ahmet Hamdi Tanpınar', kategori: 'Roman', yil: 1949, durum: 'mevcut', puan: 4.6, odunc: 99 },
  { id: 10, baslik: 'Aşk', yazar: 'Elif Şafak', kategori: 'Roman', yil: 2009, durum: 'odunc', puan: 4.1, odunc: 112 },
  { id: 11, baslik: 'Saatleri Ayarlama Enstitüsü', yazar: 'Ahmet Hamdi Tanpınar', kategori: 'Roman', yil: 1961, durum: 'mevcut', puan: 4.8, odunc: 88 },
  { id: 12, baslik: 'Vatandasligini Kaybeden Adam', yazar: 'Sabahattin Ali', kategori: 'Hikaye', yil: 1935, durum: 'mevcut', puan: 4.3, odunc: 64 }
];

const sampleMembers = [
  { id: 1, ad: 'Ayşe Yılmaz', tc: '12345678901', email: 'ayse@mail.com', kayitTarihi: '2024-09-15', oduncSayisi: 3, durum: 'aktif' },
  { id: 2, ad: 'Mehmet Kaya', tc: '23456789012', email: 'mehmet@mail.com', kayitTarihi: '2024-10-01', oduncSayisi: 1, durum: 'aktif' },
  { id: 3, ad: 'Zeynep Demir', tc: '34567890123', email: 'zeynep@mail.com', kayitTarihi: '2024-11-20', oduncSayisi: 0, durum: 'pasif' },
  { id: 4, ad: 'Ali Çelik', tc: '45678901234', email: 'ali@mail.com', kayitTarihi: '2025-01-05', oduncSayisi: 5, durum: 'aktif' },
  { id: 5, ad: 'Fatma Öztürk', tc: '56789012345', email: 'fatma@mail.com', kayitTarihi: '2025-02-14', oduncSayisi: 2, durum: 'aktif' }
];

const sampleAssets = [
  { id: 1, baslik: 'Türk Edebiyatı Antolojisi', tur: 'E-Kitap', boyut: '12 MB', format: 'PDF' },
  { id: 2, baslik: 'Osmanlı Tarihi Belgeseli', tur: 'Video', boyut: '1.2 GB', format: 'MP4' },
  { id: 3, baslik: 'Klasik Türk Müziği Koleksiyonu', tur: 'Ses', boyut: '340 MB', format: 'MP3' },
  { id: 4, baslik: 'İstanbul Haritaları Arşivi', tur: 'Görsel', boyut: '85 MB', format: 'PNG' },
  { id: 5, baslik: 'Akademik Makaleler 2024', tur: 'E-Kitap', boyut: '28 MB', format: 'PDF' },
  { id: 6, baslik: 'Anadolu Kültürü Podcast', tur: 'Ses', boyut: '210 MB', format: 'MP3' }
];

function initSidebar() {
  const navItems = document.querySelectorAll('.nav-item');
  const pages = document.querySelectorAll('.page-content');
  const sidebarToggle = document.getElementById('sidebarToggle');
  const sidebar = document.querySelector('.sidebar');

  navItems.forEach(item => {
    item.addEventListener('click', (e) => {
      e.preventDefault();
      const targetPage = item.dataset.page;
      if (!targetPage) return;

      navItems.forEach(n => n.classList.remove('active'));
      item.classList.add('active');

      pages.forEach(p => {
        p.classList.remove('active');
        if (p.id === `${targetPage}Page` || p.id === targetPage) {
          p.classList.add('active');
        }
      });

      updateBreadcrumb(item.querySelector('.nav-label')?.textContent || targetPage);

      if (window.innerWidth < 1024 && sidebar) {
        sidebar.classList.remove('open');
      }
    });
  });

  if (sidebarToggle && sidebar) {
    sidebarToggle.addEventListener('click', () => {
      sidebar.classList.toggle('collapsed');
      document.querySelector('.main-content')?.classList.toggle('expanded');
    });
  }
}

function initMobileMenu() {
  const mobileBtn = document.getElementById('mobileMenuBtn');
  const sidebar = document.querySelector('.sidebar');
  if (mobileBtn && sidebar) {
    mobileBtn.addEventListener('click', () => {
      sidebar.classList.toggle('open');
    });
  }
}

function initTheme() {
  const themeToggle = document.getElementById('themeToggle');
  const saved = localStorage.getItem('akilli_kutup_theme');
  if (saved === 'light') {
    document.body.classList.add('light-theme');
  }
  if (themeToggle) {
    themeToggle.addEventListener('click', () => {
      document.body.classList.toggle('light-theme');
      const isLight = document.body.classList.contains('light-theme');
      localStorage.setItem('akilli_kutup_theme', isLight ? 'light' : 'dark');
      const icon = themeToggle.querySelector('i');
      if (icon) {
        icon.className = isLight ? 'fas fa-moon' : 'fas fa-sun';
      }
    });
  }
}

function initSearch() {
  const searchInput = document.getElementById('searchInput');
  if (!searchInput) return;

  document.addEventListener('keydown', (e) => {
    if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
      e.preventDefault();
      searchInput.focus();
    }
    if (e.key === 'Escape') {
      searchInput.blur();
    }
  });

  searchInput.addEventListener('input', (e) => {
    const query = e.target.value.toLowerCase().trim();
    if (query.length < 2) return;
    const results = sampleBooks.filter(b =>
      b.baslik.toLowerCase().includes(query) || b.yazar.toLowerCase().includes(query)
    );
    console.log('Arama sonuçları:', results.length);
  });
}

function initNotifications() {
  const notifBtn = document.getElementById('notificationBtn');
  const notifPanel = document.querySelector('.notification-panel');
  if (notifBtn && notifPanel) {
    notifBtn.addEventListener('click', (e) => {
      e.stopPropagation();
      notifPanel.classList.toggle('open');
    });
    document.addEventListener('click', () => {
      notifPanel.classList.remove('open');
    });
  }
}

function initFullscreen() {
  const fsBtn = document.getElementById('fullscreenBtn');
  if (fsBtn) {
    fsBtn.addEventListener('click', () => {
      if (!document.fullscreenElement) {
        document.documentElement.requestFullscreen().catch(() => {});
      } else {
        document.exitFullscreen();
      }
    });
  }
}

function updateBreadcrumb(text) {
  const el = document.getElementById('breadcrumbCurrent');
  if (el) el.textContent = text;
}

function animateStats() {
  const counters = document.querySelectorAll('[data-target]');
  counters.forEach(counter => {
    const target = parseInt(counter.dataset.target, 10);
    if (isNaN(target)) return;
    let current = 0;
    const step = Math.max(1, Math.ceil(target / 60));
    const timer = setInterval(() => {
      current += step;
      if (current >= target) {
        current = target;
        clearInterval(timer);
      }
      counter.textContent = current.toLocaleString('tr-TR');
    }, 20);
  });
}

function renderDonutChart() {
  const svg = document.getElementById('donutChart');
  if (!svg) return;

  const data = [
    { label: 'Roman', value: 45, color: '#6c5ce7' },
    { label: 'Polisiye', value: 18, color: '#00cec9' },
    { label: 'Bilim', value: 15, color: '#fdcb6e' },
    { label: 'Tarih', value: 12, color: '#e17055' },
    { label: 'Diğer', value: 10, color: '#636e72' }
  ];

  const total = data.reduce((s, d) => s + d.value, 0);
  const cx = 80, cy = 80, r = 60;
  const circumference = 2 * Math.PI * r;
  let offset = 0;

  svg.setAttribute('viewBox', '0 0 160 160');
  svg.innerHTML = '';

  data.forEach(item => {
    const pct = item.value / total;
    const dashLength = pct * circumference;
    const circle = document.createElementNS('http://www.w3.org/2000/svg', 'circle');
    circle.setAttribute('cx', cx);
    circle.setAttribute('cy', cy);
    circle.setAttribute('r', r);
    circle.setAttribute('fill', 'none');
    circle.setAttribute('stroke', item.color);
    circle.setAttribute('stroke-width', '20');
    circle.setAttribute('stroke-dasharray', `${dashLength} ${circumference - dashLength}`);
    circle.setAttribute('stroke-dashoffset', `${-offset}`);
    circle.setAttribute('transform', `rotate(-90 ${cx} ${cy})`);
    svg.appendChild(circle);
    offset += dashLength;
  });

  const legend = svg.closest('.chart-card')?.querySelector('.chart-legend');
  if (legend) {
    legend.innerHTML = data.map(d =>
      `<div class="legend-item"><span class="legend-color" style="background:${d.color}"></span>${d.label} (%${d.value})</div>`
    ).join('');
  }
}

function renderBarChart() {
  const canvas = document.getElementById('borrowCanvas');
  if (!canvas) return;
  const ctx = canvas.getContext('2d');
  if (!ctx) return;

  const months = ['Oca', 'Şub', 'Mar', 'Nis', 'May', 'Haz', 'Tem', 'Ağu', 'Eyl', 'Eki', 'Kas', 'Ara'];
  const values = [42, 55, 38, 67, 82, 73, 61, 49, 88, 95, 78, 64];
  const maxVal = Math.max(...values);

  canvas.width = canvas.offsetWidth * 2;
  canvas.height = canvas.offsetHeight * 2;
  ctx.scale(2, 2);

  const w = canvas.offsetWidth;
  const h = canvas.offsetHeight;
  const padding = { top: 20, right: 20, bottom: 30, left: 40 };
  const chartW = w - padding.left - padding.right;
  const chartH = h - padding.top - padding.bottom;
  const barW = chartW / months.length * 0.6;
  const gap = chartW / months.length;

  ctx.clearRect(0, 0, w, h);

  ctx.strokeStyle = 'rgba(255,255,255,0.06)';
  ctx.lineWidth = 1;
  for (let i = 0; i <= 4; i++) {
    const y = padding.top + (chartH / 4) * i;
    ctx.beginPath();
    ctx.moveTo(padding.left, y);
    ctx.lineTo(w - padding.right, y);
    ctx.stroke();
  }

  values.forEach((val, i) => {
    const barH = (val / maxVal) * chartH;
    const x = padding.left + i * gap + (gap - barW) / 2;
    const y = padding.top + chartH - barH;

    const gradient = ctx.createLinearGradient(x, y, x, y + barH);
    gradient.addColorStop(0, '#6c5ce7');
    gradient.addColorStop(1, '#a29bfe');
    ctx.fillStyle = gradient;
    ctx.beginPath();
    ctx.roundRect(x, y, barW, barH, [4, 4, 0, 0]);
    ctx.fill();

    ctx.fillStyle = 'rgba(255,255,255,0.5)';
    ctx.font = '10px Inter';
    ctx.textAlign = 'center';
    ctx.fillText(months[i], x + barW / 2, h - 8);
  });
}

function renderRecentBooks() {
  const list = document.getElementById('recentBooksList');
  if (!list) return;
  const recent = sampleBooks.slice(0, 5);
  list.innerHTML = recent.map(book => `
    <div class="book-list-item">
      <div class="book-icon">📖</div>
      <div class="book-info">
        <div class="book-title">${book.baslik}</div>
        <div class="book-author">${book.yazar}</div>
      </div>
      <span class="badge ${book.durum === 'mevcut' ? 'badge-success' : 'badge-warning'}">
        ${book.durum === 'mevcut' ? 'Mevcut' : 'Ödünç'}
      </span>
    </div>
  `).join('');
}

function renderPopularBooks() {
  const list = document.getElementById('popularBooksList');
  if (!list) return;
  const popular = [...sampleBooks].sort((a, b) => b.odunc - a.odunc).slice(0, 5);
  list.innerHTML = popular.map((book, i) => `
    <div class="book-list-item">
      <div class="rank">#${i + 1}</div>
      <div class="book-info">
        <div class="book-title">${book.baslik}</div>
        <div class="book-author">${book.yazar} · ${book.odunc} ödünç</div>
      </div>
      <div class="rating">⭐ ${book.puan}</div>
    </div>
  `).join('');
}

function renderMembersTable() {
  const tbody = document.getElementById('membersTableBody');
  if (!tbody) return;
  tbody.innerHTML = sampleMembers.map(m => `
    <tr>
      <td>${m.ad}</td>
      <td>${m.tc.substring(0, 3)}*****${m.tc.substring(8)}</td>
      <td>${m.email}</td>
      <td>${m.kayitTarihi}</td>
      <td>${m.oduncSayisi}</td>
      <td><span class="badge ${m.durum === 'aktif' ? 'badge-success' : 'badge-secondary'}">${m.durum}</span></td>
      <td>
        <button class="btn-icon" title="Düzenle"><i class="fas fa-edit"></i></button>
        <button class="btn-icon" title="Sil"><i class="fas fa-trash"></i></button>
      </td>
    </tr>
  `).join('');
}

function renderBookGrid() {
  const grid = document.getElementById('bookGrid');
  if (!grid) return;
  grid.innerHTML = sampleBooks.map(book => `
    <div class="book-card" data-id="${book.id}">
      <div class="book-cover">
        <div class="cover-placeholder">📚</div>
      </div>
      <div class="book-card-body">
        <h4 class="book-card-title">${book.baslik}</h4>
        <p class="book-card-author">${book.yazar}</p>
        <div class="book-card-meta">
          <span class="badge ${book.durum === 'mevcut' ? 'badge-success' : 'badge-warning'}">${book.durum === 'mevcut' ? 'Mevcut' : 'Ödünç'}</span>
          <span class="book-year">${book.yil}</span>
        </div>
      </div>
    </div>
  `).join('');
}

function renderAssetGrid() {
  const grid = document.getElementById('assetGrid');
  if (!grid) return;
  const icons = { 'E-Kitap': '📄', 'Video': '🎬', 'Ses': '🎵', 'Görsel': '🖼️' };
  grid.innerHTML = sampleAssets.map(asset => `
    <div class="asset-card" data-id="${asset.id}">
      <div class="asset-icon">${icons[asset.tur] || '📁'}</div>
      <div class="asset-info">
        <h4>${asset.baslik}</h4>
        <p>${asset.tur} · ${asset.format} · ${asset.boyut}</p>
      </div>
      <button class="btn btn-sm btn-outline">İndir</button>
    </div>
  `).join('');
}

function initAddBookModal() {
  const modal = document.getElementById('addBookModal');
  if (!modal) return;

  const openBtns = document.querySelectorAll('[data-action="addBook"]');
  const closeBtns = modal.querySelectorAll('[data-dismiss="modal"], .modal-overlay');
  const form = modal.querySelector('form');

  openBtns.forEach(btn => {
    btn.addEventListener('click', () => modal.classList.add('open'));
  });

  closeBtns.forEach(btn => {
    btn.addEventListener('click', () => modal.classList.remove('open'));
  });

  if (form) {
    form.addEventListener('submit', (e) => {
      e.preventDefault();
      const formData = new FormData(form);
      const book = Object.fromEntries(formData.entries());
      sampleBooks.push({
        id: sampleBooks.length + 1,
        baslik: book.baslik || '',
        yazar: book.yazar || '',
        kategori: book.kategori || 'Roman',
        yil: parseInt(book.yil, 10) || 2025,
        durum: 'mevcut',
        puan: 0,
        odunc: 0
      });
      modal.classList.remove('open');
      form.reset();
      renderBookGrid();
      renderRecentBooks();
      showToast('Kitap başarıyla eklendi.', 'success');
    });
  }
}

function showToast(message, type = 'info') {
  const container = document.getElementById('toastContainer');
  if (!container) return;
  const icons = { success: 'check-circle', error: 'exclamation-circle', warning: 'exclamation-triangle', info: 'info-circle' };
  const toast = document.createElement('div');
  toast.className = `toast toast-${type}`;
  toast.innerHTML = `<i class="fas fa-${icons[type] || icons.info}"></i><span>${message}</span>`;
  container.appendChild(toast);
  requestAnimationFrame(() => toast.classList.add('show'));
  setTimeout(() => {
    toast.classList.remove('show');
    setTimeout(() => toast.remove(), 300);
  }, 3500);
}

function initAIChat() {
  const sendBtn = document.getElementById('aiSendBtn');
  const input = document.getElementById('aiInput');
  const chatBody = document.querySelector('.ai-chat-body');

  if (!sendBtn || !input || !chatBody) return;

  const responses = [
    'Bu konuda size yardımcı olabilirim. Kütüphanemizdeki en popüler kitap şu anda "Tutunamayanlar".',
    'Ödünç verme istatistiklerine göre bu ay %12 artış var.',
    'Sabahattin Ali\'nin tüm eserleri kütüphanemizde mevcuttur.',
    'Yeni üyelik başvuruları "Üyeler" sekmesinden takip edilebilir.',
    'Dijital varlık arşivine 6 yeni içerik eklenmiştir.',
    'Katalog araması için Ctrl+K kısayolunu kullanabilirsiniz.'
  ];

  function addMessage(text, sender) {
    const msg = document.createElement('div');
    msg.className = `chat-message ${sender}`;
    msg.innerHTML = `<div class="message-bubble">${text}</div>`;
    chatBody.appendChild(msg);
    chatBody.scrollTop = chatBody.scrollHeight;
  }

  function handleSend() {
    const text = input.value.trim();
    if (!text) return;
    addMessage(text, 'user');
    input.value = '';
    setTimeout(() => {
      const response = responses[Math.floor(Math.random() * responses.length)];
      addMessage(response, 'assistant');
    }, 800 + Math.random() * 700);
  }

  sendBtn.addEventListener('click', handleSend);
  input.addEventListener('keydown', (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  });
}
