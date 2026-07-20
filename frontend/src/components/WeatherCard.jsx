import { useEffect, useState } from 'react';
import { getCurrentTripWeather } from '../api/client';
import { describeWeatherCode, getWeatherIconCategory } from '../utils/weatherCodes';
import WeatherIcon from './WeatherIcon';

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
      <div className="card weather-card">
        <h2>Weather</h2>
        <div className="weather-fallback">
          <WeatherIcon category="unknown" className="weather-icon weather-icon-muted" />
          <p>Loading forecast...</p>
        </div>
      </div>
    );
  }

  if (status === 'error') {
    return (
      <div className="card weather-card">
        <h2>Weather</h2>
        <div className="weather-fallback">
          <WeatherIcon category="unknown" className="weather-icon weather-icon-muted" />
          <p>Weather unavailable right now</p>
        </div>
      </div>
    );
  }

  if (status === 'unavailable') {
    return (
      <div className="card weather-card">
        <h2>Weather</h2>
        <div className="weather-fallback">
          <WeatherIcon category="unknown" className="weather-icon weather-icon-muted" />
          <p>Forecast available closer to your trip</p>
        </div>
      </div>
    );
  }

  const iconCategory = getWeatherIconCategory(weather.weatherCode);
  const precipPct = weather.precipitationProbabilityMax;

  return (
    <div className="card weather-card">
      <h2>Weather</h2>
      <div className="weather-headline">
        <WeatherIcon category={iconCategory} className="weather-icon" />
        <div className="weather-temps">
          <div className="weather-temp weather-temp-max">
            <span className="weather-temp-value">{weather.temperatureMaxC}°C</span>
            <span className="weather-temp-label">High</span>
          </div>
          <div className="weather-temp weather-temp-min">
            <span className="weather-temp-value">{weather.temperatureMinC}°C</span>
            <span className="weather-temp-label">Low</span>
          </div>
        </div>
      </div>
      <p className="weather-condition">{describeWeatherCode(weather.weatherCode)}</p>
      <div className="weather-precip">
        <span className="weather-precip-label">Chance of precipitation</span>
        <div className="weather-precip-bar">
          <div className="weather-precip-bar-fill" style={{ width: `${precipPct}%` }} />
        </div>
        <span className="weather-precip-value">{precipPct}%</span>
      </div>
    </div>
  );
}
