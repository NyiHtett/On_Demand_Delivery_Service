import { useEffect, useState } from 'react';

function ProductDetailModal({
  product,
  quantity,
  isAdding,
  onDecrease,
  onIncrease,
  onAddToCart,
  onClose,
}) {
  const [isVisible, setIsVisible] = useState(false);

  useEffect(() => {
    if (!product) return undefined;

    setIsVisible(false);
    const animationFrame = requestAnimationFrame(() => setIsVisible(true));

    return () => cancelAnimationFrame(animationFrame);
  }, [product]);

  useEffect(() => {
    if (!product) return undefined;

    function handleKeyDown(event) {
      if (event.key === 'Escape') onClose();
    }

    document.addEventListener('keydown', handleKeyDown);
    return () => document.removeEventListener('keydown', handleKeyDown);
  }, [product, onClose]);

  if (!product) return null;

  return (
    <div
      className={`fixed inset-0 z-50 grid place-items-center px-4 py-6 backdrop-blur-sm transition-opacity duration-200 motion-reduce:transition-none ${isVisible ? 'bg-ink/60 opacity-100' : 'bg-ink/0 opacity-0'}`}
      role="presentation"
      onMouseDown={(event) => {
        if (event.target === event.currentTarget) onClose();
      }}
    >
      <section
        className={`relative grid max-h-[90vh] w-full max-w-2xl overflow-auto rounded-3xl border-2 border-brand-green-100 bg-paper shadow-2xl transition duration-300 ease-out motion-reduce:transition-none ${isVisible ? 'translate-y-0 scale-100 opacity-100' : 'translate-y-3 scale-95 opacity-0'}`}
        role="dialog"
        aria-modal="true"
        aria-labelledby="product-detail-title"
      >
        <button
          type="button"
          onClick={onClose}
          className="absolute right-4 top-4 z-10 grid size-10 place-items-center rounded-full border-2 border-brand-green-100 bg-white text-2xl font-bold text-brand-green-700 shadow-sm transition hover:bg-brand-green-50"
          aria-label="Close product details"
        >
          ×
        </button>

        <div className="grid gap-6 p-5 sm:grid-cols-2 sm:p-7">
          <div className="flex min-h-64 items-center justify-center overflow-hidden rounded-2xl border-2 border-brand-green-100 bg-white p-5 sm:min-h-80">
            {product.imageUrl ? (
              <img
                src={product.imageUrl}
                alt={product.name}
                className="max-h-72 w-full object-contain"
              />
            ) : (
              <span className="text-9xl" role="img" aria-label={product.name}>
                {product.icon}
              </span>
            )}
          </div>

          <div className="flex flex-col">
            <p className="text-xs font-bold uppercase tracking-[0.16em] text-brand-green-700">
              OFS Market · fresh pick
            </p>
            <h2 id="product-detail-title" className="mt-2 font-display text-3xl font-black capitalize text-ink">
              {product.name}
            </h2>
            <p className="mt-2 font-display text-2xl font-black text-brand-orange-500">
              {product.price}
            </p>
            <p className="mt-1 text-sm text-ink/65">{product.unit || 'market fresh'}</p>
            <p className="mt-5 leading-7 text-ink/75">
              {product.description || 'Freshly selected for your next delivery.'}
            </p>

            <div className="mt-auto pt-6">
              <div className="flex items-center justify-between border-t border-brand-green-100 pt-4">
                <p className="text-sm font-semibold text-brand-green-700">
                  In stock: {product.stock}
                </p>
                <div className="flex items-center gap-2">
                  <button
                    type="button"
                    onClick={onDecrease}
                    disabled={quantity === 0}
                    className="grid size-10 place-items-center rounded-full border-2 border-brand-green-500 text-xl font-black text-brand-green-700 transition hover:bg-brand-green-50 disabled:cursor-not-allowed disabled:opacity-35"
                    aria-label={`Decrease ${product.name} quantity`}
                  >
                    −
                  </button>
                  <output className="grid min-w-8 place-items-center font-bold" aria-live="polite">
                    {quantity}
                  </output>
                  <button
                    type="button"
                    onClick={onIncrease}
                    disabled={quantity >= product.stock}
                    className="grid size-10 place-items-center rounded-full border-2 border-brand-green-500 text-xl font-black text-brand-green-700 transition hover:bg-brand-green-50 disabled:cursor-not-allowed disabled:opacity-35"
                    aria-label={`Increase ${product.name} quantity`}
                  >
                    +
                  </button>
                </div>
              </div>

              <button
                type="button"
                onClick={onAddToCart}
                disabled={quantity === 0 || isAdding}
                className="mt-4 flex min-h-12 w-full items-center justify-center rounded-xl bg-brand-orange-400 px-4 font-display font-bold text-brand-green-700 transition hover:bg-brand-orange-500 disabled:cursor-not-allowed disabled:bg-brand-green-100 disabled:text-brand-green-700/50"
              >
                {isAdding ? 'adding…' : 'add to cart'}
              </button>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
}

export default ProductDetailModal;
