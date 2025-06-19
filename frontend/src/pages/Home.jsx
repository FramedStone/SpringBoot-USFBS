import React, { useState } from 'react';
import Navbar from "@components/Navbar";
import Toast from "@components/Toast";
import '@styles/Home.css';

const Home = () => {
  const [activeTab, setActiveTab] = useState("home");
  const [toast, setToast] = useState({ msg: "", type: "success" });

  return (
    <div className="home-container">
      <Navbar activeTab={activeTab} setActiveTab={setActiveTab} navType="home" />
      <div className="home-content">
        <div className="hero-section">
          <h1>Welcome to MMU Sport Facility Booking System</h1>
          <p>Book your favorite sport facilities with ease</p>
        </div>
        
        <div className="features-section">
          <div className="feature-card">
            <h3>Easy Booking</h3>
            <p>Book sport facilities in just a few clicks</p>
          </div>
          <div className="feature-card">
            <h3>Real-time Availability</h3>
            <p>Check real-time availability of courts and facilities</p>
          </div>
          <div className="feature-card">
            <h3>Secure Payment</h3>
            <p>Secure blockchain-based booking system</p>
          </div>
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

export default Home;