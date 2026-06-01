let appData = { books: [], members: [], assets: [] };
let catalogState = {
  query: '',
  category: '',
  status: '',
  language: '',
  sort: 'newest',
  view: 'grid',
  currentPage: 1,
  itemsPerPage: 12
};

document.addEventListener('DOMContentLoaded', async () => {
  initSidebar();
  initTheme();
  initSearch();
  initNotifications();
  initFullscreen();
  initMobileMenu();
  
  await loadDataFromAPI();
  
  animateStats();
  renderDonutChart();
  renderBarChart();
  renderRecentBooks();
  renderPopularBooks();
  renderMembersTable();
  renderBookGrid();
  renderAssetGrid();
  initAddBookModal();
  initAddMemberModal();
  initAIChat();
  initDummyButtons();
  initCatalogFilters();
});

async function loadDataFromAPI() {
    try {
        if(typeof API !== 'undefined') {
            const books = await API.getBooks();
            if(books && Array.isArray(books)) appData.books = books;
            
            const users = await API.getUsers();
            if(users && Array.isArray(users)) appData.members = users;
        }
    } catch (e) {
        console.error("API baglanti hatasi", e);
    }
    
    // UI'in bos ve anlamsiz kalmamasi adina (API kapaliysa vs.) Gercekci Dummy veriler:
    if (!appData.books || appData.books.length === 0) {
        appData.books = [
            { id: 1, baslik: 'Kürk Mantolu Madonna', yazar: 'Sabahattin Ali', stokAdedi: 5, birimFiyat: 45, odunc: 15 },
            { id: 2, baslik: 'Suç ve Ceza', yazar: 'Fyodor Dostoyevski', stokAdedi: 3, birimFiyat: 60, odunc: 12 },
            { id: 3, baslik: 'Sefiller', yazar: 'Victor Hugo', stokAdedi: 0, birimFiyat: 80, odunc: 25 },
            { id: 4, baslik: '1984', yazar: 'George Orwell', stokAdedi: 2, birimFiyat: 35, odunc: 8 },
            { id: 5, baslik: 'Simyacı', yazar: 'Paulo Coelho', stokAdedi: 10, birimFiyat: 40, odunc: 30 },
            { id: 6, baslik: 'Yüzüklerin Efendisi', yazar: 'J.R.R. Tolkien', stokAdedi: 1, birimFiyat: 120, odunc: 40 },
            { id: 7, baslik: 'İçimizdeki Şeytan', yazar: 'Sabahattin Ali', stokAdedi: 8, birimFiyat: 38, odunc: 5 }
        ];
    }
    
    if (!appData.members || appData.members.length === 0 || (appData.members.length === 1 && appData.members[0].isim === 'Tekrar Eden')) {
        appData.members = [
            { id: 'M-1021', isim: 'Ahmet Yılmaz', tcKimlikNo: '12345678901', email: 'ahmet.y@example.com' },
            { id: 'M-1022', isim: 'Ayşe Demir', tcKimlikNo: '98765432109', email: 'ayse.demir@example.com' },
            { id: 'M-1023', isim: 'Mehmet Kaya', tcKimlikNo: '55555555555', email: 'mkaya@example.com' },
            { id: 'M-1024', isim: 'Zeynep Çelik', tcKimlikNo: '33333333333', email: 'zeynep.c@example.com' },
            { id: 'M-1025', isim: 'Ali Vefa', tcKimlikNo: '11111111111', email: 'ali.vefa@example.com' },
            { id: 'M-1026', isim: 'Fatma Gül', tcKimlikNo: '22222222222', email: 'fgul@example.com' }
        ];
    }

    appData.assets = [
        { id: 1, baslik: 'Türk Edebiyatı Antolojisi', tur: 'E-Kitap', boyut: '12 MB', format: 'PDF' },
        { id: 2, baslik: 'Osmanlı Tarihi Belgeseli', tur: 'Video', boyut: '1.2 GB', format: 'MP4' },
        { id: 3, baslik: 'Klasik Türk Müziği Koleksiyonu', tur: 'Ses', boyut: '340 MB', format: 'MP3' },
        { id: 4, baslik: 'Python Programlama Rehberi', tur: 'E-Kitap', boyut: '8 MB', format: 'EPUB' }
    ];
}

function initSidebar() {
  const navItems = document.querySelectorAll('.nav-item');
  const pages = document.querySelectorAll('.page');
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
        if (p.id === `${targetPage}Page` || p.id === targetPage || p.id === `page-${targetPage}`) {
          p.classList.add('active');
        }
      });

      updateBreadcrumb(item.querySelector('span')?.textContent || targetPage);

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
    document.documentElement.setAttribute('data-theme', 'light');
  }
  if (themeToggle) {
    themeToggle.addEventListener('click', () => {
      const isLight = document.documentElement.getAttribute('data-theme') === 'light';
      if (isLight) {
        document.documentElement.removeAttribute('data-theme');
        localStorage.setItem('akilli_kutup_theme', 'dark');
      } else {
        document.documentElement.setAttribute('data-theme', 'light');
        localStorage.setItem('akilli_kutup_theme', 'light');
      }
      const icon = themeToggle.querySelector('i');
      if (icon) {
        icon.className = !isLight ? 'fas fa-moon' : 'fas fa-sun';
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
    let q = e.target.value.toLowerCase().trim();
    if (q.length > 0 && q.length < 2) return;
    catalogState.query = q.length >= 2 ? q : '';
    catalogState.currentPage = 1;
    if (typeof updateCatalog === 'function') updateCatalog();
  });
}

function initNotifications() {
  const notifBtn = document.getElementById('notificationBtn');
  const notifPanel = document.querySelector('.notification-panel');
  const markReadBtn = document.getElementById('markAllReadBtn');

  if (notifBtn && notifPanel) {
    notifBtn.addEventListener('click', (e) => {
      e.stopPropagation();
      notifPanel.classList.toggle('active');
    });
    document.addEventListener('click', (e) => {
      if (!notifPanel.contains(e.target)) {
        notifPanel.classList.remove('active');
      }
    });
  }

  if (markReadBtn) {
    markReadBtn.addEventListener('click', () => {
      const unreadItems = document.querySelectorAll('.notification-item.unread');
      unreadItems.forEach(item => {
        item.classList.remove('unread');
      });
      // Optionally show a toast if available
      if (typeof showToast === 'function') {
        showToast('Tüm bildirimler okundu olarak işaretlendi.', 'success');
      }
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
    circle.style.animation = 'dash 1.5s ease-out forwards'; // CSS Animasyon entegrasyonu
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
  const recent = appData.books.slice(0, 5);
  list.innerHTML = recent.map(book => `
    <div class="book-list-item fade-in-up">
      <div class="book-icon">📖</div>
      <div class="book-info">
        <div class="book-title">${book.baslik || 'Bilinmeyen Başlık'}</div>
        <div class="book-author">${book.yazar || 'Bilinmeyen Yazar'}</div>
      </div>
      <span class="badge ${book.stokAdedi > 0 ? 'badge-success' : 'badge-warning'}">
        ${book.stokAdedi > 0 ? 'Mevcut' : 'Tükendi'}
      </span>
    </div>
  `).join('');
}

function renderPopularBooks() {
  const list = document.getElementById('popularBooksList');
  if (!list) return;
  const popular = [...appData.books].sort((a, b) => (b.odunc || 0) - (a.odunc || 0)).slice(0, 5);
  list.innerHTML = popular.map((book, i) => `
    <div class="book-list-item fade-in-up" style="animation-delay: ${i*0.1}s">
      <div class="rank">#${i + 1}</div>
      <div class="book-info">
        <div class="book-title">${book.baslik || 'Bilinmeyen Başlık'}</div>
        <div class="book-author">${book.yazar || 'Yazar Yok'}</div>
      </div>
      <div class="rating">⭐ ${book.stokAdedi || 0} Stok</div>
    </div>
  `).join('');
}

function renderMembersTable() {
  const tbody = document.getElementById('membersTableBody');
  if (!tbody) return;
  tbody.innerHTML = appData.members.map(m => `
    <tr class="fade-in">
      <td>${m.isim || 'Bilinmiyor'}</td>
      <td>${(m.tcKimlikNo || '00000000000').substring(0, 3)}*****${(m.tcKimlikNo || '00000000000').substring(8)}</td>
      <td>${m.email || 'Yok'}</td>
      <td>${m.id || '-'}</td>
      <td>-</td>
      <td><span class="badge badge-success">Aktif</span></td>
      <td>
        <button class="btn-icon btn-edit-member" data-id="${m.id}" title="Düzenle"><i class="fas fa-edit"></i></button>
        <button class="btn-icon btn-delete-member" data-id="${m.id}" title="Sil"><i class="fas fa-trash"></i></button>
      </td>
    </tr>
  `).join('');

  const memberBadge = document.querySelector('#nav-members .nav-badge');
  if (memberBadge) {
    memberBadge.textContent = appData.members.length;
  }

  // Bind event listeners to delete buttons
  tbody.querySelectorAll('.btn-delete-member').forEach(btn => {
    btn.addEventListener('click', () => {
      const memberId = btn.dataset.id;
      if (confirm('Bu üyeyi silmek istediğinize emin misiniz?')) {
        appData.members = appData.members.filter(m => m.id !== memberId);
        renderMembersTable();
        showToast('Üye silindi.', 'info');
      }
    });
  });

  // Bind event listeners to edit buttons
  tbody.querySelectorAll('.btn-edit-member').forEach(btn => {
    btn.addEventListener('click', () => {
      const memberId = btn.dataset.id;
      const member = appData.members.find(m => m.id === memberId);
      if (member) {
        const modal = document.getElementById('addMemberModal');
        if (modal) {
          modal.querySelector('h2').innerHTML = '<i class="fas fa-user-edit"></i> Üyeyi Düzenle';
          document.getElementById('memberName').value = member.isim;
          document.getElementById('memberTc').value = member.tcKimlikNo;
          document.getElementById('memberEmail').value = member.email;
          modal.dataset.editId = memberId;
          modal.classList.add('active');
        }
      }
    });
  });
}

function renderBookGrid() {
  updateCatalog();
}

function updateCatalog() {
  const grid = document.getElementById('bookGrid');
  if (!grid) return;

  let filtered = appData.books.filter(b => {
    let matchQuery = true;
    if (catalogState.query) {
      const q = catalogState.query;
      matchQuery = (b.baslik && b.baslik.toLowerCase().includes(q)) || 
                   (b.yazar && b.yazar.toLowerCase().includes(q)) ||
                   (b.isbn && b.isbn.includes(q));
    }
    
    let matchCategory = true;
    if (catalogState.category) {
       matchCategory = (b.tur && b.tur.toLowerCase() === catalogState.category.toLowerCase()) || catalogState.category === '';
    }

    let matchStatus = true;
    if (catalogState.status) {
       if (catalogState.status === 'available') matchStatus = b.stokAdedi > 0;
       if (catalogState.status === 'borrowed') matchStatus = b.stokAdedi <= 0;
    }
    
    return matchQuery && matchCategory && matchStatus;
  });

  filtered.sort((a, b) => {
    if (catalogState.sort === 'newest') return (b.id.toString().localeCompare(a.id.toString()));
    if (catalogState.sort === 'oldest') return (a.id.toString().localeCompare(b.id.toString()));
    if (catalogState.sort === 'title-asc') return (a.baslik || '').localeCompare(b.baslik || '');
    if (catalogState.sort === 'title-desc') return (b.baslik || '').localeCompare(a.baslik || '');
    if (catalogState.sort === 'popular') return (b.odunc || 0) - (a.odunc || 0);
    return 0;
  });

  const countEl = document.getElementById('catalogCount');
  if (countEl) countEl.textContent = `${filtered.length} kitap bulundu`;

  const totalPages = Math.max(1, Math.ceil(filtered.length / catalogState.itemsPerPage));
  if (catalogState.currentPage > totalPages) catalogState.currentPage = totalPages;
  
  const start = (catalogState.currentPage - 1) * catalogState.itemsPerPage;
  const paginated = filtered.slice(start, start + catalogState.itemsPerPage);

  grid.className = catalogState.view === 'list' ? 'book-grid list-view' : 'book-grid';
  grid.innerHTML = paginated.map((book, i) => `
    <div class="book-card" data-id="${book.id}">
      <div class="book-cover">
        <div class="cover-placeholder">📚</div>
      </div>
      <div class="book-card-body">
        <div class="book-card-info">
            <h4 class="book-card-title">${book.baslik}</h4>
            <p class="book-card-author">${book.yazar || 'Yazar Belirtilmemiş'}</p>
        </div>
        <div class="book-card-meta">
          <span class="badge ${book.stokAdedi > 0 ? 'badge-success' : 'badge-warning'}">${book.stokAdedi > 0 ? 'Mevcut' : 'Tükendi'}</span>
          <span class="book-year">${book.birimFiyat || 0} TL</span>
        </div>
      </div>
    </div>
  `).join('');

  grid.querySelectorAll('.book-card').forEach(card => {
    card.addEventListener('click', () => {
        const b = appData.books.find(x => x.id.toString() === card.dataset.id);
        if (b) {
            document.getElementById('detailsBookTitle').textContent = b.baslik || 'Bilinmeyen Başlık';
            document.getElementById('detailsBookAuthor').textContent = b.yazar || 'Yazar Belirtilmemiş';
            document.getElementById('detailsBookCategory').textContent = b.tur || 'Belirtilmemiş';
            document.getElementById('detailsBookPrice').textContent = (b.birimFiyat || 0) + ' TL';
            document.getElementById('detailsBookBorrowCount').textContent = (b.odunc || 0) + ' kez';
            
            const stockEl = document.getElementById('detailsBookStock');
            if (b.stokAdedi > 0) {
                stockEl.className = 'badge badge-success';
                stockEl.textContent = b.stokAdedi + ' Adet Mevcut';
            } else {
                stockEl.className = 'badge badge-warning';
                stockEl.textContent = 'Tükendi';
            }
            
            document.getElementById('bookDetailsModal').classList.add('active');
        }
    });
  });

  renderPagination(totalPages);
}

function renderPagination(totalPages) {
  const pag = document.getElementById('catalogPagination');
  if (!pag) return;
  
  let html = `<button class="pagination-btn" ${catalogState.currentPage === 1 ? 'disabled' : ''} data-page="prev"><i class="fas fa-chevron-left"></i></button>`;
  
  for(let i = 1; i <= totalPages; i++) {
     if (i === 1 || i === totalPages || (i >= catalogState.currentPage - 1 && i <= catalogState.currentPage + 1)) {
        html += `<button class="pagination-btn ${i === catalogState.currentPage ? 'active' : ''}" data-page="${i}">${i}</button>`;
     } else if (i === catalogState.currentPage - 2 || i === catalogState.currentPage + 2) {
        html += `<span class="pagination-dots">...</span>`;
     }
  }
  
  html += `<button class="pagination-btn" ${catalogState.currentPage === totalPages ? 'disabled' : ''} data-page="next"><i class="fas fa-chevron-right"></i></button>`;
  
  pag.innerHTML = html;
  
  pag.querySelectorAll('.pagination-btn').forEach(btn => {
    btn.addEventListener('click', () => {
       if (btn.disabled) return;
       let p = btn.dataset.page;
       if (p === 'prev') catalogState.currentPage--;
       else if (p === 'next') catalogState.currentPage++;
       else catalogState.currentPage = parseInt(p);
       updateCatalog();
    });
  });
}

function initCatalogFilters() {
    const filterCategory = document.getElementById('filterCategory');
    const filterStatus = document.getElementById('filterStatus');
    const filterLanguage = document.getElementById('filterLanguage');
    const filterSort = document.getElementById('filterSort');

    if (filterCategory) filterCategory.addEventListener('change', (e) => { catalogState.category = e.target.value; catalogState.currentPage = 1; updateCatalog(); });
    if (filterStatus) filterStatus.addEventListener('change', (e) => { catalogState.status = e.target.value; catalogState.currentPage = 1; updateCatalog(); });
    if (filterLanguage) filterLanguage.addEventListener('change', (e) => { catalogState.language = e.target.value; catalogState.currentPage = 1; updateCatalog(); });
    if (filterSort) filterSort.addEventListener('change', (e) => { catalogState.sort = e.target.value; catalogState.currentPage = 1; updateCatalog(); });

    const btnGrid = document.getElementById('viewGridBtn');
    const btnList = document.getElementById('viewListBtn');
    
    if (btnGrid) btnGrid.addEventListener('click', () => {
        catalogState.view = 'grid';
        btnGrid.classList.add('active');
        if(btnList) btnList.classList.remove('active');
        updateCatalog();
    });
    
    if (btnList) btnList.addEventListener('click', () => {
        catalogState.view = 'list';
        btnList.classList.add('active');
        if(btnGrid) btnGrid.classList.remove('active');
        updateCatalog();
    });
}

function renderAssetGrid() {
  const grid = document.getElementById('assetGrid');
  if (!grid) return;
  const icons = { 'E-Kitap': '📄', 'Video': '🎬', 'Ses': '🎵', 'Görsel': '🖼️' };
  grid.innerHTML = appData.assets.map(asset => `
    <div class="asset-card fade-in" data-id="${asset.id}">
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

  const openBtns = [
    document.getElementById('addBookBtn'),
    document.getElementById('catalogAddBtn')
  ];
  const closeBtns = [
    document.getElementById('modalClose'),
    document.getElementById('modalCancelBtn'),
    modal
  ];
  const form = document.getElementById('addBookForm');
  const saveBtn = document.getElementById('modalSaveBtn');
  
  // Book Details Modal kapatma işlemleri
  const detailsModal = document.getElementById('bookDetailsModal');
  const detailsCloseBtns = [
      document.getElementById('detailsModalClose'),
      document.getElementById('detailsModalCancel'),
      detailsModal
  ];
  
  detailsCloseBtns.forEach(btn => {
      if(btn) btn.addEventListener('click', (e) => {
          if (e.target === detailsModal || e.currentTarget !== detailsModal) {
              detailsModal.classList.remove('active');
          }
      });
  });

  openBtns.forEach(btn => {
    if (btn) {
      btn.addEventListener('click', () => modal.classList.add('active'));
    }
  });

  closeBtns.forEach(btn => {
    if (btn) {
      btn.addEventListener('click', (e) => {
        if (e.target === modal || e.currentTarget !== modal) {
          modal.classList.remove('active');
        }
      });
    }
  });

  if (saveBtn && form) {
    saveBtn.addEventListener('click', (e) => {
      if (form.checkValidity()) {
        e.preventDefault();
        const title = document.getElementById('bookTitle').value;
        const author = document.getElementById('bookAuthor').value;
        const price = parseInt(document.getElementById('bookYear').value) || 50;
        
        appData.books.push({
          id: appData.books.length + 1,
          baslik: title,
          yazar: author,
          stokAdedi: 5,
          birimFiyat: price,
          odunc: 0
        });
        
        modal.classList.remove('active');
        form.reset();
        renderBookGrid();
        renderRecentBooks();
        showToast('Kitap başarıyla eklendi.', 'success');
      } else {
        form.reportValidity();
      }
    });
  }
}

function initAddMemberModal() {
  const modal = document.getElementById('addMemberModal');
  if (!modal) return;

  const openBtn = document.getElementById('addMemberBtn');
  const closeBtns = [
    document.getElementById('memberModalClose'),
    document.getElementById('memberModalCancelBtn'),
    modal
  ];
  const form = document.getElementById('addMemberForm');
  const saveBtn = document.getElementById('memberSaveBtn');

  if (openBtn) {
    openBtn.addEventListener('click', () => {
      modal.querySelector('h2').innerHTML = '<i class="fas fa-user-plus"></i> Yeni Üye Ekle';
      form.reset();
      delete modal.dataset.editId;
      modal.classList.add('active');
    });
  }

  closeBtns.forEach(btn => {
    if (btn) {
      btn.addEventListener('click', (e) => {
        if (e.target === modal || e.currentTarget !== modal) {
          modal.classList.remove('active');
        }
      });
    }
  });

  if (saveBtn && form) {
    saveBtn.addEventListener('click', (e) => {
      if (form.checkValidity()) {
        e.preventDefault();
        const name = document.getElementById('memberName').value;
        const tc = document.getElementById('memberTc').value;
        const email = document.getElementById('memberEmail').value;
        
        const editId = modal.dataset.editId;
        if (editId) {
          const memberIndex = appData.members.findIndex(m => m.id === editId);
          if (memberIndex !== -1) {
            appData.members[memberIndex].isim = name;
            appData.members[memberIndex].tcKimlikNo = tc;
            appData.members[memberIndex].email = email;
            showToast('Üye bilgileri güncellendi.', 'success');
          }
        } else {
          const newId = 'M-' + (1021 + appData.members.length);
          appData.members.push({
            id: newId,
            isim: name,
            tcKimlikNo: tc,
            email: email
          });
          showToast('Yeni üye başarıyla kaydedildi.', 'success');
        }

        modal.classList.remove('active');
        form.reset();
        delete modal.dataset.editId;
        renderMembersTable();
      } else {
        form.reportValidity();
      }
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
  const chatBody = document.getElementById('aiChatWindow');

  if (!sendBtn || !input || !chatBody) return;

  function formatMarkdown(text) {
    if (!text) return "";
    return text
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
      .replace(/\*(.*?)\*/g, '<em>$1</em>')
      .replace(/\n/g, '<br>');
  }

  function addMessage(text, sender) {
    const msg = document.createElement('div');
    const isBot = sender === 'assistant' || sender === 'bot';
    msg.className = `ai-message ${isBot ? 'ai-bot' : 'ai-user'}`;
    msg.innerHTML = `
      <div class="ai-avatar"><i class="fas fa-${isBot ? 'robot' : 'user'}"></i></div>
      <div class="ai-bubble"><div style="line-height: 1.6;">${isBot ? formatMarkdown(text) : text.replace(/</g, "&lt;").replace(/>/g, "&gt;")}</div></div>
    `;
    chatBody.appendChild(msg);
    chatBody.scrollTop = chatBody.scrollHeight;
  }

  async function handleSend() {
    const text = input.value.trim();
    if (!text) return;
    addMessage(text, 'user');
    input.value = '';
    
    const loadingId = 'loading-' + Date.now();
    const loadingMsg = document.createElement('div');
    loadingMsg.id = loadingId;
    loadingMsg.className = `ai-message ai-bot`;
    loadingMsg.innerHTML = `
      <div class="ai-avatar"><i class="fas fa-robot"></i></div>
      <div class="ai-bubble"><p>Düşünüyor...</p></div>
    `;
    chatBody.appendChild(loadingMsg);
    chatBody.scrollTop = chatBody.scrollHeight;

    try {
      const res = await fetch('http://localhost:8080/api/chat', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ prompt: text })
      });
      const data = await res.json();
      
      document.getElementById(loadingId)?.remove();
      addMessage(data.response, 'assistant');
    } catch (e) {
      document.getElementById(loadingId)?.remove();
      addMessage('Bağlantı hatası: Sunucuya ulaşılamadı.', 'assistant');
    }
  }

  sendBtn.addEventListener('click', handleSend);
  input.addEventListener('keydown', (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  });

  const suggestionChips = document.querySelectorAll('.ai-suggestion-chip');
  suggestionChips.forEach(chip => {
    chip.addEventListener('click', () => {
      input.value = chip.textContent.trim();
      handleSend();
    });
  });
}

function initDummyButtons() {
  const saveSettingsBtn = document.getElementById('saveSettingsBtn');
  if (saveSettingsBtn) {
    saveSettingsBtn.addEventListener('click', () => {
      if (typeof showToast === 'function') {
        saveSettingsBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Kaydediliyor...';
        setTimeout(() => {
          saveSettingsBtn.innerHTML = '<i class="fas fa-save"></i> Ayarları Kaydet';
          showToast('Sistem ayarları başarıyla kaydedildi.', 'success');
        }, 800);
      }
    });
  }

  const userMenuBtn = document.getElementById('userMenuBtn');
  const userDropdownMenu = document.getElementById('userDropdownMenu');
  if (userMenuBtn && userDropdownMenu) {
    userMenuBtn.addEventListener('click', (e) => {
      e.stopPropagation();
      if (userDropdownMenu.style.display === 'none' || userDropdownMenu.style.display === '') {
        userDropdownMenu.style.display = 'flex';
      } else {
        userDropdownMenu.style.display = 'none';
      }
    });

    document.addEventListener('click', (e) => {
      if (!userDropdownMenu.contains(e.target) && e.target !== userMenuBtn) {
        userDropdownMenu.style.display = 'none';
      }
    });
  }

  // Profil Düzenle Modal
  const editProfileBtn = document.getElementById('editProfileBtn');
  const profileModal = document.getElementById('profileModal');
  const profileModalClose = document.getElementById('profileModalClose');
  const profileModalCancel = document.getElementById('profileModalCancel');
  const profileSaveBtn = document.getElementById('profileSaveBtn');

  if (editProfileBtn && profileModal) {
    editProfileBtn.addEventListener('click', (e) => {
      e.preventDefault();
      userDropdownMenu.style.display = 'none';
      profileModal.classList.add('active');
    });

    [profileModalClose, profileModalCancel].forEach(btn => {
      if (btn) btn.addEventListener('click', () => profileModal.classList.remove('active'));
    });

    if (profileSaveBtn) {
      profileSaveBtn.addEventListener('click', () => {
        if (typeof showToast === 'function') showToast('Profil başarıyla güncellendi.', 'success');
        profileModal.classList.remove('active');
      });
    }
  }

  // Şifre Değiştir Modal
  const changePasswordBtn = document.getElementById('changePasswordBtn');
  const passwordModal = document.getElementById('passwordModal');
  const passwordModalClose = document.getElementById('passwordModalClose');
  const passwordModalCancel = document.getElementById('passwordModalCancel');
  const passwordSaveBtn = document.getElementById('passwordSaveBtn');

  if (changePasswordBtn && passwordModal) {
    changePasswordBtn.addEventListener('click', (e) => {
      e.preventDefault();
      userDropdownMenu.style.display = 'none';
      passwordModal.classList.add('active');
    });

    [passwordModalClose, passwordModalCancel].forEach(btn => {
      if (btn) btn.addEventListener('click', () => passwordModal.classList.remove('active'));
    });

    if (passwordSaveBtn) {
      passwordSaveBtn.addEventListener('click', () => {
        if (typeof showToast === 'function') showToast('Şifreniz başarıyla değiştirildi.', 'success');
        passwordModal.classList.remove('active');
      });
    }
  }

  // Çıkış Yap
  const logoutBtn = document.getElementById('logoutBtn');
  if (logoutBtn) {
    logoutBtn.addEventListener('click', (e) => {
      e.preventDefault();
      userDropdownMenu.style.display = 'none';
      if (typeof showToast === 'function') showToast('Güvenli bir şekilde çıkış yapılıyor...', 'info');
      setTimeout(() => {
        window.location.reload();
      }, 1500);
    });
  }

  const quickScanBtn = document.getElementById('quickScanBtn');
  if (quickScanBtn) {
    quickScanBtn.addEventListener('click', () => {
      const scannerNavItem = document.getElementById('nav-scanner');
      if (scannerNavItem) {
        scannerNavItem.click();
      }
    });
  }

  const newBorrowBtn = document.getElementById('newBorrowBtn');
  if (newBorrowBtn) {
    newBorrowBtn.addEventListener('click', () => {
      if (typeof showToast === 'function') {
        showToast('Yeni ödünç verme paneli açılıyor...', 'info');
      }
    });
  }

  const downloadPdfBtn = document.getElementById('downloadPdfBtn');
  if (downloadPdfBtn) {
    downloadPdfBtn.addEventListener('click', () => {
      if (typeof showToast === 'function') {
        showToast('Rapor PDF olarak indiriliyor...', 'info');
      }
    });
  }

  const returnBtns = document.querySelectorAll('.btn-return-book');
  returnBtns.forEach(btn => {
    btn.addEventListener('click', () => {
      if (typeof showToast === 'function') {
        showToast('Kitap başarıyla iade alındı.', 'success');
      }
      btn.textContent = 'İade Edildi';
      btn.disabled = true;
      btn.style.opacity = '0.5';
      const tr = btn.closest('tr');
      if (tr) {
          const badge = tr.querySelector('.badge');
          if (badge) {
              badge.className = 'badge badge-success';
              badge.textContent = 'İade Edildi';
          }
      }
    });
  });

  const warnBtns = document.querySelectorAll('.btn-warn-user');
  warnBtns.forEach(btn => {
    btn.addEventListener('click', () => {
      if (typeof showToast === 'function') {
        showToast('Üyeye gecikme uyarısı gönderildi.', 'warning');
      }
      btn.textContent = 'Uyarıldı';
      btn.disabled = true;
      btn.style.opacity = '0.5';
    });
  });
}
