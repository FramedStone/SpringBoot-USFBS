import React, { useState, useEffect } from 'react';
import { useWeb3Auth, useWeb3AuthDisconnect } from "@web3auth/modal/react";
import { useNavigate } from "react-router-dom";
import Toast from "../components/Toast";                  // ← import Toast
import { authFetch } from "@utils/authFetch";             // ← helper with credentials
import {
  Users, Calendar, FileText, Settings,
  ExternalLink, Download, Edit, Trash2,
  Plus, Menu, X, User, LogOut
} from 'lucide-react';
import './AdminDashboard.css';

export default function AdminDashboard() {
  const { web3Auth } = useWeb3Auth();
  const { disconnect } = useWeb3AuthDisconnect();
  const navigate = useNavigate();

  const [userEmail, setUserEmail] = useState('');
  const [toast, setToast] = useState({ msg: "", type: "success" });  // ← toast state
  const [activeTab, setActiveTab] = useState('dashboard');
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const [isProfileDropdownOpen, setIsProfileDropdownOpen] = useState(false);

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
    { type: 'admin', message: 'Admin Action', status: 'warning' }
  ]);

  const tabs = [
    { id: 'dashboard', label: 'Dashboard', icon: Settings },
    { id: 'users', label: 'User Management', icon: Users },
    { id: 'courts', label: 'Court & Slot Management', icon: Calendar },
    { id: 'bookings', label: 'Booking Management', icon: FileText }
  ];

  const handleDeleteAnnouncement = (id) => {
    setAnnouncements(announcements.filter(ann => ann.id !== id));
  };

  const handleRejectBooking = (id) => {
    console.log('Rejecting booking:', id);
  };

  const handleTabClick = (tabId) => {
    setActiveTab(tabId);
    setIsMobileMenuOpen(false);
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
      <nav className="dashboard-nav">
        <div className="nav-brand">
          <Settings size={24} />
          <span>Admin Dashboard</span>
        </div>
        
        {/* ─── Mobile Menu & Profile ─────────────────────────── */}
        <button
          className="mobile-menu-toggle"
          onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
        >
          {isMobileMenuOpen ? <X size={24}/> : <Menu size={24}/>}
        </button>
        <div className={`nav-tabs ${isMobileMenuOpen ? 'mobile-open' : ''}`}>
          {tabs.map(tab => {
            const IconComponent = tab.icon;
            return (
              <button
                key={tab.id}
                className={`nav-tab ${activeTab === tab.id ? 'active' : ''}`}
                onClick={() => handleTabClick(tab.id)}
              >
                <IconComponent size={20} />
                <span>{tab.label}</span>
              </button>
            );
          })}

          {isMobileMenuOpen && (
            <div className="mobile-profile-section">
              <div className="email-item-mobile">{userEmail}</div>
              <button
                className="logout-item-mobile"
                onClick={handleLogout}
              >
                <LogOut size={16}/> Logout
              </button>
            </div>
          )}
        </div>

        {/* ─── Desktop Profile Dropdown ──────────────────────── */}
        <div className="profile-section">
          <div className="profile-dropdown">
            <button
              className="profile-button"
              onClick={() => setIsProfileDropdownOpen(!isProfileDropdownOpen)}
            >
              <User size={20}/>
            </button>
            {isProfileDropdownOpen && (
              <>
                <div className="dropdown-menu">
                  <div className="dropdown-item email-item">
                    {userEmail}
                  </div>
                  <button
                    className="dropdown-item logout-item"
                    onClick={handleLogout}
                  >
                    <LogOut size={16}/>
                    Logout
                  </button>
                </div>
                <div
                  className="dropdown-overlay"
                  onClick={() => setIsProfileDropdownOpen(false)}
                />
              </>
            )}
          </div>
        </div>

        {isMobileMenuOpen && (
          <div
            className="mobile-overlay"
            onClick={() => setIsMobileMenuOpen(false)}
          />
        )}
      </nav>

      <div className="dashboard-content">
        <div className="content-grid">
          <div className="system-logs-section">
            <div className="section-header">
              <h3>System Logs</h3>
              <ExternalLink size={16} />
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

        <div className="bookings-section">
          <div className="section-header">
            <h3>System Approved Bookings</h3>
            <div className="header-actions">
              <span className="requests-count">3 requests</span>
              <button className="export-btn">
                <Download size={16} />
                Export Report
              </button>
              <ExternalLink size={16} />
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
};