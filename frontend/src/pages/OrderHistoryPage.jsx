import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import Header from '../components/layout/Header';
import { API_TOKEN_KEY } from '../services/apiClient';
import { getCustomerOrders } from '../services/orderService';

function formatMoney(value) {
  return `$${Number(value || 0).toFixed(2)}`;
}

function formatDate(value) {
  if (!value) return 'Date unavailable';
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? 'Date unavailable' : date.toLocaleDateString();
}

function OrderHistoryPage() {
  const [orders, setOrders] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!localStorage.getItem(API_TOKEN_KEY)) {
      setIsLoading(false);
      return;
    }

    getCustomerOrders()
      .then((response) => setOrders(Array.isArray(response) ? response : response?.orders || []))
      .catch((loadError) => setError(loadError instanceof Error ? loadError.message : 'Unable to load order history.'))
      .finally(() => setIsLoading(false));
  }, []);

  return (
    <div className="min-h-screen bg-paper px-5 pb-10 sm:px-8 lg:px-12">
      <div className="mx-auto max-w-7xl">
        <Header showCart cartCount={0} />
        <main className="mx-auto max-w-4xl pt-8">
          <p className="font-display text-sm font-bold uppercase tracking-[0.2em] text-brand-orange-500">OFS delivery</p>
          <h1 className="mt-1 font-display text-4xl font-black tracking-tight text-brand-green-700 sm:text-5xl">order history</h1>

          {!localStorage.getItem(API_TOKEN_KEY) ? (
            <section className="mt-8 rounded-2xl border-2 border-brand-green-100 bg-white p-8 text-center">
              <p className="text-ink/75">Log in to view your previous orders.</p>
              <Link to="/login" className="mt-5 inline-flex rounded-xl bg-brand-green-600 px-5 py-3 font-display font-bold text-white no-underline hover:bg-brand-green-700">log in</Link>
            </section>
          ) : isLoading ? (
            <p className="mt-8 rounded-2xl bg-white p-8 text-center text-ink/65">loading order history...</p>
          ) : error ? (
            <p className="mt-8 rounded-xl border border-red-200 bg-red-50 p-4 text-red-700" role="alert">{error}</p>
          ) : orders.length === 0 ? (
            <section className="mt-8 rounded-2xl border-2 border-dashed border-brand-green-100 bg-white p-8 text-center">
              <p className="font-display text-xl font-black text-brand-green-700">no orders yet</p>
              <Link to="/shop" className="mt-5 inline-flex rounded-xl border-2 border-brand-green-600 px-5 py-3 font-display font-bold text-brand-green-700 no-underline hover:bg-brand-green-50">start shopping</Link>
            </section>
          ) : (
            <ul className="mt-8 space-y-4">
              {orders.map((order) => (
                <li key={order.orderId ?? order.order_id} className="rounded-2xl border-2 border-brand-green-100 bg-white p-5">
                  <div className="flex flex-wrap items-start justify-between gap-3">
                    <div>
                      <h2 className="font-display text-xl font-black text-ink">Order #{order.orderId ?? order.order_id}</h2>
                      <p className="mt-1 text-sm text-ink/65">{formatDate(order.createdAt ?? order.created_at)}</p>
                    </div>
                    <span className="rounded-full bg-brand-green-50 px-3 py-1 text-sm font-bold text-brand-green-700">{order.orderStatus ?? order.order_status}</span>
                  </div>
                  <p className="mt-4 text-sm text-ink/75">Deliver to: {order.deliveryAddress ?? order.delivery_address}</p>
                  <div className="mt-4 flex items-center justify-between border-t border-brand-green-100 pt-4">
                    <span className="text-sm text-ink/65">{order.items?.length || 0} item types</span>
                    <strong className="font-display text-lg text-brand-orange-500">{formatMoney(order.subtotal)}</strong>
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
