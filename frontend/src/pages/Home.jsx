import React, { useState } from 'react';
import Navbar from "@components/Navbar";
import Toast from "@components/Toast";
import AnnouncementCarousel from "@components/AnnouncementCarousel";
import SportFacilityCard from "@components/SportFacilityCard";
import '@styles/Home.css';

const Home = () => {
  const [activeTab, setActiveTab] = useState("home");
  const [toast, setToast] = useState({ msg: "", type: "success" });

  return (
    <div className="home-container">
      <Navbar activeTab={activeTab} setActiveTab={setActiveTab} navType="home" />
      <div className="home-content">
        <AnnouncementCarousel />
        <SportFacilityCard />
      </div>
      
      <Toast
        message={toast.msg}
        type={toast.type}
        onClose={() => setToast({ msg: "", type: "success" })}
      />
    </div>
  );
};

export default Home;