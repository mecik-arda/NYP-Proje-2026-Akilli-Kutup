import { API } from "./api.js";
import { Auth } from "./auth.js";
import { appData, catalogState } from "./store.js";
import * as UI from "./ui.js";
import * as Charts from "./charts.js";
import { escapeHtml, showToast } from "./utils.js";
document.addEventListener('DOMContentLoaded', async () => {
  if (typeof Auth !== 'undefined') {
      if (!Auth.isAuthenticated()) {
          window.location.href = 'login.html';
          return;
      } else {
          const u = Auth.getUser();
          if (u) {
              const nameEl = document.querySelector('.user-name');
              const roleEl = document.querySelector('.user-role');
              const avatarEl = document.querySelector('.user-avatar span:first-child');
              const headerWelcomeEl = document.querySelector('h1.dashboard-welcome');
              if (nameEl) nameEl.textContent = u.ad + ' ' + (u.soyad || '');
              if (roleEl) roleEl.textContent = u.rol.toUpperCase() === 'ADMIN' ? 'Y\u00f6netici' : '\u00dcye';
              if (avatarEl) avatarEl.textContent = (u.ad?.[0] || '') + (u.soyad?.[0] || '');
              if (headerWelcomeEl) headerWelcomeEl.textContent = 'Ho\u015f Geldin, ' + u.ad;
          }
          applyRBAC();
      }
  }
  initSidebar();
  if (typeof UI.initViewAllLinks === 'function') UI.initViewAllLinks();
  initTheme();
  initSearch();
  initNotifications();
  initFullscreen();
  initMobileMenu();
  await loadDataFromAPI();
  if (typeof updateDashboardStats === 'function') updateDashboardStats();
  if (typeof updateUserInfo === 'function') updateUserInfo();
  animateStats();
  renderDonutChart();
  renderBarChart();
  renderRecentBooks();
  renderPopularBooks();
  renderMembersTable();
  renderBorrowsTable();
  renderBookGrid();
  renderAssetGrid();
  // Raporları gerçek verilerle yükle
  if (typeof Charts !== 'undefined' && typeof Charts.renderReports === 'function') {
    Charts.renderReports();
  }
  initAddBookModal();
  initAddMemberModal();
  initAIChat();
  initDummyButtons();
  initCatalogFilters();
  initSettings();
  initActiveMemberCard();
  startActivityPing();
  if (typeof UI.initDigitalAssetsUI === 'function') UI.initDigitalAssetsUI();
});
export async function loadDataFromAPI() {
    try {
        if(typeof API !== 'undefined') {
            const materials = await API.getBooks();
            if(materials && Array.isArray(materials)) {
                appData.books = materials.filter(m => m.tur === 'Kitap');
                appData.assets = materials.filter(m => m.tur === 'DijitalMedya' || m.tur === 'Klasor').map(m => {
                    return {
                        id: m.id,
                        baslik: m.baslik,
                        tur: m.tur === 'Klasor' ? 'Klasor' : m.dijitalTur,
                        boyut: m.boyut || '-',
                        format: m.dosyaFormati || '-'
                    };
                });
            }
            
            const users = await API.getUsers();
            if(users && Array.isArray(users)) appData.members = users;
        }
    } catch (e) {
        console.error("API baglanti hatasi", e);
        if (e.message && (e.message.includes('Yetkisiz') || e.message.includes('401'))) {
            if (typeof showToast === 'function') showToast('Oturumunuzun s\u00fcresi doldu. L\u00fctfen tekrar giri\u015f yap\u0131n.', 'error');
            setTimeout(() => {
                if (typeof Auth !== 'undefined') Auth.logout();
                window.location.href = 'login.html';
            }, 1500);
            return;
        }
    }
    
    if (!appData.books || appData.books.length === 0) {
        appData.books = [
            { id: 1, baslik: 'K\u00fcrk Mantolu Madonna', yazar: 'Sabahattin Ali', stokAdedi: 5, birimFiyat: 45, odunc: 15 },
            { id: 2, baslik: 'Su\u00e7 ve Ceza', yazar: 'Fyodor Dostoyevski', stokAdedi: 3, birimFiyat: 60, odunc: 12 },
            { id: 3, baslik: 'Sefiller', yazar: 'Victor Hugo', stokAdedi: 0, birimFiyat: 80, odunc: 25 },
            { id: 4, baslik: '1984', yazar: 'George Orwell', stokAdedi: 2, birimFiyat: 35, odunc: 8 },
            { id: 5, baslik: 'Simyac\u0131', yazar: 'Paulo Coelho', stokAdedi: 10, birimFiyat: 40, odunc: 30 },
            { id: 6, baslik: 'Y\u00fcz\u00fcklerin Efendisi', yazar: 'J.R.R. Tolkien', stokAdedi: 1, birimFiyat: 120, odunc: 40 },
            { id: 7, baslik: '\u0130\u00e7imizdeki \u015eeytan', yazar: 'Sabahattin Ali', stokAdedi: 8, birimFiyat: 38, odunc: 5 }
        ];
    }
    
    if (!appData.members || appData.members.length === 0 || (appData.members.length === 1 && appData.members[0].isim === 'Tekrar Eden')) {
        appData.members = [
            { id: 'M-1021', isim: 'Ahmet Y\u0131lmaz', tcKimlikNo: '12345678901', email: 'ahmet.y@example.com' },
            { id: 'M-1022', isim: 'Ay\u015fe Demir', tcKimlikNo: '98765432109', email: 'ayse.demir@example.com' },
            { id: 'M-1023', isim: 'Mehmet Kaya', tcKimlikNo: '55555555555', email: 'mkaya@example.com' },
            { id: 'M-1024', isim: 'Zeynep \u00c7elik', tcKimlikNo: '33333333333', email: 'zeynep.c@example.com' },
            { id: 'M-1025', isim: 'Ali Vefa', tcKimlikNo: '11111111111', email: 'ali.vefa@example.com' },
            { id: 'M-1026', isim: 'Fatma G\u00fcl', tcKimlikNo: '22222222222', email: 'fgul@example.com' }
        ];
    }

    if (!appData.assets || appData.assets.length === 0) {
        appData.assets = [
            { id: 1, baslik: 'T\u00fcrk Edebiyat\u0131 Antolojisi', tur: 'E-Kitap', boyut: '12 MB', format: 'PDF' },
            { id: 2, baslik: 'Osmanl\u0131 Tarihi Belgeseli', tur: 'Video', boyut: '1.2 GB', format: 'MP4' },
            { id: 3, baslik: 'Klasik T\u00fcrk M\u00fczi\u011fi Koleksiyonu', tur: 'Ses', boyut: '340 MB', format: 'MP3' },
            { id: 4, baslik: 'Python Programlama Rehberi', tur: 'E-Kitap', boyut: '8 MB', format: 'EPUB' }
        ];
    }
}

Object.assign(window, UI, Charts, {API, Auth, appData, catalogState, escapeHtml, showToast});

// ═══════════════════════════════════════════════════════════════
// Aktif Üye Kartı Özellikleri (uye_prompt.md)
// ═══════════════════════════════════════════════════════════════

let sseConnection = null;
let activityPingInterval = null;

function initActiveMemberCard() {
  if (typeof Charts !== 'undefined' && typeof Charts.loadActiveMemberFeatures === 'function') {
    Charts.loadActiveMemberFeatures();
  }

  // Kart tıklaması: Quick Actions veya Drawer
  const card = document.getElementById('activeMemberCard');
  if (card) {
    card.addEventListener('click', (e) => {
      // Eğer quick action butonuna tıklandıysa işlem yapma
      if (e.target.closest('.qa-btn') || e.target.closest('.quick-actions-popover')) return;

      // Quick actions toggle
      card.classList.toggle('active-quick-actions');

      // Dışa tıklanınca kapat
      setTimeout(() => {
        const closeHandler = (ev) => {
          if (!card.contains(ev.target)) {
            card.classList.remove('active-quick-actions');
            document.removeEventListener('click', closeHandler);
          }
        };
        document.addEventListener('click', closeHandler, { once: true });
      }, 10);
    });
  }

  // Quick Action: Duyuru Gönder
  const qaAnnounce = document.getElementById('qaAnnounce');
  if (qaAnnounce) {
    qaAnnounce.addEventListener('click', (e) => {
      e.stopPropagation();
      card.classList.remove('active-quick-actions');
      openAnnouncementModal();
    });
  }

  // Quick Action: Oturumları Kapat
  const qaTerminate = document.getElementById('qaTerminateSessions');
  if (qaTerminate) {
    qaTerminate.addEventListener('click', async (e) => {
      e.stopPropagation();
      card.classList.remove('active-quick-actions');
      if (!confirm('Tüm aktif kullanıcı oturumlarını kapatmak istediğinize emin misiniz?')) return;
      try {
        const res = await API.terminateAllSessions();
        if (res && res.basarili) {
          showToast(res.mesaj || 'Tüm oturumlar kapatıldı', 'success');
          if (typeof Charts !== 'undefined' && typeof Charts.loadActiveMemberFeatures === 'function') {
            Charts.loadActiveMemberFeatures();
          }
        }
      } catch (err) {
        showToast('Oturumlar kapatılamadı: ' + err.message, 'error');
      }
    });
  }

  // Quick Action: CSV Export
  const qaExport = document.getElementById('qaExportCSV');
  if (qaExport) {
    qaExport.addEventListener('click', async (e) => {
      e.stopPropagation();
      card.classList.remove('active-quick-actions');
      try {
        await API.exportActiveUsersCSV();
        showToast('Liste CSV olarak indirildi', 'success');
      } catch (err) {
        showToast('Dışa aktarma başarısız: ' + err.message, 'error');
      }
    });
  }

  // Drawer overlay ve close
  const drawer = document.getElementById('activeDrawer');
  const overlay = document.getElementById('activeDrawerOverlay');
  const drawerClose = document.getElementById('activeDrawerClose');

  function openDrawer() {
    if (drawer) drawer.classList.add('open');
    if (overlay) overlay.classList.add('open');
    // Drawer'ı güncelle
    if (typeof Charts !== 'undefined' && typeof Charts.renderActiveDrawer === 'function') {
      Charts.renderActiveDrawer(activeUsersCache || []);
    }
  }

  function closeDrawer() {
    if (drawer) drawer.classList.remove('open');
    if (overlay) overlay.classList.add('open');
    if (overlay) overlay.classList.remove('open');
  }

  if (overlay) overlay.addEventListener('click', closeDrawer);
  if (drawerClose) drawerClose.addEventListener('click', closeDrawer);

  // Sağ taraftan drawer: kart hover'ında sağ kenara küçük bir "çek" ipucu
  // Ana drawer tetikleyici: kartın stat-icon'una veya belirli bir bölgeye tıklama
  const statIcon = card ? card.querySelector('.stat-icon') : null;
  if (statIcon) {
    statIcon.style.cursor = 'pointer';
    statIcon.addEventListener('click', (e) => {
      e.stopPropagation();
      card.classList.remove('active-quick-actions');
      openDrawer();
    });
  }

  // Sparkline tıklama: genişletilmiş grafik
  const sparklineContainer = document.getElementById('activeSparklineContainer');
  const expandedChart = document.getElementById('sparklineExpanded');
  const expandedClose = document.getElementById('sparklineExpandedClose');

  if (sparklineContainer && expandedChart) {
    sparklineContainer.style.cursor = 'pointer';
    sparklineContainer.addEventListener('click', (e) => {
      e.stopPropagation();
      card.classList.remove('active-quick-actions');
      expandedChart.classList.toggle('open');
      if (expandedChart.classList.contains('open')) {
        const data = window._hourlyActiveData || [];
        if (typeof Charts !== 'undefined' && typeof Charts.renderExpandedSparkline === 'function') {
          setTimeout(() => Charts.renderExpandedSparkline(data), 100);
        }
      }
    });
  }

  if (expandedClose && expandedChart) {
    expandedClose.addEventListener('click', () => expandedChart.classList.remove('open'));
  }

  // Duyuru modalı
  const announcementModal = document.getElementById('announcementModal');
  const announcementClose = document.getElementById('announcementModalClose');
  const announcementCancel = document.getElementById('announcementModalCancel');
  const announcementSend = document.getElementById('announcementSendBtn');

  if (announcementClose) announcementClose.addEventListener('click', () => announcementModal.style.display = 'none');
  if (announcementCancel) announcementCancel.addEventListener('click', () => announcementModal.style.display = 'none');
  if (announcementSend) {
    announcementSend.addEventListener('click', async () => {
      const msg = document.getElementById('announcementMessage')?.value?.trim();
      if (!msg) { showToast('Lütfen bir mesaj yazın', 'warning'); return; }
      try {
        const res = await API.sendAnnouncement(msg);
        if (res && res.basarili) {
          showToast(res.mesaj || 'Duyuru gönderildi', 'success');
          announcementModal.style.display = 'none';
          document.getElementById('announcementMessage').value = '';
        }
      } catch (err) {
        showToast('Duyuru gönderilemedi: ' + err.message, 'error');
      }
    });
  }

  // Broadcast toast close
  const broadcastClose = document.getElementById('broadcastToastClose');
  if (broadcastClose) {
    broadcastClose.addEventListener('click', () => {
      document.getElementById('broadcastToast')?.classList.remove('show');
    });
  }

  // SSE bağlantısını başlat
  initSSEConnection();
}

function openAnnouncementModal() {
  const modal = document.getElementById('announcementModal');
  if (modal) modal.style.display = 'flex';
}

function initSSEConnection() {
  if (sseConnection) {
    sseConnection.close();
  }

  try {
    const rawSession = sessionStorage.getItem('akilli_kutup_session');
    if (!rawSession) return;
    const session = JSON.parse(rawSession);
    if (!session.token) return;

    const baseUrl = 'http://localhost:8080';
    sseConnection = new EventSource(baseUrl + '/api/aktif-kullanicilar/stream?token=' + encodeURIComponent(session.token));

    sseConnection.addEventListener('connected', () => {
      console.log('[SSE] Bağlantı kuruldu');
    });

    sseConnection.addEventListener('activeCount', (event) => {
      try {
        const data = JSON.parse(event.data);
        if (data.count !== undefined) {
          const count = data.count;
          const users = activeUsersCache || [];
          updateActiveMemberCard(count, users);
          updateTooltip(users);
        }
      } catch (e) { /* ignore */ }
    });

    sseConnection.addEventListener('activeUsers', (event) => {
      try {
        const data = JSON.parse(event.data);
        if (data.users) {
          activeUsersCache = data.users;
          updateActiveMemberCard(data.users.length, data.users);
          updateTooltip(data.users);
          if (typeof Charts !== 'undefined' && typeof Charts.renderActiveDrawer === 'function') {
            const drawer = document.getElementById('activeDrawer');
            if (drawer && drawer.classList.contains('open')) {
              Charts.renderActiveDrawer(data.users);
            }
          }
        }
      } catch (e) { /* ignore */ }
    });

    sseConnection.addEventListener('userJoined', (event) => {
      try {
        const data = JSON.parse(event.data);
        showToast(data.userName + ' giriş yaptı', 'info');
        if (typeof Charts !== 'undefined' && typeof Charts.loadActiveMemberFeatures === 'function') {
          Charts.loadActiveMemberFeatures();
        }
      } catch (e) { /* ignore */ }
    });

    sseConnection.addEventListener('userLeft', (event) => {
      try {
        const data = JSON.parse(event.data);
        if (data.userName) {
          // Sessizce güncelle
          if (typeof Charts !== 'undefined' && typeof Charts.loadActiveMemberFeatures === 'function') {
            Charts.loadActiveMemberFeatures();
          }
        }
      } catch (e) { /* ignore */ }
    });

    sseConnection.addEventListener('announcement', (event) => {
      try {
        const data = JSON.parse(event.data);
        const toast = document.getElementById('broadcastToast');
        const msgEl = document.getElementById('broadcastToastMessage');
        if (toast && msgEl && data.message) {
          msgEl.textContent = data.message;
          toast.classList.add('show');
          setTimeout(() => toast.classList.remove('show'), 8000);
        }
      } catch (e) { /* ignore */ }
    });

    sseConnection.onerror = () => {
      console.warn('[SSE] Bağlantı hatası, 10sn sonra tekrar deneniyor...');
      setTimeout(initSSEConnection, 10000);
    };
  } catch (e) {
    console.warn('[SSE] Bağlantı kurulamadı:', e);
  }
}

function startActivityPing() {
  if (activityPingInterval) clearInterval(activityPingInterval);

  // Mevcut sayfa bilgisini al
  function getCurrentAction() {
    const activeNav = document.querySelector('.nav-item.active span');
    if (activeNav) return activeNav.textContent.trim() + ' sayfasını görüntülüyor';
    const breadcrumb = document.getElementById('breadcrumbCurrent');
    if (breadcrumb) return breadcrumb.textContent.trim() + ' sayfasını görüntülüyor';
    return 'Gösterge panelini görüntülüyor';
  }

  // İlk ping hemen
  sendActivityPing(getCurrentAction());

  // Her 30 saniyede bir ping
  activityPingInterval = setInterval(() => {
    sendActivityPing(getCurrentAction());
  }, 30000);

  // Sayfa değişikliklerini dinle
  const navItems = document.querySelectorAll('.nav-item');
  navItems.forEach(item => {
    item.addEventListener('click', () => {
      setTimeout(() => sendActivityPing(getCurrentAction()), 500);
    });
  });
}

async function sendActivityPing(action) {
  try {
    if (typeof API !== 'undefined' && typeof API.reportActivity === 'function') {
      await API.reportActivity(action);
    }
  } catch (e) {
    // Sessizce hata
  }
}

// Yardımcı fonksiyonlar (charts.js'ten referans alır)
function updateActiveMemberCard(count, users) {
  const valueEl = document.getElementById('activeMemberCount');
  if (!valueEl) return;
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
