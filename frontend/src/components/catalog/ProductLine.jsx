{/* This is a line element for the manager product page
    each line contains the product image
    the name and the unit
    the price
    and then three buttons */}

function ProductLine({ product, onDecrease, onIncrease, onWeightChange, onPriceChange }) {
  return (
    <div className="flex w-full items-center gap-4 rounded-2xl border-2 border-brand-green-100 bg-white p-4 transition-colors hover:border-brand-green-500">
      <div className="grid size-16 shrink-0 place-items-center rounded-xl border border-brand-green-100 bg-paper text-4xl">
        <span role="img">{product.icon}</span>
      </div>
 
      <div className="min-w-0 flex-1">
        <h2 className="truncate font-display text-lg font-black text-ink">{product.name}</h2>
        <input type="text" value={product.unit} onChange={(event)=>onWeightChange(event.target.value)}
        className="w-full rounded border border-brand-green-100 px-2 text-sm text-ink/75"/>
      </div>
 
      <input type="text" value={product.price} onChange={(event)=>onPriceChange(event.target.value)}
        className="hidden w-24 shrink-0 rounded border border-brand-green-100 px-2 font-display text-lg font-black text-brand-orange-500 sm:block"/>
 
      <div className="grid shrink-0 grid-cols-[2.5rem_4rem_2.5rem] items-center gap-2">
        <button
          type="button"
          onClick={onDecrease}
          disabled={product.stock === 0}
          className="grid size-10 place-items-center rounded-lg border-2 border-brand-green-500 font-display text-xl font-black text-brand-green-700 hover:bg-brand-green-50 disabled:cursor-not-allowed disabled:opacity-35"
        >
          -
        </button>
        <output
          className="grid h-10 place-items-center rounded-lg border border-brand-green-100 font-bold"
        >
          {product.stock}
        </output>
        <button
          type="button"
          onClick={onIncrease}
          className="grid size-10 place-items-center rounded-lg border-2 border-brand-green-500 font-display text-xl font-black text-brand-green-700 hover:bg-brand-green-50"
        >
          +
        </button>
      </div>
    </div>
  );
}
 
export default ProductLine;