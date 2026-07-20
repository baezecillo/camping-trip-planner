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
    <div className="page search-screen">
      <div className="card">
        <h1>Plan a Trip</h1>
        <form className="form" onSubmit={handleSubmit}>
          <label className="form-label" htmlFor="from">From</label>
          <input
            id="from"
            className="input"
            value={origin}
            onChange={(event) => setOrigin(event.target.value)}
          />

          <label className="form-label" htmlFor="where">Where</label>
          <input
            id="where"
            className="input"
            value={destination}
            onChange={(event) => setDestination(event.target.value)}
          />

          <label className="form-label" htmlFor="start-date">Start date</label>
          <input
            id="start-date"
            type="date"
            className="input"
            value={startDate}
            onChange={(event) => setStartDate(event.target.value)}
          />

          <label className="form-label" htmlFor="end-date">End date</label>
          <input
            id="end-date"
            type="date"
            className="input"
            value={endDate}
            onChange={(event) => setEndDate(event.target.value)}
          />

          <button type="submit" className="btn">Go</button>
        </form>
        {error && <p role="alert" className="error-text">{error}</p>}
      </div>
    </div>
  );
}
