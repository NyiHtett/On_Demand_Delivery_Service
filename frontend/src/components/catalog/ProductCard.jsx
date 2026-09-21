function ProductCard({ product, quantity, onDecrease, onIncrease, onAddToCart }) {
  return (
    <article className="flex h-full min-h-80 flex-col rounded-2xl border-2 border-brand-green-100 bg-white p-4 text-center transition-colors hover:border-brand-green-500">
      <div className="mx-auto grid size-24 place-items-center rounded-xl border border-brand-green-100 bg-paper text-6xl">
        <span role="img" aria-label={product.name}>{product.icon}</span>
      </div>

      <div className="mt-3">
        <h2 className="font-display text-lg font-black text-ink">{product.name}</h2>
        <p className="mt-1 font-display text-lg font-black text-brand-orange-500">{product.price}</p>
        <p className="mt-1 text-sm text-ink/75">{product.unit}</p>
        <p className="text-sm text-brand-green-700">In stock: {product.stock}</p>
      </div>

      <div className="mt-auto pt-4">
        <div className="mx-auto grid max-w-40 grid-cols-[2.5rem_1fr_2.5rem] items-center gap-2">
          <button
            type="button"
            onClick={onDecrease}
            disabled={quantity === 0}
            className="grid size-10 place-items-center rounded-lg border-2 border-brand-green-500 font-display text-xl font-black text-brand-green-700 hover:bg-brand-green-50 disabled:cursor-not-allowed disabled:opacity-35"
            aria-label={`Decrease ${product.name} quantity`}
          >
            −
          </button>
          <output className="grid h-10 place-items-center rounded-lg border border-brand-green-100 font-bold" aria-live="polite">
            {quantity}
          </output>
          <button
            type="button"
            onClick={onIncrease}
            disabled={quantity >= product.stock}
            className="grid size-10 place-items-center rounded-lg border-2 border-brand-green-500 font-display text-xl font-black text-brand-green-700 hover:bg-brand-green-50 disabled:cursor-not-allowed disabled:opacity-35"
            aria-label={`Increase ${product.name} quantity`}
          >
            +
          </button>
        </div>

        <button
          type="button"
          onClick={onAddToCart}
          disabled={quantity === 0}
          className="mt-3 min-h-10 w-full rounded-lg bg-brand-green-600 px-4 font-display font-bold text-white hover:bg-brand-green-700 disabled:cursor-not-allowed disabled:bg-brand-green-100 disabled:text-brand-green-700/50"
        >
          add to cart
        </button>
      </div>
    </article>
  );
}

export default ProductCard;
