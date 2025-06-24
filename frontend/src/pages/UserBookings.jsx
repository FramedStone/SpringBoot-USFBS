import React, { useState, useEffect } from 'react';
import Navbar from "@components/Navbar";
import Toast from "@components/Toast";
import Spinner from "@components/Spinner";
import { authFetch } from "@utils/authFetch";
import { useWeb3Auth } from "@web3auth/modal/react";
import '@styles/UserBookings.css';

// Modal component for confirmation
const ConfirmModal = ({ open, onConfirm, onCancel }) => {
  if (!open) return null;
  return (
    <div className="modal-overlay">
      <div className="modal-content">
        <h4>Cancel Booking</h4>
        <p>Are you sure you want to cancel this booking?</p>
        <div className="modal-actions">
          <button onClick={onCancel} className="modal-btn cancel">No</button>
          <button onClick={onConfirm} className="modal-btn confirm">Yes, Cancel</button>
        </div>
      </div>
    </div>
  );
};

const PAGE_SIZE = 10;

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

  const { web3Auth } = useWeb3Auth();

  // Fetch user role and eth address
  useEffect(() => {
    const fetchRoleAndAddress = async () => {
      try {
        // Get user role from backend
        const backendUrl = import.meta.env.VITE_BACKEND_URL || "http://localhost:8080";
        const res = await authFetch(`${backendUrl}/api/auth/me`);
        if (res.ok) {
          const data = await res.json();
          setUserRole(data.role);
        }
      } catch {
        setUserRole("");
      }
      // Get eth address from Web3Auth
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

  // Fetch bookings when role/address/page changes
  useEffect(() => {
    const fetchBookings = async () => {
      setLoading(true);
      try {
        const backendUrl = import.meta.env.VITE_BACKEND_URL || "http://localhost:8080";
        let url = "";
        let options = { headers: { "Content-Type": "application/json" } };

        if (userRole === "Admin") {
          url = `${backendUrl}/api/admin/bookings`;
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
    if ((userRole === "Admin") || (userRole === "User" && ethAddress)) {
      fetchBookings();
    }
  }, [userRole, ethAddress]);

  // Pagination
  const totalPages = Math.ceil(bookings.length / PAGE_SIZE);
  const pagedBookings = bookings.slice(page * PAGE_SIZE, (page + 1) * PAGE_SIZE);

  // Confirm cancel handler
  const confirmCancel = async () => {
    if (!pendingCancel) return;
    const { b, cancelEndpoint } = pendingCancel;
    try {
      const backendUrl = import.meta.env.VITE_BACKEND_URL || "http://localhost:8080";
      const res = await authFetch(`${backendUrl}${cancelEndpoint}`, {
        method: "POST",
        headers: { "Content-Type": "application/json" }
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
      setModalOpen(false);
      setPendingCancel(null);
    }
  };

  return (
    <div className="user-bookings-container">
      <Navbar activeTab={activeTab} setActiveTab={setActiveTab} navType="home" />
      <div className="bookings-content">
        {loading ? (
          <Spinner />
        ) : pagedBookings.length === 0 ? (
          <div className="bookings-placeholder">
            <h3>Bookings Page</h3>
            <p>No bookings found.</p>
          </div>
        ) : (
          <div>
            <table className="bookings-table">
              <thead>
                <tr>
                  <th>Facility</th>
                  <th>Court</th>
                  <th>Date</th>
                  <th>Start Time</th>
                  <th>End Time</th>
                  <th>Status</th>
                  <th>IPFS Hash</th>
                </tr>
              </thead>
              <tbody>
                {pagedBookings.map((b, idx) => {
                  const statusString = (() => {
                    // Manual status conversion
                    if (typeof b.status === "string") {
                      switch (b.status) {
                        case "0": return "Approved";
                        case "1": return "Rejected";
                        case "2": return "Completed";
                        case "3": return "Cancelled";
                        default: return "-";
                      }
                    }
                    if (typeof b.status === "string") {
                      return b.status.charAt(0).toUpperCase() + b.status.slice(1).toLowerCase();
                    }
                    return "-";
                  })();

                  // Determine cancel endpoint based on role
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
                      <td>{statusString}</td>
                      <td>
                        {b.ipfsHash ? (
                          <>
                            <a
                              href={`https://gateway.pinata.cloud/ipfs/${b.ipfsHash}`}
                              target="_blank"
                              rel="noopener noreferrer"
                            >
                              {b.ipfsHash.slice(0, 6)}...{b.ipfsHash.slice(-4)}
                            </a>
                            {statusString === "Approved" && (
                              <button
                                style={{ marginLeft: 8 }}
                                onClick={openCancelModal}
                              >
                                Cancel
                              </button>
                            )}
                          </>
                        ) : "-"}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
            <div className="pagination">
              <button
                onClick={() => setPage((p) => Math.max(0, p - 1))}
                disabled={page === 0}
              >
                Prev
              </button>
              <span>
                Page {page + 1} of {totalPages}
              </span>
              <button
                onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
                disabled={page >= totalPages - 1}
              >
                Next
              </button>
            </div>
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