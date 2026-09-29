function PaymentMethod({ payment, onEdit, onDelete }) {
    return (
      <div className="flex flex-col gap-4 rounded-xl border-2 border-brand-green-100 bg-brand-green-50 p-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <p className="font-display font-bold text-brand-green-700">
            {payment.cardType} ending in {payment.lastFour}
          </p>
  
          <p className="mt-1 text-sm text-ink/70">
            {payment.billingName} · Expires {payment.expirationDate}
          </p>
        </div>
  
        <div className="flex gap-2">
          <button
            type="button"
            onClick={() => onEdit(payment)}
            className="rounded-lg border-2 border-brand-green-600 px-4 py-2 font-display text-sm font-bold text-brand-green-700 transition-colors hover:bg-white"
          >
            edit
          </button>
  
          <button
            type="button"
            onClick={() => onDelete(payment.id)}
            className="rounded-lg border-2 border-brand-orange-400 px-4 py-2 font-display text-sm font-bold text-brand-orange-500 transition-colors hover:bg-brand-orange-50"
          >
            delete
          </button>
        </div>
      </div>
    );
  }
  
  export default PaymentMethod;