import React, { useState, useEffect, useCallback } from "react";
import Navbar from "@components/Navbar";
import Spinner from "@components/Spinner";
import { Download } from "lucide-react";
import { authFetch } from "@utils/authFetch";
import * as XLSX from "xlsx";
import "@styles/BookingManagement.css";

// Utility to abbreviate Ethereum or user addresses for display
const abbreviateAddress = (address) => {
  if (!address || typeof address !== "string") return "-";
  if (address.length <= 10) return address;
  return `${address.slice(0, 6)}...${address.slice(-4)}`;
};

// Utility to generate Pinata gateway URL for an IPFS hash
const getIpfsGatewayUrl = (ipfsHash) => {
  if (!ipfsHash) return "#";
  return `https://gateway.pinata.cloud/ipfs/${ipfsHash}`;
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
    day: "numeric",
  };
  const timeOptions = {
    timeZone: "Asia/Kuala_Lumpur",
    hour: "2-digit",
    minute: "2-digit",
    hour12: true,
  };
  return (
    <>
      {start.toLocaleDateString("en-GB", dateOptions)}
      <br />
      {start
        .toLocaleTimeString("en-GB", timeOptions)
        .toLowerCase()}{" "}
      -{" "}
      {end.toLocaleTimeString("en-GB", timeOptions).toLowerCase()}
    </>
  );
};

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

const BookingManagement = () => {
  const [activeTab, setActiveTab] = useState("bookings");
  const [bookings, setBookings] = useState([]);
  const [bookingsLoading, setBookingsLoading] = useState(false);
  const [toast, setToast] = useState({ msg: "", type: "success" });

  // Filters and search state
  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState({
    approved: true,
    rejected: true,
    completed: true,
    cancelled: true,
  });
  const [facilityFilter, setFacilityFilter] = useState("");
  const [courtFilter, setCourtFilter] = useState("");
  // Change idRange to dateRange
  const [dateRange, setDateRange] = useState({ start: "", end: "" });

  // Export modal state
  const [showExportModal, setShowExportModal] = useState(false);
  const [isExporting, setIsExporting] = useState(false);

  // Pagination state
  const BOOKINGS_PER_PAGE = 10;
  const [currentPage, setCurrentPage] = useState(1);

  useEffect(() => {
    setBookingsLoading(true);
    (async () => {
      try {
        const res = await authFetch("/api/admin/bookings/all");
        if (!res.ok)
          throw new Error(await res.text() || "Failed to load bookings");
        const { data } = await res.json();
        setBookings(data || []);
      } catch (err) {
        setBookings([]);
        setToast({ msg: err.message, type: "error" });
      } finally {
        setBookingsLoading(false);
      }
    })();
  }, []);

  // Get unique facilities and courts for filter dropdowns
  const facilities = Array.from(new Set(bookings.map((b) => b.facilityName).filter(Boolean)));
  const courts = Array.from(new Set(bookings.map((b) => b.courtName).filter(Boolean)));

  // Filtering logic
  const filteredBookings = bookings.filter((booking) => {
    // Status filter
    const statusKey = String(booking.status).toLowerCase();
    const statusLabel = STATUS_MAP[booking.status]?.toLowerCase() || statusKey;
    if (!statusFilter[statusLabel]) return false;

    // Facility filter
    if (facilityFilter && booking.facilityName !== facilityFilter) return false;

    // Court filter
    if (courtFilter && booking.courtName !== courtFilter) return false;

    // Date range filter (start and end are YYYY-MM-DD)
    if (dateRange.start) {
      const bookingStart = new Date(Number(booking.startTime) * 1000);
      const filterStart = new Date(dateRange.start + "T00:00:00");
      if (bookingStart < filterStart) return false;
    }
    if (dateRange.end) {
      const bookingEnd = new Date(Number(booking.endTime) * 1000);
      const filterEnd = new Date(dateRange.end + "T23:59:59");
      if (bookingEnd > filterEnd) return false;
    }

    // Search filter (searches IPFS, owner, facility, court)
    if (search) {
      const s = search.toLowerCase();
      if (
        !(booking.ipfsHash && booking.ipfsHash.toLowerCase().includes(s)) &&
        !(booking.owner && booking.owner.toLowerCase().includes(s)) &&
        !(booking.userAddress && booking.userAddress.toLowerCase().includes(s)) &&
        !(booking.facilityName && booking.facilityName.toLowerCase().includes(s)) &&
        !(booking.courtName && booking.courtName.toLowerCase().includes(s))
      ) {
        return false;
      }
    }

    return true;
  });

  // Pagination logic
  const totalPages = Math.ceil(filteredBookings.length / BOOKINGS_PER_PAGE);
  const paginatedBookings = filteredBookings.slice(
    (currentPage - 1) * BOOKINGS_PER_PAGE,
    currentPage * BOOKINGS_PER_PAGE
  );

  const getStatusClass = (status) => {
    const s = typeof status === "string" ? status.toLowerCase() : status;
    switch (s) {
      case "approved":
      case 0:
      case "0":
        return "status-approved";
      case "pending":
        return "status-pending";
      case "rejected":
      case 1:
      case "1":
        return "status-rejected";
      case "completed":
      case 2:
      case "2":
        return "status-completed";
      case "cancelled":
      case 3:
      case "3":
        return "status-cancelled";
      default:
        return "";
    }
  };

  // Handle filter changes
  const handleStatusChange = (status) => {
    setStatusFilter((prev) => ({
      ...prev,
      [status]: !prev[status],
    }));
  };

  // Reject booking logic (same as AdminDashboard)
  const openRejectModal = async (booking) => {
    if (!booking) return;
    const reason = window.prompt("Please enter the reason for rejection:");
    if (reason && reason.trim()) {
      await handleConfirmReject(reason.trim(), booking);
    }
  };

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
      // Refresh bookings after rejection
      const refreshed = await authFetch("/api/admin/bookings/all");
      const { data } = await refreshed.json();
      setBookings(data || []);
    } catch (err) {
      setToast({ msg: err.message, type: "error" });
    }
  };

  // Export logic (same as AdminDashboard)
  const handleExportReport = async (startDate, endDate) => {
    try {
      setIsExporting(true);
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
    } finally {
      setIsExporting(false);
      setShowExportModal(false);
    }
  };

  // Export modal component (same as AdminDashboard)
  function ExportReportModal({ open, onClose, onExport }) {
    const [startDate, setStartDate] = useState("");
    const [endDate, setEndDate] = useState("");
    const [isExportingLocal, setIsExportingLocal] = useState(false);

    const handleExport = async () => {
      if (!startDate || !endDate) {
        alert("Please select both start and end dates.");
        return;
      }
      setIsExportingLocal(true);
      await onExport(startDate, endDate);
      setIsExportingLocal(false);
      onClose();
    };

    useEffect(() => {
      if (!open) {
        setStartDate("");
        setEndDate("");
        setIsExportingLocal(false);
      }
    }, [open]);

    if (!open) return null;

    return (
      <div className="modal-overlay" onClick={e => e.target === e.currentTarget && !isExportingLocal && onClose()}>
        <div className="modal-content" onClick={e => e.stopPropagation()}>
          <div className="modal-header">
            <h3>Export Booking Report</h3>
            <button className="close-btn" onClick={onClose} disabled={isExportingLocal}>
              ×
            </button>
          </div>
          <div className="announcement-form">
            <div className="form-group">
              <label>Start Date</label>
              <input
                type="date"
                value={startDate}
                onChange={e => setStartDate(e.target.value)}
                disabled={isExportingLocal}
              />
            </div>
            <div className="form-group">
              <label>End Date</label>
              <input
                type="date"
                value={endDate}
                onChange={e => setEndDate(e.target.value)}
                disabled={isExportingLocal}
              />
            </div>
          </div>
          <div className="modal-actions">
            <button className="cancel-btn" onClick={onClose} disabled={isExportingLocal}>
              Cancel
            </button>
            <button className="save-btn" onClick={handleExport} disabled={isExportingLocal || !startDate || !endDate}>
              {isExportingLocal ? "Exporting..." : "Export"}
            </button>
          </div>
        </div>
      </div>
    );
  }

  // Clear all filters handler
  const handleClearFilters = () => {
    setSearch("");
    setStatusFilter({
      approved: true,
      rejected: true,
      completed: true,
      cancelled: true,
    });
    setFacilityFilter("");
    setCourtFilter("");
    setDateRange({ start: "", end: "" });
  };

  // Toast auto-hide after 3 seconds
  useEffect(() => {
    if (toast.msg) {
      const timer = setTimeout(() => {
        setToast({ msg: "", type: "success" });
      }, 3000);
      return () => clearTimeout(timer);
    }
  }, [toast.msg]);

  // Reset to first page when filters/search change
  useEffect(() => {
    setCurrentPage(1);
  }, [search, statusFilter, facilityFilter, courtFilter, dateRange]);

  return (
    <div className="booking-management">
      <Navbar activeTab={activeTab} setActiveTab={setActiveTab} />
      <div className="content">
        {/* Filters Section */}
        <div className="filters-section">
          <div className="search-bar">
            <input
              className="search-input"
              type="text"
              placeholder="Search by IPFS, owner, facility, court..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
          </div>
          <div className="filters">
            <div className="filter-group">
              <h3>Status</h3>
              <div className="checkbox-group">
                {Object.keys(statusFilter).map((status) => (
                  <label key={status} className="checkbox-label">
                    <input
                      type="checkbox"
                      checked={statusFilter[status]}
                      onChange={() => handleStatusChange(status)}
                    />
                    {status.charAt(0).toUpperCase() + status.slice(1)}
                  </label>
                ))}
              </div>
            </div>
            <div className="filter-group">
              <h3>Facility</h3>
              <select
                className="range-input"
                value={facilityFilter}
                onChange={(e) => setFacilityFilter(e.target.value)}
              >
                <option value="">All</option>
                {facilities.map((f) => (
                  <option key={f} value={f}>{f}</option>
                ))}
              </select>
            </div>
            <div className="filter-group">
              <h3>Court</h3>
              <select
                className="range-input"
                value={courtFilter}
                onChange={(e) => setCourtFilter(e.target.value)}
              >
                <option value="">All</option>
                {courts.map((c) => (
                  <option key={c} value={c}>{c}</option>
                ))}
              </select>
            </div>
            {/* Date Range Filter */}
            <div className="filter-group">
              <h3>Date Range</h3>
              <div className="id-range">
                <input
                  className="range-input"
                  type="date"
                  placeholder="Start"
                  value={dateRange.start}
                  onChange={(e) => setDateRange((prev) => ({ ...prev, start: e.target.value }))}
                />
                <span>-</span>
                <input
                  className="range-input"
                  type="date"
                  placeholder="End"
                  value={dateRange.end}
                  onChange={(e) => setDateRange((prev) => ({ ...prev, end: e.target.value }))}
                />
              </div>
            </div>
            <div style={{ marginTop: 16, textAlign: "right" }}>
              <button
                type="button"
                className="export-btn"
                style={{ background: "#f3f4f6", color: "#111827", border: "1px solid #d1d5db", marginTop: 8 }}
                onClick={handleClearFilters}
              >
                Reset All Filters
              </button>
            </div>
          </div>
        </div>
        {/* Bookings Table Section */}
        <div className="bookings-section">
          <div
            className="bookings-header"
            style={{
              display: "flex",
              alignItems: "center",
              justifyContent: "space-between",
            }}
          >
            <h2>Bookings</h2>
            <div style={{ display: "flex", alignItems: "center", gap: "1rem" }}>
              <span style={{ fontWeight: 500, color: "#374151" }}>
                Displaying Bookings: {filteredBookings.length}
              </span>
              <button className="export-btn" onClick={() => setShowExportModal(true)}>
                <Download size={16} style={{ marginRight: 6 }} />
                Export
              </button>
            </div>
          </div>
          <div className="bookings-table">
            <table>
              <thead>
                <tr>
                  <th>IPFS</th>
                  <th>Owner</th>
                  <th>Facility</th>
                  <th>Court</th>
                  <th>Time</th>
                  <th>Status</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {bookingsLoading ? (
                  <tr>
                    <td colSpan={7} style={{ textAlign: "center" }}>
                      <Spinner />
                    </td>
                  </tr>
                ) : paginatedBookings.length === 0 ? (
                  <tr>
                    <td colSpan={7} style={{ textAlign: "center", color: "#888" }}>
                      No bookings found
                    </td>
                  </tr>
                ) : (
                  paginatedBookings.map((booking) => (
                    <tr key={booking.ipfsHash || booking.id}>
                      <td>
                        {booking.ipfsHash ? (
                          <a
                            href={getIpfsGatewayUrl(booking.ipfsHash)}
                            target="_blank"
                            rel="noopener noreferrer"
                            style={{
                              color: "#3b82f6",
                              textDecoration: "underline",
                            }}
                          >
                            {booking.ipfsHash.slice(0, 10)}...{booking.ipfsHash.slice(-6)}
                          </a>
                        ) : (
                          "-"
                        )}
                      </td>
                      <td>{abbreviateAddress(booking.owner || booking.userAddress)}</td>
                      <td>{booking.facilityName}</td>
                      <td>{booking.courtName}</td>
                      <td className="time-cell">
                        {formatBookingTimeGmt8(booking.startTime, booking.endTime)}
                      </td>
                      <td>
                        <span className={`status ${getStatusClass(booking.status)}`}>
                          {STATUS_MAP[booking.status] || booking.status}
                        </span>
                      </td>
                      <td>
                        <button
                          className="action-btn reject-btn"
                          disabled={
                            String(booking.status).toLowerCase() === "rejected" ||
                            String(booking.status) === "1"
                          }
                          onClick={() => openRejectModal(booking)}
                        >
                          Reject
                        </button>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
          {/* Pagination Controls */}
          {totalPages > 1 && (
            <div style={{ display: "flex", justifyContent: "center", margin: "1rem 0" }}>
              <button
                onClick={() => setCurrentPage((p) => Math.max(1, p - 1))}
                disabled={currentPage === 1}
                style={{ marginRight: 8 }}
              >
                Prev
              </button>
              <span style={{ margin: "0 8px" }}>
                Page {currentPage} of {totalPages}
              </span>
              <button
                onClick={() => setCurrentPage((p) => Math.min(totalPages, p + 1))}
                disabled={currentPage === totalPages}
              >
                Next
              </button>
            </div>
          )}
        </div>
      </div>
      {/* Export Modal */}
      <ExportReportModal
        open={showExportModal}
        onClose={() => setShowExportModal(false)}
        onExport={handleExportReport}
      />
      {/* Toast for feedback */}
      {toast.msg && (
        <div
          style={{
            position: "fixed",
            bottom: 24,
            left: "50%",
            transform: "translateX(-50%)",
            background: toast.type === "error" ? "#fee2e2" : "#d1fae5",
            color: toast.type === "error" ? "#b91c1c" : "#065f46",
            padding: "12px 24px",
            borderRadius: 8,
            zIndex: 9999,
            minWidth: 200,
            textAlign: "center",
            fontWeight: 500,
          }}
          onClick={() => setToast({ msg: "", type: "success" })}
        >
          {toast.msg}
        </div>
      )}
    </div>
  );
};

export default BookingManagement;