import { Link } from 'react-router-dom';

function Header({ cartCount, showCart = false }) {
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
        {showCart && (
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
        )}

        <Link
          to="/account"
          className="grid size-11 place-items-center rounded-full border-2 border-brand-green-700 text-brand-green-700 transition-colors hover:bg-brand-green-100 focus-visible:outline-3 focus-visible:outline-offset-2 focus-visible:outline-brand-orange-400"
          aria-label="Open account"
        >
          <svg viewBox="0 0 24 24" className="size-6" aria-hidden="true">
            <circle cx="12" cy="8" r="4" fill="none" stroke="currentColor" strokeWidth="2" />
            <path d="M4.5 21a7.5 7.5 0 0 1 15 0" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
          </svg>
        </Link>
      </nav>
    </header>
  );
}

export default Header;
