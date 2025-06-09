import React, { useState, useMemo, useEffect } from 'react';
import { ChevronUp, ChevronDown } from 'lucide-react';
import { authFetch } from '@utils/authFetch';
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
    'Booking Created': true,
    'Booking Updated': true,
    'Booking Deleted': true,
    'Announcement Added': true,
    'Announcement Deleted': true,
    'Announcement Modified': true,
    'Announcement Time Modified': true,
    'Announcement Requested': true,
    'User Added': true,
    'User Banned': true,
    'User Unbanned': true,
    'Sport Facility Added': true,
    'Sport Facility Modified': true,
    'Sport Facility Deleted': true,
    'Court Added': true,
    'Court Modified': true,
    'Court Deleted': true,
    'Facility Details Requested': true,
    'Court Details Requested': true
  });
  const [sortBy, setSortBy] = useState({
    field: 'dateAdded',
    order: 'desc'
  });
  const [logsData, setLogsData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [lastUpdate, setLastUpdate] = useState(null);

  // Fetch event logs from backend
  useEffect(() => {
    const fetchEventLogs = async () => {
      try {
        setLoading(true);
        const response = await authFetch(`${import.meta.env.VITE_BACKEND_URL}/logs`);
        
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const data = await response.json();
        
        // Transform backend data to frontend format
        const transformedData = data.map(log => ({
          id: log.ipfsHash || `event-${Date.now()}-${Math.random()}`,
          action: log.action,
          email: log.fromAddress,
          timestamp: log.timestamp,
          originalOutput: log.originalOutput,
          dateAdded: new Date(log.dateAdded),
          eventType: log.eventType
        }));
        
        setLogsData(transformedData);
        setError(null);
        setLastUpdate(new Date());
        
        console.log(`Fetched ${transformedData.length} event logs from backend`);
      } catch (err) {
        console.error('Error fetching event logs:', err);
        setError('Failed to fetch event logs from blockchain');
      } finally {
        setLoading(false);
      }
    };

    fetchEventLogs();
    
    // Set up polling for real-time updates - TODO: Consider WebSocket implementation
    const interval = setInterval(fetchEventLogs, 10000);
    
    return () => clearInterval(interval);
  }, []);

  const handleActionChange = (action) => {
    setSelectedActions(prev => ({
      ...prev,
      [action]: !prev[action]
    }));
  };

  const handleSortChange = () => {
    setSortBy(prev => ({
      ...prev,
      order: prev.order === 'asc' ? 'desc' : 'asc'
    }));
  };

  const clearFilters = () => {
    setSearchTerm('');
    setStartDate('');
    setEndDate('');
    setSelectedActions({
      'Booking Created': true,
      'Booking Updated': true,
      'Booking Deleted': true,
      'Announcement Added': true,
      'Announcement Deleted': true,
      'Announcement Modified': true,
      'Announcement Time Modified': true,
      'Announcement Requested': true,
      'User Added': true,
      'User Banned': true,
      'User Unbanned': true,
      'Sport Facility Added': true,
      'Sport Facility Modified': true,
      'Sport Facility Deleted': true,
      'Court Added': true,
      'Court Modified': true,
      'Court Deleted': true,
      'Facility Details Requested': true,
      'Court Details Requested': true
    });
    setSortBy({
      field: 'dateAdded',
      order: 'desc'
    });
  };

  const filteredAndSortedLogs = useMemo(() => {
    const filtered = logsData.filter(log => {
      // Search filter
      const matchesSearch = searchTerm === '' ||
        log.id.toLowerCase().includes(searchTerm.toLowerCase()) ||
        log.action.toLowerCase().includes(searchTerm.toLowerCase()) ||
        log.email.toLowerCase().includes(searchTerm.toLowerCase()) ||
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

    return filtered.sort((a, b) => {
      if (sortBy.field === 'dateAdded') {
        const dateA = a.dateAdded;
        const dateB = b.dateAdded;
        
        if (sortBy.order === 'asc') {
          return dateA - dateB;
        } else {
          return dateB - dateA;
        }
      }
      return 0;
    });
  }, [logsData, searchTerm, selectedActions, startDate, endDate, sortBy]);

  if (loading && logsData.length === 0) {
    return (
      <div className="system-logs">
        <h1 className="page-title">System Logs</h1>
        <div className="loading-container">
          <p>Loading blockchain event logs...</p>
        </div>
      </div>
    );
  }

  if (error && logsData.length === 0) {
    return (
      <div className="system-logs">
        <h1 className="page-title">System Logs</h1>
        <div className="error-container">
          <p>Error: {error}</p>
          <button onClick={() => window.location.reload()}>Retry</button>
        </div>
      </div>
    );
  }

  return (
    <div className="system-logs">
      <h1 className="page-title">System Logs</h1>
      
      {lastUpdate && (
        <div className="status-bar">
          <span className="status-text">
            Last updated: {lastUpdate.toLocaleTimeString()} | 
            Total events: {logsData.length} | 
            Filtered: {filteredAndSortedLogs.length}
          </span>
        </div>
      )}

      <div className="filters-container">
        {/* Search Bar */}
        <div className="search-section">
          <input
            type="text"
            placeholder="Search logs by IPFS CID, action, address, or note..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="search-input"
          />
        </div>

        {/* Sort By Section */}
        <div className="filter-section">
          <h3>Sort By</h3>
          <div className="sort-controls">
            <button
              onClick={handleSortChange}
              className={`sort-btn ${sortBy.field === 'dateAdded' ? 'active' : ''}`}
              type="button"
            >
              Date Added
              {sortBy.field === 'dateAdded' && (
                sortBy.order === 'asc' ? <ChevronUp size={16} /> : <ChevronDown size={16} />
              )}
            </button>
          </div>
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
              <th>IPFS CID / Event ID</th>
              <th>Action</th>
              <th>From Address</th>
              <th>Timestamp</th>
              <th>Note</th>
            </tr>
          </thead>
          <tbody>
            {filteredAndSortedLogs.length > 0 ? (
              filteredAndSortedLogs.map((log) => (
                <tr key={`${log.id}-${log.dateAdded.getTime()}`}>
                  <td className="cid-cell">
                    <span className="cid-desktop">{log.id}</span>
                    <span className="cid-mobile">{abbreviate(log.id)}</span>
                  </td>
                  <td>
                    <span className={`action-badge ${log.eventType?.toLowerCase()}`}>
                      {log.action}
                    </span>
                  </td>
                  <td className="email-cell">
                    <span className="email-desktop">{log.email}</span>
                    <span className="email-mobile">{abbreviate(log.email)}</span>
                  </td>
                  <td>{log.timestamp}</td>
                  <td className="original-output-cell">
                    <details>
                      <summary>View Details</summary>
                      <pre className="original-output">{log.originalOutput}</pre>
                    </details>
                  </td>
                </tr>
              ))
            ) : (
              <tr>
                <td colSpan="5" className="no-results">
                  No blockchain events found matching your criteria
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