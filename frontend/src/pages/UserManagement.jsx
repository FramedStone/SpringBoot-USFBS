import React, { useState } from 'react';
import Navbar from "@components/Navbar";
import Toast from "@components/Toast";
import '@styles/UserManagement.css';

const UserManagement = () => {
  const [activeTab, setActiveTab] = useState("users");
  const [users, setUsers] = useState([
    {
      id: 1,
      blockchainAddress: '0x1a2b3c4d5e6f7890abcdef1234567890abcdef12',
      email: '123120100@mmu.com',
      reasons: '',
      isBanned: false
    },
    {
      id: 2,
      blockchainAddress: '0x2b3c4d5e6f7890abcdef1234567890abcdef1234',
      email: '123120101@mmu.com',
      reasons: '',
      isBanned: false
    },
    {
      id: 3,
      blockchainAddress: '0x3c4d5e6f7890abcdef1234567890abcdef123456',
      email: '123120102@mmu.com',
      reasons: '',
      isBanned: false
    },
    {
      id: 4,
      blockchainAddress: '0x4d5e6f7890abcdef1234567890abcdef12345678',
      email: '123120103@mmu.com',
      reasons: 'Violation of Facility Rules',
      isBanned: true
    },
    {
      id: 5,
      blockchainAddress: '0x5e6f7890abcdef1234567890abcdef1234567890',
      email: '123120104@mmu.com',
      reasons: 'No-Shows Without Valid Reasons',
      isBanned: true
    }
  ]);

  const [searchTerm, setSearchTerm] = useState('');
  const [showBanModal, setShowBanModal] = useState(false);
  const [selectedUser, setSelectedUser] = useState(null);
  const [banReason, setBanReason] = useState('');
  const [toast, setToast] = useState({ open: false, message: '', type: 'success' });

  const abbreviate = (value) => {
    if (!value) return '';
    return value.length > 16 ? value.slice(0, 6) + '...' + value.slice(-4) : value;
  };

  const handleCopy = (value) => {
    try {
      navigator.clipboard.writeText(value);
      setToast({ open: true, message: 'Copied to clipboard!', type: 'success' });
      setTimeout(() => setToast({ open: false, message: '', type: 'success' }), 1500);
    } catch (err) {
      setToast({ open: true, message: 'Failed to copy.', type: 'error' });
      setTimeout(() => setToast({ open: false, message: '', type: 'error' }), 1500);
    }
  };

  const filteredUsers = users.filter(user =>
    user.blockchainAddress.toLowerCase().includes(searchTerm.toLowerCase()) ||
    user.email.toLowerCase().includes(searchTerm.toLowerCase()) ||
    (user.reasons && user.reasons.toLowerCase().includes(searchTerm.toLowerCase()))
  );

  const handleBanClick = (user) => {
    setSelectedUser(user);
    setBanReason('');
    setShowBanModal(true);
  };

  const handleUnbanClick = (userId) => {
    setUsers(users.map(user =>
      user.id === userId
        ? { ...user, isBanned: false, reasons: '' }
        : user
    ));
  };

  const handleBanConfirm = () => {
    if (banReason.trim()) {
      setUsers(users.map(user =>
        user.id === selectedUser.id
          ? { ...user, isBanned: true, reasons: banReason }
          : user
      ));
      setShowBanModal(false);
      setSelectedUser(null);
      setBanReason('');
    }
  };

  const handleModalClose = () => {
    setShowBanModal(false);
    setSelectedUser(null);
    setBanReason('');
  };

  return (
    <div className="um-user-management-layout">
      <Navbar activeTab={activeTab} setActiveTab={setActiveTab} />
      <div className="um-user-management-content">
        <div className="um-header">
          <div className="um-header-controls um-header-controls-center">
            <input
              type="text"
              placeholder="Search users by address, email, or reason..."
              className="um-search-input"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </div>
        </div>

        <div className="um-table-container um-table-container-full">
          <div className="um-table-responsive">
            <table className="um-users-table">
              <thead>
                <tr>
                  <th>Blockchain Address</th>
                  <th>Email</th>
                  <th>Reasons</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {filteredUsers.map(user => (
                  <tr key={user.id} className={user.isBanned ? 'um-banned-row' : ''}>
                    <td>
                      <div className="um-address-cell">
                        {/* Desktop: show full address */}
                        <span className="um-address-text um-address-desktop">
                          {user.blockchainAddress}
                        </span>
                        {/* Mobile: show abbr address only */}
                        <span className="um-address-text um-address-mobile">
                          {abbreviate(user.blockchainAddress)}
                        </span>
                      </div>
                    </td>
                    <td>{user.email}</td>
                    <td className="um-reasons-cell">{user.reasons || '-'}</td>
                    <td>
                      {user.isBanned ? (
                        <button
                          className="um-unban-btn"
                          onClick={() => handleUnbanClick(user.id)}
                        >
                          Unban
                        </button>
                      ) : (
                        <button
                          className="um-ban-btn"
                          onClick={() => handleBanClick(user)}
                        >
                          Ban
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>

        {/* Ban Modal */}
        {showBanModal && (
          <div className="um-modal-overlay">
            <div className="um-modal">
              <div className="um-modal-header">
                <h2>Ban User</h2>
                <button className="um-close-btn" onClick={handleModalClose}>×</button>
              </div>
              <div className="um-modal-content">
                <div className="um-user-info">
                  <p><strong>Blockchain Address:</strong> {selectedUser?.blockchainAddress}</p>
                  <p><strong>Email:</strong> {selectedUser?.email}</p>
                </div>
                <div className="um-reason-section">
                  <label htmlFor="ban-reason">Reason for Ban:</label>
                  <textarea
                    id="ban-reason"
                    className="um-reason-input"
                    value={banReason}
                    onChange={(e) => setBanReason(e.target.value)}
                    placeholder="Enter the reason for banning this user..."
                    rows="4"
                  />
                </div>
              </div>
              <div className="um-modal-footer">
                <button className="um-cancel-btn" onClick={handleModalClose}>Cancel</button>
                <button
                  className="um-confirm-ban-btn"
                  onClick={handleBanConfirm}
                  disabled={!banReason.trim()}
                >
                  Confirm Ban
                </button>
              </div>
            </div>
          </div>
        )}
        <Toast
          open={toast.open}
          message={toast.message}
          type={toast.type}
          onClose={() => setToast({ ...toast, open: false })}
        />
      </div>
    </div>
  );
};

export default UserManagement;