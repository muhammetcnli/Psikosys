// Basitleştirilmiş profile.js
document.addEventListener('DOMContentLoaded', function() {
    console.log('Profile page loaded');

    // Şifre görünürlüğü toggle - sadece varsa çalıştır
    const toggleButtons = document.querySelectorAll('.toggle-password');
    if (toggleButtons.length > 0) {
        toggleButtons.forEach(button => {
            button.addEventListener('click', function() {
                const input = this.previousElementSibling;
                if (input) {
                    const type = input.getAttribute('type') === 'password' ? 'text' : 'password';
                    input.setAttribute('type', type);

                    this.classList.toggle('fa-eye');
                    this.classList.toggle('fa-eye-slash');
                }
            });
        });
    }

    // Şifre gücü kontrolü - sadece input varsa
    const newPasswordInput = document.getElementById('newPassword');
    const confirmPasswordInput = document.getElementById('confirmPassword');

    if (newPasswordInput) {
        newPasswordInput.addEventListener('input', function() {
            const password = this.value;
            updatePasswordStrength(password);
            checkPasswordMatch();
        });
    }

    if (confirmPasswordInput) {
        confirmPasswordInput.addEventListener('input', checkPasswordMatch);
    }

    function updatePasswordStrength(password) {
        const strengthIndicator = document.querySelector('.password-strength');
        if (!strengthIndicator) return;

        if (password.length > 0) {
            strengthIndicator.classList.add('show');
            const strength = calculatePasswordStrength(password);

            const strengthFill = document.querySelector('.strength-fill');
            const strengthText = document.querySelector('.strength-text');

            if (strengthFill) {
                strengthFill.className = 'strength-fill ' + strength;
            }

            if (strengthText) {
                const currentLang = document.documentElement.lang || 'tr';
                const texts = {
                    weak: currentLang === 'en' ? 'Weak password' : 'Zayıf şifre',
                    medium: currentLang === 'en' ? 'Medium password' : 'Orta şifre',
                    strong: currentLang === 'en' ? 'Strong password' : 'Güçlü şifre'
                };
                strengthText.textContent = texts[strength] || texts.weak;
            }
        } else {
            strengthIndicator.classList.remove('show');
        }
    }

    function calculatePasswordStrength(password) {
        let score = 0;

        if (password.length >= 6) score += 1;
        if (password.length >= 10) score += 1;
        if (/[a-z]/.test(password)) score += 1;
        if (/[A-Z]/.test(password)) score += 1;
        if (/[0-9]/.test(password)) score += 1;
        if (/[^A-Za-z0-9]/.test(password)) score += 1;

        if (score < 3) return 'weak';
        if (score < 5) return 'medium';
        return 'strong';
    }

    function checkPasswordMatch() {
        if (!newPasswordInput || !confirmPasswordInput) return;

        const password = newPasswordInput.value;
        const confirm = confirmPasswordInput.value;
        const matchIndicator = document.querySelector('.password-match-indicator');

        if (!matchIndicator) return;

        if (confirm.length > 0) {
            if (password === confirm) {
                confirmPasswordInput.classList.add('valid');
                confirmPasswordInput.classList.remove('invalid');
                matchIndicator.classList.add('show');
                matchIndicator.classList.remove('error');

                const span = matchIndicator.querySelector('span');
                if (span) {
                    const currentLang = document.documentElement.lang || 'tr';
                    span.textContent = currentLang === 'en' ? 'Passwords match' : 'Şifreler eşleşiyor';
                }
            } else {
                confirmPasswordInput.classList.add('invalid');
                confirmPasswordInput.classList.remove('valid');
                matchIndicator.classList.add('show', 'error');

                const span = matchIndicator.querySelector('span');
                if (span) {
                    const currentLang = document.documentElement.lang || 'tr';
                    span.textContent = currentLang === 'en' ? 'Passwords do not match' : 'Şifreler eşleşmiyor';
                }
            }
        } else {
            confirmPasswordInput.classList.remove('valid', 'invalid');
            matchIndicator.classList.remove('show');
        }
    }
});