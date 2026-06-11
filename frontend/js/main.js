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
              if (roleEl) roleEl.textContent = u.rol.toUpperCase() === 'ADMIN' ? 'Yönetici' : 'Üye';
              if (avatarEl) avatarEl.textContent = (u.ad?.[0] || '') + (u.soyad?.[0] || '');
              if (headerWelcomeEl) headerWelcomeEl.textContent = 'Hoş Geldin, ' + u.ad;
          }
          applyRBAC();
      }
  }
  initSidebar();
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
  initAddBookModal();
  initAddMemberModal();
  initAIChat();
  initDummyButtons();
  initCatalogFilters();
  initSettings();
});
export async function loadDataFromAPI() {
    try {
        if(typeof API !== 'undefined') {
            const books = await API.getBooks();
            if(books && Array.isArray(books)) appData.books = books;
            
            const users = await API.getUsers();
            if(users && Array.isArray(users)) appData.members = users;
        }
    } catch (e) {
        console.error("API baglanti hatasi", e);
        if (e.message && (e.message.includes('Yetkisiz') || e.message.includes('401'))) {
            if (typeof showToast === 'function') showToast('Oturumunuzun süresi doldu. Lütfen tekrar giriş yapın.', 'error');
            setTimeout(() => {
                if (typeof Auth !== 'undefined') Auth.logout();
                window.location.href = 'login.html';
            }, 1500);
            return;
        }
    }
    
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
Object.assign(window, UI, Charts, {API, Auth, appData, catalogState, escapeHtml, showToast});
