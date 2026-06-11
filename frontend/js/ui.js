import { API } from "./api.js";
import { Auth } from "./auth.js";
import { appData, catalogState } from "./store.js";
import { loadDataFromAPI } from "./main.js";
import { updateCatalog, renderRecentBooks, renderBookGrid, renderMembersTable, renderBorrowsTable } from "./charts.js";
import { escapeHtml, showToast } from "./utils.js";
export let notificationsData = [];


export function applyRBAC() {
  if (!Auth.isAdmin()) {
    
    const restrictedNavs = ['nav-members', 'nav-reports', 'nav-settings'];
    restrictedNavs.forEach(id => {
      const el = document.getElementById(id);
      if (el) el.style.display = 'none';
    });

    const style = document.createElement('style');
    style.innerHTML = `
      .admin-only,
      button:has(i.fa-plus),
      button:has(i.fa-user-plus),
      button:has(i.fa-book-medical),
      .action-btn.edit-btn,
      .action-btn.delete-btn,
      .action-btn[onclick*="delete"],
      .action-btn[onclick*="edit"] {
        display: none !important;
      }
    `;
    document.head.appendChild(style);

    const restrictedPages = ['members', 'reports', 'settings'];
    const activeNav = document.querySelector('.nav-item.active');
    if (activeNav && restrictedPages.includes(activeNav.dataset.page)) {
      const dashBtn = document.getElementById('nav-dashboard');
      if (dashBtn) dashBtn.click();
    }
  }
}
export function initSidebar() {
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
export function initMobileMenu() {
  const mobileBtn = document.getElementById('mobileMenuBtn');
  const sidebar = document.querySelector('.sidebar');
  if (mobileBtn && sidebar) {
    mobileBtn.addEventListener('click', () => {
      sidebar.classList.toggle('open');
    });
  }
}
export function initTheme() {
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
export function initSearch() {
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
export function renderNotifications() {
  const notifList = document.getElementById('notificationList');
  if (!notifList) return;
  if (!notificationsData || notificationsData.length === 0) {
      notifList.innerHTML = '<div style="padding: 15px; text-align: center; color: var(--text-tertiary);">Bildiriminiz yok.</div>';
      const dot = document.querySelector('.notification-dot');
      if (dot) dot.style.display = 'none';
      return;
  }
  notifList.innerHTML = notificationsData.map(n => `
    <div class="notification-item ${n.unread ? 'unread' : ''}" data-id="${n.id}">
      <div class="notification-icon ${n.type}"><i class="fas ${n.icon}"></i></div>
      <div class="notification-content">
        <p class="notification-text">${escapeHtml(n.text)}</p>
        <span class="notification-time">${escapeHtml(n.time)}</span>
      </div>
    </div>
  `).join('');
  const dot = document.querySelector('.notification-dot');
  if (dot) dot.style.display = notificationsData.some(n => n.unread) ? 'block' : 'none';
}
export async function fetchNotifications() {
  try {
    notificationsData = await API.getNotifications() || [];
    renderNotifications();
  } catch(e) {
    console.error("Bildirimler alinamadi", e);
  }
}
export function initNotifications() {
  const notifBtn = document.getElementById('notificationBtn');
  const notifPanel = document.querySelector('.notification-panel');
  const markReadBtn = document.getElementById('markAllReadBtn');

  fetchNotifications();

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
    markReadBtn.addEventListener('click', async () => {
      try {
        await API.markAllNotificationsRead();
        notificationsData.forEach(n => n.unread = false);
        renderNotifications();
        if (typeof showToast === 'function') {
          showToast('Tüm bildirimler okundu olarak işaretlendi.', 'success');
        }
      } catch (e) {
        if (typeof showToast === 'function') showToast('Hata oluştu.', 'error');
      }
    });
  }
}
export function initFullscreen() {
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
export function updateBreadcrumb(text) {
  const el = document.getElementById('breadcrumbCurrent');
  if (el) el.textContent = text;
}
export function initAddBookModal() {
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

  const borrowBtn = document.getElementById('detailsBorrowBtn');
  if (borrowBtn) {
      borrowBtn.addEventListener('click', async () => {
          if (!borrowBtn.dataset.id) return;
          const user = typeof Auth !== 'undefined' ? Auth.getUser() : null;
          if (!user) {
              if (typeof showToast === 'function') showToast('Lütfen önce giriş yapın.', 'warning');
              return;
          }
          borrowBtn.disabled = true;
          borrowBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> İşleniyor...';
          try {
              const res = await API.borrowBook(borrowBtn.dataset.id, user.tcNo);
              if (res && res.basarili) {
                  if (typeof showToast === 'function') showToast('Kitap başarıyla ödünç alındı.', 'success');
                  detailsModal.classList.remove('active');
                  await loadDataFromAPI();
                  updateCatalog();
                  renderRecentBooks();
              } else {
                  if (typeof showToast === 'function') showToast(res?.mesaj || 'Ödünç alma işlemi başarısız oldu.', 'error');
              }
          } catch (e) {
              if (typeof showToast === 'function') showToast('Bir hata oluştu.', 'error');
          } finally {
              borrowBtn.disabled = false;
              borrowBtn.innerHTML = '<i class="fas fa-hand-holding"></i> Ödünç Al';
          }
      });
  }

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
        
        const newId = appData.books.length > 0 ? Math.max(...appData.books.map(b => parseInt(b.id) || 0)) + 1 : 1;
        appData.books.push({
          id: newId,
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
export function initAddMemberModal() {
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
    saveBtn.addEventListener('click', async (e) => {
      if (form.checkValidity()) {
        e.preventDefault();
        const name = document.getElementById('memberName').value;
        const tc = document.getElementById('memberTc').value;
        const email = document.getElementById('memberEmail').value;
        
        const editId = modal.dataset.editId;
        if (editId) {
          try {
              const res = await API.updateUser(editId, { isim: name, tcKimlikNo: tc, email: email });
              if(res && res.basarili) {
                  const memberIndex = appData.members.findIndex(m => m.id === editId);
                  if (memberIndex !== -1) {
                    appData.members[memberIndex].isim = name;
                    appData.members[memberIndex].tcKimlikNo = tc;
                    appData.members[memberIndex].email = email;
                    if (typeof showToast === 'function') showToast('Üye bilgileri güncellendi.', 'success');
                    renderMembersTable();
                  }
              }
          } catch(err) {
              if (typeof showToast === 'function') showToast('Güncelleme başarısız.', 'error');
          }
        } else {
          const maxNum = appData.members.length > 0 
            ? Math.max(...appData.members.map(m => parseInt((m.id || '').replace('M-', '')) || 0)) 
            : 1020;
          const newId = 'M-' + (maxNum + 1);
          appData.members.push({
            id: newId,
            isim: name,
            tcKimlikNo: tc,
            email: email,
            rol: 'uye'
          });
          if (typeof showToast === 'function') showToast('Yeni üye başarıyla kaydedildi.', 'success');
          renderMembersTable();
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

export function initAIChat() {
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
      const session = Auth.getSession();
      const res = await fetch('/api/chat', {
        method: 'POST',
        headers: { 
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${session?.token || ''}`
        },
        body: JSON.stringify({ prompt: text })
      });
      const data = await res.json();
      
      document.getElementById(loadingId)?.remove();
      if (res.ok) {
        addMessage(data.response, 'assistant');
      } else {
        addMessage('Sunucu hatası: ' + (data.response || data.error || 'Bilinmeyen hata'), 'assistant');
      }
    } catch (e) {
      document.getElementById(loadingId)?.remove();
      addMessage('Bağlantı hatası: Sunucuya ulaşılamadı. CORS veya ağ sorunu.', 'assistant');
      console.error("AI Chat Fetch Error:", e);
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
export function initDummyButtons() {
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

  const editProfileBtn = document.getElementById('editProfileBtn');
  const profileModal = document.getElementById('profileModal');
  const profileModalClose = document.getElementById('profileModalClose');
  const profileModalCancel = document.getElementById('profileModalCancel');
  const profileSaveBtn = document.getElementById('profileSaveBtn');

  if (editProfileBtn && profileModal) {
    editProfileBtn.addEventListener('click', (e) => {
      e.preventDefault();
      userDropdownMenu.style.display = 'none';
      const currentUser = typeof Auth !== 'undefined' ? Auth.getUser() : null;
      if (currentUser) {
          const profileNameInput = document.getElementById('profileNameInput');
          const profileRoleInput = document.getElementById('profileRoleInput');
          const profileApiKeyInput = document.getElementById('profileApiKeyInput');
          if (profileNameInput) profileNameInput.value = currentUser.ad || currentUser.isim || '';
          if (profileRoleInput) profileRoleInput.value = currentUser.rol || '';
          if (profileApiKeyInput) profileApiKeyInput.value = currentUser.geminiApiKey || '';
      }
      profileModal.classList.add('active');
    });

    [profileModalClose, profileModalCancel].forEach(btn => {
      if (btn) btn.addEventListener('click', () => profileModal.classList.remove('active'));
    });

      if (profileSaveBtn) {
      profileSaveBtn.addEventListener('click', async () => {
        const profileNameInput = document.getElementById('profileNameInput');
        const profileApiKeyInput = document.getElementById('profileApiKeyInput');
        if (profileNameInput) {
            try {
                await API.updateProfile({ 
                    isim: profileNameInput.value, 
                    geminiApiKey: profileApiKeyInput ? profileApiKeyInput.value : undefined 
                });
                
                // Update session
                if (typeof Auth !== 'undefined') {
                    let user = Auth.getSession();
                    if (user) {
                        user.isim = profileNameInput.value;
                        user.ad = profileNameInput.value;
                        if (profileApiKeyInput) user.geminiApiKey = profileApiKeyInput.value;
                        sessionStorage.setItem('akilli_kutup_session', JSON.stringify(user));
                    }
                }
                
                if (typeof showToast === 'function') showToast('Profil başarıyla güncellendi.', 'success');
                profileModal.classList.remove('active');
            } catch (err) {
                if (typeof showToast === 'function') showToast('Profil güncellenirken hata oluştu.', 'error');
            }
        }
      });
    }
  }

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
      passwordSaveBtn.addEventListener('click', async () => {
        const oldPw = document.getElementById('oldPasswordInput')?.value;
        const newPw = document.getElementById('newPasswordInput')?.value;
        const confirmPw = document.getElementById('newPasswordConfirmInput')?.value;
        
        if (!oldPw || !newPw || !confirmPw) {
            if (typeof showToast === 'function') showToast('Tüm alanları doldurun.', 'error');
            return;
        }
        if (newPw !== confirmPw) {
            if (typeof showToast === 'function') showToast('Yeni şifreler eşleşmiyor.', 'error');
            return;
        }
        
        try {
            await API.updatePassword({ eskiSifre: oldPw, yeniSifre: newPw });
            if (typeof showToast === 'function') showToast('Şifreniz başarıyla değiştirildi.', 'success');
            passwordModal.classList.remove('active');
            document.getElementById('passwordForm')?.reset();
        } catch (err) {
            if (typeof showToast === 'function') showToast('Mevcut şifre hatalı veya bir hata oluştu.', 'error');
        }
      });
    }
  }

  const logoutBtn = document.getElementById('logoutBtn');
  if (logoutBtn) {
    logoutBtn.addEventListener('click', (e) => {
      e.preventDefault();
      userDropdownMenu.style.display = 'none';
      if (typeof showToast === 'function') showToast('Güvenli bir şekilde çıkış yapılıyor...', 'info');
      setTimeout(() => {
        if (typeof Auth !== 'undefined') {
            Auth.logout();
        } else {
            window.location.href = 'login.html';
        }
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
      const modal = document.getElementById('borrowManualModal');
      const userSelect = document.getElementById('borrowManualUserSelect');
      const bookSelect = document.getElementById('borrowManualBookSelect');
      
      if (modal && userSelect && bookSelect) {
          userSelect.innerHTML = appData.members.map(u => `<option value="${escapeHtml(u.id)}">${escapeHtml(u.isim)} (${escapeHtml(u.tcKimlikNo)})</option>`).join('');
          bookSelect.innerHTML = appData.books.filter(b => b.stokAdedi > 0).map(b => `<option value="${escapeHtml(b.id.toString())}">${escapeHtml(b.baslik)}</option>`).join('');
          modal.classList.add('active');
      }
    });
  }

  const borrowManualModalClose = document.getElementById('borrowManualModalClose');
  const borrowManualModalCancel = document.getElementById('borrowManualModalCancel');
  const borrowManualSaveBtn = document.getElementById('borrowManualSaveBtn');

  const closeBorrowManualModal = () => {
      const modal = document.getElementById('borrowManualModal');
      if (modal) modal.classList.remove('active');
  };

  if (borrowManualModalClose) borrowManualModalClose.addEventListener('click', closeBorrowManualModal);
  if (borrowManualModalCancel) borrowManualModalCancel.addEventListener('click', closeBorrowManualModal);
  
  if (borrowManualSaveBtn) {
      borrowManualSaveBtn.addEventListener('click', async () => {
          const userId = document.getElementById('borrowManualUserSelect').value;
          const bookId = document.getElementById('borrowManualBookSelect').value;
          if (!userId || !bookId) {
              if (typeof showToast === 'function') showToast('Lütfen kullanıcı ve kitap seçin.', 'error');
              return;
          }
          
          borrowManualSaveBtn.disabled = true;
          borrowManualSaveBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> İşleniyor...';
          
          try {
              if (typeof API !== 'undefined') {
                  const res = await API.borrowBook(bookId, userId);
                  if (res && res.basarili) {
                      if (typeof showToast === 'function') showToast('Kitap başarıyla ödünç verildi.', 'success');
                      closeBorrowManualModal();
                      await loadDataFromAPI();
                      renderBorrowsTable();
                      updateCatalog();
                      renderRecentBooks();
                  } else {
                      if (typeof showToast === 'function') showToast(res?.mesaj || 'İşlem başarısız.', 'error');
                  }
              }
          } catch(e) {
              if (typeof showToast === 'function') showToast('Bir hata oluştu.', 'error');
          } finally {
              borrowManualSaveBtn.disabled = false;
              borrowManualSaveBtn.innerHTML = '<i class="fas fa-check"></i> Ödünç Ver';
          }
      });
  }

  const downloadPdfBtn = document.getElementById('downloadPdfBtn');
  if (downloadPdfBtn) {
    downloadPdfBtn.addEventListener('click', () => {
      const printWindow = window.open('', '_blank');
      printWindow.document.write(`
        <html>
        <head>
          <title>Kütüphane Raporu</title>
          <style>
            body { font-family: Arial, sans-serif; padding: 20px; }
            table { width: 100%; border-collapse: collapse; margin-top: 20px; }
            th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
            th { background-color: #f2f2f2; }
          </style>
        </head>
        <body>
          <h2>Akıllı Kütüphane - Güncel Durum Raporu</h2>
          <p>Tarih: ${new Date().toLocaleDateString('tr-TR')}</p>
          <h3>Kitaplar</h3>
          <table>
            <tr><th>Başlık</th><th>Yazar</th><th>Stok</th></tr>
            ${appData.books.map(b => `<tr><td>${escapeHtml(b.baslik)}</td><td>${escapeHtml(b.yazar || '-')}</td><td>${escapeHtml(b.stokAdedi ? b.stokAdedi.toString() : '0')}</td></tr>`).join('')}
          </table>
          <h3>Üyeler</h3>
          <table>
            <tr><th>İsim</th><th>TC No</th><th>Rol</th></tr>
            ${appData.members.map(m => {
              const tcMask = (m.tcKimlikNo || '00000000000').substring(0, 3) + '*****' + (m.tcKimlikNo || '00000000000').substring(8);
              return `<tr><td>${escapeHtml(m.isim)}</td><td>${escapeHtml(tcMask)}</td><td>${escapeHtml(m.rol || 'uye')}</td></tr>`;
            }).join('')}
          </table>
        </body>
        </html>
      `);
      printWindow.document.close();
      printWindow.focus();
      setTimeout(() => printWindow.print(), 500);
      if (typeof showToast === 'function') {
        showToast('Rapor PDF olarak hazırlanıyor...', 'info');
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
export async function initSettings() {
  const saveBtn = document.getElementById('saveSettingsBtn');
  const backupBtn = document.getElementById('backupBtn');
  const tempSlider = document.getElementById('aiTempSlider');
  const tempDisp = document.getElementById('tempValDisp');

  if(tempSlider && tempDisp) {
      tempSlider.addEventListener('input', () => {
          tempDisp.textContent = tempSlider.value;
      });
  }

  // Load Settings
  try {
      const session = typeof Auth !== 'undefined' ? Auth.getSession() : null;
      const res = await fetch('/api/settings', {
          headers: { 'Authorization': `Bearer ${session?.token || ''}` }
      });
      if (res.ok) {
          const config = await res.json();
          if (document.getElementById('sessionTimeoutSelect')) document.getElementById('sessionTimeoutSelect').value = config.sessionTimeout || 30;
          if (document.getElementById('keyRotationCheckbox')) document.getElementById('keyRotationCheckbox').checked = config.keyRotationNotify || false;
          if (document.getElementById('auditTrailCheckbox')) document.getElementById('auditTrailCheckbox').checked = config.auditTrail || true;
          if (document.getElementById('aiTempSlider')) {
              document.getElementById('aiTempSlider').value = config.aiTemperature || 0.7;
              if (tempDisp) tempDisp.textContent = config.aiTemperature || 0.7;
          }
          if (document.getElementById('maxTokensInput')) document.getElementById('maxTokensInput').value = config.maxTokens || 800;
          if (document.getElementById('systemPromptTextarea')) document.getElementById('systemPromptTextarea').value = config.systemPrompt || '';
          if (document.getElementById('backupPeriodSelect')) document.getElementById('backupPeriodSelect').value = config.backupPeriod || 'daily';
          if (document.getElementById('lateFeeInput')) document.getElementById('lateFeeInput').value = config.lateFee || 5;
          if (document.getElementById('maxPenaltyInput')) document.getElementById('maxPenaltyInput').value = config.maxPenalty || 100;
          if (document.getElementById('gracePeriodInput')) document.getElementById('gracePeriodInput').value = config.gracePeriod || 2;
          if (document.getElementById('geminiApiKeyInput')) document.getElementById('geminiApiKeyInput').value = config.geminiApiKeyRaw || '********';
      }
  } catch(e) {
      console.error("Ayarlar yuklenemedi", e);
  }

  // Save Settings
  if (saveBtn) {
      saveBtn.addEventListener('click', async () => {
          saveBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Kaydediliyor...';
          const payload = {
              sessionTimeout: parseInt(document.getElementById('sessionTimeoutSelect').value),
              keyRotationNotify: document.getElementById('keyRotationCheckbox').checked,
              auditTrail: document.getElementById('auditTrailCheckbox').checked,
              aiTemperature: parseFloat(document.getElementById('aiTempSlider').value),
              maxTokens: parseInt(document.getElementById('maxTokensInput').value),
              systemPrompt: document.getElementById('systemPromptTextarea').value,
              backupPeriod: document.getElementById('backupPeriodSelect').value,
              lateFee: parseInt(document.getElementById('lateFeeInput').value),
              maxPenalty: parseInt(document.getElementById('maxPenaltyInput').value),
              gracePeriod: parseInt(document.getElementById('gracePeriodInput').value),
              geminiApiKeyRaw: document.getElementById('geminiApiKeyInput').value
          };
          
          try {
              const session = typeof Auth !== 'undefined' ? Auth.getSession() : null;
              const res = await fetch('/api/settings', {
                  method: 'POST',
                  headers: {
                      'Content-Type': 'application/json',
                      'Authorization': `Bearer ${session?.token || ''}`
                  },
                  body: JSON.stringify(payload)
              });
              
              if (res.ok) {
                  if (typeof showToast === 'function') showToast('Sistem ayarları başarıyla kaydedildi.', 'success');
              } else {
                  if (typeof showToast === 'function') showToast('Ayarlar kaydedilirken hata oluştu.', 'error');
              }
          } catch(e) {
              if (typeof showToast === 'function') showToast('Sunucu bağlantı hatası.', 'error');
          } finally {
              saveBtn.innerHTML = '<i class="fas fa-save"></i> Ayarları Kaydet';
          }
      });
  }

  // Backup Zip
  if (backupBtn) {
      backupBtn.addEventListener('click', async () => {
          const session = typeof Auth !== 'undefined' ? Auth.getSession() : null;
          try {
              const res = await fetch('/api/backup', {
                  headers: { 'Authorization': `Bearer ${session?.token || ''}` }
              });
              if (res.ok) {
                  const blob = await res.blob();
                  const url = URL.createObjectURL(blob);
                  const a = document.createElement('a');
                  a.href = url;
                  a.download = 'kutuphane_yedek.zip';
                  a.click();
                  URL.revokeObjectURL(url);
                  if (typeof showToast === 'function') showToast('Yedekleme başarılı, indiriliyor.', 'success');
              } else {
                  if (typeof showToast === 'function') showToast('Yedekleme başarısız veya yetkiniz yok.', 'error');
              }
          } catch(e) {
              if (typeof showToast === 'function') showToast('Yedekleme hatası.', 'error');
          }
      });
  }
}
