import { get } from './apiClient';

export function getTracking(mapApiKey = '') {
  const trimmedApiKey = mapApiKey.trim();

  return get('/manager/tracking', trimmedApiKey
    ? { headers: { 'X-Google-Maps-Api-Key': trimmedApiKey } }
    : undefined);
}
