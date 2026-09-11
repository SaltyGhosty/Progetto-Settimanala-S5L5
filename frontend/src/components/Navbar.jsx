import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function Navbar() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  function handleLogout() {
    logout()
    navigate('/login')
  }

  return (
    <nav className="navbar">
      <Link to="/" className="navbar-brand">SocialApp</Link>
      {user && (
        <div className="navbar-links">
          <Link to="/">Feed</Link>
          <Link to="/new">Nuovo post</Link>
          <Link to="/profile">Profilo</Link>
          <span className="navbar-user">{user.username}</span>
          <button onClick={handleLogout}>Esci</button>
        </div>
      )}
    </nav>
  )
}
