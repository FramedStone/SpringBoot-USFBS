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
  const [newFileType, setNewFileType] = useState(null);
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

  // Handle file selection and preview
  const handleFileChange = (e) => {
    const selectedFile = e.target.files[0];
    setFormData({ ...formData, file: selectedFile });

    // Clear previous preview
    if (newFilePreviewUrl) {
      URL.revokeObjectURL(newFilePreviewUrl);
      setNewFilePreviewUrl(null);
      setNewFileType(null);
    }

    if (selectedFile) {
      // Determine file type
      const fileType = selectedFile.type;
      if (fileType.startsWith('image/')) {
        setNewFileType('image');
        const previewUrl = URL.createObjectURL(selectedFile);
        setNewFilePreviewUrl(previewUrl);
      } else if (fileType.includes('pdf')) {
        setNewFileType('pdf');
        const previewUrl = URL.createObjectURL(selectedFile);
        setNewFilePreviewUrl(previewUrl);
      } else {
        setNewFileType('document');
        setNewFilePreviewUrl(null);
      }
    }
  };

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
    setZoomedMedia({ url: imageUrl, type: 'image', alt: altText });
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

  const renderCurrentMedia = () => {
    if (!currentMediaUrl || !initialData) return null;

    return (
      <div className="current-media-preview">
        <label style={{ fontSize: '14px', fontWeight: '500', color: '#374151', marginBottom: '8px', display: 'block' }}>
          Current File
        </label>
        <div style={{ 
          border: '1px solid #d1d5db', 
          borderRadius: '8px', 
          padding: '12px',
          backgroundColor: '#f9fafb'
        }}>
          {mediaType === 'image' ? (
            <div style={{ textAlign: 'center' }}>
              <img 
                src={currentMediaUrl} 
                alt="Current announcement media"
                style={{ 
                  maxWidth: '200px', 
                  maxHeight: '150px', 
                  objectFit: 'contain',
                  borderRadius: '4px',
                  marginBottom: '8px',
                  cursor: 'zoom-in'
                }}
                onClick={() => handleImageZoom(currentMediaUrl, 'Current announcement media')}
                title="Click to zoom"
              />
              <div style={{ fontSize: '12px', color: '#6b7280' }}>
                Current image file (Click to zoom)
              </div>
            </div>
          ) : mediaType === 'pdf' ? (
            <div style={{ textAlign: 'center' }}>
              <div 
                style={{ 
                  padding: '20px', 
                  backgroundColor: '#fee2e2', 
                  borderRadius: '4px',
                  marginBottom: '8px',
                  cursor: 'zoom-in'
                }}
                onClick={() => handlePdfZoom(currentMediaUrl, 'Current PDF')}
                title="Click to view fullscreen"
              >
                PDF Document
              </div>
              <div style={{ display: 'flex', gap: '8px', justifyContent: 'center', flexWrap: 'wrap' }}>
                <button
                  type="button"
                  onClick={() => handlePdfZoom(currentMediaUrl, 'Current PDF')}
                  style={{
                    padding: '4px 8px',
                    backgroundColor: '#3b82f6',
                    color: 'white',
                    border: 'none',
                    borderRadius: '4px',
                    fontSize: '12px',
                    cursor: 'pointer'
                  }}
                >
                  Zoom View
                </button>
                <a 
                  href={currentMediaUrl} 
                  target="_blank" 
                  rel="noopener noreferrer"
                  style={{ 
                    padding: '4px 8px',
                    backgroundColor: '#10b981',
                    color: 'white',
                    textDecoration: 'none',
                    borderRadius: '4px',
                    fontSize: '12px'
                  }}
                >
                  Open in Tab
                </a>
              </div>
            </div>
          ) : (
            <div style={{ textAlign: 'center' }}>
              <div style={{ 
                padding: '20px', 
                backgroundColor: '#e5e7eb', 
                borderRadius: '4px',
                marginBottom: '8px'
              }}>
                Document
              </div>
              <a 
                href={currentMediaUrl} 
                target="_blank" 
                rel="noopener noreferrer"
                style={{ 
                  color: '#3b82f6', 
                  textDecoration: 'none',
                  fontSize: '14px'
                }}
              >
                Download Current File
              </a>
            </div>
          )}
        </div>
      </div>
    );
  };

  const renderNewFilePreview = () => {
    if (!formData.file) return null;

    return (
      <div className="new-file-preview">
        <label style={{ fontSize: '14px', fontWeight: '500', color: '#374151', marginBottom: '8px', display: 'block' }}>
          New File Preview
        </label>
        <div style={{ 
          border: '1px solid #d1d5db', 
          borderRadius: '8px', 
          padding: '12px',
          backgroundColor: '#f0f9ff'
        }}>
          {newFileType === 'image' && newFilePreviewUrl ? (
            <div style={{ textAlign: 'center' }}>
              <img 
                src={newFilePreviewUrl} 
                alt="New file preview"
                style={{ 
                  maxWidth: '200px', 
                  maxHeight: '150px', 
                  objectFit: 'contain',
                  borderRadius: '4px',
                  marginBottom: '8px',
                  cursor: 'zoom-in'
                }}
                onClick={() => handleImageZoom(newFilePreviewUrl, 'New file preview')}
                title="Click to zoom"
              />
              <div style={{ fontSize: '12px', color: '#6b7280', marginBottom: '4px' }}>
                {formData.file.name} ({(formData.file.size / 1024 / 1024).toFixed(2)} MB)
              </div>
              <div style={{ fontSize: '10px', color: '#9ca3af' }}>
                Click image to zoom
              </div>
            </div>
          ) : newFileType === 'pdf' && newFilePreviewUrl ? (
            <div style={{ textAlign: 'center' }}>
              <div style={{ 
                padding: '20px', 
                backgroundColor: '#dbeafe', 
                borderRadius: '4px',
                marginBottom: '8px'
              }}>
                PDF Document (Preview)
              </div>
              <div style={{ fontSize: '12px', color: '#6b7280', marginBottom: '8px' }}>
                {formData.file.name} ({(formData.file.size / 1024 / 1024).toFixed(2)} MB)
              </div>
              <div style={{ marginBottom: '8px' }}>
                <iframe
                  src={newFilePreviewUrl}
                  style={{
                    width: '100%',
                    height: '200px',
                    border: '1px solid #d1d5db',
                    borderRadius: '4px'
                  }}
                  title="PDF Preview"
                />
              </div>
              <button
                type="button"
                onClick={() => handlePdfZoom(newFilePreviewUrl, formData.file.name)}
                style={{
                  padding: '6px 12px',
                  backgroundColor: '#3b82f6',
                  color: 'white',
                  border: 'none',
                  borderRadius: '4px',
                  fontSize: '12px',
                  cursor: 'pointer'
                }}
              >
                View Fullscreen
              </button>
            </div>
          ) : (
            <div style={{ textAlign: 'center' }}>
              <div style={{ 
                padding: '20px', 
                backgroundColor: '#dbeafe', 
                borderRadius: '4px',
                marginBottom: '8px'
              }}>
                Document (Selected)
              </div>
              <div style={{ fontSize: '12px', color: '#6b7280' }}>
                {formData.file.name} ({(formData.file.size / 1024 / 1024).toFixed(2)} MB)
              </div>
            </div>
          )}
        </div>
      </div>
    );
  };

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
            
            {/* Show current media when editing */}
            {initialData && renderCurrentMedia()}
            
            <div className="form-group">
              <label>
                {initialData ? 'New File' : 'File'}
                {initialData && (
                  <span style={{ fontSize: '12px', color: '#6b7280', fontWeight: 'normal' }}>
                    {' '}(Optional - leave empty to keep current file)
                  </span>
                )}
              </label>
              <input
                type="file"
                onChange={handleFileChange}
                accept=".pdf,.doc,.docx,.jpg,.jpeg,.png"
                required={!initialData}
                disabled={isSubmitting}
              />
              {initialData && (
                <div style={{ fontSize: '12px', color: '#6b7280', marginTop: '4px' }}>
                  Current file will be preserved if no new file is selected
                </div>
              )}
            </div>

            {/* Show new file preview when file is selected */}
            {renderNewFilePreview()}
            
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
};

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

  useEffect(() => {
    if (!web3Auth) return;
    web3Auth.getUserInfo()
      .then(info => setUserEmail(info.email))
      // .catch(err => console.error "Failed to fetch user info:", err);
  }, [web3Auth]);

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

  // Load system logs from backend with improved error handling and refresh
  const loadSystemLogs = useCallback(async (forceRefresh = false) => {
    // Skip if already loading unless it's a forced refresh
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
      
      // Check for new logs compared to current state
      const existingIds = systemLogs.map(log => log.id);
      const newLogsIds = transformedLogs.map(log => log.id);
      const hasNewLogs = !existingIds.every(id => newLogsIds.includes(id)) || 
                         !newLogsIds.every(id => existingIds.includes(id));
      
      if (hasNewLogs) {
        // Mark new logs for animation
        transformedLogs.forEach(log => {
          log.isNew = !existingIds.includes(log.id);
        });
      }
      
      setSystemLogs(transformedLogs);
      console.log(`Updated system logs: ${transformedLogs.length} entries at ${new Date().toLocaleTimeString()}`);
      
    } catch (err) {
      console.error('Error loading system logs:', err);
      if (systemLogs.length === 0) {
        setSystemLogs([]);
      }
      setToast({ msg: 'Failed to load latest system logs', type: 'warning' });
    } finally {
      setSystemLogsLoading(false);
    }
  }, [systemLogs]); // Include systemLogs for comparison

  // Enhanced system logs loading with animation support
  useEffect(() => {
    // Initial load
    loadSystemLogs(true);
    
    // Set up more frequent polling for real-time updates
    const interval = setInterval(() => {
      loadSystemLogs(true); // Force refresh on interval
    }, 15000); // Update every 15 seconds for more real-time feel
    
    return () => clearInterval(interval);
  }, []); // Empty dependency to run once

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

  // Load system logs on component mount
  useEffect(() => {
    loadSystemLogs();
    
    // Set up polling for real-time updates
    const interval = setInterval(loadSystemLogs, 30000); // Update every 30 seconds
    return () => clearInterval(interval);
  }, [loadSystemLogs]);

  const [bookings] = useState([
  ]);

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

  const handleRejectBooking = (id) => {
    console.log('Rejecting booking:', id);
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

  return (
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
                      <div style={{ marginTop: '12px', fontSize: '14px' }}>
                        Loading system events...
                      </div>
                    </>
                  ) : (
                    <>
                      <div style={{ fontSize: '16px', marginBottom: '8px' }}>
                        No recent events
                      </div>
                      <div style={{ fontSize: '12px', color: '#9ca3af' }}>
                        System events will appear here automatically
                      </div>
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
                            backgroundColor: 'rgba(255, 255, 255, 0.8)',
                            padding: '2px 6px',
                            borderRadius: '12px'
                          }}>
                            NEW
                          </div>
                        )}
                      </div>
                    </div>
                  ))}
                  
                  {/* Fixed footer with last update time */}
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
              <span className="requests-count">0 requests</span>
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
          <div className="table-container bookings-table">
            <div className="table-header">
              <span>Booking ID</span>
              <span>User ID</span>
              <span>Court</span>
              <span>Time</span>
              <span>Sport</span>
              <span>Action</span>
            </div>
            {bookings.length === 0 ? (
              <div style={{ 
                textAlign: "center", 
                padding: "2rem", 
                color: "#888",
                gridColumn: "1 / -1"
              }}>
                No approved bookings found
              </div>
            ) : (
              bookings.map(booking => (
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
              ))
            )}
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