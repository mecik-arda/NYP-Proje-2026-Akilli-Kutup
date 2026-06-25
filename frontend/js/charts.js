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
    if (appData.members) {
      totalBorrows = appData.members.reduce((sum, m) => sum + (m.oduncAlinanMateryaller ? m.oduncAlinanMateryaller.length : 0), 0);
    }
    statBorrows.dataset.target = totalBorrows;
  }

  // Call the new dynamic renderers
  if(typeof renderActivityTimeline === 'function') renderActivityTimeline();
  if(typeof renderAssetOverview === 'function') renderAssetOverview();
}
export function updateUserInfo() {
  if (typeof Auth !== 'undefined') {
    const user = Auth.getUser();
    if (user) {
      const fullName = (user.ad + ' ' + user.soyad).trim();
      document.querySelectorAll('.user-name').forEach(span => span.textContent = fullName);
      document.querySelectorAll('.user-role').forEach(span => span.textContent = user.rol.toUpperCase() === 'ADMIN' ? 'Yönetici' : 'Üye');
      
      const welcomeHeader = document.querySelector('h1');
      const welcomeP = document.querySelector('.welcome-text p');
      if (welcomeHeader && welcomeHeader.textContent.includes('Hoş Geldin')) {
        welcomeHeader.textContent = 'Hoş Geldin, ' + (user.ad || fullName) + ' 👋';
      }
      if (welcomeP) {
        let pendingReturns = 0;
        if (appData.members) {
            pendingReturns = appData.members.reduce((sum, m) => sum + (m.oduncAlinanMateryaller ? m.oduncAlinanMateryaller.length : 0), 0);
        }
        let newRegs = appData.members ? appData.members.length : 0; 
        welcomeP.innerHTML = `Kütüphane sisteminizde toplam <strong>${newRegs} kayıtlı üye</strong> ve <strong>${pendingReturns} aktif ödünç</strong> işlemi var.`;
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

  let data = [];
  if (appData.books && appData.books.length > 0) {
      const categories = {};
      appData.books.forEach(b => {
          const cat = b.kategori || 'Diğer';
          categories[cat] = (categories[cat] || 0) + 1;
      });
      const colors = ['#6c5ce7', '#00cec9', '#fdcb6e', '#e17055', '#636e72', '#0984e3', '#d63031', '#00b894'];
      data = Object.keys(categories).map((k, i) => ({
          label: k,
          value: categories[k],
          color: colors[i % colors.length]
      })).sort((a, b) => b.value - a.value);
  } else {
      data = [{ label: 'Kayıt Yok', value: 1, color: '#636e72' }];
  }

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
  
  let totalBorrows = 0;
  if (appData.members) {
      totalBorrows = appData.members.reduce((sum, m) => sum + (m.oduncAlinanMateryaller ? m.oduncAlinanMateryaller.length : 0), 0);
  }
  
  const values = new Array(12).fill(0);
  if (totalBorrows > 0) {
      const currentMonth = new Date().getMonth();
      values[currentMonth] = totalBorrows;
  }
  
  const maxVal = Math.max(...values, 10);

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
  const recent = [...appData.books].slice(-5).reverse();
  list.innerHTML = recent.map(book => `
    <div class="book-list-item fade-in-up">
      <div class="book-icon">${book.kapakGorseli ? `<img src="${book.kapakGorseli}" alt="K" style="width:100%; height:100%; object-fit:cover; border-radius:8px;">` : '📖'}</div>
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
  // Calculate current borrows for each book
  const borrowCounts = {};
  if (appData.members) {
      appData.members.forEach(m => {
          if (m.oduncAlinanMateryaller) {
              m.oduncAlinanMateryaller.forEach(oduncItem => {
                  const bookId = typeof oduncItem === 'object' ? oduncItem.materyalId : oduncItem;
                  borrowCounts[bookId] = (borrowCounts[bookId] || 0) + 1;
              });
          }
      });
  }
  
  const popular = [...appData.books].sort((a, b) => (borrowCounts[b.id] || 0) - (borrowCounts[a.id] || 0)).slice(0, 5);
  list.innerHTML = popular.map((book, i) => `
    <div class="book-list-item fade-in-up" style="animation-delay: ${i*0.1}s">
      <div class="rank">#${i + 1}</div>
      <div class="book-info">
        <div class="book-title">${escapeHtml(book.baslik || 'Bilinmeyen Başlık')}</div>
        <div class="book-author">${escapeHtml(book.yazar || 'Yazar Yok')}</div>
      </div>
      <div class="rating">📖 ${(borrowCounts[book.id] || 0)} Ödünç</div>
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
      <td>${m.oduncAlinanMateryaller ? m.oduncAlinanMateryaller.length : 0}</td>
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
    btn.addEventListener('click', async () => {
      const memberId = btn.dataset.id;
      if (confirm('Bu üyeyi silmek istediğinize emin misiniz?')) {
        try {
          const res = await API.deleteUser(memberId);
          if (res && res.basarili) {
            appData.members = appData.members.filter(m => m.id !== memberId);
            renderMembersTable();
            if (typeof showToast === 'function') showToast('Üye silindi.', 'info');
          }
        } catch (err) {
          if (typeof showToast === 'function') showToast('Üye silinemedi (Yetkisiz işlem olabilir).', 'error');
        }
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
  const today = new Date();

  appData.members.forEach(m => {
    if (m.oduncAlinanMateryaller && m.oduncAlinanMateryaller.length > 0) {
      m.oduncAlinanMateryaller.forEach(oduncItem => {
        // Yeni format: obje { materyalId, oduncTarihi, iadeTarihi, ceza }
        const bookId = typeof oduncItem === 'object' ? oduncItem.materyalId : oduncItem;
        const book = bookMap.get(bookId);
        if (book) {
          const oduncTarihi = (typeof oduncItem === 'object' && oduncItem.oduncTarihi) ? oduncItem.oduncTarihi : null;
          const iadeTarihi = (typeof oduncItem === 'object' && oduncItem.iadeTarihi) ? oduncItem.iadeTarihi : null;
          const ceza = (typeof oduncItem === 'object' && oduncItem.ceza) ? oduncItem.ceza : 0;

          // İade tarihi hesapla (ödünç tarihi + 14 gün)
          let sonIadeTarihi = '-';
          if (oduncTarihi) {
            try {
              const oduncDate = new Date(oduncTarihi);
              const dueDate = new Date(oduncDate);
              dueDate.setDate(dueDate.getDate() + 14);
              sonIadeTarihi = dueDate.toLocaleDateString('tr-TR');
            } catch(e) { /* tarih parse hatası */ }
          }

          // Gecikme durumunu kontrol et
          const isOverdue = oduncTarihi && !iadeTarihi && (() => {
            try {
              const oduncDate = new Date(oduncTarihi);
              const dueDate = new Date(oduncDate);
              dueDate.setDate(dueDate.getDate() + 14);
              return today > dueDate;
            } catch(e) { return false; }
          })();

          // Durum belirle
          let durum = 'Aktif';
          let durumClass = 'badge-success';
          if (iadeTarihi) {
            durum = 'İade Edildi';
            durumClass = 'badge-info';
          } else if (isOverdue) {
            durum = 'Gecikmiş';
            durumClass = 'badge-warning';
          }

          borrows.push({
            book: book,
            user: m,
            date: oduncTarihi ? new Date(oduncTarihi).toLocaleDateString('tr-TR') : new Date().toLocaleDateString('tr-TR'),
            iadeTarihi: sonIadeTarihi,
            iadeEdildi: !!iadeTarihi,
            isOverdue: isOverdue,
            durum: durum,
            durumClass: durumClass,
            ceza: ceza,
            bookId: bookId,
            userId: m.id
          });
        }
      });
    }
  });

  // En yeni ödünçler önce gelsin
  borrows.sort((a, b) => b.date.localeCompare(a.date));

  tbody.innerHTML = borrows.map(b => `
    <tr class="fade-in">
        <td>${escapeHtml(b.book.baslik)}</td>
        <td>${escapeHtml(b.user.isim)}</td>
        <td>${b.date}</td>
        <td>${b.iadeTarihi}</td>
        <td><span class="badge ${b.durumClass}">${b.durum}</span></td>
        <td>
          ${!b.iadeEdildi ? `<button class="btn btn-sm btn-outline btn-return-book" data-bookid="${b.bookId}" data-userid="${b.userId}">İade Al</button>` :
            `<span style="color:var(--text-tertiary);font-size:12px;">İade: Tamamlandı</span>`}
        </td>
    </tr>
  `).join('');

  if (borrows.length === 0) {
      tbody.innerHTML = '<tr><td colspan="6" style="text-align:center; padding:20px; color:var(--text-secondary);">Aktif ödünç işlemi bulunmamaktadır.</td></tr>';
  }

  // Ödünç badge'ini güncelle
  const borrowBadge = document.querySelector('#nav-borrow .nav-badge');
  if (borrowBadge) {
      const aktifSayisi = borrows.filter(b => !b.iadeEdildi).length;
      if (aktifSayisi > 0) {
          borrowBadge.textContent = aktifSayisi;
          borrowBadge.style.display = '';
      } else {
          borrowBadge.style.display = 'none';
      }
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
                const cezaMsg = res.ceza > 0 ? ` Gecikme cezası: ${res.ceza.toFixed(2)} TL` : '';
                if (typeof showToast === 'function') showToast('Kitap başarıyla iade alındı.' + cezaMsg, 'success');
                await loadDataFromAPI();
                renderBorrowsTable();
                updateCatalog();
                // Raporları da güncelle
                if (typeof renderReports === 'function') renderReports();
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
        ${book.kapakGorseli ? `<img src="${book.kapakGorseli}" alt="Kapak" style="width:100%; height:100%; object-fit:cover; border-radius:12px 12px 0 0;">` : '<div class="cover-placeholder">📚</div>'}
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

export function renderActivityTimeline() {
    const timeline = document.getElementById('activityTimeline');
    if (!timeline) return;

    const activities = [];
    if(appData.members) {
        appData.members.forEach(m => {
            if(m.oduncAlinanMateryaller && m.oduncAlinanMateryaller.length > 0) {
                m.oduncAlinanMateryaller.forEach(oduncItem => {
                    const bookId = typeof oduncItem === 'object' ? oduncItem.materyalId : oduncItem;
                    const oduncTarihi = (typeof oduncItem === 'object' && oduncItem.oduncTarihi) ? oduncItem.oduncTarihi : null;
                    const iadeTarihi = (typeof oduncItem === 'object' && oduncItem.iadeTarihi) ? oduncItem.iadeTarihi : null;
                    const book = appData.books ? appData.books.find(b => b.id.toString() === bookId.toString()) : null;
                    if(book) {
                        // Ödünç alındı olayı
                        activities.push({
                            action: 'Ödünç Verildi',
                            time: oduncTarihi || 'Bilinmiyor',
                            title: book.baslik,
                            user: m.isim,
                            type: 'borrow',
                            date: oduncTarihi || ''
                        });
                        // İade edildiyse iade olayı da ekle
                        if (iadeTarihi) {
                            activities.push({
                                action: 'İade Edildi',
                                time: iadeTarihi,
                                title: book.baslik,
                                user: m.isim,
                                type: 'return',
                                date: iadeTarihi
                            });
                        }
                    }
                });
            }
        });
    }

    if(activities.length === 0) {
        timeline.innerHTML = '<div style="padding:15px;text-align:center;color:var(--text-secondary)">Son işlem bulunmamaktadır.</div>';
        return;
    }

    // Tarihe göre sırala (en yeni önce)
    activities.sort((a, b) => b.date.localeCompare(a.date));

    const recentAct = activities.slice(0, 5);
    timeline.innerHTML = recentAct.map(act => `
        <div class="timeline-item">
            <div class="timeline-dot ${act.type}"></div>
            <div class="timeline-content">
                <div class="timeline-header">
                    <span class="timeline-action">${escapeHtml(act.action)}</span>
                    <span class="timeline-time">${act.time !== 'Bilinmiyor' ? act.time : ''}</span>
                </div>
                <p><strong>${escapeHtml(act.title)}</strong></p>
                <span class="timeline-user"><i class="fas fa-user"></i> ${escapeHtml(act.user)}</span>
            </div>
        </div>
    `).join('');
}

export async function renderReports() {
    try {
        const stats = await API.getStats();
        if (!stats) return;

        // Haftalık etkileşim grafiğini güncelle
        renderWeeklyChart(stats.haftalikEtkilesim);

        // Finansal özeti güncelle
        renderFinancialSummary(stats);

        // Dashboard istatistik kartlarını güncelle
        updateStatCards(stats);

    } catch(e) {
        console.error('Raporlar yüklenemedi:', e);
    }
}

function renderWeeklyChart(haftalikData) {
    const weeklyContainer = document.getElementById('weeklyChartContainer');
    if (!weeklyContainer || !haftalikData) return;

    const gunAdlari = ['Pzt', 'Sal', 'Çar', 'Per', 'Cum', 'Cmt', 'Paz'];
    const values = gunAdlari.map(g => haftalikData[g] || 0);
    const maxVal = Math.max(...values, 1);

    weeklyContainer.innerHTML = values.map((val, i) => {
        const heightPct = Math.max(5, (val / maxVal) * 100);
        return `<div style="flex:1; display:flex; flex-direction:column; align-items:center; gap:8px;">
            <span style="font-size:12px; color:var(--text-primary); font-weight:600;">${val}</span>
            <div style="width:100%; background: linear-gradient(to top, var(--accent-primary-dark), var(--accent-primary)); height:${heightPct}%; border-radius:4px 4px 0 0; min-height:4px; transition: height 0.6s ease;"></div>
            <span style="font-size:11px; color:var(--text-tertiary);">${gunAdlari[i]}</span>
        </div>`;
    }).join('');

    // Gün etiketlerini güncelle
    const daysContainer = document.getElementById('weeklyDaysContainer');
    if (daysContainer) {
        daysContainer.innerHTML = gunAdlari.map(g => `<span>${g}</span>`).join('');
    }
}

function renderFinancialSummary(stats) {
    const container = document.getElementById('financialSummaryContainer');
    if (!container) return;

    const tahsilEdilen = stats.tahsilEdilenCeza || 0;
    const bekleyen = stats.toplamBekleyenCeza || 0;
    const toplam = tahsilEdilen + bekleyen;

    container.innerHTML = `
        <li style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; padding-bottom: 15px; border-bottom: 1px solid rgba(255,255,255,0.05);">
            <div style="display: flex; align-items: center; gap: 10px;">
                <div style="width: 8px; height: 8px; border-radius: 50%; background: var(--accent-success);"></div>
                <span style="color: var(--text-secondary);">Tahsil Edilen (Toplam)</span>
            </div>
            <strong style="color: var(--accent-success);">+ ${tahsilEdilen.toLocaleString('tr-TR')} ₺</strong>
        </li>
        <li style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; padding-bottom: 15px; border-bottom: 1px solid rgba(255,255,255,0.05);">
            <div style="display: flex; align-items: center; gap: 10px;">
                <div style="width: 8px; height: 8px; border-radius: 50%; background: var(--accent-warning);"></div>
                <span style="color: var(--text-secondary);">Bekleyen Alacaklar</span>
            </div>
            <strong style="color: var(--accent-warning);">${bekleyen.toLocaleString('tr-TR')} ₺</strong>
        </li>
        <li style="display: flex; justify-content: space-between; align-items: center; padding-top: 5px;">
            <span style="color: var(--text-primary); font-weight: 600;">Toplam Beklenti</span>
            <strong style="color: white; font-size: 18px;">${toplam.toLocaleString('tr-TR')} ₺</strong>
        </li>
    `;
}

function updateStatCards(stats) {
    // Dashboard istatistik kartlarını gerçek verilerle güncelle
    const statBooks = document.querySelector('.stat-card-books .stat-value');
    const statDigital = document.querySelector('.stat-card-digital .stat-value');
    const statMembers = document.querySelector('.stat-card-members .stat-value');
    const statBorrows = document.querySelector('.stat-card-borrows .stat-value');

    if (statBooks && stats.toplamKitap) statBooks.dataset.target = stats.toplamKitap;
    if (statDigital && stats.toplamDijitalVarlik) statDigital.dataset.target = stats.toplamDijitalVarlik;
    if (statMembers && stats.toplamUye) statMembers.dataset.target = stats.toplamUye;
    if (statBorrows && stats.aktifOdunc !== undefined) statBorrows.dataset.target = stats.aktifOdunc;

    // Gecikmiş uyarısını güncelle
    const statChange = document.querySelector('.stat-card-borrows .stat-change');
    if (statChange && stats.gecikmis !== undefined) {
        if (stats.gecikmis > 0) {
            statChange.innerHTML = `<i class="fas fa-exclamation-triangle"></i> ${stats.gecikmis} gecikmiş`;
            statChange.className = 'stat-change warning';
        } else {
            statChange.innerHTML = `<i class="fas fa-check-circle"></i> Gecikme yok`;
            statChange.className = 'stat-change positive';
        }
    }

    // Animasyonu yeniden başlat
    if (typeof animateStats === 'function') animateStats();
}

export function renderAssetOverview() {
    const container = document.querySelector('.digital-assets-card .card-body');
    if (!container || !appData.assets) return;

    const counts = { 'E-Kitap': 0, 'Ses': 0, 'Sesli Kitap': 0, 'Belge': 0, 'PDF': 0, 'Görsel': 0 };
    appData.assets.forEach(a => {
        if(counts[a.tur] !== undefined) counts[a.tur]++;
    });

    const ebookCount = counts['E-Kitap'];
    const audioCount = counts['Ses'] + counts['Sesli Kitap'];
    const docCount = counts['Belge'] + counts['PDF'];
    const imgCount = counts['Görsel'];
    const total = ebookCount + audioCount + docCount + imgCount;

    if (total === 0) return;

    const calcPct = (c) => total > 0 ? (c / total) * 100 : 0;
    const calcSize = (c, avgMB) => (c * avgMB / 1024).toFixed(1);

    container.innerHTML = `
        <div class="storage-overview">
            <div class="storage-bar-container">
                <div class="storage-bar">
                    <div class="storage-segment" style="width: ${calcPct(ebookCount)}%; background: var(--accent-primary)" data-label="E-Kitaplar"></div>
                    <div class="storage-segment" style="width: ${calcPct(audioCount)}%; background: var(--accent-purple)" data-label="Sesli Kitaplar"></div>
                    <div class="storage-segment" style="width: ${calcPct(docCount)}%; background: var(--accent-success)" data-label="Belgeler"></div>
                    <div class="storage-segment" style="width: ${calcPct(imgCount)}%; background: var(--accent-warning)" data-label="Görseller"></div>
                </div>
                <div class="storage-info">
                    <span>${total} toplam dijital dosya indexlendi</span>
                    <span>%100</span>
                </div>
            </div>
        </div>
        <div class="asset-type-grid">
            <div class="asset-type-item">
                <div class="asset-type-icon epub"><i class="fas fa-book"></i></div>
                <div class="asset-type-info">
                    <span class="asset-type-name">E-Kitaplar</span>
                    <span class="asset-type-count">${ebookCount} dosya</span>
                </div>
                <span class="asset-type-size">${calcSize(ebookCount, 2.5)} GB</span>
            </div>
            <div class="asset-type-item">
                <div class="asset-type-icon audio"><i class="fas fa-headphones"></i></div>
                <div class="asset-type-info">
                    <span class="asset-type-name">Sesli Kitaplar</span>
                    <span class="asset-type-count">${audioCount} dosya</span>
                </div>
                <span class="asset-type-size">${calcSize(audioCount, 150)} GB</span>
            </div>
            <div class="asset-type-item">
                <div class="asset-type-icon document"><i class="fas fa-file-alt"></i></div>
                <div class="asset-type-info">
                    <span class="asset-type-name">Belgeler</span>
                    <span class="asset-type-count">${docCount} dosya</span>
                </div>
                <span class="asset-type-size">${calcSize(docCount, 1.2)} GB</span>
            </div>
            <div class="asset-type-item">
                <div class="asset-type-icon image"><i class="fas fa-image"></i></div>
                <div class="asset-type-info">
                    <span class="asset-type-name">Görseller</span>
                    <span class="asset-type-count">${imgCount} dosya</span>
                </div>
                <span class="asset-type-size">${calcSize(imgCount, 4.5)} GB</span>
            </div>
        </div>
    `;
}

// ═══════════════════════════════════════════════════════════════
// Aktif Üye Kartı Özellikleri (uye_prompt.md)
// ═══════════════════════════════════════════════════════════════

let previousActiveCount = 0;
let activeUsersCache = [];

export async function loadActiveMemberFeatures() {
  try {
    const stats = await API.getHourlyActiveStats();
    if (stats && stats.basarili) {
      renderActiveSparkline(stats.saatlikVeri || []);
      updateMonthlyIncrease(stats.artisBuAy || 0);
      window._hourlyActiveData = stats.saatlikVeri || [];
    }
    const activeData = await API.getActiveUsers();
    if (activeData && activeData.basarili) {
      activeUsersCache = activeData.kullanicilar || [];
      updateActiveMemberCard(activeData.aktifSayisi || 0, activeUsersCache);
      renderActiveDrawer(activeUsersCache);
      updateTooltip(activeUsersCache);
    }
  } catch (e) {
    console.warn('Aktif üye özellikleri yüklenemedi:', e);
  }
}

function updateActiveMemberCard(count, users) {
  const valueEl = document.getElementById('activeMemberCount');
  if (valueEl) {
    const oldCount = parseInt(valueEl.textContent, 10) || 0;
    if (count !== oldCount && oldCount > 0) {
      valueEl.classList.remove('flipping');
      void valueEl.offsetWidth;
      valueEl.classList.add('flipping');
      if (count > oldCount) {
        const card = document.getElementById('activeMemberCard');
        if (card) {
          card.classList.remove('flash-green');
          void card.offsetWidth;
          card.classList.add('flash-green');
        }
      }
    }
    valueEl.textContent = count.toLocaleString('tr-TR');
    valueEl.dataset.target = count;
    previousActiveCount = count;
  }
}

function updateTooltip(users) {
  const tooltip = document.getElementById('activeMemberTooltip');
  if (!tooltip) return;
  if (!users || users.length === 0) {
    tooltip.textContent = 'Şu an aktif kullanıcı yok';
    return;
  }
  const names = users.slice(0, 3).map(u => u.userName);
  const remaining = users.length - names.length;
  let text = names.join(', ');
  if (remaining > 0) text += ' ve ' + remaining + ' kişi daha şu an online';
  else text += ' şu an online';
  tooltip.textContent = text;
}

function updateMonthlyIncrease(increase) {
  const changeText = document.getElementById('activeMemberChangeText');
  if (changeText) changeText.textContent = '+' + increase + ' bu ay';
}

export function renderActiveDrawer(users) {
  const body = document.getElementById('activeDrawerBody');
  const countEl = document.getElementById('activeDrawerCount');
  if (countEl) countEl.textContent = (users ? users.length : 0) + ' kişi online';
  if (!body) return;
  if (!users || users.length === 0) {
    body.innerHTML = '<div class="active-drawer-empty"><i class="fas fa-user-clock"></i><p>Şu an aktif kullanıcı bulunmuyor</p><p style="font-size:12px;margin-top:4px;">Yeni girişler burada canlı görünecek</p></div>';
    return;
  }
  body.innerHTML = users.map(u => {
    const initials = (u.userName || '??').split(' ').map(w => w[0]).join('').substring(0, 2).toUpperCase();
    const action = u.currentAction || 'Sistemi kullanıyor';
    const loginTime = u.loginTime ? formatTimeAgo(u.loginTime) : '';
    return '<div class="active-user-item"><div class="active-user-avatar">' + escapeHtml(initials) + '<span class="online-indicator"></span></div><div class="active-user-info"><div class="active-user-name">' + escapeHtml(u.userName) + '</div><div class="active-user-action">' + escapeHtml(action) + '</div></div><div class="active-user-time">' + escapeHtml(loginTime) + '</div></div>';
  }).join('');
}

function renderActiveSparkline(hourlyData) {
  const polyline = document.querySelector('#activeSparkline polyline');
  if (!polyline || !hourlyData || hourlyData.length === 0) return;
  const max = Math.max(...hourlyData, 1);
  const points = hourlyData.map((val, i) => {
    const x = (i / (hourlyData.length - 1)) * 100;
    const y = 40 - (val / max) * 35;
    return x.toFixed(1) + ',' + y.toFixed(1);
  }).join(' ');
  polyline.setAttribute('points', points);
}

export function renderExpandedSparkline(hourlyData) {
  const canvas = document.getElementById('sparklineExpandedCanvas');
  if (!canvas || !hourlyData || hourlyData.length === 0) return;
  const container = canvas.parentElement;
  canvas.width = container.clientWidth;
  canvas.height = container.clientHeight;
  const ctx = canvas.getContext('2d');
  const w = canvas.width, h = canvas.height;
  const pad = { top: 20, right: 20, bottom: 40, left: 50 };
  const cw = w - pad.left - pad.right;
  const ch = h - pad.top - pad.bottom;
  ctx.clearRect(0, 0, w, h);
  const max = Math.max(...hourlyData, 1);
  const now = new Date();
  const grad = ctx.createLinearGradient(0, pad.top, 0, h - pad.bottom);
  grad.addColorStop(0, 'rgba(16, 185, 129, 0.3)');
  grad.addColorStop(1, 'rgba(16, 185, 129, 0.02)');
  ctx.beginPath();
  hourlyData.forEach((val, i) => {
    const x = pad.left + (i / (hourlyData.length - 1)) * cw;
    const y = pad.top + ch - (val / max) * ch;
    if (i === 0) ctx.moveTo(x, y); else ctx.lineTo(x, y);
  });
  ctx.lineTo(pad.left + cw, pad.top + ch);
  ctx.lineTo(pad.left, pad.top + ch);
  ctx.closePath();
  ctx.fillStyle = grad; ctx.fill();
  ctx.beginPath();
  ctx.strokeStyle = '#10b981'; ctx.lineWidth = 2.5; ctx.lineJoin = 'round';
  hourlyData.forEach((val, i) => {
    const x = pad.left + (i / (hourlyData.length - 1)) * cw;
    const y = pad.top + ch - (val / max) * ch;
    if (i === 0) ctx.moveTo(x, y); else ctx.lineTo(x, y);
  });
  ctx.stroke();
  hourlyData.forEach((val, i) => {
    const x = pad.left + (i / (hourlyData.length - 1)) * cw;
    const y = pad.top + ch - (val / max) * ch;
    ctx.beginPath(); ctx.arc(x, y, 3, 0, Math.PI * 2);
    ctx.fillStyle = '#10b981'; ctx.fill();
  });
  ctx.fillStyle = '#94a3b8'; ctx.font = '11px Inter, sans-serif'; ctx.textAlign = 'center';
  for (let i = 0; i < hourlyData.length; i += 4) {
    const x = pad.left + (i / (hourlyData.length - 1)) * cw;
    const d = new Date(now); d.setHours(d.getHours() - (23 - i));
    ctx.fillText(d.getHours().toString().padStart(2, '0') + ':00', x, h - pad.bottom + 20);
  }
  ctx.textAlign = 'right';
  for (let i = 0; i <= 4; i++) {
    const y = pad.top + ch - (i / 4) * ch;
    ctx.fillText(Math.round((max * i) / 4).toString(), pad.left - 10, y + 4);
  }
}

function formatTimeAgo(isoString) {
  if (!isoString) return '';
  try {
    const then = new Date(isoString);
    const diffMs = Date.now() - then;
    const diffMin = Math.floor(diffMs / 60000);
    if (diffMin < 1) return 'Az önce';
    if (diffMin < 60) return diffMin + ' dk önce';
    const diffH = Math.floor(diffMin / 60);
    if (diffH < 24) return diffH + ' saat önce';
    return Math.floor(diffH / 24) + ' gün önce';
  } catch { return ''; }
}
