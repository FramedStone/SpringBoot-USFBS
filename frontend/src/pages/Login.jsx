import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  useWeb3Auth,
  useWeb3AuthConnect,
  useWeb3AuthDisconnect,
} from "@web3auth/modal/react";
import { Web3Provider } from "@ethersproject/providers";
import Toast from "@components/Toast";
import "@styles/login.css";
import { AUTH_CONNECTION, WALLET_CONNECTORS } from "@web3auth/modal";
import { authFetch } from "@utils/authFetch";
import LoginButton from "@components/LoginButton";

const SCHOOL_EMAIL_REGEX = /@(student\.)?mmu\.edu\.my$/i;

export default function Login() {
  const navigate = useNavigate();
  const { web3Auth } = useWeb3Auth();
  const { connectTo, loading: connectLoading, isConnected, error } = useWeb3AuthConnect();
  const { disconnect } = useWeb3AuthDisconnect();
  const [localUser, setLocalUser] = useState(null);
  const [toast, setToast] = useState({ msg: "", type: "error" });
  const [email, setEmail] = useState("");
  const [ethAddress, setEthAddress] = useState("");
  const [ethPrivateKey, setEthPrivateKey] = useState("");

  // Reset all states and storage when component mounts
  useEffect(() => {
    const resetLoginState = () => {
      localStorage.clear();
      sessionStorage.clear();
      setLocalUser(null);
      setEmail("");
      setEthAddress("");
      setEthPrivateKey("");
      setToast({ msg: "", type: "error" });
    };
    
    resetLoginState();
  }, []);

  useEffect(() => {
    async function checkRoleRedirect() {
      try {
        const res = await authFetch(
          `${import.meta.env.VITE_BACKEND_URL}/api/auth/me`
        );
        if (res.ok) {
          const { role } = await res.json();
          if (role === "Admin") {
            navigate("/admin/dashboard", { replace: true });
          } else if (role === "Moderator") {
            navigate("/moderator/events", { replace: true });
          } else {
            navigate("/user/bookings", { replace: true });
          }
        }
      } catch (err) {
        // Not logged in or 401, stay on login - storage already cleared by authFetch
        console.log("Not authenticated");
      }
    }
    checkRoleRedirect();
  }, [navigate]);

  useEffect(() => {
    const fetchKeys = async () => {
      if (isConnected && web3Auth && web3Auth.provider) {
        try {
          const rpcProvider = new Web3Provider(web3Auth.provider);
          const signer = rpcProvider.getSigner();
          const address = await signer.getAddress();
          setEthAddress(address);

          let privateKey = "";
          try {
            privateKey = await web3Auth.provider.request({ method: "private_key" });
          } catch (err) {
            privateKey = "";
          }
          setEthPrivateKey(privateKey);
        } catch (err) {
          console.error("Failed to fetch blockchain keys:", err);
        }
      }
    };
    fetchKeys();
  }, [isConnected, web3Auth]);

  useEffect(() => {
    (async () => {
      try {
        if (web3Auth) {
          const info = await web3Auth.getUserInfo();
          if (info) {
            setLocalUser(info);
          }
        }
      } catch (err) {
        console.error("Init getUserInfo error:", err);
      }
    })();
  }, [web3Auth]);

  return (
    <div className="login-outer">
      <div className="login-container">
        <div className="login-box">
          <img src="/MMU Logo.png" alt="MMU Logo" className="logo" />
          <div className="header">
            <h1>Login</h1>
            <p>Welcome to the MMU Sport Facility Booking System</p>
          </div>
          <LoginButton
            email={email}
            setEmail={setEmail}
            setLocalUser={setLocalUser}
            setToast={setToast}
            connectTo={connectTo}
            connectLoading={connectLoading}
            isConnected={isConnected}
            web3Auth={web3Auth}
            setEthAddress={setEthAddress}
            setEthPrivateKey={setEthPrivateKey}
            navigate={navigate}
            disconnect={disconnect}
          />
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