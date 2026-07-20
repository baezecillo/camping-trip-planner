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
    <div className="page">
      <div className="card">
        <h1>Log In</h1>
        <form className="form" onSubmit={handleSubmit}>
          <label className="form-label" htmlFor="login-username">Username</label>
          <input
            id="login-username"
            className="input"
            value={username}
            onChange={(event) => setUsername(event.target.value)}
          />

          <label className="form-label" htmlFor="login-password">Password</label>
          <input
            id="login-password"
            type="password"
            className="input"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
          />

          <button type="submit" className="btn">Log In</button>
        </form>
        {error && <p role="alert" className="error-text">{error}</p>}
        <button type="button" className="btn-link" onClick={onShowRegister}>
          Need an account? Register
        </button>
      </div>
    </div>
  );
}
