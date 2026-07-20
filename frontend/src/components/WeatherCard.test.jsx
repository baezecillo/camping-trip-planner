import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import WeatherCard from './WeatherCard';

describe('WeatherCard', () => {
  beforeEach(() => {
    global.fetch = vi.fn();
  });

  it('renders forecast data when the response is available', async () => {
    global.fetch.mockResolvedValue({
      ok: true,
      status: 200,
      json: async () => ({
        available: true,
        date: '2026-08-01',
        weatherCode: 3,
        temperatureMaxC: 24.5,
        temperatureMinC: 15.2,
        precipitationProbabilityMax: 20,
      }),
    });

    render(<WeatherCard destination="Cook Forest State Park, PA" startDate="2026-08-01" />);

    expect(await screen.findByText('Cloudy')).toBeInTheDocument();
    expect(screen.getByText(/24.5/)).toBeInTheDocument();
    expect(screen.getByText(/15.2/)).toBeInTheDocument();
    expect(screen.getByText(/20%/)).toBeInTheDocument();
  });

  it('renders a friendly message when the forecast is not available', async () => {
    global.fetch.mockResolvedValue({
      ok: true,
      status: 200,
      json: async () => ({ available: false }),
    });

    render(<WeatherCard destination="Cook Forest State Park, PA" startDate="2026-08-01" />);

    expect(await screen.findByText(/forecast available closer to your trip/i)).toBeInTheDocument();
  });

  it('renders a quiet fallback when the fetch fails outright', async () => {
    global.fetch.mockRejectedValue(new Error('network error'));

    render(<WeatherCard destination="Cook Forest State Park, PA" startDate="2026-08-01" />);

    expect(await screen.findByText(/weather unavailable right now/i)).toBeInTheDocument();
  });

  it('renders a quiet fallback on a non-200 response', async () => {
    global.fetch.mockResolvedValue({
      ok: false,
      status: 500,
      json: async () => ({}),
    });

    render(<WeatherCard destination="Cook Forest State Park, PA" startDate="2026-08-01" />);

    expect(await screen.findByText(/weather unavailable right now/i)).toBeInTheDocument();
  });
});
