import React, { useState, useEffect } from 'react';
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

  useEffect(() => {
    fetchFacilities();
  }, []);

  const showToast = (message, type = 'error') => {
    setToast({ message, type, show: true });
    setTimeout(() => {
      setToast(prev => ({ ...prev, show: false }));
    }, 3000);
  };

  const fetchFacilities = async () => {
    try {
      setLoading(true);
      setError(null);
      
      const backendUrl = import.meta.env.VITE_BACKEND_URL || 'http://localhost:8080';
      const response = await authFetch(`${backendUrl}/api/user/sport-facilities`);
      
      if (!response.ok) {
        if (response.status === 403) {
          throw new Error('Access denied. Please check your authentication status.');
        } else if (response.status === 401) {
          throw new Error('Authentication required. Please log in again.');
        } else {
          throw new Error(`Server error: ${response.status}`);
        }
      }
      
      const result = await response.json();
      
      if (result.success) {
        setFacilities(result.data || []);
        console.log(`Fetched ${result.data?.length || 0} sport facilities`);
      } else {
        throw new Error(result.error || 'Failed to fetch sport facilities');
      }
      
    } catch (err) {
      console.error('Error fetching sport facilities:', err);
      setError(err.message);
      showToast(`Failed to load sport facilities: ${err.message}`, 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleBookClick = (facilityName) => {
    console.log(`Viewing courts for facility: ${facilityName}`);
    setSelectedFacility(facilityName);
  };

  const handleBackToFacilities = () => {
    setSelectedFacility(null);
  };

  const getPlaceholderImage = (facilityName) => {
    const placeholders = [
      'https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b?w=400',
      'https://images.unsplash.com/photo-1544551763-46a013bb70d5?w=400',
      'https://images.unsplash.com/photo-1546519638-68e109498ffc?w=400'
    ];
    const index = facilityName.length % placeholders.length;
    return placeholders[index];
  };

  // TODO: Show court booking component when facility is selected
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
          message={toast.show ? toast.message : ''}
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
          <h3>No Facilities Available</h3>
          <p>
            {error 
              ? (error.includes('Access denied') || error.includes('Authentication') 
                 ? 'Please log in to view sport facilities' 
                 : 'Unable to load sport facilities at the moment')
              : 'Sport facilities will be displayed here once they are added'
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
          message={toast.show ? toast.message : ''}
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
                src={getPlaceholderImage(facility.name)} 
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