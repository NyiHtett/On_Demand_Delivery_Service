import { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import Header from '../components/layout/Header';
import AccountForm from '../components/account/AccountForm';
import PaymentMethod from '../components/account/PaymentMethod';
import {
  getCurrentUser,
  updateCurrentUser,
} from '../services/customerService';
import {
  confirmPaymentMethodSetup,
  createPaymentMethodSetupSession,
  deletePaymentMethod,
  getPaymentMethods,
} from '../services/paymentMethodService';

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
  const [searchParams, setSearchParams] = useSearchParams();
  const activeRole = profile?.userType?.toLowerCase();
  const [paymentMethods, setPaymentMethods] = useState([]);

  const [profileMessage, setProfileMessage] = useState('');
  const [paymentMessage, setPaymentMessage] = useState('');
  useEffect(() => {
    let isCurrent = true;

    async function loadAccount() {
      try {
        const setupSessionId = searchParams.get('session_id');
        if (searchParams.get('payment_method_setup') === 'success' && setupSessionId) {
          await confirmPaymentMethodSetup(setupSessionId);
          setPaymentMessage('Payment method added.');
          setSearchParams({}, { replace: true });
        }
        const account = await getCurrentUser();
        if (!isCurrent) return;

        const nextProfile = toProfile(account);
        setProfile(nextProfile);
        setPaymentMethods(nextProfile.userType.toLowerCase() === 'customer'
          ? await getPaymentMethods()
          : []);
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
  }, [searchParams, setSearchParams]);

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
    setProfileMessage('');
    setProfileError('');

    // phone is optional, but if given it needs 10-15 digits
    const phone = profile.phone.trim();
    const digits = phone.replace(/\D/g, '');
    if (phone && (!/^\+?[\d\s().-]+$/.test(phone) || digits.length < 10 || digits.length > 15 || phone.length > 20)) {
      setProfileError('Enter a valid phone number (10-15 digits).');
      return;
    }

    setIsSaving(true);

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

  async function handleAddPaymentMethod() {
    setPaymentMessage('opening secure payment setup…');
    try {
      const { url } = await createPaymentMethodSetupSession();
      window.location.assign(url);
    } catch (error) {
      setPaymentMessage(error instanceof Error ? error.message : 'Unable to add payment method.');
    }
  }

  async function handleDeletePayment(paymentId) {
    try {
      await deletePaymentMethod(paymentId);
      setPaymentMethods((currentPayments) => currentPayments.filter((payment) => payment.id !== paymentId));
      setPaymentMessage('Payment method deleted.');
    } catch (error) {
      setPaymentMessage(error instanceof Error ? error.message : 'Unable to delete payment method.');
    }
  }

  if (isLoading) {
    return (
      <div className="min-h-screen bg-paper px-5 pb-10 sm:px-8 lg:px-12">
        <Header showCart />
        <p className="py-16 text-center font-display text-lg font-bold text-brand-green-700" role="status">
          loading account information...
        </p>
      </div>
    );
  }

  if (!profile) {
    return (
      <div className="min-h-screen bg-paper px-5 pb-10 sm:px-8 lg:px-12">
        <Header showCart />
        <p className="py-16 text-center font-semibold text-red-700" role="alert">
          {profileError || 'Unable to load account information.'}
        </p>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-paper px-5 pb-10 sm:px-8 lg:px-12">
      <div className="mx-auto max-w-4xl">
        <Header showCart />

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
                      onDelete={handleDeletePayment}
                    />
                  ))
                )}
              </div>

              <div className="mt-6 flex flex-col gap-3 border-t-2 border-brand-green-100 pt-6 sm:flex-row sm:items-center">
                <button
                  type="button"
                  onClick={handleAddPaymentMethod}
                  className="min-h-12 rounded-xl bg-brand-green-600 px-6 font-display font-bold text-white transition-colors hover:bg-brand-green-700 focus-visible:outline-3 focus-visible:outline-offset-2 focus-visible:outline-brand-orange-400"
                >
                  add payment method securely
                </button>
                {paymentMessage && (
                  <p className="text-sm font-bold text-brand-green-700">
                    {paymentMessage}
                  </p>
                )}
              </div>
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
