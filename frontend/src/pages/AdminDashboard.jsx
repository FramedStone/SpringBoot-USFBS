import { useState, useEffect, useCallback, useRef } from 'react';
import { useWeb3Auth } from "@web3auth/modal/react";
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
import MediaUpload from "@components/MediaUpload";
import * as XLSX from "xlsx";

// Utility functions for date conversion
const convertDateToTimestamp = (dateString) => {
  // Convert date string to timestamp for Malaysian timezone
  const date = new Date(dateString + 'T00:00:00+08:00'); // Add Malaysian timezone offset
  return Math.floor(date.getTime() / 1000);
};

const formatDateForInput = (timestamp) => {
  // Format timestamp for date input in Malaysian timezone
  const date = new Date(timestamp);
  const malaysianDate = new Date(date.getTime() + (8 * 60 * 60 * 1000)); // Add 8 hours for MYT
  return malaysianDate.toISOString().split('T')[0];
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

  const [isSubmitting, setIsSubmitting] = useState(false);
  const [currentMediaUrl, setCurrentMediaUrl] = useState(null);
  const [mediaType, setMediaType] = useState(null);
  const [newFilePreviewUrl, setNewFilePreviewUrl] = useState(null);
  const [zoomedMedia, setZoomedMedia] = useState(null);
  const [isDatePickerOpen, setIsDatePickerOpen] = useState(false);
  const [tempDateRange, setTempDateRange] = useState({ start: '', end: '' });
  const [selectingEnd, setSelectingEnd] = useState(false);

  // Fetch current media when editing
  useEffect(() => {
    if (initialData?.fileCid) {
      const fetchCurrentMedia = async () => {
        try {
          const gatewayUrl = `https://gateway.pinata.cloud/ipfs/${initialData.fileCid}`;
          setCurrentMediaUrl(gatewayUrl);
          
          // Determine media type based on file extension or content type
          const response = await fetch(gatewayUrl, { method: 'HEAD' });
          const contentType = response.headers.get('content-type');
          
          if (contentType?.startsWith('image/')) {
            setMediaType('image');
          } else if (contentType?.includes('pdf')) {
            setMediaType('pdf');
          } else {
            setMediaType('document');
          }
        } catch (error) {
          console.error('Failed to fetch current media:', error);
        }
      };
      
      fetchCurrentMedia();
    }
  }, [initialData]);

  // Clean up preview URLs on unmount
  useEffect(() => {
    return () => {
      if (newFilePreviewUrl) {
        URL.revokeObjectURL(newFilePreviewUrl);
      }
    };
  }, [newFilePreviewUrl]);

  // Enhanced date range handlers with timezone fix
  const handleDateRangeClick = () => {
    setTempDateRange(formData.dateRange);
    setSelectingEnd(false);
    setIsDatePickerOpen(true);
  };

  const handleDateSelect = (selectedDate) => {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    
    const selectedDateObj = new Date(selectedDate + 'T00:00:00');
    
    if (!selectingEnd) {
      // Selecting start date
      let allowPastDate = false;
      
      // For editing: Check if original announcement had past dates
      if (initialData) {
        const originalStartDate = new Date(initialData.startDate * 1000);
        originalStartDate.setHours(0, 0, 0, 0);
        allowPastDate = originalStartDate < today;
      }
      
      // Prevent selection of past dates unless editing announcement with original past dates
      if (selectedDateObj < today && !allowPastDate) {
        return;
      }
      
      setTempDateRange({ start: selectedDate, end: '' });
      setSelectingEnd(true);
    } else {
      // Selecting end date - must be same day or after start date
      const startDateObj = new Date(tempDateRange.start + 'T00:00:00');
      
      if (selectedDateObj < startDateObj) {
        // If end date is before start date, reset to select start again
        let allowPastDate = false;
        
        if (initialData) {
          const originalStartDate = new Date(initialData.startDate * 1000);
          originalStartDate.setHours(0, 0, 0, 0);
          allowPastDate = originalStartDate < today;
        }
        
        if (selectedDateObj >= today || allowPastDate) {
          setTempDateRange({ start: selectedDate, end: '' });
          setSelectingEnd(true);
        }
        return;
      }
      
      setTempDateRange({ ...tempDateRange, end: selectedDate });
      // Auto-confirm after selecting end date
      setFormData({ 
        ...formData, 
        dateRange: { start: tempDateRange.start, end: selectedDate }
      });
      setIsDatePickerOpen(false);
      setSelectingEnd(false);
    }
  };

  const confirmDateRange = () => {
    if (tempDateRange.start && tempDateRange.end) {
      setFormData({ ...formData, dateRange: tempDateRange });
      setIsDatePickerOpen(false);
      setSelectingEnd(false);
    }
  };

  const cancelDateSelection = () => {
    setIsDatePickerOpen(false);
    setSelectingEnd(false);
    setTempDateRange({ start: '', end: '' });
  };

  // Fixed date generation with proper timezone handling
  const generateCalendarDates = () => {
    const today = new Date();
    const currentMonth = today.getMonth();
    const currentYear = today.getFullYear();
    
    const firstDay = new Date(currentYear, currentMonth, 1);
    const startDate = new Date(firstDay);
    startDate.setDate(startDate.getDate() - firstDay.getDay());
    
    const dates = [];
    const current = new Date(startDate);
    
    for (let i = 0; i < 42; i++) {
      dates.push(new Date(current));
      current.setDate(current.getDate() + 1);
    }
    
    return dates;
  };

  // Fixed date formatting to prevent timezone issues
  const formatDateString = (date) => {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  };

  const formatDateForDisplay = (dateString) => {
    if (!dateString) return '';
    // Fix: Parse date without timezone conversion
    const dateParts = dateString.split('-');
    const date = new Date(dateParts[0], dateParts[1] - 1, dateParts[2]);
    return date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    });
  };

  const renderDateRangePicker = () => {
    if (!isDatePickerOpen) return null;

    const dates = generateCalendarDates();
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const currentMonth = today.getMonth();
    const currentYear = today.getFullYear();

    return (
      <div className="date-picker-overlay" onClick={cancelDateSelection}>
        <div className="date-picker-modal" onClick={(e) => e.stopPropagation()}>
          <div className="date-picker-header">
            <h4>Select Date Range</h4>
            <div className="date-picker-status">
              {!selectingEnd ? (
                <span className="status-text">Select start date (today or later)</span>
              ) : (
                <span className="status-text">
                  From: <strong>{formatDateForDisplay(tempDateRange.start)}</strong> - Select end date
                </span>
              )}
            </div>
          </div>
          
          <div className="calendar-container">
            <div className="calendar-month-header">
              {new Date(currentYear, currentMonth).toLocaleDateString('en-US', {
                month: 'long',
                year: 'numeric'
              })}
            </div>
            
            <div className="calendar-weekdays">
              {['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'].map(day => (
                <div key={day} className="weekday">{day}</div>
              ))}
            </div>
            
            <div className="calendar-dates">
              {dates.map((date, index) => {
                // Fix: Use proper date string formatting
                const dateString = formatDateString(date);
                const isCurrentMonth = date.getMonth() === currentMonth;
                const isToday = date.toDateString() === today.toDateString();
                const isPast = date < today;
                const isSelected = dateString === tempDateRange.start || dateString === tempDateRange.end;
                const isInRange = tempDateRange.start && tempDateRange.end && 
                  dateString >= tempDateRange.start && dateString <= tempDateRange.end;
                
                // Enhanced validation logic for editing
                let isDisabled = isSubmitting;
                let allowPastDate = false;
                
                // For editing: Check if original announcement had past dates
                if (initialData) {
                  const originalStartDate = new Date(initialData.startDate * 1000);
                  originalStartDate.setHours(0, 0, 0, 0);
                  allowPastDate = originalStartDate < today;
                }
                
                // Disable past dates unless editing announcement with original past dates
                if (isPast && !allowPastDate) {
                  isDisabled = true;
                }
                
                // If selecting end date, disable dates before start date
                if (selectingEnd && tempDateRange.start) {
                  const startDateObj = new Date(tempDateRange.start + 'T00:00:00');
                  const currentDateObj = new Date(dateString + 'T00:00:00');
                  isDisabled = isDisabled || currentDateObj < startDateObj;
                }
                
                return (
                  <button
                    key={index}
                    type="button"
                    className={`calendar-date ${!isCurrentMonth ? 'other-month' : ''} 
                      ${isToday ? 'today' : ''} 
                      ${isPast ? 'past' : ''} 
                      ${isSelected ? 'selected' : ''} 
                      ${isInRange ? 'in-range' : ''}
                      ${isDisabled ? 'disabled' : ''}`}
                    onClick={() => !isDisabled && handleDateSelect(dateString)}
                    disabled={isDisabled}
                    title={
                      isPast && !allowPastDate ? 'Past dates are not allowed for new announcements' :
                      (isPast && allowPastDate ? 'Past date allowed - original announcement was scheduled in the past' : '') ||
                      (selectingEnd && tempDateRange.start && new Date(dateString + 'T00:00:00') < new Date(tempDateRange.start + 'T00:00:00')) ? 
                      'End date must be after start date' : ''
                    }
                  >
                    {date.getDate()}
                  </button>
                );
              })}
            </div>
          </div>
          
          <div className="date-picker-actions">
            <button 
              type="button" 
              onClick={cancelDateSelection}
              className="cancel-date-btn"
              disabled={isSubmitting}
            >
              Cancel
            </button>
            {tempDateRange.start && tempDateRange.end && (
              <button 
                type="button" 
                onClick={confirmDateRange}
                className="confirm-date-btn"
                disabled={isSubmitting}
              >
                Confirm Range
              </button>
            )}
          </div>
        </div>
      </div>
    );
  };

  // Enhanced form validation with timezone fix
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

    // Enhanced date validation with announcement creation date consideration
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    
    const startDate = new Date(formData.dateRange.start + 'T00:00:00');
    const endDate = new Date(formData.dateRange.end + 'T00:00:00');

    // For editing: Allow past dates if the announcement was originally created with past dates
    if (initialData) {
      const originalStartDate = new Date(initialData.startDate * 1000);
      originalStartDate.setHours(0, 0, 0, 0);
      
      // If original start date was in the past, allow editing with past dates
      if (originalStartDate < today) {
        console.log('Allowing past date editing for announcement originally created with past dates');
      } else {
        // If original was future date, still enforce no past dates for new dates
        if (startDate < today) {
          alert('Start date cannot be in the past for announcements originally scheduled for future dates');
          return;
        }
      }
    } else {
      // For new announcements: Start date must be today or later
      if (startDate < today) {
        alert('Start date cannot be in the past');
        return;
      }
    }

    // End date validation remains the same
    if (endDate < startDate) {
      alert('End date must be after start date');
      return;
    }

    const startTimestamp = convertDateToTimestamp(formData.dateRange.start);
    const endTimestamp = convertDateToTimestamp(formData.dateRange.end);

    const submitData = new FormData();
    submitData.append('title', formData.title);
    submitData.append('startDate', startTimestamp);
    submitData.append('endDate', endTimestamp);
    
    if (initialData) {
      submitData.append('oldIpfsHash', initialData.ipfsHash);
      // Only append file if one is selected
      if (formData.file) {
        submitData.append('file', formData.file);
      }
    } else {
      // For new announcement, file is required
      submitData.append('file', formData.file);
    }

    setIsSubmitting(true);
    try {
      await onSave(submitData);
    } finally {
      setIsSubmitting(false);
    }
  };

  // Handle overlay click to close modal
  const handleOverlayClick = (e) => {
    if (e.target === e.currentTarget && !isSubmitting) {
      onClose();
    }
  };

  // Handle zoom functionality
  const handleImageZoom = (imageUrl, altText) => {
    setZoomedMedia({ url: imageUrl, type: 'image', alt: text });
  };

  const handlePdfZoom = (pdfUrl, title) => {
    setZoomedMedia({ url: pdfUrl, type: 'pdf', title: title });
  };

  const closeZoom = () => {
    setZoomedMedia(null);
  };

  // Handle escape key to close zoom
  useEffect(() => {
    const handleEscapeKey = (event) => {
      if (event.key === 'Escape' && zoomedMedia) {
        closeZoom();
      }
    };

    document.addEventListener('keydown', handleEscapeKey);
    return () => {
      document.removeEventListener('keydown', handleEscapeKey);
    };
  }, [zoomedMedia]);

  const renderZoomModal = () => {
    if (!zoomedMedia) return null;

    return (
      <div 
        className="zoom-modal-overlay"
        onClick={closeZoom}
        style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          backgroundColor: 'rgba(0, 0, 0, 0.9)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 2000,
          padding: '20px'
        }}
      >
        <div 
          onClick={(e) => e.stopPropagation()}
          style={{
            position: 'relative',
            maxWidth: '95vw',
            maxHeight: '95vh',
            backgroundColor: 'white',
            borderRadius: '8px',
            overflow: 'hidden'
          }}
        >
          <button
            onClick={closeZoom}
            style={{
              position: 'absolute',
              top: '10px',
              right: '10px',
              background: 'rgba(0, 0, 0, 0.7)',
              color: 'white',
              border: 'none',
              borderRadius: '50%',
              width: '32px',
              height: '32px',
              cursor: 'pointer',
              zIndex: 2001,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontSize: '18px'
            }}
            title="Close (Press Esc)"
          >
            ×
          </button>
          
          {zoomedMedia.type === 'image' ? (
            <img
              src={zoomedMedia.url}
              alt={zoomedMedia.alt}
              style={{
                maxWidth: '95vw',
                maxHeight: '95vh',
                objectFit: 'contain',
                display: 'block'
              }}
            />
          ) : (
            <iframe
              src={zoomedMedia.url}
              title={zoomedMedia.title}
              style={{
                width: '90vw',
                height: '90vh',
                border: 'none',
                borderRadius: '8px'
              }}
            />
          )}
        </div>
      </div>
    );
  };

  // Add this handler:
  const handleMediaFileChange = (selectedFile) => {
    setFormData((prev) => ({ ...prev, file: selectedFile }));
  };

  return (
    <>
      <div className="modal-overlay" onClick={handleOverlayClick}>
        <div className="modal-content" onClick={(e) => e.stopPropagation()}>
          <div className="modal-header">
            <h3>{initialData ? "Edit Announcement" : "Add New Announcement"}</h3>
            <button 
              onClick={onClose} 
              className="close-btn"
              disabled={isSubmitting}
            >
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
                disabled={isSubmitting}
              />
            </div>

            {/* Use the new component here */}
            <MediaUpload
              initialFileCid={initialData?.fileCid}
              initialFileType={initialData?.mediaType}
              onFileChange={handleMediaFileChange}
              disabled={isSubmitting}
            />

            <div className="form-group">
              <label>Date Range</label>
              <div className="date-range-input-container">
                <button
                  type="button"
                  className="date-range-input"
                  onClick={handleDateRangeClick}
                  disabled={isSubmitting}
                >
                  {formData.dateRange.start && formData.dateRange.end ? (
                    <span>
                      {formatDateForDisplay(formData.dateRange.start)} - {formatDateForDisplay(formData.dateRange.end)}
                    </span>
                  ) : (
                    <span className="placeholder">Click to select date range</span>
                  )}
                </button>
              </div>
            </div>
            
            <div className="modal-actions">
              <button 
                type="button" 
                onClick={onClose} 
                className="cancel-btn"
                disabled={isSubmitting}
              >
                Cancel
              </button>
              <button 
                type="submit" 
                className="save-btn"
                disabled={isSubmitting}
              >
                {isSubmitting ? (
                  <>
                    {initialData ? "Saving..." : "Adding..."}
                  </>
                ) : (
                  initialData ? "Save Changes" : "Add Announcement"
                )}
              </button>
            </div>
          </form>
        </div>
      </div>
      
      {/* Render date picker modal */}
      {renderDateRangePicker()}
      
      {/* Render zoom modal */}
      {renderZoomModal()}
    </>
  );
}

// ExportReportModal moved to top-level, outside of AdminDashboard
function ExportReportModal({ open, onClose, onExport }) {
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");
  const [isExporting, setIsExporting] = useState(false);

  const handleExport = async () => {
    if (!startDate || !endDate) {
      alert("Please select both start and end dates.");
      return;
    }
    setIsExporting(true);
    await onExport(startDate, endDate);
    setIsExporting(false);
    onClose();
  };

  if (!open) return null;

  return (
    <div className="modal-overlay" onClick={e => e.target === e.currentTarget && !isExporting && onClose()}>
      <div className="modal-content" onClick={e => e.stopPropagation()}>
        <div className="modal-header">
          <h3>Export Booking Report</h3>
          <button className="close-btn" onClick={onClose} disabled={isExporting}>
            <X size={20} />
          </button>
        </div>
        <div className="announcement-form">
          <div className="form-group">
            <label>Start Date</label>
            <input
              type="date"
              value={startDate}
              onChange={e => setStartDate(e.target.value)}
              disabled={isExporting}
            />
          </div>
          <div className="form-group">
            <label>End Date</label>
            <input
              type="date"
              value={endDate}
              onChange={e => setEndDate(e.target.value)}
              disabled={isExporting}
            />
          </div>
        </div>
        <div className="modal-actions">
          <button className="cancel-btn" onClick={onClose} disabled={isExporting}>
            Cancel
          </button>
          <button className="save-btn" onClick={handleExport} disabled={isExporting || !startDate || !endDate}>
            {isExporting ? "Exporting..." : "Export"}
          </button>
        </div>
      </div>
    </div>
  );
}

export default function AdminDashboard() {
  const { web3Auth } = useWeb3Auth();
  const navigate = useNavigate();

  const [userEmail, setUserEmail] = useState('');
  const [toast, setToast] = useState({ msg: "", type: "success" });
  const [activeTab, setActiveTab] = useState('dashboard');

  const [announcements, setAnnouncements] = useState([]);
  const [announcementsLoading, setAnnouncementsLoading] = useState(false);
  const [deletingAnnouncementId, setDeletingAnnouncementId] = useState(null);

  // Add system logs state
  const [systemLogs, setSystemLogs] = useState([]);
  const [systemLogsLoading, setSystemLogsLoading] = useState(false);

  // New state for bookings
  const [bookings, setBookings] = useState([]);
  const [bookingsLoading, setBookingsLoading] = useState(false);
  const [currentBookingsPage, setCurrentBookingsPage] = useState(1);
  const BOOKINGS_PER_PAGE = 3;

  // Calculate paginated bookings
  const totalBookings = bookings.length;
  const totalPages = Math.ceil(totalBookings / BOOKINGS_PER_PAGE);
  const paginatedBookings = bookings.slice(
    (currentBookingsPage - 1) * BOOKINGS_PER_PAGE,
    currentBookingsPage * BOOKINGS_PER_PAGE
  );

  useEffect(() => {
    // 1. Get user info
    if (web3Auth) {
      web3Auth.getUserInfo().then(info => setUserEmail(info.email));
    }

    // 2. Load Announcements
    (async () => {
      if (!announcementsLoading) {
        setAnnouncementsLoading(true);
        try {
          const res = await authFetch("/api/admin/get-announcements");
          if (!res.ok) {
            const errorText = await res.text();
            throw new Error(errorText || "Failed to load announcements");
          }
          const data = await res.json();
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
      }
    })();

    // 3. Load Approved Bookings
    (async () => {
      setBookingsLoading(true);
      try {
        const res = await authFetch("/api/admin/bookings/all");
        if (!res.ok) throw new Error(await res.text() || "Failed to load bookings");
        const { data } = await res.json();
        const approved = (data || []).filter(
          b => b.status === "APPROVED" || b.status === 0 || b.status === "0"
        );
        setBookings(approved);
      } catch (err) {
        setBookings([]);
        setToast({ msg: err.message, type: "error" });
      } finally {
        setBookingsLoading(false);
      }
    })();
  }, [web3Auth]); // Only runs on mount and when web3Auth changes

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
    const start = formatDate(startDate * 1000); 
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
  }, []); 

  useEffect(() => {
    loadSystemLogs();
  }, );

  const systemLogsRef = useRef([]);

  // Load system logs from backend with improved error handling and refresh
  const loadSystemLogs = useCallback(async (forceRefresh = false) => {
    if (systemLogsLoading && !forceRefresh) {
      console.log('System logs already loading, skipping duplicate call');
      return;
    }
    
    setSystemLogsLoading(true);
    try {
      // Add timestamp to prevent caching
      const timestamp = new Date().getTime();
      const res = await authFetch(`${import.meta.env.VITE_BACKEND_URL}/logs?t=${timestamp}`);
      
      if (!res.ok) {
        throw new Error(`HTTP error! status: ${res.status}`);
      }
      
      const data = await res.json();
      
      // Sort by timestamp descending to get latest first
      const sortedData = data.sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp));
      
      // Transform logs for dashboard display (show only latest 4 for clean UI)
      const transformedLogs = sortedData.slice(0, 4).map((log, index) => ({
        id: log.ipfsHash || `event-${log.timestamp}-${index}`,
        message: formatLogMessage(log),
        type: getLogTypeFromAction(log.action),
        status: getLogStatusFromAction(log.action),
        timestamp: log.timestamp,
        action: log.action,
        originalLog: log,
        isNew: false // Track new logs for animation
      }));
      
      // Use ref for previous logs
      const existingIds = systemLogs.map(log => log.id);
      const newLogsIds = transformedLogs.map(log => log.id);
      const hasNewLogs = !existingIds.every(id => newLogsIds.includes(id)) ||
        !newLogsIds.every(id => existingIds.includes(id));
      if (hasNewLogs) {
        transformedLogs.forEach(log => {
          log.isNew = !existingIds.includes(log.id);
        });
      }
      setSystemLogs(transformedLogs);
      console.log(`Updated system logs: ${transformedLogs.length} entries at ${new Date().toLocaleTimeString()}`);
      
    } catch (err) {
      console.error('Error loading system logs:', err);
      if (systemLogsRef.current.length === 0) setSystemLogs([]);
      setToast({ msg: 'Failed to load latest system logs', type: 'error' });
    } finally {
      setSystemLogsLoading(false);
    }
  }, ); 

  // Enhanced format log message with better time formatting
  const formatLogMessage = (log) => {
    const timeAgo = getTimeAgo(log.timestamp);
    const roleInfo = log.role && log.role !== '-' ? ` (${log.role})` : '';
    
    return `${log.action}${roleInfo} • ${timeAgo}`;
  };

  // Enhanced time ago calculation with more precision
  const getTimeAgo = (timestamp) => {
    const now = new Date();
    const logTime = new Date(timestamp);
    const diffMs = now - logTime;
    const diffSecs = Math.floor(diffMs / 1000);
    const diffMins = Math.floor(diffMs / (1000 * 60));
    const diffHours = Math.floor(diffMs / (1000 * 60 * 60));
    const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));

    if (diffSecs < 30) return 'just now';
    if (diffSecs < 60) return `${diffSecs}s ago`;
    if (diffMins < 60) return `${diffMins}m ago`;
    if (diffHours < 24) return `${diffHours}h ago`;
    return `${diffDays}d ago`;
  };

  // Determine log type from action
  const getLogTypeFromAction = (action) => {
    const actionLower = action.toLowerCase();
    
    if (actionLower.includes('booking')) return 'booking';
    if (actionLower.includes('announcement')) return 'admin';
    if (actionLower.includes('user') || actionLower.includes('banned')) return 'admin';
    if (actionLower.includes('facility') || actionLower.includes('court')) return 'admin';
    if (actionLower.includes('requested') || actionLower.includes('details')) return 'form';
    
    return 'admin';
  };

  // Determine log status from action to match SystemLogs action categories
  const getLogStatusFromAction = (action) => {
    const actionLower = action.toLowerCase();
    
    if (actionLower.includes('created') || actionLower.includes('added')) {
      return 'create';
    }
    
    if (actionLower.includes('updated') || actionLower.includes('modified') || 
        actionLower.includes('banned') || actionLower.includes('unbanned')) {
      return 'update';
    }
    
    if (actionLower.includes('deleted') || actionLower.includes('removed')) {
      return 'delete';
    }
    
    if (actionLower.includes('requested') || actionLower.includes('details')) {
      return 'read';
    }
    
    return 'read';
  };

  // Updated status class mapping to match SystemLogs
  const getLogStatusClass = (status) => {
    switch(status) {
      case 'delete': return 'log-delete';
      case 'update': return 'log-update';
      case 'create': return 'log-create';
      case 'read': return 'log-read';
      default: return 'log-read';
    }
  };

  // Fetch approved bookings only
  const loadApprovedBookings = useCallback(async () => {
    setBookingsLoading(true);
    try {
      const res = await authFetch("/api/admin/bookings/all");
      if (!res.ok) throw new Error(await res.text() || "Failed to load bookings");
      const { data } = await res.json();

      // Filter for APPROVED bookings (status === "APPROVED" or status === 0)
      const approved = (data || []).filter(
        b => b.status === "APPROVED" || b.status === 0 || b.status === "0"
      );
      setBookings(approved);
    } catch (err) {
      setBookings([]);
      setToast({ msg: err.message, type: "error" });
    } finally {
      setBookingsLoading(false);
    }
  }, []);


  const handleDeleteAnnouncement = async (id) => {
    if (!confirm('Are you sure you want to delete this announcement?')) {
      return;
    }

    setDeletingAnnouncementId(id);
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
    } finally {
      setDeletingAnnouncementId(null);
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

      // Add old title for comparison in backend
      if (editAnnouncement && editAnnouncement.title) {
        formData.append('oldTitle', editAnnouncement.title);
      }

      const res = await authFetch("/api/admin/update-announcement", {
        method: "PUT",
        body: formData,
      });

      if (!res.ok) {
        const errText = await res.text();
        throw new Error(errText || "Update failed");
      }
      
      const { message } = await res.json();
      setEditAnnouncement(null);
      setToast({ msg: message, type: "success" });
      await loadAnnouncements();
    } catch (err) {
      console.error("Edit announcement failed:", err);
      setToast({ msg: err.message, type: "error" });
    }
  };

  const openRejectModal = async (booking) => {
    if (!booking) return;
    const reason = window.prompt("Please enter the reason for rejection:");
    if (reason && reason.trim()) {
      await handleConfirmReject(reason.trim(), booking);
    }
  };

  // Handler for confirming reject 
  const handleConfirmReject = async (reason, booking) => {
    if (!booking) return;
    try {
      const res = await authFetch(`/api/admin/bookings/${booking.ipfsHash}/reject`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ reason })
      });
      if (!res.ok) {
        const errText = await res.text();
        throw new Error(errText || "Failed to reject booking");
      }
      setToast({ msg: "Booking rejected", type: "success" });
      await loadApprovedBookings();
    } catch (err) {
      setToast({ msg: err.message, type: "error" });
    }
  };

  const [showExportModal, setShowExportModal] = useState(false);

  const handleExportReport = async (startDate, endDate) => {
    try {
      // Fetch all bookings from backend
      const res = await authFetch("/api/admin/bookings/all");
      if (!res.ok) throw new Error(await res.text() || "Failed to fetch bookings");
      const { data } = await res.json();

      // Filter bookings by date range (inclusive)
      const start = new Date(startDate + "T00:00:00").getTime() / 1000;
      const end = new Date(endDate + "T23:59:59").getTime() / 1000;
      const filtered = data.filter(
        b => Number(b.startTime) >= start && Number(b.endTime) <= end
      );

      // Prepare data for XLSX
      const sheetData = [
        ["Owner", "Facility", "Court", "Start Time", "End Time", "Status", "IPFS Hash"],
        ...filtered.map(b => [
          b.owner,
          b.facilityName,
          b.courtName,
          new Date(Number(b.startTime) * 1000).toLocaleString("en-MY", { timeZone: "Asia/Kuala_Lumpur" }),
          new Date(Number(b.endTime) * 1000).toLocaleString("en-MY", { timeZone: "Asia/Kuala_Lumpur" }),
          STATUS_MAP[b.status] || b.status,
          b.ipfsHash
        ])
      ];

      const ws = XLSX.utils.aoa_to_sheet(sheetData);
      const wb = XLSX.utils.book_new();
      XLSX.utils.book_append_sheet(wb, ws, "Bookings");
      XLSX.writeFile(wb, `booking_report_${startDate}_to_${endDate}.xlsx`);
      setToast({ msg: "Report exported successfully", type: "success" });
    } catch (err) {
      setToast({ msg: err.message, type: "error" });
    }
  };

  useEffect(() => {
    if (toast.msg) {
      const timer = setTimeout(() => {
        setToast({ msg: "", type: "success" });
      }, 3000);
      return () => clearTimeout(timer);
    }
  }, [toast.msg]);

  return (
    <>
      <div className="admin-dashboard">
        <Navbar activeTab={activeTab} setActiveTab={setActiveTab} />
        <div className="dashboard-content">
          <div className="content-grid">
            {/* System Logs Section */}
            <div className="system-logs-section">
              <div className="section-header">
                <h3>Recent System Events</h3>
                <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
                  <button
                    className="icon-link-btn"
                    onClick={() => navigate("/logs")}
                    aria-label="Go to Logs"
                    style={{ background: "none", border: "none", cursor: "pointer" }}
                  >
                    <ExternalLink size={16} />
                  </button>
                </div>
              </div>
              
              <div className="logs-container">
                {systemLogs.length === 0 ? (
                  <div style={{ 
                    display: "flex",
                    flexDirection: "column",
                    justifyContent: "center",
                    alignItems: "center",
                    minHeight: "160px",
                    color: "#888",
                    fontStyle: "italic"
                  }}>
                    {systemLogsLoading ? (
                      <>
                        <Spinner />
                      </>
                    ) : (
                      <>
                      </>
                    )}
                  </div>
                ) : (
                  <div className="logs-list">
                    {systemLogs.map((log, index) => (
                      <div 
                        key={log.id} 
                        className={`log-item ${getLogStatusClass(log.status)} ${log.isNew ? 'entering' : ''}`}
                        style={{
                          animationDelay: `${index * 0.1}s`,
                          opacity: log.isNew ? 0 : 1,
                          transform: log.isNew ? 'translateY(-10px)' : 'translateY(0)',
                          animation: log.isNew ? 'slideIn 0.4s ease-out forwards' : 'none'
                        }}
                      >
                        <div className="log-content">
                          <span className="log-message">{log.message}</span>
                          {log.isNew && (
                            <div style={{ 
                              fontSize: '10px', 
                              color: '#dc2626',
                              fontWeight: '600',
                              marginLeft: '8px',
                              backgroundColor: 'rgba(255, 55, 255, 0.8)',
                              padding: '2px 6px',
                              borderRadius: '12px'
                            }}>
                              NEW
                            </div>
                          )}
                        </div>
                      </div>
                    ))}
                    
                    <div style={{
                      textAlign: 'center',
                      fontSize: '11px',
                      color: '#9ca3af',
                      padding: '12px 8px 8px 8px',
                      borderTop: '1px solid #f3f4f6',
                      marginTop: 'auto',
                      display: 'flex',
                      justifyContent: 'space-between',
                      alignItems: 'center'
                    }}>
                    </div>
                  </div>
                )}
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
                    disabled={announcementsLoading}
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
                            disabled={deletingAnnouncementId === announcement.id}
                          >
                            <Edit size={14} />
                            Edit
                          </button>
                          <button 
                            className="delete-btn"
                            onClick={() => handleDeleteAnnouncement(announcement.id)}
                            disabled={deletingAnnouncementId === announcement.id}
                          >
                            {deletingAnnouncementId === announcement.id ? (
                              <>
                                Deleting...
                              </>
                            ) : (
                              <>
                                <Trash2 size={14} />
                                Delete
                              </>
                            )}
                          </button>
                        </div>
                      </div>
                    ))
                  ))
                }
              </div>
            </div>
          </div>

          {/* System Approved Bookings Section */}
          <div className="bookings-section">
            <div className="section-header">
              <h3>System Approved Bookings</h3>
              <div className="header-actions">
                <span className="requests-count">{bookings.length} requests</span>
                <button
                  className="create-booking-btn"
                  onClick={() => navigate("/home")}
                >
                  <Plus size={16} />
                  Create Booking
                </button>
                <button className="export-btn" onClick={() => setShowExportModal(true)}>
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
            <div className="table-container bookings-table">
              <div className="table-header">
                <span>Booking Receipt (IPFS)</span>
                <span>Owner</span>
                <span>Facility</span>
                <span>Court</span>
                <span>Time</span>
                <span>Action</span>
              </div>
              {bookingsLoading ? (
                <div
                  style={{
                    textAlign: "center",
                    padding: "2rem",
                    color: "#888",
                    gridColumn: "1 / -1",
                  }}
                >
                  <Spinner />
                </div>
              ) : paginatedBookings.length === 0 ? (
                <div
                  style={{
                    textAlign: "center",
                    padding: "2rem",
                    color: "#888",
                    gridColumn: "1 / -1",
                  }}
                >
                  No approved bookings found
                </div>
              ) : (
                paginatedBookings.map((booking) => (
                  <div key={booking.ipfsHash || booking.id} className="table-row">
                    <span>
                      {booking.ipfsHash ? (
                        <a
                          href={getIpfsGatewayUrl(booking.ipfsHash)}
                          target="_blank"
                          rel="noopener noreferrer"
                          style={{ color: "#3b82f6", textDecoration: "underline" }}
                        >
                          {booking.ipfsHash.slice(0, 10)}...{booking.ipfsHash.slice(-6)}
                        </a>
                      ) : (
                        "-"
                      )}
                    </span>
                    <span>{abbreviateAddress(booking.owner || booking.userAddress)}</span>
                    <span>{booking.facilityName}</span>
                    <span>{booking.courtName}</span>
                    <span>{formatBookingTimeGmt8(booking.startTime, booking.endTime)}</span>
                    <span>
                      <button
                        className="reject-btn"
                        style={{
                          background: "#ef4444",
                          color: "#fff",
                          border: "none",
                          borderRadius: "4px",
                          padding: "4px 10px",
                          cursor: "pointer",
                          fontSize: "13px",
                          fontWeight: 500,
                          marginLeft: "4px"
                        }}
                        onClick={() => openRejectModal(booking)}
                      >
                        Reject
                      </button>
                    </span>
                  </div>
                ))
              )}
            </div>
            {/* Pagination Controls */}
            {totalPages > 1 && (
              <div style={{ display: "flex", justifyContent: "center", margin: "1rem 0" }}>
                <button
                  onClick={() => setCurrentBookingsPage((p) => Math.max(1, p - 1))}
                  disabled={currentBookingsPage === 1}
                  style={{ marginRight: 8 }}
                >
                  Prev
                </button>
                <span style={{ margin: "0 8px" }}>
                  Page {currentBookingsPage} of {totalPages}
                </span>
                <button
                  onClick={() => setCurrentBookingsPage((p) => Math.min(totalPages, p + 1))}
                  disabled={currentBookingsPage === totalPages}
                >
                  Next
                </button>
              </div>
            )}
          </div>
        </div>

        <Toast
          message={toast.msg}
          type={toast.type}
          onClose={() => setToast({ msg: "", type: "success" })}
        />
      </div>

      {/* Announcement Modals */}
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

      {/* Export Report Modal */}
      <ExportReportModal
        open={showExportModal}
        onClose={() => setShowExportModal(false)}
        onExport={handleExportReport}
      />
    </>
  );
}

const STATUS_MAP = {
  0: "APPROVED",
  1: "REJECTED",
  2: "COMPLETED",
  3: "CANCELLED",
  "0": "APPROVED",
  "1": "REJECTED",
  "2": "COMPLETED",
  "3": "CANCELLED",
};

// Utility to generate Pinata gateway URL for an IPFS hash
const getIpfsGatewayUrl = (ipfsHash) => {
  if (!ipfsHash) return "#";
  return `https://gateway.pinata.cloud/ipfs/${ipfsHash}`;
};

// Utility to abbreviate Ethereum or user addresses for display
const abbreviateAddress = (address) => {
  if (!address || typeof address !== "string") return "-";
  if (address.length <= 10) return address;
  return `${address.slice(0, 6)}...${address.slice(-4)}`;
};

// Utility to format booking time in GMT+8 (Asia/Kuala_Lumpur) with line break
const formatBookingTimeGmt8 = (startTime, endTime) => {
  if (!startTime || !endTime) return "-";
  const start = new Date(Number(startTime) * 1000);
  const end = new Date(Number(endTime) * 1000);
  const dateOptions = {
    timeZone: "Asia/Kuala_Lumpur",
    year: "numeric",
    month: "short",
    day: "numeric"
  };
  const timeOptions = {
    timeZone: "Asia/Kuala_Lumpur",
    hour: "2-digit",
    minute: "2-digit",
    hour12: true
  };
  return (
    <>
      {start.toLocaleDateString("en-GB", dateOptions)}
      <br />
      {start.toLocaleTimeString("en-GB", timeOptions).toLowerCase()} - {end.toLocaleTimeString("en-GB", timeOptions).toLowerCase()}
    </>
  );
};