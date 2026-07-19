import { useState } from 'react';
import { createTrip } from '../api/client';

export default function SearchScreen({ onTripCreated }) {
  const [origin, setOrigin] = useState('');
  const [destination, setDestination] = useState('');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [error, setError] = useState(null);

  async function handleSubmit(event) {
    event.preventDefault();
    setError(null);
    try {
      const trip = await createTrip({
        origin,
        destination,
        startDate,
        endDate,
      });
      onTripCreated(trip);
    } catch {
      setError('Could not create trip. Please try again.');
    }
  }

  return (
    <div>
      <h1>Plan a Trip</h1>
      <form onSubmit={handleSubmit}>
        <label htmlFor="from">From</label>
        <input
          id="from"
          value={origin}
          onChange={(event) => setOrigin(event.target.value)}
        />

        <label htmlFor="where">Where</label>
        <input
          id="where"
          value={destination}
          onChange={(event) => setDestination(event.target.value)}
        />

        <label htmlFor="start-date">Start date</label>
        <input
          id="start-date"
          type="date"
          value={startDate}
          onChange={(event) => setStartDate(event.target.value)}
        />

        <label htmlFor="end-date">End date</label>
        <input
          id="end-date"
          type="date"
          value={endDate}
          onChange={(event) => setEndDate(event.target.value)}
        />

        <button type="submit">Go</button>
      </form>
      {error && <p role="alert">{error}</p>}
    </div>
  );
}
