import { Link } from 'react-router-dom';
import Header from '../layout/Header';

function AuthForm({pageTitle}) {
  return (
    <form>
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
                  {pageTitle}
                </h1>
                <p className="mx-auto mt-3 max-w-xl text-base leading-7 text-ink/75 sm:text-lg">
                  Organic favorites from your neighborhood store, delivered fast :)
                </p>
              </div>

              {pageTitle === "Sign Up" && (
                <div className="mt-4">
                  <div className="mb-5 flex items-end justify-between gap-4">
                    <h2 className="font-display text-xl font-black text-ink sm:text-2xl">
                      Name
                    </h2>
                  </div>
                  <label htmlFor="Email" className="sr-only">
                    Enter Your Name
                  </label>
                  <input
                    id="username"
                    type="text"
                    placeholder="Enter your name..."
                    className="min-h-12 w-full rounded-xl border-2 border-brand-green-100 bg-white py-3 pl-12 pr-12 text-ink outline-none transition-colors placeholder:text-ink/40 focus:border-brand-green-500"
                  />
                </div>
              )}
              <div className="mb-5 flex items-end justify-between gap-4">
                <h2 className="font-display text-xl font-black text-ink sm:text-2xl">
                  Email
                </h2>
              </div>


              <label htmlFor="Email" className="sr-only">
                Enter Your Email
              </label>
              <input
                id="username"
                type="email"
                placeholder="Enter your email..."
                className="min-h-12 w-full rounded-xl border-2 border-brand-green-100 bg-white py-3 pl-12 pr-12 text-ink outline-none transition-colors placeholder:text-ink/40 focus:border-brand-green-500"
              />
              <div className="mb-5 flex items-end justify-between gap-4">
                <h2 className="font-display text-xl font-black text-ink sm:text-2xl">
                  Password
                </h2>
              </div>
              <label htmlFor="Password" className="sr-only">
                Enter Your Password
              </label>
              <input
                id="password"
                type="password"
                placeholder="Enter your password..."
                className="min-h-12 w-full rounded-xl border-2 border-brand-green-100 bg-white py-3 pl-12 pr-12 text-ink outline-none transition-colors placeholder:text-ink/40 focus:border-brand-green-500"
              />
              {pageTitle === "Sign Up" && (
                <div className="mt-4">
                  <label htmlFor="confirmPassword" className="sr-only">Confirm Your Password</label>
                  <input 
                    id="confirmPassword" 
                    type="password" 
                    placeholder="Confirm your password..." 
                    className="min-h-12 w-full rounded-xl border-2 border-brand-green-100 bg-white py-3 pl-12 pr-12 text-ink outline-none transition-colors placeholder:text-ink/40 focus:border-brand-green-500" 
                  />
                </div>
              )}

              <button onClick={() => console.log('Clicked!')}
                      className="mt-5 flex min-h-12 w-full items-center justify-center rounded-xl bg-brand-green-600 px-5 font-display font-bold text-white no-underline transition-colors hover:bg-brand-green-700 focus-visible:outline-3 focus-visible:outline-offset-2 focus-visible:outline-brand-orange-400"
              >
                {pageTitle}
              </button>
            </section>
          </main>
        </div>
      </div>
    </form>
  );
}

export default AuthForm;
