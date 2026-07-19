import { useState } from 'react';
import LoginPage from './components/LoginPage';
import RegisterPage from './components/RegisterPage';
import TripPlanner from './components/TripPlanner';

export default function App() {
  const [user, setUser] = useState(null);
  const [view, setView] = useState('login');

  if (!user) {
    if (view === 'register') {
      return (
        <RegisterPage
          onLoggedIn={setUser}
          onShowLogin={() => setView('login')}
        />
      );
    }
    return (
      <LoginPage onLoggedIn={setUser} onShowRegister={() => setView('register')} />
    );
  }

  return <TripPlanner />;
}
