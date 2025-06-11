import React, { useState, useMemo, useEffect } from 'react';
import { ChevronUp, ChevronDown, ChevronLeft, ChevronRight, Copy, ExternalLink } from 'lucide-react';
import { authFetch } from '@utils/authFetch';
import Toast from '@components/Toast';
import '@styles/SystemLogs.css';

// IPFS utility functions
const abbreviateCid = (cid, prefixLength = 6, suffixLength = 4) => {
  if (!cid || typeof cid !== 'string') return '';
  
  const minLength = prefixLength + suffixLength + 3;
  if (cid.length <= minLength) return cid;
  
  return `${cid.slice(0, prefixLength)}...${cid.slice(-suffixLength)}`;
};

const getGatewayUrl = (cid, gateway = 'https://gateway.pinata.cloud') => {
  if (!cid) return '';
  return `${gateway}/ipfs/${cid}`;
};

// Component for IPFS hash display with copy and gateway link
const IpfsHashCell = ({ hash, isOld = false, onCopy }) => {
  const [copied, setCopied] = useState(false);
  
  const handleCopy = async (e) => {
    e.stopPropagation();
    if (!hash || hash === '-') return;
    
    try {
      await navigator.clipboard.writeText(hash);
      setCopied(true);
      onCopy('IPFS hash copied to clipboard!', 'success');
      setTimeout(() => setCopied(false), 2000);
    } catch (err) {
      console.error('Failed to copy:', err);
      onCopy('Failed to copy IPFS hash', 'error');
    }
  };

  const handleGatewayOpen = (e) => {
    e.stopPropagation();
    if (!hash || hash === '-') return;
    
    const gatewayUrl = getGatewayUrl(hash);
    window.open(gatewayUrl, '_blank', 'noopener,noreferrer');
    onCopy('Opening IPFS content in new tab', 'success');
  };

  if (!hash || hash === '-') {
    return <span className="ipfs-hash-empty">-</span>;
  }

  return (
    <div className={`ipfs-hash-container ${isOld ? 'old-hash' : 'new-hash'}`}>
      <span className="ipfs-hash-desktop">
        <span className="hash-text" title={hash}>{abbreviateCid(hash, 8, 4)}</span>
        <div className="hash-actions visible">
          <button 
            className={`copy-btn ${copied ? 'copied' : ''}`}
            onClick={handleCopy}
            title={copied ? 'Copied!' : 'Copy full hash'}
          >
            <Copy size={12} />
          </button>
          <button 
            className="gateway-btn"
            onClick={handleGatewayOpen}
            title="Open in Pinata Gateway"
          >
            <ExternalLink size={12} />
          </button>
        </div>
      </span>
      <span className="ipfs-hash-mobile">
        <span className="hash-text" title={hash}>{abbreviateCid(hash, 4, 3)}</span>
        <div className="hash-actions visible">
          <button 
            className={`copy-btn ${copied ? 'copied' : ''}`}
            onClick={handleCopy}
            title={copied ? 'Copied!' : 'Copy full hash'}
          >
            <Copy size={10} />
          </button>
          <button 
            className="gateway-btn"
            onClick={handleGatewayOpen}
            title="Open in Pinata Gateway"
          >
            <ExternalLink size={10} />
          </button>
        </div>
      </span>
    </div>
  );
};

const SystemLogs = () => {
  const [searchTerm, setSearchTerm] = useState('');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [toast, setToast] = useState({ msg: "", type: "success" });
  const [selectedActions, setSelectedActions] = useState({
    'Booking Created': true,
    'Booking Updated': true,
    'Booking Deleted': true,
    'Booking Requested': true,
    'Announcement Added': true,
    'Announcement Deleted': true,
    'Announcement Modified': true,
    'Announcement IPFS Hash Modified': true, 
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
    field: 'timestamp',   
    order: 'desc'
  });
  const [logsData, setLogsData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [lastUpdate, setLastUpdate] = useState(null);
  const [currentPage, setCurrentPage] = useState(1);
  const ITEMS_PER_PAGE = 10;

  const handleToastMessage = (message, type) => {
    setToast({ msg: message, type });
  };

  // Fetch event logs from backend with enhanced user context
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
          fromAddress: log.fromAddress,
          email: log.email || '-',
          role: log.role || '-',
          timestamp: log.timestamp,
          originalOutput: log.originalOutput,
          dateAdded: new Date(log.dateAdded),
          eventType: log.eventType,
          oldIpfsHash: extractOldIpfsHash(log.originalOutput, log.action),
          newIpfsHash: extractNewIpfsHash(log.originalOutput, log.action, log.ipfsHash),
          note: extractNoteFromEvent(log.originalOutput, log.action)
        }));
        
        setLogsData(transformedData);
        setError(null);
        setLastUpdate(new Date());
        setCurrentPage(1);
        
        console.log(`Fetched ${transformedData.length} event logs with enhanced user context`);
      } catch (err) {
        console.error('Error fetching event logs:', err);
        setError('Failed to fetch event logs from blockchain');
      } finally {
        setLoading(false);
      }
    };

    fetchEventLogs();
    const interval = setInterval(fetchEventLogs, 10000);
    return () => clearInterval(interval);
  }, []);

  // Helper function to extract old IPFS hash from originalOutput
  const extractOldIpfsHash = (originalOutput, action) => {
    if (!originalOutput) return '-';
    
    if (action.includes('IPFS Hash Modified')) {
      const oldHashMatch = originalOutput.match(/ipfsHash_\s*=\s*([^\s\n,]+)/);
      if (oldHashMatch) return oldHashMatch[1];
      
      const oldHashMatch2 = originalOutput.match(/oldIpfsHash\s*=\s*([^\s\n,]+)/);
      if (oldHashMatch2) return oldHashMatch2[1];
    }
    
    if (action.includes('Booking Updated')) {
      const oldDataMatch = originalOutput.match(/oldData\s*=\s*([^\s\n,]+)/);
      if (oldDataMatch) return oldDataMatch[1];
    }
    
    const ipfsHashMatch = originalOutput.match(/ipfsHash\s*=\s*([^\s\n,]+)/);
    return ipfsHashMatch ? ipfsHashMatch[1] : '-';
  };

  // Helper function to extract new IPFS hash from originalOutput
  const extractNewIpfsHash = (originalOutput, action, fallbackHash) => {
    if (!originalOutput) return fallbackHash || '-';
    
    if (action.includes('IPFS Hash Modified')) {
      const newHashMatch = originalOutput.match(/ipfsHash\s*=\s*([^\s\n,]+)(?!_)/);
      if (newHashMatch) return newHashMatch[1];
      
      const newHashMatch2 = originalOutput.match(/newIpfsHash\s*=\s*([^\s\n,]+)/);
      if (newHashMatch2) return newHashMatch2[1];
      
      if (fallbackHash) return fallbackHash;
    }
    
    if (action.includes('Booking Updated')) {
      const newDataMatch = originalOutput.match(/newData\s*=\s*([^\s\n,]+)/);
      if (newDataMatch) return newDataMatch[1];
    }
    
    return fallbackHash || '-';
  };

  // Helper function to extract note from originalOutput based on event type
  const extractNoteFromEvent = (originalOutput, action) => {
    if (!originalOutput) return '';
    
    if (action === 'User Banned') {
      const noteMatch = originalOutput.match(/note\s*=\s*([^\n]+)/);
      return noteMatch ? noteMatch[1].trim() : 'User banned by admin';
    }
    
    if (action === 'User Unbanned') {
      const noteMatch = originalOutput.match(/note\s*=\s*([^\n]+)/);
      return noteMatch ? noteMatch[1].trim() : 'User unbanned by admin';
    }
    
    if (action === 'Booking Created') {
      const noteMatch = originalOutput.match(/note\s*=\s*([^\n]+)/);
      return noteMatch ? noteMatch[1].trim() : '';
    }
    
    if (action === 'Booking Updated') {
      const noteMatch = originalOutput.match(/note\s*=\s*([^\n]+)/);
      return noteMatch ? noteMatch[1].trim() : '';
    }
    
    if (action === 'Booking Deleted') {
      const noteMatch = originalOutput.match(/note\s*=\s*([^\n]+)/);
      return noteMatch ? noteMatch[1].trim() : '';
    }
    
    if (action === 'Facility Details Requested') {
      const noteMatch = originalOutput.match(/note\s*=\s*([^\n]+)/);
      return noteMatch ? noteMatch[1].trim() : '';
    }
    
    if (action === 'Court Details Requested') {
      const noteMatch = originalOutput.match(/note\s*=\s*([^\n]+)/);
      return noteMatch ? noteMatch[1].trim() : '';
    }
    
    return '';
  };

  const handleActionChange = (action) => {
    setSelectedActions(prev => ({
      ...prev,
      [action]: !prev[action]
    }));
    setCurrentPage(1);
  };

  const handleSortChange = () => {
    setSortBy(prev => ({
      ...prev,
      order: prev.order === 'asc' ? 'desc' : 'asc'
    }));
    setCurrentPage(1);
  };

  const clearFilters = () => {
    setSearchTerm('');
    setStartDate('');
    setEndDate('');
    setSelectedActions({
      'Booking Created': true,
      'Booking Updated': true,
      'Booking Deleted': true,
      'Booking Requested': true,
      'Announcement Added': true,
      'Announcement Deleted': true,
      'Announcement Modified': true,
      'Announcement IPFS Hash Modified': true, 
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
      field: 'timestamp',   
      order: 'desc'
    });
    setCurrentPage(1);
  };

  useEffect(() => {
    setCurrentPage(1);
  }, [searchTerm, startDate, endDate]);

  const getActionCategory = (action) => {
    const actionLower = action.toLowerCase();
    
    if (actionLower.includes('created') || actionLower.includes('added')) {
      return 'create';
    }
    
    if (actionLower.includes('updated') || actionLower.includes('modified') || 
        actionLower.includes('banned') || actionLower.includes('unbanned')) {
      return 'update';
    }
    
    if (actionLower.includes('deleted') || actionLower.includes('removed')) {
      return 'delete';
    }
    
    if (actionLower.includes('requested') || actionLower.includes('details')) {
      return 'read';
    }
    
    return 'read';
  };

  const filteredAndSortedLogs = useMemo(() => {
    const filtered = logsData.filter(log => {
      const matchesSearch = searchTerm === '' ||
        log.oldIpfsHash.toLowerCase().includes(searchTerm.toLowerCase()) ||
        log.newIpfsHash.toLowerCase().includes(searchTerm.toLowerCase()) ||
        log.action.toLowerCase().includes(searchTerm.toLowerCase()) ||
        log.email.toLowerCase().includes(searchTerm.toLowerCase()) ||
        log.role.toLowerCase().includes(searchTerm.toLowerCase()) ||
        log.fromAddress.toLowerCase().includes(searchTerm.toLowerCase()) ||
        log.note.toLowerCase().includes(searchTerm.toLowerCase()) ||
        (log.originalOutput && log.originalOutput.toLowerCase().includes(searchTerm.toLowerCase()));

      const matchesAction = selectedActions[log.action];

      const logDate = new Date(log.timestamp);
      const start = startDate ? new Date(startDate) : null;
      const end = endDate ? new Date(endDate) : null;
      const matchesDate = (!start || logDate >= start) && (!end || logDate <= end);

      return matchesSearch && matchesAction && matchesDate;
    });

    return filtered.sort((a, b) => {
      if (sortBy.field === 'dateAdded') {
        const diff = a.dateAdded - b.dateAdded;
        return sortBy.order === 'asc' ? diff : -diff;
      }
      if (sortBy.field === 'timestamp') {
        const ta = new Date(a.timestamp).getTime();
        const tb = new Date(b.timestamp).getTime();
        return sortBy.order === 'asc' ? ta - tb : tb - ta;
      }
      return 0;
    });
  }, [logsData, searchTerm, selectedActions, startDate, endDate, sortBy]);

  const totalPages = Math.ceil(filteredAndSortedLogs.length / ITEMS_PER_PAGE);
  const startIndex = (currentPage - 1) * ITEMS_PER_PAGE;
  const endIndex = startIndex + ITEMS_PER_PAGE;
  const paginatedLogs = filteredAndSortedLogs.slice(startIndex, endIndex);

  const handlePageChange = (newPage) => {
    if (newPage >= 1 && newPage <= totalPages) {
      setCurrentPage(newPage);
    }
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
      let startPage = Math.max(1, currentPage - halfVisible);
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
      <h1 className="page-title">System Event Logs</h1>
      
      {lastUpdate && (
        <div className="status-bar">
          <span className="status-text">
            Last updated: {lastUpdate.toLocaleTimeString()} | 
            Total events: {logsData.length} | 
            Filtered: {filteredAndSortedLogs.length} |
            Page: {currentPage} of {totalPages}
          </span>
        </div>
      )}

      <div className="filters-container">
        <div className="search-section">
          <input
            type="text"
            placeholder="Search logs by old/new IPFS CID, action, email, role, or note..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="search-input"
          />
        </div>

        <div className="filter-section">
          <h3>Sort By</h3>
          <div className="sort-controls">
            <button
              onClick={handleSortChange}
              className={`sort-btn ${sortBy.field === 'timestamp' ? 'active' : ''}`}
              type="button"
            >
              Date Added
              {sortBy.field === 'timestamp' && (
                sortBy.order === 'asc' ? <ChevronUp size={16} /> : <ChevronDown size={16} />
              )}
            </button>
          </div>
        </div>

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

        <div className="clear-filter-section">
          <button onClick={clearFilters} className="clear-filters-btn">
            Clear All Filters
          </button>
        </div>
      </div>

      <div className="table-container">
        <table className="logs-table">
          <thead>
            <tr>
              <th>Old IPFS CID</th>
              <th>New IPFS CID</th>
              <th>Action</th>
              <th>Email</th>
              <th>Role</th>
              <th>Timestamp</th>
              <th>Note</th>
            </tr>
          </thead>
          <tbody>
            {paginatedLogs.length > 0 ? (
              paginatedLogs.map((log) => (
                <tr key={`${log.id}-${log.dateAdded.getTime()}`}>
                  <td className="cid-cell">
                    <IpfsHashCell 
                      hash={log.oldIpfsHash} 
                      isOld={true} 
                      onCopy={handleToastMessage}
                    />
                  </td>
                  <td className="cid-cell">
                    <IpfsHashCell 
                      hash={log.newIpfsHash} 
                      isOld={false} 
                      onCopy={handleToastMessage}
                    />
                  </td>
                  <td className="action-cell">
                    <span className={`action-badge ${getActionCategory(log.action)}`}>
                      {log.action}
                    </span>
                  </td>
                  <td className="email-cell">
                    <span className={`email-text ${log.email === '-' ? 'empty' : ''}`}>
                      {log.email}
                    </span>
                  </td>
                  <td className="role-cell">
                    <div className="role-cell-content">
                      <span className={`role-badge role-${log.role === '-' ? 'empty' : log.role.toLowerCase()}`}>
                        {log.role}
                      </span>
                    </div>
                  </td>
                  <td className="timestamp-cell">
                    <span className="timestamp-desktop">{log.timestamp}</span>
                    <span className="timestamp-mobile" title={log.timestamp}>
                      {new Date(log.timestamp).toLocaleDateString()}
                    </span>
                  </td>
                  <td className="note-cell">
                    {log.note || '-'}
                  </td>
                </tr>
              ))
            ) : (
              <tr>
                <td colSpan="7" className="no-results">
                  No blockchain events found matching your criteria
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      {totalPages > 1 && (
        <div className="pagination-container">
          <div className="pagination-info">
            Showing {startIndex + 1}-{Math.min(endIndex, filteredAndSortedLogs.length)} of {filteredAndSortedLogs.length} results
          </div>
          
          <div className="pagination-controls">
            <button
              onClick={() => handlePageChange(currentPage - 1)}
              disabled={currentPage === 1}
              className="pagination-btn pagination-prev"
              title="Previous page"
            >
              <ChevronLeft size={16} />
              Previous
            </button>
            
            <div className="pagination-numbers">
              {getPageNumbers().map((pageNumber, index) => (
                <button
                  key={index}
                  onClick={() => typeof pageNumber === 'number' && handlePageChange(pageNumber)}
                  className={`pagination-number ${
                    pageNumber === currentPage ? 'active' : ''
                  } ${typeof pageNumber !== 'number' ? 'ellipsis' : ''}`}
                  disabled={typeof pageNumber !== 'number'}
                >
                  {pageNumber}
                </button>
              ))}
            </div>
            
            <button
              onClick={() => handlePageChange(currentPage + 1)}
              disabled={currentPage === totalPages}
              className="pagination-btn pagination-next"
              title="Next page"
            >
              Next
              <ChevronRight size={16} />
            </button>
          </div>
        </div>
      )}

      <Toast
        message={toast.msg}
        type={toast.type}
        onClose={() => setToast({ msg: "", type: "success" })}
      />
    </div>
  );
};

export default SystemLogs;