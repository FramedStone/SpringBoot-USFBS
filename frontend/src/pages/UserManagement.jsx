import React, { useState, useEffect } from 'react';
import Navbar from "@components/Navbar";
import Toast from "@components/Toast";
import { authFetch } from "@utils/authFetch";
import '@styles/UserManagement.css';

const UserManagement = () => {
  const [activeTab, setActiveTab] = useState("users");
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [showBanModal, setShowBanModal] = useState(false);
  const [showUnbanModal, setShowUnbanModal] = useState(false);
  const [selectedUser, setSelectedUser] = useState(null);
  const [banReason, setBanReason] = useState('');
  const [unbanReason, setUnbanReason] = useState('');
  const [toast, setToast] = useState({ open: false, message: '', type: 'success' });
  const [processingUsers, setProcessingUsers] = useState(new Set());
  const [modalLoading, setModalLoading] = useState(false);

  const abbreviate = (value) => {
    if (!value) return '';
    return value.length > 16 ? value.slice(0, 6) + '...' + value.slice(-4) : value;
  };

  const handleToastMessage = (message, type) => {
    setToast({ open: true, message, type });
    setTimeout(() => setToast({ open: false, message: '', type: 'success' }), 3000);
  };

  const addProcessingUser = (userAddress) => {
    setProcessingUsers(prev => new Set([...prev, userAddress]));
  };

  const removeProcessingUser = (userAddress) => {
    setProcessingUsers(prev => {
      const newSet = new Set(prev);
      newSet.delete(userAddress);
      return newSet;
    });
  };

  const isUserProcessing = (userAddress) => {
    return processingUsers.has(userAddress);
  };

  const fetchUsers = async () => {
    try {
      setLoading(true);
      const response = await authFetch(`${import.meta.env.VITE_BACKEND_URL}/api/admin/users`);
      
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      
      const result = await response.json();
      
      if (result.success) {
        setUsers(result.data);
        console.log(`Fetched ${result.data.length} users from blockchain`);
      } else {
        throw new Error(result.error || 'Failed to fetch users');
      }
    } catch (error) {
      console.error('Error fetching users:', error);
      handleToastMessage('Failed to fetch users: ' + error.message, 'error');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchUsers();
  }, []);

  const filteredUsers = users.filter(user =>
    user.userAddress.toLowerCase().includes(searchTerm.toLowerCase()) ||
    (user.email && user.email.toLowerCase().includes(searchTerm.toLowerCase())) ||
    (user.banReason && user.banReason.toLowerCase().includes(searchTerm.toLowerCase()))
  );

  const handleBanClick = (user) => {
    setSelectedUser(user);
    setBanReason('');
    setShowBanModal(true);
  };

  const handleUnbanClick = (user) => {
    setSelectedUser(user);
    setUnbanReason('');
    setShowUnbanModal(true);
  };

  const handleBanConfirm = async () => {
    if (!banReason.trim() || modalLoading) return;
    
    try {
      setModalLoading(true);
      addProcessingUser(selectedUser.userAddress);
      
      const response = await authFetch(`${import.meta.env.VITE_BACKEND_URL}/api/admin/users/ban`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ 
          userAddress: selectedUser.userAddress,
          reason: banReason.trim()
        }),
      });

      const result = await response.json();

      if (result.success) {
        handleToastMessage(`User ${selectedUser.userAddress} has been banned successfully`, 'success');
        await fetchUsers();
        setShowBanModal(false);
        setSelectedUser(null);
        setBanReason('');
      } else {
        throw new Error(result.error || 'Failed to ban user');
      }
    } catch (error) {
      console.error('Error banning user:', error);
      handleToastMessage('Failed to ban user: ' + error.message, 'error');
    } finally {
      setModalLoading(false);
      removeProcessingUser(selectedUser?.userAddress);
    }
  };

  const handleUnbanConfirm = async () => {
    if (!unbanReason.trim() || modalLoading) return;
    
    try {
      setModalLoading(true);
      addProcessingUser(selectedUser.userAddress);
      
      const response = await authFetch(`${import.meta.env.VITE_BACKEND_URL}/api/admin/users/unban`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ 
          userAddress: selectedUser.userAddress,
          reason: unbanReason.trim()
        }),
      });

      const result = await response.json();

      if (result.success) {
        handleToastMessage(`User ${selectedUser.userAddress} has been unbanned successfully`, 'success');
        await fetchUsers();
        setShowUnbanModal(false);
        setSelectedUser(null);
        setUnbanReason('');
      } else {
        throw new Error(result.error || 'Failed to unban user');
      }
    } catch (error) {
      console.error('Error unbanning user:', error);
      handleToastMessage('Failed to unban user: ' + error.message, 'error');
    } finally {
      setModalLoading(false);
      removeProcessingUser(selectedUser?.userAddress);
    }
  };

  const handleBanModalClose = () => {
    if (modalLoading) return;
    setShowBanModal(false);
    setSelectedUser(null);
    setBanReason('');
  };

  const handleUnbanModalClose = () => {
    if (modalLoading) return;
    setShowUnbanModal(false);
    setSelectedUser(null);
    setUnbanReason('');
  };

  const getStatusDisplay = (user) => {
    if (user.isBanned) {
      return 'BANNED';
    } else if (user.isRegistered) {
      return 'ACTIVE';
    } else {
      return 'NOT_REGISTERED';
    }
  };

  const getStatusColor = (user) => {
    if (user.isBanned) {
      return '#ef4444';
    } else if (user.isRegistered) {
      return '#10b981';
    } else {
      return '#6b7280';
    }
  };

  if (loading) {
    return (
      <div className="um-user-management-layout">
        <Navbar activeTab={activeTab} setActiveTab={setActiveTab} />
        <div className="um-user-management-content">
          <div className="um-loading">
            <p>Loading users...</p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="um-user-management-layout">
      <Navbar activeTab={activeTab} setActiveTab={setActiveTab} />
      <div className="um-user-management-content">
        <div className="um-header">
          <div className="um-header-controls um-header-controls-center">
            <input
              type="text"
              placeholder="Search users by address, email, or ban reason..."
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
                  <th>Status</th>
                  <th>Ban Reason</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {filteredUsers.length === 0 ? (
                  <tr>
                    <td colSpan="5" style={{ textAlign: 'center', padding: '24px' }}>
                      {searchTerm ? 'No users found matching your search.' : 'No users found.'}
                    </td>
                  </tr>
                ) : (
                  filteredUsers.map((user, index) => (
                    <tr key={user.userAddress || index} className={user.isBanned ? 'um-banned-row' : ''}>
                      <td>
                        <div className="um-address-cell">
                          <span className="um-address-text um-address-desktop">
                            {user.userAddress}
                          </span>
                          <span className="um-address-text um-address-mobile">
                            {abbreviate(user.userAddress)}
                          </span>
                        </div>
                      </td>
                      <td>{user.email || '-'}</td>
                      <td>
                        <span style={{ 
                          color: getStatusColor(user), 
                          fontWeight: '500',
                          fontSize: '13px'
                        }}>
                          {getStatusDisplay(user)}
                        </span>
                      </td>
                      <td className="um-reasons-cell">
                        {user.banReason || '-'}
                      </td>
                      <td>
                        {user.isBanned ? (
                          <button
                            className="um-unban-btn"
                            onClick={() => handleUnbanClick(user)}
                            disabled={isUserProcessing(user.userAddress)}
                          >
                            {isUserProcessing(user.userAddress) ? 'Processing...' : 'Unban'}
                          </button>
                        ) : (
                          <button
                            className="um-ban-btn"
                            onClick={() => handleBanClick(user)}
                            disabled={isUserProcessing(user.userAddress)}
                          >
                            {isUserProcessing(user.userAddress) ? 'Processing...' : 'Ban'}
                          </button>
                        )}
                      </td>
                    </tr>
                  ))
                )}
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
                <button className="um-close-btn" onClick={handleBanModalClose} disabled={modalLoading}>
                  ×
                </button>
              </div>
              <div className="um-modal-content">
                <div className="um-user-info">
                  <p><strong>Blockchain Address:</strong> {selectedUser?.userAddress}</p>
                  <p><strong>Email:</strong> {selectedUser?.email || 'Unknown'}</p>
                  <p><strong>Status:</strong> {selectedUser ? getStatusDisplay(selectedUser) : '-'}</p>
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
                    disabled={modalLoading}
                  />
                </div>
              </div>
              <div className="um-modal-footer">
                <button 
                  className="um-cancel-btn" 
                  onClick={handleBanModalClose}
                  disabled={modalLoading}
                >
                  Cancel
                </button>
                <button
                  className="um-confirm-ban-btn"
                  onClick={handleBanConfirm}
                  disabled={!banReason.trim() || modalLoading}
                >
                  {modalLoading ? 'Banning...' : 'Confirm Ban'}
                </button>
              </div>
            </div>
          </div>
        )}

        {/* Unban Modal */}
        {showUnbanModal && (
          <div className="um-modal-overlay">
            <div className="um-modal">
              <div className="um-modal-header">
                <h2>Unban User</h2>
                <button className="um-close-btn" onClick={handleUnbanModalClose} disabled={modalLoading}>
                  ×
                </button>
              </div>
              <div className="um-modal-content">
                <div className="um-user-info">
                  <p><strong>Blockchain Address:</strong> {selectedUser?.userAddress}</p>
                  <p><strong>Email:</strong> {selectedUser?.email || 'Unknown'}</p>
                  <p><strong>Current Status:</strong> {selectedUser ? getStatusDisplay(selectedUser) : '-'}</p>
                  <p><strong>Current Ban Reason:</strong> {selectedUser?.banReason || 'Not specified'}</p>
                </div>
                <div className="um-reason-section">
                  <label htmlFor="unban-reason">Reason for Unban:</label>
                  <textarea
                    id="unban-reason"
                    className="um-reason-input"
                    value={unbanReason}
                    onChange={(e) => setUnbanReason(e.target.value)}
                    placeholder="Enter the reason for unbanning this user..."
                    rows="4"
                    disabled={modalLoading}
                  />
                </div>
              </div>
              <div className="um-modal-footer">
                <button 
                  className="um-cancel-btn" 
                  onClick={handleUnbanModalClose}
                  disabled={modalLoading}
                >
                  Cancel
                </button>
                <button
                  className="um-confirm-unban-btn"
                  onClick={handleUnbanConfirm}
                  disabled={!unbanReason.trim() || modalLoading}
                >
                  {modalLoading ? 'Unbanning...' : 'Confirm Unban'}
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