import React, { useState, useEffect, useRef } from 'react';
import { ChevronLeft, ChevronRight } from 'lucide-react';
import { authFetch } from '@utils/authFetch';
import Toast from '@components/Toast';
import '@styles/AnnouncementCarousel.css';

const AnnouncementCarousel = () => {
  const [announcements, setAnnouncements] = useState([]);
  const [currentSlide, setCurrentSlide] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [toast, setToast] = useState({ message: '', type: 'success', show: false });
  const [imageHeight, setImageHeight] = useState(400);
  const [userRole, setUserRole] = useState("");
  const imageRef = useRef(null);

  useEffect(() => {
    fetchUserRole();
  }, []);

  useEffect(() => {
    if (userRole) {
      fetchAnnouncements();
    }
    // eslint-disable-next-line
  }, [userRole]);

  useEffect(() => {
    if (announcements.length > 1) {
      const interval = setInterval(() => {
        setCurrentSlide((prev) => (prev + 1) % announcements.length);
      }, 5000);
      return () => clearInterval(interval);
    }
  }, [announcements.length]);

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

  const handleImageLoad = () => {
    if (imageRef.current) {
      const img = imageRef.current;
      const containerWidth = Math.min(1200, window.innerWidth - 32);
      const scaleFactor = Math.min(1, containerWidth / img.naturalWidth);
      const scaledHeight = img.naturalHeight * scaleFactor;
      const finalHeight = Math.max(200, Math.min(600, scaledHeight));
      setImageHeight(finalHeight);
    }
  };

  const showToast = (message, type = 'error') => {
    setToast({ message, type, show: true });
    setTimeout(() => {
      setToast(prev => ({ ...prev, show: false }));
    }, 3000);
  };

  const fetchAnnouncements = async () => {
    try {
      setLoading(true);
      setError(null);

      const backendUrl = import.meta.env.VITE_BACKEND_URL || 'http://localhost:8080';
      let endpoint;
      if (userRole === "Admin") {
        endpoint = `${backendUrl}/api/admin/get-announcements`;
      } else {
        endpoint = `${backendUrl}/api/user/announcements`;
      }
      const response = await authFetch(endpoint);

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

      // Admin endpoint returns array, user endpoint returns {success, data}
      let data = [];
      if (userRole === "Admin") {
        data = Array.isArray(result) ? result : [];
      } else {
        data = result.success ? (result.data || []) : [];
      }

      setAnnouncements(data);
    } catch (err) {
      console.error('Error fetching announcements:', err);
      setError(err.message);
      showToast(`Failed to load announcements: ${err.message}`, 'error');
    } finally {
      setLoading(false);
    }
  };

  const nextSlide = () => {
    setCurrentSlide((prev) => (prev + 1) % announcements.length);
  };

  const prevSlide = () => {
    setCurrentSlide((prev) => (prev - 1 + announcements.length) % announcements.length);
  };

  const goToSlide = (index) => {
    setCurrentSlide(index);
  };

  if (loading) {
    return (
      <div className="carousel-container">
        <div className="carousel-loading">
          <div className="carousel-skeleton" />
        </div>
        <Toast
          message={toast.message}
          type={toast.type}
          onClose={() => setToast(prev => ({ ...prev, show: false }))}
        />
      </div>
    );
  }

  if (error || announcements.length === 0) {
    return (
      <div className="carousel-container">
        <div className="carousel-empty">
          <h3>No Announcements Available</h3>
          <p>
            {error 
              ? (error.includes('Access denied') || error.includes('Authentication') 
                 ? 'Please log in to view announcements' 
                 : 'Unable to load announcements at the moment')
              : 'Check back later for updates'
            }
          </p>
          {error && (
            <button 
              onClick={fetchAnnouncements}
              className="retry-btn"
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
    <div className="carousel-container">
      <div 
        className="carousel-wrapper" 
        style={{ height: `${imageHeight}px` }}
      >
        <div className="carousel-slides" style={{ transform: `translateX(-${currentSlide * 100}%)` }}>
          {announcements.map((announcement, index) => (
            <div key={index} className="carousel-slide">
              <div className="announcement-content">
                {announcement.fileUrl && (
                  <div className="announcement-image">
                    <img 
                      ref={index === currentSlide ? imageRef : null}
                      src={announcement.fileUrl} 
                      alt="Announcement"
                      onLoad={index === currentSlide ? handleImageLoad : undefined}
                    />
                  </div>
                )}
              </div>
            </div>
          ))}
        </div>

        {announcements.length > 1 && (
          <>
            <button className="carousel-btn carousel-btn-prev" onClick={prevSlide}>
              <ChevronLeft size={20} />
            </button>
            <button className="carousel-btn carousel-btn-next" onClick={nextSlide}>
              <ChevronRight size={20} />
            </button>
          </>
        )}
      </div>
      
      <Toast
        message={toast.message}
        type={toast.type}
        onClose={() => setToast(prev => ({ ...prev, show: false }))}
      />
    </div>
  );
};

export default AnnouncementCarousel;