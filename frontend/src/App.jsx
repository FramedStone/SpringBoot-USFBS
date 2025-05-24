import { useState } from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import Navbar from "./components/Navbar";
import Home from "./pages/Home";
import Login from "./components/Login";
import Dashboard from "./pages/Dashboard";
// import BookingHistory from "./pages/BookingHistory";
// import AdminPanel from "./pages/AdminPanel";
// import ModeratorPanel from "./pages/ModeratorPanel";
// import NotFound from "./pages/NotFound";
import "./App.css";

function App() {
  const [web3auth, setWeb3auth] = useState(null);
  const [user,      setUser ]   = useState(null);

  return (
    <Router>
      <Navbar web3auth={web3auth} setUser={setUser} />
      <Routes>
        <Route
          path="/login"
          element={
            <Login
              web3auth={web3auth}
              setWeb3auth={setWeb3auth}
              setUser={setUser}
            />
          }
        />
        <Route path="/" element={<Home />} />
        <Route path="/dashboard" element={<Dashboard />} />
        {/*
        <Route path="/history"   element={<BookingHistory />} />
        <Route path="/admin"     element={<AdminPanel />}     />
        <Route path="/moderator" element={<ModeratorPanel />} />
        <Route path="*"          element={<NotFound />}       />
        */}
      </Routes>
    </Router>
  );
}

export default App;
