import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import CartItem from '../components/cart/CartItem';
import CartSidebar from '../components/cart/CartSidebar';
import EmptyCart from '../components/cart/EmptyCart';
import Header from '../components/layout/Header';
import { API_TOKEN_KEY } from '../services/apiClient';
import { getInventory } from '../services/customerService';
import { getCart } from '../services/orderService';

const initialCartItems = [
  { id: 1, name: 'apples', price: 2.49, unit: '1 lb', weight: 0.5, quantity: 2, icon: '🍎' },
  { id: 2, name: 'bread', price: 3.25, unit: '1 loaf (16 oz)', weight: 1, quantity: 1, icon: '🍞' },
  { id: 3, name: 'carrots', price: 1.89, unit: '1 lb', weight: 1, quantity: 1, icon: '🥕' },
  { id: 4, name: 'avocados', price: 1.5, unit: '1 each', weight: 1, quantity: 1, icon: '🥑' },
];

const deliveryFee = 2.99;

function getProductId(item) {
  return item.productID ?? item.productId ?? item.product_id ?? item.id;
}

function getImageUrl(item) {
  return item.imageUrl ?? item.image_url ?? '';
}

function toCartItem(item, productsById, productsByName) {
  const weight = Number(item.unitWeight ?? item.unit_weight ?? item.weight ?? 0);
  const productId = getProductId(item);
  const name = (item.productName ?? item.product_name ?? item.name ?? 'product').toLowerCase();
  const backendProduct = productsById.get(String(productId)) || productsByName.get(name);

  return {
    id: productId ?? item.cartItemID ?? item.id,
    name,
    price: Number(item.unitPrice ?? item.unit_price ?? item.price ?? 0),
    unit: weight ? `${weight} lb` : '',
    weight,
    quantity: Number(item.quantity ?? 0),
    imageUrl: getImageUrl(item) || (backendProduct ? getImageUrl(backendProduct) : ''),
    icon: item.icon ?? '🛒',
  };
}

function CartPage() {
  const [cartItems, setCartItems] = useState(initialCartItems);

  useEffect(() => {
    if (!localStorage.getItem(API_TOKEN_KEY)) return undefined;

    let isCurrent = true;

    Promise.all([
      getCart(),
      getInventory().catch(() => []),
    ])
      .then(([cart, inventoryResponse]) => {
        if (!isCurrent) return;
        const items = Array.isArray(cart) ? cart : cart?.items || [];
        const inventory = Array.isArray(inventoryResponse)
          ? inventoryResponse
          : inventoryResponse?.products || inventoryResponse?.items || [];
        const productsById = new Map(
          inventory.map((product) => [String(getProductId(product)), product]),
        );
        const productsByName = new Map(
          inventory.map((product) => [
            (product.name ?? product.productName ?? product.product_name ?? '').toLowerCase(),
            product,
          ]),
        );

        setCartItems(items.map((item) => toCartItem(item, productsById, productsByName)));
      })
      .catch(() => {
        // Keep the local preview if the cart cannot be loaded.
      });

    return () => {
      isCurrent = false;
    };
  }, []);

  const summary = useMemo(() => {
    return cartItems.reduce(
      (totals, item) => ({
        itemCount: totals.itemCount + item.quantity,
        totalWeight: totals.totalWeight + item.weight * item.quantity,
        subtotal: totals.subtotal + item.price * item.quantity,
      }),
      { itemCount: 0, totalWeight: 0, subtotal: 0 },
    );
  }, [cartItems]);

  function updateQuantity(itemId, amount) {
    setCartItems((currentItems) =>
      currentItems.map((item) =>
        item.id === itemId
          ? { ...item, quantity: Math.max(1, item.quantity + amount) }
          : item,
      ),
    );
  }

  function removeItem(itemId) {
    setCartItems((currentItems) => currentItems.filter((item) => item.id !== itemId));
  }

  return (
    <div className="min-h-screen bg-paper px-5 pb-10 sm:px-8 lg:px-12">
      <div className="mx-auto max-w-7xl">
        <Header showCart cartCount={summary.itemCount} />

        <main className="pt-8">
          <h1 className="font-display text-4xl font-black tracking-tight text-ink sm:text-5xl">
            my cart
          </h1>

          {cartItems.length > 0 ? (
            <>
              <div className="mt-6 grid items-start gap-7 lg:grid-cols-[minmax(0,1fr)_25rem]">
                <section
                  className="overflow-hidden rounded-2xl border-2 border-brand-green-100 bg-white px-4 sm:px-6"
                  aria-label="Cart items"
                >
                  <ul>
                    {cartItems.map((item) => (
                      <CartItem
                        key={item.id}
                        item={item}
                        onDecrease={() => updateQuantity(item.id, -1)}
                        onIncrease={() => updateQuantity(item.id, 1)}
                        onRemove={() => removeItem(item.id)}
                      />
                    ))}
                  </ul>
                </section>

                <CartSidebar summary={summary} deliveryFee={deliveryFee} />
              </div>

              <Link
                to="/shop"
                className="mt-7 inline-flex items-center gap-2 font-display font-bold text-brand-green-700 no-underline hover:text-brand-orange-500"
              >
                <span aria-hidden="true">←</span>
                continue shopping
              </Link>
            </>
          ) : (
            <EmptyCart />
          )}
        </main>
      </div>
    </div>
  );
}

export default CartPage;
