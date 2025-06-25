import React, { useState, useEffect } from 'react';
import Navbar from "@components/Navbar";
import Toast from "@components/Toast";
import { authFetch } from '@utils/authFetch';
import '@styles/Cart.css';
import { useWeb3Auth } from "@web3auth/modal/react";
import { Web3Provider } from "@ethersproject/providers";
import { useNavigate } from "react-router-dom"; 

const Cart = () => {
  const [activeTab, setActiveTab] = useState("cart");
  const [toast, setToast] = useState({ msg: "", type: "success" });
  const [cartSlots, setCartSlots] = useState(() => {
    const saved = localStorage.getItem('admin_cart_slots');
    return saved ? JSON.parse(saved) : [];
  });
  const [submitting, setSubmitting] = useState(false);
  const [userRole, setUserRole] = useState(""); 
  const [ethAddress, setEthAddress] = useState(""); 

  const { web3Auth } = useWeb3Auth();
  const navigate = useNavigate(); 

  // Fetch user role on mount
  useEffect(() => {
    const fetchRole = async () => {
      try {
        const backendUrl = import.meta.env.VITE_BACKEND_URL || "http://localhost:8080";
        const res = await authFetch(`${backendUrl}/api/auth/me`);
        if (res.ok) {
          const data = await res.json();
          setUserRole(data.role || "User");
        }
      } catch (err) {
        setUserRole("User");
      }
    };
    fetchRole();
  }, []);

  // Fetch ethAddress from Web3Auth (same logic as Login.jsx)
  useEffect(() => {
    const fetchEthAddress = async () => {
      if (web3Auth && web3Auth.provider) {
        try {
          const rpcProvider = new Web3Provider(web3Auth.provider);
          const signer = rpcProvider.getSigner();
          const address = await signer.getAddress();
          setEthAddress(address);
        } catch (err) {
          setEthAddress("");
        }
      }
    };
    fetchEthAddress();
  }, [web3Auth]);

  // Helper: convert "YYYY-MM-DD" and "HH:mm" to unix timestamp (seconds)
  const getUnixTimestamp = (dateStr, timeStr) => {
    const [year, month, day] = dateStr.split('-').map(Number);
    const [hour, minute] = timeStr.split(':').map(Number);
    return Math.floor(new Date(year, month - 1, day, hour, minute, 0).getTime() / 1000);
  };

  // Remove slot from cart
  const removeSlot = (slotKey) => {
    const updated = cartSlots.filter(s => s.key !== slotKey);
    setCartSlots(updated);
    localStorage.setItem('admin_cart_slots', JSON.stringify(updated));
  };

  // Confirm booking: call backend for each slot
  const handleConfirmBooking = async () => {
    setSubmitting(true);
    try {
      for (const slot of cartSlots) {
        const startTime = getUnixTimestamp(slot.date, slot.timeSlot);
        const endTime = startTime + 3600; // 1 hour slot

        const payload = {
          facilityName: slot.facilityName,
          courtName: slot.courtName,
          startTime,
          endTime,
          userAddress: ethAddress // always from Web3Auth
        };

        // Use correct endpoint based on role
        const endpoint =
          userRole === "Admin"
            ? "/api/admin/bookings"
            : "/api/user/bookings";

        const res = await authFetch(endpoint, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(payload),
        });

        if (!res.ok) {
          const err = await res.json().catch(() => ({}));
          throw new Error(err.error || "Booking failed");
        }
      }
      setToast({ msg: "All bookings submitted!", type: "success" });
      setCartSlots([]);
      localStorage.removeItem('admin_cart_slots');
      navigate("/bookings");
    } catch (err) {
      setToast({ msg: "Booking failed: " + err.message, type: "error" });
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="cart-container">
      <Navbar activeTab={activeTab} setActiveTab={setActiveTab} navType="home" />
      <div className="cart-content">
        <div className="page-header">
          <h1>Booking Cart</h1>
          <p>Review your selected bookings before checkout</p>
        </div>
        {cartSlots.length === 0 ? (
          <div className="cart-placeholder">
            <h3>Cart Page</h3>
            <p>No bookings selected. Go back and select time slots.</p>
          </div>
        ) : (
          <div>
            <table className="cart-table">
              <thead>
                <tr>
                  <th>Facility</th>
                  <th>Court</th>
                  <th>Date</th>
                  <th>Time Slot</th>
                  <th>Remove</th>
                </tr>
              </thead>
              <tbody>
                {cartSlots.map(slot => {
                  // Calculate start and end time strings
                  const startTime = getUnixTimestamp(slot.date, slot.timeSlot);
                  const endTime = startTime + 3600; // 1 hour slot
                  const formatTime = (unix) => {
                    const d = new Date(unix * 1000);
                    return d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
                  };
                  return (
                    <tr key={slot.key}>
                      <td>{slot.facilityName}</td>
                      <td>{slot.courtName}</td>
                      <td>{slot.date}</td>
                      <td>
                        {formatTime(startTime)} - {formatTime(endTime)}
                      </td>
                      <td>
                        <button onClick={() => removeSlot(slot.key)}>Remove</button>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
            <button
              className="cart-confirm-btn"
              onClick={handleConfirmBooking}
              disabled={submitting}
            >
              {submitting ? "Booking..." : "Confirm Booking"}
            </button>
          </div>
        )}
      </div>
      
      <Toast
        message={toast.msg}
        type={toast.type}
        onClose={() => setToast({ msg: "", type: "success" })}
      />
    </div>
  );
};

export default Cart;