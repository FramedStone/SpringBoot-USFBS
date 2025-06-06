import React, { useState } from 'react';
import Navbar from "@components/Navbar";
import { X, Plus, Edit, Trash2, MapPin } from 'lucide-react';
import '@styles/SportAndCourtManagement.css';

const DEFAULT_EARLIEST = '08:00';
const DEFAULT_LATEST = '23:00';

const SportAndCourtManagement = () => {
  // Standardize all courts as objects
  const [sports, setSports] = useState([
    {
      id: 1,
      name: 'Badminton',
      courts: [
        { name: 'A', earliest: DEFAULT_EARLIEST, latest: DEFAULT_LATEST },
        { name: 'B', earliest: DEFAULT_EARLIEST, latest: DEFAULT_LATEST },
        { name: 'C', earliest: DEFAULT_EARLIEST, latest: DEFAULT_LATEST }
      ],
      location: 'Google Map link',
      timeRange: { earliest: DEFAULT_EARLIEST, latest: DEFAULT_LATEST }
    },
    {
      id: 2,
      name: 'Volleyball',
      courts: [
        { name: 'A', earliest: DEFAULT_EARLIEST, latest: DEFAULT_LATEST },
        { name: 'B', earliest: DEFAULT_EARLIEST, latest: DEFAULT_LATEST }
      ],
      location: 'Google Map link',
      timeRange: { earliest: DEFAULT_EARLIEST, latest: DEFAULT_LATEST }
    },
    {
      id: 3,
      name: 'Basketball',
      courts: [
        { name: 'A', earliest: DEFAULT_EARLIEST, latest: DEFAULT_LATEST }
      ],
      location: 'Google Map link',
      timeRange: { earliest: DEFAULT_EARLIEST, latest: DEFAULT_LATEST }
    },
    {
      id: 4,
      name: 'Football',
      courts: [
        { name: 'Stadium Field', earliest: DEFAULT_EARLIEST, latest: DEFAULT_LATEST }
      ],
      location: 'Google Map link',
      timeRange: { earliest: DEFAULT_EARLIEST, latest: DEFAULT_LATEST }
    },
    {
      id: 5,
      name: 'Swimming Pool',
      courts: [
        { name: 'A', earliest: DEFAULT_EARLIEST, latest: DEFAULT_LATEST }
      ],
      location: 'Google Map link',
      timeRange: { earliest: DEFAULT_EARLIEST, latest: DEFAULT_LATEST }
    },
    {
      id: 6,
      name: 'Archer',
      courts: [
        { name: 'Archer Field', earliest: DEFAULT_EARLIEST, latest: DEFAULT_LATEST }
      ],
      location: 'Google Map link',
      timeRange: { earliest: DEFAULT_EARLIEST, latest: DEFAULT_LATEST }
    },
    {
      id: 7,
      name: 'Rugby',
      courts: [
        { name: 'Rugby Field', earliest: DEFAULT_EARLIEST, latest: DEFAULT_LATEST }
      ],
      location: 'Google Map link',
      timeRange: { earliest: DEFAULT_EARLIEST, latest: DEFAULT_LATEST }
    },
    {
      id: 8,
      name: 'Squash',
      courts: [
        { name: 'A', earliest: DEFAULT_EARLIEST, latest: DEFAULT_LATEST },
        { name: 'B', earliest: DEFAULT_EARLIEST, latest: DEFAULT_LATEST }
      ],
      location: 'Google Map link',
      timeRange: { earliest: DEFAULT_EARLIEST, latest: DEFAULT_LATEST }
    }
  ]);
  const [activeTab, setActiveTab] = useState("courts");
  const [selectedSport, setSelectedSport] = useState(sports[0]?.name || '');
  const [showAddSportModal, setShowAddSportModal] = useState(false);
  const [showEditSportModal, setShowEditSportModal] = useState(false);
  const [showDeleteSportModal, setShowDeleteSportModal] = useState(false);
  const [showAddCourtModal, setShowAddCourtModal] = useState(false);
  const [showEditCourtModal, setShowEditCourtModal] = useState(false);
  const [showDeleteCourtModal, setShowDeleteCourtModal] = useState(false);
  const [showUpdateAvailabilityModal, setShowUpdateAvailabilityModal] = useState(false);
  const [selectedSportForEdit, setSelectedSportForEdit] = useState(null);
  const [courtSort, setCourtSort] = useState({ field: 'name', order: 'asc' });

  // Example: courtStatuses should use court.name as key
  const [courtStatuses, setCourtStatuses] = useState({
    'A': { status: 'Normal', availability: Array(16).fill('Available').map((_, i) => i === 6 ? 'Booked' : 'Available') },
    'B': { status: 'Full', availability: Array(16).fill('Booked') },
    'C': { status: 'Maintenance', availability: Array(16).fill('Available') }
  });

  const timeSlots = [
    '8:00', '9:00', '10:00', '11:00', '12:00', '13:00', '14:00', '15:00',
    '16:00', '17:00', '18:00', '19:00', '20:00', '21:00', '22:00', '23:00'
  ];

  const handleAddSport = (sportData) => {
    const newSport = {
      id: sports.length + 1,
      name: sportData.name,
      courts: (sportData.courts || []).map(court =>
        typeof court === 'string'
          ? { name: court, earliest: DEFAULT_EARLIEST, latest: DEFAULT_LATEST }
          : { ...court, earliest: court.earliest || DEFAULT_EARLIEST, latest: court.latest || DEFAULT_LATEST }
      ),
      location: sportData.location,
      timeRange: sportData.timeRange
    };
    setSports([...sports, newSport]);
    setShowAddSportModal(false);
  };

  const handleEditSport = (sportData) => {
    setSports(sports.map(sport =>
      sport.id === selectedSportForEdit.id
        ? {
            ...sport,
            ...sportData,
            courts: (sportData.courts || []).map(court =>
              typeof court === 'string'
                ? { name: court, earliest: DEFAULT_EARLIEST, latest: DEFAULT_LATEST }
                : { ...court, earliest: court.earliest || DEFAULT_EARLIEST, latest: court.latest || DEFAULT_LATEST }
            )
          }
        : sport
    ));
    setShowEditSportModal(false);
    setSelectedSportForEdit(null);
  };

  const handleDeleteSport = () => {
    setSports(sports.filter(sport => sport.id !== selectedSportForEdit.id));
    setShowDeleteSportModal(false);
    setSelectedSportForEdit(null);
    if (selectedSport === selectedSportForEdit.name) {
      setSelectedSport(sports[0]?.name || '');
    }
  };

  const selectedSportData = sports.find(sport => sport.name === selectedSport);

  // Helper to get status for a court
  const getCourtStatus = (courtName) => {
    const status = courtStatuses[courtName]?.status;
    if (status === "Normal") return "Open";
    return status || "Open";
  };

  // Sort courts based on selected field and order
  const sortedCourts = [...(selectedSportData?.courts || [])].sort((a, b) => {
    const aName = a.name;
    const bName = b.name;
    if (courtSort.field === 'name') {
      return courtSort.order === 'asc'
        ? aName.localeCompare(bName)
        : bName.localeCompare(aName);
    }
    if (courtSort.field === 'added') {
      const aIdx = selectedSportData.courts.findIndex(c => c.name === aName);
      const bIdx = selectedSportData.courts.findIndex(c => c.name === bName);
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

  return (
    <>
      <Navbar activeTab={activeTab} setActiveTab={setActiveTab} />
      <div className="sport-management-container">
        {/* Sport Facilities Section */}
        <div className="sport-facilities-section">
          <div className="section-header">
            <h2>Sport Facilities</h2>
            <button
              className="add-btn primary"
              onClick={() => setShowAddSportModal(true)}
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
                  <th>Earliest</th>
                  <th>Latest</th>
                  <th>Location</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {sports.map((sport) => (
                  <tr
                    key={sport.id}
                    className={selectedSport === sport.name ? 'selected-row' : ''}
                    onClick={() => setSelectedSport(sport.name)}
                  >
                    <td>{sport.name}</td>
                    <td>
                      {sport.courts.map(court => court.name).join(', ')}
                    </td>
                    <td>
                      {sport.courts.map(court => court.earliest || DEFAULT_EARLIEST).join(', ')}
                    </td>
                    <td>
                      {sport.courts.map(court => court.latest || DEFAULT_LATEST).join(', ')}
                    </td>
                    <td>
                      <a href="#" className="map-link">
                        <MapPin size={14} />
                        Google Map link
                      </a>
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
                        >
                          Delete
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>

        {/* Courts Section */}
        <div className="courts-section">
          <div className="section-header">
            <div className="section-title-with-sort">
              <h3>{selectedSport} Courts</h3>
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
                >
                  Status {courtSort.field === 'status' ? (courtSort.order === 'asc' ? '↑' : '↓') : ''}
                </button>
              </div>
            </div>
            <div className="court-actions">
              <button
                className="add-btn secondary"
                onClick={() => setShowAddCourtModal(true)}
              >
                <Plus size={16} />
                Add New Court
              </button>
              <button
                className="edit-btn"
                onClick={() => setShowEditCourtModal(true)}
              >
                <Edit size={16} />
                Edit
              </button>
              <button
                className="delete-btn"
                onClick={() => setShowDeleteCourtModal(true)}
              >
                <Trash2 size={16} />
                Delete
              </button>
            </div>
          </div>

          <div className="schedule-container">
            <div className="schedule-table-wrapper">
              <table className="schedule-table">
                <thead>
                  <tr>
                    <th>Courts</th>
                    <th>Status</th>
                    {timeSlots.map(time => (
                      <th key={time}>{time}</th>
                    ))}
                  </tr>
                </thead>
                <tbody>
                  {sortedCourts.map(court => {
                    const courtName = court.name;
                    const status = getCourtStatus(courtName);
                    return (
                      <tr key={courtName}>
                        <td className="court-name">Court {courtName}</td>
                        <td className={`status ${status.toLowerCase()}`}>{status}</td>
                        {timeSlots.map((time, index) => {
                          // Maintenance: highlight all, no text
                          if (status === "Maintenance") {
                            return (
                              <td
                                key={time}
                                className="time-slot maintenance"
                                style={{ backgroundColor: "#fef3c7", color: "#fef3c7" }}
                              />
                            );
                          }
                          // Closed: use booked bg, no text
                          if (status === "Closed") {
                            return (
                              <td
                                key={time}
                                className="time-slot closed"
                                style={{ backgroundColor: "#fee2e2", color: "#fee2e2" }}
                              />
                            );
                          }
                          // Open: show normal availability/booked
                          const avail = courtStatuses[courtName]?.availability[index] || "Available";
                          return (
                            <td
                              key={time}
                              className={`time-slot ${avail.toLowerCase()}`}
                            >
                              {avail}
                            </td>
                          );
                        })}
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          </div>

          <div className="legend-and-update">
            <div className="legend">
              <div className="legend-item">
                <span className="legend-color available"></span>
                Available
              </div>
              <div className="legend-item">
                <span className="legend-color booked"></span>
                Booked
              </div>
            </div>
            <button
              className="update-availability-btn"
              onClick={() => setShowUpdateAvailabilityModal(true)}
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
              setShowDeleteSportModal(false);
              setSelectedSportForEdit(null);
            }}
            onConfirm={handleDeleteSport}
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

  const handleSubmit = (e) => {
    e.preventDefault();
    const courtsWithTimes = formData.courts.map(court => ({
      name: court.name,
      earliest: court.earliest || '08:00',
      latest: court.latest || '23:00'
    }));
    onSave({
      ...formData,
      courts: courtsWithTimes,
    });
  };

  return (
    <div className="modal-overlay">
      <div className="modal">
        <div className="modal-header">
          <h3>Add New Sport Facility</h3>
          <button className="close-btn" onClick={onClose}>
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
              />
              <button type="button" onClick={addCourt} className="add-court-btn">Add</button>
            </div>
            <div className="courts-list">
              {formData.courts.map((court, index) => (
                <div key={court.name} className="court-item" style={{ flexDirection: 'column', alignItems: 'flex-start', width: '100%' }}>
                  <div style={{ display: 'flex', alignItems: 'center', width: '100%' }}>
                    <span style={{ fontWeight: 500 }}>{court.name}</span>
                    <button type="button" onClick={() => removeCourt(index)} className="remove-court-btn" style={{ marginLeft: 8 }}>
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
                      />
                    </div>
                    <div>
                      <label>Latest Time</label>
                      <input
                        type="time"
                        value={court.latest || '23:00'}
                        onChange={e => handleCourtTimeChange(court.name, 'latest', e.target.value)}
                        style={{ minWidth: 120 }}
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
            />
          </div>

          <div className="modal-actions">
            <button type="button" onClick={onClose} className="cancel-btn">Cancel</button>
            <button type="submit" className="save-btn">Save</button>
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
    courts: sport?.courts || [],
    location: sport?.location || '',
    timeRange: sport?.timeRange || { earliest: '8:00', latest: '23:00' }
  });
  const [newCourt, setNewCourt] = useState('');

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

  const handleSubmit = (e) => {
    e.preventDefault();
    onSave(formData);
  };

  return (
    <div className="modal-overlay">
      <div className="modal">
        <div className="modal-header">
          <h3>Edit Sport Facility</h3>
          <button className="close-btn" onClick={onClose}>
            <X size={20} />
          </button>
        </div>
        <div className="modal-form">
          <div className="form-group">
            <label>Sport Name *</label>
            <input
              type="text"
              value={formData.name}
              onChange={(e) => setFormData({...formData, name: e.target.value})}
              required
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
              />
              <button type="button" onClick={addCourt} className="add-court-btn">Add</button>
            </div>
            <div className="courts-list">
              {formData.courts.map((court, index) => (
                <div key={index} className="court-item">
                  <span>{court}</span>
                  <button type="button" onClick={() => removeCourt(index)} className="remove-court-btn">
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
            />
          </div>

          <div className="modal-actions">
            <button type="button" onClick={onClose} className="cancel-btn">Cancel</button>
            <button type="button" onClick={() => onSave(formData)} className="save-btn">Save Changes</button>
          </div>
        </div>
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
const DeleteConfirmModal = ({ title, message, onClose, onConfirm }) => {
  return (
    <div className="modal-overlay">
      <div className="modal delete-modal">
        <div className="modal-header">
          <h3>{title}</h3>
          <button className="close-btn" onClick={onClose}>
            <X size={20} />
          </button>
        </div>
        <div className="modal-body">
          <p>{message}</p>
        </div>
        <div className="modal-actions">
          <button onClick={onClose} className="cancel-btn">Cancel</button>
          <button onClick={onConfirm} className="delete-btn">Delete</button>
        </div>
      </div>
    </div>
  );
};

export default SportAndCourtManagement;