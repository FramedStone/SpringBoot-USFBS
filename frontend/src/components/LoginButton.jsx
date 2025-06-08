import { Web3Provider } from "@ethersproject/providers";
import { AUTH_CONNECTION, WALLET_CONNECTORS } from "@web3auth/modal";
import { authFetch } from "@utils/authFetch";

const SCHOOL_EMAIL_REGEX = /@(student\.)?mmu\.edu\.my$/i;

export default function LoginButton({
  email,
  setEmail,
  setLocalUser,
  setToast,
  connectTo,
  connectLoading,
  isConnected,
  web3Auth,
  setEthAddress,
  setEthPrivateKey,
  navigate,
  disconnect,
}) {
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
          flow_type: "link",
        },
      });
      if (!provider) throw new Error("No wallet provider");

      const info = await web3Auth.getUserInfo();
      if (!SCHOOL_EMAIL_REGEX.test(info.email)) {
        await web3Auth.logout();
        await disconnect();
        setLocalUser(null);
        setToast({ msg: "Invalid school email", type: "error" });
        setTimeout(() => {
          window.location.reload();
        }, 2000);
        return;
      }

      const rpcProvider = new Web3Provider(provider);
      const signer = rpcProvider.getSigner();
      const address = await signer.getAddress();
      setEthAddress(address);
      console.log("Blockchain Address Retrieved: ", address);

      let privateKey = "";
      try {
        privateKey = await provider.request({ method: "private_key" });
      } catch (err) {
        privateKey = "";
      }
      setEthPrivateKey(privateKey);
      console.log("Private Key Retrieved: 0x", privateKey); // comment out during production

      // SpringBoot verify JWT tokens
      const payload = { email: info.email, userAddress: address };
      const res = await fetch(`/api/auth/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        credentials: "include", // <-- include cookies
        body: JSON.stringify(payload), 
      });

      if (!res.ok) {
        const errorText = await res.text();
        throw new Error(`Login failed: ${res.status} – ${errorText}`);
      }
      const data = await res.json();
      setLocalUser(info);
      setToast({ msg: "Login successful!", type: "success" });

      // Redirect based on role
      if (data.role === "Admin") {
        navigate("/admin/dashboard");
      } else if (data.role === "Moderator") {
        navigate("/moderator/events");
      } else {
        navigate("/user/bookings");
      }
    } catch (err) {
      setToast({ msg: "Login error: " + err.message, type: "error" });
    }
  };

  return (
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
  );
}