import { useEffect, useState } from 'react';

const statusStyles = {
  Pending: 'bg-brand-orange-100 text-brand-orange-500',
  Ordered: 'bg-brand-green-50 text-brand-green-700',
  Shipping: 'bg-brand-orange-100 text-brand-orange-500',
  Completed: 'bg-brand-green-600 text-white',
  Cancelled: 'bg-red-50 text-red-700',
};

const statusLabels = {
  Pending: 'awaiting payment',
  Ordered: 'waiting for robot',
  Shipping: 'on the way',
  Completed: 'delivered',
  Cancelled: 'cancelled',
};

export function formatMoney(value) {
  return `$${Number(value || 0).toFixed(2)}`;
}

export function formatCountdown(ms) {
  const seconds = Math.max(0, Math.round(ms / 1000));
  return `${Math.floor(seconds / 60)}:${String(seconds % 60).padStart(2, '0')}`;
}

export function formatClock(epochMs) {
  return new Date(epochMs).toLocaleTimeString([], { hour: 'numeric', minute: '2-digit' });
}

// Re-renders every second so countdowns tick between polls.
export function useNow() {
  const [now, setNow] = useState(Date.now());
  useEffect(() => {
    const timer = setInterval(() => setNow(Date.now()), 1000);
    return () => clearInterval(timer);
  }, []);
  return now;
}

export function StatusBadge({ status }) {
  return (
    <span className={`rounded-full px-3 py-1 text-sm font-bold ${statusStyles[status] || 'bg-brand-green-50 text-brand-green-700'}`}>
      {statusLabels[status] || status}
    </span>
  );
}

/** One line describing where the order is: robot timer, ETA, or delivered. */
export function DeliveryLine({ order }) {
  const now = useNow();
  const { orderStatus, estimatedDispatch, estimatedArrival } = order;

  let text = null;
  if (orderStatus === 'Ordered' && estimatedDispatch) {
    text = estimatedDispatch > now
      ? `Robot leaves the store in ${formatCountdown(estimatedDispatch - now)}. More orders joining can make it leave sooner.`
      : 'Robot is loading up and leaving now.';
  } else if (orderStatus === 'Shipping' && estimatedArrival) {
    text = estimatedArrival > now
      ? `Robot arrives around ${formatClock(estimatedArrival)} (in ${formatCountdown(estimatedArrival - now)}).`
      : 'Robot is pulling up now.';
  } else if (orderStatus === 'Completed') {
    text = estimatedArrival ? `Delivered at ${formatClock(estimatedArrival)}.` : 'Delivered.';
  } else if (orderStatus === 'Pending') {
    text = 'Waiting for payment to finish.';
  }

  return text ? <p className="text-sm font-semibold text-brand-green-700" aria-live="polite">{text}</p> : null;
}
