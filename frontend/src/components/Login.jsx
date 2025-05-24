import { useEffect, useState }        from "react";
import { useNavigate }                 from "react-router-dom";
import { useWeb3Auth, useWeb3AuthConnect } from "@web3auth/modal/react";
import { Web3Provider }                from "@ethersproject/providers";
import Toast                           from "./Toast";
import "../styles/login.css";

const SCHOOL_EMAIL_REGEX = /@(student\.)?mmu\.edu\.my$/i;

function Login({ setUser }) {
  const { web3Auth, initialized } = useWeb3Auth();
  const { connect, loading: connectLoading, isConnected, error } = useWeb3AuthConnect();
  const [localUser, setLocalUser] = useState(null);
  const [toast, setToast]         = useState({ msg: "", type: "error" });
  const navigate                  = useNavigate();

  useEffect(() => {
    if (!initialized) return;
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
  }, [initialized, web3Auth, setUser]);

  const handleLogin = async () => {
    setToast({ msg: "", type: "error" });
    try {
      const provider = await connect();
      if (!provider) throw new Error("No wallet provider");

      const info = await web3Auth.getUserInfo();
      if (!SCHOOL_EMAIL_REGEX.test(info.email)) {
        await web3Auth.logout();
        setToast({ msg: "Invalid school email", type: "error" });
        return;
      }

      const ethProvider = new Web3Provider(provider);
      const address     = await ethProvider.getSigner().getAddress();
      console.log("🔑 Connected address:", address);

      const rawKey = await provider.request({ method: "private_key" });
      console.log("🔐 Private key:", rawKey);

      setLocalUser(info);
      setUser(info);
      setToast({ msg: "Login successful!", type: "success" });
      navigate("/dashboard");
    } catch (err) {
      console.error("Web3Auth login error:", err);
      setToast({ msg: "Login error: " + err.message, type: "error" });
    }
  };

  return (
    <div className="login-container">
      <div className="login-box">
        <img src="/MMU Logo.png" alt="MMU Logo" className="logo" />
        <h1 className="login-title">Login</h1>

        {(!localUser) && (
          <button
            className="btn"
            onClick={handleLogin}
            disabled={connectLoading || isConnected}
          >
            {connectLoading
              ? "Connecting…"
              : isConnected
              ? "Connected"
              : "Sign In with MMU Email"}
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
  );
}

export default Login;