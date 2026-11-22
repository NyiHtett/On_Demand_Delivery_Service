import { useEffect, useState } from 'react';

function formatMoney(amount) {
  return `$${amount.toFixed(2)}`;
}

function CartItem({ item, onDecrease, onIncrease, onRemove, onSetQuantity }) {
  const [imageFailed, setImageFailed] = useState(false);
  const [itemQuantity, setItemQuantity] = useState(String(item.quantity));

  useEffect(() => {
    setItemQuantity(String(item.quantity));
  }, [item.quantity]);

  useEffect(() => {
    setImageFailed(false);
  }, [item.imageUrl]);

  const maxQuantity = item.stock != null ? Math.max(1, Number(item.stock)) : 999;

  function snapQuantity() {
    const parsed = parseInt(itemQuantity, 10);
    const snapped = Number.isNaN(parsed) ? item.quantity : Math.min(Math.max(1, parsed), maxQuantity);
    setItemQuantity(String(snapped));
    if (snapped !== item.quantity) {
      onSetQuantity(snapped);
    }
  }

  return (
    <li className="grid grid-cols-[5rem_minmax(0,1fr)_auto] items-center gap-4 border-b border-brand-green-100 py-5 last:border-b-0 xl:grid-cols-[6rem_minmax(9rem,1fr)_12rem_7rem_3rem]">
      <div className="grid size-20 place-items-center rounded-xl border border-brand-green-100 bg-paper text-5xl sm:size-24 sm:text-6xl">
        {item.imageUrl && !imageFailed ? (
          <img
            src={item.imageUrl}
            alt={item.name}
            className="size-full rounded-xl object-contain p-2"
            onError={() => setImageFailed(true)}
          />
        ) : (
          <span role="img" aria-label={item.name}>{item.icon}</span>
        )}
      </div>

      <div>
        <h2 className="font-display text-lg font-black text-ink">{item.name}</h2>
        <p className="mt-1 text-ink/75">{formatMoney(item.price)} each</p>
        <p className="text-sm text-ink/65">{item.unit}</p>
      </div>

      <div className="col-span-2 row-start-2 grid grid-cols-[2.5rem_4rem_2.5rem] items-center gap-2 xl:col-span-1 xl:col-start-3 xl:row-start-1">
        <button
          type="button"
          onClick={onDecrease}
          disabled={item.quantity === 1}
          className="grid size-10 place-items-center rounded-lg border-2 border-brand-green-500 font-display text-xl font-black text-brand-green-700 hover:bg-brand-green-50 disabled:cursor-not-allowed disabled:opacity-35"
          aria-label={`Decrease ${item.name} quantity`}
        >
          −
        </button>
        <input
          type="text"
          inputMode="numeric"
          value={itemQuantity}
          onChange={(event) => setItemQuantity(event.target.value.replace(/\D/g, ''))}
          onBlur={snapQuantity}
          onKeyDown={(event) => {
            if (event.key === 'Enter') {
              event.currentTarget.blur();
            } else if (event.key === 'Escape') {
              setItemQuantity(String(item.quantity));
            }
          }}
          onFocus={(event) => event.target.select()}
          className="h-10 w-full rounded-lg border border-brand-green-100 text-center font-bold focus:border-brand-green-500 focus:outline-none"
          aria-label={`${item.name} quantity`}
          />
        <button
          type="button"
          onClick={onIncrease}
          disabled={item.stock != null && item.quantity >= item.stock}
          className="disabled:cursor-not-allowed disabled:opacity-35 grid size-10 place-items-center rounded-lg border-2 border-brand-green-500 font-display text-xl font-black text-brand-green-700 hover:bg-brand-green-50"
          aria-label={`Increase ${item.name} quantity`}
        >
          +
        </button>
      </div>

      <p className="row-start-2 whitespace-nowrap text-right font-display text-lg font-black text-brand-orange-500 xl:col-start-4 xl:row-start-1">
        {formatMoney(item.price * item.quantity)}
      </p>

      <button
        type="button"
        onClick={onRemove}
        className="col-start-3 row-start-1 grid size-10 place-items-center justify-self-end rounded-lg border border-red-200 bg-red-50 text-xl font-bold text-red-600 hover:bg-red-100 xl:col-start-5"
        aria-label={`Remove ${item.name} from cart`}
      >
        ×
      </button>
    </li>
  );
}

export default CartItem;
