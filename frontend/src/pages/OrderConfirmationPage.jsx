import { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import Header from '../components/layout/Header';
import { DeliveryLine, StatusBadge, formatMoney } from '../components/orders/DeliveryStatus';
import usePolling from '../hooks/usePolling';
import { CART_UPDATED_EVENT, confirmCheckout, getOrder } from '../services/orderService';

// Stripe sends customers here with ?session_id=...; without Stripe configured the backend uses ?order_id=...
function OrderConfirmationPage() {
  const [searchParams] = useSearchParams();
  const sessionId = searchParams.get('session_id');
  const orderIdParam = searchParams.get('order_id');
  const [order, setOrder] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    let isCurrent = true;
    const params = sessionId ? { sessionId } : { orderId: Number(orderIdParam) };
    if (!sessionId && !orderIdParam) {
      setError('No order to show.');
      return undefined;
    }

    confirmCheckout(params)
      .then((confirmed) => {
        if (!isCurrent) return;
        setOrder(confirmed);
        // The paid cart is closed; tell the header its badge is now empty.
        window.dispatchEvent(new CustomEvent(CART_UPDATED_EVENT, { detail: { items: [] } }));
      })
      .catch((confirmError) => isCurrent && setError(confirmError.message || 'We couldn’t confirm this order.'));

    return () => {
      isCurrent = false;
    };
  }, [sessionId, orderIdParam]);

  // Live updates as the robot is dispatched and delivers.
  const isActive = order && ['Ordered', 'Shipping'].includes(order.orderStatus);
  usePolling(() => {
    getOrder(order.orderId).then(setOrder).catch(() => {});
  }, 4000, Boolean(isActive));

  return (
    <div className="min-h-screen bg-paper px-5 pb-10 sm:px-8 lg:px-12">
      <div className="mx-auto max-w-7xl">
        <Header showCart />
        <main className="mx-auto max-w-3xl pt-8">
          {error ? (
            <section className="rounded-2xl border-2 border-red-200 bg-white p-8 text-center">
              <h1 className="font-display text-3xl font-black text-ink">something went wrong</h1>
              <p className="mt-3 text-red-700" role="alert">{error}</p>
              <Link to="/order-history" className="mt-6 inline-flex rounded-xl border-2 border-brand-green-600 px-5 py-3 font-display font-bold text-brand-green-700 no-underline hover:bg-brand-green-50">view my orders</Link>
            </section>
          ) : !order ? (
            <p className="rounded-2xl bg-white p-8 text-center text-ink/65" role="status">confirming your payment...</p>
          ) : (
            <>
              <p className="font-display text-sm font-bold uppercase tracking-[0.2em] text-brand-orange-500">order #{order.orderId}</p>
              <h1 className="mt-1 font-display text-4xl font-black tracking-tight text-brand-green-700 sm:text-5xl">
                {order.orderStatus === 'Completed' ? 'delivered!' : 'thanks, your order is in'}
              </h1>

              <section className="mt-8 grid gap-4 rounded-2xl border-2 border-brand-green-100 bg-white p-6">
                <div className="flex flex-wrap items-center justify-between gap-3">
                  <StatusBadge status={order.orderStatus} />
                  <span className="text-sm text-ink/60">{order.createdAt ? new Date(order.createdAt).toLocaleString() : ''}</span>
                </div>
                <DeliveryLine order={order} />
                <p className="text-sm text-ink/75">Deliver to: {order.deliveryAddress}</p>

                <ul className="divide-y divide-brand-green-100 border-y border-brand-green-100">
                  {order.items.map((item) => (
                    <li key={item.orderItemId} className="flex justify-between gap-4 py-2 text-ink/80">
                      <span>{item.quantity} × {item.productName}</span>
                      <span className="tabular-nums">{formatMoney(item.unitPrice * item.quantity)}</span>
                    </li>
                  ))}
                </ul>

                <dl className="grid gap-1 text-sm text-ink/75">
                  <div className="flex justify-between"><dt>Subtotal</dt><dd className="tabular-nums">{formatMoney(order.subtotal)}</dd></div>
                  <div className="flex justify-between"><dt>Total weight</dt><dd className="tabular-nums">{Number(order.totalWeight).toFixed(1)} lb</dd></div>
                  <div className="flex justify-between"><dt>Delivery</dt><dd className="tabular-nums">{Number(order.deliveryFee) > 0 ? formatMoney(order.deliveryFee) : 'free'}</dd></div>
                  <div className="mt-2 flex justify-between font-display text-lg font-black text-ink"><dt>Total paid</dt><dd className="tabular-nums text-brand-orange-500">{formatMoney(Number(order.subtotal) + Number(order.deliveryFee))}</dd></div>
                </dl>
              </section>

              <div className="mt-6 flex flex-wrap gap-3">
                <Link to="/order-history" className="inline-flex rounded-xl bg-brand-green-600 px-5 py-3 font-display font-bold text-white no-underline hover:bg-brand-green-700">view my orders</Link>
                <Link to="/shop" className="inline-flex rounded-xl border-2 border-brand-green-600 px-5 py-3 font-display font-bold text-brand-green-700 no-underline hover:bg-brand-green-50">keep shopping</Link>
              </div>
            </>
          )}
        </main>
      </div>
    </div>
  );
}

export default OrderConfirmationPage;
