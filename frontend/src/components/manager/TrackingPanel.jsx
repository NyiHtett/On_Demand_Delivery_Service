import OrdersTable from './OrdersTable';
import GoogleRouteMap from './GoogleRouteMap';

function TrackingPanel({ tracking, loading, error, onRefresh }) {
  const orders = tracking?.orders || [];
  const routeStatus = tracking?.routeStatus || 'Not Started';

  return (
    <section className="mt-8 space-y-5">
      {error && <p className="rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-700">{error}</p>}
      {loading ? <p className="rounded-2xl bg-white p-8 text-center text-ink/65">Loading active deliveries…</p> : (
        <>
          <GoogleRouteMap encodedPolyline={tracking?.encodedPolyline} stopLocations={tracking?.stopLocations} />
          {tracking?.routeError && <p className="rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-700">{tracking.routeError}</p>}
          <OrdersTable orders={orders} />
        </>
      )}
    </section>
  );
}

export default TrackingPanel;
