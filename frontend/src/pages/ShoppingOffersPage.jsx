import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Header from '../components/layout/Header';
import GradientButton from '../components/ui/button-1';
import { getInventory } from '../services/customerService';
import { addCartItem } from '../services/orderService';

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
  const name = product.name || product.productName || 'product';
  const normalizedName = name.toLowerCase();

  const iconKey = Object.keys(productIcons).find((key) =>
    normalizedName.includes(key)
  );

  const numericPrice = Number(
    product.unitPrice ?? product.unit_price ?? product.price ?? 0
  );

  const weight =
    product.unitWeight ?? product.unit_weight ?? product.weight;

  return {
    id: product.productId ?? product.product_id ?? product.id,
    name: normalizedName,
    price: `$${numericPrice.toFixed(2)}`,
    numericPrice,
    unit:
      weight === undefined || weight === null
        ? ''
        : `${weight} lb`,
    stock: Number(product.quantity ?? product.stock ?? 0),
    description: product.description ?? '',
    imageUrl: product.imageUrl ?? product.image_url ?? '',
    icon: productIcons[iconKey] || '🛒',
  };
}

function ProductOfferCard({
  product,
  quantity,
  isAdding,
  onDecrease,
  onIncrease,
  onAddToCart,
}) {
  const [showDetails, setShowDetails] = useState(false);

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
          <div
            className="grid size-full place-items-center text-8xl"
            role="img"
            aria-label={product.name}
          >
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

            <h2 className="mt-2 font-display text-2xl font-black capitalize leading-tight">
              {product.name}
            </h2>

            <p className="mt-1 text-sm text-paper/70">
              {product.unit || 'market fresh'} · {product.price}
            </p>
          </div>

          <GradientButton
            width="72px"
            height="40px"
            onClick={() => setShowDetails((open) => !open)}
            aria-label={`View ${product.name} details`}
            aria-expanded={showDetails}
          >
            {showDetails ? 'Hide' : 'View'}
          </GradientButton>
        </div>

        {showDetails && (
          <p className="mt-3 text-sm leading-6 text-paper/85">
            {product.description || 'No description yet.'}
          </p>
        )}

        <div className="mt-auto pt-5">
          <div className="flex items-center justify-between border-t border-paper/20 pt-4">
            <p className="text-sm text-paper/75">
              In stock: {product.stock}
            </p>

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

              <output
                className="grid min-w-5 place-items-center text-sm font-bold"
                aria-live="polite"
              >
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
            disabled={quantity === 0 || isAdding}
            className="mt-4 flex min-h-11 w-full items-center justify-center rounded-xl bg-brand-orange-400 px-4 font-display font-bold text-brand-green-700 transition hover:bg-brand-orange-500 disabled:cursor-not-allowed disabled:bg-white/15 disabled:text-white/45"
          >
            {isAdding ? 'adding…' : 'add to cart'}
          </button>
        </div>
      </div>
    </article>
  );
}

function ShoppingOffersPage() {
  const [products, setProducts] = useState([]);
  const [search, setSearch] = useState('');
  const [sortBy, setSortBy] = useState('default');
  const [quantities, setQuantities] = useState({});
  const [addingId, setAddingId] = useState(null);
  const [notice, setNotice] = useState(null);
  const navigate = useNavigate();
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

        if (isCurrent) {
          setProducts(inventory.map(toDisplayProduct));
        }
      } catch (error) {
        if (isCurrent) {
          setLoadError(
            error instanceof Error
              ? error.message
              : 'Unable to load inventory.'
          );
        }
      } finally {
        if (isCurrent) {
          setIsLoading(false);
        }
      }
    }

    loadInventory();

    return () => {
      isCurrent = false;
    };
  }, []);

  const visibleProducts = useMemo(() => {
    const query = search.trim().toLowerCase();

    const filteredProducts = query
      ? products.filter((product) => product.name.includes(query))
      : products;

    return [...filteredProducts].sort((firstProduct, secondProduct) => {
      switch (sortBy) {
        case 'name-asc':
          return firstProduct.name.localeCompare(secondProduct.name);

        case 'price-asc':
          return firstProduct.numericPrice - secondProduct.numericPrice;

        case 'price-desc':
          return secondProduct.numericPrice - firstProduct.numericPrice;

        default:
          return 0;
      }
    });
  }, [products, search, sortBy]);

  function changeQuantity(productId, amount, stock) {
    setQuantities((current) => ({
      ...current,
      [productId]: Math.min(
        stock,
        Math.max(0, (current[productId] ?? 0) + amount)
      ),
    }));
  }

  async function addToCart(product) {
    const quantity = quantities[product.id] ?? 0;

    if (quantity === 0) return;

    setAddingId(product.id);
    setNotice(null);
    try {
      await addCartItem(product.id, quantity);
      setQuantities((current) => ({
        ...current,
        [product.id]: 0,
      }));
      setNotice({ kind: 'ok', text: `Added ${quantity} ${product.name} to your cart.` });
    } catch (error) {
      if (error.status === 401) {
        navigate('/login');
      } else if (error.status === 403) {
        setNotice({ kind: 'error', text: 'Employee accounts can’t shop. Log in as a customer to place an order.' });
      } else {
        setNotice({ kind: 'error', text: error.message || 'Couldn’t add that to your cart.' });
      }
    } finally {
      setAddingId(null);
    }
  }

  return (
    <div className="min-h-screen bg-paper px-5 pb-12 sm:px-8 lg:px-12">
      <div className="mx-auto max-w-7xl">
        <Header showCart />

        <main className="pt-6 sm:pt-8">
          <div className="my-10 flex flex-col items-center justify-center gap-4 sm:flex-row">
            <label className="w-full max-w-md">
              <span className="sr-only">Search products</span>

              <input
                value={search}
                onChange={(event) => setSearch(event.target.value)}
                placeholder="Search the market..."
                className="min-h-12 w-full rounded-xl border-2 border-brand-green-100 bg-white px-4 text-ink outline-none transition placeholder:text-ink/45 focus:border-brand-green-500"
              />
            </label>

            <label className="flex min-h-12 items-center gap-2 rounded-xl border-2 border-brand-green-100 bg-white px-4 text-sm font-semibold text-brand-green-700">
              <span className="whitespace-nowrap">Sort by:</span>

              <select
                value={sortBy}
                onChange={(event) => setSortBy(event.target.value)}
                className="bg-transparent font-semibold text-ink outline-none"
              >
                <option value="default">Recommended</option>
                <option value="name-asc">Name: A to Z</option>
                <option value="price-asc">Price: Low to High</option>
                <option value="price-desc">Price: High to Low</option>
              </select>
            </label>
          </div>

          {notice && (
            <p
              className={`mx-auto mb-6 max-w-xl rounded-xl px-4 py-3 text-center text-sm font-semibold ${notice.kind === 'ok' ? 'bg-brand-green-50 text-brand-green-700' : 'bg-red-50 text-red-700'}`}
              role={notice.kind === 'ok' ? 'status' : 'alert'}
            >
              {notice.text}
            </p>
          )}

          {isLoading ? (
            <p
              className="py-16 text-center font-display text-lg font-bold text-brand-green-700"
              role="status"
            >
              loading inventory...
            </p>
          ) : loadError ? (
            <p
              className="py-16 text-center font-semibold text-red-700"
              role="alert"
            >
              {loadError}
            </p>
          ) : visibleProducts.length > 0 ? (
            <ul
              className="mx-auto grid w-full max-w-5xl grid-cols-1 gap-10 sm:grid-cols-2 lg:grid-cols-3"
              aria-label="Available products"
            >
              {visibleProducts.map((product) => (
                <li key={product.id}>
                  <ProductOfferCard
                    product={product}
                    quantity={quantities[product.id] ?? 0}
                    onDecrease={() =>
                      changeQuantity(
                        product.id,
                        -1,
                        Number.POSITIVE_INFINITY
                      )
                    }
                    onIncrease={() =>
                      changeQuantity(product.id, 1, product.stock)
                    }
                    isAdding={addingId === product.id}
                    onAddToCart={() => addToCart(product)}
                  />
                </li>
              ))}
            </ul>
          ) : (
            <p className="py-16 text-center font-semibold text-ink/65">
              No products found. Try another search.
            </p>
          )}
        </main>
      </div>
    </div>
  );
}

export default ShoppingOffersPage;