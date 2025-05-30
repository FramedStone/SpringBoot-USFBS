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

const AddAnnouncementModal = ({ onClose, onSave, initialData }) => {
  const [media, setMedia] = useState(initialData?.media || null);
  const [mediaPreview, setMediaPreview] = useState(initialData?.mediaUrl || null);
  const [title, setTitle] = useState(initialData?.title || '');
  const [dateRange, setDateRange] = useState(initialData?.dateRange || { start: '', end: '' });
  const [error, setError] = useState('');

  useEffect(() => {
    if (!initialData) {
      setMedia(null);
      setMediaPreview(null);
      setTitle('');
      setDateRange({ start: '', end: '' });
      setError('');
    } else {
      setMedia(initialData.media || null);
      setMediaPreview(initialData.mediaUrl || null);
      setTitle(initialData.title || '');
      setDateRange(initialData.dateRange || { start: '', end: '' });
      setError('');
    }
  }, [onClose, initialData]);

  const handleFileChange = (e) => {
    const file = e.target.files?.[0];
    setError("");

    if (file) {
      console.log("Selected file size (bytes):", file.size);
      if (file.size > MAX_MEDIA_SIZE_MB * 1024 * 1024) {
        const msg = `File size must be less than ${MAX_MEDIA_SIZE_MB} MB`;
        setError(msg);
        setMedia(null);
        setMediaPreview(null);
        return;
      }
      setMedia(file);
      setMediaPreview(URL.createObjectURL(file));
    } else {
      setMedia(null);
      setMediaPreview(null);
    }
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!title.trim()) {
      setError('Title is required');
      if (typeof window.setToast === 'function') {
        window.setToast({ msg: 'Title is required', type: 'error' });
      }
      return;
    }
    if (!dateRange.start || !dateRange.end) {
      setError('Date range is required');
      if (typeof window.setToast === 'function') {
        window.setToast({ msg: 'Date range is required', type: 'error' });
      }
      return;
    }

    const startTs = new Date(dateRange.start).getTime();
    const endTs = new Date(dateRange.end).getTime();

    const formData = new FormData();
    formData.append("file", media);                 // File object
    formData.append("title", title);
    formData.append("startDate", startTs);
    formData.append("endDate",   endTs);

    // DEBUG
    console.log("FormData entries:", [...formData.entries()]);

    onSave(formData); 

    // Reset form state
    setMedia(null);
    setMediaPreview(null);
    setTitle("");
    setDateRange({ start: "", end: "" });
    setError("");
  };

  const previewStyle = {
    width: 320,
    height: 180,
    objectFit: "cover",
    borderRadius: 8,
    background: "#eee",
    display: "block",
    margin: "0 auto"
  };

  return (
    <div className="modal-overlay">
      <div className="modal">
        <div className="modal-header">
          <h3>{initialData ? "Edit Announcement" : "Add Announcement"}</h3>
          <button className="close-btn" onClick={onClose}>
            <X size={20} />
          </button>
        </div>
        <form className="modal-form" onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Upload Media (max 10MB, image/video)</label>
            <input
              type="file"
              accept="image/*,video/*"
              onChange={handleFileChange}     
            />
            {mediaPreview && (
              <div style={{ marginTop: 12 }}>
                {media && media.type && media.type.startsWith('video') ? (
                  <video src={mediaPreview} controls style={previewStyle} />
                ) : (
                  <img src={mediaPreview} alt="Preview" style={previewStyle} />
                )}
                {media && (
                  <div style={{ textAlign: "center", marginTop: 8, fontSize: 13, color: "#374151" }}>
                    {media.name}
                  </div>
                )}
              </div>
            )}
            {error && (
              <div style={{ color: "#dc2626", marginTop: 8, fontSize: 13 }}>{error}</div>
            )}
          </div>
          <div className="form-group">
            <label>Announcement Title *</label>
            <input
              type="text"
              value={title}
              onChange={e => setTitle(e.target.value)}
              required
              placeholder="Enter announcement title"
            />
          </div>
          <div className="form-group">
            <label>Date Display (Range)</label>
            <div style={{ display: 'flex', gap: 8 }}>
              <input
                type="date"
                value={dateRange.start}
                onChange={e => setDateRange({ ...dateRange, start: e.target.value })}
                required
              />
              <span style={{ alignSelf: 'center' }}>to</span>
              <input
                type="date"
                value={dateRange.end}
                onChange={e => setDateRange({ ...dateRange, end: e.target.value })}
                required
              />
            </div>
          </div>
          <div className="modal-actions">
            <button type="button" onClick={onClose} className="cancel-btn">Cancel</button>
            <button type="submit" className="save-btn">{initialData ? "Save Changes" : "Add Announcement"}</button>
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

  // utility to compute “Time Left” display
  const calculateTimeLeft = (endTs) => {
    const diff = endTs - Date.now();
    if (diff <= 0) return 'Expired';
    const hrs = Math.floor(diff / 3600000);
    const mins = Math.floor((diff % 3600000) / 60000);
    return `${hrs}h ${mins}m`;
  };

  // Load all announcements from backend, always use authFetch
  useEffect(() => {
    async function loadAnnouncements() {
      setAnnouncementsLoading(true);
      try {
        const res = await authFetch("/api/admin/get-announcements");
        if (!res.ok) throw new Error("Failed to load announcements");
        const data = await res.json();
        console.log("Fetched announcements data:", data);
        const items = data.map(item => ({
          ...item,
          timeLeft: calculateTimeLeft(item.endDate),
          id: item.ipfsHash,
        }));
        setAnnouncements(items);
      } catch (err) {
        console.error("Error loading announcements:", err);
        setToast({ msg: err.message, type: "error" });
      } finally {
        setAnnouncementsLoading(false);
      }
    }

    loadAnnouncements();

  }, []);

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

  const [showAddAnnouncementModal, setShowAddAnnouncementModal] = useState(false);
  const [editAnnouncement, setEditAnnouncement] = useState(null);

  // Always use authFetch for add announcement
  const handleAddAnnouncement = async (formData) => {
    try {
      const res = await authFetch("/api/admin/upload-announcement", {
        method: "POST",
        body: formData,
        // Don't set Content-Type for FormData, browser will set it
        headers: {},
      });
      if (!res.ok) {
        const errText = await res.text();
        throw new Error(errText || "Upload failed");
      }
      const { message } = await res.json();
      setShowAddAnnouncementModal(false);
      setToast({ msg: message, type: "success" });
      // Refresh list after add
      await loadAnnouncements();
    } catch (err) {
      console.error("Add announcement failed:", err);
      setToast({ msg: err.message, type: "error" });
    }
  };

  const handleEditAnnouncement = (announcement) => {
    setEditAnnouncement(announcement);
  };

  const handleSaveEditAnnouncement = (data) => {
    // TODO: Update announcement in backend/blockchain
    setEditAnnouncement(null);
    setToast({ msg: "Announcement updated!", type: "success" });
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
              <button
                className="add-new-btn"
                onClick={() => setShowAddAnnouncementModal(true)}
              >
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
                      <span>{announcement.timeLeft}</span>
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