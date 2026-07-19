import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import TripDetailScreen from './TripDetailScreen';

const trip = {
  id: 1,
  origin: 'Pittsburgh, PA',
  destination: 'Cook Forest State Park, PA',
  startDate: '2026-08-01',
  endDate: '2026-08-03',
  daysUntilStart: 15,
  checklist: [
    { id: 1, itemName: 'Tent', category: 'Shelter & Sleeping', isPacked: false },
    { id: 2, itemName: 'Sleeping bag', category: 'Shelter & Sleeping', isPacked: false },
    { id: 3, itemName: 'Camp stove', category: 'Cooking & Food', isPacked: false },
  ],
};

describe('TripDetailScreen', () => {
  beforeEach(() => {
    global.fetch = vi.fn();
  });

  it('renders the countdown value and all checklist items grouped by category', () => {
    render(<TripDetailScreen trip={trip} onTripEnded={() => {}} onItemUpdated={() => {}} />);

    expect(screen.getByText(/15 days until your trip/i)).toBeInTheDocument();

    expect(screen.getByRole('heading', { name: 'Shelter & Sleeping' })).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: 'Cooking & Food' })).toBeInTheDocument();

    expect(screen.getByLabelText('Tent')).toBeInTheDocument();
    expect(screen.getByLabelText('Sleeping bag')).toBeInTheDocument();
    expect(screen.getByLabelText('Camp stove')).toBeInTheDocument();
  });

  it('calls PATCH /api/checklist/{id} with the new value when a checkbox is clicked', async () => {
    global.fetch.mockResolvedValue({
      ok: true,
      status: 200,
      json: async () => ({ id: 1, itemName: 'Tent', category: 'Shelter & Sleeping', isPacked: true }),
    });
    const user = userEvent.setup();
    render(<TripDetailScreen trip={trip} onTripEnded={() => {}} onItemUpdated={() => {}} />);

    await user.click(screen.getByLabelText('Tent'));

    expect(global.fetch).toHaveBeenCalledWith(
      'http://localhost:8080/api/checklist/1',
      expect.objectContaining({
        method: 'PATCH',
        credentials: 'include',
        body: JSON.stringify({ isPacked: true }),
      })
    );
  });

  it('calls DELETE /api/trips/current when Wrap Up is clicked', async () => {
    global.fetch.mockResolvedValue({ ok: true, status: 204, json: async () => null });
    const user = userEvent.setup();
    const onTripEnded = vi.fn();
    render(<TripDetailScreen trip={trip} onTripEnded={onTripEnded} onItemUpdated={() => {}} />);

    await user.click(screen.getByRole('button', { name: /wrap up/i }));

    expect(global.fetch).toHaveBeenCalledWith(
      'http://localhost:8080/api/trips/current',
      expect.objectContaining({ method: 'DELETE', credentials: 'include' })
    );
    expect(onTripEnded).toHaveBeenCalled();
  });
});
