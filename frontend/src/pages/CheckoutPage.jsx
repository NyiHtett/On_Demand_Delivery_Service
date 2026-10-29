import { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import CartSidebar from '../components/cart/CartSidebar';
import Header from '../components/layout/Header';
import { formatMoney } from '../components/orders/DeliveryStatus';
import { API_TOKEN_KEY } from '../services/apiClient';
import { getCurrentUser } from '../services/customerService';
import { cancelCheckout, checkoutCart, getCart } from '../services/orderService';

function CheckoutPage() {
  const isLoggedIn = Boolean(localStorage.getItem(API_TOKEN_KEY));
  const [searchParams, setSearchParams] = useSearchParams();
  const [cart, setCart] = useState(null);
  const [address, setAddress] = useState('');
  const [isLoading, setIsLoading] = useState(isLoggedIn);
  const [isPaying, setIsPaying] = useState(false);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');

  useEffect(() => {
    if (!isLoggedIn) return undefined;
    let isCurrent = true;

    async function load() {
      // Back from Stripe without paying: release the held items first so the cart's stock is accurate.
      const canceledSession = searchParams.get('canceled') && searchParams.get('session_id');
      if (canceledSession) {
        await cancelCheckout(canceledSession).catch(() => {});
        if (isCurrent) {
          setNotice('Payment was canceled. Nothing was charged and your cart is just as you left it.');
          setSearchParams({}, { replace: true });
        }
      }

      try {
        const [cartResponse, account] = await Promise.all([getCart(), getCurrentUser().catch(() => null)]);
        if (!isCurrent) return;
        setCart(cartResponse);
        setAddress((current) => current || account?.address || '');
      } catch (loadError) {
        if (isCurrent) setError(loadError.message || 'Unable to load your cart.');
      } finally {
        if (isCurrent) setIsLoading(false);
      }
    }

    load();
    return () => {
      isCurrent = false;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isLoggedIn]);

  async function handlePay(event) {
    event.preventDefault();
    setError('');
    setIsPaying(true);
    try {
      const { url } = await checkoutCart(address.trim());
      window.location.assign(url);
    } catch (payError) {
      setError(payError.message || 'Checkout failed. Please try again.');
      setIsPaying(false);
      getCart().then(setCart).catch(() => {});
    }
  }

  const items = cart?.items || [];
  const summary = {
    itemCount: items.reduce((total, item) => total + item.quantity, 0),
    totalWeight: Number(cart?.totalWeight ?? 0),
    subtotal: Number(cart?.subtotal ?? 0),
    deliveryFee: Number(cart?.deliveryFee ?? 0),
    total: Number(cart?.total ?? 0),
  };

  let content;
  if (!isLoggedIn) {
    content = (
      <section className="mt-8 rounded-2xl border-2 border-brand-green-100 bg-white p-8 text-center">
        <p className="text-ink/75">Log in to check out.</p>
        <Link to="/login" className="mt-5 inline-flex rounded-xl bg-brand-green-600 px-5 py-3 font-display font-bold text-white no-underline hover:bg-brand-green-700">log in</Link>
      </section>
    );
  } else if (isLoading) {
    content = <p className="mt-8 rounded-2xl bg-white p-8 text-center text-ink/65">loading checkout...</p>;
  } else if (items.length === 0) {
    content = (
      <section className="mt-8 rounded-2xl border-2 border-dashed border-brand-green-100 bg-white p-8 text-center">
        <p className="font-display text-xl font-black text-brand-green-700">your cart is empty</p>
        <Link to="/shop" className="mt-5 inline-flex rounded-xl border-2 border-brand-green-600 px-5 py-3 font-display font-bold text-brand-green-700 no-underline hover:bg-brand-green-50">go shopping</Link>
      </section>
    );
  } else {
    content = (
      <div className="mt-6 grid items-start gap-7 lg:grid-cols-[minmax(0,1fr)_25rem]">
        <form onSubmit={handlePay} className="grid gap-6 rounded-2xl border-2 border-brand-green-100 bg-white p-6">
          <section aria-labelledby="items-heading">
            <h2 id="items-heading" className="font-display text-xl font-black text-brand-green-700">items</h2>
            <ul className="mt-3 divide-y divide-brand-green-100">
              {items.map((item) => (
                <li key={item.productID ?? item.cartItemID} className="flex justify-between gap-4 py-2 text-ink/80">
                  <span>{item.quantity} × {item.productName}</span>
                  <span className="tabular-nums">{formatMoney(item.unitPrice * item.quantity)}</span>
                </li>
              ))}
            </ul>
          </section>

          <label className="grid gap-2">
            <span className="font-display text-xl font-black text-brand-green-700">deliver to</span>
            <textarea
              id="checkout-address"
              value={address}
              onChange={(event) => setAddress(event.target.value)}
              required
              rows={2}
              placeholder="Street, city, state ZIP"
              className="rounded-lg border-2 border-brand-green-100 bg-paper px-3 py-2 text-ink outline-none focus:border-brand-green-500"
            />
          </label>

          <button
            type="submit"
            disabled={isPaying || !address.trim()}
            className="flex min-h-12 w-full items-center justify-center rounded-xl bg-brand-orange-400 px-5 font-display font-bold text-brand-green-700 hover:bg-brand-orange-500 disabled:cursor-not-allowed disabled:opacity-50"
          >
            {isPaying ? 'opening secure payment…' : `pay ${formatMoney(summary.total)}`}
          </button>
          <p className="-mt-3 text-center text-xs text-ink/55">Payments are handled by Stripe.</p>
        </form>

        <CartSidebar summary={summary} checkoutLink={false} />
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-paper px-5 pb-10 sm:px-8 lg:px-12">
      <div className="mx-auto max-w-7xl">
        <Header showCart />
        <main className="pt-8">
          <h1 className="font-display text-4xl font-black tracking-tight text-ink sm:text-5xl">checkout</h1>
          {notice && <p className="mt-6 rounded-xl bg-brand-orange-100 p-4 text-ink/80" role="status">{notice}</p>}
          {error && <p className="mt-6 rounded-xl border border-red-200 bg-red-50 p-4 text-red-700" role="alert">{error}</p>}
          {content}
          {items.length > 0 && (
            <Link to="/cart" className="mt-7 inline-flex items-center gap-2 font-display font-bold text-brand-green-700 no-underline hover:text-brand-orange-500">
              <span aria-hidden="true">←</span>
              back to cart
            </Link>
          )}
        </main>
      </div>
    </div>
  );
}

export default CheckoutPage;
