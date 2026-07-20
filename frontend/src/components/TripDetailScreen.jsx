import WrapUpButton from './WrapUpButton';
import RouteMap from './RouteMap';
import Countdown from './Countdown';
import WeatherCard from './WeatherCard';
import Checklist from './Checklist';

export default function TripDetailScreen({ trip, onTripEnded, onItemUpdated }) {
  return (
    <div>
      <WrapUpButton onTripEnded={onTripEnded} />
      <RouteMap origin={trip.origin} destination={trip.destination} />
      <Countdown daysUntilStart={trip.daysUntilStart} />
      <WeatherCard destination={trip.destination} startDate={trip.startDate} />
      <Checklist items={trip.checklist} onItemUpdated={onItemUpdated} />
    </div>
  );
}
