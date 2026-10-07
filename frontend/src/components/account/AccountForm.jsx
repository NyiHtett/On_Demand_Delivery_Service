function AccountForm({ profile, onChange, onSubmit, message }) {
  return (
    <section
      className="rounded-2xl border-2 border-brand-green-100 bg-white p-6 shadow-sm sm:p-8"
      aria-labelledby="account-information-heading"
    >
      <h2
        id="account-information-heading"
        className="mb-6 font-display text-2xl font-black text-brand-green-700"
      >
        account information
      </h2>

      <form onSubmit={onSubmit} className="space-y-5">
        <div>
          <label
            htmlFor="account-name"
            className="mb-2 block font-display text-sm font-bold uppercase tracking-wide text-ink/70"
          >
            name
          </label>

          <input
            id="account-name"
            name="name"
            type="text"
            value={profile.name}
            onChange={onChange}
            required
            className="min-h-12 w-full rounded-xl border-2 border-brand-green-100 bg-brand-green-50 px-4 text-ink outline-none focus:border-brand-green-600"
          />
        </div>

        <div>
          <label
            htmlFor="account-email"
            className="mb-2 block font-display text-sm font-bold uppercase tracking-wide text-ink/70"
          >
            email
          </label>

          <input
            id="account-email"
            name="email"
            type="email"
            value={profile.email}
            onChange={onChange}
            required
            className="min-h-12 w-full rounded-xl border-2 border-brand-green-100 bg-brand-green-50 px-4 text-ink outline-none focus:border-brand-green-600"
          />
        </div>

        <div>
          <label
            htmlFor="account-address"
            className="mb-2 block font-display text-sm font-bold uppercase tracking-wide text-ink/70"
          >
            address
          </label>

          <textarea
            id="account-address"
            name="address"
            value={profile.address}
            onChange={onChange}
            required
            rows="3"
            className="w-full resize-none rounded-xl border-2 border-brand-green-100 bg-brand-green-50 px-4 py-3 text-ink outline-none focus:border-brand-green-600"
          />
        </div>

        <div>
          <label
            htmlFor="account-phone"
            className="mb-2 block font-display text-sm font-bold uppercase tracking-wide text-ink/70"
          >
            phone number
          </label>

          <input
            id="account-phone"
            name="phone"
            type="tel"
            value={profile.phone}
            onChange={onChange}
            required
            className="min-h-12 w-full rounded-xl border-2 border-brand-green-100 bg-brand-green-50 px-4 text-ink outline-none focus:border-brand-green-600"
          />
        </div>

        <div className="flex flex-col gap-3 pt-2 sm:flex-row sm:items-center">
          <button
            type="submit"
            className="min-h-12 rounded-xl bg-brand-green-600 px-6 font-display font-bold text-white transition-colors hover:bg-brand-green-700 focus-visible:outline-3 focus-visible:outline-offset-2 focus-visible:outline-brand-orange-400"
          >
            update information
          </button>      

          {message && (
            <p className="text-sm font-bold text-brand-green-700">
              {message}
            </p>
          )}
        </div>
      </form>
    </section>
  );
}

export default AccountForm;