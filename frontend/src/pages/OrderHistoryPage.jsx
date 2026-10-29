import { useState } from 'react';
import { Link } from 'react-router-dom';
import Header from '../components/layout/Header';
import { DeliveryLine, StatusBadge, formatMoney } from '../components/orders/DeliveryStatus';
import usePolling from '../hooks/usePolling';
import { API_TOKEN_KEY } from '../services/apiClient';
import { cancelOrder, getCustomerOrders } from '../services/orderService';

function formatDate(value) {
  if (!value) return 'Date unavailable';
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? 'Date unavailable' : date.toLocaleString([], { dateStyle: 'medium', timeStyle: 'short' });
}

function OrderHistoryPage() {
  const isLoggedIn = Boolean(localStorage.getItem(API_TOKEN_KEY));
  const [orders, setOrders] = useState([]);
  const [isLoading, setIsLoading] = useState(isLoggedIn);
  const [error, setError] = useState('');

  // Polls so status changes (robot leaving, delivered) show up without a refresh.
  usePolling(() => {
    getCustomerOrders()
      .then((response) => {
        setOrders(Array.isArray(response) ? response : response?.orders || []);
        setError('');
      })
      .catch((loadError) => setError(loadError instanceof Error ? loadError.message : 'Unable to load order history.'))
      .finally(() => setIsLoading(false));
  }, 5000, isLoggedIn);

  async function handleCancel(order) {
    if (!window.confirm(`Cancel order #${order.orderId}? You'll be refunded ${formatMoney(Number(order.subtotal) + Number(order.deliveryFee))}.`)) return;
    try {
      const updated = await cancelOrder(order.orderId);
      setOrders((current) => current.map((o) => (o.orderId === updated.orderId ? updated : o)));
    } catch (cancelError) {
      setError(cancelError.message || 'Couldn’t cancel that order.');
    }
  }

  return (
    <div className="min-h-screen bg-paper px-5 pb-10 sm:px-8 lg:px-12">
      <div className="mx-auto max-w-7xl">
        <Header showCart />
        <main className="mx-auto max-w-4xl pt-8">
          <p className="font-display text-sm font-bold uppercase tracking-[0.2em] text-brand-orange-500">OFS delivery</p>
          <h1 className="mt-1 font-display text-4xl font-black tracking-tight text-brand-green-700 sm:text-5xl">order history</h1>

          {error && <p className="mt-8 rounded-xl border border-red-200 bg-red-50 p-4 text-red-700" role="alert">{error}</p>}

          {!isLoggedIn ? (
            <section className="mt-8 rounded-2xl border-2 border-brand-green-100 bg-white p-8 text-center">
              <p className="text-ink/75">Log in to view your previous orders.</p>
              <Link to="/login" className="mt-5 inline-flex rounded-xl bg-brand-green-600 px-5 py-3 font-display font-bold text-white no-underline hover:bg-brand-green-700">log in</Link>
            </section>
          ) : isLoading ? (
            <p className="mt-8 rounded-2xl bg-white p-8 text-center text-ink/65">loading order history...</p>
          ) : orders.length === 0 ? (
            <section className="mt-8 rounded-2xl border-2 border-dashed border-brand-green-100 bg-white p-8 text-center">
              <p className="font-display text-xl font-black text-brand-green-700">no orders yet</p>
              <Link to="/shop" className="mt-5 inline-flex rounded-xl border-2 border-brand-green-600 px-5 py-3 font-display font-bold text-brand-green-700 no-underline hover:bg-brand-green-50">start shopping</Link>
            </section>
          ) : (
            <ul className="mt-8 space-y-4">
              {orders.map((order) => (
                <li key={order.orderId} className="grid gap-3 rounded-2xl border-2 border-brand-green-100 bg-white p-5">
                  <div className="flex flex-wrap items-start justify-between gap-3">
                    <div>
                      <h2 className="font-display text-xl font-black text-ink">Order #{order.orderId}</h2>
                      <p className="mt-1 text-sm text-ink/65">{formatDate(order.createdAt)}</p>
                    </div>
                    <StatusBadge status={order.orderStatus} />
                  </div>
                  <DeliveryLine order={order} />
                  <p className="text-sm text-ink/75">Deliver to: {order.deliveryAddress}</p>
                  <p className="text-sm text-ink/65">
                    {order.items?.map((item) => `${item.quantity} × ${item.productName}`).join(', ')}
                  </p>
                  <div className="flex flex-wrap items-center justify-between gap-3 border-t border-brand-green-100 pt-4">
                    {['Ordered', 'Pending'].includes(order.orderStatus) ? (
                      <button type="button" onClick={() => handleCancel(order)} className="rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm font-bold text-red-700 hover:bg-red-100">
                        cancel order
                      </button>
                    ) : <span />}
                    <strong className="font-display text-lg text-brand-orange-500">{formatMoney(Number(order.subtotal) + Number(order.deliveryFee))}</strong>
                  </div>
                </li>
              ))}
            </ul>
          )}
        </main>
      </div>
    </div>
  );
}

export default OrderHistoryPage;
