import React, { useState, useEffect } from 'react';
import Navbar from "@components/Navbar";
import Toast from "@components/Toast";
import Spinner from "@components/Spinner";
import { authFetch } from "@utils/authFetch";
import { useWeb3Auth } from "@web3auth/modal/react";
import '@styles/UserBookings.css';

const PAGE_SIZE = 10;

const UserBookings = () => {
  const [activeTab, setActiveTab] = useState("bookings");
  const [toast, setToast] = useState({ msg: "", type: "success" });
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(false);
  const [userRole, setUserRole] = useState("");
  const [ethAddress, setEthAddress] = useState("");
  const [page, setPage] = useState(0);

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
          setUserRole(data.role || "User");
        }
      } catch {
        setUserRole("User");
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
                {pagedBookings.map((b, idx) => (
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
                    <td>
                      {(() => {
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
                      })()}
                    </td>
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
                  </tr>
                ))}
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
    </div>
  );
};

export default UserBookings;