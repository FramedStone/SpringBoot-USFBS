import React, { useState, useEffect, useMemo } from 'react';
import Navbar from "@components/Navbar";
import Toast from "@components/Toast";
import Spinner from "@components/Spinner";
import { authFetch } from "@utils/authFetch";
import { useWeb3Auth } from "@web3auth/modal/react";
import { useRequestQueue } from "@components/@RequestQueue";
import { ChevronUp, ChevronDown } from 'lucide-react';
import '@styles/UserBookings.css';
import dayjs from "dayjs";

// Modal component for confirmation
const ConfirmModal = ({ open, onConfirm, onCancel }) => {
  if (!open) return null;
  return (
    <div className="cancel-modal-overlay">
      <div className="cancel-modal-content">
        <h4>Cancel Booking</h4>
        <p>Are you sure you want to cancel this booking?</p>
        <div className="cancel-modal-actions">
          <button onClick={onCancel} className="cancel-modal-btn cancel">No</button>
          <button onClick={onConfirm} className="cancel-modal-btn confirm">Yes, Cancel</button>
        </div>
      </div>
    </div>
  );
};

const PAGE_SIZE = 10;

const STATUS_OPTIONS = [
  { value: '', label: 'All Status' },
  { value: '0', label: 'Approved' },
  { value: '1', label: 'Rejected' },
  { value: '2', label: 'Completed' },
  { value: '3', label: 'Cancelled' }
];

const UserBookings = () => {
  const [activeTab, setActiveTab] = useState("bookings");
  const [toast, setToast] = useState({ msg: "", type: "success" });
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(false);
  const [userRole, setUserRole] = useState("");
  const [ethAddress, setEthAddress] = useState("");
  const [page, setPage] = useState(0);
  const [modalOpen, setModalOpen] = useState(false);
  const [pendingCancel, setPendingCancel] = useState(null);
  const [cancelJobs, setCancelJobs] = useState({});
  const [sortBy, setSortBy] = useState({
    field: 'date',
    order: 'desc'
  });

  // Search and filter states
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [facilityFilter, setFacilityFilter] = useState('');
  const [courtFilter, setCourtFilter] = useState('');
  const [dateFrom, setDateFrom] = useState('');
  const [dateTo, setDateTo] = useState('');

  const { web3Auth } = useWeb3Auth();
  const { addJob, queue } = useRequestQueue();

  // Fetch user role and eth address
  useEffect(() => {
    const fetchRoleAndAddress = async () => {
      try {
        const backendUrl = import.meta.env.VITE_BACKEND_URL || "http://localhost:8080";
        const res = await authFetch(`${backendUrl}/api/auth/me`);
        if (res.ok) {
          const data = await res.json();
          setUserRole(data.role);
        }
      } catch {
        setUserRole("");
      }
      if (web3Auth && web3Auth.provider) {
        try {
          const { Web3Provider } = await import("@ethersproject/providers");
          const rpcProvider = new Web3Provider(web3Auth.provider);
          const signer = rpcProvider.getSigner();
          const address = await signer.getAddress();
          setEthAddress(address);
        } catch {
          setEthAddress("");
        }
      }
    };
    fetchRoleAndAddress();
  }, [web3Auth]);

  // Fetch bookings when role/address changes
  useEffect(() => {
    const fetchBookings = async () => {
      setLoading(true);
      try {
        const backendUrl = import.meta.env.VITE_BACKEND_URL || "http://localhost:8080";
        let url = "";
        let options = { headers: { "Content-Type": "application/json" } };

        if (userRole === "Admin") {
          url = `${backendUrl}/api/admin/bookings?userAddress=${ethAddress}`;
        } else {
          url = `${backendUrl}/api/user/bookings`;
          options.headers["user-address"] = ethAddress;
        }

        const res = await authFetch(url, options);
        if (!res.ok) throw new Error("Failed to fetch bookings");
        const data = await res.json();
        setBookings(data.data || []);
      } catch (err) {
        setToast({ msg: "Failed to fetch bookings", type: "error" });
        setBookings([]);
      } finally {
        setLoading(false);
      }
    };
    if ((userRole === "Admin" || userRole === "User") && ethAddress) {
      fetchBookings();
    }
  }, [userRole, ethAddress]);

  // Get unique facilities and courts for filter dropdowns
  const facilityOptions = useMemo(() => {
    const set = new Set();
    bookings.forEach(b => b.facilityName && set.add(b.facilityName));
    return ['', ...Array.from(set)];
  }, [bookings]);
  const courtOptions = useMemo(() => {
    const set = new Set();
    bookings.forEach(b => b.courtName && set.add(b.courtName));
    return ['', ...Array.from(set)];
  }, [bookings]);

  // Sorting handler for Date column
  const handleColumnSort = (field) => {
    if (field !== 'date') return;
    setSortBy(prev => ({
      field,
      order: prev.field === field && prev.order === 'asc' ? 'desc' : 'asc'
    }));
    setPage(0);
  };

  // Get sort indicator for Date column
  const getSortIndicator = (field) => {
    if (sortBy.field !== field || field !== 'date') return null;
    return sortBy.order === 'asc' ? <ChevronUp size={14} /> : <ChevronDown size={14} />;
  };

  // Filter and sort bookings
  const filteredAndSortedBookings = useMemo(() => {
    let filtered = bookings;
    if (searchTerm.trim()) {
      const lower = searchTerm.trim().toLowerCase();
      filtered = filtered.filter(b =>
        (b.ipfsHash && b.ipfsHash.toLowerCase().includes(lower)) ||
        (b.facilityName && b.facilityName.toLowerCase().includes(lower)) ||
        (b.courtName && b.courtName.toLowerCase().includes(lower)) ||
        (b.status && getStatusString(b.status).toLowerCase().includes(lower)) ||
        (b.startTime && new Date(Number(b.startTime) * 1000).toLocaleDateString().toLowerCase().includes(lower)) ||
        (b.endTime && new Date(Number(b.endTime) * 1000).toLocaleDateString().toLowerCase().includes(lower))
      );
    }
    if (statusFilter) {
      filtered = filtered.filter(b => String(b.status) === statusFilter);
    }
    if (facilityFilter) {
      filtered = filtered.filter(b => b.facilityName === facilityFilter);
    }
    if (courtFilter) {
      filtered = filtered.filter(b => b.courtName === courtFilter);
    }
    // Date range filter
    if (dateFrom) {
      const fromEpoch = dayjs(dateFrom).startOf('day').unix();
      filtered = filtered.filter(b => Number(b.startTime) >= fromEpoch);
    }
    if (dateTo) {
      const toEpoch = dayjs(dateTo).endOf('day').unix();
      filtered = filtered.filter(b => Number(b.startTime) <= toEpoch);
    }
    return [...filtered].sort((a, b) => {
      const dateA = a.startTime ? Number(a.startTime) : 0;
      const dateB = b.startTime ? Number(b.startTime) : 0;
      return sortBy.order === 'asc' ? dateA - dateB : dateB - dateA;
    });
  }, [bookings, searchTerm, statusFilter, facilityFilter, courtFilter, dateFrom, dateTo, sortBy]);

  // Pagination
  const totalPages = Math.ceil(filteredAndSortedBookings.length / PAGE_SIZE);
  const pagedBookings = filteredAndSortedBookings.slice(page * PAGE_SIZE, (page + 1) * PAGE_SIZE);

  // Helper to check if a booking is being canceled
  const isCanceling = (ipfsHash) => {
    const jobId = cancelJobs[ipfsHash];
    if (!jobId) return false;
    const job = queue.find(j => j.id === jobId);
    return job && (job.status === "queued" || job.status === "running");
  };

  // Confirm cancel handler using queue
  const confirmCancel = async () => {
    if (!pendingCancel) return;
    const { b, cancelEndpoint } = pendingCancel;
    setModalOpen(false);
    setPendingCancel(null);

    const jobId = addJob(`Cancel booking ${b.ipfsHash}`, async () => {
      try {
        const backendUrl = import.meta.env.VITE_BACKEND_URL || "http://localhost:8080";
        const res = await authFetch(`${backendUrl}${cancelEndpoint}`, {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            "user-address": ethAddress
          }
        });
        if (!res.ok) throw new Error("Failed to cancel booking");
        setToast({ msg: "Booking cancelled successfully", type: "success" });
        setBookings(prev =>
          prev.map(book =>
            book.ipfsHash === b.ipfsHash
              ? { ...book, status: "3" }
              : book
          )
        );
      } catch (err) {
        setToast({ msg: "Failed to cancel booking", type: "error" });
      } finally {
        setCancelJobs(jobs => {
          const copy = { ...jobs };
          delete copy[b.ipfsHash];
          return copy;
        });
      }
    });
    setCancelJobs(jobs => ({ ...jobs, [b.ipfsHash]: jobId }));
  };

  const getPageNumbers = () => {
    const pageNumbers = [];
    const maxVisiblePages = 5;

    if (totalPages <= maxVisiblePages) {
      for (let i = 1; i <= totalPages; i++) {
        pageNumbers.push(i);
      }
    } else {
      const halfVisible = Math.floor(maxVisiblePages / 2);
      let startPage = Math.max(1, page + 1 - halfVisible);
      let endPage = Math.min(totalPages, startPage + maxVisiblePages - 1);

      if (endPage - startPage + 1 < maxVisiblePages) {
        startPage = Math.max(1, endPage - maxVisiblePages + 1);
      }

      if (startPage > 1) {
        pageNumbers.push(1);
        if (startPage > 2) {
          pageNumbers.push('...');
        }
      }

      for (let i = startPage; i <= endPage; i++) {
        pageNumbers.push(i);
      }

      if (endPage < totalPages) {
        if (endPage < totalPages - 1) {
          pageNumbers.push('...');
        }
        pageNumbers.push(totalPages);
      }
    }

    return pageNumbers;
  };

  function getStatusString(status) {
    switch (String(status)) {
      case "0": return "Approved";
      case "1": return "Rejected";
      case "2": return "Completed";
      case "3": return "Cancelled";
      default: return "-";
    }
  }

  return (
    <div className="user-bookings-container">
      <Navbar activeTab={activeTab} setActiveTab={setActiveTab} navType="home" />
      <div className="bookings-content">
        {/* Filter/Search Bar */}
        <div className="filters-container" style={{ marginBottom: 24 }}>
          <div className="filters-top-row" style={{ display: 'flex', gap: 24, flexWrap: 'wrap' }}>
            <input
              type="text"
              placeholder="Search by IPFS hash, facility, court, status..."
              value={searchTerm}
              onChange={e => { setSearchTerm(e.target.value); setPage(0); }}
              className="search-input"
              style={{ flex: 2, minWidth: 220, marginBottom: 0 }}
            />
            <select
              value={statusFilter}
              onChange={e => { setStatusFilter(e.target.value); setPage(0); }}
              className="date-input"
              style={{ minWidth: 140 }}
            >
              {STATUS_OPTIONS.map(opt => (
                <option key={opt.value} value={opt.value}>{opt.label}</option>
              ))}
            </select>
            <select
              value={facilityFilter}
              onChange={e => { setFacilityFilter(e.target.value); setPage(0); }}
              className="date-input"
              style={{ minWidth: 140 }}
            >
              <option value="">All Facilities</option>
              {facilityOptions.filter(f => f).map(f => (
                <option key={f} value={f}>{f}</option>
              ))}
            </select>
            <select
              value={courtFilter}
              onChange={e => { setCourtFilter(e.target.value); setPage(0); }}
              className="date-input"
              style={{ minWidth: 140 }}
            >
              <option value="">All Courts</option>
              {courtOptions.filter(c => c).map(c => (
                <option key={c} value={c}>{c}</option>
              ))}
            </select>
            {/* Date pickers */}
            <input
              type="date"
              value={dateFrom}
              onChange={e => { setDateFrom(e.target.value); setPage(0); }}
              className="date-input"
              style={{ minWidth: 140 }}
              max={dateTo || undefined}
              placeholder="From"
              title="From date"
            />
            <span className="date-separator" style={{ alignSelf: 'center', color: '#888' }}>–</span>
            <input
              type="date"
              value={dateTo}
              onChange={e => { setDateTo(e.target.value); setPage(0); }}
              className="date-input"
              style={{ minWidth: 140 }}
              min={dateFrom || undefined}
              placeholder="To"
              title="To date"
            />
          </div>
        </div>
        {/* End Filter/Search Bar */}

        {loading ? (
          <Spinner />
        ) : pagedBookings.length === 0 ? (
          <div className="bookings-placeholder">
            <h3>Bookings Page</h3>
            <p>No bookings found.</p>
          </div>
        ) : (
          <div className="bookings-table-container">
            <div className="bookings-table-responsive">
              <table className="bookings-table styled-table">
                <thead>
                  <tr>
                    <th>IPFS Hash</th>
                    <th>Facility</th>
                    <th>Court</th>
                    <th
                      className={`sortable ${sortBy.field === 'date' ? 'active' : ''}`}
                      onClick={() => handleColumnSort('date')}
                      style={{ cursor: 'pointer', userSelect: 'none' }}
                      title="Sort by Date"
                    >
                      Date
                      <span className="sort-indicator">
                        {getSortIndicator('date')}
                      </span>
                    </th>
                    <th>Start Time</th>
                    <th>End Time</th>
                    <th>Status</th>
                    <th>Action</th>
                  </tr>
                </thead>
                <tbody>
                  {pagedBookings.map((b, idx) => {
                    const statusString = getStatusString(b.status);

                    const cancelEndpoint =
                      userRole === "Admin"
                        ? `/api/admin/bookings/${b.ipfsHash}/cancel`
                        : `/api/user/bookings/${b.ipfsHash}/cancel`;

                    const openCancelModal = () => {
                      setPendingCancel({ b, cancelEndpoint });
                      setModalOpen(true);
                    };

                    return (
                      <tr key={b.ipfsHash || idx}>
                        <td>
                          {b.ipfsHash ? (
                            <a
                              href={`https://gateway.pinata.cloud/ipfs/${b.ipfsHash}`}
                              target="_blank"
                              rel="noopener noreferrer"
                            >
                              {b.ipfsHash.slice(0, 6)}...{b.ipfsHash.slice(-4)}
                            </a>
                          ) : "-"}
                        </td>
                        <td>{b.facilityName}</td>
                        <td>{b.courtName}</td>
                        <td>
                          {b.startTime
                            ? new Date(Number(b.startTime) * 1000).toLocaleDateString()
                            : "-"}
                        </td>
                        <td>
                          {b.startTime
                            ? new Date(Number(b.startTime) * 1000).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
                            : "-"}
                        </td>
                        <td>
                          {b.endTime
                            ? new Date(Number(b.endTime) * 1000).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
                            : "-"}
                        </td>
                        <td>
                          <span
                            className={
                              statusString === "Approved"
                                ? "status-badge status-approved"
                                : statusString === "Cancelled"
                                ? "status-badge status-cancelled"
                                : statusString === "Rejected"
                                ? "status-badge status-rejected"
                                : statusString === "Completed"
                                ? "status-badge status-completed"
                                : ""
                            }
                          >
                            {statusString}
                          </span>
                        </td>
                        <td>
                          {statusString === "Approved" && (
                            <button
                              onClick={openCancelModal}
                              disabled={isCanceling(b.ipfsHash)}
                              className="cancel-btn-table"
                            >
                              {isCanceling(b.ipfsHash) ? "Canceling..." : "Cancel"}
                            </button>
                          )}
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>

            {/* Pagination Controls */}
            {totalPages > 1 && (
              <div className="pagination-container">
                <div className="pagination-info">
                  Showing {page * PAGE_SIZE + 1}
                  -
                  {Math.min((page + 1) * PAGE_SIZE, filteredAndSortedBookings.length)}
                  {" "}of {filteredAndSortedBookings.length} results
                </div>
                <div className="pagination-controls">
                  <button
                    onClick={() => setPage((p) => Math.max(0, p - 1))}
                    disabled={page === 0}
                    className="pagination-btn pagination-prev"
                    title="Previous page"
                  >
                    Prev
                  </button>
                  <div className="pagination-numbers">
                    {getPageNumbers().map((pageNumber, idx) => (
                      <button
                        key={idx}
                        onClick={() =>
                          typeof pageNumber === 'number' && setPage(pageNumber - 1)
                        }
                        className={`pagination-number ${
                          pageNumber === page + 1 ? 'active' : ''
                        } ${typeof pageNumber !== 'number' ? 'ellipsis' : ''}`}
                        disabled={typeof pageNumber !== 'number'}
                      >
                        {pageNumber}
                      </button>
                    ))}
                  </div>
                  <button
                    onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
                    disabled={page >= totalPages - 1}
                    className="pagination-btn pagination-next"
                    title="Next page"
                  >
                    Next
                  </button>
                </div>
              </div>
            )}
          </div>
        )}
      </div>
      <Toast
        message={toast.msg}
        type={toast.type}
        onClose={() => setToast({ msg: "", type: "success" })}
      />
      <ConfirmModal
        open={modalOpen}
        onConfirm={confirmCancel}
        onCancel={() => { setModalOpen(false); setPendingCancel(null); }}
      />
    </div>
  );
};

export default UserBookings;