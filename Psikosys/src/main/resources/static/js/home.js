// Smooth scroll davranışını etkinleştir
document.documentElement.style.scrollBehavior = 'smooth';

document.addEventListener('DOMContentLoaded', function() {
    // Ana sayfaya scrollable özellik ekle
    document.body.classList.add('home-body');

    // Smooth scrolling için navigasyon linklerini dinle
    const navLinks = document.querySelectorAll('.nav-links a[href^="#"]');
    navLinks.forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            const targetId = this.getAttribute('href').substring(1);
            const targetElement = document.getElementById(targetId);
            if (targetElement) {
                targetElement.scrollIntoView({
                    behavior: 'smooth',
                    block: 'start'
                });
            }
        });
    });

    // Aktif dil butonunu ayarla
    setActiveLanguageButton();
});

// Features bölümüne scroll
function scrollToFeatures() {
    const featuresSection = document.getElementById('features');
    if (featuresSection) {
        featuresSection.scrollIntoView({
            behavior: 'smooth',
            block: 'start'
        });
    }
}

// About bölümüne scroll
function scrollToAbout() {
    const aboutSection = document.getElementById('about');
    if (aboutSection) {
        aboutSection.scrollIntoView({
            behavior: 'smooth',
            block: 'start'
        });
    }
}

// Dil değiştirme fonksiyonu
function changeLanguageHome(selectedLang) {
    console.log('Dil değiştirme isteği:', selectedLang);

    fetch('/change-language', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: 'language=' + encodeURIComponent(selectedLang)
    })
        .then(response => {
            if (response.ok) {
                console.log('Dil başarıyla değiştirildi:', selectedLang);
                window.location.reload();
            } else {
                console.error('Dil değiştirme başarısız:', response.status);
            }
        })
        .catch(error => {
            console.error('Dil değiştirme hatası:', error);
        });
}

// Aktif dil butonunu ayarla
function setActiveLanguageButton() {
    const userLang = getCookie('user_language') || 'tr';

    document.querySelectorAll('.language-btn').forEach(btn => {
        btn.classList.remove('active');
    });

    const activeBtn = document.getElementById(userLang + '-btn');
    if (activeBtn) {
        activeBtn.classList.add('active');
    }
}

// Cookie okuma fonksiyonu
function getCookie(name) {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop().split(';').shift();
    return null;
}