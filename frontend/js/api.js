import { showToast } from './utils.js';

export const API = (() => {
  const BASE_URL = 'http://localhost:8080';

  async function request(method, endpoint, body = null) {
    const options = {
      method,
      headers: { 'Content-Type': 'application/json' }
    };
    try {
      const rawSession = sessionStorage.getItem('akilli_kutup_session');
      if (rawSession) {
        const session = JSON.parse(rawSession);
        if (session.token) {
          options.headers['Authorization'] = `Bearer ${session.token}`;
        }
      }
    } catch(e) {}
    if (body) {
      options.body = JSON.stringify(body);
    }
    try {
      const response = await fetch(`${BASE_URL}${endpoint}`, options);
      if (!response.ok) {
        const error = await response.text();
        throw new Error(error || `HTTP ${response.status}`);
      }
      const text = await response.text();
      return text ? JSON.parse(text) : null;
    } catch (err) {
      if (err.name === 'TypeError' && err.message.includes('fetch')) {
        throw new Error('SERVER_UNREACHABLE');
      }
      throw err;
    }
  }

  async function get(endpoint) {
    return request('GET', endpoint);
  }

  async function post(endpoint, data) {
    return request('POST', endpoint, data);
  }

  async function put(endpoint, data) {
    return request('PUT', endpoint, data);
  }

  async function del(endpoint) {
    return request('DELETE', endpoint);
  }

  async function checkServerStatus() {
    try {
      await get('/api/status');
      return true;
    } catch {
      return false;
    }
  }

  async function getBooks() {
    return get('/api/kitaplar');
  }

  async function getUsers() {
    return get('/api/kullanicilar');
  }

  async function addBook(data) {
    return post('/api/kitaplar', data);
  }

  async function borrowBook(bookId, userId) {
    return post('/api/odunc', { bookId: bookId, userId: userId });
  }

  async function returnBook(bookId, userId) {
    return post('/api/iade', { bookId: bookId, userId: userId });
  }

  async function searchBooks(query) {
    return get(`/api/kitaplar/ara?q=${encodeURIComponent(query)}`);
  }

  async function login(tcNo, password) {
    return post('/api/giris', { tcKimlikNo: tcNo, sifre: password });
  }

  async function getStats() {
    return get('/api/istatistikler');
  }

  async function getBorrowHistory() {
    return get('/api/odunc-gecmisi');
  }

  async function getDigitalAssets() {
    return get('/api/dijital-varliklar');
  }

  async function deleteBook(bookId) {
    return del(`/api/kitaplar/${bookId}`);
  }

  async function updateBook(bookId, data) {
    return put(`/api/kitaplar/${bookId}`, data);
  }

  async function updateUser(userId, data) {
    return put(`/api/kullanicilar/${userId}`, data);
  }

  async function updateProfile(data) {
    return post('/api/profil', data);
  }

  async function updatePassword(data) {
    return post('/api/sifre', data);
  }

  async function getNotifications() {
    return get('/api/bildirimler');
  }

  async function markAllNotificationsRead() {
    return post('/api/bildirimler/okundu', {});
  }

  async function uploadAsset(data) {
    return post('/api/dijital/upload', data);
  }

  async function createFolder(data) {
    return post('/api/dijital/klasor', data);
  }

  async function addUser(data) {
    return post('/api/kullanicilar', data);
  }

  async function deleteUser(id) {
    return del(`/api/kullanicilar/${id}`);
  }

  // ─── Aktif Kullanıcılar ──────────────────────────────────────

  async function getActiveUsers() {
    return get('/api/aktif-kullanicilar');
  }

  async function reportActivity(action) {
    return post('/api/aktif-kullanicilar/aktivite', { action: action });
  }

  async function getHourlyActiveStats() {
    return get('/api/istatistikler/saatlik-aktif');
  }

  async function sendAnnouncement(message) {
    return post('/api/duyuru', { mesaj: message });
  }

  async function terminateAllSessions() {
    return post('/api/oturumlari-kapat', {});
  }

  async function exportActiveUsersCSV() {
    const rawSession = sessionStorage.getItem('akilli_kutup_session');
    let token = '';
    try {
      if (rawSession) {
        const session = JSON.parse(rawSession);
        if (session.token) token = session.token;
      }
    } catch(e) {}
    const resp = await fetch(`${BASE_URL}/api/aktif-kullanicilar/export`, {
      headers: { 'Authorization': `Bearer ${token}` }
    });
    if (!resp.ok) throw new Error('Export failed');
    const blob = await resp.blob();
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'aktif_kullanicilar.csv';
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
    return { basarili: true };
  }

  return {
    checkServerStatus,
    getBooks,
    getUsers,
    addUser,
    deleteUser,
    addBook,
    borrowBook,
    returnBook,
    searchBooks,
    login,
    getStats,
    getDigitalAssets,
    deleteBook,
    updateBook,
    updateUser,
    updateProfile,
    updatePassword,
    getNotifications,
    markAllNotificationsRead,
    uploadAsset,
    createFolder,
    getBorrowHistory,
    getActiveUsers,
    reportActivity,
    getHourlyActiveStats,
    sendAnnouncement,
    terminateAllSessions,
    exportActiveUsersCSV
  };
})();
