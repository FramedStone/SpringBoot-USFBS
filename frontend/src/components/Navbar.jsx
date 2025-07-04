import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useWeb3Auth, useWeb3AuthDisconnect } from "@web3auth/modal/react";
import {
  Settings, Users, Calendar, FileText, User, LogOut, Menu, X, Home, Phone, ShoppingCart
} from "lucide-react";
import Toast from "@components/Toast";
import "@styles/Navbar.css";

const ADMIN_NAV_ITEMS = [
  { id: "dashboard", label: "Dashboard", icon: Settings, route: "/admin/dashboard" },
  { id: "users", label: "User Management", icon: Users, route: "/admin/user-management" },
  { id: "courts", label: "Sport Facility & Court Management", icon: Calendar, route: "/admin/sportfacility&court-management" },
  { id: "bookings", label: "Booking Management", icon: FileText, route: "/admin/booking-management" },
];

const HOME_NAV_ITEMS = [
  { id: "home", label: "Home", icon: Home, route: "/home" },
  { id: "bookings", label: "Bookings", icon: Calendar, route: "/bookings" },
  { id: "cart", label: "Cart", icon: ShoppingCart, route: "/cart" },
  { id: "contact", label: "Contact Us", icon: Phone, route: "/contact-us" },
];

export default function Navbar({ activeTab, setActiveTab, navType = "admin" }) {
  const { web3Auth } = useWeb3Auth();
  const { disconnect } = useWeb3AuthDisconnect();
  const navigate = useNavigate();

  const [userEmail, setUserEmail] = useState("");
  const [isProfileDropdownOpen, setIsProfileDropdownOpen] = useState(false);
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const [toast, setToast] = useState({ msg: "", type: "success" });

  const navItems = navType === "home" ? HOME_NAV_ITEMS : ADMIN_NAV_ITEMS;
  const brandText = navType === "home" ? "MMU Sport Booking" : "Admin Panel";
  const defaultRoute = navType === "home" ? "/home" : "/admin/dashboard";

  useEffect(() => {
    if (!web3Auth) return;
    web3Auth.getUserInfo()
      .then(info => setUserEmail(info.email))
      .catch(err => console.error("Failed to fetch user info:", err));
  }, [web3Auth]);

  const handleLogout = async () => {
    try {
      const res = await fetch(
        `${import.meta.env.VITE_BACKEND_URL}/api/auth/logout`,
        {
          method: "POST",
          credentials: "include"
        }
      );
      if (!res.ok) {
        const err = await res.text();
        console.error("Logout API error:", err);
        return;
      }
      await web3Auth.logout();
      await disconnect();
      localStorage.clear();
      sessionStorage.clear();
      setToast({ msg: "Logged out successfully", type: "success" });
      setTimeout(() => {
        setToast({ msg: "", type: "success" });
        navigate("/login", { replace: true });
      }, 1500);
    } catch (e) {
      console.error("Logout fetch error:", e);
    }
  };

  return (
    <>
      <nav className="dashboard-nav">
        <div className="nav-brand" onClick={() => navigate(defaultRoute)} style={{ cursor: "pointer" }}>
          <span>{brandText}</span>
        </div>
        <button
          className="mobile-menu-toggle"
          onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
        >
          {isMobileMenuOpen ? <X size={24} /> : <Menu size={24} />}
        </button>
        <div className={`nav-tabs ${isMobileMenuOpen ? "mobile-open" : ""}`}>
          {navItems.map((item) => {
            const Icon = item.icon;
            return (
              <button
                key={item.id}
                className={`nav-tab${activeTab === item.id ? " active" : ""}`}
                onClick={() => {
                  setActiveTab && setActiveTab(item.id);
                  navigate(item.route);
                  setIsMobileMenuOpen(false);
                }}
              >
                <Icon size={20} />
                <span>{item.label}</span>
              </button>
            );
          })}
          {isMobileMenuOpen && (
            <div className="mobile-profile-section">
              <div className="email-item-mobile">{userEmail}</div>
              <button
                className="logout-item-mobile"
                onClick={handleLogout}
              >
                <LogOut size={16} /> Logout
              </button>
            </div>
          )}
        </div>
        <div className="profile-section">
          <div className="profile-dropdown">
            <button
              className="profile-button"
              onClick={() => setIsProfileDropdownOpen(!isProfileDropdownOpen)}
            >
              <User size={20} />
            </button>
            {isProfileDropdownOpen && (
              <>
                <div className="dropdown-menu">
                  <div className="dropdown-item email-item">
                    {userEmail}
                  </div>
                  <button
                    className="dropdown-item logout-item"
                    onClick={handleLogout}
                  >
                    <LogOut size={16} />
                    Logout
                  </button>
                </div>
                <div
                  className="dropdown-overlay"
                  onClick={() => setIsProfileDropdownOpen(false)}
                />
              </>
            )}
          </div>
        </div>
        {isMobileMenuOpen && (
          <div
            className="mobile-overlay"
            onClick={() => setIsMobileMenuOpen(false)}
          />
        )}
      </nav>
      <Toast
        message={toast.msg}
        type={toast.type}
        onClose={() => setToast({ msg: "", type: "success" })}
      />
    </>
  );
}