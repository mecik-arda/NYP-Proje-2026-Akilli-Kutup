const Auth = (() => {
  const SESSION_KEY = 'akilli_kutup_session';

  async function hashPassword(password) {
    const encoder = new TextEncoder();
    const data = encoder.encode(password);
    const hashBuffer = await crypto.subtle.digest('SHA-256', data);
    const hashArray = Array.from(new Uint8Array(hashBuffer));
    return hashArray.map(b => b.toString(16).padStart(2, '0')).join('');
  }

  async function login(tcNo, password) {
    try {
      const passwordHash = await hashPassword(password);
      const response = await API.login(tcNo, passwordHash);

      if (response && response.basarili) {
        const session = {
          tcNo: tcNo,
          ad: response.ad || '',
          soyad: response.soyad || '',
          rol: response.rol || 'uye',
          girisZamani: Date.now()
        };
        sessionStorage.setItem(SESSION_KEY, JSON.stringify(session));
        return { success: true, user: session };
      }

      return { success: false, message: response?.mesaj || 'Giriş başarısız.' };
    } catch (err) {
      if (err.message === 'SERVER_UNREACHABLE') {
        return { success: false, message: 'Sunucuya ulaşılamıyor.' };
      }
      return { success: false, message: 'Bir hata oluştu.' };
    }
  }

  function logout() {
    sessionStorage.removeItem(SESSION_KEY);
    window.location.href = 'index.html';
  }

  function isAuthenticated() {
    const session = getSession();
    if (!session) return false;
    const elapsed = Date.now() - session.girisZamani;
    const MAX_SESSION = 8 * 60 * 60 * 1000;
    if (elapsed > MAX_SESSION) {
      logout();
      return false;
    }
    return true;
  }

  function getSession() {
    try {
      const raw = sessionStorage.getItem(SESSION_KEY);
      return raw ? JSON.parse(raw) : null;
    } catch {
      return null;
    }
  }

  function getUser() {
    return getSession();
  }

  function isAdmin() {
    const session = getSession();
    return session?.rol === 'admin';
  }

  return {
    hashPassword,
    login,
    logout,
    isAuthenticated,
    getSession,
    getUser,
    isAdmin
  };
})();
