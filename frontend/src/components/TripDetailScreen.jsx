import WrapUpButton from './WrapUpButton';
import RouteMap from './RouteMap';
import Countdown from './Countdown';
import WeatherCard from './WeatherCard';
import Checklist from './Checklist';

export default function TripDetailScreen({ trip, onTripEnded, onItemUpdated }) {
  return (
    <div className="page">
      <div className="page-header">
        <h1>Your Trip</h1>
        <WrapUpButton onTripEnded={onTripEnded} />
      </div>
      <RouteMap origin={trip.origin} destination={trip.destination} />
      <Countdown daysUntilStart={trip.daysUntilStart} />
      <WeatherCard destination={trip.destination} startDate={trip.startDate} />
      <Checklist items={trip.checklist} onItemUpdated={onItemUpdated} />
    </div>
  );
}
