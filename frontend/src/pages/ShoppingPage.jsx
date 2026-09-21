import { useMemo, useState } from 'react';
import Header from '../components/layout/Header';
import ProductGrid from '../components/catalog/ProductGrid';
import ProductSearch from '../components/catalog/ProductSearch';

const products = [
  { id: 1, name: 'apples', price: '$2.49', unit: '1 lb', stock: 50, icon: '🍎' },
  { id: 2, name: 'carrots', price: '$1.89', unit: '1 lb', stock: 32, icon: '🥕' },
  { id: 3, name: 'bread', price: '$3.25', unit: '1 loaf (16 oz)', stock: 20, icon: '🍞' },
  { id: 4, name: 'avocados', price: '$1.50', unit: '1 each', stock: 28, icon: '🥑' },
  { id: 5, name: 'bananas', price: '$1.29', unit: '1 lb', stock: 45, icon: '🍌' },
  { id: 6, name: 'tomatoes', price: '$2.15', unit: '1 lb', stock: 36, icon: '🍅' },
  { id: 7, name: 'milk', price: '$4.10', unit: '1 gal', stock: 18, icon: '🥛' },
  { id: 8, name: 'lettuce', price: '$2.75', unit: '1 head', stock: 26, icon: '🥬' },
];

function ShoppingPage() {
  const [search, setSearch] = useState('');
  const [quantities, setQuantities] = useState({});
  const [cartCount, setCartCount] = useState(0);

  const visibleProducts = useMemo(() => {
    const query = search.trim().toLowerCase();
    // cheap aah filter that needs to be replaced
    return query ? products.filter((product) => product.name.includes(query)) : products;
  }, [search]);

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

          {visibleProducts.length > 0 ? (
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
