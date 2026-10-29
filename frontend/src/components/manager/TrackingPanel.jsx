import GoogleRouteMap from './GoogleRouteMap';
import { formatClock, formatCountdown, useNow } from '../orders/DeliveryStatus';

function Meter({ label, value, max, unit }) {
  const percent = Math.min(100, (Number(value) / Number(max)) * 100);
  return (
    <div className="grid gap-1">
      <div className="flex justify-between text-xs font-bold uppercase tracking-wide text-ink/60">
        <span>{label}</span>
        <span className="tabular-nums">{Number(value).toFixed(unit === 'lb' ? 1 : 0)} / {Number(max)} {unit}</span>
      </div>
      <div className="h-2 overflow-hidden rounded-full bg-brand-green-50">
        <div className="h-full rounded-full bg-brand-green-500" style={{ width: `${percent}%` }} />
      </div>
    </div>
  );
}

function StopStatus({ stop, now }) {
  if (stop.orderStatus === 'Completed') return <span className="rounded-full bg-brand-green-600 px-3 py-1 text-xs font-bold text-white">delivered</span>;
  if (stop.estimatedArrival) {
    return (
      <span className="rounded-full bg-brand-orange-100 px-3 py-1 text-xs font-bold text-brand-orange-500">
        {stop.estimatedArrival > now ? `in ${formatCountdown(stop.estimatedArrival - now)}` : 'arriving'}
      </span>
    );
  }
  return <span className="rounded-full bg-brand-green-50 px-3 py-1 text-xs font-bold text-brand-green-700">{stop.orderStatus}</span>;
}

function StopsTable({ stops, now }) {
  return (
    <div className="overflow-x-auto">
      <table className="w-full min-w-[620px] text-left text-sm">
        <thead className="bg-brand-green-50 text-xs uppercase tracking-wide text-brand-green-700">
          <tr><th className="px-4 py-2">Stop</th><th className="px-4 py-2">Order</th><th className="px-4 py-2">Customer</th><th className="px-4 py-2">Address</th><th className="px-4 py-2">Weight</th><th className="px-4 py-2">Status</th></tr>
        </thead>
        <tbody>
          {stops.map((stop, index) => (
            <tr key={stop.orderId} className="border-t border-brand-green-100">
              <td className="px-4 py-3"><span className="rounded-full bg-brand-orange-100 px-3 py-1 text-xs font-black text-brand-orange-500">{stop.stopNumber ?? index + 1}</span></td>
              <td className="px-4 py-3 font-bold">#{stop.orderId}</td>
              <td className="px-4 py-3">{stop.customerName}</td>
              <td className="max-w-xs px-4 py-3 text-ink/75">{stop.deliveryAddress}</td>
              <td className="px-4 py-3 tabular-nums">{Number(stop.totalWeight).toFixed(1)} lb</td>
              <td className="px-4 py-3"><StopStatus stop={stop} now={now} /></td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function QueueCard({ queue, now }) {
  if (!queue) {
    return (
      <section className="rounded-2xl border-2 border-dashed border-brand-green-100 bg-white p-6">
        <h2 className="font-display text-xl font-black text-ink">At the store</h2>
        <p className="mt-1 text-sm text-ink/65">No orders waiting. The next paid order starts a new robot batch with a 5-minute timer.</p>
      </section>
    );
  }
  const remaining = queue.dispatchAt - now;
  return (
    <section className="overflow-hidden rounded-2xl border-2 border-brand-orange-400 bg-white">
      <div className="grid gap-4 p-5 sm:grid-cols-[auto_minmax(0,1fr)] sm:items-center">
        <div>
          <p className="text-xs font-bold uppercase tracking-wide text-brand-orange-500">Robot leaves in</p>
          <p className="font-display text-5xl font-black tabular-nums text-ink">{remaining > 0 ? formatCountdown(remaining) : '0:00'}</p>
        </div>
        <div className="grid gap-3">
          <h2 className="font-display text-xl font-black text-ink">At the store: batch #{queue.taskId}</h2>
          <Meter label="Orders" value={queue.orderCount} max={queue.maxOrders} unit="orders" />
          <Meter label="Weight" value={queue.totalWeight} max={queue.maxWeight} unit="lb" />
          <p className="text-xs text-ink/60">Each new order shortens the timer by a minute (5 → 4 → … → 1). The robot leaves right away at {queue.maxOrders} orders or {Number(queue.maxWeight)} lb.</p>
        </div>
      </div>
      <StopsTable stops={queue.orders} now={now} />
    </section>
  );
}

function RobotCard({ robot, now, serverOffsetMs }) {
  const elapsed = now + serverOffsetMs - robot.startTime;
  const progress = robot.durationSeconds ? Math.min(1, Math.max(0, elapsed / (robot.durationSeconds * 1000))) : 0;
  const delivered = robot.stops.filter((stop) => stop.orderStatus === 'Completed').length;
  const finishAt = robot.startTime + (robot.durationSeconds || 0) * 1000 - serverOffsetMs;

  return (
    <section className="grid gap-4 overflow-hidden rounded-2xl border-2 border-brand-green-100 bg-white p-5">
      <div className="flex flex-wrap items-baseline justify-between gap-2">
        <h2 className="font-display text-xl font-black text-ink">Robot #{robot.taskId} · en route</h2>
        <p className="text-sm text-ink/65">
          {delivered} of {robot.stops.length} delivered
          {robot.distanceMeters ? ` · ${(robot.distanceMeters / 1609.34).toFixed(1)} mi` : ''}
          {' · '}last stop {formatClock(finishAt)}
        </p>
      </div>
      <div className="h-3 overflow-hidden rounded-full bg-brand-green-50" role="progressbar" aria-valuenow={Math.round(progress * 100)} aria-valuemin={0} aria-valuemax={100} aria-label={`Robot ${robot.taskId} route progress`}>
        <div className="h-full rounded-full bg-brand-orange-400 transition-[width] duration-1000" style={{ width: `${progress * 100}%` }} />
      </div>
      <GoogleRouteMap
        encodedPolyline={robot.encodedPolyline}
        stops={robot.stops}
        startTime={robot.startTime}
        durationSeconds={robot.durationSeconds}
        serverOffsetMs={serverOffsetMs}
      />
      <div className="-mx-5 -mb-5"><StopsTable stops={robot.stops} now={now + serverOffsetMs} /></div>
    </section>
  );
}

function TrackingPanel({ tracking, loading, error, mapApiKey, onMapApiKeyChange, onRefresh }) {
  const now = useNow();
  const robots = tracking?.robots || [];
  const serverOffsetMs = tracking?.serverOffsetMs || 0;

  return (
    <section className="mt-8 space-y-5">
      {/* Only ask for a key when the server doesn't already have GOOGLE_MAPS_API_KEY. */}
      {tracking && !tracking.serverHasMapsKey && <form
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
          Used to plan each robot&apos;s route when it leaves the store. Not needed if the server has GOOGLE_MAPS_API_KEY set.
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
            save key
          </button>
        </div>
        {mapApiKey && !tracking.routeError && (
          <p className="mt-2 text-sm font-semibold text-brand-green-700" role="status">Key saved. The next robot to leave will get a Google Maps route.</p>
        )}
      </form>}
      {error && <p className="rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-700">{error}</p>}
      {tracking?.routeError && <p className="rounded-xl border border-brand-orange-100 bg-brand-orange-100/50 p-4 text-sm text-ink/70">{tracking.routeError}</p>}
      {loading && !tracking ? <p className="rounded-2xl bg-white p-8 text-center text-ink/65">Loading deliveries…</p> : tracking && (
        <>
          <QueueCard queue={tracking.queue} now={now + serverOffsetMs} />
          {robots.length === 0 ? (
            <section className="rounded-2xl border-2 border-dashed border-brand-green-100 bg-white p-6">
              <h2 className="font-display text-xl font-black text-ink">On the road</h2>
              <p className="mt-1 text-sm text-ink/65">No robots out right now.</p>
            </section>
          ) : robots.map((robot) => (
            <RobotCard key={robot.taskId} robot={robot} now={now} serverOffsetMs={serverOffsetMs} />
          ))}
        </>
      )}
    </section>
  );
}

export default TrackingPanel;
