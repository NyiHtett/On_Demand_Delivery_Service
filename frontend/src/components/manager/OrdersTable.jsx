import { Fragment, useEffect, useState } from 'react';
import { getOrders } from '../../services/orderService';

function OrdersTable() {
  const [orders, setOrders] = useState([]);
  const [expandedIds, setExpandedIds] = useState([]);

  useEffect(() => {
    getOrders().then((data) => {
      console.log("orders from backend: ", data);
      setOrders(data);
    }).catch((e) => console.error("issue loading errors: ", e));
  }, []);

  function toggleExpanded(orderId) {
    setExpandedIds((current) => 
      current.includes(orderId) ? current.filter((id) => id !== orderId) : [...current, orderId]
    )
  }

  return (
    <section className="mt-4 overflow-x-auto max-w-5xl mx-auto rounded-xl border-2 border-brand-green-100">
      <table className="w-full text-left text-sm text-ink">
        <thead className="bg-brand-green-50 font-bold">
          <tr>
            <th className="p-3"></th> {/* putting arrows in the column to expand the orders */}
            <th className="p-3">Order #</th>
            <th className="p-3">Customer ID</th>
            <th className="p-3">Items</th>
            <th className="p-3">Weight</th>
            <th className="p-3">Total</th>
            <th className="p-3">Status</th>
          </tr>
        </thead>

        <tbody>
          {orders.map((order) => {
            const total = Number(order.subtotal) + Number(order.deliveryFee);
            const isExpanded = expandedIds.includes(order.orderId);

            return (
              <Fragment key={order.orderId}>
                <tr className="border-t border-brand-green-100">
                  <td className="p-3">
                    <button type="button" onClick={() => toggleExpanded(order.orderId)} className="font-bold text-brand-green-700">
                      {isExpanded ? '▾': '▸'}
                    </button>
                  </td>
                  <td className="p-3 font-bold">#{order.orderId}</td>
                  <td className="p-3">{order.customerId}</td>
                  <td className="p-3">{order.items.length}</td>
                  <td className="p-3">{order.totalWeight}</td>
                  <td className="p-3">${Number(total).toFixed(2)}</td>
                  <td className="p-3">{order.orderStatus}</td>
                </tr>

                {isExpanded && (
                  <tr className="bg-brand-green-50">
                    <td colSpan={7} className="p-3 pl-12">
                      <p className="mb-2 text-ink/75">Deliver to: {order.deliveryAddress}</p>
                      {order.items.map((item) => (
                        <p key = {item.orderItemId}>
                          {item.quantity} x {item.productName} @ ${Number(item.unitPrice).toFixed(2)} and {item.unitWeight} lbs per unit
                        </p>
                      ))}
                    </td>
                  </tr>
                ) /*this part is the expansion part when the user clicks the arrow*/}
              </Fragment>
            )
          })}
        </tbody>
      </table>
    </section>
  );
}

export default OrdersTable;
