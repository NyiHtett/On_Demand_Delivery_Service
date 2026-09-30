import { useMemo, useState } from 'react';
import ProductTable from '../components/catalog/ProductTable';
import ProductSearch from '../components/catalog/ProductSearch';
 
const initialProducts = [
  { id: 1, name: 'apples', price: '$2.49', unit: '1 lb', stock: 50, icon: '🍎' },
  { id: 2, name: 'carrots', price: '$1.89', unit: '1 lb', stock: 32, icon: '🥕' },
  { id: 3, name: 'bread', price: '$3.25', unit: '1 loaf (16 oz)', stock: 20, icon: '🍞' },
  { id: 4, name: 'avocados', price: '$1.50', unit: '1 each', stock: 28, icon: '🥑' },
  { id: 5, name: 'bananas', price: '$1.29', unit: '1 lb', stock: 45, icon: '🍌' },
  { id: 6, name: 'tomatoes', price: '$2.15', unit: '1 lb', stock: 36, icon: '🍅' },
  { id: 7, name: 'milk', price: '$4.10', unit: '1 gal', stock: 18, icon: '🥛' },
  { id: 8, name: 'lettuce', price: '$2.75', unit: '1 head', stock: 26, icon: '🥬' },
];
 
function ManagerPage() {
  const [products, setProducts] = useState(initialProducts);
  const [search, setSearch] = useState('');
 
  {/* What this basically does is dynamically update the visible productions object*/}
  const visibleProducts = useMemo(() => {
    const query = search.trim().toLowerCase();
    return query ? products.filter((product) => product.name.toLowerCase().includes(query)) : products;
  }, [search, products]);
 
  {/* the three functions below changes the stock, price, and units in the product object (not the database yet since we haven't hooked everything up*/}
  function changeStock(productId, amount) {
    setProducts((current) => current.map((product) => product.id === productId ? { ...product, stock: Math.max(0, product.stock + amount) } : product
      )
    );
  }

  function changePrice(productId, price) {
    setProducts((current) => current.map((product) => product.id === productId ? { ...product, price: price } : product
      )
    );
  }

  function changeUnit(productId, unit) {
    setProducts((current) => current.map((product) => product.id === productId ? { ...product, unit:unit } : product
      )
    );
  }

  return (
    <main className="mx-auto w-full max-w-5xl p-4 sm:p-6">
      <h1 className="font-display text-3xl font-black text-ink">Employee Product Dashboard</h1>
      <p className="mt-1 text-sm text-ink/75">Search products and adjust their stock.</p>
 
      <div className="my-7">
            <ProductSearch value={search} onChange={setSearch} onClear={() => setSearch('')} />
          </div>
 
      <div className="mt-4">
        {visibleProducts.length > 0 ? (
          <ProductTable
            products={visibleProducts}
            onDecrease={(id) => changeStock(id, -1)}
            onIncrease={(id) => changeStock(id, 1)}
            onWeightChange={(id, value) => changeUnit(id, value)}
            onPriceChange={(id, value) => changePrice(id, value)}
          />
        ) : (
          <p className="rounded-2xl border-2 border-dashed border-brand-green-100 p-8 text-center text-ink/75">
            No products match "{search.trim()}".
          </p>
        )}
      </div>
    </main>
  );
}
 
export default ManagerPage;