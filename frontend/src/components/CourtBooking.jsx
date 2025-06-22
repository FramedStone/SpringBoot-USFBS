import React, { useState, useEffect } from 'react';
import { MapPin, Calendar, Clock, ArrowLeft } from 'lucide-react';
import { authFetch } from '@utils/authFetch';
import Toast from '@components/Toast';
import Spinner from '@components/Spinner';
import '@styles/CourtBooking.css';

const CourtBooking = ({ facilityName, onBack }) => {
  const [facilityDetails, setFacilityDetails] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedDate, setSelectedDate] = useState(
    new Date().toISOString().split('T')[0]
  );
  const [courtTimeRanges, setCourtTimeRanges] = useState({});
  const [dynamicTimeSlots, setDynamicTimeSlots] = useState([]);
  const [toast, setToast] = useState({ message: '', type: 'success', show: false });

  useEffect(() => {
    if (facilityName) {
      fetchFacilityDetails();
    }
  }, [facilityName]);

  const showToast = (message, type = 'error') => {
    setToast({ message, type, show: true });
    setTimeout(() => {
      setToast(prev => ({ ...prev, show: false }));
    }, 3000);
  };

  const fetchFacilityDetails = async () => {
    try {
      setLoading(true);
      setError(null);
      
      const backendUrl = import.meta.env.VITE_BACKEND_URL || 'http://localhost:8080';
      const response = await authFetch(
        `${backendUrl}/api/user/sport-facilities/${encodeURIComponent(facilityName)}/details`
      );
      
      if (!response.ok) {
        if (response.status === 404) {
          throw new Error('Sport facility not found');
        } else if (response.status === 403) {
          throw new Error('Access denied. Please check your authentication status.');
        } else if (response.status === 401) {
          throw new Error('Authentication required. Please log in again.');
        } else {
          throw new Error(`Server error: ${response.status}`);
        }
      }
      
      const result = await response.json();
      
      if (result.success) {
        setFacilityDetails(result.data);
        console.log(`Fetched details for facility: ${facilityName}`);
        
        if (result.data.courts) {
          await loadCourtTimeRanges(result.data.courts);
        }
      } else {
        throw new Error(result.error || 'Failed to fetch facility details');
      }
      
    } catch (err) {
      console.error('Error fetching facility details:', err);
      setError(err.message);
      showToast(`Failed to load facility details: ${err.message}`, 'error');
    } finally {
      setLoading(false);
    }
  };

  const loadCourtTimeRanges = async (courts) => {
    if (!courts || courts.length === 0) {
      setCourtTimeRanges({});
      setDynamicTimeSlots([]);
      return;
    }

    try {
      const timeRanges = {};
      let allTimeSlots = new Set();

      for (const court of courts) {
        try {
          const backendUrl = import.meta.env.VITE_BACKEND_URL || 'http://localhost:8080';
          const res = await authFetch(
            `${backendUrl}/api/admin/sport-facilities/${encodeURIComponent(facilityName)}/courts/${encodeURIComponent(court.name)}/time-range-with-bookings`
          );

          if (res.ok) {
            const data = await res.json();
            if (data.success) {
              timeRanges[court.name] = {
                ...data.data,
                status: data.data.status || "OPEN",
                available: data.data.available !== false,
                bookedTimeSlots: data.data.bookedTimeSlots || [],
                timeSlotAvailability: data.data.timeSlotAvailability || {},
              };

              const courtSlots = generateTimeSlots(
                data.data.earliestTime,
                data.data.latestTime
              );
              courtSlots.forEach((slot) => allTimeSlots.add(slot));
            }
          } else {
            timeRanges[court.name] = {
              earliestTime: court.earliestTime,
              latestTime: court.latestTime,
              earliestTimeStr: formatTime(court.earliestTime),
              latestTimeStr: formatTime(court.latestTime),
              status: getStatusString(court.status),
              available: court.status === 0,
              bookedTimeSlots: [],
              timeSlotAvailability: {},
            };

            const courtSlots = generateTimeSlots(court.earliestTime, court.latestTime);
            courtSlots.forEach((slot) => allTimeSlots.add(slot));
          }
        } catch (err) {
          console.error(`Error loading time range for court ${court.name}:`, err);
          timeRanges[court.name] = {
            earliestTime: 28800,
            latestTime: 82800,
            earliestTimeStr: "08:00",
            latestTimeStr: "23:00",
            status: "ERROR",
            available: false,
            bookedTimeSlots: [],
            timeSlotAvailability: {},
          };
        }
      }

      setCourtTimeRanges(timeRanges);
      
      const sortedSlots = Array.from(allTimeSlots).sort();
      setDynamicTimeSlots(sortedSlots.length > 0 ? sortedSlots : [
        "08:00", "09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00",
        "16:00", "17:00", "18:00", "19:00", "20:00", "21:00", "22:00", "23:00"
      ]);

    } catch (err) {
      console.error("Error loading court time ranges:", err);
      showToast("Failed to load court time ranges", "error");
    }
  };

  const generateTimeSlots = (earliestTime, latestTime) => {
    const slots = [];
    const start = Math.floor(earliestTime / 3600);
    const end = Math.floor(latestTime / 3600);

    for (let hour = start; hour <= end; hour++) {
      slots.push(`${hour.toString().padStart(2, "0")}:00`);
    }
    return slots;
  };

  const formatTime = (seconds) => {
    if (!seconds && seconds !== 0) return 'N/A';
    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);
    return `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}`;
  };

  const getStatusBadgeClass = (status) => {
    if (typeof status === 'number') {
      switch (status) {
        case 0: return 'open';
        case 1: return 'closed';
        case 2: return 'maintenance';
        case 3: return 'booked';
        default: return 'unknown';
      }
    }
    return status ? status.toLowerCase() : 'unknown';
  };

  const getStatusString = (statusValue) => {
    const STATUS_MAP = {
      0: "OPEN",
      1: "CLOSED", 
      2: "MAINTENANCE",
      3: "BOOKED",
    };
    return STATUS_MAP[statusValue] || "OPEN";
  };

  const getStatusText = (status) => {
    if (typeof status === 'number') {
      return getStatusString(status);
    }
    return status || 'UNKNOWN';
  };

  const handleLocationClick = () => {
    if (facilityDetails?.location) {
      window.open(facilityDetails.location, '_blank');
    }
  };

  const handleBookTimeSlot = (courtName, timeSlot) => {
    console.log(`Booking court: ${courtName} at ${timeSlot} for date: ${selectedDate}`);
    // TODO: Implement booking logic
    showToast(`Booking ${courtName} at ${timeSlot} on ${selectedDate} will be implemented soon`, 'info');
  };

  const renderCourtTimeSlots = (courtName, status) => {
    const courtTimeRange = courtTimeRanges[courtName];

    if (!courtTimeRange) {
      return dynamicTimeSlots.map((time) => (
        <td key={time} className="court-time-slot loading">
        </td>
      ));
    }

    const actualStatus = courtTimeRange.status || status;
    const timeSlotAvailability = courtTimeRange.timeSlotAvailability || {};

    return dynamicTimeSlots.map((time) => {
      const timeHour = parseInt(time.split(":")[0]);
      const courtStartHour = Math.floor(courtTimeRange.earliestTime / 3600);
      const courtEndHour = Math.floor(courtTimeRange.latestTime / 3600);

      const isWithinRange = timeHour >= courtStartHour && timeHour <= courtEndHour;
      const slotStatus = timeSlotAvailability[time] || "UNAVAILABLE";

      const getSlotStyle = (status) => {
        switch (status) {
          case "AVAILABLE":
            return { backgroundColor: "#dcfce7", color: "#166534" };
          case "BOOKED":
            return { backgroundColor: "#fde68a", color: "#d97706", fontWeight: "bold" };
          case "MAINTENANCE":
            return { backgroundColor: "#fed7aa", color: "#ea580c", fontWeight: "bold" };
          case "CLOSED":
            return { backgroundColor: "#fecaca", color: "#dc2626", fontWeight: "bold" };
          default:
            return { backgroundColor: "#f3f4f6", color: "#6b7280" };
        }
      };

      const isBookable = isWithinRange && slotStatus === "AVAILABLE" && actualStatus === "OPEN";

      return (
        <td
          key={time}
          className={`court-time-slot ${slotStatus.toLowerCase()} ${isBookable ? 'bookable' : ''}`}
          style={getSlotStyle(slotStatus)}
          onClick={isBookable ? () => handleBookTimeSlot(courtName, time) : undefined}
          title={isBookable ? `Click to book ${courtName} at ${time}` : `${slotStatus} - ${time}`}
        >
        </td>
      );
    });
  };

  if (loading) {
    return (
      <div className="court-booking-container">
        <div className="court-booking-header">
          <button onClick={onBack} className="court-booking-back-btn">
            <ArrowLeft size={16} />
            Back
          </button>
        </div>
        <Spinner />
        <Toast
          message={toast.show ? toast.message : ''}
          type={toast.type}
          onClose={() => setToast(prev => ({ ...prev, show: false }))}
        />
      </div>
    );
  }

  if (error || !facilityDetails) {
    return (
      <div className="court-booking-container">
        <div className="court-booking-error">
          <button onClick={onBack} className="court-booking-back-btn">
            <ArrowLeft size={16} />
            Back to Facilities
          </button>
          <h3>Unable to Load Courts</h3>
          <p>{error || 'Facility details not found'}</p>
          <button onClick={fetchFacilityDetails} className="court-booking-retry-btn">
            Retry
          </button>
        </div>
        <Toast
          message={toast.show ? toast.message : ''}
          type={toast.type}
          onClose={() => setToast(prev => ({ ...prev, show: false }))}
        />
      </div>
    );
  }

  return (
    <div className="court-booking-container">
      <div className="court-booking-header">
        <div className="court-booking-header-left">
          <button onClick={onBack} className="court-booking-back-btn">
            <ArrowLeft size={16} />
            Back
          </button>
          <div className="court-booking-title-section">
            <h2 className="court-booking-title">{facilityDetails.name}</h2>
            {facilityDetails.location && (
              <button 
                onClick={handleLocationClick}
                className="court-booking-location-btn"
                title="View location"
              >
                <MapPin size={18} />
              </button>
            )}
          </div>
        </div>
        <div className="court-booking-date-picker">
          <input
            id="booking-date"
            type="date"
            value={selectedDate}
            onChange={(e) => setSelectedDate(e.target.value)}
            min={new Date().toISOString().split('T')[0]}
            className="court-booking-date-input"
          />
        </div>
      </div>

      <div className="court-booking-schedule-container">
        <div className="court-booking-table-wrapper">
          <table className="court-booking-schedule-table">
            <thead>
              <tr>
                <th className="court-name-header">Court</th>
                <th className="court-status-header">Status</th>
                <th className="court-time-range-header">Time Range</th>
                {dynamicTimeSlots.map((time) => (
                  <th key={time} className="time-slot-header">{time}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {facilityDetails.courts && facilityDetails.courts.length > 0 ? (
                facilityDetails.courts.map((court, index) => (
                  <tr key={index} className="court-booking-row">
                    <td className="court-name">Court {court.name}</td>
                    <td className="court-status">
                      <span className={`court-status-badge ${getStatusBadgeClass(court.status)}`}>
                        {getStatusText(court.status)}
                      </span>
                    </td>
                    <td className="court-time-range">
                      {courtTimeRanges[court.name] 
                        ? `${courtTimeRanges[court.name].earliestTimeStr} - ${courtTimeRanges[court.name].latestTimeStr}`
                        : `${formatTime(court.earliestTime)} - ${formatTime(court.latestTime)}`
                      }
                    </td>
                    {renderCourtTimeSlots(court.name, getStatusText(court.status))}
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan={dynamicTimeSlots.length + 3} className="court-booking-empty">
                    No courts available for this facility
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
        
        <div className="court-booking-legend">
          <div className="legend-item">
            <span className="legend-color available"></span>
            <span>Available (Click to Book)</span>
          </div>
          <div className="legend-item">
            <span className="legend-color booked"></span>
            <span>Booked</span>
          </div>
          <div className="legend-item">
            <span className="legend-color maintenance"></span>
            <span>Maintenance</span>
          </div>
          <div className="legend-item">
            <span className="legend-color closed"></span>
            <span>Closed</span>
          </div>
          <div className="legend-item">
            <span className="legend-color unavailable"></span>
            <span>Outside Operating Hours</span>
          </div>
        </div>
      </div>

      <Toast
        message={toast.show ? toast.message : ''}
        type={toast.type}
        onClose={() => setToast(prev => ({ ...prev, show: false }))}
      />
    </div>
  );
};

export default CourtBooking;