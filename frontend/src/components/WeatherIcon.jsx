const ICONS = {
  clear: (
    <>
      <circle cx="12" cy="12" r="5" />
      <path d="M12 1.5v3M12 19.5v3M4.2 4.2l2.1 2.1M17.7 17.7l2.1 2.1M1.5 12h3M19.5 12h3M4.2 19.8l2.1-2.1M17.7 6.3l2.1-2.1" />
    </>
  ),
  cloudy: (
    <path d="M7 18a4.5 4.5 0 0 1-.5-8.97A5.5 5.5 0 0 1 17.2 8.1 4 4 0 0 1 17 16v0" fill="none" style={{ strokeLinejoin: 'round' }} />
  ),
  fog: (
    <>
      <path d="M6.5 10.5a4.5 4.5 0 0 1 8.9-1 4 4 0 0 1 1.1 7.9" fill="none" />
      <path d="M3 17h18M5 20h14" />
    </>
  ),
  rain: (
    <>
      <path d="M7 13a4.5 4.5 0 0 1-.5-8.97A5.5 5.5 0 0 1 17.2 5.1 4 4 0 0 1 17 11" fill="none" />
      <path d="M8 16.5l-1 3M12 16.5l-1 3M16 16.5l-1 3" />
    </>
  ),
  snow: (
    <>
      <path d="M7 13a4.5 4.5 0 0 1-.5-8.97A5.5 5.5 0 0 1 17.2 5.1 4 4 0 0 1 17 11" fill="none" />
      <path d="M8 16.5v4M6 17.5l4 2M10 17.5l-4 2M16 16.5v4M14 17.5l4 2M18 17.5l-4 2" />
    </>
  ),
  thunderstorm: (
    <>
      <path d="M7 12.5a4.5 4.5 0 0 1-.5-8.97A5.5 5.5 0 0 1 16.7 4.6 4 4 0 0 1 16.5 10.5" fill="none" />
      <path d="M13 12l-3 5h3l-2 5 5-6.5h-3l2-3.5z" fill="currentColor" stroke="none" />
    </>
  ),
  unknown: (
    <>
      <circle cx="12" cy="12" r="9" fill="none" />
      <path d="M9.5 9a2.5 2.5 0 0 1 4.9.7c0 1.7-2.4 2-2.4 3.6" />
      <circle cx="12" cy="17" r="0.1" />
    </>
  ),
};

export default function WeatherIcon({ category, className }) {
  const paths = ICONS[category] ?? ICONS.unknown;
  return (
    <svg
      className={className}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.5"
      strokeLinecap="round"
      aria-hidden="true"
    >
      {paths}
    </svg>
  );
}
