import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import SearchScreen from './SearchScreen';

describe('SearchScreen', () => {
  beforeEach(() => {
    global.fetch = vi.fn().mockResolvedValue({
      ok: true,
      status: 201,
      json: async () => ({
        id: 1,
        origin: 'Pittsburgh, PA',
        destination: 'Cook Forest State Park, PA',
        startDate: '2026-08-01',
        endDate: '2026-08-03',
        daysUntilStart: 15,
        checklist: [],
      }),
    });
  });

  it('renders the From, Where, start date, end date inputs and a Go button', () => {
    render(<SearchScreen onTripCreated={() => {}} />);

    expect(screen.getByLabelText(/from/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/where/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/start date/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/end date/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /go/i })).toBeInTheDocument();
  });

  it('calls POST /api/trips with the entered values on submit', async () => {
    const user = userEvent.setup();
    const onTripCreated = vi.fn();
    render(<SearchScreen onTripCreated={onTripCreated} />);

    await user.type(screen.getByLabelText(/from/i), 'Pittsburgh, PA');
    await user.type(screen.getByLabelText(/where/i), 'Cook Forest State Park, PA');
    await user.type(screen.getByLabelText(/start date/i), '2026-08-01');
    await user.type(screen.getByLabelText(/end date/i), '2026-08-03');
    await user.click(screen.getByRole('button', { name: /go/i }));

    expect(global.fetch).toHaveBeenCalledWith(
      'http://localhost:8080/api/trips',
      expect.objectContaining({
        method: 'POST',
        credentials: 'include',
        body: JSON.stringify({
          origin: 'Pittsburgh, PA',
          destination: 'Cook Forest State Park, PA',
          startDate: '2026-08-01',
          endDate: '2026-08-03',
        }),
      })
    );
    expect(onTripCreated).toHaveBeenCalled();
  });
});
