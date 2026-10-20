import { get } from './apiClient';

export function getTracking() {
  return get('/manager/tracking');
}
