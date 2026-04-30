// ==================== UTILITY FUNCTIONS ====================

// Format price to VND
function formatPrice(price) {
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
    }).format(price);
}

// Show toast notification
function showToast(message, type = 'info') {
    let container = document.getElementById('toastContainer');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toastContainer';
        container.className = 'fixed bottom-6 right-6 z-[200] space-y-2';
        document.body.appendChild(container);
    }
    
    const toast = document.createElement('div');
    const bgClass = type === 'success' ? 'bg-green-600' : type === 'error' ? 'bg-red-600' : 'bg-on-surface';
    toast.className = `${bgClass} text-white px-6 py-3 rounded-xl shadow-2xl flex items-center gap-3 animate-slide-in font-bold text-sm min-w-[200px]`;
    
    let icon = 'info';
    if (type === 'success') icon = 'check_circle';
    if (type === 'error') icon = 'error';

    toast.innerHTML = `
        <span class="material-symbols-outlined text-lg">${icon}</span>
        <span>${message}</span>
    `;
    
    container.appendChild(toast);
    
    setTimeout(() => {
        toast.classList.add('opacity-0', 'translate-x-full');
        toast.style.transition = 'all 0.5s ease-out';
        setTimeout(() => toast.remove(), 500);
    }, 3000);
}

// Toggle mobile menu
function toggleMobileMenu() {
    const menu = document.getElementById('navMenu');
    menu.classList.toggle('show');
}

// ==================== USER AUTHENTICATION ====================

// Check if user is logged in
async function checkAuth() {
    try {
        const response = await fetch('/api/auth/me');
        const data = await response.json();
        return data.user || null;
    } catch {
        return null;
    }
}

// Update user menu based on login status
async function updateUserMenu() {
    const userMenu = document.getElementById('userMenu');
    if (!userMenu) return;
    
    const user = await checkAuth();
    
    if (user) {
        userMenu.innerHTML = `
            <div class="relative group">
                <button class="material-symbols-outlined text-stone-900 hover:scale-105 transition-transform cursor-pointer flex items-center justify-center">
                    person
                </button>
                <div class="absolute right-0 top-full w-48 pt-2 opacity-0 group-hover:opacity-100 pointer-events-none group-hover:pointer-events-auto transition-all z-[60]">
                    <div class="bg-white dark:bg-stone-900 rounded-2xl shadow-xl py-2 border border-outline-variant/20 overflow-hidden">
                        <div class="px-4 py-2 border-b border-outline-variant/10">
                            <p class="text-[10px] font-black uppercase tracking-widest text-outline">Xin chào,</p>
                            <p class="text-xs font-bold text-on-surface truncate">${user.ho_ten}</p>
                        </div>
                        <a href="/account" class="block px-4 py-2 text-xs font-bold hover:bg-primary/10 hover:text-primary transition-colors flex items-center gap-2">
                            <span class="material-symbols-outlined text-sm">account_circle</span> Tài khoản
                        </a>
                        <a href="/orders" class="block px-4 py-2 text-xs font-bold hover:bg-primary/10 hover:text-primary transition-colors flex items-center gap-2">
                            <span class="material-symbols-outlined text-sm">package</span> Đơn hàng
                        </a>
                        <hr class="my-2 border-outline-variant/20">
                        <button onclick="logout()" class="w-full text-left px-4 py-2 text-xs font-bold text-error hover:bg-error/10 transition-colors flex items-center gap-2">
                            <span class="material-symbols-outlined text-sm">logout</span> Đăng xuất
                        </button>
                    </div>
                </div>
            </div>
        `;
        const loginBtn = document.getElementById('loginBtn');
        if (loginBtn) loginBtn.style.display = 'none';
    } else {
        userMenu.innerHTML = `
            <button onclick="typeof openAuthModal === 'function' ? openAuthModal() : window.location.href='/login'" class="material-symbols-outlined text-stone-900 hover:scale-105 transition-transform cursor-pointer">
                person
            </button>
        `;
        const loginBtn = document.getElementById('loginBtn');
        if (loginBtn) loginBtn.style.display = 'none';
    }
}

// Toggle user dropdown
function toggleUserDropdown() {
    const dropdown = document.getElementById('userDropdown');
    if (dropdown) {
        dropdown.classList.toggle('show');
    }
}

// Close dropdown when clicking outside
document.addEventListener('click', (e) => {
    const userMenu = document.getElementById('userMenu');
    const dropdown = document.getElementById('userDropdown');
    if (dropdown && userMenu && !userMenu.contains(e.target)) {
        dropdown.classList.remove('show');
    }
});

// Logout
async function logout() {
    try {
        await fetch('/api/auth/logout', { method: 'POST' });
        localStorage.removeItem('user');
        showToast('Đăng xuất thành công', 'success');
        setTimeout(() => {
            window.location.href = '/';
        }, 1000);
    } catch (error) {
        showToast('Lỗi đăng xuất', 'error');
    }
}

// ==================== CART FUNCTIONS ====================

// Update cart count in header
async function updateCartCount() {
    const cartCountElements = document.querySelectorAll('#cartCount');
    if (cartCountElements.length === 0) return;
    
    try {
        const response = await fetch('/api/cart');
        if (response.ok) {
            const data = await response.json();
            const items = data.items || data.cart || [];
            const count = items.reduce((sum, item) => sum + item.so_luong, 0);
            
            cartCountElements.forEach(el => {
                el.textContent = count;
                el.style.display = count > 0 ? 'flex' : 'none';
            });
        }
    } catch (err) {
        console.error('Cart count error:', err);
    }
}

// Add to cart
async function addToCart(productId, quantity = 1, size = null) {
    try {
        const response = await fetch('/api/cart', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                san_pham_id: productId,
                so_luong: quantity,
                size: size
            })
        });
        
        const data = await response.json();
        
        if (response.ok) {
            showToast('Đã thêm vào giỏ hàng', 'success');
            updateCartCount();
            if (typeof updateCartUI === 'function') updateCartUI();
            if (typeof loadCartModal === 'function') loadCartModal();
        } else if (response.status === 401) {
            showToast('Vui lòng đăng nhập để thêm vào giỏ', 'error');
            setTimeout(() => {
                window.location.href = '/login';
            }, 1500);
        } else {
            showToast(data.message || 'Lỗi thêm vào giỏ', 'error');
        }
    } catch (error) {
        showToast('Lỗi kết nối server', 'error');
    }
}

// Remove from cart
async function removeFromCart(cartItemId) {
    try {
        const response = await fetch(`/api/cart/${cartItemId}`, {
            method: 'DELETE'
        });
        
        if (response.ok) {
            showToast('Đã xóa khỏi giỏ hàng', 'success');
            updateCartCount();
            if (typeof updateCartUI === 'function') updateCartUI();
            if (typeof loadCartModal === 'function') loadCartModal();
            return true;
        } else {
            showToast('Lỗi xóa sản phẩm', 'error');
            return false;
        }
    } catch (error) {
        showToast('Lỗi kết nối server', 'error');
        return false;
    }
}

// Update cart item quantity
async function updateCartQuantity(cartItemId, quantity) {
    try {
        const response = await fetch(`/api/cart/${cartItemId}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ so_luong: quantity })
        });
        
        if (response.ok) {
            updateCartCount();
            return true;
        } else {
            showToast('Lỗi cập nhật số lượng', 'error');
            return false;
        }
    } catch (error) {
        showToast('Lỗi kết nối server', 'error');
        return false;
    }
}

// ==================== ORDER STATUS ====================

function getOrderStatusText(status) {
    const statusMap = {
        0: 'Chờ xử lý',
        1: 'Đang giao',
        2: 'Hoàn thành',
        3: 'Đã hủy'
    };
    return statusMap[status] || 'Không xác định';
}

function getOrderStatusClass(status) {
    const classMap = {
        0: 'status-pending',
        1: 'status-shipping',
        2: 'status-completed',
        3: 'status-cancelled'
    };
    return classMap[status] || '';
}

// Format date
function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('vi-VN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    });
}

// ==================== PRODUCT MODAL ====================

let currentModalProduct = null;
let selectedModalSize = null;

async function openProductModal(productId) {
    const modal = document.getElementById('productModal');
    const content = document.getElementById('productModalContent');
    if (!modal || !content) return;

    try {
        const res = await fetch('/api/products/' + productId);
        const data = await res.json();
        const product = data.product;
        currentModalProduct = product;
        selectedModalSize = null;

        // Update content
        document.getElementById('modalProductImg').src = product.hinh_anh || '/uploads/placeholder.jpg';
        document.getElementById('modalProductName').textContent = product.ten_sp;
        document.getElementById('modalProductCat').textContent = product.ten_danh_muc || 'Giày thể thao';
        document.getElementById('modalProductPrice').textContent = formatPrice(product.gia_km || product.gia);
        document.getElementById('modalProductOldPrice').textContent = product.gia_km ? formatPrice(product.gia) : '';
        document.getElementById('modalProductDesc').textContent = product.mo_ta || 'Đang cập nhật...';
        document.getElementById('modalQty').value = 1;

        // Sizes
        const sizeGrid = document.getElementById('modalSizeGrid');
        const sizes = [38, 39, 40, 41, 42, 43, 44, 45];
        sizeGrid.innerHTML = sizes.map(size => `
            <button onclick="selectModalSize(${size}, this)" class="size-btn py-3 rounded-xl border border-outline-variant/30 text-sm font-bold hover:border-primary transition-all">
                ${size}
            </button>
        `).join('');

        // Action button
        document.getElementById('modalAddToCartBtn').onclick = () => {
            if (!selectedModalSize) {
                showToast('Vui lòng chọn kích thước', 'error');
                return;
            }
            const qty = parseInt(document.getElementById('modalQty').value);
            addToCart(product.id, qty, selectedModalSize);
            closeProductModal();
        };

        // Show
        modal.classList.remove('hidden');
        setTimeout(() => {
            content.classList.remove('scale-95', 'opacity-0');
            content.classList.add('scale-100', 'opacity-100');
        }, 10);
        document.body.style.overflow = 'hidden';

    } catch (err) {
        console.error(err);
        showToast('Lỗi tải thông tin sản phẩm', 'error');
    }
}

function closeProductModal() {
    const modal = document.getElementById('productModal');
    const content = document.getElementById('productModalContent');
    if (!modal || !content) return;

    content.classList.add('scale-95', 'opacity-0');
    content.classList.remove('scale-100', 'opacity-100');
    setTimeout(() => {
        modal.classList.add('hidden');
        document.body.style.overflow = '';
    }, 500); 
}

function selectModalSize(size, btn) {
    selectedModalSize = size;
    document.querySelectorAll('.size-btn').forEach(b => {
        b.classList.remove('bg-on-surface', 'text-surface', 'border-on-surface');
        b.classList.add('border-outline-variant/30');
    });
    btn.classList.remove('border-outline-variant/30');
    btn.classList.add('bg-on-surface', 'text-surface', 'border-on-surface');
}

function updateModalQty(delta) {
    const input = document.getElementById('modalQty');
    let val = parseInt(input.value) + delta;
    if (val < 1) val = 1;
    input.value = val;
}
