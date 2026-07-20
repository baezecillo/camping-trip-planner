import { deleteCurrentTrip } from '../api/client';

export default function WrapUpButton({ onTripEnded }) {
  async function handleClick() {
    await deleteCurrentTrip();
    onTripEnded();
  }

  return (
    <button type="button" className="btn-outline-danger" onClick={handleClick}>
      Wrap Up
    </button>
  );
}
