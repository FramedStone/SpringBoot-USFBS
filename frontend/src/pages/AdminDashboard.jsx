import React, { useState, useEffect } from 'react';
import { useWeb3Auth, useWeb3AuthDisconnect } from "@web3auth/modal/react";
import { useNavigate } from "react-router-dom";
import Toast from "@components/Toast";
import Navbar from "@components/Navbar";
import {
  ExternalLink, Download, Edit, Trash2,
  Plus
} from 'lucide-react';
import '@styles/AdminDashboard.css';

export default function AdminDashboard() {
  const { web3Auth } = useWeb3Auth();
  const { disconnect } = useWeb3AuthDisconnect();
  const navigate = useNavigate();

  const [userEmail, setUserEmail] = useState('');
  const [toast, setToast] = useState({ msg: "", type: "success" });
  const [activeTab, setActiveTab] = useState('dashboard');

  useEffect(() => {
    if (!web3Auth) return;
    web3Auth.getUserInfo()
      .then(info => setUserEmail(info.email))
      .catch(err => console.error("Failed to fetch user info:", err));
  }, [web3Auth]);

  const handleLogout = async () => {
    try {
      const res = await fetch(
        `${import.meta.env.VITE_BACKEND_URL}/api/auth/logout`,
        {
          method: 'POST',
          credentials: 'include'
        }
      );
      if (!res.ok) {
        const err = await res.text();
        console.error('Logout API error:', err);
        setToast({ msg: `Logout failed: ${err}`, type: 'error' });
        return;
      }

      await web3Auth.logout();
      await disconnect();
      localStorage.clear();
      sessionStorage.clear();

      setToast({ msg: 'Logged out successfully', type: 'success' });

      setTimeout(() => {
        navigate('/login', { replace: true });
      }, 1500);

    } catch (e) {
      console.error('Logout fetch error:', e);
      setToast({ msg: `Logout error: ${e.message}`, type: 'error' });
    }
  };

  const [announcements, setAnnouncements] = useState([
    { id: 1, title: 'Court Maintenance', timeLeft: '1 Days' },
    { id: 2, title: 'Basketball Tournament', timeLeft: '7 Days' }
  ]);
  
  const [bookings] = useState([
    { id: 'BK001', user: '123120000', court: 'Court A', time: '2025-03-24\n10 AM - 12 PM', sport: 'Badminton' },
    { id: 'BK002', user: '1231200001', court: 'Court B', time: '2025-03-25\n8 AM - 10 AM', sport: 'Volleyball' },
    { id: 'BK003', user: '1231200002', court: 'Court C', time: '2025-03-25\n1 PM - 3 PM', sport: 'Basketball' }
  ]);

  const [systemLogs] = useState([
    { type: 'form', message: 'Form Received', status: 'error' },
    { type: 'admin', message: 'Admin Action', status: 'warning' },
    { type: 'booking', message: 'Booking Approved (System)', status: 'success' },
  ]);

  const handleDeleteAnnouncement = (id) => {
    setAnnouncements(announcements.filter(ann => ann.id !== id));
  };

  const handleRejectBooking = (id) => {
    console.log('Rejecting booking:', id);
  };

  const getLogIcon = (type) => {
    switch(type) {
      case 'form': return '📋';
      case 'admin': return '⚙️';
      case 'booking': return '✅';
      default: return '📝';
    }
  };

  const getLogStatusClass = (status) => {
    switch(status) {
      case 'error': return 'log-error';
      case 'warning': return 'log-warning';
      case 'success': return 'log-success';
      default: return '';
    }
  };

  return (
    <div className="admin-dashboard">
      <Navbar activeTab={activeTab} setActiveTab={setActiveTab} />
      <div className="dashboard-content">
        <div className="content-grid">
          {/* System Logs Section */}
          <div className="system-logs-section">
            <div className="section-header">
              <h3>System Logs</h3>
              <button
                className="icon-link-btn"
                onClick={() => navigate("/logs")}
                aria-label="Go to System Logs"
                style={{ background: "none", border: "none", cursor: "pointer" }}
              >
                <ExternalLink size={16} />
              </button>
            </div>
            <div className="logs-list">
              {systemLogs.map((log, index) => (
                <div key={index} className={`log-item ${getLogStatusClass(log.status)}`}>
                  <span className="log-icon">{getLogIcon(log.type)}</span>
                  <span className="log-message">{log.message}</span>
                </div>
              ))}
            </div>
          </div>

          {/* Announcements Section */}
          <div className="announcements-section">
            <div className="section-header">
              <h3>Announcement</h3>
              <button className="add-new-btn">
                <Plus size={16} />
                Add New
              </button>
            </div>
            <div className="announcements-table">
              <div className="table-header">
                <span>Title</span>
                <span>Time Left</span>
                <span>Action</span>
              </div>
              {announcements.map(announcement => (
                <div key={announcement.id} className="table-row">
                  <span>{announcement.title}</span>
                  <span>{announcement.timeLeft}</span>
                  <div className="action-buttons">
                    <button className="edit-btn">
                      <Edit size={14} />
                      Edit
                    </button>
                    <button 
                      className="delete-btn"
                      onClick={() => handleDeleteAnnouncement(announcement.id)}
                    >
                      <Trash2 size={14} />
                      Delete
                    </button>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* System Approved Bookings Section */}
        <div className="bookings-section">
          <div className="section-header">
            <h3>System Approved Bookings</h3>
            <div className="header-actions">
              <span className="requests-count">3 requests</span>
              <button className="export-btn">
                <Download size={16} />
                Export Report
              </button>
              <button
                className="icon-link-btn"
                onClick={() => navigate("/admin/booking-management")}
                aria-label="Go to Booking Management"
                style={{ background: "none", border: "none", cursor: "pointer" }}
              >
                <ExternalLink size={16} />
              </button>
            </div>
          </div>
          <div className="bookings-table">
            <div className="table-header">
              <span>ID</span>
              <span>User</span>
              <span>Court</span>
              <span>Time</span>
              <span>Sport</span>
              <span>Actions</span>
            </div>
            {bookings.map(booking => (
              <div key={booking.id} className="table-row">
                <span>{booking.id}</span>
                <span>{booking.user}</span>
                <span>{booking.court}</span>
                <span className="time-cell">{booking.time}</span>
                <span>{booking.sport}</span>
                <div className="action-buttons">
                  <button
                    className="reject-btn"
                    onClick={() => handleRejectBooking(booking.id)}
                    disabled={
                      booking.status &&
                      booking.status.toLowerCase() === "rejected"
                    }
                  >
                    Reject
                  </button>
                </div>
              </div>
            ))}
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
}