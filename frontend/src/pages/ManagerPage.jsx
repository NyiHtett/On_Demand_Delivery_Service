import { useEffect, useMemo, useState } from 'react';
import ProductTable from '../components/manager/ProductTable';
import ProductSearch from '../components/catalog/ProductSearch';
import { getInventory, updateProduct, createProduct, deleteProduct } from '../services/productService';
import CreateProductForm from "../components/manager/CreateProductForm";
 

function toDisplayProduct(product) {
  const name = product.name || 'product';
  const normalizedName = name.toLowerCase();
  const price = Number(product.unitPrice ?? product.unit_price ?? product.price ?? 0);
  const weight = product.unitWeight ?? product.unit_weight ?? product.weight;

  return {
    id: product.productId ?? product.product_id ?? product.id,
    name: normalizedName,
    price: price.toFixed(2),
    unit: weight ?? '',
    stock: product.quantity ?? product.stock ?? 0,
    description: product.description ?? null,
    imageUrl: product.imageUrl ?? null
  };
}
 
function ManagerPage() {
  const [products, setProducts] = useState([]);
  const [search, setSearch] = useState('');
  const [savedProducts, setSavedProducts] = useState([]);

  useEffect(() => {
    getInventory().then((data) => {
      const loaded = data.map(toDisplayProduct);
      setProducts(loaded);
      setSavedProducts(loaded); //need two copies to compare and 
    }).catch((error) => console.error('Issue loading products: ', error));
  }
, []);
 
  {/* What this basically does is dynamically update the visible productions object*/}
  const visibleProducts = useMemo(() => {
    const query = search.trim().toLowerCase();
    return query ? products.filter((product) => product.name.toLowerCase().includes(query)) : products;
  }, [search, products]);

  const changedProducts = useMemo(() => {
    return products.filter((product) => {
      const saved = savedProducts.find((p) => p.id === product.id);
      return saved && (
        saved.stock !== product.stock || Number(saved.price) !== Number(product.price) || Number(saved.unit) !== Number(product.unit) ||
        (saved.description ?? '') !== (product.description ?? '') || (saved.imageUrl ?? '') !== (product.imageUrl ?? '')
      );
    });
  } , [products, savedProducts]);

  const hasChanges = changedProducts.length > 0;
 
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

  function setStock(productID, stock) {
    setProducts(
      (current) => current.map((product) => product.id === productID ? {...product, stock: stock} : product)
    );
  }

  function changeDescription(productId, description) {
    setProducts((current) => current.map((product) => product.id === productId ? {...product, description: description } : product)
  );
  }

  function changeImageUrl(productId, imageUrl) {
    setProducts((current) => current.map((product) => product.id === productId ? {...product, imageUrl: imageUrl} : product));
  }

  function handleUndo() {
    setProducts(savedProducts);
  }

  async function handleSave() {
    const ok = window.confirm(`Save changes to ${changedProducts.length} product(s)?`);
    if (!ok) return;

    try{
      await Promise.all(
        changedProducts.map((product) => 
          updateProduct(product.id, {
            quantity: product.stock,
            unitPrice: Number(product.price),
            unitWeight: Number(product.unit),
            description: product.description,
            imageUrl: product.imageUrl
          })
      )
      );
      setSavedProducts(products);
    } catch (error) {
      alert('Save failed: ' + error.message);
    }
  }
  
  async function handleCreateProduct(newProduct) {
    try {
      const created = await createProduct(newProduct);
      const display = toDisplayProduct(created);
      setProducts((current) => [...current, display]);
      setSavedProducts((current) => [...current, display]);
      return true;
    } catch (error) {
      alert('error creating ' + error.message);
      return false;
    }
  }

  async function handleDeleteProduct(productId) {
    const product = products.find((p) => p.id === productId);
    const deleteok = window.confirm(`Delete "${product.name}"?`);
    if (!deleteok) return;

    try {
      await deleteProduct(productId);
      setProducts((current) => current.filter((p) => p.id !== productId));
      setSavedProducts((current) => current.filter((p) => p.id !== productId))
    } catch (e) {
      alert("Failure deleting: " + e.message)
    } 
  }

  return (
    <main className="mx-auto w-full max-w-5xl p-4 sm:p-6">
      <h1 className="font-display text-3xl font-black text-ink">Employee Product Dashboard</h1>
      <p className="mt-1 text-sm text-ink/75">Search products and adjust their stock.</p>

      {hasChanges && (
        <div className="sticky top-0 z-10 mt-4 flex items-center justify-between rounded-xl border-2 border-brand-orange-500 bg-white p-3">
          <span className="font-bold text-ink">{changedProducts.length} unsaved product(s)</span>
          <div className="flex gap-2">
            <button type="button" onClick={handleUndo}
              className="rounded-lg border-2 border-brand-green-500 px-4 py-2 font-bold text-brand-green-700 hover:bg-brand-green-50">
              Undo
            </button>
            <button type="button" onClick={handleSave}
              className="rounded-lg bg-brand-green-600 px-4 py-2 font-bold text-white hover:bg-brand-green-700">
              Save
            </button>
          </div>
        </div>
      )}
 
      <div className="my-7">
            <ProductSearch value={search} onChange={setSearch} onClear={() => setSearch('')} />
          </div>
          
      <CreateProductForm onCreate={handleCreateProduct} />
 
      <div className="mt-4">
        {visibleProducts.length > 0 ? (
          <ProductTable
            products={visibleProducts}
            onDecrease={(id) => changeStock(id, -1)}
            onIncrease={(id) => changeStock(id, 1)}
            onWeightChange={(id, value) => changeUnit(id, value)}
            onPriceChange={(id, value) => changePrice(id, value)}
            onStockChange={(id, value) => setStock(id, value)}
            onDescriptionChange={(id, value) => changeDescription(id, value)}
            onImageUrlChange={(id, value) => changeImageUrl(id, value)}
            onDelete = {(id) => handleDeleteProduct(id)}
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