import { useEffect, useState } from 'react';
import { getCurrentTrip, ApiError } from '../api/client';
import SearchScreen from './SearchScreen';
import TripDetailScreen from './TripDetailScreen';

export default function TripPlanner() {
  const [trip, setTrip] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;
    getCurrentTrip()
      .then((currentTrip) => {
        if (!cancelled) setTrip(currentTrip);
      })
      .catch((error) => {
        if (cancelled) return;
        if (error instanceof ApiError && error.status === 404) {
          setTrip(null);
        }
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, []);

  function handleItemUpdated(updatedItem) {
    setTrip((currentTrip) => ({
      ...currentTrip,
      checklist: currentTrip.checklist.map((item) =>
        item.id === updatedItem.id ? updatedItem : item
      ),
    }));
  }

  if (loading) {
    return <p>Loading...</p>;
  }

  if (!trip) {
    return <SearchScreen onTripCreated={setTrip} />;
  }

  return (
    <TripDetailScreen
      trip={trip}
      onTripEnded={() => setTrip(null)}
      onItemUpdated={handleItemUpdated}
    />
  );
}
