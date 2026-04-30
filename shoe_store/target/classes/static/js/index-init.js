/**
 * index-init.js
 * Tích hợp backend API cho trang chủ mới (Tailwind UI)
 * Phụ thuộc vào: main.js (checkAuth, updateCartCount, addToCart, formatPrice, showToast, logout)
 */

// ===================== PRODUCTS =====================

async function loadIndexProducts() {
    const grid = document.getElementById('productsGrid');
    if (!grid) return;

    try {
        const response = await fetch('/api/products?page=1&limit=8&sort=newest');
        if (!response.ok) throw new Error('API error');
        const data = await response.json();
        const products = data.products || [];

        if (products.length === 0) {
            grid.innerHTML = '<p class="col-span-full text-center text-outline py-20">Không có sản phẩm nào</p>';
            return;
        }

        grid.innerHTML = products.map(product => {
            const price = product.gia_km || product.gia;
            const oldPrice = product.gia_km ? product.gia : null;
            const badge = product.gia_km
                ? '<div class="absolute top-6 left-6 speed-skew bg-secondary text-on-secondary px-3 py-1 font-label text-[10px] font-bold uppercase tracking-tighter z-10">SALE</div>'
                : (product.is_new ? '<div class="absolute top-6 left-6 speed-skew bg-secondary text-on-secondary px-3 py-1 font-label text-[10px] font-bold uppercase tracking-tighter z-10">MỚI RA MẮT</div>' : '');
            const img = product.hinh_anh || '/uploads/placeholder.jpg';
            const link = `/product/${product.slug || product.id}`;
            const catName = product.ten_danh_muc || 'Giày thể thao';

            return `
            <div class="group cursor-pointer" onclick="openProductModal(${product.id})">
                <div class="relative aspect-[4/5] bg-surface-container-lowest rounded-xl p-8 flex items-center justify-center overflow-hidden mb-6 transition-all duration-500 group-hover:shadow-[0_20px_40px_rgba(47,46,47,0.06)]">
                    ${badge}
                    <button onclick="event.stopPropagation(); toggleWishlist(${product.id})" data-product-id="${product.id}"
                        class="wishlist-heart absolute top-6 right-6 w-10 h-10 rounded-full bg-white/80 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity z-10">
                        <span class="material-symbols-outlined text-outline">favorite_border</span>
                    </button>
                    <div class="w-full h-full flex items-center justify-center">
                        <img alt="${product.ten_sp}"
                            class="w-full h-auto object-contain transform group-hover:scale-110 group-hover:-rotate-6 transition-transform duration-500"
                            src="${img}" onerror="this.src='/images/placeholder.jpg'">
                    </div>
                    <button onclick="event.stopPropagation(); openProductModal(${product.id})"
                        class="absolute bottom-6 right-6 w-12 h-12 rounded-full primary-gradient text-white flex items-center justify-center opacity-0 translate-y-4 group-hover:opacity-100 group-hover:translate-y-0 transition-all duration-300 shadow-lg">
                        <span class="material-symbols-outlined">add_shopping_cart</span>
                    </button>
                </div>
                <div class="space-y-1">
                    <div class="font-label text-[10px] text-outline uppercase tracking-widest font-bold">${catName}</div>
                    <h4 class="font-headline font-bold text-lg hover:text-primary transition-colors">${product.ten_sp}</h4>
                    <div class="flex items-center gap-2">
                        <div class="font-headline font-bold text-primary">${formatPrice(price)}</div>
                        ${oldPrice ? `<div class="font-headline text-sm text-outline line-through">${formatPrice(oldPrice)}</div>` : ''}
                    </div>
                </div>
            </div>`;
        }).join('');

    } catch (error) {
        console.error('Error loading products:', error);
        if (grid) grid.innerHTML = '<p class="col-span-full text-center text-outline py-20">Lỗi tải sản phẩm</p>';
    }
}

// ===================== WISHLIST =====================

let wishlist = JSON.parse(localStorage.getItem('shoeshop_wishlist') || '[]');

function toggleWishlist(productId) {
    const idx = wishlist.indexOf(productId);
    if (idx === -1) {
        wishlist.push(productId);
        showToast('Đã thêm vào yêu thích', 'success');
    } else {
        wishlist.splice(idx, 1);
        showToast('Đã xóa khỏi yêu thích', 'info');
    }
    localStorage.setItem('shoeshop_wishlist', JSON.stringify(wishlist));
}

// ===================== AUTH MODAL =====================

let currentUser = null;

async function initIndexAuth() {
    currentUser = await checkAuth();
    if (currentUser) {
        showUserProfileInModal(currentUser.ho_ten || currentUser.email);
    }
    if (typeof updateCartCount === 'function') updateCartCount();
}

function openAuthModal() {
    const modal = document.getElementById('authModal');
    if (!modal) return;
    modal.classList.remove('hidden');
    document.body.style.overflow = 'hidden';
    if (currentUser) {
        showUserProfileInModal(currentUser.ho_ten || currentUser.email);
    } else {
        switchTab('login');
    }
}

function closeAuthModal() {
    const modal = document.getElementById('authModal');
    if (modal) modal.classList.add('hidden');
    document.body.style.overflow = '';
}

function switchTab(tab) {
    const loginTab = document.getElementById('loginTab');
    const signupTab = document.getElementById('signupTab');
    const loginForm = document.getElementById('loginForm');
    const signupForm = document.getElementById('signupForm');
    const userProfile = document.getElementById('userProfile');
    const authTabs = document.getElementById('authTabs');

    const loginErr = document.getElementById('loginError');
    const signupErr = document.getElementById('signupError');
    if (loginErr) loginErr.classList.add('hidden');
    if (signupErr) signupErr.classList.add('hidden');

    if (tab === 'login') {
        loginTab.classList.add('border-primary', 'text-primary');
        loginTab.classList.remove('border-transparent', 'text-outline');
        signupTab.classList.remove('border-primary', 'text-primary');
        signupTab.classList.add('border-transparent', 'text-outline');
        loginForm.classList.remove('hidden');
        signupForm.classList.add('hidden');
        userProfile.classList.add('hidden');
        authTabs.classList.remove('hidden');
    } else {
        signupTab.classList.add('border-primary', 'text-primary');
        signupTab.classList.remove('border-transparent', 'text-outline');
        loginTab.classList.remove('border-primary', 'text-primary');
        loginTab.classList.add('border-transparent', 'text-outline');
        signupForm.classList.remove('hidden');
        loginForm.classList.add('hidden');
        userProfile.classList.add('hidden');
        authTabs.classList.remove('hidden');
    }
}

function togglePassword(inputId) {
    const input = document.getElementById(inputId);
    if (!input) return;
    const btn = input.nextElementSibling;
    input.type = input.type === 'password' ? 'text' : 'password';
    if (btn) btn.textContent = input.type === 'password' ? 'visibility_off' : 'visibility';
}

async function handleLogin(e) {
    e.preventDefault();
    const email = document.getElementById('loginEmail').value;
    const password = document.getElementById('loginPassword').value;
    const errorEl = document.getElementById('loginError');

    try {
        const response = await fetch('/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, mat_khau: password })
        });
        const data = await response.json();

        if (response.ok) {
            currentUser = data.user;
            showUserProfileInModal(currentUser.ho_ten || currentUser.email);
            errorEl.classList.add('hidden');
            showToast('Đăng nhập thành công!', 'success');
            updateCartCount();
        } else {
            errorEl.textContent = data.message || 'Email hoặc mật khẩu không đúng';
            errorEl.classList.remove('hidden');
        }
    } catch {
        errorEl.textContent = 'Lỗi kết nối server';
        errorEl.classList.remove('hidden');
    }
}

async function handleSignup(e) {
    e.preventDefault();
    const email = document.getElementById('signupEmail').value;
    const password = document.getElementById('signupPassword').value;
    const confirm = document.getElementById('signupConfirm').value;
    const errorEl = document.getElementById('signupError');
    const successEl = document.getElementById('signupSuccess');

    if (password !== confirm) {
        errorEl.textContent = 'Mật khẩu xác nhận không khớp';
        errorEl.classList.remove('hidden');
        successEl.classList.add('hidden');
        return;
    }

    try {
        const ho_ten = email.split('@')[0];
        const response = await fetch('/api/auth/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ ho_ten, email, mat_khau: password })
        });
        const data = await response.json();

        if (response.ok) {
            errorEl.classList.add('hidden');
            successEl.textContent = 'Đăng ký thành công! Vui lòng đăng nhập';
            successEl.classList.remove('hidden');
            setTimeout(() => {
                document.getElementById('signupForm').reset();
                switchTab('login');
            }, 1500);
        } else {
            errorEl.textContent = data.message || 'Đăng ký thất bại';
            errorEl.classList.remove('hidden');
            successEl.classList.add('hidden');
        }
    } catch {
        errorEl.textContent = 'Lỗi kết nối server';
        errorEl.classList.remove('hidden');
    }
}

function showUserProfileInModal(name) {
    const loginForm = document.getElementById('loginForm');
    const signupForm = document.getElementById('signupForm');
    const authTabs = document.getElementById('authTabs');
    const userProfile = document.getElementById('userProfile');
    const userEmail = document.getElementById('userEmail');

    if (loginForm) loginForm.classList.add('hidden');
    if (signupForm) signupForm.classList.add('hidden');
    if (authTabs) authTabs.classList.add('hidden');
    if (userProfile) userProfile.classList.remove('hidden');
    if (userEmail) userEmail.textContent = name;
}

async function doLogout() {
    if (typeof logout === 'function') {
        await logout();
    } else {
        await fetch('/api/auth/logout', { method: 'POST' });
        window.location.reload();
    }
}

// ===================== CART MODAL =====================

async function loadCartModal() {
    const container = document.getElementById('cartItems');
    const emptyEl = document.getElementById('cartEmpty');
    const totalEl = document.getElementById('cartTotal');
    const checkoutBtn = document.getElementById('checkoutBtn');

    try {
        const response = await fetch('/api/cart');
        if (!response.ok) return;
        
        const data = await response.json();
        const items = data.items || data.cart || [];

        if (items.length === 0) {
            if (container) container.classList.add('hidden');
            if (emptyEl) emptyEl.classList.remove('hidden');
            if (totalEl) totalEl.textContent = '0 ₫';
            if (checkoutBtn) checkoutBtn.disabled = true;
        } else {
            if (container) container.classList.remove('hidden');
            if (emptyEl) emptyEl.classList.add('hidden');
            if (checkoutBtn) checkoutBtn.disabled = false;

            let total = 0;
            if (container) {
                container.innerHTML = items.map(item => {
                    const price = item.gia_km || item.gia;
                    total += price * item.so_luong;
                    const img = item.hinh_anh || '/uploads/placeholder.jpg';
                    return `
                    <div class="flex gap-4 border-b border-outline-variant/20 pb-4">
                        <img src="${img}" class="w-20 h-20 object-cover rounded-lg" onerror="this.src='/images/placeholder.jpg'">
                        <div class="flex-1">
                            <h4 class="font-headline font-bold text-sm">${item.ten_sp}</h4>
                            <div class="text-xs text-outline mb-2">Size: ${item.size || 'N/A'} · SL: ${item.so_luong}</div>
                            <div class="flex justify-between items-center">
                                <div class="font-headline font-bold text-primary">${formatPrice(price)}</div>
                                <button onclick="removeFromCart(${item.id}); loadCartModal();"
                                    class="text-outline hover:text-error transition-colors">
                                    <span class="material-symbols-outlined text-sm">delete</span>
                                </button>
                            </div>
                        </div>
                    </div>`;
                }).join('');
            }
            if (totalEl) totalEl.textContent = formatPrice(total);
        }
    } catch (e) {
        console.error('Error loading cart:', e);
    }
}

function openCartModal() {
    const modal = document.getElementById('cartModal');
    const panel = document.getElementById('cartPanel');
    if (!modal || !panel) return;
    modal.classList.remove('hidden');
    setTimeout(() => panel.classList.remove('translate-x-full'), 10);
    document.body.style.overflow = 'hidden';
    loadCartModal();
}

function closeCartModal() {
    const panel = document.getElementById('cartPanel');
    const modal = document.getElementById('cartModal');
    if (!panel || !modal) return;
    panel.classList.add('translate-x-full');
    setTimeout(() => modal.classList.add('hidden'), 300);
    document.body.style.overflow = '';
}

// ===================== WISHLIST MODAL =====================

function openWishlistModal() {
    const modal = document.getElementById('wishlistModal');
    const panel = document.getElementById('wishlistPanel');
    if (!modal || !panel) return;
    modal.classList.remove('hidden');
    setTimeout(() => panel.classList.remove('translate-x-full'), 10);
    document.body.style.overflow = 'hidden';
}

function closeWishlistModal() {
    const panel = document.getElementById('wishlistPanel');
    const modal = document.getElementById('wishlistModal');
    if (!panel || !modal) return;
    panel.classList.add('translate-x-full');
    setTimeout(() => modal.classList.add('hidden'), 300);
    document.body.style.overflow = '';
}

// ===================== PRODUCT GRID (prepare DOM) =====================

function prepareProductsGrid() {
    // Replace static product cards with dynamic loader div
    const section = document.querySelector('section.py-24.bg-surface-container-low');
    if (!section) return;
    const existingGrid = section.querySelector('.grid.grid-cols-1');
    if (!existingGrid) return;
    existingGrid.id = 'productsGrid';
    existingGrid.innerHTML = `
        <div class="col-span-full py-20 flex justify-center">
            <div class="animate-spin rounded-full h-12 w-12 border-4 border-primary border-t-transparent"></div>
        </div>`;
}

// ===================== INIT =====================

document.addEventListener('DOMContentLoaded', () => {
    // Prepare grid
    prepareProductsGrid();

    // Load data
    initIndexAuth();
    loadIndexProducts();

    // Expose functions globally (needed by inline onclick handlers)
    window.openAuthModal = openAuthModal;
    window.closeAuthModal = closeAuthModal;
    window.switchTab = switchTab;
    window.togglePassword = togglePassword;
    window.logout = doLogout;
    window.openCartModal = openCartModal;
    window.closeCartModal = closeCartModal;
    window.openWishlistModal = openWishlistModal;
    window.closeWishlistModal = closeWishlistModal;
    window.toggleWishlist = toggleWishlist;
    window.openProfileModal = () => window.location.href = '/account';
    window.openOrdersModal = () => window.location.href = '/orders';
    window.checkout = () => window.location.href = '/checkout';
    window.closeProductModal = () => {
        const m = document.getElementById('productModal');
        if (m) m.classList.add('hidden');
    };
    window.closeProfileModal = () => {
        const m = document.getElementById('profileModal');
        if (m) m.classList.add('hidden');
    };
    window.closeOrdersModal = () => {
        const m = document.getElementById('ordersModal');
        if (m) m.classList.add('hidden');
    };

    // Wire event listeners
    const loginBtn = document.getElementById('loginBtn');
    if (loginBtn) loginBtn.addEventListener('click', openAuthModal);

    const loginForm = document.getElementById('loginForm');
    if (loginForm) loginForm.addEventListener('submit', handleLogin);

    const signupForm = document.getElementById('signupForm');
    if (signupForm) signupForm.addEventListener('submit', handleSignup);

    // View all products button
    const viewAllBtn = document.querySelector('[data-action="view-all-products"]');
    if (viewAllBtn) viewAllBtn.addEventListener('click', () => window.location.href = '/products');

    // Escape closes modals
    document.addEventListener('keydown', e => {
        if (e.key === 'Escape') {
            closeAuthModal();
            closeCartModal();
            closeWishlistModal();
        }
    });
    
    // Initial UI state
    updateUserMenu();
    updateCartCount();
});
