import { useState } from 'react'
import { session } from './api'
import { LoginForm } from './LoginForm'
import { Panel } from './Panel'

export default function App() {
  const [loggedIn, setLoggedIn] = useState(() => session.get() !== null)

  function logout() {
    session.clear()
    setLoggedIn(false)
  }

  return loggedIn ? (
    <Panel onLogout={logout} />
  ) : (
    <main className="center">
      <LoginForm onLoggedIn={() => setLoggedIn(true)} />
    </main>
  )
}
