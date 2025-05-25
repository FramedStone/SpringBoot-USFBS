import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  useWeb3Auth,
  useWeb3AuthConnect,
  useWeb3AuthDisconnect
} from "@web3auth/modal/react";
import { Web3Provider } from "@ethersproject/providers";
import Toast from "@components/Toast";
import "@styles/login.css";
import { AUTH_CONNECTION, WALLET_CONNECTORS } from "@web3auth/modal";

const SCHOOL_EMAIL_REGEX = /@(student\.)?mmu\.edu\.my$/i;

function Login({ setUser }) {
  const navigate = useNavigate();
  const { web3Auth } = useWeb3Auth();
  const { connectTo, loading: connectLoading, isConnected, error } = useWeb3AuthConnect();
  const { disconnect } = useWeb3AuthDisconnect();
  const [localUser, setLocalUser] = useState(null);
  const [toast, setToast] = useState({ msg: "", type: "error" });
  const [email, setEmail] = useState(""); 

  useEffect(() => {
    (async () => {
      try {
        const info = await web3Auth.getUserInfo();
        if (info) {
          setLocalUser(info);
          setUser(info);
        }
      } catch (err) {
        console.error("Init getUserInfo error:", err);
      }
    })();
  }, [web3Auth, setUser]);

  const handleLogin = async (e) => {
    e.preventDefault();
    setToast({ msg: "", type: "error" });
    if (!SCHOOL_EMAIL_REGEX.test(email)) {
      setToast({ msg: "Please use your MMU email address.", type: "error" });
      return;
    }
    try {
      const provider = await connectTo(WALLET_CONNECTORS.AUTH, {
        authConnection: AUTH_CONNECTION.EMAIL_PASSWORDLESS,
        authConnectionId: "usfbs",
        extraLoginOptions: {
          login_hint: email,
        },
      });
      if (!provider) throw new Error("No wallet provider");

      const info = await web3Auth.getUserInfo();
      if (!SCHOOL_EMAIL_REGEX.test(info.email)) {
        await web3Auth.logout();
        await disconnect();
        setLocalUser(null);
        setUser(null);
        setToast({ msg: "Invalid school email", type: "error" });
        setTimeout(() => {
          window.location.reload();
        }, 2000);
        return;
      }

      // SpringBoot verify JWT tokens
      const res = await fetch(`${import.meta.env.VITE_BACKEND_URL}/api/auth/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email: info.email })
      });
      if (!res.ok) {
        throw new Error("Backend login failed");
      }
      const { accessToken, refreshToken, role } = await res.json();

      // Store tokens and role in localStorage
      localStorage.setItem("accessToken", accessToken);
      localStorage.setItem("refreshToken", refreshToken);
      localStorage.setItem("role", role);

      setLocalUser(info);
      setUser(info);
      setToast({ msg: "Login successful!", type: "success" });
      navigate("/dashboard");
    } catch (err) {
      console.error("Web3Auth login error:", err);
      setToast({ msg: "Login error: " + err.message, type: "error" });
    }
  };

  const handleLogout = async () => {
    try {
      await web3Auth.logout();
      await disconnect();
      setLocalUser(null);
      setUser(null);
      // 🦙 camelCase: Clear tokens on logout
      localStorage.removeItem("accessToken");
      localStorage.removeItem("refreshToken");
      localStorage.removeItem("role");
      setToast({ msg: "Logged out successfully.", type: "success" });
    } catch (err) {
      console.error("Logout error:", err);
      setToast({ msg: "Logout error: " + err.message, type: "error" });
    }
  };

  return (
    <div className="login-outer">
      <div className="login-container">
        <div className="login-box">
          <img src="/MMU Logo.png" alt="MMU Logo" className="logo" />
          <div className="header">
            <h1>Login</h1>
            <p>Welcome to the MMU Sport Facility Booking System</p>
          </div>
          {!isConnected ? (
            <form onSubmit={handleLogin} autoComplete="on">
              <div className="form-group" style={{ display: "flex", flexDirection: "column", alignItems: "center" }}>
                <input
                  type="email"
                  id="email"
                  required
                  placeholder="Please enter your MMU email"
                  value={email}
                  onChange={e => setEmail(e.target.value)}
                  className="form-input"
                  autoComplete="on"
                />
              </div>
              <button
                type="submit"
                className="btn"
                disabled={connectLoading || isConnected}
              >
                {connectLoading
                  ? "Connecting…"
                  : isConnected
                  ? "Connected"
                  : "Sign In with MMU Email"}
              </button>
            </form>
          ) : (
            <button
              className="btn"
              style={{ width: "100%", maxWidth: 350, margin: "0 auto", display: "block" }}
              onClick={handleLogout}
            >
              Logout
            </button>
          )}
          {localUser && (
            <p className="welcome-msg">
              Welcome, {localUser.name || localUser.email}
            </p>
          )}
          {error && <div style={{ color: "#a00" }}>{error.message}</div>}
          <Toast
            message={toast.msg}
            type={toast.type}
            onClose={() => setToast({ msg: "", type: "error" })}
          />
        </div>
      </div>
    </div>
  );
}

export default Login;