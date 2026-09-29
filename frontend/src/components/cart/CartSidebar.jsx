import { Link } from 'react-router-dom';

function formatMoney(amount) {
  return `$${amount.toFixed(2)}`;
}

function CartSidebar({ summary, deliveryFee }) {
  return (
    <aside className="rounded-2xl border-2 border-brand-green-100 bg-white p-6" aria-labelledby="order-summary-heading">
      <h2 id="order-summary-heading" className="font-display text-2xl font-black text-brand-green-700">
        order summary
      </h2>

      <dl className="mt-5 space-y-3 text-ink/75">
        <div className="flex justify-between gap-4">
          <dt>Number of items</dt>
          <dd>{summary.itemCount}</dd>
        </div>
        <div className="flex justify-between gap-4">
          <dt>Total weight</dt>
          <dd>{summary.totalWeight.toFixed(1)} lb</dd>
        </div>
        <div className="my-5 border-t border-brand-green-100" />
        <div className="flex justify-between gap-4">
          <dt>Subtotal</dt>
          <dd>{formatMoney(summary.subtotal)}</dd>
        </div>
        <div className="flex justify-between gap-4">
          <dt>Estimated delivery fee</dt>
          <dd>{formatMoney(deliveryFee)}</dd>
        </div>
      </dl>

      <div className="mt-6 flex items-center justify-between gap-4 border-t border-brand-green-100 pt-5">
        <span className="font-display text-xl font-black text-ink">Estimated total</span>
        <strong className="font-display text-2xl text-brand-orange-500">
          {formatMoney(summary.subtotal + deliveryFee)}
        </strong>
      </div>

      <Link
        to="/checkout"
        className="mt-6 flex min-h-12 w-full items-center justify-center rounded-xl bg-brand-green-600 px-5 font-display font-bold text-white no-underline hover:bg-brand-green-700 focus-visible:outline-3 focus-visible:outline-offset-2 focus-visible:outline-brand-orange-400"
      >
        continue to checkout
      </Link>
    </aside>
  );
}

export default CartSidebar;
