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

const deliveryIcon = L.divIcon({
  className: '',
  html: '<span style="display:grid;place-items:center;width:42px;height:42px;border-radius:50%;background:#397522;border:3px solid white;box-shadow:0 2px 8px #555;font-size:22px">🚚</span>',
  iconSize: [42, 42],
  iconAnchor: [21, 21],
});

function stopIcon(label) {
  return L.divIcon({
    className: '',
    html: `<span style="display:grid;place-items:center;width:28px;height:28px;border-radius:50%;background:#f5a623;border:3px solid white;box-shadow:0 1px 5px #555;color:#20301d;font-weight:800">${label}</span>`,
    iconSize: [28, 28],
    iconAnchor: [14, 14],
  });
}

function GoogleRouteMap({ encodedPolyline, stopLocations = [] }) {
  const mapElement = useRef(null);
  const [error, setError] = useState('');

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
    const bounds = L.latLngBounds(path);
    map.fitBounds(bounds, { padding: [24, 24] });

    const animatedLine = L.polyline([], { color: '#8fbe82', weight: 3, opacity: 0.9 }).addTo(map);
    const deliveryMarker = L.marker(path[0], { icon: deliveryIcon, zIndexOffset: 1000 }).addTo(map);
    const distances = getPathDistances(path);
    let pointIndex = 0;
    const animationDuration = 14000;
    const animationStart = performance.now();
    let animationFrame;

    const animateDelivery = (timestamp) => {
      const progress = ((timestamp - animationStart) % animationDuration) / animationDuration;
      deliveryMarker.setLatLng(interpolatePath(path, distances, progress));
      animationFrame = requestAnimationFrame(animateDelivery);
    };
    animationFrame = requestAnimationFrame(animateDelivery);

    const timer = window.setInterval(() => {
      animatedLine.addLatLng(path[pointIndex]);
      pointIndex += 1;
      if (pointIndex >= path.length) window.clearInterval(timer);
    }, 18);

    L.circleMarker(path[0], { radius: 8, color: '#e8890c', fillColor: '#f5a623', fillOpacity: 1 }).addTo(map).bindTooltip('Pickup');
    stopLocations.forEach((stop, index) => {
      L.marker([stop.latitude, stop.longitude], { icon: stopIcon(index + 1) }).addTo(map).bindTooltip(`Stop ${index + 1}`);
    });

    return () => {
      window.clearInterval(timer);
      window.cancelAnimationFrame(animationFrame);
      map.remove();
    };
  }, [encodedPolyline, stopLocations]);

  if (error) return <p className="mt-4 rounded-xl border border-brand-orange-100 bg-brand-orange-100/50 p-4 text-sm text-ink/70">{error}</p>;
  if (!encodedPolyline) return null;
  return <div ref={mapElement} className="mt-5 h-80 w-full overflow-hidden rounded-xl border-2 border-brand-green-100" aria-label="Animated OpenStreetMap route map" />;
}

export default GoogleRouteMap;
