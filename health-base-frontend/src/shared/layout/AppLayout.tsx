import { NavLink, Outlet } from 'react-router-dom'

const navItems = [
  { to: '/users', label: 'User & Role' },
  { to: '/clinics', label: 'Clinic' },
  { to: '/medications', label: 'Medication' },
  { to: '/diagnostics', label: 'Diagnostic Test' },
]

export function AppLayout() {
  return (
    <div className="app-layout">
      <header className="app-header">
        <h1>Sole Health Base</h1>
        <nav>
          {navItems.map((item) => (
            <NavLink key={item.to} to={item.to}>
              {item.label}
            </NavLink>
          ))}
        </nav>
      </header>
      <main className="app-content">
        <Outlet />
      </main>
    </div>
  )
}
