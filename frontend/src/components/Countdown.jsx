export default function Countdown({ daysUntilStart }) {
  return (
    <div className="card countdown">
      <p className="countdown-value">{daysUntilStart} days until your trip</p>
    </div>
  );
}
