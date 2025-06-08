import React, { useState, useEffect, useCallback } from 'react';
import { useWeb3Auth, useWeb3AuthDisconnect } from "@web3auth/modal/react";
import { useNavigate } from "react-router-dom";
import Toast from "@components/Toast";
import Navbar from "@components/Navbar";
import Spinner from "@components/Spinner";
import {
  ExternalLink, Download, Edit, Trash2,
  Plus, X
} from 'lucide-react';
import '@styles/AdminDashboard.css';
import { authFetch } from "@utils/authFetch";

const MAX_MEDIA_SIZE_MB = 10;

// Helper function to format date for input field - declared at module level
const formatDateForInput = (timestamp) => {
  if (!timestamp) return '';
  const date = new Date(timestamp);
  return date.toISOString().split('T')[0];
};

// Convert date to timestamp for backend
const convertDateToTimestamp = (dateString) => {
  return Math.floor(new Date(dateString).getTime() / 1000);
};

const AddAnnouncementModal = ({ onClose, onSave, initialData }) => {
  const [formData, setFormData] = useState({
    title: initialData?.title || '',
    file: null,
    dateRange: {
      start: initialData ? formatDateForInput(initialData.startDate * 1000) : '',
      end: initialData ? formatDateForInput(initialData.endDate * 1000) : ''
    }
  });

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!formData.title.trim()) {
      alert('Please enter a title');
      return;
    }
    
    if (!formData.file && !initialData) {
      alert('Please select a file');
      return;
    }
    
    if (!formData.dateRange.start || !formData.dateRange.end) {
      alert('Please select both start and end dates');
      return;
    }

    const startTimestamp = convertDateToTimestamp(formData.dateRange.start);
    const endTimestamp = convertDateToTimestamp(formData.dateRange.end);

    if (startTimestamp >= endTimestamp) {
      alert('End date must be after start date');
      return;
    }

    const submitData = new FormData();
    submitData.append('title', formData.title);
    submitData.append('startDate', startTimestamp);
    submitData.append('endDate', endTimestamp);
    
    if (initialData) {
      // For editing existing announcement
      submitData.append('oldIpfsHash', initialData.ipfsHash);
      // Only append file if one is selected
      if (formData.file) {
        submitData.append('file', formData.file);
      }
    } else {
      // For new announcement, file is required
      submitData.append('file', formData.file);
    }

    onSave(submitData);
  };

  // Handle overlay click to close modal
  const handleOverlayClick = (e) => {
    if (e.target === e.currentTarget) {
      onClose();
    }
  };

  return (
    <div className="modal-overlay" onClick={handleOverlayClick}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h3>{initialData ? "Edit Announcement" : "Add New Announcement"}</h3>
          <button onClick={onClose} className="close-btn">
            <X size={20} />
          </button>
        </div>
        <form onSubmit={handleSubmit} className="announcement-form">
          <div className="form-group">
            <label>Title</label>
            <input
              type="text"
              value={formData.title}
              onChange={e => setFormData({ ...formData, title: e.target.value })}
              placeholder="Enter announcement title"
              required
            />
          </div>
          <div className="form-group">
            <label>
              File 
              {initialData && (
                <span style={{ fontSize: '12px', color: '#6b7280', fontWeight: 'normal' }}>
                  {' '}(Optional - leave empty to keep current file)
                </span>
              )}
            </label>
            <input
              type="file"
              onChange={e => setFormData({ ...formData, file: e.target.files[0] })}
              accept=".pdf,.doc,.docx,.jpg,.jpeg,.png"
              required={!initialData}
            />
            {initialData && (
              <div style={{ fontSize: '12px', color: '#6b7280', marginTop: '4px' }}>
                Current file will be preserved if no new file is selected
              </div>
            )}
          </div>
          <div className="form-group">
            <label>Date Range</label>
            <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
              <input
                type="date"
                value={formData.dateRange.start}
                onChange={e => setFormData({ 
                  ...formData, 
                  dateRange: { ...formData.dateRange, start: e.target.value }
                })}
                required
              />
              <span style={{ color: '#6b7280', fontSize: '14px' }}>to</span>
              <input
                type="date"
                value={formData.dateRange.end}
                onChange={e => setFormData({ 
                  ...formData, 
                  dateRange: { ...formData.dateRange, end: e.target.value }
                })}
                required
              />
            </div>
          </div>
          <div className="modal-actions">
            <button type="button" onClick={onClose} className="cancel-btn">
              Cancel
            </button>
            <button type="submit" className="save-btn">
              {initialData ? "Save Changes" : "Add Announcement"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default function AdminDashboard() {
  const { web3Auth } = useWeb3Auth();
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

  const [announcements, setAnnouncements] = useState([]);
  const [announcementsLoading, setAnnouncementsLoading] = useState(false);

  // Utility to format date display
  const formatDate = (timestamp) => {
    if (!timestamp) return 'Not set';
    const date = new Date(timestamp);
    return date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    });
  };

  // Utility to format date range display
  const formatDateRange = (startDate, endDate) => {
    const start = formatDate(startDate * 1000); // Convert from seconds to milliseconds
    const end = formatDate(endDate * 1000);
    return `${start} - ${end}`;
  };

  // Fixed: Remove announcementsLoading from dependency array to prevent infinite loop
  const loadAnnouncements = useCallback(async () => {
    // Prevent multiple concurrent calls using current state value
    if (announcementsLoading) {
        console.log('Announcements already loading, skipping duplicate call');
        return;
    }
    
    setAnnouncementsLoading(true);
    try {
        const res = await authFetch("/api/admin/get-announcements");
        if (!res.ok) {
            const errorText = await res.text();
            throw new Error(errorText || "Failed to load announcements");
        }
        const data = await res.json();
        
        // Process announcement data for display
        const items = data.map(item => ({
            ...item,
            dateRange: formatDateRange(item.startDate, item.endDate),
            id: item.ipfsHash,
        }));
        setAnnouncements(items);
    } catch (err) {
        console.error("Error loading announcements:", err);
        if (err.message.includes("No Announcement found")) {
            setAnnouncements([]);
            setToast({ msg: "No announcements available", type: "info" });
        } else {
            setToast({ msg: err.message, type: "error" });
        }
    } finally {
        setAnnouncementsLoading(false);
    }
  }, []); // Empty dependency array to prevent infinite loops

  // Load all announcements from backend on component mount
  useEffect(() => {
    loadAnnouncements();
  }, [loadAnnouncements]);

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

  const handleDeleteAnnouncement = async (id) => {
    if (!confirm('Are you sure you want to delete this announcement?')) {
      return;
    }

    try {
      const res = await authFetch(`/api/admin/delete-announcement?ipfsHash=${encodeURIComponent(id)}`, {
        method: "DELETE",
      });
      
      if (!res.ok) {
        const errorText = await res.text();
        throw new Error(errorText || "Failed to delete announcement");
      }
      
      const { message } = await res.json();
      setAnnouncements(announcements.filter(ann => ann.id !== id));
      setToast({ msg: message, type: "success" });
      
    } catch (err) {
      console.error("Delete announcement failed:", err);
      setToast({ msg: err.message, type: "error" });
    }
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

  const [showAddAnnouncementModal, setShowAddAnnouncementModal] = useState(false);
  const [editAnnouncement, setEditAnnouncement] = useState(null);

  // Always use authFetch for add announcement
  const handleAddAnnouncement = async (formData) => {
    try {
      const res = await authFetch("/api/admin/upload-announcement", {
        method: "POST",
        body: formData,
        headers: {},
      });
      if (!res.ok) {
        const errText = await res.text();
        throw new Error(errText || "Upload failed");
      }
      const { message } = await res.json();
      setShowAddAnnouncementModal(false);
      setToast({ msg: message, type: "success" });
      await loadAnnouncements();
    } catch (err) {
      console.error("Add announcement failed:", err);
      setToast({ msg: err.message, type: "error" });
    }
  };

  const handleEditAnnouncement = (announcement) => {
    setEditAnnouncement(announcement);
  };

  const handleSaveEditAnnouncement = async (formData) => {
    try {
      // Debug log to check FormData contents
      console.log('Submitting form data:');
      for (let [key, value] of formData.entries()) {
        console.log(key, ':', value);
      }

      const res = await authFetch("/api/admin/update-announcement", {
        method: "PUT",
        body: formData,
        // Don't set Content-Type header for FormData - let browser set it
      });
      
      if (!res.ok) {
        const errText = await res.text();
        console.error('Update response error:', errText);
        throw new Error(errText || "Update failed");
      }
      
      const response = await res.json();
      console.log('Update response:', response);
      
      setEditAnnouncement(null);
      setToast({ msg: response.message, type: "success" });
      await loadAnnouncements();
    } catch (err) {
      console.error("Edit announcement failed:", err);
      setToast({ msg: err.message, type: "error" });
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
                aria-label="Go to Logs"
                style={{ background: "none", border: "none", cursor: "pointer" }}
              >
                <ExternalLink size={16} />
              </button>
            </div>
            <div className="logs-container">
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
              <h3>Announcements</h3>
              <div className="header-actions">
                <span className="announcements-count">{announcements.length} total</span>
                <button
                  className="add-btn"
                  onClick={() => setShowAddAnnouncementModal(true)}
                >
                  <Plus size={16} />
                  Add New
                </button>
              </div>
            </div>
            <div className="table-container">
              <div className="table-header">
                <span>Title</span>
                <span>Date Range</span>
                <span>Action</span>
              </div>
              {announcementsLoading ? (
                <div style={{
                  display: "flex",
                  justifyContent: "center",
                  alignItems: "center",
                  minHeight: "160px",
                  height: "100%",
                  width: "100%"
                }}>
                  <Spinner />
                </div>
              ) : (
                announcements.length === 0 ? (
                  <div style={{ textAlign: "center", padding: "2rem", color: "#888" }}>
                    No announcements found.
                  </div>
                ) : (
                  announcements.map(announcement => (
                    <div key={announcement.id} className="table-row">
                      <span>{announcement.title}</span>
                      <span>{announcement.dateRange}</span>
                      <div className="action-buttons">
                        <button
                          className="edit-btn"
                          onClick={() => handleEditAnnouncement(announcement)}
                        >
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
                  ))
                )
              )}
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
          <div className="table-container">
            <div className="table-header">
              <span>Booking ID</span>
              <span>User ID</span>
              <span>Court</span>
              <span>Time</span>
              <span>Sport</span>
              <span>Action</span>
            </div>
            {bookings.map(booking => (
              <div key={booking.id} className="table-row">
                <span>{booking.id}</span>
                <span>{booking.user}</span>
                <span>{booking.court}</span>
                <span style={{ whiteSpace: 'pre-line' }}>{booking.time}</span>
                <span>{booking.sport}</span>
                <div className="action-buttons">
                  <button
                    className="reject-btn"
                    onClick={() => handleRejectBooking(booking.id)}
                  >
                    <X size={14} />
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

      {showAddAnnouncementModal && (
        <AddAnnouncementModal
          onClose={() => setShowAddAnnouncementModal(false)}
          onSave={handleAddAnnouncement}
        />
      )}
      {editAnnouncement && (
        <AddAnnouncementModal
          onClose={() => setEditAnnouncement(null)}
          onSave={handleSaveEditAnnouncement}
          initialData={editAnnouncement}
        />
      )}
    </div>
  );
}