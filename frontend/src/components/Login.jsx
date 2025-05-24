import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useWeb3AuthConnect } from "@web3auth/modal/react";
import { CHAIN_NAMESPACES, WEB3AUTH_NETWORK } from "@web3auth/base";
import { Web3Auth } from "@web3auth/modal";
import { Web3Provider } from "@ethersproject/providers";
import Toast from "./Toast";
import "../styles/login.css";

const CLIENT_ID          = import.meta.env.VITE_WEB3AUTH_CLIENT_ID;
const RPC_URL            = import.meta.env.VITE_QUORUM_RPC_URL;
const CHAIN_ID           = import.meta.env.VITE_QUORUM_CHAIN_ID;
const SCHOOL_EMAIL_REGEX = /@(student\.)?mmu\.edu\.my$/i;

function Login({ web3auth, setWeb3auth, setUser }) {
  const { connect, loading: connectLoading, isConnected, error } =
    useWeb3AuthConnect();
  const [localUser, setLocalUser] = useState(null);
  const [toast,     setToast    ] = useState({ msg: "", type: "error" });
  const [loading,   setLoading  ] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    async function init() {
      try {
        const w3a = new Web3Auth({
          clientId:        CLIENT_ID,
          web3AuthNetwork: WEB3AUTH_NETWORK.TESTNET,
          chainConfig: {
            chainNamespace: CHAIN_NAMESPACES.EIP155,
            chainId:        CHAIN_ID,
            rpcTarget:      RPC_URL,
          },
        });
        setWeb3auth(w3a);
        await w3a.init();
        if (w3a.provider) {
          const info = await w3a.getUserInfo();
          setLocalUser(info);
          setUser(info);
        }
      } catch (err) {
        console.error("Web3Auth init error:", err);
        setToast({ msg: "Init error: " + err.message, type: "error" });
      } finally {
        setLoading(false);
      }
    }
    init();
  }, [setWeb3auth, setUser]);

  const handleLogin = async () => {
    setToast({ msg: "", type: "error" });
    try {
      if (!web3auth) throw new Error("Web3Auth not initialized");

      const provider = await connect();
      if (!provider) throw new Error("No provider returned");

      const ethProvider = new Web3Provider(provider);
      const address     = await ethProvider.getSigner().getAddress();
      console.log("Connected address:", address);

      const rawKey = await provider.request({ method: "private_key" });
      console.log("Private key:", rawKey);

      const info = await web3auth.getUserInfo();
      if (!SCHOOL_EMAIL_REGEX.test(info.email)) {
        setToast({ msg: "Invalid school email", type: "error" });
        return;
      }

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
        {(loading || connectLoading) && <div className="spinner">Loading…</div>}

        {!loading && !localUser && (
          <button
            className="btn"
            onClick={handleLogin}
            disabled={connectLoading || isConnected}
          >
            {connectLoading
              ? "Connecting..."
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