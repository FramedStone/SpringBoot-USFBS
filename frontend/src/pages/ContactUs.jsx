import React, { useState } from 'react';
import Navbar from "@components/Navbar";
import Toast from "@components/Toast";
import '@styles/ContactUs.css';

const ContactUs = () => {
  const [activeTab, setActiveTab] = useState("contact");
  const [toast, setToast] = useState({ msg: "", type: "success" });

  return (
    <div className="contact-us-container">
      <Navbar activeTab={activeTab} setActiveTab={setActiveTab} navType="home" />
      <div className="contact-content">
        <div className="page-header">
          <h1>Contact Us</h1>
          <p>Get in touch with our support team</p>
        </div>
        
        <div className="contact-placeholder">
          <h3>Contact Us Page</h3>
          <p>TODO: Implement contact form and support information</p>
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

export default ContactUs;