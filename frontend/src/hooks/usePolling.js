import { useEffect, useRef } from 'react';

/**
 * Calls callback right away and then every intervalMs while the tab is visible.
 * Used for live order/robot updates instead of websockets.
 */
export default function usePolling(callback, intervalMs, enabled = true) {
  const savedCallback = useRef(callback);
  savedCallback.current = callback;

  useEffect(() => {
    if (!enabled) return undefined;
    let timer;
    const run = () => {
      if (document.visibilityState === 'visible') savedCallback.current();
    };
    savedCallback.current();
    timer = setInterval(run, intervalMs);
    document.addEventListener('visibilitychange', run);
    return () => {
      clearInterval(timer);
      document.removeEventListener('visibilitychange', run);
    };
  }, [intervalMs, enabled]);
}
