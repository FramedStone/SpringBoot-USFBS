import React, { useState, useMemo } from 'react';
import '@styles/SystemLogs.css';

const abbreviate = (value) => {
  if (!value) return '';
  return value.length > 16 ? value.slice(0, 6) + '...' + value.slice(-4) : value;
};

const SystemLogs = () => {
  const [searchTerm, setSearchTerm] = useState('');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [selectedActions, setSelectedActions] = useState({
    'Form Received': true,
    'Rejected Booking': true,
    'Approved Booking': true,
    'Added Announcement': true
  });

  // Sample data with blockchain addresses and IPFS CIDs
  const logsData = [
    {
      id: 'QmX7vQJ8KfGxRtN2pL9wYzHm5Fq3Bs4CvDnE8A1rT6kM',
      action: 'Form Received',
      user: '0x742d35C5F6c4B8c9d8E1F3A2B5C7D9E4F6G8H2I3J5K',
      timestamp: '2025-04-29 14:32',
      note: 'Complaint'
    },
    {
      id: 'QmY8wRK9LgHyStO3qM0xZnI6Gr4Ct5DwEnF9B2sU7lN',
      action: 'Rejected Booking',
      user: '0xA1B2C3D4E5F6789012345678901234567890ABCD',
      timestamp: '2025-04-29 14:40',
      note: 'Booking #100 Rejected'
    },
    {
      id: 'QmZ9xSL0MhIzTuP4rN1yAoJ7Hs5Du6ExFoG0C3tV8mO',
      action: 'Approved Booking',
      user: '0xB2C3D4E5F6789012345678901234567890ABCDEF',
      timestamp: '2025-04-29 15:00',
      note: 'Booking #101 Approved'
    },
    {
      id: 'QmA0yTM1NiJ0UvQ5sO2zBpK8It6Ev7FyGpH1D4uW9nP',
      action: 'Added Announcement',
      user: '0xC3D4E5F6789012345678901234567890ABCDEF12',
      timestamp: '2025-04-29 15:30',
      note: 'Basketball Tournament'
    }
  ];

  const handleActionChange = (action) => {
    setSelectedActions(prev => ({
      ...prev,
      [action]: !prev[action]
    }));
  };

  const clearFilters = () => {
    setSearchTerm('');
    setStartDate('');
    setEndDate('');
    setSelectedActions({
      'Form Received': true,
      'Rejected Booking': true,
      'Approved Booking': true,
      'Added Announcement': true
    });
  };

  const filteredLogs = useMemo(() => {
    return logsData.filter(log => {
      // Search filter
      const matchesSearch = searchTerm === '' || 
        log.id.toLowerCase().includes(searchTerm.toLowerCase()) ||
        log.action.toLowerCase().includes(searchTerm.toLowerCase()) ||
        log.user.toLowerCase().includes(searchTerm.toLowerCase()) ||
        log.note.toLowerCase().includes(searchTerm.toLowerCase());

      // Action filter
      const matchesAction = selectedActions[log.action];

      // Date filter
      const logDate = new Date(log.timestamp);
      const start = startDate ? new Date(startDate) : null;
      const end = endDate ? new Date(endDate) : null;
      
      const matchesDate = (!start || logDate >= start) && (!end || logDate <= end);

      return matchesSearch && matchesAction && matchesDate;
    });
  }, [searchTerm, selectedActions, startDate, endDate]);

  return (
    <div className="system-logs">
      <h1 className="page-title">System Logs</h1>
      
      <div className="filters-container">
        {/* Search Bar */}
        <div className="search-section">
          <input
            type="text"
            placeholder="Search logs..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="search-input"
          />
        </div>

        {/* Timestamp Filter */}
        <div className="filter-section">
          <h3>Timestamp Range</h3>
          <div className="date-inputs">
            <input
              type="date"
              value={startDate}
              onChange={(e) => setStartDate(e.target.value)}
              className="date-input"
              placeholder="Start Date"
            />
            <span className="date-separator">to</span>
            <input
              type="date"
              value={endDate}
              onChange={(e) => setEndDate(e.target.value)}
              className="date-input"
              placeholder="End Date"
            />
          </div>
        </div>

        {/* Action Filter */}
        <div className="filter-section">
          <h3>Action Type</h3>
          <div className="checkbox-group">
            {Object.keys(selectedActions).map(action => (
              <label key={action} className="checkbox-label">
                <input
                  type="checkbox"
                  checked={selectedActions[action]}
                  onChange={() => handleActionChange(action)}
                  className="checkbox-input"
                />
                <span className="checkbox-text">{action}</span>
              </label>
            ))}
          </div>
        </div>

        {/* Clear Filters Button */}
        <div className="clear-filter-section">
          <button onClick={clearFilters} className="clear-filters-btn">
            Clear All Filters
          </button>
        </div>
      </div>

      {/* Logs Table */}
      <div className="table-container">
        <table className="logs-table">
          <thead>
            <tr>
              <th>IPFS CID</th>
              <th>Action</th>
              <th>Blockchain Address</th>
              <th>Timestamp</th>
              <th>Note</th>
            </tr>
          </thead>
          <tbody>
            {filteredLogs.length > 0 ? (
              filteredLogs.map((log) => (
                <tr key={log.id}>
                  <td className="cid-cell">
                    <span className="cid-desktop">{log.id}</span>
                    <span className="cid-mobile">{abbreviate(log.id)}</span>
                  </td>
                  <td>{log.action}</td>
                  <td className="address-cell">
                    <span className="address-desktop">{log.user}</span>
                    <span className="address-mobile">{abbreviate(log.user)}</span>
                  </td>
                  <td>{log.timestamp}</td>
                  <td>{log.note}</td>
                </tr>
              ))
            ) : (
              <tr>
                <td colSpan="5" className="no-results">
                  No logs found matching your criteria
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default SystemLogs;