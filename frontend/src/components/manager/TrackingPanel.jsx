import OrdersTable from './OrdersTable';
import GoogleRouteMap from './GoogleRouteMap';

function TrackingPanel({ tracking, loading, error, mapApiKey, onMapApiKeyChange, onRefresh }) {
  const orders = tracking?.orders || [];
  const routeStatus = tracking?.routeStatus || 'Not Started';

  return (
    <section className="mt-8 space-y-5">
      <form
        className="rounded-2xl border-2 border-brand-green-100 bg-white p-5"
        onSubmit={(event) => {
          event.preventDefault();
          onRefresh();
        }}
      >
        <label htmlFor="maps-api-key" className="block font-display text-lg font-black text-ink">
          Google Maps API key
        </label>
        <p className="mt-1 text-sm text-ink/65">
          Enter your key to calculate and display the delivery route.
        </p>
        <div className="mt-3 flex flex-col gap-3 sm:flex-row">
          <input
            id="maps-api-key"
            type="password"
            value={mapApiKey}
            onChange={(event) => onMapApiKeyChange(event.target.value)}
            placeholder="Enter API key"
            autoComplete="off"
            className="min-h-11 min-w-0 flex-1 rounded-lg border-2 border-brand-green-100 bg-paper px-3 text-ink outline-none focus:border-brand-green-500"
          />
          <button
            type="submit"
            className="min-h-11 rounded-lg bg-brand-green-600 px-5 font-bold text-white hover:bg-brand-green-700"
          >
            load deliveries
          </button>
        </div>
      </form>
      {error && <p className="rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-700">{error}</p>}
      {loading ? <p className="rounded-2xl bg-white p-8 text-center text-ink/65">Loading active deliveries…</p> : (
        <>
          <GoogleRouteMap encodedPolyline={tracking?.encodedPolyline} stopLocations={tracking?.stopLocations} />
          {!tracking?.encodedPolyline && orders.length > 0 && !tracking?.routeError && (
            <p className="rounded-xl border border-brand-orange-100 bg-brand-orange-100/50 p-4 text-sm text-ink/70">
              The active orders were loaded, but no route was returned. Restart the backend after updating it, then try again.
            </p>
          )}
          {tracking?.routeError && <p className="rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-700">{tracking.routeError}</p>}
          <OrdersTable orders={orders} />
        </>
      )}
    </section>
  );
}

export default TrackingPanel;
