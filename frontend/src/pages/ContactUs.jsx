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
        <div className="contact-grid">
          <div className="contact-forms-section">
            <ul className="contact-forms-list">
              <li>
                <a
                  href="https://docs.google.com/forms/d/e/1FAIpQLSfcN4o41DxtpneR7hB7lDMvRxBu8iOGyoaze6mnunD6l9eoBQ/viewform?usp=header"
                  target="_blank"
                  rel="noopener noreferrer"
                  className="contact-form-link"
                >
                  Complaint Form
                </a>
              </li>
              <li>
                <a
                  href="https://docs.google.com/forms/d/e/1FAIpQLSdLdR_mXPLcegsZoUFVjkJJxNGPw_QoGAJImMjDK3kkMYg8Ww/viewform"
                  target="_blank"
                  rel="noopener noreferrer"
                  className="contact-form-link"
                >
                  Feedback Form
                </a>
              </li>
              <li>
                <a
                  href="https://docs.google.com/forms/d/e/1FAIpQLSf2Z_NVS7AxaRHN_7XfP6W-e7ZE_Xya-a4mdL749gNSS0PRNQ/viewform?usp=header"
                  target="_blank"
                  rel="noopener noreferrer"
                  className="contact-form-link"
                >
                  Maintenance Request Form
                </a>
              </li>
            </ul>
          </div>
          <div className="contact-info-section">
            <div className="contact-info-block">
              <div>
                <strong>Department Contact Number</strong>
                <div>+1-300-80-0668</div>
              </div>
              <div style={{ marginTop: "1.5rem" }}>
                <strong>Person In Charge:</strong>
                <ul className="contact-person-list">
                  <li>John Doe - <a href="tel:+601111111111">+6011-1111-1111</a></li>
                  <li>John Coe - <a href="tel:+6022222222222">+6022-2222-22222</a></li>
                  <li>John Low - <a href="tel:+603333333333">+6033-3333-3333</a></li>
                </ul>
              </div>
            </div>
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

export default ContactUs;