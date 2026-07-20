import { useEffect, useState } from 'react';
import { getCurrentTripWeather } from '../api/client';
import { describeWeatherCode } from '../utils/weatherCodes';

export default function WeatherCard({ destination, startDate }) {
  const [status, setStatus] = useState('loading');
  const [weather, setWeather] = useState(null);

  useEffect(() => {
    let cancelled = false;

    async function fetchWeather() {
      setStatus('loading');
      try {
        const data = await getCurrentTripWeather();
        if (cancelled) return;
        if (data && data.available) {
          setWeather(data);
          setStatus('available');
        } else if (data && data.available === false) {
          setStatus('unavailable');
        } else {
          setStatus('error');
        }
      } catch {
        if (!cancelled) {
          setStatus('error');
        }
      }
    }

    fetchWeather();

    return () => {
      cancelled = true;
    };
  }, [destination, startDate]);

  if (status === 'loading') {
    return (
      <div className="card">
        <h2>Weather</h2>
        <p>Loading forecast...</p>
      </div>
    );
  }

  if (status === 'error') {
    return (
      <div className="card">
        <h2>Weather</h2>
        <p>Weather unavailable right now</p>
      </div>
    );
  }

  if (status === 'unavailable') {
    return (
      <div className="card">
        <h2>Weather</h2>
        <p>Forecast available closer to your trip</p>
      </div>
    );
  }

  return (
    <div className="card">
      <h2>Weather</h2>
      <p>{describeWeatherCode(weather.weatherCode)}</p>
      <p>High: {weather.temperatureMaxC}°C</p>
      <p>Low: {weather.temperatureMinC}°C</p>
      <p>Chance of precipitation: {weather.precipitationProbabilityMax}%</p>
    </div>
  );
}
