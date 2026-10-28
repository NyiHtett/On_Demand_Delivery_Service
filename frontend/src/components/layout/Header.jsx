import { Link, useLocation } from 'react-router-dom';
import { useEffect, useState } from 'react';
import { API_TOKEN_KEY } from '../../services/apiClient';
import { getCurrentUser } from '../../services/customerService';

function Header({ cartCount, showCart = false }) {
  const location = useLocation();
  const [userIdentity, setUserIdentity] = useState({ name: 'Guest', status: '', canManage: false });
  const [isAccountMenuOpen, setIsAccountMenuOpen] = useState(false);
  const isManagerView = location.pathname === '/manager';
  const isGuest = userIdentity.name === 'Guest' && !userIdentity.status;

  useEffect(() => {
    let isCurrent = true;

    async function loadUserLabel() {
      if (!localStorage.getItem(API_TOKEN_KEY)) {
        return;
      }

      try {
        const user = await getCurrentUser();
        if (!isCurrent) return;

        const userType = (user?.userType ?? user?.user_type ?? '').toLowerCase();
        const name = user?.name ?? user?.user_name ?? '';
        const canManage = ['admin', 'employee', 'manager'].includes(userType);
        const status = canManage ? 'Admin' : 'Customer';
        setUserIdentity(name ? { name, status, canManage } : { name: 'Guest', status: '', canManage: false });
      } catch {
        // A missing or expired token should appear as a guest in the navbar.
        if (isCurrent) setUserIdentity({ name: 'Guest', status: '', canManage: false });
      }
    }

    loadUserLabel();

    return () => {
      isCurrent = false;
    };
  }, []);

  return (
    <header className="flex items-center justify-between border-b-4 border-brand-green-600 py-4">
      <Link
        to="/"
        className="font-display text-2xl font-black tracking-tight text-brand-green-700 no-underline sm:text-3xl"
        aria-label="OFS Delivery home"
      >
        OFS
        <span className="text-brand-orange-500"> delivery</span>
      </Link>

      <nav className="flex items-center gap-4" aria-label="Customer navigation">
        <span className="rounded-lg border-2 border-brand-green-700 px-3 py-2 text-sm font-bold text-brand-green-700">
          {userIdentity.name}
          {userIdentity.status && (
            <span className="ml-2 font-normal text-brand-green-600">
              · {userIdentity.status}
            </span>
          )}
        </span>

        {userIdentity.canManage && (
          <Link
            to={isManagerView ? '/shop' : '/manager'}
            className="rounded-lg border-2 border-brand-orange-400 px-3 py-2 text-sm font-bold text-brand-orange-500 transition-colors hover:bg-brand-orange-100 focus-visible:outline-3 focus-visible:outline-offset-2 focus-visible:outline-brand-orange-400"
            aria-label={isManagerView ? 'Switch to shopping view' : 'Switch to manager view'}
          >
            {isManagerView ? 'shop view' : 'manager view'}
          </Link>
        )}

        {showCart && (
          <>
            <Link
              to="/cart"
              className="relative grid size-11 place-items-center text-brand-green-700 transition-colors hover:text-brand-orange-500 focus-visible:outline-3 focus-visible:outline-offset-2 focus-visible:outline-brand-orange-400"
              aria-label={`Open cart with ${cartCount} items`}
            >
              <svg viewBox="0 0 24 24" className="size-8" aria-hidden="true">
                <path d="M3 4h2l2.1 10.2a2 2 0 0 0 2 1.6h7.8a2 2 0 0 0 2-1.6L20 8H6" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" />
                <circle cx="10" cy="20" r="1.4" fill="currentColor" />
                <circle cx="18" cy="20" r="1.4" fill="currentColor" />
              </svg>
              {cartCount > 0 && (
                <span className="absolute right-0 top-0 grid min-w-5 place-items-center rounded-full bg-brand-orange-100 px-1 text-xs font-bold text-brand-orange-500">
                  {cartCount}
                </span>
              )}
            </Link>
            {!userIdentity.canManage && (
              <Link
                to="/order-history"
                className="grid size-11 place-items-center text-brand-green-700 transition-colors hover:text-brand-orange-500 focus-visible:outline-3 focus-visible:outline-offset-2 focus-visible:outline-brand-orange-400"
                aria-label="View order history"
              >
                <svg viewBox="0 0 24 24" className="size-7" aria-hidden="true">
                  <path d="M7 3h10a2 2 0 0 1 2 2v16H5V5a2 2 0 0 1 2-2Z" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinejoin="round" />
                  <path d="M8.5 8h7M8.5 12h7M8.5 16h4" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" />
                </svg>
              </Link>
            )}
          </>
        )}

        <div className="relative">
          <Link
            to="/account"
            onClick={(event) => {
              if (isGuest) {
                event.preventDefault();
                setIsAccountMenuOpen((isOpen) => !isOpen);
              }
            }}
            className="grid size-11 place-items-center rounded-full border-2 border-brand-green-700 text-brand-green-700 transition-colors hover:bg-brand-green-100 focus-visible:outline-3 focus-visible:outline-offset-2 focus-visible:outline-brand-orange-400"
            aria-label="Open account"
            aria-expanded={isGuest ? isAccountMenuOpen : undefined}
            aria-haspopup={isGuest ? 'menu' : undefined}
          >
            <svg viewBox="0 0 24 24" className="size-6" aria-hidden="true">
              <circle cx="12" cy="8" r="4" fill="none" stroke="currentColor" strokeWidth="2" />
              <path d="M4.5 21a7.5 7.5 0 0 1 15 0" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
            </svg>
          </Link>

          {isGuest && isAccountMenuOpen && (
            <div
              className="absolute right-0 top-14 z-10 w-36 rounded-xl border-2 border-brand-green-100 bg-white p-2 shadow-lg"
              role="menu"
              aria-label="Guest account options"
            >
              <Link
                to="/login"
                className="block rounded-lg px-3 py-2 text-center text-sm font-bold text-brand-green-700 no-underline hover:bg-brand-green-50"
                role="menuitem"
                onClick={() => setIsAccountMenuOpen(false)}
              >
                log in
              </Link>
              <Link
                to="/sign-up"
                className="mt-1 block rounded-lg px-3 py-2 text-center text-sm font-bold text-brand-orange-500 no-underline hover:bg-brand-orange-100"
                role="menuitem"
                onClick={() => setIsAccountMenuOpen(false)}
              >
                sign up
              </Link>
            </div>
          )}
        </div>
      </nav>
    </header>
  );
}

export default Header;
