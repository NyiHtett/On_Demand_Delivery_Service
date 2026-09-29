import { Link } from 'react-router-dom';

function EmptyCart() {
  return (
    <section className="mx-auto mt-8 flex min-h-96 max-w-xl flex-col items-center justify-center rounded-2xl border-2 border-brand-green-100 bg-white px-6 py-12 text-center" aria-live="polite">
      <svg viewBox="0 0 64 64" className="size-24 text-ink/25" aria-hidden="true">
        <path d="M8 10h7l6 31h27l6-22H18" fill="none" stroke="currentColor" strokeWidth="4" strokeLinecap="round" strokeLinejoin="round" />
        <circle cx="27" cy="51" r="4" fill="none" stroke="currentColor" strokeWidth="4" />
        <circle cx="47" cy="51" r="4" fill="none" stroke="currentColor" strokeWidth="4" />
      </svg>
      <h2 className="mt-5 font-display text-2xl font-black text-brand-green-700">your cart is empty</h2>
      <p className="mt-2 text-ink/65">Looks like you haven&apos;t added anything yet.</p>
      <Link
        to="/shop"
        className="mt-6 flex min-h-11 w-full max-w-64 items-center justify-center rounded-lg border-2 border-brand-green-600 px-5 font-display font-bold text-brand-green-700 no-underline hover:bg-brand-green-50"
      >
        go shopping
      </Link>
    </section>
  );
}

export default EmptyCart;
