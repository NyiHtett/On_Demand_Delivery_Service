import { useNavigate } from 'react-router-dom';
import Header from '../layout/Header';
import { useState } from 'react';
import { loginUser, signUpUser } from '../../services/customerService';

const NAME_PATTERN = /^[a-zA-Z\s'-]{2,40}$/;
const PASSWORD_PATTERN = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z\d]).{8,}$/;
const EMAIL_PATTERN = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;

function AuthForm({pageTitle}) {
  const navigate = useNavigate();
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [errorMessage, setErrorMessage] = useState('');

  const isValidName = NAME_PATTERN.test(name);
  const isValidEmail = EMAIL_PATTERN.test(email);
  const isValidSignupPassword = PASSWORD_PATTERN.test(password);
  // Condition evaluates directly on every render
  const isSubmitButtonEnabled = 
    (pageTitle === "Login" &&
     email !== '' &&
     isValidEmail &&
     password !== '')
      ||
    (pageTitle === "Sign Up" &&
     name !== '' &&
     isValidName &&
     email !== '' &&
     isValidEmail &&
     password !== '' &&
     isValidSignupPassword &&
     password === confirmPassword);
  
  async function handleSubmit(event) {
    event.preventDefault();

    setErrorMessage('');
    try {
    const request = pageTitle === 'Login'
      ? loginUser({ email, password })
      : signUpUser({ name, email, password });

    const response = await request;
    // Employees go straight to their dashboard; customers to the store.
    navigate(response?.user?.userType?.toLowerCase() === 'employee' ? '/manager' : '/shop');
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : 'Signup failed.');
    }
  }

  return (
    <form onSubmit={handleSubmit}>
      <div className="min-h-screen bg-paper px-5 pb-8 sm:px-8 lg:px-12">
        <div className="mx-auto max-w-7xl">
          <Header />

          <main className="flex justify-center pt-7">
            <section className="w-full max-w-2xl" aria-labelledby="home-heading">
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
                    value={name} 
                    onChange={(e) => setName(e.target.value)} 
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
                value={email} 
                onChange={(e) => setEmail(e.target.value)} 
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
                pattern={pageTitle === 'Sign Up' ? PASSWORD_PATTERN.source : undefined}
                minLength={pageTitle === 'Sign Up' ? 8 : undefined}
                value={password} 
                onChange={(e) => setPassword(e.target.value)} 
                placeholder="Enter your password..."
                className="min-h-12 w-full rounded-xl border-2 border-brand-green-100 bg-white py-3 pl-12 pr-12 text-ink outline-none transition-colors placeholder:text-ink/40 focus:border-brand-green-500"
              />
              {pageTitle === "Sign Up" && (
                <div className="mt-4">
                  <label htmlFor="confirmPassword" className="sr-only">Confirm Your Password</label>
                  <input 
                    id="confirmPassword" 
                    type="password" 
                    value={confirmPassword} 
                    onChange={(e) => setConfirmPassword(e.target.value)} 
                    placeholder="Confirm your password..." 
                    className="min-h-12 w-full rounded-xl border-2 border-brand-green-100 bg-white py-3 pl-12 pr-12 text-ink outline-none transition-colors placeholder:text-ink/40 focus:border-brand-green-500" 
                  />
                </div>
              )}
              {errorMessage && (
                <p className="mt-4 text-sm font-semibold text-red-700" role="alert">
                  {errorMessage}
                </p>
              )}
              <button disabled={!isSubmitButtonEnabled}
                      type="submit"
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
