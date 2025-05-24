import { useEffect } from "react";

function Toast({ message, type = "error", onClose }) {
  useEffect(() => {
    if (!message) return;
    const timer = setTimeout(onClose, 3000);
    return () => clearTimeout(timer);
  }, [message, onClose]);

  if (!message) return null;
  return (
    <div
      style={{
        position: "fixed",
        top: "1rem",
        left: "50%",
        transform: "translateX(-50%)",
        background: type === "error" ? "#ffdddd" : "#ddffdd",
        color: type === "error" ? "#a00" : "#070",
        padding: "1em",
        borderRadius: "8px",
        boxShadow: "0 2px 8px rgba(0,0,0,0.1)",
        zIndex: 1000,
      }}
      role="alert"
    >
      {message}
    </div>
  );
}

export default Toast;