import React, { useState, useEffect } from 'react';
import { MapPin, Calendar, Clock, ArrowLeft } from 'lucide-react';
import { authFetch } from '@utils/authFetch';
import Toast from '@components/Toast';
import Spinner from '@components/Spinner';
import '@styles/CourtBooking.css';
import { useNavigate } from 'react-router-dom';
import dayjs from "dayjs"; // Make sure to install dayjs or use native Date if preferred

const MAX_USER_BOOKINGS_PER_DAY = 3;

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
  const [allBookings, setAllBookings] = useState([]);
  const [userRole, setUserRole] = useState(""); 
  const [selectedSlots, setSelectedSlots] = useState(() => {
    // Persist selection in localStorage for Cart page
    const saved = localStorage.getItem('admin_cart_slots');
    return saved ? JSON.parse(saved) : [];
  });
  const [bookedTimeSlotsMap, setBookedTimeSlotsMap] = useState({}); // { courtName: [ { startTime, endTime, ... }, ... ] }
  const [userBookingCount, setUserBookingCount] = useState(0);
  const navigate = useNavigate();

  // fetch user role on mount
  useEffect(() => {
    const fetchRole = async () => {
      try {
        const backendUrl = import.meta.env.VITE_BACKEND_URL || "http://localhost:8080";
        const res = await authFetch(`${backendUrl}/api/auth/me`);
        if (res.ok) {
          const data = await res.json();
          setUserRole(data.role || "User");
        }
      } catch (err) {
        setUserRole("User");
      }
    };
    fetchRole();
  }, []);

  // Only fetch facility details after userRole is set
  useEffect(() => {
    if (facilityName && userRole) {
      fetchFacilityDetails();
    }
    // eslint-disable-next-line
  }, [facilityName, userRole]);

  // Only fetch court time ranges after userRole is set
  useEffect(() => {
    if (facilityName && selectedDate && userRole) {
      fetchCourtTimeRangesAndBookings();
    }
    // eslint-disable-next-line
  }, [facilityName, selectedDate, userRole]);

  // Fetch user's bookings for the selected date (for restriction)
  useEffect(() => {
    const fetchUserBookingsForDate = async () => {
      if (userRole !== "User") return;
      try {
        const backendUrl = import.meta.env.VITE_BACKEND_URL || "http://localhost:8080";
        const res = await authFetch(`${backendUrl}/api/user/bookings`);
        if (res.ok) {
          const data = await res.json();
          // Filter bookings for the selected date
          const count = (data.data || []).filter(b => {
            const bookingDate = dayjs.unix(Number(b.startTime)).format("YYYY-MM-DD");
            return bookingDate === selectedDate;
          }).length;
          setUserBookingCount(count);
        }
      } catch (err) {
        setUserBookingCount(0);
      }
    };
    if (userRole === "User" && selectedDate) {
      fetchUserBookingsForDate();
    }
  }, [userRole, selectedDate]);

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
      let response;
      if (userRole === "Admin") {
        response = await authFetch(
          `${backendUrl}/api/admin/${encodeURIComponent(facilityName)}/courts`
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
        const courtsResult = await response.json();
        if (!courtsResult.success) throw new Error(courtsResult.error || 'Facility courts error');
        const courts = courtsResult.data || [];
        setFacilityDetails({ name: facilityName, courts });
        if (courts.length > 0) {
          await loadCourtTimeRanges(courts);
        }
      } else {
        response = await authFetch(
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
          if (result.data.courts) {
            await loadCourtTimeRanges(result.data.courts);
          }
        } else {
          throw new Error(result.error || 'Failed to fetch facility details');
        }
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
      setBookedTimeSlotsMap({});
      return;
    }

    try {
      const timeRanges = {};
      let allTimeSlots = new Set();
      const bookedMap = {};

      for (const court of courts) {
        try {
          const backendUrl = import.meta.env.VITE_BACKEND_URL || 'http://localhost:8080';
          const courtRoute =
            userRole === "Admin"
              ? `/api/admin/${encodeURIComponent(facilityName)}/${encodeURIComponent(court.name)}/booked-timeslots`
              : `/api/user/${encodeURIComponent(facilityName)}/${encodeURIComponent(court.name)}/booked-timeslots`;

          const res = await authFetch(`${backendUrl}${courtRoute}`);

          if (res.ok) {
            const data = await res.json();
            // The backend returns { success, data: [ {startTime, endTime}, ... ] }
            // We want to store the array directly for this court
            bookedMap[court.name] = Array.isArray(data.data) ? data.data : [];

            // For time range, fallback to court's own earliest/latest if not available
            timeRanges[court.name] = {
              earliestTime: court.earliestTime,
              latestTime: court.latestTime,
              earliestTimeStr: formatTime(court.earliestTime),
              latestTimeStr: formatTime(court.latestTime),
              status: getStatusString(court.status),
              available: court.status === 0,
              bookedTimeSlots: bookedMap[court.name],
              timeSlotAvailability: {}, // TODO: support if needed
            };

            // Generate all time slots for this court
            const courtSlots = generateTimeSlots(court.earliestTime, court.latestTime);
            courtSlots.forEach((slot) => allTimeSlots.add(slot));
          } else {
            // fallback if error
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
            bookedMap[court.name] = [];
            const courtSlots = generateTimeSlots(court.earliestTime, court.latestTime);
            courtSlots.forEach((slot) => allTimeSlots.add(slot));
          }
        } catch (err) {
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
          bookedMap[court.name] = [];
        }
      }

      setCourtTimeRanges(timeRanges);
      setBookedTimeSlotsMap(bookedMap);

      const sortedSlots = Array.from(allTimeSlots).sort();
      setDynamicTimeSlots(
        sortedSlots.length > 0
          ? sortedSlots
          : [
              "08:00", "09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00",
              "16:00", "17:00", "18:00", "19:00", "20:00", "21:00", "22:00", "23:00"
            ]
      );
    } catch (err) {
      setCourtTimeRanges({});
      setDynamicTimeSlots([]);
      setBookedTimeSlotsMap({});
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

  const timeToSeconds = (timeString) => {
    const [hours, minutes] = timeString.split(":").map(Number);
    return hours * 3600 + minutes * 60;
  };

  const isSlotBooked = (courtName, slotSeconds) => {
    const bookedSlots = bookedTimeSlotsMap[courtName] || [];
    // Convert selectedDate + slotSeconds to UNIX timestamp
    const [year, month, day] = selectedDate.split('-').map(Number);
    const slotDate = new Date(year, month - 1, day, 0, 0, 0);
    const slotUnix = Math.floor(slotDate.getTime() / 1000) + slotSeconds;

    return bookedSlots.some(
      (b) =>
        Number(b.startTime) <= slotUnix &&
        slotUnix < Number(b.endTime)
    );
  };

  // Admin: handle slot selection
  const handleAdminSlotSelect = (courtName, timeSlot) => {
    const slotKey = `${facilityName}|${courtName}|${selectedDate}|${timeSlot}`;
    let updated = [...selectedSlots];
    if (updated.some(s => s.key === slotKey)) {
      updated = updated.filter(s => s.key !== slotKey);
    } else {
      updated.push({
        key: slotKey,
        facilityName,
        courtName,
        date: selectedDate,
        timeSlot,
      });
    }
    persistSelectedSlots(updated);
  };

  // User: handle slot selection (max 3 per day)
  const handleUserSlotSelect = (courtName, timeSlot) => {
    const slotKey = `${facilityName}|${courtName}|${selectedDate}|${timeSlot}`;
    let updated = [...selectedSlots];
    if (updated.some(s => s.key === slotKey)) {
      updated = updated.filter(s => s.key !== slotKey);
    } else {
      // Only allow up to 3 bookings per day
      const selectedForDay = updated.filter(s => s.date === selectedDate).length;
      if (selectedForDay + userBookingCount >= MAX_USER_BOOKINGS_PER_DAY) {
        showToast(`You can only select up to ${MAX_USER_BOOKINGS_PER_DAY} bookings per day.`, "error");
        return;
      }
      updated.push({
        key: slotKey,
        facilityName,
        courtName,
        date: selectedDate,
        timeSlot,
      });
    }
    persistSelectedSlots(updated);
  };

  // Helper to persist selection
  const persistSelectedSlots = (slots) => {
    setSelectedSlots(slots);
    localStorage.setItem('admin_cart_slots', JSON.stringify(slots));
  };

  // Update renderCourtTimeSlots to allow user selection with restriction
  const renderCourtTimeSlots = (courtName, status) => {
    const courtTimeRange = courtTimeRanges[courtName];

    if (!courtTimeRange) {
      return dynamicTimeSlots.map((time) => (
        <td key={time} className="court-time-slot loading"></td>
      ));
    }

    const isOpen = (courtTimeRange.status || status) === "OPEN";
    const slotClass = isOpen ? "available" : (courtTimeRange.status || status).toLowerCase();

    const earliest = courtTimeRange.earliestTime;
    const latest = courtTimeRange.latestTime;

    return dynamicTimeSlots.map((time) => {
      const slotSeconds = timeToSeconds(time);

      if (slotSeconds < earliest || slotSeconds > latest) {
        return (
          <td
            key={time}
            className="court-time-slot unavailable"
            style={{
              backgroundColor: "#f3f4f6",
              color: "#9ca3af",
              fontStyle: "italic",
            }}
          >
            Unavailable
          </td>
        );
      }

      if (isSlotBooked(courtName, slotSeconds)) {
        return (
          <td
            key={time}
            className="court-time-slot booked"
            style={{
              backgroundColor: "#fde68a",
              color: "#d97706",
              fontWeight: "bold",
            }}
          >
            Booked
          </td>
        );
      }

      const slotKey = `${facilityName}|${courtName}|${selectedDate}|${time}`;
      const isSelected = selectedSlots.some(s => s.key === slotKey);

      // Admin: multi-select
      if (userRole === "Admin" && isOpen && !isSlotBooked(courtName, slotSeconds)) {
        return (
          <td
            key={time}
            className={`court-time-slot available bookable ${isSelected ? 'selected' : ''}`}
            style={{
              backgroundColor: isSelected ? "#a7f3d0" : "#dcfce7",
              border: isSelected ? "2px solid #059669" : undefined,
              cursor: "pointer"
            }}
            onClick={() => handleAdminSlotSelect(courtName, time)}
            title={`Click to ${isSelected ? "remove" : "add"} ${courtName} at ${time} to cart`}
          >
            {isSelected ? "Selected" : "Available"}
          </td>
        );
      }

      // User: up to 3 per day
      if (userRole === "User" && isOpen && !isSlotBooked(courtName, slotSeconds)) {
        return (
          <td
            key={time}
            className={`court-time-slot available bookable ${isSelected ? 'selected' : ''}`}
            style={{
              backgroundColor: isSelected ? "#a7f3d0" : "#dcfce7",
              border: isSelected ? "2px solid #059669" : undefined,
              cursor: "pointer"
            }}
            onClick={() => handleUserSlotSelect(courtName, time)}
            title={`Click to ${isSelected ? "remove" : "add"} ${courtName} at ${time} to cart`}
          >
            {isSelected ? "Selected" : "Available"}
          </td>
        );
      }

      // Default
      return (
        <td
          key={time}
          className={`court-time-slot ${slotClass} ${isOpen ? 'bookable' : ''}`}
          style={{
            backgroundColor: isOpen
              ? "#dcfce7"
              : slotClass === "closed"
              ? "#fecaca"
              : slotClass === "maintenance"
              ? "#fed7aa"
              : slotClass === "booked"
              ? "#fde68a"
              : "#f3f4f6",
            color: isOpen
              ? "#166534"
              : slotClass === "closed"
              ? "#dc2626"
              : slotClass === "maintenance"
              ? "#ea580c"
              : slotClass === "booked"
              ? "#d97706"
              : "#6b7280",
            fontWeight: !isOpen ? "bold" : "normal",
          }}
          onClick={isOpen ? () => handleBookTimeSlot(courtName, time) : undefined}
          title={isOpen ? `Click to book ${courtName} at ${time}` : `${slotClass.charAt(0).toUpperCase() + slotClass.slice(1)} - ${time}`}
        >
          {isOpen ? "Available" : slotClass.charAt(0).toUpperCase() + slotClass.slice(1)}
        </td>
      );
    });
  };

  // use correct endpoint based on role
  const fetchCourtTimeRangesAndBookings = async () => {
    try {
      const backendUrl = import.meta.env.VITE_BACKEND_URL || "http://localhost:8080";
      let facilityRes;
      let courts = [];
      let facilityData = {};

      if (userRole === "Admin") {
        facilityRes = await authFetch(
          `${backendUrl}/api/admin/${encodeURIComponent(facilityName)}/courts`
        );
        if (!facilityRes.ok) throw new Error("Failed to fetch courts for facility");
        const courtsResult = await facilityRes.json();
        if (!courtsResult.success) throw new Error(courtsResult.error || "Facility courts error");
        courts = courtsResult.data || [];
        facilityData = { name: facilityName, courts };
      } else {
        facilityRes = await authFetch(
          `${backendUrl}/api/user/sport-facilities/${encodeURIComponent(facilityName)}/details`
        );
        if (!facilityRes.ok) throw new Error("Failed to fetch facility details");
        const result = await facilityRes.json();
        if (!result.success) throw new Error(result.error || "Facility details error");
        facilityData = result.data;
        courts = facilityData.courts || [];
      }

      setFacilityDetails(facilityData);

      const timeRanges = {};
      let allTimeSlots = new Set();
      const bookedMap = {};

      // Fetch booked timeslots for each court
      for (const court of courts) {
        try {
          const courtRoute =
            userRole === "Admin"
              ? `/api/admin/${encodeURIComponent(facilityName)}/${encodeURIComponent(court.name)}/booked-timeslots`
              : `/api/user/${encodeURIComponent(facilityName)}/${encodeURIComponent(court.name)}/booked-timeslots`;

          const res = await authFetch(`${backendUrl}${courtRoute}`);

          let bookedSlots = [];
          if (res.ok) {
            const data = await res.json();
            bookedSlots = Array.isArray(data.data) ? data.data : [];
          }

          bookedMap[court.name] = bookedSlots;

          timeRanges[court.name] = {
            earliestTime: court.earliestTime,
            latestTime: court.latestTime,
            earliestTimeStr: formatTime(court.earliestTime),
            latestTimeStr: formatTime(court.latestTime),
            status: getStatusString(court.status),
            available: court.status === 0,
            bookedTimeSlots: bookedSlots,
            timeSlotAvailability: {},
          };

          const courtSlots = generateTimeSlots(court.earliestTime, court.latestTime);
          courtSlots.forEach((slot) => allTimeSlots.add(slot));
        } catch (err) {
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
          bookedMap[court.name] = [];
        }
      }

      setCourtTimeRanges(timeRanges);
      setBookedTimeSlotsMap(bookedMap);

      const sortedSlots = Array.from(allTimeSlots).sort();
      setDynamicTimeSlots(
        sortedSlots.length > 0
          ? sortedSlots
          : [
              "08:00", "09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00",
              "16:00", "17:00", "18:00", "19:00", "20:00", "21:00", "22:00", "23:00"
            ]
      );
    } catch (err) {
      setCourtTimeRanges({});
      setDynamicTimeSlots([]);
      setBookedTimeSlotsMap({});
      setToast({ message: "Failed to load court time ranges or bookings", type: "error", show: true });
    }
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

        {userRole === "Admin" && selectedSlots.length > 0 && (
          <div style={{ textAlign: "right", marginTop: "1rem" }}>
            <button
              className="court-booking-cart-btn"
              onClick={() => navigate('/cart')}
            >
              Go to Cart ({selectedSlots.length} selected)
            </button>
          </div>
        )}
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