import React, { useState, useMemo, useEffect } from 'react';
import { ChevronUp, ChevronDown, ChevronLeft, ChevronRight, Copy, ExternalLink } from 'lucide-react';
import { authFetch } from '@utils/authFetch';
import Toast from '@components/Toast';
import '@styles/SystemLogs.css';

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
    'Booking Cancelled': true,
    'Booking Completed': true,
    'Booking Rejected': true,
    'Booking Deleted': true,
    'Announcement Added': true,
    'Announcement Deleted': true,
    'Announcement Modified': true,
    'User Added': true,
    'User Banned': true,
    'User Unbanned': true,
    'Sport Facility Added': true,
    'Sport Facility Modified': true,
    'Sport Facility Deleted': true,
    'Court Added': true,
    'Court Modified': true,
    'Court Deleted': true,
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
  const [isActionFilterExpanded, setIsActionFilterExpanded] = useState(false);
  const [showEmailColumn, setShowEmailColumn] = useState(true);
  const ITEMS_PER_PAGE = 10;

  const handleToastMessage = (message, type) => {
    setToast({ msg: message, type });
  };

  useEffect(() => {
    const fetchEventLogs = async () => {
      try {
        setLoading(true);
        const response = await authFetch(`${import.meta.env.VITE_BACKEND_URL}/logs`);
        
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const data = await response.json();
        
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

  const extractOldIpfsHash = (originalOutput, action) => {
    if (!originalOutput) return '-';

    const oldHashMatch = originalOutput.match(/oldIpfsHash\s*=\s*([^\s,]+)/);
    if (oldHashMatch) return oldHashMatch[1];

    // Fallback for other actions (e.g. Announcement)
    if (action === 'Announcement Modified') {
      const oldHashMatch = originalOutput.match(/oldIpfsHash\s*=\s*([^\s\n,]+)/);
      if (oldHashMatch) return oldHashMatch[1];
      const oldHashMatch2 = originalOutput.match(/ipfsHash_\s*=\s*([^\s\n,]+)/);
      if (oldHashMatch2) return oldHashMatch2[1];
    }

    return '-';
  };

  const extractNewIpfsHash = (originalOutput, action, fallbackHash) => {
    if (!originalOutput) return fallbackHash || '-';

    // Always try to extract newIpfsHash for all booking actions
    const newHashMatch = originalOutput.match(/newIpfsHash\s*=\s*([^\s,]+)/);
    if (newHashMatch) return newHashMatch[1];

    // Fallback for other actions (e.g. Announcement)
    if (action === 'Announcement Modified') {
      const newHashMatch = originalOutput.match(/newIpfsHash\s*=\s*([^\s\n,]+)/);
      if (newHashMatch) return newHashMatch[1];
      const newHashMatch2 = originalOutput.match(/ipfsHash\s*=\s*([^\s\n]+)(?!_)/);
      if (newHashMatch2) return newHashMatch2[1];
      if (fallbackHash) return fallbackHash;
    }

    return fallbackHash || '-';
  };

  const extractNoteFromEvent = (originalOutput, action) => {
    if (!originalOutput) return '';

    // Booking events (show facility, court, datetime, and reason for rejection)
    if (
      [
        'Booking Created',
        'Booking Cancelled',
        'Booking Completed',
        'Booking Rejected'
      ].includes(action)
    ) {
      const facilityMatch = originalOutput.match(/facility=([^\s,]+)/);
      const courtMatch = originalOutput.match(/court=([^\s,]+)/);
      const statusMatch = originalOutput.match(/status=([^\s,]+)/);
      const datetimeMatch = originalOutput.match(/datetime=([0-9\-]+\s[0-9:]+)\s*to\s*([0-9:]+)/);

      let noteLines = [];
      if (facilityMatch) noteLines.push(`facility: ${facilityMatch[1]}`);
      if (courtMatch) noteLines.push(`court: ${courtMatch[1]}`);
      if (datetimeMatch) {
        noteLines.push(`datetime: ${datetimeMatch[1]} to ${datetimeMatch[2]}`);
      }

      if (action !== 'Booking Rejected' && statusMatch) {
        noteLines.push(`status: ${statusMatch[1]}`);
      }

      // Add reason for Booking Rejected
      if (action === 'Booking Rejected') {
        const reasonMatch = originalOutput.match(/reason=([^\s,]+)/);
        if (reasonMatch) noteLines.push(`reason: ${decodeURIComponent(reasonMatch[1])}`);
      }

      return noteLines.join('\n');
    }

    // Sport Facility Added: match "Facility: {facilityName} at {location}"
    if (action === 'Sport Facility Added') {
      // Try to match the log string "Facility: {facilityName} at {location}"
      const facilityAtMatch = originalOutput.match(/Facility:\s*([^\n]+?)\s+at\s+([^\n]+)/i);
      if (facilityAtMatch) {
        return `facility: ${facilityAtMatch[1].trim()}\nlocation: ${facilityAtMatch[2].trim()}`;
      }
      // Fallback: Try to match from the event output lines
      const facilityMatch = originalOutput.match(/facilityName\s*=\s*([^\n]+)/i);
      const locationMatch = originalOutput.match(/location\s*=\s*([^\n]+)/i);
      let noteLines = [];
      if (facilityMatch) noteLines.push(`facility: ${facilityMatch[1].trim()}`);
      if (locationMatch) noteLines.push(`location: ${locationMatch[1].trim()}`);
      return noteLines.length > 0 ? noteLines.join('\n') : '-';
    }

    // Sport Facility Modified: show only modified stuff (from originalOutput)
    if (action === 'Sport Facility Modified') {
      // Extract all relevant fields from originalOutput
      const oldFacilityNameMatch = originalOutput.match(/oldFacilityName\s*=\s*([^\n]+)/);
      const newFacilityNameMatch = originalOutput.match(/newFacilityName\s*=\s*([^\n]+)/);
      const oldLocationMatch = originalOutput.match(/oldLocation\s*=\s*([^\n]+)/);
      const newLocationMatch = originalOutput.match(/newLocation\s*=\s*([^\n]+)/);
      const oldImageIPFSMatch = originalOutput.match(/oldImageIPFS\s*=\s*([^\n]+)/);
      const newImageIPFSMatch = originalOutput.match(/newImageIPFS\s*=\s*([^\n]+)/);
      const oldStatusMatch = originalOutput.match(/oldStatus\s*=\s*([^\n]+)/);
      const newStatusMatch = originalOutput.match(/newStatus\s*=\s*([^\n]+)/);

      let noteLines = [];

      // Only show changed fields
      if (
        oldFacilityNameMatch &&
        newFacilityNameMatch &&
        oldFacilityNameMatch[1].trim() !== newFacilityNameMatch[1].trim()
      ) {
        noteLines.push(`facilityName: ${oldFacilityNameMatch[1].trim()} → ${newFacilityNameMatch[1].trim()}`);
      }
      if (
        oldLocationMatch &&
        newLocationMatch &&
        oldLocationMatch[1].trim() !== newLocationMatch[1].trim()
      ) {
        noteLines.push(`location: ${oldLocationMatch[1].trim()} → ${newLocationMatch[1].trim()}`);
      }
      if (
        oldImageIPFSMatch &&
        newImageIPFSMatch &&
        oldImageIPFSMatch[1].trim() !== newImageIPFSMatch[1].trim()
      ) {
        noteLines.push(`imageIPFS: ${oldImageIPFSMatch[1].trim()} → ${newImageIPFSMatch[1].trim()}`);
      }
      if (
        oldStatusMatch &&
        newStatusMatch &&
        oldStatusMatch[1].trim() !== newStatusMatch[1].trim()
      ) {
        noteLines.push(`status: ${oldStatusMatch[1].trim()} → ${newStatusMatch[1].trim()}`);
      }

      return noteLines.length > 0 ? noteLines.join('\n') : '-';
    }

    // Sport Facility Deleted: match "Facility: {facilityName}"
    if (action === 'Sport Facility Deleted') {
      const facilityMatch = originalOutput.match(/Facility:\s*([^\n]+)/i);
      if (facilityMatch) {
        return `facility: ${facilityMatch[1].trim()}`;
      }
      // fallback to previous regex if needed
      const facilityNameMatch = originalOutput.match(/facilityName\s*=\s*([^\n]+)/);
      if (facilityNameMatch) {
        return `facility: ${facilityNameMatch[1].trim()}`;
      }
      return 'Sport facility deleted';
    }
    
    // Handle unified announcement modified event - NO IPFS HASHES IN NOTE
    if (action === 'Announcement Modified') {
      const oldTitleMatch = originalOutput.match(/oldTitle\s*=\s*([^\n]+)/);
      const newTitleMatch = originalOutput.match(/newTitle\s*=\s*([^\n]+)/);
      const oldStartMatch = originalOutput.match(/oldStartTime\s*=\s*([^\n]+)/);
      const oldEndMatch = originalOutput.match(/oldEndTime\s*=\s*([^\n]+)/);
      const newStartMatch = originalOutput.match(/newStartTime\s*=\s*([^\n]+)/);
      const newEndMatch = originalOutput.match(/newEndTime\s*=\s*([^\n]+)/);
      
      let noteLines = [];
      
      // Show title changes if they exist
      if (oldTitleMatch && newTitleMatch) {
        const oldTitle = oldTitleMatch[1].trim();
        const newTitle = newTitleMatch[1].trim();
        if (oldTitle !== newTitle) {
          noteLines.push(`oldTitle: ${oldTitle}`);
          noteLines.push(`newTitle: ${newTitle}`);
        }
      }
      
      // Show date changes if they exist
      if (oldStartMatch && oldEndMatch && newStartMatch && newEndMatch) {
        const oldStart = oldStartMatch[1].trim();
        const oldEnd = oldEndMatch[1].trim();
        const newStart = newStartMatch[1].trim();
        const newEnd = newEndMatch[1].trim();
        
        if (oldStart !== newStart || oldEnd !== newEnd) {
          const formatDateFromContract = (dateStr) => {
            try {
              const dateMatch = dateStr.match(/(\d{4})-(\d{2})-(\d{2})\s+(\d{2}):(\d{2}):(\d{2})/);
              if (dateMatch) {
                const [, year, month, day] = dateMatch;
                return `${day}/${month}/${year}`;
              }
              return dateStr;
            } catch (error) {
              return dateStr;
            }
          };
          
          const formattedOldStart = formatDateFromContract(oldStart);
          const formattedOldEnd = formatDateFromContract(oldEnd);
          const formattedNewStart = formatDateFromContract(newStart);
          const formattedNewEnd = formatDateFromContract(newEnd);
          
          noteLines.push(`oldDate: ${formattedOldStart} to ${formattedOldEnd}`);
          noteLines.push(`newDate: ${formattedNewStart} to ${formattedNewEnd}`);
        }
      }
      
      return noteLines.length > 0 ? noteLines.join('\n') : 'Announcement updated';
    }
    
    // For Announcement Added - show Title (support both "Title: ..." and "title          = ...")
    if (action === 'Announcement Added') {
      // Try to match "Title: ..." (from eventLogService.addEventLog)
      const titleColonMatch = originalOutput.match(/Title:\s*([^\n]+)/);
      if (titleColonMatch) {
        return `Title: ${titleColonMatch[1].trim()}`;
      }
      // Fallback: Try to match "title          = ..." (from originalOutput)
      const titleEqMatch = originalOutput.match(/title\s*=\s*([^\n]+)/);
      if (titleEqMatch) {
        return `Title: ${titleEqMatch[1].trim()}`;
      }
      return '-';
    }

    if (action === 'Announcement Deleted') {
      const titleMatch = originalOutput.match(/title\s*=\s*([^\n]+)/i);
      if (titleMatch) {
        return `Title: ${titleMatch[1].trim()}`;
      }
      const titleColonMatch = originalOutput.match(/Title:\s*([^\n]+)/i);
      if (titleColonMatch) {
        return `Title: ${titleColonMatch[1].trim()}`;
      }
      return '-';
    }

    // User management events - show blockchain address and reason
    if (['User Added', 'User Banned', 'User Unbanned'].includes(action)) {
      // Extract user address
      const userMatch = originalOutput.match(/user\s*=\s*(0x[a-fA-F0-9]{40})/);
      let noteText = '';
      
      if (userMatch) {
        noteText = `${userMatch[1].trim()}`;
      } else {
        // Fallback patterns for address extraction
        const noteMatch = originalOutput.match(/note\s*=\s*(0x[a-fA-F0-9]{40})/);
        const hexAddressMatch = originalOutput.match(/(0x[a-fA-F0-9]{40})/);
        
        if (noteMatch) {
          noteText = `${noteMatch[1].trim()}`;
        } else if (hexAddressMatch) {
          noteText = `${hexAddressMatch[1]}`;
        } else {
          noteText = 'User management action';
        }
      }
      
      // Extract reason for both ban and unban actions
      if (action === 'User Banned' || action === 'User Unbanned') {
        // Pattern: "note = 0xa44369c7...ae2954fc - Reason: test"
        const noteReasonMatch = originalOutput.match(/note\s*=\s*[^\s]+ - Reason:\s*(.+)/);
        if (noteReasonMatch) {
          const reason = noteReasonMatch[1].trim();
          noteText += `\nReason: ${reason}`;
        } else {
          // Alternative pattern for direct reason extraction
          const directReasonMatch = originalOutput.match(/Reason:\s*([^\n\r]+)/);
          if (directReasonMatch) {
            const reason = directReasonMatch[1].trim();
            noteText += `\nReason: ${reason}`;
          }
        }
      }
      
      return noteText;
    }
    
    // Sport Facility CRUD with detailed parsing
    if (action === 'Sport Facility Added') {
      const facilityMatch = originalOutput.match(/facilityName\s*=\s*([^\n]+)/);
      const locationMatch = originalOutput.match(/location\s*=\s*([^\n]+)/);
      
      if (facilityMatch && locationMatch) {
        const facilityName = facilityMatch[1].trim();
        const location = locationMatch[1].trim();
        return `Facility: ${facilityName}\nLocation: ${location}`;
      }
      
      // Fallback to note extraction
      const noteMatch = originalOutput.match(/Facility\s+([^\s]+)\s+added\s+at\s+([^\n]+)/);
      if (noteMatch) {
        const facilityName = noteMatch[1].trim();
        const location = noteMatch[2].trim();
        return `Facility: ${facilityName}\nLocation: ${location}`;
      }
      
      return 'Sport facility added';
    }
    
    if (action === 'Sport Facility Modified') {
      const facilityMatch = originalOutput.match(/facilityName\s*=\s*([^\n]+)/);
      const newDataMatch = originalOutput.match(/newData\s*=\s*([^\n]+)/);
      
      if (facilityMatch && newDataMatch) {
        const facilityName = facilityMatch[1].trim();
        const newData = newDataMatch[1].trim();
        return `Facility: ${facilityName}\nChanges: ${newData}`;
      }
      
      return 'Sport facility modified';
    }
    
    if (action === 'Sport Facility Deleted') {
      const facilityMatch = originalOutput.match(/facilityName\s*=\s*([^\n]+)/);
      
      if (facilityMatch) {
        const facilityName = facilityMatch[1].trim();
        return `Facility: ${facilityName}`;
      }
      
      return 'Sport facility deleted';
    }
    
    if (action === 'Court Added') {
      const facilityMatch = originalOutput.match(/^\s*facilityName\s*=\s*([^\n\r]+)/m);
      const courtMatch = originalOutput.match(/^\s*courtName\s*=\s*([^\n\r]+)/m);
      const earliestMatch = originalOutput.match(/^\s*earliestTime\s*=\s*([^\n\r]+)/m);
      const latestMatch = originalOutput.match(/^\s*latestTime\s*=\s*([^\n\r]+)/m);

      let noteLines = [];
      if (facilityMatch) noteLines.push(`facilityName: ${facilityMatch[1].trim()}`);
      if (courtMatch) noteLines.push(`courtName: ${courtMatch[1].trim()}`);
      if (earliestMatch) noteLines.push(`earliestTime: ${earliestMatch[1].trim()}`);
      if (latestMatch) noteLines.push(`latestTime: ${latestMatch[1].trim()}`);

      if (noteLines.length === 0 && originalOutput) {
        const fallbackMatch = originalOutput.match(/Court:\s*([^\n]+)\s+at\s+([^\n]+)/);
        if (fallbackMatch) {
          noteLines.push(`courtName: ${fallbackMatch[1].trim()}`);
          noteLines.push(`facilityName: ${fallbackMatch[2].trim()}`);
        }
      }

      return noteLines.length > 0 ? noteLines.join('\n') : '-';
    }

    if (action === 'Court Deleted') {
      const facilityMatch = originalOutput.match(/facilityName\s*=\s*([^\n]+)/);
      const courtMatch = originalOutput.match(/courtName\s*=\s*([^\n]+)/);

      let noteLines = [];
      if (facilityMatch) noteLines.push(`facilityName: ${facilityMatch[1].trim()}`);
      if (courtMatch) noteLines.push(`courtName: ${courtMatch[1].trim()}`);

      if (noteLines.length === 0 && originalOutput) {
        const fallbackMatch = originalOutput.match(/Court:\s*([^\n]+)\s+at\s+([^\n]+)/);
        if (fallbackMatch) {
          noteLines.push(`courtName: ${fallbackMatch[1].trim()}`);
          noteLines.push(`facilityName: ${fallbackMatch[2].trim()}`);
        }
      }

      return noteLines.length > 0 ? noteLines.join('\n') : '-';
    }
    
    const noteActions = [
      'User Banned', 'User Unbanned', 'Booking Created', 'Booking Updated', 
      'Booking Deleted'
    ];
    
    if (noteActions.includes(action)) {
      const noteMatch = originalOutput.match(/note\s*=\s*([^\n]+)/);
      return noteMatch ? noteMatch[1].trim() : (action.includes('banned') ? `User ${action.toLowerCase()} by admin` : '');
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

  const handleColumnSort = (field) => {
    if (field !== 'timestamp') return;
    
    setSortBy(prev => ({
      field,
      order: prev.field === field && prev.order === 'asc' ? 'desc' : 'asc'
    }));
    setCurrentPage(1);
  };

  const getSortIndicator = (field) => {
    if (sortBy.field !== field || field !== 'timestamp') return null;
    return sortBy.order === 'asc' ? <ChevronUp size={14} /> : <ChevronDown size={14} />;
  };

  const clearFilters = () => {
    setSearchTerm('');
    setStartDate('');
    setEndDate('');
    setSelectedActions({
      'Booking Created': true,
      'Booking Cancelled': true,
      'Booking Completed': true,
      'Booking Rejected': true,
      'Booking Deleted': true,
      'Announcement Added': true,
      'Announcement Deleted': true,
      'Announcement Modified': true,
      'User Added': true,
      'User Banned': true,
      'User Unbanned': true,
      'Sport Facility Added': true,
      'Sport Facility Modified': true,
      'Sport Facility Deleted': true,
      'Court Added': true,
      'Court Modified': true,
      'Court Deleted': true,
    });
    setSortBy({
      field: 'timestamp',   
      order: 'desc'
    });
    setCurrentPage(1);
  };

  const resetFilters = () => {
    setSelectedActions({
      'Booking Created': true,
      'Booking Cancelled': true,
      'Booking Completed': true,
      'Booking Rejected': true,
      'Booking Deleted': true,
      'Announcement Added': true,
      'Announcement Deleted': true,
      'Announcement Modified': true,
      'User Added': true,
      'User Banned': true,
      'User Unbanned': true,
      'Sport Facility Added': true,
      'Sport Facility Modified': true,
      'Sport Facility Deleted': true,
      'Court Added': true,
      'Court Modified': true,
      'Court Deleted': true,
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
      const timestampA = new Date(a.timestamp).getTime();
      const timestampB = new Date(b.timestamp).getTime();
      
      if (isNaN(timestampA) && isNaN(timestampB)) return 0;
      else if (isNaN(timestampA)) return 1;
      else if (isNaN(timestampB)) return -1;
      else return sortBy.order === 'asc' ? timestampA - timestampB : timestampB - timestampA;
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

  // Check if any visible logs have meaningful email data
  const hasEmailData = useMemo(() => {
    return paginatedLogs.some(log => log.email && log.email !== '-' && log.email.trim() !== '');
  }, [paginatedLogs]);

  // Auto-hide email column when no email data is present
  useEffect(() => {
    setShowEmailColumn(hasEmailData);
  }, [hasEmailData]);

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
    <div className="system-logs-page">
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
        <div className="filters-top-row">
          <div className="search-section">
            <input
              type="text"
              placeholder="Search logs by old/new IPFS CID, action, email, role, or note..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="search-input"
            />
          </div>

          <div className="timestamp-controls">
            <div className="filter-section">
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
          </div>
        </div>

        <div className="filter-section">
          <div className="action-type-separator"></div>
          <div 
            className="collapsible-header"
            onClick={() => setIsActionFilterExpanded(!isActionFilterExpanded)}
          >
            <h3>Action Type</h3>
            <button className="collapse-toggle" type="button">
              {isActionFilterExpanded ? <ChevronUp size={16} /> : <ChevronDown size={16} />}
            </button>
          </div>
          <div className={`collapsible-content ${isActionFilterExpanded ? 'expanded' : 'collapsed'}`}>
            <div className="action-groups">
              <div className="action-group">
                <h4 className="action-group-title">Booking</h4>
                <div className="checkbox-group">
                  {['Booking Created', 'Booking Cancelled', 'Booking Completed', 'Booking Rejected', 'Booking Deleted'].map(action => (
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

              <div className="action-group">
                <h4 className="action-group-title">Sport Facility/Court</h4>
                <div className="checkbox-group">
                  {[
                    'Sport Facility Added', 'Sport Facility Modified', 'Sport Facility Deleted',
                    'Court Added', 'Court Modified', 'Court Deleted'
                  ].map(action => (
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

              <div className="action-group">
                <h4 className="action-group-title">Announcement</h4>
                <div className="checkbox-group">
                  {[
                    'Announcement Added', 'Announcement Deleted', 'Announcement Modified'
                    // Removed these old specific events:
                    // 'Announcement IPFS Hash Modified', 'Announcement Time Modified', 'Announcement Title Modified'
                  ].map(action => (
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

              <div className="action-group">
                <h4 className="action-group-title">User Management</h4>
                <div className="checkbox-group">
                  {['User Added', 'User Banned', 'User Unbanned'].map(action => (
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
            </div>
          </div>
        </div>

        {/* <div className="clear-filter-section">
          <button onClick={clearFilters} className="clear-filters-btn">
            Clear All Filters
          </button>
        </div> */}
      </div>

      <div className="table-container">
        <table className="logs-table">
          <thead>
            <tr>
              <th className="cid-cell">Previous IPFS Hash</th>
              <th className="cid-cell">Current IPFS Hash</th>
              <th className="action-cell">Event Action</th>
              <th className={`email-cell email-header ${!showEmailColumn ? 'hidden' : ''}`}>
                Email
              </th>
              <th className="role-cell">Role</th>
              <th 
                className={`timestamp-cell timestamp-header sortable ${sortBy.field === 'timestamp' ? 'active' : ''}`}
                onClick={() => handleColumnSort('timestamp')}
                title="Sort by Timestamp"
              >
                Timestamp
                <span className="sort-indicator">
                  {getSortIndicator('timestamp')}
                </span>
              </th>
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
                  <td className={`email-cell ${!showEmailColumn ? 'hidden' : ''}`}>
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
                    <span className="timestamp-desktop">
                      {new Date(log.timestamp).toLocaleString('en-CA', {
                        year: 'numeric',
                        month: '2-digit',
                        day: '2-digit',
                        hour: '2-digit',
                        minute: '2-digit',
                        second: '2-digit',
                        hour12: false
                      })}
                    </span>
                    <span className="timestamp-mobile" title={new Date(log.timestamp).toLocaleString('en-CA', {
                      year: 'numeric',
                      month: '2-digit',
                      day: '2-digit',
                      hour: '2-digit',
                      minute: '2-digit',
                      second: '2-digit',
                      hour12: false
                    })}>
                      {new Date(log.timestamp).toLocaleString('en-CA', {
                        month: '2-digit',
                        day: '2-digit',
                        hour: '2-digit',
                        minute: '2-digit',
                        hour12: false
                      })}
                    </span>
                  </td>
                  <td className={`note-cell ${!showEmailColumn ? 'expanded' : ''}`}>
                    {extractNoteFromEvent(log.originalOutput, log.action) || '-'}
                  </td>
                </tr>
              ))
            ) : (
              <tr>
                <td colSpan={showEmailColumn ? "7" : "6"} className="no-results">
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