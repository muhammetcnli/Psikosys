document.addEventListener('DOMContentLoaded', function() {
    // Chat geçmişini en alta kaydır
    const chatHistory = document.querySelector('.chat-history');
    if (chatHistory) {
        chatHistory.scrollTop = chatHistory.scrollHeight;
    }

    // Dinamik textarea boyutlandırma
    const textareas = document.querySelectorAll('.chat-input');
    textareas.forEach(textarea => {
        textarea.addEventListener('input', function() {
            this.style.height = 'auto';
            this.style.height = (this.scrollHeight) + 'px';
        });

        // Focus olduğunda placeholder'ı temizle
        textarea.addEventListener('focus', function() {
            this.style.borderColor = '#4a6fa5';
        });

        textarea.addEventListener('blur', function() {
            this.style.borderColor = '#d0d7de';
        });
    });

    // Profil butonu hover efekti
    const profileButton = document.querySelector('.profile-button');
    if (profileButton) {
        profileButton.addEventListener('mouseenter', function() {
            const icon = this.querySelector('.profile-icon');
            if (icon) {
                icon.style.transform = 'scale(1.05)';
                icon.style.transition = 'transform 0.2s ease';
            }
        });

        profileButton.addEventListener('mouseleave', function() {
            const icon = this.querySelector('.profile-icon');
            if (icon) {
                icon.style.transform = 'scale(1)';
            }
        });
    }

    // Chat silme butonları için event listener ekle
    const deleteButtons = document.querySelectorAll('.delete-chat-btn');
    deleteButtons.forEach(button => {
        button.addEventListener('click', function(event) {
            event.preventDefault();
            event.stopPropagation();
            const chatId = this.getAttribute('data-chat-id');
            showDeleteModal(chatId);
        });
    });
});

// Chat silme fonksiyonları
let chatToDelete = null;

function showDeleteModal(chatId) {
    chatToDelete = chatId;
    document.getElementById('deleteModal').style.display = 'block';
}

function hideDeleteModal() {
    document.getElementById('deleteModal').style.display = 'none';
    chatToDelete = null;
}

function confirmDelete() {
    if (chatToDelete) {
        // CSRF token'ı al
        const token = document.querySelector('meta[name="_csrf"]');
        const header = document.querySelector('meta[name="_csrf_header"]');

        const form = document.createElement('form');
        form.method = 'POST';
        form.action = `/chat/${chatToDelete}/delete`;

        // CSRF token ekle
        if (token && header) {
            const csrfInput = document.createElement('input');
            csrfInput.type = 'hidden';
            csrfInput.name = header.getAttribute('content');
            csrfInput.value = token.getAttribute('content');
            form.appendChild(csrfInput);
        }

        document.body.appendChild(form);
        form.submit();
    }
    hideDeleteModal();
}

// Modal dışına tıklayınca kapat
window.onclick = function(event) {
    const modal = document.getElementById('deleteModal');
    if (event.target === modal) {
        hideDeleteModal();
    }
}

// Dil değiştirme fonksiyonu
function changeLanguage() {
    const select = document.getElementById('languageSelect');
    const selectedLang = select.value;

    // CSRF token'ı al
    const token = document.querySelector('meta[name="_csrf"]');
    const header = document.querySelector('meta[name="_csrf_header"]');

    const headers = {
        'Content-Type': 'application/x-www-form-urlencoded',
    };

    // CSRF token varsa ekle
    if (token && header) {
        headers[header.getAttribute('content')] = token.getAttribute('content');
    }

    fetch('/change-language', {
        method: 'POST',
        headers: headers,
        body: 'language=' + encodeURIComponent(selectedLang)
    })
        .then(response => {
            if (response.ok) {
                location.reload(); // Sayfayı yenile
            } else {
                console.error('Dil değiştirme başarısız:', response.status);
            }
        })
        .catch(error => {
            console.error('Dil değiştirme hatası:', error);
        });
}