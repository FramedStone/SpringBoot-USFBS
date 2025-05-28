import React, { useState } from "react";
import Navbar from "@components/Navbar";
import "@styles/BookingManagement.css";

const DEFAULT_START_TIME = "00:00"; 
const DEFAULT_END_TIME = "23:59";  

const BookingManagement = () => {
  const [activeTab, setActiveTab] = useState("bookings");
  const [bookings] = useState([
    {
      id: "001",
      user: "user1@example.com",
      sportFacility: "Badminton",
      court: "Court A",
      time: "2025-03-24\n10 AM - 12 PM",
      status: "Approved",
    },
    {
      id: "002",
      user: "user2@example.com",
      sportFacility: "Volleyball",
      court: "Court B",
      time: "2025-03-25\n8 AM - 10 AM",
      status: "Pending",
    },
    {
      id: "003",
      user: "user3@example.com",
      sportFacility: "Basketball",
      court: "Court C",
      time: "2025-03-26\n1 PM - 3 PM",
      status: "Rejected",
    },
  ]);

  const [filters, setFilters] = useState({
    idRange: { min: "", max: "" },
    startDate: "",
    endDate: "",
    startTime: DEFAULT_START_TIME,
    endTime: DEFAULT_END_TIME,
    sportFacilities: [],
    courts: [],
    statuses: [],
  });

  const [searchTerm, setSearchTerm] = useState("");

  const sportFacilities = [
    "Badminton",
    "Volleyball",
    "Basketball",
    "Tennis",
    "Squash",
  ];
  const courtsBySport = {
    Badminton: ["Court A", "Court B", "Court C"],
    Volleyball: ["Court B", "Court D"],
    Basketball: ["Court C", "Court E", "Court F"],
    Tennis: ["Court G", "Court H"],
    Squash: ["Court I", "Court J"],
  };
  const statuses = ["Approved", "Pending", "Rejected"];

  const handleFilterChange = (filterType, value, checked) => {
    setFilters((prev) => {
      const newFilters = { ...prev };

      if (filterType === "sportFacilities") {
        if (checked) {
          newFilters.sportFacilities = [...prev.sportFacilities, value];
        } else {
          newFilters.sportFacilities = prev.sportFacilities.filter(
            (item) => item !== value
          );
          // Remove courts of unchecked sport facility
          const courtsToRemove = courtsBySport[value] || [];
          newFilters.courts = prev.courts.filter(
            (court) => !courtsToRemove.includes(court)
          );
        }
      } else if (filterType === "courts") {
        if (checked) {
          newFilters.courts = [...prev.courts, value];
        } else {
          newFilters.courts = prev.courts.filter((item) => item !== value);
        }
      } else if (filterType === "statuses") {
        if (checked) {
          newFilters.statuses = [...prev.statuses, value];
        } else {
          newFilters.statuses = prev.statuses.filter((item) => item !== value);
        }
      }

      return newFilters;
    });
  };

  const handleIdRangeChange = (type, value) => {
    setFilters((prev) => ({
      ...prev,
      idRange: {
        ...prev.idRange,
        [type]: value,
      },
    }));
  };

  const handleStartDateChange = (value) => {
    setFilters((prev) => ({
      ...prev,
      startDate: value,
    }));
  };
  const handleEndDateChange = (value) => {
    setFilters((prev) => ({
      ...prev,
      endDate: value,
    }));
  };

  const handleStartTimeChange = (value) => {
    setFilters((prev) => ({
      ...prev,
      startTime: value,
    }));
  };
  const handleEndTimeChange = (value) => {
    setFilters((prev) => ({
      ...prev,
      endTime: value,
    }));
  };

  const clearFilters = () => {
    setFilters({
      idRange: { min: "", max: "" },
      startDate: "",
      endDate: "",
      startTime: DEFAULT_START_TIME,
      endTime: DEFAULT_END_TIME,
      sportFacilities: [],
      courts: [],
      statuses: [],
    });
    setSearchTerm("");
  };

  const getAvailableCourts = () => {
    if (filters.sportFacilities.length === 0) {
      return Object.values(courtsBySport).flat();
    }

    return filters.sportFacilities.reduce((courts, sport) => {
      return [...courts, ...(courtsBySport[sport] || [])];
    }, []);
  };

  const normalizeTime = (timeStr) => {
    return timeStr
      .replace(/\s+/g, "")
      .replace(/:/g, "")
      .replace(/\n/g, "")
      .replace(/(\d)(am|pm)/gi, "$1$2")
      .toLowerCase();
  };

  const extractBookingDate = (bookingTime) => {
    const dateLine = bookingTime.split("\n")[0];
    return dateLine;
  };

  const extractBookingTimes = (bookingTime) => {
    // Example: "2025-03-24\n10 AM - 12 PM"
    const timeLine = bookingTime.split("\n")[1];
    if (!timeLine) return { start: "", end: "" };
    // Try to parse "10 AM - 12 PM" or "8 AM - 10 AM"
    const [startRaw, endRaw] = timeLine.split("-").map((s) => s.trim());
    const to24 = (str) => {
      if (!str) return "";
      // Accept "10 AM", "12 PM", "8AM", "10AM"
      const match = str.match(/^(\d{1,2})(?::(\d{2}))?\s*(am|pm)$/i);
      if (!match) return "";
      let hour = parseInt(match[1], 10);
      const min = match[2] ? parseInt(match[2], 10) : 0;
      const period = match[3].toLowerCase();
      if (period === "pm" && hour !== 12) hour += 12;
      if (period === "am" && hour === 12) hour = 0;
      return `${hour.toString().padStart(2, "0")}:${min.toString().padStart(2, "0")}`;
    };
    return {
      start: to24(startRaw),
      end: to24(endRaw),
    };
  };

  const filteredBookings = bookings.filter((booking) => {
    // Search term filter (with normalized time)
    if (
      searchTerm &&
      !booking.user.toLowerCase().includes(searchTerm.toLowerCase()) &&
      !booking.id.toLowerCase().includes(searchTerm.toLowerCase()) &&
      !booking.sportFacility.toLowerCase().includes(searchTerm.toLowerCase()) &&
      !booking.court.toLowerCase().includes(searchTerm.toLowerCase()) &&
      !booking.time.toLowerCase().includes(searchTerm.toLowerCase()) &&
      !normalizeTime(booking.time).includes(normalizeTime(searchTerm))
    ) {
      return false;
    }

    // ID range filter
    if (filters.idRange.min && parseInt(booking.id) < parseInt(filters.idRange.min)) {
      return false;
    }
    if (filters.idRange.max && parseInt(booking.id) > parseInt(filters.idRange.max)) {
      return false;
    }

    if (filters.startDate || filters.endDate) {
      const bookingDate = extractBookingDate(booking.time);
      if (filters.startDate && bookingDate < filters.startDate) {
        return false;
      }
      if (filters.endDate && bookingDate > filters.endDate) {
        return false;
      }
    }

    if (filters.startTime || filters.endTime) {
      const { start, end } = extractBookingTimes(booking.time);
      // If booking has no time, skip
      if ((filters.startTime && !start) || (filters.endTime && !end)) return false;
      if (filters.startTime && start < filters.startTime) return false;
      if (filters.endTime && end > filters.endTime) return false;
    }

    // Sport facility filter
    if (
      filters.sportFacilities.length > 0 &&
      !filters.sportFacilities.includes(booking.sportFacility)
    ) {
      return false;
    }

    // Court filter
    if (filters.courts.length > 0 && !filters.courts.includes(booking.court)) {
      return false;
    }

    // Status filter
    if (filters.statuses.length > 0 && !filters.statuses.includes(booking.status)) {
      return false;
    }

    return true;
  });

  const getStatusClass = (status) => {
    switch (status.toLowerCase()) {
      case "approved":
        return "status-approved";
      case "pending":
        return "status-pending";
      case "rejected":
        return "status-rejected";
      default:
        return "";
    }
  };

  const exportReport = () => {
    console.log("Exporting report...");
    // TODO: implement export functionality
  };

  return (
    <div className="booking-management">
      <Navbar activeTab={activeTab} setActiveTab={setActiveTab} />
      <div className="content">
        <div className="filters-section">
          <div className="search-bar">
            <input
              type="text"
              placeholder="Search bookings..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="search-input"
            />
          </div>

          {/* Clear Filters Button */}
          <div style={{ margin: "12px 0" }}>
            <button onClick={clearFilters} className="clear-filters-btn">
              Clear All Filters
            </button>
          </div>

          <div className="filters">
            <div className="filter-group">
              <h3>ID Range</h3>
              <div className="id-range">
                <input
                  type="number"
                  placeholder="Min"
                  value={filters.idRange.min}
                  onChange={(e) => handleIdRangeChange("min", e.target.value)}
                  className="range-input"
                />
                <span>-</span>
                <input
                  type="number"
                  placeholder="Max"
                  value={filters.idRange.max}
                  onChange={(e) => handleIdRangeChange("max", e.target.value)}
                  className="range-input"
                />
              </div>
            </div>

            {/* Date Filter */}
            <div className="filter-group">
              <h3>Date</h3>
              <div style={{ display: "flex", gap: "8px" }}>
                <input
                  type="date"
                  value={filters.startDate}
                  onChange={(e) => handleStartDateChange(e.target.value)}
                  className="range-input"
                  style={{ minWidth: 0, flex: 1 }}
                  placeholder="Start date"
                />
                <span style={{ alignSelf: "center" }}>to</span>
                <input
                  type="date"
                  value={filters.endDate}
                  onChange={(e) => handleEndDateChange(e.target.value)}
                  className="range-input"
                  style={{ minWidth: 0, flex: 1 }}
                  placeholder="End date"
                />
              </div>
            </div>

            {/* Time Filter */}
            <div className="filter-group">
              <h3>Time</h3>
              <div style={{ display: "flex", gap: "8px" }}>
                <input
                  type="time"
                  value={filters.startTime}
                  onChange={(e) => handleStartTimeChange(e.target.value)}
                  className="range-input"
                  style={{ minWidth: 0, flex: 1 }}
                  placeholder="Start time"
                />
                <span style={{ alignSelf: "center" }}>to</span>
                <input
                  type="time"
                  value={filters.endTime}
                  onChange={(e) => handleEndTimeChange(e.target.value)}
                  className="range-input"
                  style={{ minWidth: 0, flex: 1 }}
                  placeholder="End time"
                />
              </div>
            </div>

            <div className="filter-group">
              <h3>Sport Facility</h3>
              <div className="checkbox-group">
                {sportFacilities.map((sport) => (
                  <label key={sport} className="checkbox-label">
                    <input
                      type="checkbox"
                      checked={filters.sportFacilities.includes(sport)}
                      onChange={(e) =>
                        handleFilterChange("sportFacilities", sport, e.target.checked)
                      }
                    />
                    {sport}
                  </label>
                ))}
              </div>
            </div>

            <div className="filter-group">
              <h3>Court</h3>
              <div className="checkbox-group">
                {getAvailableCourts().map((court) => (
                  <label key={court} className="checkbox-label">
                    <input
                      type="checkbox"
                      checked={filters.courts.includes(court)}
                      onChange={(e) => handleFilterChange("courts", court, e.target.checked)}
                    />
                    {court}
                  </label>
                ))}
              </div>
            </div>

            <div className="filter-group">
              <h3>Status</h3>
              <div className="checkbox-group">
                {statuses.map((status) => (
                  <label key={status} className="checkbox-label">
                    <input
                      type="checkbox"
                      checked={filters.statuses.includes(status)}
                      onChange={(e) => handleFilterChange("statuses", status, e.target.checked)}
                    />
                    {status}
                  </label>
                ))}
              </div>
            </div>
          </div>
        </div>

        <div className="bookings-section">
          <div className="bookings-header">
            <h2>Bookings</h2>
            <button onClick={exportReport} className="export-btn">
              Export Report
            </button>
          </div>

          <div className="bookings-table">
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>User</th>
                  <th>Sport Facility</th>
                  <th>Court</th>
                  <th>Time</th>
                  <th>Status</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {filteredBookings.map((booking) => (
                  <tr key={booking.id}>
                    <td>{booking.id}</td>
                    <td>{booking.user}</td>
                    <td>{booking.sportFacility}</td>
                    <td>{booking.court}</td>
                    <td className="time-cell">
                      {booking.time.split("\n").map((line, index) => (
                        <div key={index}>{line}</div>
                      ))}
                    </td>
                    <td>
                      <span className={`status ${getStatusClass(booking.status)}`}>
                        {booking.status}
                      </span>
                    </td>
                    <td>
                      <button
                        className="action-btn reject-btn"
                        disabled={booking.status.toLowerCase() === "rejected"}
                      >
                        Reject
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
};

export default BookingManagement;