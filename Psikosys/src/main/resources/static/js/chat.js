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