import { Navigate, Route, Routes } from 'react-router-dom'
import { AppLayout } from './shared/layout/AppLayout'
import { UserPage } from './modules/user/pages/UserPage'
import { ClinicPage } from './modules/clinic/pages/ClinicPage'
import { MedicationPage } from './modules/medication/pages/MedicationPage'
import { DiagnosticPage } from './modules/diagnostic/pages/DiagnosticPage'

function App() {
  return (
    <Routes>
      <Route element={<AppLayout />}>
        <Route index element={<Navigate to="/users" replace />} />
        <Route path="/users" element={<UserPage />} />
        <Route path="/clinics" element={<ClinicPage />} />
        <Route path="/medications" element={<MedicationPage />} />
        <Route path="/diagnostics" element={<DiagnosticPage />} />
      </Route>
    </Routes>
  )
}

export default App
