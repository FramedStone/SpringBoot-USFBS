import { BrowserRouter as Router, Routes, Route, Navigate } from "react-router-dom";
import { useState } from "react";
import Login from "./components/Login";
import AdminDashboard from "./pages/AdminDashboard";
import "./App.css";

function App() {
  const [web3Auth, setWeb3Auth] = useState(null);
  const [user, setUser] = useState(null);

  return (
    <Router>
      <Routes>
        <Route
          path="/login"
          element={
            <Login
              web3Auth={web3Auth}
              setWeb3Auth={setWeb3Auth}
              setUser={setUser}
            />
          }
        />
        <Route path="/" element={<Navigate to="/login" replace />} />
        <Route path="/dashboard" element={<AdminDashboard />} />
      </Routes>
    </Router>
  );
}

export default App;
