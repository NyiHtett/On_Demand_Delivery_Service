import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import CartItem from '../components/cart/CartItem';
import CartSidebar from '../components/cart/CartSidebar';
import EmptyCart from '../components/cart/EmptyCart';
import Header from '../components/layout/Header';
import { API_TOKEN_KEY } from '../services/apiClient';
import { getCart, removeCartItem, updateCartItemQuantity } from '../services/orderService';

const productIcons = {
  apple: '🍎', avocado: '🥑', banana: '🍌', carrot: '🥕', lettuce: '🥬', milk: '🥛', toast: '🍞', tomato: '🍅',
};

function toCartItem(item) {
  const name = (item.productName ?? 'product').toLowerCase();
  const weight = Number(item.unitWeight ?? 0);
  const iconKey = Object.keys(productIcons).find((key) => name.includes(key));

  return {
    id: item.productID,
    name,
    price: Number(item.unitPrice ?? 0),
    unit: weight ? `${weight} lb` : '',
    weight,
    quantity: Number(item.quantity ?? 0),
    stock: item.stock,
    imageUrl: item.imageUrl ?? '',
    icon: productIcons[iconKey] || '🛒',
  };
}

// Totals come from the backend so the delivery-fee rule lives in one place.
function toSummary(cart) {
  const items = cart?.items || [];
  return {
    itemCount: items.reduce((total, item) => total + Number(item.quantity || 0), 0),
    totalWeight: Number(cart?.totalWeight ?? 0),
    subtotal: Number(cart?.subtotal ?? 0),
    deliveryFee: Number(cart?.deliveryFee ?? 0),
    total: Number(cart?.total ?? 0),
  };
}

function CartPage() {
  const isLoggedIn = Boolean(localStorage.getItem(API_TOKEN_KEY));
  const [cart, setCart] = useState(null);
  const [isLoading, setIsLoading] = useState(isLoggedIn);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!isLoggedIn) return undefined;
    let isCurrent = true;

    getCart()
      .then((response) => isCurrent && setCart(response))
      .catch((loadError) => isCurrent && setError(loadError.status === 403
        ? 'Employee accounts don’t have a cart. Log in as a customer to shop.'
        : loadError.message || 'Unable to load your cart.'))
      .finally(() => isCurrent && setIsLoading(false));

    return () => {
      isCurrent = false;
    };
  }, [isLoggedIn]);

  async function changeCart(request) {
    setError('');
    try {
      setCart(await request);
    } catch (changeError) {
      setError(changeError.message || 'Couldn’t update your cart.');
    }
  }

  const items = (cart?.items || []).map(toCartItem);
  const summary = toSummary(cart);

  let content;
  if (!isLoggedIn) {
    content = (
      <section className="mx-auto mt-8 max-w-xl rounded-2xl border-2 border-brand-green-100 bg-white p-8 text-center">
        <p className="text-ink/75">Log in to see your cart and check out.</p>
        <Link to="/login" className="mt-5 inline-flex rounded-xl bg-brand-green-600 px-5 py-3 font-display font-bold text-white no-underline hover:bg-brand-green-700">log in</Link>
      </section>
    );
  } else if (isLoading) {
    content = <p className="mt-8 rounded-2xl bg-white p-8 text-center text-ink/65">loading your cart...</p>;
  } else if (items.length === 0) {
    content = cart ? <EmptyCart /> : null;
  } else {
    content = (
      <>
        <div className="mt-6 grid items-start gap-7 lg:grid-cols-[minmax(0,1fr)_25rem]">
          <section
            className="overflow-hidden rounded-2xl border-2 border-brand-green-100 bg-white px-4 sm:px-6"
            aria-label="Cart items"
          >
            <ul>
              {items.map((item) => (
                <CartItem
                  key={item.id}
                  item={item}
                  onDecrease={() => changeCart(updateCartItemQuantity(item.id, item.quantity - 1))}
                  onIncrease={() => changeCart(updateCartItemQuantity(item.id, item.quantity + 1))}
                  onRemove={() => changeCart(removeCartItem(item.id))}
                />
              ))}
            </ul>
          </section>

          <CartSidebar summary={summary} />
        </div>

        <Link
          to="/shop"
          className="mt-7 inline-flex items-center gap-2 font-display font-bold text-brand-green-700 no-underline hover:text-brand-orange-500"
        >
          <span aria-hidden="true">←</span>
          continue shopping
        </Link>
      </>
    );
  }

  return (
    <div className="min-h-screen bg-paper px-5 pb-10 sm:px-8 lg:px-12">
      <div className="mx-auto max-w-7xl">
        <Header showCart />

        <main className="pt-8">
          <h1 className="font-display text-4xl font-black tracking-tight text-ink sm:text-5xl">
            my cart
          </h1>

          {error && (
            <p className="mt-6 rounded-xl border border-red-200 bg-red-50 p-4 text-red-700" role="alert">{error}</p>
          )}

          {content}
        </main>
      </div>
    </div>
  );
}

export default CartPage;
