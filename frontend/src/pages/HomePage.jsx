import { Link } from 'react-router-dom';
import Header from '../components/layout/Header';

const availableItems = [
  { id: 1, name: 'apples', price: '$2.49', icon: '🍎' },
  { id: 2, name: 'carrots', price: '$1.89', icon: '🥕' },
  { id: 3, name: 'bread', price: '$3.25', icon: '🍞' },
  { id: 4, name: 'avocados', price: '$1.50', icon: '🥑' },
  { id: 5, name: 'bananas', price: '$1.29', icon: '🍌' },
  { id: 6, name: 'tomatoes', price: '$2.15', icon: '🍅' },
  { id: 7, name: 'milk', price: '$4.10', icon: '🥛' },
  { id: 8, name: 'lettuce', price: '$2.75', icon: '🥬' },
];

const cartPreview = [
  { id: 1, name: 'apples', quantity: 2 },
  { id: 2, name: 'bread', quantity: 1 },
  { id: 3, name: 'carrots', quantity: 1 },
];

function HomePage() {
  return (
    <div className="min-h-screen bg-paper px-5 pb-8 sm:px-8 lg:px-12">
      <div className="mx-auto max-w-7xl">
        <Header />

        <main className="grid gap-8 pt-7 lg:grid-cols-[minmax(0,1fr)_17rem] lg:gap-10">
          <section className="min-w-0" aria-labelledby="home-heading">
            <div className="mb-8 text-center">
              <p className="mb-1 font-display text-sm font-bold uppercase tracking-[0.18em] text-brand-orange-500">
                OFS delivery
              </p>
              <h1 id="home-heading" className="font-display text-4xl font-black tracking-tight text-brand-green-700 sm:text-5xl">
                fresh groceries
              </h1>
              <p className="mx-auto mt-3 max-w-xl text-base leading-7 text-ink/75 sm:text-lg">
                Organic favorites from your neighborhood store, delivered fast :)
              </p>
            </div>

            <div className="mb-5 flex items-end justify-between gap-4">
              <h2 className="font-display text-xl font-black text-ink sm:text-2xl">
                available items at <span className="text-brand-green-600">OFS Market</span>
              </h2>
            </div>

            <ul className="grid grid-cols-2 gap-3 sm:grid-cols-3 md:grid-cols-4" aria-label="Available items">
              {availableItems.map((item) => (
                <li key={item.id}>
                  <article className="flex min-h-40 flex-col justify-between rounded-2xl border-2 border-brand-green-100 bg-white p-4 transition-colors hover:border-brand-green-500">
                    <span className="text-4xl" role="img" aria-label={item.name}>
                      {item.icon}
                    </span>
                    <div className="mt-4">
                      <h3 className="font-display text-base font-bold text-ink">{item.name}</h3>
                      <p className="mt-1 text-sm font-bold text-brand-orange-500">{item.price}</p>
                    </div>
                  </article>
                </li>
              ))}
            </ul>

            <Link
              to="/shop"
              className="mt-5 flex min-h-12 w-full items-center justify-center rounded-xl bg-brand-green-600 px-5 font-display font-bold text-white no-underline transition-colors hover:bg-brand-green-700 focus-visible:outline-3 focus-visible:outline-offset-2 focus-visible:outline-brand-orange-400"
            >
              click to go to store
            </Link>
          </section>

          <aside className="border-t-2 border-brand-green-100 pt-6 lg:border-l-2 lg:border-t-0 lg:pl-8 lg:pt-0" aria-labelledby="cart-heading">
            <div className="mb-5 flex items-center justify-between">
              <h2 id="cart-heading" className="font-display text-2xl font-black text-brand-green-700">
                my cart
              </h2>
              <span className="grid size-8 place-items-center rounded-full bg-brand-orange-100 text-sm font-bold text-brand-orange-500">
                4
              </span>
            </div>

            <ul className="space-y-3">
              {cartPreview.map((item) => (
                <li key={item.id} className="flex min-h-20 items-center justify-between rounded-xl border-2 border-brand-green-100 bg-white px-4 py-3">
                  <span className="font-display font-bold text-ink">{item.name}</span>
                  <span className="text-sm font-bold text-brand-green-600">x{item.quantity}</span>
                </li>
              ))}
            </ul>

            <Link
              to="/cart"
              className="mt-5 flex min-h-11 w-full items-center justify-center rounded-xl border-2 border-brand-green-600 px-4 font-display font-bold text-brand-green-700 no-underline transition-colors hover:bg-brand-green-50 focus-visible:outline-3 focus-visible:outline-offset-2 focus-visible:outline-brand-orange-400"
            >
              view cart
            </Link>
          </aside>
        </main>
      </div>
    </div>
  );
}

export default HomePage;
