import { useEffect, useRef, useState } from 'react';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';

function decodePolyline(encoded) {
  const points = [];
  let index = 0;
  let latitude = 0;
  let longitude = 0;
  while (index < encoded.length) {
    let shift = 0;
    let result = 0;
    let byte;
    do { byte = encoded.charCodeAt(index++) - 63; result |= (byte & 0x1f) << shift; shift += 5; } while (byte >= 0x20);
    latitude += (result & 1) ? ~(result >> 1) : result >> 1;
    shift = 0;
    result = 0;
    do { byte = encoded.charCodeAt(index++) - 63; result |= (byte & 0x1f) << shift; shift += 5; } while (byte >= 0x20);
    longitude += (result & 1) ? ~(result >> 1) : result >> 1;
    points.push([latitude / 100000, longitude / 100000]);
  }
  return points;
}

function getPathDistances(path) {
  const distances = [0];
  for (let index = 1; index < path.length; index += 1) {
    const previous = path[index - 1];
    const current = path[index];
    const distance = Math.hypot(current[0] - previous[0], current[1] - previous[1]);
    distances.push(distances[index - 1] + distance);
  }
  return distances;
}

function interpolatePath(path, distances, progress) {
  if (path.length === 1) return path[0];
  const targetDistance = progress * distances[distances.length - 1];
  let index = 1;
  while (index < distances.length - 1 && distances[index] < targetDistance) index += 1;
  const segmentDistance = distances[index] - distances[index - 1] || 1;
  const fraction = (targetDistance - distances[index - 1]) / segmentDistance;
  const start = path[index];
  const end = path[index - 1];
  return [
    end[0] + (start[0] - end[0]) * fraction,
    end[1] + (start[1] - end[1]) * fraction,
  ];
}

function getSegmentIndex(distances, progress) {
  const targetDistance = progress * distances[distances.length - 1];
  let index = 1;
  while (index < distances.length - 1 && distances[index] < targetDistance) index += 1;
  return index;
}

const deliveryIcon = L.divIcon({
  className: '',
  html: '<span style="display:grid;place-items:center;width:42px;height:42px;border-radius:50%;background:#397522;border:3px solid white;box-shadow:0 2px 8px #555;font-size:22px">🚚</span>',
  iconSize: [42, 42],
  iconAnchor: [21, 21],
});

function stopIcon(label, delivered) {
  const background = delivered ? '#397522' : '#f5a623';
  const color = delivered ? 'white' : '#20301d';
  return L.divIcon({
    className: '',
    html: `<span style="display:grid;place-items:center;width:28px;height:28px;border-radius:50%;background:${background};border:3px solid white;box-shadow:0 1px 5px #555;color:${color};font-weight:800">${label}</span>`,
    iconSize: [28, 28],
    iconAnchor: [14, 14],
  });
}

/**
 * Draws a robot's route and moves the truck by real elapsed time:
 * progress = (now - startTime) / durationSeconds, using the server clock (serverOffsetMs).
 */
function GoogleRouteMap({ encodedPolyline, stops = [], startTime, durationSeconds, serverOffsetMs = 0 }) {
  const mapElement = useRef(null);
  const stopLayer = useRef(null);
  const timing = useRef({ startTime, durationSeconds, serverOffsetMs });
  const [error, setError] = useState('');
  timing.current = { startTime, durationSeconds, serverOffsetMs };

  useEffect(() => {
    if (!encodedPolyline || !mapElement.current) return undefined;
    const path = decodePolyline(encodedPolyline);
    if (!path.length) { setError('Google returned an empty route polyline.'); return undefined; }

    const map = L.map(mapElement.current, { zoomControl: true });
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; OpenStreetMap contributors',
      className: 'dark-map-tiles',
      maxZoom: 19,
    }).addTo(map);
    map.fitBounds(L.latLngBounds(path), { padding: [24, 24] });

    L.polyline(path, { color: '#397522', weight: 5, opacity: 0.2, dashArray: '8 10' }).addTo(map);
    const travelledLine = L.polyline([], { color: '#8fbe82', weight: 5, opacity: 1 }).addTo(map);
    const deliveryMarker = L.marker(path[0], { icon: deliveryIcon, zIndexOffset: 1000 }).addTo(map);
    const distances = getPathDistances(path);
    stopLayer.current = L.layerGroup().addTo(map);
    let animationFrame;

    const animateDelivery = () => {
      const { startTime: start, durationSeconds: duration, serverOffsetMs: offset } = timing.current;
      const elapsed = Date.now() + offset - start;
      const progress = duration ? Math.min(1, Math.max(0, elapsed / (duration * 1000))) : 0;
      const currentPoint = interpolatePath(path, distances, progress);
      deliveryMarker.setLatLng(currentPoint);
      travelledLine.setLatLngs([...path.slice(0, getSegmentIndex(distances, progress)), currentPoint]);
      animationFrame = requestAnimationFrame(animateDelivery);
    };
    animationFrame = requestAnimationFrame(animateDelivery);

    L.circleMarker(path[0], { radius: 8, color: '#e8890c', fillColor: '#f5a623', fillOpacity: 1 })
      .addTo(map)
      .bindTooltip('OFS store', { permanent: true, direction: 'top', offset: [0, -8] });

    return () => {
      window.cancelAnimationFrame(animationFrame);
      stopLayer.current = null;
      map.remove();
    };
  }, [encodedPolyline]);

  // Redraw stop pins on each poll so delivered stops turn green.
  useEffect(() => {
    const layer = stopLayer.current;
    if (!layer) return;
    layer.clearLayers();
    stops.filter((stop) => stop.latitude != null).forEach((stop) => {
      L.marker([stop.latitude, stop.longitude], { icon: stopIcon(stop.stopNumber, stop.orderStatus === 'Completed') })
        .addTo(layer)
        .bindTooltip(`Stop ${stop.stopNumber}: order #${stop.orderId}, ${stop.customerName}`, { direction: 'top', offset: [0, -12] });
    });
  }, [stops, encodedPolyline]);

  if (error) return <p className="mt-4 rounded-xl border border-brand-orange-100 bg-brand-orange-100/50 p-4 text-sm text-ink/70">{error}</p>;
  if (!encodedPolyline) return null;
  return (
    <div className="relative">
      <div ref={mapElement} className="h-80 w-full overflow-hidden rounded-xl border-2 border-brand-green-100" aria-label="Robot route map" />
      <span className="absolute right-3 top-3 z-[400] rounded-full bg-white/90 px-3 py-1 text-xs font-bold text-brand-green-700 shadow-sm">
        live robot position
      </span>
    </div>
  );
}

export default GoogleRouteMap;
