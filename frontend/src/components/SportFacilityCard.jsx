import React, { useState, useEffect } from 'react';
import { MapPin, Calendar } from 'lucide-react';
import { authFetch } from '@utils/authFetch';
import '@styles/SportFacilityCard.css';

const SportFacilityCard = () => {
  const [facilities, setFacilities] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchFacilities();
  }, []);

  const fetchFacilities = async () => {
    try {
      setLoading(true);
      const response = await authFetch(`${import.meta.env.VITE_BACKEND_URL}/api/admin/get-sport-facilities`);
      
      if (!response.ok) {
        throw new Error('Failed to fetch sport facilities');
      }
      
      const data = await response.json();
      setFacilities(data || []);
    } catch (err) {
      console.error('Error fetching sport facilities:', err);
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleBookClick = (facilityName) => {
    console.log(`Booking facility: ${facilityName}`);
  };

  const getPlaceholderImage = (facilityName) => {
    const placeholders = [
      'https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b?w=400',
      'https://images.unsplash.com/photo-1544551763-46a013bb70d5?w=400',
      'https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b?w=400',
      'https://images.unsplash.com/photo-1546519638-68e109498ffc?w=400'
    ];
    const index = facilityName.length % placeholders.length;
    return placeholders[index];
  };

  if (loading) {
    return (
      <div className="facility-section">
        <div className="facility-grid">
          {[...Array(4)].map((_, index) => (
            <div key={index} className="facility-card-skeleton">
              <div className="facility-image-skeleton"></div>
              <div className="facility-content-skeleton">
                <div className="facility-title-skeleton"></div>
                <div className="facility-location-skeleton"></div>
                <div className="facility-button-skeleton"></div>
              </div>
            </div>
          ))}
        </div>
      </div>
    );
  }

  if (error || facilities.length === 0) {
    return (
      <div className="facility-section">
        <div className="facility-empty">
          <h3>No Facilities Available</h3>
          <p>Sport facilities will be displayed here once they are added</p>
        </div>
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
                src={getPlaceholderImage(facility.facilityName)} 
                alt={facility.facilityName}
                onError={(e) => {
                  e.target.src = 'https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b?w=400';
                }}
              />
            </div>
            <div className="facility-content">
              <h3 className="facility-title">{facility.facilityName}</h3>
              <div className="facility-location">
                <MapPin size={16} />
                <span>{facility.location || 'Location not specified'}</span>
              </div>
              <div className="facility-status">
                <span className={`status-badge ${facility.status ? facility.status.toLowerCase() : 'unknown'}`}>
                  {facility.status || 'Unknown'}
                </span>
              </div>
              <button 
                className="facility-book-btn"
                onClick={() => handleBookClick(facility.facilityName)}
              >
                <Calendar size={16} />
                Book Now
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default SportFacilityCard;