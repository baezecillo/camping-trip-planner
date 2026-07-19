import { useState } from 'react';
import { login } from '../api/client';

export default function LoginPage({ onLoggedIn, onShowRegister }) {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState(null);

  async function handleSubmit(event) {
    event.preventDefault();
    setError(null);
    try {
      const user = await login(username, password);
      onLoggedIn(user);
    } catch {
      setError('Invalid username or password.');
    }
  }

  return (
    <div>
      <h1>Log In</h1>
      <form onSubmit={handleSubmit}>
        <label htmlFor="login-username">Username</label>
        <input
          id="login-username"
          value={username}
          onChange={(event) => setUsername(event.target.value)}
        />

        <label htmlFor="login-password">Password</label>
        <input
          id="login-password"
          type="password"
          value={password}
          onChange={(event) => setPassword(event.target.value)}
        />

        <button type="submit">Log In</button>
      </form>
      {error && <p role="alert">{error}</p>}
      <button type="button" onClick={onShowRegister}>
        Need an account? Register
      </button>
    </div>
  );
}
