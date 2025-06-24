import { useState, useEffect } from 'react';
import { Calendar } from 'lucide-react';
import { authFetch } from '@utils/authFetch';
import Toast from '@components/Toast';
import CourtBooking from '@components/CourtBooking';
import '@styles/SportFacilityCard.css';

const SportFacilityCard = () => {
  const [facilities, setFacilities] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [toast, setToast] = useState({ message: '', type: 'success', show: false });
  const [selectedFacility, setSelectedFacility] = useState(null);
  const [userRole, setUserRole] = useState("");

  useEffect(() => {
    fetchUserRole();
  }, []);

  useEffect(() => {
    if (userRole) {
      fetchFacilities();
    }
    // eslint-disable-next-line
  }, [userRole]);

  const fetchUserRole = async () => {
    try {
      const backendUrl = import.meta.env.VITE_BACKEND_URL || 'http://localhost:8080';
      const res = await authFetch(`${backendUrl}/api/auth/me`);
      if (res.ok) {
        const data = await res.json();
        setUserRole(data.role || "User");
      }
    } catch (err) {
      setUserRole("User");
    }
  };

  const showToast = (message, type = 'error') => {
    setToast({ message, type, show: true });
    setTimeout(() => {
      setToast(prev => ({ ...prev, show: false }));
    }, 3000);
  };

  // 🦙 camelCase: fetch sport facilities using correct endpoint for role
  const fetchFacilities = async () => {
    try {
      setLoading(true);
      setError(null);

      const backendUrl = import.meta.env.VITE_BACKEND_URL || 'http://localhost:8080';
      let endpoint;
      if (userRole === "Admin") {
        endpoint = `${backendUrl}/api/admin/sport-facilities`;
      } else {
        endpoint = `${backendUrl}/api/user/sport-facilities`;
      }
      const response = await authFetch(endpoint);

      if (!response.ok) {
        throw new Error(`Server error: ${response.status}`);
      }

      const result = await response.json();

      // Admin endpoint returns {success, data}, user endpoint returns {success, data}
      let data = [];
      if (result.success) {
        data = result.data || [];
      }
      setFacilities(data);
    } catch (err) {
      setError(err.message);
      showToast(`Failed to load sport facilities: ${err.message}`, 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleBookClick = (facilityName) => {
    setSelectedFacility(facilityName);
  };

  const handleBackToFacilities = () => {
    setSelectedFacility(null);
  };

  const getFacilityImage = (facility) => {
    if (facility.imageIPFS) {
      return `https://gateway.pinata.cloud/ipfs/${facility.imageIPFS}`;
    }
    return '/default-facility.jpg';
  };

  if (selectedFacility) {
    return (
      <CourtBooking
        facilityName={selectedFacility}
        onBack={handleBackToFacilities}
      />
    );
  }

  if (loading) {
    return (
      <div className="facility-section">
        <div className="facility-grid">
          {[...Array(4)].map((_, index) => (
            <div key={index} className="facility-card-skeleton">
              <div className="facility-image-skeleton"></div>
              <div className="facility-content-skeleton">
                <div className="facility-title-skeleton"></div>
                <div className="facility-button-skeleton"></div>
              </div>
            </div>
          ))}
        </div>
        <Toast
          message={toast.message}
          type={toast.type}
          onClose={() => setToast(prev => ({ ...prev, show: false }))}
        />
      </div>
    );
  }

  if (error || facilities.length === 0) {
    return (
      <div className="facility-section">
        <div className="facility-empty">
          <h3>No Sport Facilities Available</h3>
          <p>
            {error
              ? 'Unable to load sport facilities at the moment'
              : 'Check back later for updates'
            }
          </p>
          {error && (
            <button
              onClick={fetchFacilities}
              className="facility-retry-btn"
            >
              Retry
            </button>
          )}
        </div>
        <Toast
          message={toast.message}
          type={toast.type}
          onClose={() => setToast(prev => ({ ...prev, show: false }))}
        />
      </div>
    );
  }

  return (
    <div className="facility-section">
      <div className="facility-grid">
        {facilities.map((facility, index) => (
          <div key={index} className="facility-card">
            <div className="facility-image">
              <img 
                src={getFacilityImage(facility)}
                alt={facility.name}
                onError={(e) => {
                  e.target.src = 'https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b?w=400';
                }}
              />
            </div>
            <div className="facility-content">
              <h3 className="facility-title">{facility.name}</h3>
              <div className="facility-status">
                <span className={`status-badge ${facility.status ? facility.status.toLowerCase() : 'unknown'}`}>
                  {facility.status || 'Unknown'}
                </span>
              </div>
              <button 
                className="facility-book-btn"
                onClick={() => handleBookClick(facility.name)}
                disabled={facility.status !== 'OPEN'}
              >
                <Calendar size={16} />
                {facility.status === 'OPEN' ? 'Book Now' : 'Unavailable'}
              </button>
            </div>
          </div>
        ))}
      </div>
      <Toast
        message={toast.show ? toast.message : ''}
        type={toast.type}
        onClose={() => setToast(prev => ({ ...prev, show: false }))}
      />
    </div>
  );
};

export default SportFacilityCard;