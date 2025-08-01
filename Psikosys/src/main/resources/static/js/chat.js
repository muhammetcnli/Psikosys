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
});

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