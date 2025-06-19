import React, { useState } from 'react';
import Navbar from "@components/Navbar";
import Toast from "@components/Toast";
import '@styles/UserBookings.css';

const UserBookings = () => {
  const [activeTab, setActiveTab] = useState("bookings");
  const [toast, setToast] = useState({ msg: "", type: "success" });

  return (
    <div className="user-bookings-container">
      <Navbar activeTab={activeTab} setActiveTab={setActiveTab} navType="home" />
      <div className="bookings-content">
        <div className="page-header">
          <h1>My Bookings</h1>
          <p>Manage your sport facility bookings</p>
        </div>
        
        <div className="bookings-placeholder">
          <h3>Bookings Page</h3>
          <p>TODO: Implement booking management functionality</p>
        </div>
      </div>
      
      <Toast
        message={toast.msg}
        type={toast.type}
        onClose={() => setToast({ msg: "", type: "success" })}
      />
    </div>
  );
};

export default UserBookings;