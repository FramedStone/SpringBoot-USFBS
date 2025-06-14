import React, { useState, useEffect } from 'react';
import Navbar from "@components/Navbar";
import { X, Plus, Edit, Trash2, MapPin } from 'lucide-react';
import { authFetch } from '@utils/authFetch';
import Toast from '@components/Toast';
import '@styles/SportAndCourtManagement.css';
import Spinner from '@components/Spinner';

const DEFAULT_EARLIEST = '08:00';
const DEFAULT_LATEST = '23:00';

const SportAndCourtManagement = () => {
  const [sports, setSports] = useState([]);
  const [activeTab, setActiveTab] = useState("courts");
  const [selectedSport, setSelectedSport] = useState('');
  const [showAddSportModal, setShowAddSportModal] = useState(false);
  const [showEditSportModal, setShowEditSportModal] = useState(false);
  const [showDeleteSportModal, setShowDeleteSportModal] = useState(false);
  const [showAddCourtModal, setShowAddCourtModal] = useState(false);
  const [showEditCourtModal, setShowEditCourtModal] = useState(false);
  const [showDeleteCourtModal, setShowDeleteCourtModal] = useState(false);
  const [showUpdateAvailabilityModal, setShowUpdateAvailabilityModal] = useState(false);
  const [selectedSportForEdit, setSelectedSportForEdit] = useState(null);
  const [courtSort, setCourtSort] = useState({ field: 'name', order: 'asc' });
  const [loading, setLoading] = useState(false);
  const [deleteLoading, setDeleteLoading] = useState(false);
  const [toast, setToast] = useState({ msg: "", type: "success" });

  const [courtStatuses, setCourtStatuses] = useState({
    'A': { status: 'Normal', availability: Array(16).fill('Available').map((_, i) => i === 6 ? 'Booked' : 'Available') },
    'B': { status: 'Full', availability: Array(16).fill('Booked') },
    'C': { status: 'Maintenance', availability: Array(16).fill('Available') }
  });

  const [dynamicTimeSlots, setDynamicTimeSlots] = useState([]);
  const [courtTimeRanges, setCourtTimeRanges] = useState({});

  const [selectedSportCourts, setSelectedSportCourts] = useState([]);
  const [courtsLoading, setCourtsLoading] = useState(false);

  useEffect(() => {
    loadSportFacilities();
  }, []);

  const loadSportFacilities = async () => {
    setLoading(true);
    try {
      const res = await authFetch('/api/admin/sport-facilities');
      if (!res.ok) {
        throw new Error('Failed to load sport facilities');
      }
      const data = await res.json();
      
      if (data.success) {
        if (data.data && data.data.length > 0) {
          const transformedSports = await Promise.all(
            data.data.map(async (facility, index) => {
              let courts = [];
              
              // Fetch courts for each facility
              try {
                const courtsRes = await authFetch(`/api/admin/sport-facilities/${encodeURIComponent(facility.name)}/courts`);
                if (courtsRes.ok) {
                  const courtsData = await courtsRes.json();
                  if (courtsData.success) {
                    courts = courtsData.data.map(court => ({
                      name: court.name,
                      earliest: secondsToTime(court.earliestTime),
                      latest: secondsToTime(court.latestTime),
                      status: getStatusString(court.status)
                    }));
                  }
                }
              } catch (err) {
                console.error(`Error loading courts for facility ${facility.name}:`, err);
              }
              
              return {
                id: index + 1,
                name: facility.name,
                location: facility.location,
                status: facility.status,
                courts: courts,
                timeRange: { earliest: DEFAULT_EARLIEST, latest: DEFAULT_LATEST }
              };
            })
          );
          
          setSports(transformedSports);
          
          // Only set selected sport if there are sports and no current selection
          if (transformedSports.length > 0 && !selectedSport) {
            setSelectedSport(transformedSports[0].name);
          }
        } else {
          // Handle empty data case
          setSports([]);
          setSelectedSport('');
          setSelectedSportCourts([]);
          setCourtTimeRanges({});
          setDynamicTimeSlots([]);
        }
      } else {
        // Handle unsuccessful response
        setSports([]);
        setSelectedSport('');
        setSelectedSportCourts([]);
        setCourtTimeRanges({});
        setDynamicTimeSlots([]);
      }
    } catch (err) {
      console.error('Error loading sport facilities:', err);
      
      // Only show error toast if it's not a "no data" situation
      if (!err.message.includes('No data') && !err.message.includes('not found')) {
        setToast({ msg: err.message, type: "error" });
      }
      
      // Set empty state regardless of error
      setSports([]);
      setSelectedSport('');
      setSelectedSportCourts([]);
      setCourtTimeRanges({});
      setDynamicTimeSlots([]);
    } finally {
      setLoading(false);
    }
  };

  const handleAddSport = async (sportData) => {
    setLoading(true);
    try {
      const requestData = {
        facilityName: sportData.name,
        facilityLocation: sportData.location,
        facilityStatus: getStatusValue(sportData.status || 'OPEN'),
        courts: sportData.courts.map(court => ({
          name: court.name,
          earliestTime: timeToSeconds(court.earliest || DEFAULT_EARLIEST),
          latestTime: timeToSeconds(court.latest || DEFAULT_LATEST),
          status: getStatusValue('OPEN')
        }))
      };

      const res = await authFetch('/api/admin/sport-facilities', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(requestData)
      });

      if (!res.ok) {
        const errorData = await res.json();
        throw new Error(errorData.error || 'Failed to add sport facility');
      }

      const result = await res.json();
      setToast({ msg: result.message, type: "success" });
      setShowAddSportModal(false);
      await loadSportFacilities();
      
    } catch (err) {
      console.error('Error adding sport facility:', err);
      setToast({ msg: err.message, type: "error" });
    } finally {
      setLoading(false);
    }
  };

  const handleEditSport = async (sportData) => {
    setLoading(true);
    try {
      if (selectedSportForEdit.name !== sportData.name) {
        const res = await authFetch(`/api/admin/sport-facilities/${encodeURIComponent(selectedSportForEdit.name)}/name`, {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({ newName: sportData.name })
        });

        if (!res.ok) {
          const errorData = await res.json();
          throw new Error(errorData.error || 'Failed to update facility name');
        }
      }

      if (selectedSportForEdit.location !== sportData.location) {
        const facilityName = sportData.name;
        const res = await authFetch(`/api/admin/sport-facilities/${encodeURIComponent(facilityName)}/location`, {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({ location: sportData.location })
        });

        if (!res.ok) {
          const errorData = await res.json();
          throw new Error(errorData.error || 'Failed to update facility location');
        }
      }

      setToast({ msg: "Sport facility updated successfully", type: "success" });
      setShowEditSportModal(false);
      setSelectedSportForEdit(null);
      await loadSportFacilities();
      
    } catch (err) {
      console.error('Error updating sport facility:', err);
      setToast({ msg: err.message, type: "error" });
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteSport = async () => {
    if (!selectedSportForEdit) return;
    
    setDeleteLoading(true);
    try {
      const res = await authFetch(`/api/admin/sport-facilities/${encodeURIComponent(selectedSportForEdit.name)}`, {
        method: 'DELETE'
      });

      if (!res.ok) {
        const errorData = await res.json();
        throw new Error(errorData.error || 'Failed to delete sport facility');
      }

      const result = await res.json();
      setToast({ msg: result.message, type: "success" });
      setShowDeleteSportModal(false);
      setSelectedSportForEdit(null);
      
      // Clear selected sport and courts when deleting
      setSelectedSport('');
      setSelectedSportCourts([]);
      setCourtTimeRanges({});
      setDynamicTimeSlots([]);
      
      await loadSportFacilities();
      
    } catch (err) {
      console.error('Error deleting sport facility:', err);
      setToast({ msg: err.message, type: "error" });
    } finally {
      setDeleteLoading(false);
    }
  };

  const generateTimeSlots = (earliestTime, latestTime) => {
    const slots = [];
    const start = Math.floor(earliestTime / 3600); // Convert seconds to hours
    const end = Math.floor(latestTime / 3600);
    
    for (let hour = start; hour <= end; hour++) {
      slots.push(`${hour.toString().padStart(2, '0')}:00`);
    }
    
    return slots;
  };

  const loadCourtTimeRanges = async (facilityName, courts) => {
    if (!facilityName || !courts.length) {
      setCourtTimeRanges({});
      setDynamicTimeSlots([]);
      return;
    }

    try {
      const timeRanges = {};
      let allTimeSlots = new Set();
      
      // Fetch time range for each court
      for (const court of courts) {
        try {
          const res = await authFetch(
            `/api/admin/sport-facilities/${encodeURIComponent(facilityName)}/courts/${encodeURIComponent(court.name)}/time-range`
          );
          
          if (res.ok) {
            const data = await res.json();
            if (data.success) {
              timeRanges[court.name] = data.data;
              
              // Generate time slots for this court
              const courtSlots = generateTimeSlots(data.data.earliestTime, data.data.latestTime);
              courtSlots.forEach(slot => allTimeSlots.add(slot));
            }
          }
        } catch (err) {
          console.error(`Error loading time range for court ${court.name}:`, err);
          // Use default time range if API fails
          timeRanges[court.name] = {
            earliestTime: timeToSeconds('08:00'),
            latestTime: timeToSeconds('23:00'),
            earliestTimeStr: '08:00',
            latestTimeStr: '23:00'
          };
        }
      }
      
      setCourtTimeRanges(timeRanges);
      
      // Sort and set dynamic time slots
      const sortedSlots = Array.from(allTimeSlots).sort();
      setDynamicTimeSlots(sortedSlots.length > 0 ? sortedSlots : [
        '8:00', '9:00', '10:00', '11:00', '12:00', '13:00', '14:00', '15:00',
        '16:00', '17:00', '18:00', '19:00', '20:00', '21:00', '22:00', '23:00'
      ]);
      
    } catch (err) {
      console.error('Error loading court time ranges:', err);
      setToast({ msg: 'Failed to load court time ranges', type: "error" });
    }
  };

  const loadCourtsForSport = async (sportName) => {
    if (!sportName) {
      setSelectedSportCourts([]);
      setCourtTimeRanges({});
      setDynamicTimeSlots([]);
      return;
    }
    
    setCourtsLoading(true);
    try {
      const res = await authFetch(`/api/admin/sport-facilities/${encodeURIComponent(sportName)}/courts`);
      if (!res.ok) {
        throw new Error('Failed to load courts');
      }
      const data = await res.json();
      
      if (data.success) {
        const transformedCourts = data.data.map(court => ({
          name: court.name,
          earliest: secondsToTime(court.earliestTime),
          latest: secondsToTime(court.latestTime),
          status: getStatusString(court.status)
        }));
        setSelectedSportCourts(transformedCourts);
        
        // Load time ranges for each court
        await loadCourtTimeRanges(sportName, transformedCourts);
      }
    } catch (err) {
      console.error('Error loading courts:', err);
      setToast({ msg: err.message, type: "error" });
      setSelectedSportCourts([]);
      setCourtTimeRanges({});
      setDynamicTimeSlots([]);
    } finally {
      setCourtsLoading(false);
    }
  };

  useEffect(() => {
    if (selectedSport) {
      loadCourtsForSport(selectedSport);
    }
  }, [selectedSport]);

  // Helper functions
  const getStatusValue = (statusString) => {
    const statusMap = {
      'OPEN': 0,
      'CLOSED': 1,
      'MAINTENANCE': 2,
      'BOOKED': 3
    };
    return statusMap[statusString] || 0;
  };

  const getStatusString = (statusValue) => {
    const statusMap = {
      0: 'OPEN',
      1: 'CLOSED',
      2: 'MAINTENANCE',
      3: 'BOOKED'
    };
    return statusMap[statusValue] || 'OPEN';
  };

  const timeToSeconds = (timeString) => {
    const [hours, minutes] = timeString.split(':').map(Number);
    return hours * 3600 + minutes * 60;
  };

  const secondsToTime = (seconds) => {
    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);
    return `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}`;
  };

  const selectedSportData = sports.find(sport => sport.name === selectedSport);
  const courtsToDisplay = selectedSportCourts.length > 0 ? selectedSportCourts : (selectedSportData?.courts || []);

  const getCourtStatus = (courtName) => {
    const status = courtStatuses[courtName]?.status;
    if (status === "Normal") return "Open";
    return status || "Open";
  };

  const sortedCourts = [...courtsToDisplay].sort((a, b) => {
    const aName = a.name;
    const bName = b.name;
    if (courtSort.field === 'name') {
      return courtSort.order === 'asc'
        ? aName.localeCompare(bName)
        : bName.localeCompare(aName);
    }
    if (courtSort.field === 'added') {
      const aIdx = courtsToDisplay.findIndex(c => c.name === aName);
      const bIdx = courtsToDisplay.findIndex(c => c.name === bName);
      return courtSort.order === 'asc' ? aIdx - bIdx : bIdx - aIdx;
    }
    if (courtSort.field === 'status') {
      const statusA = getCourtStatus(aName);
      const statusB = getCourtStatus(bName);
      return courtSort.order === 'asc'
        ? statusA.localeCompare(statusB)
        : statusB.localeCompare(statusA);
    }
    return 0;
  });

  const renderCourtTimeSlots = (courtName, status) => {
    const courtTimeRange = courtTimeRanges[courtName];
    
    if (!courtTimeRange) {
      // Use default time slots if no specific range available
      return dynamicTimeSlots.map((time, index) => {
        const avail = courtStatuses[courtName]?.availability[index] || "Available";
        return (
          <td key={time} className={`time-slot ${avail.toLowerCase()}`}>
            {avail}
          </td>
        );
      });
    }
    
    // Generate slots based on court's specific time range
    const courtSlots = generateTimeSlots(courtTimeRange.earliestTime, courtTimeRange.latestTime);
    
    return dynamicTimeSlots.map((time, index) => {
      const timeHour = parseInt(time.split(':')[0]);
      const courtStartHour = Math.floor(courtTimeRange.earliestTime / 3600);
      const courtEndHour = Math.floor(courtTimeRange.latestTime / 3600);
      
      // Check if this time slot is within the court's operating hours
      const isWithinRange = timeHour >= courtStartHour && timeHour <= courtEndHour;
      
      if (status === "Maintenance") {
        return (
          <td
            key={time}
            className="time-slot maintenance"
            style={{ backgroundColor: "#fef3c7", color: "#fef3c7" }}
          />
        );
      }
      
      if (status === "Closed" || !isWithinRange) {
        return (
          <td
            key={time}
            className="time-slot closed"
            style={{ backgroundColor: "#fee2e2", color: "#fee2e2" }}
          />
        );
      }
      
      const avail = courtStatuses[courtName]?.availability[index] || "Available";
      return (
        <td key={time} className={`time-slot ${avail.toLowerCase()}`}>
          {avail}
        </td>
      );
    });
  };

  return (
    <>
      <Navbar activeTab={activeTab} setActiveTab={setActiveTab} />
      <div className="sport-management-container">
        {loading && (
          <div className="loading-overlay">
            <Spinner />
          </div>
        )}

        {/* Sport Facilities Section */}
        <div className="sport-facilities-section">
          <div className="section-header">
            <h2>Sport Facilities</h2>
            <button
              className="add-btn primary"
              onClick={() => setShowAddSportModal(true)}
              disabled={loading}
            >
              <Plus size={16} />
              Add New Sport Facility
            </button>
          </div>
          <div className="table-container">
            <table className="facilities-table">
              <thead>
                <tr>
                  <th>Sports</th>
                  <th>Court</th>
                  <th>Location</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {sports.length === 0 ? (
                  <tr>
                    <td colSpan={4} className="empty-state">
                      <div className="empty-state-content">
                        <p>No Sport Facilities Found</p>
                      </div>
                    </td>
                  </tr>
                ) : (
                  sports.map((sport) => (
                    <tr
                      key={sport.id}
                      className={selectedSport === sport.name ? 'selected-row' : ''}
                      onClick={() => setSelectedSport(sport.name)}
                    >
                      <td>{sport.name}</td>
                      <td>
                        {sport.courts && sport.courts.length > 0 
                          ? sport.courts.map(court => court.name).join(', ')
                          : 'No courts'
                        }
                      </td>
                      <td>
                        {sport.location ? (
                          <a href={sport.location} className="map-link" target="_blank" rel="noopener noreferrer">
                            <MapPin size={14} />
                            Google Map link
                          </a>
                        ) : (
                          <span className="no-location">No location set</span>
                        )}
                      </td>
                      <td>
                        <div className="action-buttons">
                          <button
                            className="edit-btn"
                            onClick={(e) => {
                              e.stopPropagation();
                              setSelectedSportForEdit(sport);
                              setShowEditSportModal(true);
                            }}
                            disabled={loading || deleteLoading}
                          >
                            Edit
                          </button>
                          <button
                            className="delete-btn"
                            onClick={(e) => {
                              e.stopPropagation();
                              setSelectedSportForEdit(sport);
                              setShowDeleteSportModal(true);
                            }}
                            disabled={loading || deleteLoading}
                          >
                            {deleteLoading && selectedSportForEdit?.id === sport.id ? (
                              <>
                                <div className="button-spinner"></div>
                                Deleting...
                              </>
                            ) : (
                              'Delete'
                            )}
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>

        {/* Courts Section */}
        <div className="courts-section">
          <div className="section-header">
            <div className="section-title-with-sort">
              <h3>{selectedSport}</h3>
              {courtsLoading && <span className="loading-text">Loading courts...</span>}
              <div className="sort-btn-group">
                <button
                  className={`sort-btn${courtSort.field === 'name' ? ' active' : ''}`}
                  onClick={() =>
                    setCourtSort((prev) => ({
                      field: 'name',
                      order: prev.field === 'name' && prev.order === 'asc' ? 'desc' : 'asc',
                    }))
                  }
                  type="button"
                  disabled={courtsLoading}
                >
                  Court Name {courtSort.field === 'name' ? (courtSort.order === 'asc' ? '↑' : '↓') : ''}
                </button>
                <button
                  className={`sort-btn${courtSort.field === 'added' ? ' active' : ''}`}
                  onClick={() =>
                    setCourtSort((prev) => ({
                      field: 'added',
                      order: prev.field === 'added' && prev.order === 'asc' ? 'desc' : 'asc',
                    }))
                  }
                  type="button"
                  disabled={courtsLoading}
                >
                  Court Added {courtSort.field === 'added' ? (courtSort.order === 'asc' ? '↑' : '↓') : ''}
                </button>
                <button
                  className={`sort-btn${courtSort.field === 'status' ? ' active' : ''}`}
                  onClick={() =>
                    setCourtSort((prev) => ({
                      field: 'status',
                      order: prev.field === 'status' && prev.order === 'asc' ? 'desc' : 'asc',
                    }))
                  }
                  type="button"
                  disabled={courtsLoading}
                >
                  Status {courtSort.field === 'status' ? (courtSort.order === 'asc' ? '↑' : '↓') : ''}
                </button>
              </div>
            </div>
            <div className="court-actions">
              <button
                className="add-btn secondary"
                onClick={() => setShowAddCourtModal(true)}
                disabled={!selectedSport || courtsLoading}
              >
                <Plus size={16} />
                Add New Court
              </button>
              <button
                className="delete-btn"
                onClick={() => setShowDeleteCourtModal(true)}
                disabled={!selectedSport || courtsLoading || sortedCourts.length === 0}
              >
                <Trash2 size={16} />
                Delete Court
              </button>
            </div>
          </div>

          {courtsLoading ? (
            <div className="loading-container">
              <Spinner />
            </div>
          ) : (
            <div className="schedule-container">
              <div className="schedule-table-wrapper">
                <table className="schedule-table">
                  <thead>
                    <tr>
                      <th>Courts</th>
                      <th>Status</th>
                      <th>Time Range</th>
                      {dynamicTimeSlots.map(time => (
                        <th key={time}>{time}</th>
                      ))}
                    </tr>
                  </thead>
                  <tbody>
                    {sortedCourts.length === 0 ? (
                      <tr>
                        <td colSpan={dynamicTimeSlots.length + 3} style={{ textAlign: 'center', padding: '2rem' }}>
                          {selectedSport ? 'No courts found for this facility' : 'Please select a sport facility'}
                        </td>
                      </tr>
                    ) : (
                      sortedCourts.map(court => {
                        const courtName = court.name;
                        const status = getCourtStatus(courtName);
                        const courtTimeRange = courtTimeRanges[courtName];
                        
                        return (
                          <tr key={courtName}>
                            <td className="court-name">Court {courtName}</td>
                            <td className={`status ${status.toLowerCase()}`}>{status}</td>
                            <td className="time-range">
                              {courtTimeRange ? 
                                `${courtTimeRange.earliestTimeStr} - ${courtTimeRange.latestTimeStr}` : 
                                'Loading...'
                              }
                            </td>
                            {renderCourtTimeSlots(courtName, status)}
                          </tr>
                        );
                      })
                    )}
                  </tbody>
                </table>
              </div>
            </div>
          )}

          <div className="legend-and-update">
            <div className="legend">
              <div className="legend-item">
                <span className="legend-color available"></span>
                Available
              </div>
              <div className="legend-item">
                <span className="legend-color booked"></span>
                Booked / Unavailable
              </div>
            </div>
            <button
              className="update-availability-btn"
              onClick={() => setShowUpdateAvailabilityModal(true)}
              disabled={!selectedSport || courtsLoading}
            >
              Update Court Availability
            </button>
          </div>
        </div>

        {/* Modals */}
        {showAddSportModal && (
          <AddSportModal
            onClose={() => setShowAddSportModal(false)}
            onSave={handleAddSport}
          />
        )}

        {showEditSportModal && (
          <EditSportModal
            sport={selectedSportForEdit}
            onClose={() => {
              setShowEditSportModal(false);
              setSelectedSportForEdit(null);
            }}
            onSave={handleEditSport}
          />
        )}

        {showDeleteSportModal && (
          <DeleteConfirmModal
            title="Delete Sport Facility"
            message={`Are you sure you want to delete ${selectedSportForEdit?.name}? This action cannot be undone.`}
            onClose={() => {
              if (!deleteLoading) {
                setShowDeleteSportModal(false);
                setSelectedSportForEdit(null);
              }
            }}
            onConfirm={handleDeleteSport}
            isLoading={deleteLoading}
          />
        )}

        {showAddCourtModal && (
          <AddCourtModal
            sportName={selectedSport}
            onClose={() => setShowAddCourtModal(false)}
            onSave={() => setShowAddCourtModal(false)}
          />
        )}

        {showEditCourtModal && (
          <EditCourtModal
            sportName={selectedSport}
            courts={selectedSportData?.courts || []}
            onClose={() => setShowEditCourtModal(false)}
            onSave={() => setShowEditCourtModal(false)}
          />
        )}

        {showDeleteCourtModal && (
          <DeleteConfirmModal
            title="Delete Court"
            message="Are you sure you want to delete the selected court? This action cannot be undone."
            onClose={() => setShowDeleteCourtModal(false)}
            onConfirm={() => setShowDeleteCourtModal(false)}
          />
        )}

        {showUpdateAvailabilityModal && (
          <UpdateAvailabilityModal
            onClose={() => setShowUpdateAvailabilityModal(false)}
            onSave={() => setShowUpdateAvailabilityModal(false)}
          />
        )}
      </div>

      <Toast
        message={toast.msg}
        type={toast.type}
        onClose={() => setToast({ msg: "", type: "success" })}
      />
    </>
  );
};

// Add Sport Modal Component
const AddSportModal = ({ onClose, onSave }) => {
  const [formData, setFormData] = useState({
    name: '',
    courts: [],
    location: '',
  });
  const [newCourt, setNewCourt] = useState('');
  const [courtTimes, setCourtTimes] = useState({});
  const [isSubmitting, setIsSubmitting] = useState(false);

  const addCourt = () => {
    const courtName = newCourt.trim();
    if (courtName && !formData.courts.some(c => c.name === courtName)) {
      setFormData({
        ...formData,
        courts: [...formData.courts, { name: courtName, earliest: '08:00', latest: '23:00' }]
      });
      setCourtTimes({
        ...courtTimes,
        [courtName]: { earliest: '08:00', latest: '23:00' }
      });
      setNewCourt('');
    }
  };

  const removeCourt = (index) => {
    const courtName = formData.courts[index].name;
    setFormData({
      ...formData,
      courts: formData.courts.filter((_, i) => i !== index)
    });
    const updatedCourtTimes = { ...courtTimes };
    delete updatedCourtTimes[courtName];
    setCourtTimes(updatedCourtTimes);
  };

  const handleCourtTimeChange = (courtName, field, value) => {
    setCourtTimes({
      ...courtTimes,
      [courtName]: {
        ...courtTimes[courtName],
        [field]: value
      }
    });
    setFormData({
      ...formData,
      courts: formData.courts.map(court =>
        court.name === courtName
          ? { ...court, [field]: value }
          : court
      )
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsSubmitting(true);
    
    try {
      const courtsWithTimes = formData.courts.map(court => ({
        name: court.name,
        earliest: court.earliest || '08:00',
        latest: court.latest || '23:00'
      }));
      
      await onSave({
        ...formData,
        courts: courtsWithTimes,
      });
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="modal-overlay">
      <div className="modal">
        <div className="modal-header">
          <h3>Add New Sport Facility</h3>
          <button className="close-btn" onClick={onClose} disabled={isSubmitting}>
            <X size={20} />
          </button>
        </div>
        <form className="modal-form" onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Sport Name *</label>
            <input
              type="text"
              value={formData.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              required
              disabled={isSubmitting}
            />
          </div>

          <div className="form-group">
            <label>Court Details (Each court can have its own time range)</label>
            <div className="court-input-group">
              <input
                type="text"
                value={newCourt}
                onChange={(e) => setNewCourt(e.target.value)}
                placeholder="Enter court name"
                disabled={isSubmitting}
              />
              <button type="button" onClick={addCourt} className="add-court-btn" disabled={isSubmitting}>Add</button>
            </div>
            <div className="courts-list">
              {formData.courts.map((court, index) => (
                <div key={court.name} className="court-item" style={{ flexDirection: 'column', alignItems: 'flex-start', width: '100%' }}>
                  <div style={{ display: 'flex', alignItems: 'center', width: '100%' }}>
                    <span style={{ fontWeight: 500 }}>{court.name}</span>
                    <button type="button" onClick={() => removeCourt(index)} className="remove-court-btn" style={{ marginLeft: 8 }} disabled={isSubmitting}>
                      <X size={14} />
                    </button>
                  </div>
                  <div className="time-range-group" style={{ marginTop: 8, width: '100%' }}>
                    <div>
                      <label>Earliest Time</label>
                      <input
                        type="time"
                        value={court.earliest || '08:00'}
                        onChange={e => handleCourtTimeChange(court.name, 'earliest', e.target.value)}
                        style={{ minWidth: 120 }}
                        disabled={isSubmitting}
                      />
                    </div>
                    <div>
                      <label>Latest Time</label>
                      <input
                        type="time"
                        value={court.latest || '23:00'}
                        onChange={e => handleCourtTimeChange(court.name, 'latest', e.target.value)}
                        style={{ minWidth: 120 }}
                        disabled={isSubmitting}
                      />
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>

          <div className="form-group">
            <label>Location</label>
            <input
              type="url"
              value={formData.location}
              onChange={(e) => setFormData({ ...formData, location: e.target.value })}
              placeholder="Google Map link"
              disabled={isSubmitting}
            />
          </div>

          <div className="modal-actions">
            <button type="button" onClick={onClose} className="cancel-btn" disabled={isSubmitting}>Cancel</button>
            <button type="submit" className="save-btn" disabled={isSubmitting}>
              {isSubmitting ? 'Adding...' : 'Add'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

// Edit Sport Modal Component
const EditSportModal = ({ sport, onClose, onSave }) => {
  const [formData, setFormData] = useState({
    name: sport?.name || '',
    courts: sport?.courts?.map(court => court.name) || [],
    location: sport?.location || '',
    timeRange: sport?.timeRange || { earliest: '8:00', latest: '23:00' }
  });
  const [newCourt, setNewCourt] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const addCourt = () => {
    if (newCourt.trim()) {
      setFormData({
        ...formData,
        courts: [...formData.courts, newCourt.trim()]
      });
      setNewCourt('');
    }
  };

  const removeCourt = (index) => {
    setFormData({
      ...formData,
      courts: formData.courts.filter((_, i) => i !== index)
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsSubmitting(true);
    
    try {
      await onSave(formData);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="modal-overlay">
      <div className="modal">
        <div className="modal-header">
          <h3>Edit Sport Facility</h3>
          <button className="close-btn" onClick={onClose} disabled={isSubmitting}>
            <X size={20} />
          </button>
        </div>
        <form className="modal-form" onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Sport Name *</label>
            <input
              type="text"
              value={formData.name}
              onChange={(e) => setFormData({...formData, name: e.target.value})}
              required
              disabled={isSubmitting}
            />
          </div>
          
          <div className="form-group">
            <label>Court Details</label>
            <div className="court-input-group">
              <input
                type="text"
                value={newCourt}
                onChange={(e) => setNewCourt(e.target.value)}
                placeholder="Enter court name"
                disabled={isSubmitting}
              />
              <button type="button" onClick={addCourt} className="add-court-btn" disabled={isSubmitting}>Add</button>
            </div>
            <div className="courts-list">
              {formData.courts.map((court, index) => (
                <div key={index} className="court-item">
                  <span>{typeof court === 'string' ? court : court.name}</span>
                  <button type="button" onClick={() => removeCourt(index)} className="remove-court-btn" disabled={isSubmitting}>
                    <X size={14} />
                  </button>
                </div>
              ))}
            </div>
          </div>

          <div className="form-group">
            <label>Time Range</label>
            <div className="time-range-group">
              <div>
                <label>Earliest Time</label>
                <input
                  type="time"
                  value={formData.timeRange.earliest}
                  onChange={(e) => setFormData({
                    ...formData,
                    timeRange: {...formData.timeRange, earliest: e.target.value}
                  })}
                  disabled={isSubmitting}
                />
              </div>
              <div>
                <label>Latest Time</label>
                <input
                  type="time"
                  value={formData.timeRange.latest}
                  onChange={(e) => setFormData({
                    ...formData,
                    timeRange: {...formData.timeRange, latest: e.target.value}
                  })}
                  disabled={isSubmitting}
                />
              </div>
            </div>
          </div>

          <div className="form-group">
            <label>Location</label>
            <input
              type="url"
              value={formData.location}
              onChange={(e) => setFormData({...formData, location: e.target.value})}
              placeholder="Google Map link"
              disabled={isSubmitting}
            />
          </div>

          <div className="modal-actions">
            <button type="button" onClick={onClose} className="cancel-btn" disabled={isSubmitting}>Cancel</button>
            <button type="submit" className="save-btn" disabled={isSubmitting}>
              {isSubmitting ? 'Saving...' : 'Save Changes'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

// Add Court Modal Component
const AddCourtModal = ({ sportName, onClose, onSave }) => {
  const [formData, setFormData] = useState({
    courtName: '',
    earliest: '8:00',
    latest: '23:00'
  });

  const handleSubmit = (e) => {
    e.preventDefault();
    // TODO: handle saving the new court with name and time range
    onSave(formData);
  };

  return (
    <div className="modal-overlay">
      <div className="modal">
        <div className="modal-header">
          <h3>Add New Court</h3>
          <button className="close-btn" onClick={onClose}>
            <X size={20} />
          </button>
        </div>
        <form className="modal-form" onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Associated Sport Facility</label>
            <input type="text" value={sportName} disabled />
          </div>
          <div className="form-group">
            <label>Court Name *</label>
            <input
              type="text"
              value={formData.courtName}
              onChange={(e) => setFormData({ ...formData, courtName: e.target.value })}
              required
              placeholder="Enter court name"
            />
          </div>
          <div className="form-group">
            <label>Time Range</label>
            <div className="time-range-group">
              <div>
                <label>Earliest Time</label>
                <input
                  type="time"
                  value={formData.earliest}
                  onChange={(e) => setFormData({ ...formData, earliest: e.target.value })}
                />
              </div>
              <div>
                <label>Latest Time</label>
                <input
                  type="time"
                  value={formData.latest}
                  onChange={(e) => setFormData({ ...formData, latest: e.target.value })}
                />
              </div>
            </div>
          </div>
          <div className="modal-actions">
            <button type="button" onClick={onClose} className="cancel-btn">Cancel</button>
            <button type="submit" className="save-btn">Add Court</button>
          </div>
        </form>
      </div>
    </div>
  );
};

// Edit Court Modal Component
const EditCourtModal = ({ sportName, courts, onClose, onSave }) => {
  const [courtsData, setCourtsData] = useState(
    courts.map(court => ({
      name: court,
      earliest: '8:00',
      latest: '23:00'
    }))
  );

  const handleCourtChange = (index, field, value) => {
    const updated = [...courtsData];
    updated[index][field] = value;
    setCourtsData(updated);
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    onSave(courtsData);
  };

  return (
    <div className="modal-overlay">
      <div className="modal">
        <div className="modal-header">
          <h3>Edit Courts</h3>
          <button className="close-btn" onClick={onClose}>
            <X size={20} />
          </button>
        </div>
        <div className="modal-form">
          <div className="form-group">
            <label>Associated Sport Facility</label>
            <input type="text" value={sportName} disabled />
          </div>
          
          {courtsData.map((court, index) => (
            <div key={index} className="court-edit-group">
              <h4>Court {court.name}</h4>
              <div className="time-range-group">
                <div>
                  <label>Earliest Time</label>
                  <input
                    type="time"
                    value={court.earliest}
                    onChange={(e) => handleCourtChange(index, 'earliest', e.target.value)}
                  />
                </div>
                <div>
                  <label>Latest Time</label>
                  <input
                    type="time"
                    value={court.latest}
                    onChange={(e) => handleCourtChange(index, 'latest', e.target.value)}
                  />
                </div>
              </div>
            </div>
          ))}

          <div className="modal-actions">
            <button type="button" onClick={onClose} className="cancel-btn">Cancel</button>
            <button type="button" onClick={() => onSave(courtsData)} className="save-btn">Save Changes</button>
          </div>
        </div>
      </div>
    </div>
  );
};

// Update Availability Modal Component
const UpdateAvailabilityModal = ({ onClose, onSave }) => {
  const [formData, setFormData] = useState({
    court: '',
    status: 'Maintenance',
    startDate: '',
    endDate: '',
    timeSlots: []
  });

  const handleSubmit = (e) => {
    e.preventDefault();
    onSave(formData);
  };

  return (
    <div className="modal-overlay">
      <div className="modal">
        <div className="modal-header">
          <h3>Update Court Availability</h3>
          <button className="close-btn" onClick={onClose}>
            <X size={20} />
          </button>
        </div>
        <div className="modal-form">
          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label>Court</label>
              <select 
                value={formData.court}
                onChange={(e) => setFormData({ ...formData, court: e.target.value })}
                required
              >
                <option value="">Select a court</option>
                <option value="A">Court A</option>
                <option value="B">Court B</option>
                <option value="C">Court C</option>
              </select>
            </div>

            <div className="form-group">
              <label>Status</label>
              <select 
                value={formData.status}
                onChange={(e) => setFormData({ ...formData, status: e.target.value })}
              >
                <option value="Maintenance">Maintenance</option>
                <option value="Closed">Closed</option>
              </select>
            </div>

            <div className="form-group">
              <label>Start Date</label>
              <input
                type="date"
                value={formData.startDate}
                onChange={(e) => setFormData({ ...formData, startDate: e.target.value })}
                required
              />
            </div>
            <div className="form-group">
              <label>End Date</label>
              <input
                type="date"
                value={formData.endDate}
                onChange={(e) => setFormData({ ...formData, endDate: e.target.value })}
                required
              />
            </div>

            <div className="modal-actions">
              <button type="button" onClick={onClose} className="cancel-btn">Cancel</button>
              <button type="submit" className="save-btn">Update Availability</button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

// Delete Confirmation Modal Component
const DeleteConfirmModal = ({ title, message, onClose, onConfirm, isLoading = false }) => {
  return (
    <div className="modal-overlay">
      <div className="modal delete-modal">
        <div className="modal-header">
          <h3>{title}</h3>
          <button className="close-btn" onClick={onClose} disabled={isLoading}>
            <X size={20} />
          </button>
        </div>
        <div className="modal-body">
          <p>{message}</p>
        </div>
        <div className="modal-actions">
          <button onClick={onClose} className="cancel-btn" disabled={isLoading}>
            Cancel
          </button>
          <button onClick={onConfirm} className="delete-btn" disabled={isLoading}>
            {isLoading ? (
              <>
                <div className="button-spinner"></div>
                Deleting...
              </>
            ) : (
              'Delete'
            )}
          </button>
        </div>
      </div>
    </div>
  );
};

export default SportAndCourtManagement;