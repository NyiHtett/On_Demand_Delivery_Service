function ProductSearch({ value, onChange, onClear }) {
  return (
    <div className="relative mx-auto w-full max-w-2xl">
      <svg
        viewBox="0 0 24 24"
        className="pointer-events-none absolute left-4 top-1/2 size-5 -translate-y-1/2 text-ink/45"
        aria-hidden="true"
      >
        <circle cx="10.5" cy="10.5" r="6.5" fill="none" stroke="currentColor" strokeWidth="2" />
        <path d="m16 16 5 5" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
      </svg>

      <label htmlFor="product-search" className="sr-only">
        Search for a product
      </label>
      <input
        id="product-search"
        type="search"
        value={value}
        onChange={(event) => onChange(event.target.value)}
        placeholder="Search for a product..."
        className="min-h-12 w-full rounded-xl border-2 border-brand-green-100 bg-white py-3 pl-12 pr-12 text-ink outline-none transition-colors placeholder:text-ink/40 focus:border-brand-green-500"
      />

      {value && (
        <button
          type="button"
          onClick={onClear}
          className="absolute right-3 top-1/2 grid size-7 -translate-y-1/2 place-items-center rounded-full bg-ink/15 text-sm font-bold text-white hover:bg-brand-orange-500 focus-visible:outline-3 focus-visible:outline-offset-2 focus-visible:outline-brand-orange-400"
          aria-label="Clear product search"
        >
          ×
        </button>
      )}
    </div>
  );
}

export default ProductSearch;
