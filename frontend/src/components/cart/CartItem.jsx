function formatMoney(amount) {
  return `$${amount.toFixed(2)}`;
}

function CartItem({ item, onDecrease, onIncrease, onRemove }) {
  return (
    <li className="grid grid-cols-[5rem_minmax(0,1fr)_auto] items-center gap-4 border-b border-brand-green-100 py-5 last:border-b-0 sm:grid-cols-[6rem_minmax(9rem,1fr)_12rem_7rem_3rem]">
      <div className="grid size-20 place-items-center rounded-xl border border-brand-green-100 bg-paper text-5xl sm:size-24 sm:text-6xl">
        <span role="img" aria-label={item.name}>{item.icon}</span>
      </div>

      <div>
        <h2 className="font-display text-lg font-black text-ink">{item.name}</h2>
        <p className="mt-1 text-ink/75">{formatMoney(item.price)} each</p>
        <p className="text-sm text-ink/65">{item.unit}</p>
      </div>

      <div className="col-span-2 row-start-2 grid grid-cols-[2.5rem_4rem_2.5rem] items-center gap-2 sm:col-span-1 sm:col-start-3 sm:row-start-1">
        <button
          type="button"
          onClick={onDecrease}
          disabled={item.quantity === 1}
          className="grid size-10 place-items-center rounded-lg border-2 border-brand-green-500 font-display text-xl font-black text-brand-green-700 hover:bg-brand-green-50 disabled:cursor-not-allowed disabled:opacity-35"
          aria-label={`Decrease ${item.name} quantity`}
        >
          −
        </button>
        <output className="grid h-10 place-items-center rounded-lg border border-brand-green-100 font-bold" aria-live="polite">
          {item.quantity}
        </output>
        <button
          type="button"
          onClick={onIncrease}
          className="grid size-10 place-items-center rounded-lg border-2 border-brand-green-500 font-display text-xl font-black text-brand-green-700 hover:bg-brand-green-50"
          aria-label={`Increase ${item.name} quantity`}
        >
          +
        </button>
      </div>

      <p className="row-start-2 text-right font-display text-lg font-black text-brand-orange-500 sm:col-start-4 sm:row-start-1">
        {formatMoney(item.price * item.quantity)}
      </p>

      <button
        type="button"
        onClick={onRemove}
        className="col-start-3 row-start-1 grid size-10 place-items-center justify-self-end rounded-lg border border-red-200 bg-red-50 text-xl font-bold text-red-600 hover:bg-red-100 sm:col-start-5"
        aria-label={`Remove ${item.name} from cart`}
      >
        ×
      </button>
    </li>
  );
}

export default CartItem;
