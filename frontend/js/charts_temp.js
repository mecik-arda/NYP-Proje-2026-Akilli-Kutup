import { API } from "./api.js";
import { Auth } from "./auth.js";
import { appData, catalogState } from "./store.js";
import { escapeHtml, showToast } from "./utils.js";
import { loadDataFromAPI } from "./main.js";
export function animateStats() {
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
export function updateDashboardStats() {
  const statBooks = document.querySelector('.stat-card .stat-value');
  const statDigital = document.querySelector('.stat-card-digital .stat-value');
  const statMembers = document.querySelector('.stat-card-members .stat-value');
  const statBorrows = document.querySelector('.stat-card-borrows .stat-value');

  if (statBooks) statBooks.dataset.target = appData.books ? appData.books.length : 0;
  if (statDigital) statDigital.dataset.target = appData.assets ? appData.assets.length : 0;
  if (statMembers) statMembers.dataset.target = appData.members ? appData.members.length : 0;
  if (statBorrows) {
    let totalBorrows = 0;
    if (appData.books) {
      totalBorrows = appData.books.reduce((sum, b) => sum + (b.odunc || 0), 0);
    }
    statBorrows.dataset.target = totalBorrows;
  }

}
export function updateUserInfo() {
  if (typeof Auth !== 'undefined') {
    const user = Auth.getUser();
    if (user) {
      const fullName = (user.ad + ' ' + user.soyad).trim();
      document.querySelectorAll('.user-name').forEach(span => span.textContent = fullName);
      document.querySelectorAll('.user-role').forEach(span => span.textContent = user.rol.toUpperCase() === 'ADMIN' ? 'Yönetici' : 'Üye');
      
      const welcomeHeader = document.querySelector('h1');
      if (welcomeHeader && welcomeHeader.textContent.includes('Hoş Geldin')) {
        welcomeHeader.textContent = 'Hoş Geldin, ' + (user.ad || fullName) + ' 👋';
      }
      
      const inputs = document.querySelectorAll('input[type="text"]');
      inputs.forEach(input => {
        if (input.value === 'Ahmet Yılmaz') input.value = fullName;
      });

      document.querySelectorAll('.timeline-user').forEach(span => {
        if(span.innerHTML.includes('Ahmet Yılmaz')) span.innerHTML = `<i class="fas fa-user"></i> ${escapeHtml(fullName)}`;
      });
    }
  }
}
export function renderDonutChart() {
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

  const fragment = document.createDocumentFragment();
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
    circle.style.animation = 'dash 1.5s ease-out forwards'; 
    fragment.appendChild(circle);
    offset += dashLength;
  });
  svg.appendChild(fragment);

  const legend = svg.closest('.chart-card')?.querySelector('.chart-legend');
  if (legend) {
    legend.innerHTML = data.map(d =>
      `<div class="legend-item"><span class="legend-color" style="background:${d.color}"></span>${d.label} (%${d.value})</div>`
    ).join('');
  }
}
export function renderBarChart() {
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
export function renderRecentBooks() {
  const list = document.getElementById('recentBooksList');
  if (!list) return;
  const recent = appData.books.slice(0, 5);
  list.innerHTML = recent.map(book => `
    <div class="book-list-item fade-in-up">
      <div class="book-icon">📖</div>
      <div class="book-info">
        <div class="book-title">${escapeHtml(book.baslik || 'Bilinmeyen Başlık')}</div>
        <div class="book-author">${escapeHtml(book.yazar || 'Bilinmeyen Yazar')}</div>
      </div>
      <span class="badge ${book.stokAdedi > 0 ? 'badge-success' : 'badge-warning'}">
        ${book.stokAdedi > 0 ? 'Mevcut' : 'Tükendi'}
      </span>
    </div>
  `).join('');
}
export function renderPopularBooks() {
  const list = document.getElementById('popularBooksList');
  if (!list) return;
  const popular = [...appData.books].sort((a, b) => (b.odunc || 0) - (a.odunc || 0)).slice(0, 5);
  list.innerHTML = popular.map((book, i) => `
    <div class="book-list-item fade-in-up" style="animation-delay: ${i*0.1}s">
      <div class="rank">#${i + 1}</div>
      <div class="book-info">
        <div class="book-title">${escapeHtml(book.baslik || 'Bilinmeyen Başlık')}</div>
        <div class="book-author">${escapeHtml(book.yazar || 'Yazar Yok')}</div>
      </div>
      <div class="rating">⭐ ${book.stokAdedi || 0} Stok</div>
    </div>
  `).join('');
}
export function renderMembersTable() {
  const tbody = document.getElementById('membersTableBody');
  if (!tbody) return;
  tbody.innerHTML = appData.members.map(m => `
    <tr class="fade-in">
      <td>${escapeHtml(m.isim || 'Bilinmiyor')}</td>
      <td>${(m.tcKimlikNo || '00000000000').substring(0, 3)}*****${(m.tcKimlikNo || '00000000000').substring(8)}</td>
      <td>${escapeHtml(m.email || 'Yok')}</td>
      <td>${escapeHtml(m.id || '-')}</td>
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
export function renderBorrowsTable() {
  const tbody = document.getElementById('borrowTableBody');
  if (!tbody) return;

  const bookMap = new Map();
  if (appData.books && appData.books.length > 0) {
      appData.books.forEach(b => bookMap.set(b.id, b));
  }

  const borrows = [];
  appData.members.forEach(m => {
    if (m.oduncAlinanMateryaller && m.oduncAlinanMateryaller.length > 0) {
      m.oduncAlinanMateryaller.forEach(bookId => {
        const book = bookMap.get(bookId);
        if (book) {
          borrows.push({
            book: book,
            user: m,
            
            date: new Date().toLocaleDateString('tr-TR')
          });
        }
      });
    }
  });

  tbody.innerHTML = borrows.map(b => `
    <tr class="fade-in">
        <td>${escapeHtml(b.book.baslik)}</td>
        <td>${escapeHtml(b.user.isim)}</td>
        <td>${b.date}</td>
        <td>-</td>
        <td><span class="badge badge-success">Aktif</span></td>
        <td><button class="btn btn-sm btn-outline btn-return-book" data-bookid="${b.book.id}" data-userid="${b.user.id}">İade Al</button></td>
    </tr>
  `).join('');

  if (borrows.length === 0) {
      tbody.innerHTML = '<tr><td colspan="6" style="text-align:center; padding:20px; color:var(--text-secondary);">Aktif ödünç işlemi bulunmamaktadır.</td></tr>';
  }

  tbody.querySelectorAll('.btn-return-book').forEach(btn => {
    btn.addEventListener('click', async () => {
      const bookId = btn.dataset.bookid;
      const userId = btn.dataset.userid;
      if (confirm('Kitabı iade almak istediğinize emin misiniz?')) {
        btn.disabled = true;
        btn.textContent = 'İşleniyor...';
        try {
            const res = await API.returnBook(bookId, userId);
            if (res && res.basarili) {
                if (typeof showToast === 'function') showToast('Kitap başarıyla iade alındı.', 'success');
                await loadDataFromAPI();
                renderBorrowsTable();
                updateCatalog();
            } else {
                if (typeof showToast === 'function') showToast(res?.mesaj || 'İade işlemi başarısız.', 'error');
                btn.disabled = false;
                btn.textContent = 'İade Al';
            }
        } catch (e) {
            if (typeof showToast === 'function') showToast('Bir hata oluştu.', 'error');
            btn.disabled = false;
            btn.textContent = 'İade Al';
        }
      }
    });
  });
}
export function renderBookGrid() {
  updateCatalog();
}
export function updateCatalog() {
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
    if (catalogState.sort === 'newest') return ((parseInt(b.id) || 0) - (parseInt(a.id) || 0));
    if (catalogState.sort === 'oldest') return ((parseInt(a.id) || 0) - (parseInt(b.id) || 0));
    if (catalogState.sort === 'title-asc') return (a.baslik || '').localeCompare(b.baslik || '');
    if (catalogState.sort === 'title-desc') return (b.baslik || '').localeCompare(a.baslik || '');
    if (catalogState.sort === 'popular') return (b.odunc || 0) - (a.odunc || 0);
    return 0;
  });

  const countEl = document.getElementById('catalogCount');
  if (countEl) countEl.textContent = `${filtered.length} kitap bulundu`;

  const totalPages = Math.ceil(filtered.length / catalogState.itemsPerPage) || 1;
  if (catalogState.currentPage > totalPages) catalogState.currentPage = totalPages;
  
  const startIdx = (catalogState.currentPage - 1) * catalogState.itemsPerPage;
  const paginated = filtered.slice(startIdx, startIdx + catalogState.itemsPerPage);

  grid.className = catalogState.view === 'list' ? 'book-grid list-view' : 'book-grid';
  grid.innerHTML = paginated.map((book, i) => `
    <div class="book-card" data-id="${book.id}">
      <div class="book-cover">
        <div class="cover-placeholder">📚</div>
      </div>
      <div class="book-card-body">
        <div class="book-card-info">
            <h4 class="book-card-title">${escapeHtml(book.baslik)}</h4>
            <p class="book-card-author">${escapeHtml(book.yazar || 'Yazar Belirtilmemiş')}</p>
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
            const borrowBtn = document.getElementById('detailsBorrowBtn');
            if (borrowBtn) {
                borrowBtn.dataset.id = b.id;
                borrowBtn.style.display = b.stokAdedi > 0 ? 'inline-block' : 'none';
            }
            document.getElementById('bookDetailsModal').classList.add('active');
        }
    });
  });

  renderPagination(totalPages);
}
export function renderPagination(totalPages) {
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
export function initCatalogFilters() {
    const catalogFilterBtn = document.getElementById('catalogFilterBtn');
    const catalogFilters = document.getElementById('catalogFilters');

    if (catalogFilterBtn && catalogFilters) {
        catalogFilterBtn.addEventListener('click', () => {
            catalogFilters.classList.toggle('active');
            if (catalogFilters.style.display === 'none' || catalogFilters.style.display === '') {
                catalogFilters.style.display = 'flex';
            } else {
                catalogFilters.style.display = 'none';
            }
        });
    }

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
export function renderAssetGrid() {
  const grid = document.getElementById('assetGrid');
  if (!grid) return;
  
  const allCount = appData.assets.length;
  const counts = appData.assets.reduce((acc, a) => {
      acc[a.tur] = (acc[a.tur] || 0) + 1;
      return acc;
  }, {});
  
  const ebookCount = counts['E-Kitap'] || 0;
  const audioCount = (counts['Ses'] || 0) + (counts['Sesli Kitap'] || 0);
  const docCount = (counts['Belge'] || 0) + (counts['PDF'] || 0);
  const imgCount = counts['Görsel'] || 0;
  
  const tabs = document.querySelectorAll('.asset-tab .tab-count');
  if (tabs.length >= 5) {
      tabs[0].textContent = allCount.toLocaleString('tr-TR');
      tabs[1].textContent = ebookCount.toLocaleString('tr-TR');
      tabs[2].textContent = audioCount.toLocaleString('tr-TR');
      tabs[3].textContent = docCount.toLocaleString('tr-TR');
      tabs[4].textContent = imgCount.toLocaleString('tr-TR');
  }

  const filterMap = {
      'ebook': ['E-Kitap'],
      'audio': ['Ses', 'Sesli Kitap'],
      'document': ['Belge', 'PDF'],
      'image': ['Görsel']
  };

  const currentFilter = window.currentAssetFilter || 'all';
  let filteredAssets = appData.assets;
  
  if (currentFilter !== 'all') {
      const allowedTypes = filterMap[currentFilter] || [];
      filteredAssets = appData.assets.filter(a => allowedTypes.includes(a.tur));
  }

  const icons = { 'E-Kitap': '📄', 'Video': '🎬', 'Ses': '🎵', 'Görsel': '🖼️', 'Belge': '📝', 'Klasor': '📁' };
  
  if (filteredAssets.length === 0) {
      grid.innerHTML = '<div style="grid-column: 1/-1; text-align: center; padding: 40px; color: var(--text-tertiary);">Bu kategoride varlık bulunamadı.</div>';
      return;
  }

  grid.innerHTML = filteredAssets.map(asset => `
    <div class="asset-card fade-in" data-id="${escapeHtml(asset.id.toString())}">
      <div class="asset-icon">${icons[asset.tur] || '📁'}</div>
      <div class="asset-info">
        <h4>${escapeHtml(asset.baslik)}</h4>
        <p>${asset.tur === 'Klasor' ? 'Klasör' : `${escapeHtml(asset.tur)} · ${escapeHtml(asset.format)} · ${escapeHtml(asset.boyut)}`}</p>
      </div>
      ${asset.tur !== 'Klasor' ? `<button class="btn btn-sm btn-outline">İndir</button>` : `<button class="btn btn-sm btn-primary">Aç</button>`}
    </div>
  `).join('');
}
