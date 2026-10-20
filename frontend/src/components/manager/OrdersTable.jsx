function OrdersTable({ orders }) {
  if (!orders.length) {
    return (
      <section className="rounded-2xl border-2 border-dashed border-brand-green-100 bg-white p-8 text-center">
        <p className="font-display text-xl font-black text-brand-green-700">No active deliveries</p>
        <p className="mt-1 text-sm text-ink/65">New orders will appear here when they are ready for delivery.</p>
      </section>
    );
  }

  return (
    <section className="overflow-hidden rounded-2xl border-2 border-brand-green-100 bg-white">
      <div className="border-b border-brand-green-100 px-5 py-4">
        <h2 className="font-display text-xl font-black text-ink">Active orders</h2>
        <p className="text-sm text-ink/65">The delivery order is the current route sequence.</p>
      </div>
      <div className="overflow-x-auto">
        <table className="w-full min-w-[680px] text-left text-sm">
          <thead className="bg-brand-green-50 text-xs uppercase tracking-wide text-brand-green-700">
            <tr><th className="px-5 py-3">Route stop</th><th className="px-5 py-3">Order ID</th><th className="px-5 py-3">Customer</th><th className="px-5 py-3">Address</th><th className="px-5 py-3">Status</th></tr>
          </thead>
          <tbody>
            {orders.map((order, index) => (
              <tr key={order.orderId} className="border-t border-brand-green-100">
                <td className="px-5 py-4"><span className="rounded-full bg-brand-orange-100 px-3 py-1 text-xs font-black text-brand-orange-500">Stop {index + 1}</span></td>
                <td className="px-5 py-4 font-bold">Order #{order.orderId}</td>
                <td className="px-5 py-4">{order.customerName}</td>
                <td className="max-w-xs px-5 py-4 text-ink/75">{order.deliveryAddress}</td>
                <td className="px-5 py-4"><span className="rounded-full bg-brand-green-50 px-3 py-1 text-xs font-bold text-brand-green-700">{order.deliveryTaskStatus || order.orderStatus}</span></td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  );
}

export default OrdersTable;
