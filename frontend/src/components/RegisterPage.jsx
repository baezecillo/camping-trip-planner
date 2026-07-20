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
    <div className="page">
      <div className="card">
        <h1>Register</h1>
        <form className="form" onSubmit={handleSubmit}>
          <label className="form-label" htmlFor="register-username">Username</label>
          <input
            id="register-username"
            className="input"
            value={username}
            onChange={(event) => setUsername(event.target.value)}
          />

          <label className="form-label" htmlFor="register-password">Password</label>
          <input
            id="register-password"
            type="password"
            className="input"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
          />

          <button type="submit" className="btn">Register</button>
        </form>
        {error && <p role="alert" className="error-text">{error}</p>}
        <button type="button" className="btn-link" onClick={onShowLogin}>
          Already have an account? Log in
        </button>
      </div>
    </div>
  );
}
