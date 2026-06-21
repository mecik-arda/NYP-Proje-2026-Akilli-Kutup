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
