export default function RouteMap({ origin, destination }) {
  const apiKey = import.meta.env.VITE_GOOGLE_MAPS_KEY;
  const src = `https://www.google.com/maps/embed/v1/directions?key=${apiKey}&origin=${encodeURIComponent(
    origin
  )}&destination=${encodeURIComponent(destination)}&mode=driving`;

  return (
    <iframe
      title="Route Map"
      src={src}
      width="600"
      height="450"
      style={{ border: 0 }}
      loading="lazy"
      allowFullScreen
    />
  );
}
