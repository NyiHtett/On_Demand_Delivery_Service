import { useState, useEffect } from 'react';
import Header from '../components/layout/Header';
import AccountForm from '../components/account/AccountForm';
import PaymentMethod from '../components/account/PaymentMethod';
import {
  getCurrentUser,
  updateCurrentUser,
} from '../services/customerService';

function toProfile(account) {
  return {
    id: account.id ?? account.user_id,
    name: account.name ?? account.user_name ?? '',
    email: account.email ?? '',
    address: account.address ?? '',
    phone: account.phone ?? '',
    userType: account.userType ?? account.user_type ?? '',
  };
}

function AccountPage() {
  const [profile, setProfile] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [profileError, setProfileError] = useState('');
  const activeRole = profile?.userType?.toLowerCase();

  const paymentMethodsByRole = {
    customer: [
      {
        id: 1,
        cardType: 'Visa',
        lastFour: '1121',
        billingName: 'Customer Joe',
        expirationDate: '2028-12',
      },
    ],
  };

  const [paymentMethods, setPaymentMethods] = useState(
    paymentMethodsByRole[activeRole] || [],
  );

  const [profileMessage, setProfileMessage] = useState('');
  const [paymentMessage, setPaymentMessage] = useState('');
  const [editingPaymentId, setEditingPaymentId] = useState(null);

  const [paymentForm, setPaymentForm] = useState({
    cardNumber: '',
    cardType: 'Visa',
    billingName: '',
    expirationDate: '',
  });

  useEffect(() => {
    let isCurrent = true;

    async function loadAccount() {
      try {
        const account = await getCurrentUser();
        if (!isCurrent) return;

        const nextProfile = toProfile(account);
        setProfile(nextProfile);
        setPaymentMethods(paymentMethodsByRole[nextProfile.userType.toLowerCase()] || []);
        setPaymentForm((currentPayment) => ({
          ...currentPayment,
          billingName: nextProfile.name,
        }));
      } catch (error) {
        if (isCurrent) {
          setProfileError(
            error instanceof Error
              ? error.message
              : 'Unable to load account information.',
          );
        }
      } finally {
        if (isCurrent) setIsLoading(false);
      }
    }

    loadAccount();
    return () => {
      isCurrent = false;
    };
  }, []);

  function handleProfileChange(event) {
    const { name, value } = event.target;

    setProfile((currentProfile) => ({
      ...currentProfile,
      [name]: value,
    }));

    setProfileMessage('');
  }

  async function handleProfileUpdate(event) {
    event.preventDefault();
    setIsSaving(true);
    setProfileMessage('');
    setProfileError('');

    try {
      const updatedAccount = await updateCurrentUser(profile);
      setProfile(toProfile(updatedAccount));
      setProfileMessage('Account information updated.');
    } catch (error) {
      setProfileError(
        error instanceof Error
          ? error.message
          : 'Unable to update account information.',
      );
    } finally {
      setIsSaving(false);
    }
  }

  function handlePaymentChange(event) {
    const { name, value } = event.target;

    setPaymentForm((currentPayment) => ({
      ...currentPayment,
      [name]: value,
    }));

    setPaymentMessage('');
  }

  function resetPaymentForm() {
    setPaymentForm({
      cardNumber: '',
      cardType: 'Visa',
      billingName: profile.name,
      expirationDate: '',
    });

    setEditingPaymentId(null);
  }

  function handlePaymentSubmit(event) {
    event.preventDefault();

    if (!paymentForm.billingName || !paymentForm.expirationDate) {
      setPaymentMessage('Please complete all payment fields.');
      return;
    }

    if (!editingPaymentId && paymentForm.cardNumber.length < 4) {
      setPaymentMessage('Please enter a valid card number.');
      return;
    }

    if (editingPaymentId) {
      setPaymentMethods((currentPayments) =>
        currentPayments.map((payment) =>
          payment.id === editingPaymentId
            ? {
                ...payment,
                cardType: paymentForm.cardType,
                billingName: paymentForm.billingName,
                expirationDate: paymentForm.expirationDate,
              }
            : payment,
        ),
      );

      setPaymentMessage('Payment method updated.');
    } else {
      const cleanCardNumber = paymentForm.cardNumber.replace(/\D/g, '');

      setPaymentMethods((currentPayments) => [
        ...currentPayments,
        {
          id: Date.now(),
          cardType: paymentForm.cardType,
          lastFour: cleanCardNumber.slice(-4),
          billingName: paymentForm.billingName,
          expirationDate: paymentForm.expirationDate,
        },
      ]);

      setPaymentMessage('Payment method added.');
    }

    resetPaymentForm();
  }

  function handleEditPayment(payment) {
    setEditingPaymentId(payment.id);

    setPaymentForm({
      cardNumber: '',
      cardType: payment.cardType,
      billingName: payment.billingName,
      expirationDate: payment.expirationDate,
    });

    setPaymentMessage('');
  }

  function handleDeletePayment(paymentId) {
    setPaymentMethods((currentPayments) =>
      currentPayments.filter((payment) => payment.id !== paymentId),
    );

    setPaymentMessage('Payment method deleted.');
  }

  if (isLoading) {
    return (
      <div className="min-h-screen bg-paper px-5 pb-10 sm:px-8 lg:px-12">
        <Header showCart cartCount={0} />
        <p className="py-16 text-center font-display text-lg font-bold text-brand-green-700" role="status">
          loading account information...
        </p>
      </div>
    );
  }

  if (!profile) {
    return (
      <div className="min-h-screen bg-paper px-5 pb-10 sm:px-8 lg:px-12">
        <Header showCart cartCount={0} />
        <p className="py-16 text-center font-semibold text-red-700" role="alert">
          {profileError || 'Unable to load account information.'}
        </p>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-paper px-5 pb-10 sm:px-8 lg:px-12">
      <div className="mx-auto max-w-4xl">
        <Header showCart cartCount={0} />

        <main className="pt-8 sm:pt-10">
          <div className="mb-8 text-center">
            <p className="font-display text-sm font-bold uppercase tracking-[0.22em] text-brand-orange-500">
              OFS delivery
            </p>

            <h1 className="mt-1 font-display text-4xl font-black tracking-tight text-brand-green-700 sm:text-5xl">
              my account
            </h1>

            <p className="mt-2 text-lg text-ink/75">
              Manage your account information
            </p>
          </div>

          <AccountForm
            profile={profile}
            onChange={handleProfileChange}
            onSubmit={handleProfileUpdate}
            message={profileError || profileMessage}
            isSaving={isSaving}
          />

          {activeRole === 'customer' && (
            <section
              className="mt-8 rounded-2xl border-2 border-brand-green-100 bg-white p-6 shadow-sm sm:p-8"
              aria-labelledby="payment-methods-heading"
            >
              <div className="mb-6">
                <h2
                  id="payment-methods-heading"
                  className="font-display text-2xl font-black text-brand-green-700"
                >
                  payment methods
                </h2>

                <p className="mt-1 text-sm text-ink/65">
                  Add, edit, or remove your saved payment methods.
                </p>
              </div>

              <div className="space-y-3">
                {paymentMethods.length === 0 ? (
                  <div className="rounded-xl border-2 border-dashed border-brand-green-100 bg-brand-green-50 p-5 text-center">
                    <p className="font-bold text-ink/70">
                      No payment methods saved.
                    </p>
                  </div>
                ) : (
                  paymentMethods.map((payment) => (
                    <PaymentMethod
                      key={payment.id}
                      payment={payment}
                      onEdit={handleEditPayment}
                      onDelete={handleDeletePayment}
                    />
                  ))
                )}
              </div>

              <form
                onSubmit={handlePaymentSubmit}
                className="mt-6 border-t-2 border-brand-green-100 pt-6"
              >
                <h3 className="mb-4 font-display text-xl font-black text-ink">
                  {editingPaymentId
                    ? 'edit payment method'
                    : 'add payment method'}
                </h3>

                <div className="grid gap-5 sm:grid-cols-2">
                  {!editingPaymentId && (
                    <div className="sm:col-span-2">
                      <label
                        htmlFor="card-number"
                        className="mb-2 block font-display text-sm font-bold uppercase tracking-wide text-ink/70"
                      >
                        card number
                      </label>

                      <input
                        id="card-number"
                        name="cardNumber"
                        type="text"
                        inputMode="numeric"
                        value={paymentForm.cardNumber}
                        onChange={handlePaymentChange}
                        placeholder="Enter card number"
                        className="min-h-12 w-full rounded-xl border-2 border-brand-green-100 bg-brand-green-50 px-4 text-ink outline-none focus:border-brand-green-600"
                      />
                    </div>
                  )}

                  <div>
                    <label
                      htmlFor="card-type"
                      className="mb-2 block font-display text-sm font-bold uppercase tracking-wide text-ink/70"
                    >
                      card type
                    </label>

                    <select
                      id="card-type"
                      name="cardType"
                      value={paymentForm.cardType}
                      onChange={handlePaymentChange}
                      className="min-h-12 w-full rounded-xl border-2 border-brand-green-100 bg-brand-green-50 px-4 text-ink outline-none focus:border-brand-green-600"
                    >
                      <option value="Visa">Visa</option>
                      <option value="Mastercard">Mastercard</option>
                      <option value="American Express">
                        American Express
                      </option>
                      <option value="Discover">Discover</option>
                    </select>
                  </div>

                  <div>
                    <label
                      htmlFor="expiration-date"
                      className="mb-2 block font-display text-sm font-bold uppercase tracking-wide text-ink/70"
                    >
                      expiration date
                    </label>

                    <input
                      id="expiration-date"
                      name="expirationDate"
                      type="month"
                      value={paymentForm.expirationDate}
                      onChange={handlePaymentChange}
                      required
                      className="min-h-12 w-full rounded-xl border-2 border-brand-green-100 bg-brand-green-50 px-4 text-ink outline-none focus:border-brand-green-600"
                    />
                  </div>

                  <div className="sm:col-span-2">
                    <label
                      htmlFor="billing-name"
                      className="mb-2 block font-display text-sm font-bold uppercase tracking-wide text-ink/70"
                    >
                      billing name
                    </label>

                    <input
                      id="billing-name"
                      name="billingName"
                      type="text"
                      value={paymentForm.billingName}
                      onChange={handlePaymentChange}
                      required
                      className="min-h-12 w-full rounded-xl border-2 border-brand-green-100 bg-brand-green-50 px-4 text-ink outline-none focus:border-brand-green-600"
                    />
                  </div>
                </div>

                <div className="mt-5 flex flex-col gap-3 sm:flex-row sm:items-center">
                  <button
                    type="submit"
                    className="min-h-12 rounded-xl bg-brand-green-600 px-6 font-display font-bold text-white transition-colors hover:bg-brand-green-700 focus-visible:outline-3 focus-visible:outline-offset-2 focus-visible:outline-brand-orange-400"
                  >
                    {editingPaymentId
                      ? 'save payment method'
                      : 'add payment method'}
                  </button>

                  {editingPaymentId && (
                    <button
                      type="button"
                      onClick={resetPaymentForm}
                      className="min-h-12 rounded-xl border-2 border-brand-green-600 px-6 font-display font-bold text-brand-green-700 transition-colors hover:bg-brand-green-50"
                    >
                      cancel
                    </button>
                  )}

                  {paymentMessage && (
                    <p className="text-sm font-bold text-brand-green-700">
                      {paymentMessage}
                    </p>
                  )}
                </div>
              </form>

               
            </section>
          )}
          <div className="mt-8 flex justify-center">
            <button
              onClick={() => {
                localStorage.removeItem('api_token');
                window.location.href = '/login';
              }}
              type="button"
              className="min-h-12 rounded-xl bg-red-600 px-6 font-display font-bold text-white transition-colors hover:bg-red-700 focus-visible:outline-3 focus-visible:outline-offset-2 focus-visible:outline-red-500"
            >
              Log out
            </button>
          </div>
        </main>
      </div>
    </div>
  );
}

export default AccountPage;