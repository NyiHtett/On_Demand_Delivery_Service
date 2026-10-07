{/* This is a line element for the manager product page
    each line contains the product image
    the name and the unit
    the price
    and then three buttons
    most of these fields are now editable */}
    import { useState, useEffect } from 'react';

function ProductLine({ product, onDecrease, onIncrease, onWeightChange, onPriceChange, onStockChange, onDescriptionChange, onImageUrlChange, onDelete }) {
  const [imageFailed, setImageFailed] = useState(false);
  useEffect(() => {
    setImageFailed(false);
  }, [product.imageUrl]);

  return (
    <div className="flex w-full items-center gap-4 rounded-2xl border-2 border-brand-green-100 bg-white p-4 transition-colors hover:border-brand-green-500">
      <div className="grid size-16 shrink-0 place-items-center rounded-xl border border-brand-green-100 bg-paper text-4xl">
        {
          product.imageUrl && !imageFailed ? (
            <img src={product.imageUrl} alt={product.name} className="size-full object-cover" onError={(e) => setImageFailed(true)}/>
          ) : (
            <span role="img">🛒</span>
          )
        }
      </div>
 
      <div className="min-w-0 flex-1">
        <h2 className="truncate font-display text-lg font-black text-ink">{product.name}</h2>
        <div className="flex items-center gap-1">
          <input type="number" step="0.1" min="0" key={product.unit} defaultValue={Number(product.unit).toFixed(3)} 
          onBlur={(event)=> {
            const weight = Math.max(0, Number(event.target.value || 0)).toFixed(3);
            event.target.value = weight;
            onWeightChange(weight);
          }}
          className="w-20 rounded border border-brand-green-100 px-2 text-sm text-ink/75"/>
          <span className="text-sm text-ink/75">lb</span>
        </div>

        <input type="text" placeholder="product description here..." maxLength={1000}
          key={product.description ?? ''} defaultValue={product.description ?? ''}
          onBlur={(event) => onDescriptionChange(event.target.value.trim() || null)}
          className="mt-1 w-full rounded border border-brand-green-100 px-2 text-sm text-ink/75"/>

        <input type="url" placeholder="image url here..." maxLength={5000}
          key={product.imageUrl ?? ''} defaultValue={product.imageUrl ?? ''}
          onBlur={(event) => onImageUrlChange(event.target.value.trim() || null)}
          className="mt-1 w-full rounded border border-brand-green-100 px-2 text-sm text-ink/75"/>
      </div>
 
      <div className="hidden shrink-0 items-center gap-1 sm:flex">
        <span className="font-display text-lg font-black text-brand-orange-500">$</span>
        <input type="number" min="0" key={product.price} defaultValue={product.price}
          onBlur={(event)=> {
            const price = Math.max(0, Number(event.target.value || 0)).toFixed(2);
            event.target.value = price;
            onPriceChange(price);
          }}
          className="w-24 rounded border border-brand-green-100 px-2 font-display text-lg font-black text-brand-orange-500"/>
      </div>
 
      <div className="grid shrink-0 grid-cols-[2.5rem_4rem_2.5rem] items-center gap-2">
        <button
          type="button"
          onClick={onDecrease}
          disabled={product.stock === 0}
          className="grid size-10 place-items-center rounded-lg border-2 border-brand-green-500 font-display text-xl font-black text-brand-green-700 hover:bg-brand-green-50 disabled:cursor-not-allowed disabled:opacity-35"
        >
          -
        </button>
        <input type="number" min="0" step="1" key = {product.stock} defaultValue={product.stock}
          onBlur={(event) => {
            const stock = Math.max(0, Number(event.target.value || 0));
            event.target.value = stock;
            onStockChange(stock);
          }}
          className="h-10 w-16 rounded-lg border border-brand-green-100 text-center font-bold"/>
        <button
          type="button"
          onClick={onIncrease}
          className="grid size-10 place-items-center rounded-lg border-2 border-brand-green-500 font-display text-xl font-black text-brand-green-700 hover:bg-brand-green-50"
        >
          +
        </button>
      </div>

      <button type="button" onClick={onDelete} className="grid size-10 shrink-0 place-items-center rounded-lg border-2 border-red-300 font-bold text-red-600 hover:bg-red-50">X</button>
    </div>
  );
}
 
export default ProductLine;