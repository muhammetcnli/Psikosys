document.addEventListener('DOMContentLoaded', function() {
    // Chat sayfası yüklendiğinde sohbet konteynerini aşağı kaydır
    const chatHistory = document.querySelector('.chat-history');
    if (chatHistory) {
        chatHistory.scrollTop = chatHistory.scrollHeight;
    }

    // Dinamik mesaj giriş alanı için
    const textareas = document.querySelectorAll('.chat-input');
    textareas.forEach(textarea => {
        textarea.addEventListener('input', function() {
            this.style.height = 'auto';
            this.style.height = (this.scrollHeight) + 'px';
        });
    });

    // Form gönderimi öncesi kontrol
    const forms = document.querySelectorAll('.chat-form');
    forms.forEach(form => {
        form.addEventListener('submit', function(e) {
            const personalitySelect = this.querySelector('.personality-select');
            const questionInput = this.querySelector('.chat-input');

            console.log('Form gönderiliyor...');
            console.log('Seçilen kişilik:', personalitySelect ? personalitySelect.value : 'YOK');
            console.log('Mesaj:', questionInput ? questionInput.value : 'YOK');
        });
    });
});