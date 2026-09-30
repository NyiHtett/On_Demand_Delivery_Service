import { useEffect, useMemo, useState } from 'react';
import Header from '../components/layout/Header';
import ProductGrid from '../components/catalog/ProductGrid';
import ProductSearch from '../components/catalog/ProductSearch';
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
    stock: product.quantity ?? product.stock ?? 0,
    icon: productIcons[iconKey] || '🛒',
  };
}

function ShoppingPage() {
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
        if (isCurrent) {
          setLoadError(error instanceof Error ? error.message : 'Unable to load inventory.');
        }
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

  // both of the functions below are ui/temp data only
  // the temp data bit needs to be replaced by a helper function that connects it to orderService.js
  //
  function changeQuantity(productId, amount, stock) {
    setQuantities((current) => {
      const nextQuantity = Math.min(stock, Math.max(0, (current[productId] ?? 0) + amount));
      return { ...current, [productId]: nextQuantity };
    });
  }

  function addToCart(productId) {
    const quantity = quantities[productId] ?? 0;
    if (quantity === 0) return;

    setCartCount((current) => current + quantity);
    setQuantities((current) => ({ ...current, [productId]: 0 }));
  }

  return (
    <div className="min-h-screen bg-paper px-5 pb-10 sm:px-8 lg:px-12">
      <div className="mx-auto max-w-7xl">
        <Header showCart cartCount={cartCount} />

        <main className="pt-6 sm:pt-8">
          <div className="text-center">
            <p className="font-display text-sm font-bold uppercase tracking-[0.22em] text-brand-orange-500">
              OFS delivery
            </p>
            <h1 className="mt-1 font-display text-4xl font-black tracking-tight text-brand-green-700 sm:text-5xl">
              shop
            </h1>
            <p className="mt-1 text-lg text-ink/75">OFS Market</p>
          </div>

          <div className="my-7">
            <ProductSearch value={search} onChange={setSearch} onClear={() => setSearch('')} />
          </div>

          {isLoading ? (
            <p className="py-16 text-center font-display text-lg font-bold text-brand-green-700" role="status">
              loading inventory...
            </p>
          ) : loadError ? (
            <p className="py-16 text-center font-semibold text-red-700" role="alert">
              {loadError}
            </p>
          ) : visibleProducts.length > 0 ? (
            <ProductGrid
              products={visibleProducts}
              quantities={quantities}
              onDecrease={(productId) => changeQuantity(productId, -1, Number.POSITIVE_INFINITY)}
              onIncrease={(productId, stock) => changeQuantity(productId, 1, stock)}
              onAddToCart={addToCart}
            />
          ) : (
            <section className="mx-auto flex min-h-80 max-w-xl flex-col items-center justify-center rounded-2xl border-2 border-brand-green-100 bg-white px-6 py-12 text-center" aria-live="polite">
              <svg viewBox="0 0 64 64" className="size-20 text-ink/25" aria-hidden="true">
                <circle cx="27" cy="27" r="18" fill="none" stroke="currentColor" strokeWidth="5" />
                <path d="m41 41 16 16" fill="none" stroke="currentColor" strokeWidth="5" strokeLinecap="round" />
              </svg>
              <h2 className="mt-5 font-display text-2xl font-black text-brand-green-700">no items found</h2>
              <p className="mt-2 text-ink/65">Try a different search term.</p>
              <button
                type="button"
                onClick={() => setSearch('')}
                className="mt-6 min-h-11 w-full max-w-64 rounded-lg border-2 border-brand-green-600 px-5 font-display font-bold text-brand-green-700 hover:bg-brand-green-50"
              >
                clear search
              </button>
            </section>
          )}
        </main>
      </div>
    </div>
  );
}

export default ShoppingPage;
