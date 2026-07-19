import { useState } from 'react';
import { register, login } from '../api/client';

export default function RegisterPage({ onLoggedIn, onShowLogin }) {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState(null);

  async function handleSubmit(event) {
    event.preventDefault();
    setError(null);
    try {
      await register(username, password);
      const user = await login(username, password);
      onLoggedIn(user);
    } catch {
      setError('Registration failed. Try a different username.');
    }
  }

  return (
    <div>
      <h1>Register</h1>
      <form onSubmit={handleSubmit}>
        <label htmlFor="register-username">Username</label>
        <input
          id="register-username"
          value={username}
          onChange={(event) => setUsername(event.target.value)}
        />

        <label htmlFor="register-password">Password</label>
        <input
          id="register-password"
          type="password"
          value={password}
          onChange={(event) => setPassword(event.target.value)}
        />

        <button type="submit">Register</button>
      </form>
      {error && <p role="alert">{error}</p>}
      <button type="button" onClick={onShowLogin}>
        Already have an account? Log in
      </button>
    </div>
  );
}
