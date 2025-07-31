// Smooth scrolling için fonksiyon
function scrollToFeatures() {
    const featuresSection = document.getElementById('features');
    featuresSection.scrollIntoView({ behavior: 'smooth' });
}

// Navbar scroll efekti
window.addEventListener('scroll', function() {
    const nav = document.querySelector('.home-nav');
    if (window.scrollY > 50) {
        nav.style.boxShadow = '0 4px 10px rgba(0,0,0,0.1)';
    } else {
        nav.style.boxShadow = '0 2px 5px rgba(0,0,0,0.05)';
    }
});

// Feature kartları için görünürlük animasyonu
document.addEventListener('DOMContentLoaded', function() {
    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.style.opacity = '1';
                entry.target.style.transform = 'translateY(0)';
            }
        });
    }, { threshold: 0.2 });

    document.querySelectorAll('.feature-card').forEach(card => {
        card.style.opacity = '0';
        card.style.transform = 'translateY(20px)';
        card.style.transition = 'opacity 0.5s ease, transform 0.5s ease';
        observer.observe(card);
    });

    // Aktif dil butonunu belirle
    const urlParams = new URLSearchParams(window.location.search);
    let currentLang = urlParams.get('lang');

    // URL'de lang parametresi yoksa cookie'yi kontrol et
    if (!currentLang) {
        const cookies = document.cookie.split(';');
        for (let i = 0; i < cookies.length; i++) {
            const cookie = cookies[i].trim();
            if (cookie.startsWith('language=')) {
                currentLang = cookie.substring('language='.length, cookie.length);
                break;
            }
        }
    }

    // Varsayılan olarak Türkçe
    if (!currentLang) {
        currentLang = 'tr';
    }

    // Aktif butonun stilini güncelle
    const activeBtn = document.getElementById(currentLang + '-btn');
    if (activeBtn) {
        activeBtn.classList.add('active');
    }
});

// Dil değiştirme fonksiyonu
function changeLanguage(lang) {
    document.cookie = "language=" + lang + ";path=/";
    window.location.href = window.location.pathname + "?lang=" + lang;
}