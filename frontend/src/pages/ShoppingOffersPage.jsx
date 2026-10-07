import { useEffect, useMemo, useState } from 'react';
import Header from '../components/layout/Header';
import GradientButton from '../components/ui/button-1';
import { getInventory } from '../services/customerService';

const productIcons = {
  apple: '🍎',
  avocado: '🥑',
  banana: '🍌',
  carrot: '🥕',
  lettuce: '🥬',
  milk: '🥛',
  toast: '🍞',
  tomato: '🍅',
};

function toDisplayProduct(product) {
  const name = product.name || 'product';
  const normalizedName = name.toLowerCase();
  const iconKey = Object.keys(productIcons).find((key) => normalizedName.includes(key));
  const price = Number(product.unitPrice ?? product.unit_price ?? product.price ?? 0);
  const weight = product.unitWeight ?? product.unit_weight ?? product.weight;

  return {
    id: product.productId ?? product.product_id ?? product.id,
    name: normalizedName,
    price: `$${price.toFixed(2)}`,
    unit: weight === undefined || weight === null ? '' : `${weight} lb`,
    stock: Number(product.quantity ?? product.stock ?? 0),
    imageUrl: product.imageUrl ?? product.image_url ?? '',
    icon: productIcons[iconKey] || '🛒',
  };
}

function ProductOfferCard({ product, quantity, onDecrease, onIncrease, onAddToCart }) {
  return (
    <article className="group relative flex min-h-[390px] flex-col overflow-hidden rounded-2xl border-2 border-brand-green-600 bg-brand-green-700 shadow-sm transition duration-300 hover:-translate-y-1 hover:shadow-xl">
      <div className="relative h-44 shrink-0 overflow-hidden bg-brand-green-50">
        {product.imageUrl ? (
          <img
            src={product.imageUrl}
            alt={product.name}
            className="size-full object-contain p-3 transition duration-500 group-hover:scale-105"
          />
        ) : (
          <div className="grid size-full place-items-center text-8xl" role="img" aria-label={product.name}>
            {product.icon}
          </div>
        )}
        <span className="absolute left-4 top-4 rounded-full bg-paper/90 px-3 py-1 text-xs font-bold uppercase tracking-[0.16em] text-brand-green-700">
          fresh pick
        </span>
      </div>

      <div className="flex flex-1 flex-col border-t border-white/20 bg-brand-green-700/85 p-4 text-paper shadow-[inset_0_1px_0_rgba(255,255,255,0.12)] backdrop-blur-xl">
        <div className="flex items-start justify-between gap-3">
          <div>
            <p className="text-xs font-semibold uppercase tracking-[0.16em] text-brand-green-100">
              OFS Market
            </p>
            <h2 className="mt-2 font-display text-2xl font-black capitalize leading-tight">{product.name}</h2>
            <p className="mt-1 text-sm text-paper/70">{product.unit || 'market fresh'} · {product.price}</p>
          </div>
          <GradientButton
            width="72px"
            height="40px"
            onClick={() => window.location.assign(`/products/${product.id}`)}
            aria-label={`View ${product.name} details`}
          >
            View
          </GradientButton>
        </div>

        <div className="mt-auto pt-5">
          <div className="flex items-center justify-between border-t border-paper/20 pt-4">
            <p className="text-sm text-paper/75">In stock: {product.stock}</p>
            <div className="flex items-center gap-2">
              <button
                type="button"
                onClick={onDecrease}
                disabled={quantity === 0}
                className="grid size-9 place-items-center rounded-full border-2 border-white/25 bg-white/10 text-lg font-bold text-paper shadow-[inset_0_1px_1px_rgba(255,255,255,0.25),0_4px_12px_rgba(0,0,0,0.12)] backdrop-blur-md transition hover:bg-white/20 disabled:cursor-not-allowed disabled:opacity-35"
                aria-label={`Decrease ${product.name} quantity`}
              >
                −
              </button>
              <output className="grid min-w-5 place-items-center text-sm font-bold" aria-live="polite">
                {quantity}
              </output>
              <button
                type="button"
                onClick={onIncrease}
                disabled={quantity >= product.stock}
                className="grid size-9 place-items-center rounded-full border-2 border-white/60 bg-white/75 text-lg font-bold text-brand-green-700 shadow-[inset_0_1px_1px_rgba(255,255,255,0.8),0_4px_12px_rgba(0,0,0,0.14)] backdrop-blur-md transition hover:bg-white disabled:cursor-not-allowed disabled:opacity-35"
                aria-label={`Increase ${product.name} quantity`}
              >
                +
              </button>
            </div>
          </div>
          <button
            type="button"
            onClick={onAddToCart}
            disabled={quantity === 0}
            className="mt-4 flex min-h-11 w-full items-center justify-center rounded-xl bg-brand-orange-400 px-4 font-display font-bold text-brand-green-700 transition hover:bg-brand-orange-500 disabled:cursor-not-allowed disabled:bg-white/15 disabled:text-white/45"
          >
            add to cart
          </button>
        </div>
      </div>
    </article>
  );
}

function ShoppingOffersPage() {
  const [products, setProducts] = useState([]);
  const [search, setSearch] = useState('');
  const [quantities, setQuantities] = useState({});
  const [cartCount, setCartCount] = useState(0);
  const [isLoading, setIsLoading] = useState(true);
  const [loadError, setLoadError] = useState('');

  useEffect(() => {
    let isCurrent = true;

    async function loadInventory() {
      setIsLoading(true);
      setLoadError('');
      try {
        const response = await getInventory();
        const inventory = Array.isArray(response)
          ? response
          : response?.products || response?.items || [];
        if (isCurrent) setProducts(inventory.map(toDisplayProduct));
      } catch (error) {
        if (isCurrent) setLoadError(error instanceof Error ? error.message : 'Unable to load inventory.');
      } finally {
        if (isCurrent) setIsLoading(false);
      }
    }

    loadInventory();
    return () => {
      isCurrent = false;
    };
  }, []);

  const visibleProducts = useMemo(() => {
    const query = search.trim().toLowerCase();
    return query ? products.filter((product) => product.name.includes(query)) : products;
  }, [products, search]);

  function changeQuantity(productId, amount, stock) {
    setQuantities((current) => ({
      ...current,
      [productId]: Math.min(stock, Math.max(0, (current[productId] ?? 0) + amount)),
    }));
  }

  function addToCart(productId) {
    const quantity = quantities[productId] ?? 0;
    if (quantity === 0) return;
    setCartCount((current) => current + quantity);
    setQuantities((current) => ({ ...current, [productId]: 0 }));
  }

  return (
    <div className="min-h-screen bg-paper px-5 pb-12 sm:px-8 lg:px-12">
      <div className="mx-auto max-w-7xl">
        <Header showCart cartCount={cartCount} />

        <main className="pt-6 sm:pt-8">
          <div className="my-10 flex justify-center">
            <label className="w-full max-w-md">
              <span className="sr-only">Search products</span>
              <input
                value={search}
                onChange={(event) => setSearch(event.target.value)}
                placeholder="Search the market..."
                className="min-h-12 w-full rounded-xl border-2 border-brand-green-100 bg-white px-4 text-ink outline-none transition placeholder:text-ink/45 focus:border-brand-green-500"
              />
            </label>
          </div>

          {isLoading ? (
            <p className="py-16 text-center font-display text-lg font-bold text-brand-green-700" role="status">loading inventory...</p>
          ) : loadError ? (
            <p className="py-16 text-center font-semibold text-red-700" role="alert">{loadError}</p>
          ) : visibleProducts.length > 0 ? (
            <ul className="mx-auto grid w-full max-w-5xl grid-cols-1 gap-10 sm:grid-cols-2 lg:grid-cols-3" aria-label="Available products">
              {visibleProducts.map((product) => (
                <li key={product.id}>
                  <ProductOfferCard
                    product={product}
                    quantity={quantities[product.id] ?? 0}
                    onDecrease={() => changeQuantity(product.id, -1, Number.POSITIVE_INFINITY)}
                    onIncrease={() => changeQuantity(product.id, 1, product.stock)}
                    onAddToCart={() => addToCart(product.id)}
                  />
                </li>
              ))}
            </ul>
          ) : (
            <p className="py-16 text-center font-semibold text-ink/65">No products found. Try another search.</p>
          )}
        </main>
      </div>
    </div>
  );
}

export default ShoppingOffersPage;
