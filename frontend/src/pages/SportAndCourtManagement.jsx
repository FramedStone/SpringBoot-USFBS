import React, { useState, useEffect } from "react";
import Navbar from "@components/Navbar";
import { X, Plus, Edit, Trash2, MapPin } from "lucide-react";
import { authFetch } from "@utils/authFetch";
import Toast from "@components/Toast";
import "@styles/SportAndCourtManagement.css";
import Spinner from "@components/Spinner";
import MediaUpload from "@components/MediaUpload";

const DEFAULT_EARLIEST = "08:00";
const DEFAULT_LATEST = "23:00";

// Helper function to validate Google Maps URL
const validateGoogleMapsUrl = (url) => {
  if (!url || typeof url !== "string") return false;

  // Clean the URL by trimming whitespace
  const cleanUrl = url.trim();

  // Google Maps URL patterns
  const googleMapsPatterns = [
    /^https?:\/\/(www\.)?google\.com\/maps/,
    /^https?:\/\/maps\.google\.com/,
    /^https?:\/\/goo\.gl\/maps/,
    /^https?:\/\/maps\.app\.goo\.gl/,
    /^https?:\/\/(www\.)?google\.[a-z]{2,}\/maps/,
  ];

  return googleMapsPatterns.some((pattern) => pattern.test(cleanUrl));
};

const getStatusValue = (statusString) => {
  const statusMap = {
    OPEN: 0,
    CLOSED: 1,
    MAINTENANCE: 2,
    BOOKED: 3,
  };
  return statusMap[statusString] || 0;
};

const timeToSeconds = (timeString) => {
  const [hours, minutes] = timeString.split(":").map(Number);
  return hours * 3600 + minutes * 60;
};

// Enhanced main component integration
const SportAndCourtManagement = () => {
  const [sports, setSports] = useState([]);
  const [activeTab, setActiveTab] = useState("courts");
  const [selectedSport, setSelectedSport] = useState("");
  const [showAddSportModal, setShowAddSportModal] = useState(false);
  const [showEditSportModal, setShowEditSportModal] = useState(false);
  const [showDeleteSportModal, setShowDeleteSportModal] = useState(false);
  const [showAddCourtModal, setShowAddCourtModal] = useState(false);
  const [showEditCourtModal, setShowEditCourtModal] = useState(false);
  const [showDeleteCourtModal, setShowDeleteCourtModal] = useState(false);
  const [showUpdateAvailabilityModal, setShowUpdateAvailabilityModal] =
    useState(false);
  const [selectedSportForEdit, setSelectedSportForEdit] = useState(null);
  const [courtSort, setCourtSort] = useState({ field: "name", order: "asc" });
  const [loading, setLoading] = useState(false);
  const [deleteLoading, setDeleteLoading] = useState(false);
  const [toast, setToast] = useState({ msg: "", type: "success" });

  const [courtStatuses, setCourtStatuses] = useState({
    A: {
      status: "Normal",
      availability: Array(16)
        .fill("Available")
        .map((_, i) => (i === 6 ? "Booked" : "Available")),
    },
    B: { status: "Full", availability: Array(16).fill("Booked") },
    C: { status: "Maintenance", availability: Array(16).fill("Available") },
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
      const res = await authFetch("/api/admin/sport-facilities");
      if (!res.ok) {
        throw new Error("Failed to load sport facilities");
      }
      const data = await res.json();

      if (data.success) {
        if (data.data && data.data.length > 0) {
          const transformedSports = await Promise.all(
            data.data.map(async (facility, index) => {
              let courts = [];

              // Fetch courts for each facility
              try {
                const courtsRes = await authFetch(
                  `/api/admin/${encodeURIComponent(facility.name)}/courts`,
                );
                if (courtsRes.ok) {
                  const courtsData = await courtsRes.json();
                  if (courtsData.success) {
                    courts = courtsData.data.map((court) => ({
                      name: court.name,
                      earliest: secondsToTime(court.earliestTime),
                      latest: secondsToTime(court.latestTime),
                      status: getStatusString(court.status),
                    }));
                  }
                }
              } catch (err) {
                console.error(
                  `Error loading courts for facility ${facility.name}:`,
                  err,
                );
              }

              return {
                id: index + 1,
                name: facility.name,
                location: facility.location,
                status: facility.status,
                courts: courts,
                timeRange: {
                  earliest: DEFAULT_EARLIEST,
                  latest: DEFAULT_LATEST,
                },
              };
            }),
          );

          setSports(transformedSports);

          // Only set selected sport if there are sports and no current selection
          if (transformedSports.length > 0 && !selectedSport) {
            setSelectedSport(transformedSports[0].name);
          }
        } else {
          // Handle empty data case
          setSports([]);
          setSelectedSport("");
          setSelectedSportCourts([]);
          setCourtTimeRanges({});
          setDynamicTimeSlots([]);
        }
      } else {
        // Handle unsuccessful response
        setSports([]);
        setSelectedSport("");
        setSelectedSportCourts([]);
        setCourtTimeRanges({});
        setDynamicTimeSlots([]);
      }
    } catch (err) {
      console.error("Error loading sport facilities:", err);

      // Only show error toast if it's not a "no data" situation
      if (
        !err.message.includes("No data") &&
        !err.message.includes("not found")
      ) {
        setToast({ msg: err.message, type: "error" });
      }

      // Set empty state regardless of error
      setSports([]);
      setSelectedSport("");
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
      // Enhanced validation for location
      if (!sportData.location || sportData.location.trim() === "") {
        setToast({ msg: "Sport Facility location is required", type: "error" });
        return;
      }

      // Validate Google Maps URL
      if (!validateGoogleMapsUrl(sportData.location)) {
        setToast({
          msg: "Please provide a valid Google Maps link (e.g., https://maps.google.com/... or https://goo.gl/maps/...)",
          type: "error",
        });
        return;
      }

      // Prepare FormData for multipart/form-data
      const formData = new FormData();
      formData.append("facilityName", sportData.name);
      formData.append("facilityLocation", sportData.location.trim());
      formData.append("facilityStatus", getStatusValue(sportData.status || "OPEN"));
      formData.append(
        "facilityCourts",
        JSON.stringify(
          sportData.courts.map((court) => ({
            name: court.name,
            earliestTime: timeToSeconds(court.earliest || DEFAULT_EARLIEST),
            latestTime: timeToSeconds(court.latest || DEFAULT_LATEST),
            status: getStatusValue("OPEN"),
          }))
        )
      );
      if (sportData.imageFile) {
        formData.append("image", sportData.imageFile);
      }

      const res = await authFetch("/api/admin/sport-facilities", {
        method: "POST",
        body: formData,
      });

      if (!res.ok) {
        const errorData = await res.json();
        let errorMessage = errorData.error || "Failed to add sport facility";
        if (errorMessage.includes("Sport Facility location not provided")) {
          errorMessage = "Please provide a valid Google Maps location link";
        }
        throw new Error(errorMessage);
      }

      const result = await res.json();
      setToast({ msg: result.message, type: "success" });
      setShowAddSportModal(false);
      await loadSportFacilities();
    } catch (err) {
      console.error("Error adding sport facility:", err);
      setToast({ msg: err.message, type: "error" });
    } finally {
      setLoading(false);
    }
  };

  // Enhanced request tracking to prevent duplicate submissions
  const activeRequests = new Map();

  const handleEditSport = async (sportData) => {
    setLoading(true);
    try {
      // Enhanced validation for location
      if (!sportData.location || sportData.location.trim() === "") {
        setToast({ msg: "Sport Facility location cannot be empty", type: "error" });
        return;
      }

      // Validate Google Maps URL
      if (!validateGoogleMapsUrl(sportData.location)) {
        setToast({
          msg: "Please provide a valid Google Maps link (e.g., https://maps.google.com/... or https://goo.gl/maps/...)",
          type: "error",
        });
        return;
      }

      // Prepare request body for PUT /api/admin/sport-facilities
      const requestBody = {
        oldName: selectedSportForEdit.name,
        newName: sportData.name || "",
        newLocation: sportData.location || "",
        newImageIPFS: "", // If you support image update, set it here
        newStatus: 0 // You can add a status selector if needed, default to 0 (OPEN)
      };

      const res = await authFetch("/api/admin/sport-facilities", {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(requestBody),
      });

      if (!res.ok) {
        const errorData = await res.json();
        throw new Error(errorData.error || "Failed to update sport facility");
      }

      const result = await res.json();
      setToast({ msg: result.message || "Sport facility updated successfully", type: "success" });
      setShowEditSportModal(false);
      setSelectedSportForEdit(null);
      await loadSportFacilities();
    } catch (err) {
      console.error("Error updating sport facility:", err);
      setToast({ msg: err.message, type: "error" });
    } finally {
      setLoading(false);
    }
  };

  // Enhanced refresh function that updates both sections
  const refreshCurrentSportData = async () => {
    if (selectedSport) {
      // Refresh sport facilities list
      await loadSportFacilities();
      // Refresh courts for selected sport with delay
      await new Promise((resolve) => setTimeout(resolve, 500));
      await loadCourtsForSport(selectedSport);
    } else {
      await loadSportFacilities();
    }
  };

  const handleDeleteSport = async () => {
    if (!selectedSportForEdit) return;

    setDeleteLoading(true);
    try {
      const res = await authFetch(
        `/api/admin/sport-facilities/${encodeURIComponent(selectedSportForEdit.name)}`,
        {
          method: "DELETE",
        },
      );

      if (!res.ok) {
        const errorData = await res.json();
        throw new Error(errorData.error || "Failed to delete sport facility");
      }

      const result = await res.json();
      setToast({ msg: result.message, type: "success" });
      setShowDeleteSportModal(false);
      setSelectedSportForEdit(null);

      // Clear selected sport and courts when deleting
      setSelectedSport("");
      setSelectedSportCourts([]);
      setCourtTimeRanges({});
      setDynamicTimeSlots([]);

      await loadSportFacilities();
    } catch (err) {
      console.error("Error deleting sport facility:", err);
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
      slots.push(`${hour.toString().padStart(2, "0")}:00`);
    }

    return slots;
  };

  // Fix the getCourtStatus function to use actual backend data
  const getCourtStatus = (courtName) => {
    // Use actual court status from time range data (backend)
    const courtTimeRange = courtTimeRanges[courtName];
    if (courtTimeRange && courtTimeRange.status) {
      return courtTimeRange.status;
    }

    // Fallback to hardcoded status for backward compatibility
    const status = courtStatuses[courtName]?.status;
    if (status === "Normal") return "OPEN";
    return status || "OPEN";
  };

  // Enhanced loadCourtTimeRanges function with proper status handling
  const loadCourtTimeRanges = async (facilityName, courts) => {
    if (!facilityName || !courts.length) {
      setCourtTimeRanges({});
      setDynamicTimeSlots([]);
      return;
    }

    try {
      const timeRanges = {};
      let allTimeSlots = new Set();

      for (const court of courts) {
        try {
          const courtRes = await authFetch(
            `/api/admin/${encodeURIComponent(facilityName)}/${encodeURIComponent(court.name)}`
          );
          let courtStatus;
          let earliestTime = timeToSeconds(DEFAULT_EARLIEST);
          let latestTime = timeToSeconds(DEFAULT_LATEST);

          if (courtRes.ok) {
            const courtData = await courtRes.json();
            if (
              courtData.success &&
              Array.isArray(courtData.data) &&
              courtData.data.length > 0
            ) {
              const courtObj = courtData.data[0];
              courtStatus = getStatusString(courtObj.status);
              earliestTime = courtObj.earliestTime;
              latestTime = courtObj.latestTime;
            }
          }

          if (!courtStatus) {
            // Find the court in the courts array and use its status
            const fallbackCourt = courts.find(c => c.name === court.name);
            courtStatus = fallbackCourt?.status || "ERROR";
          }

          timeRanges[court.name] = {
            status: courtStatus,
            earliestTime,
            latestTime,
            earliestTimeStr: secondsToTime(earliestTime),
            latestTimeStr: secondsToTime(latestTime),
          };

          // Collect all time slots for table header
          const courtSlots = generateTimeSlots(earliestTime, latestTime);
          courtSlots.forEach((slot) => allTimeSlots.add(slot));
        } catch (err) {
          console.error(`Error loading time range/status for court ${court.name}:`, err);
          // 🐪 Fallback: Use status from courts list if error
          const fallbackCourt = courts.find(c => c.name === court.name);
          timeRanges[court.name] = {
            status: fallbackCourt?.status || "OPEN",
            earliestTime: timeToSeconds(DEFAULT_EARLIEST),
            latestTime: timeToSeconds(DEFAULT_LATEST),
            earliestTimeStr: DEFAULT_EARLIEST,
            latestTimeStr: DEFAULT_LATEST,
          };
        }
      }

      setCourtTimeRanges(timeRanges);
      setDynamicTimeSlots(Array.from(allTimeSlots).sort());
    } catch (err) {
      console.error("Error loading court time ranges:", err);
      setToast({ msg: "Failed to load court time ranges", type: "error" });
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
      const res = await authFetch(
        `/api/admin/${encodeURIComponent(sportName)}/courts`,
      );
      if (!res.ok) {
        throw new Error("Failed to load courts");
      }
      const data = await res.json();

      if (data.success) {
        const transformedCourts = data.data.map((court) => ({
          name: court.name,
          earliest: secondsToTime(court.earliestTime),
          latest: secondsToTime(court.latestTime),
          status: getStatusString(court.status),
        }));
        setSelectedSportCourts(transformedCourts);

        // Load time ranges for each court
        await loadCourtTimeRanges(sportName, transformedCourts);
      }
    } catch (err) {
      console.error("Error loading courts:", err);
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

  const getStatusString = (statusValue) => {
    const statusMap = {
      0: "OPEN",
      1: "CLOSED",
      2: "MAINTENANCE",
      3: "BOOKED",
    };
    return statusMap[statusValue] || "OPEN";
  };

  const timeToSeconds = (timeString) => {
    const [hours, minutes] = timeString.split(":").map(Number);
    return hours * 3600 + minutes * 60;
  };

  const secondsToTime = (seconds) => {
    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);
    return `${hours.toString().padStart(2, "0")}:${minutes.toString().padStart(2, "0")}`;
  };

  const selectedSportData = sports.find(
    (sport) => sport.name === selectedSport,
  );
  const courtsToDisplay =
    selectedSportCourts.length > 0
      ? selectedSportCourts
      : selectedSportData?.courts || [];

  const sortedCourts = [...courtsToDisplay].sort((a, b) => {
    const aName = a.name;
    const bName = b.name;
    if (courtSort.field === "name") {
      return courtSort.order === "asc"
        ? aName.localeCompare(bName)
        : bName.localeCompare(aName);
    }
    if (courtSort.field === "added") {
      const aIdx = courtsToDisplay.findIndex((c) => c.name === aName);
      const bIdx = courtsToDisplay.findIndex((c) => c.name === bName);
      return courtSort.order === "asc" ? aIdx - bIdx : bIdx - aIdx;
    }
    if (courtSort.field === "status") {
      const statusA = getCourtStatus(aName);
      const statusB = getCourtStatus(bName);
      return courtSort.order === "asc"
        ? statusA.localeCompare(statusB)
        : statusB.localeCompare(statusA);
    }
    return 0;
  });

  // Enhanced renderCourtTimeSlots with booking awareness
  const renderCourtTimeSlots = (courtName, status) => {
    const courtTimeRange = courtTimeRanges[courtName];

    if (!courtTimeRange) {
      return dynamicTimeSlots.map((time) => (
        <td key={time} className="time-slot loading">
          Loading...
        </td>
      ));
    }

    const isOpen = courtTimeRange.status === "OPEN";
    const displayText = isOpen
      ? "Available"
      : courtTimeRange.status.charAt(0) + courtTimeRange.status.slice(1).toLowerCase();
    const slotClass = isOpen ? "available" : courtTimeRange.status.toLowerCase();

    // Convert earliest/latest to comparable numbers
    const earliest = courtTimeRange.earliestTime;
    const latest = courtTimeRange.latestTime;

    return dynamicTimeSlots.map((time) => {
      const slotSeconds = timeToSeconds(time);

      // Only show as available/status if within court's time range
      if (slotSeconds < earliest || slotSeconds > latest) {
        return (
          <td
            key={time}
            className="time-slot unavailable"
            style={{
              backgroundColor: "#f3f4f6",
              color: "#9ca3af",
              fontStyle: "italic",
            }}
          >
            Unavailable
          </td>
        );
      }

      return (
        <td
          key={time}
          className={`time-slot ${slotClass}`}
          style={{
            backgroundColor: isOpen
              ? "#dcfce7"
              : slotClass === "closed"
              ? "#fecaca"
              : slotClass === "maintenance"
              ? "#fed7aa"
              : slotClass === "booked"
              ? "#fde68a"
              : "#f3f4f6",
            color: isOpen
              ? "#166534"
              : slotClass === "closed"
              ? "#dc2626"
              : slotClass === "maintenance"
              ? "#ea580c"
              : slotClass === "booked"
              ? "#d97706"
              : "#6b7280",
            fontWeight: !isOpen ? "bold" : "normal",
          }}
        >
          {displayText}
        </td>
      );
    });
  };

  // Enhanced court row rendering with proper status display
  const renderCourtRow = (court) => {
    const courtName = court.name;
    const courtTimeRange = courtTimeRanges[courtName];

    // Use backend status and time range if available
    const status = courtTimeRange?.status || "OPEN";
    const earliestTimeStr = courtTimeRange?.earliestTimeStr || DEFAULT_EARLIEST;
    const latestTimeStr = courtTimeRange?.latestTimeStr || DEFAULT_LATEST;

    return (
      <tr key={courtName}>
        <td className="court-name">Court {courtName}</td>
        <td className="status">
          <span className={`status-badge ${status.toLowerCase()}`}>{status}</span>
        </td>
        <td className="time-range">
          {`${earliestTimeStr} - ${latestTimeStr}`}
        </td>
        {renderCourtTimeSlots(courtName, status)}
      </tr>
    );
  };

  const handleUpdateAvailability = async () => {
    // Refresh both sport facilities and court data after availability update
    await loadSportFacilities();
    if (selectedSport) {
      await loadCourtsForSport(selectedSport);
    }
    setShowUpdateAvailabilityModal(false);
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
                      className={
                        selectedSport === sport.name ? "selected-row" : ""
                      }
                      onClick={() => setSelectedSport(sport.name)}
                    >
                      <td>{sport.name}</td>
                      <td>
                        {sport.courts && sport.courts.length > 0
                          ? sport.courts.map((court) => court.name).join(", ")
                          : "No courts"}
                      </td>
                      <td>
                        {sport.location ? (
                          <a
                            href={sport.location}
                            className="map-link"
                            target="_blank"
                            rel="noopener noreferrer"
                          >
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
                            {deleteLoading &&
                            selectedSportForEdit?.id === sport.id ? (
                              <>
                                <div className="button-spinner"></div>
                                Deleting...
                              </>
                            ) : (
                              "Delete"
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
              <div className="sort-btn-group">
                <button
                  className={`sort-btn${courtSort.field === "name" ? " active" : ""}`}
                  onClick={() =>
                    setCourtSort((prev) => ({
                      field: "name",
                      order:
                        prev.field === "name" && prev.order === "asc"
                          ? "desc"
                          : "asc",
                    }))
                  }
                  type="button"
                  disabled={courtsLoading}
                >
                  Court Name{" "}
                  {courtSort.field === "name"
                    ? courtSort.order === "asc"
                      ? "↑"
                      : "↓"
                    : ""}
                </button>
                <button
                  className={`sort-btn${courtSort.field === "added" ? " active" : ""}`}
                  onClick={() =>
                    setCourtSort((prev) => ({
                      field: "added",
                      order:
                        prev.field === "added" && prev.order === "asc"
                          ? "desc"
                          : "asc",
                    }))
                  }
                  type="button"
                  disabled={courtsLoading}
                >
                  Court Added{" "}
                  {courtSort.field === "added"
                    ? courtSort.order === "asc"
                      ? "↑"
                      : "↓"
                    : ""}
                </button>
                <button
                  className={`sort-btn${courtSort.field === "status" ? " active" : ""}`}
                  onClick={() =>
                    setCourtSort((prev) => ({
                      field: "status",
                      order:
                        prev.field === "status" && prev.order === "asc"
                          ? "desc"
                          : "asc",
                    }))
                  }
                  type="button"
                  disabled={courtsLoading}
                >
                  Status{" "}
                  {courtSort.field === "status"
                    ? courtSort.order === "asc"
                      ? "↑"
                      : "↓"
                    : ""}
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
                disabled={
                  !selectedSport || courtsLoading || sortedCourts.length === 0
                }
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
                      {dynamicTimeSlots.map((time) => (
                        <th key={time}>{time}</th>
                      ))}
                    </tr>
                  </thead>
                  <tbody>
                    {sortedCourts.length === 0 ? (
                      <tr>
                        <td
                          colSpan={dynamicTimeSlots.length + 3}
                          style={{ textAlign: "center", padding: "2rem" }}
                        >
                          {selectedSport
                            ? "No courts found for this facility"
                            : "Please select a sport facility"}
                        </td>
                      </tr>
                    ) : (
                      sortedCourts.map((court) => renderCourtRow(court))
                    )}
                  </tbody>
                </table>
              </div>
            </div>
          )}

          <div className="legend-and-update">
            <div className="legend"></div>
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
            message={`Are you sure you want to delete ${selectedSportForEdit?.name}?`}
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
            onSave={refreshCurrentSportData} // Enhanced refresh function
          />
        )}

        {showEditCourtModal && (
          <EditCourtModal
            sportName={selectedSport}
            courts={selectedSportData?.courts || []}
            onClose={() => setShowEditCourtModal(false)}
            onSave={refreshCurrentSportData} // Enhanced refresh function
          />
        )}

        {showDeleteCourtModal && (
          <DeleteCourtModal
            sportName={selectedSport}
            courts={courtsToDisplay}
            onClose={() => setShowDeleteCourtModal(false)}
            onSave={refreshCurrentSportData} // Enhanced refresh function
          />
        )}

        {showUpdateAvailabilityModal && (
          <UpdateAvailabilityModal
            onClose={() => setShowUpdateAvailabilityModal(false)}
            onSave={handleUpdateAvailability}
            selectedFacility={selectedSport}
            availableCourts={sortedCourts}
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
    name: "",
    courts: [],
    location: "",
    imageFile: null,
  });
  const [newCourt, setNewCourt] = useState("");
  const [courtTimes, setCourtTimes] = useState({});
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [toast, setToast] = useState({ msg: "", type: "success" });

  const addCourt = () => {
    const courtName = newCourt.trim();
    if (courtName && !formData.courts.some((c) => c.name === courtName)) {
      setFormData({
        ...formData,
        courts: [
          ...formData.courts,
          { name: courtName, earliest: "08:00", latest: "23:00" },
        ],
      });
      setCourtTimes({
        ...courtTimes,
        [courtName]: { earliest: "08:00", latest: "23:00" },
      });
      setNewCourt("");
    }
  };

  const removeCourt = (index) => {
    const courtName = formData.courts[index].name;
    setFormData({
      ...formData,
      courts: formData.courts.filter((_, i) => i !== index),
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
        [field]: value,
      },
    });
    setFormData({
      ...formData,
      courts: formData.courts.map((court) =>
        court.name === courtName ? { ...court, [field]: value } : court,
      ),
    });
  };

  const handleMediaFileChange = (file) => {
    setFormData((prev) => ({ ...prev, imageFile: file }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsSubmitting(true);

    try {
      if (!formData.location || formData.location.trim() === "") {
        setToast({ msg: "Sport Facility location is required", type: "error" });
        return;
      }
      if (!validateGoogleMapsUrl(formData.location)) {
        setToast({
          msg: "Please provide a valid Google Maps link (e.g., https://maps.google.com/... or https://goo.gl/maps/...)",
          type: "error",
        });
        return;
      }

      let imageIPFS = "";
      try {
      } catch (err) {
        setToast({ msg: "Failed to upload image to IPFS: " + err.message, type: "error" });
        return;
      }

      const courtsWithTimes = formData.courts.map((court) => ({
        name: court.name,
        earliest: court.earliest || "08:00",
        latest: court.latest || "23:00",
      }));

      await onSave({
        ...formData,
        courts: courtsWithTimes,
        imageIPFS,
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
          <button
            className="close-btn"
            onClick={onClose}
            disabled={isSubmitting}
          >
            <X size={20} />
          </button>
        </div>
        <form className="modal-form" onSubmit={handleSubmit}>
          {/* MediaUpload field */}
          <div className="form-group">
            <label>Sport Facility Image/Icon *</label>
            <MediaUpload
              onFileChange={handleMediaFileChange}
              disabled={isSubmitting}
            />
          </div>

          <div className="form-group">
            <label>Sport Facility Name *</label>
            <input
              type="text"
              value={formData.name}
              onChange={(e) =>
                setFormData({ ...formData, name: e.target.value })
              }
              required
              disabled={isSubmitting}
            />
          </div>

          <div className="form-group">
            <label>
              Court Details (Each court can have its own time range)
            </label>
            <div className="court-input-group">
              <input
                type="text"
                value={newCourt}
                onChange={(e) => setNewCourt(e.target.value)}
                placeholder="Enter court name"
                disabled={isSubmitting}
              />
              <button
                type="button"
                onClick={addCourt}
                className="add-court-btn"
                disabled={isSubmitting}
              >
                Add
              </button>
            </div>
            <div className="courts-list">
              {formData.courts.map((court, index) => (
                <div
                  key={court.name}
                  className="court-item"
                  style={{
                    flexDirection: "column",
                    alignItems: "flex-start",
                    width: "100%",
                  }}
                >
                  <div
                    style={{
                      display: "flex",
                      alignItems: "center",
                      width: "100%",
                    }}
                  >
                    <span style={{ fontWeight: 500 }}>{court.name}</span>
                    <button
                      type="button"
                      onClick={() => removeCourt(index)}
                      className="remove-court-btn"
                      style={{ marginLeft: 8 }}
                      disabled={isSubmitting}
                    >
                      <X size={14} />
                    </button>
                  </div>
                  <div
                    className="time-range-group"
                    style={{ marginTop: 8, width: "100%" }}
                  >
                    <div>
                      <label>Earliest Time</label>
                      <input
                        type="time"
                        value={court.earliest || "08:00"}
                        onChange={(e) =>
                          handleCourtTimeChange(
                            court.name,
                            "earliest",
                            e.target.value,
                          )
                        }
                        style={{ minWidth: 120 }}
                        disabled={isSubmitting}
                      />
                    </div>
                    <div>
                      <label>Latest Time</label>
                      <input
                        type="time"
                        value={court.latest || "23:00"}
                        onChange={(e) =>
                          handleCourtTimeChange(
                            court.name,
                            "latest",
                            e.target.value,
                          )
                        }
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
            <label>Location *</label>
            <input
              type="url"
              value={formData.location}
              onChange={(e) =>
                setFormData({ ...formData, location: e.target.value })
              }
              placeholder="Google Maps link (e.g., https://maps.google.com/...)"
              required
              disabled={isSubmitting}
            />
            <small className="form-hint">
              Please provide a valid Google Maps link. Accepted formats:
              maps.google.com, google.com/maps, goo.gl/maps, maps.app.goo.gl
            </small>
          </div>

          <div className="modal-actions">
            <button
              type="button"
              onClick={onClose}
              className="cancel-btn"
              disabled={isSubmitting}
            >
              Cancel
            </button>
            <button type="submit" className="save-btn" disabled={isSubmitting}>
              {isSubmitting ? "Adding..." : "Add"}
            </button>
          </div>
        </form>

        {toast.msg && (
          <Toast
            message={toast.msg}
            type={toast.type}
            onClose={() => setToast({ msg: "", type: "success" })}
          />
        )}
      </div>
    </div>
  );
};

// Edit Sport Modal Component
const EditSportModal = ({ sport, onClose, onSave }) => {
  const [formData, setFormData] = useState({
    name: sport?.name || "",
    courts:
      sport?.courts?.map((court) => ({
        name: court.name,
        earliest: court.earliest || "08:00",
        latest: court.latest || "23:00",
        status: court.status || "OPEN",
      })) || [],
    location: sport?.location || "",
  });
  const [newCourt, setNewCourt] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [toast, setToast] = useState({ msg: "", type: "success" });

  const addCourt = () => {
    const courtName = newCourt.trim();
    if (courtName && !formData.courts.some((c) => c.name === courtName)) {
      setFormData({
        ...formData,
        courts: [
          ...formData.courts,
          {
            name: courtName,
            earliest: "08:00",
            latest: "23:00",
            status: "OPEN",
          },
        ],
      });
      setNewCourt("");
    }
  };

  const removeCourt = (index) => {
    setFormData({
      ...formData,
      courts: formData.courts.filter((_, i) => i !== index),
    });
  };

  const handleCourtTimeChange = (courtIndex, field, value) => {
    const updatedCourts = formData.courts.map((court, index) => {
      if (index === courtIndex) {
        return { ...court, [field]: value };
      }
      return court;
    });

    setFormData({
      ...formData,
      courts: updatedCourts,
    });
  };

  const handleCourtNameChange = (courtIndex, newName) => {
    const updatedCourts = formData.courts.map((court, index) => {
      if (index === courtIndex) {
        return { ...court, name: newName };
      }
      return court;
    });

    setFormData({
      ...formData,
      courts: updatedCourts,
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsSubmitting(true);

    try {
      // Enhanced validation for location
      if (!formData.location || formData.location.trim() === "") {
        setToast({
          msg: "Sport Facility location cannot be empty",
          type: "error",
        });
        return;
      }

      // Validate Google Maps URL
      if (!validateGoogleMapsUrl(formData.location)) {
        setToast({
          msg: "Please provide a valid Google Maps link (e.g., https://maps.google.com/... or https://goo.gl/maps/...)",
          type: "error",
        });
        return;
      }

      // Validate court names are unique
      const courtNames = formData.courts.map((court) => court.name.trim());
      const uniqueNames = new Set(courtNames);
      if (courtNames.length !== uniqueNames.size) {
        setToast({ msg: "Court names must be unique", type: "error" });
        return;
      }

      // Validate time ranges
      for (const court of formData.courts) {
        const earliestTime = new Date(`1970-01-01T${court.earliest}:00`);
        const latestTime = new Date(`1970-01-01T${court.latest}:00`);

        if (earliestTime >= latestTime) {
          setToast({
            msg: `Court ${court.name}: Latest time must be after earliest time`,
            type: "error",
          });
          return;
        }
      }

      const originalCourts = sport?.courts || [];
      const updatedCourts = formData.courts;

      // 1. Detect added courts
      const addedCourts = updatedCourts.filter(
        updated => !originalCourts.some(orig => orig.name === updated.name)
      );

      // 2. Detect deleted courts
      const deletedCourts = originalCourts.filter(
        orig => !updatedCourts.some(updated => updated.name === orig.name)
      );

      // 3. Detect time changes
      const timeChangedCourts = updatedCourts.filter(updated => {
        const orig = originalCourts.find(o => o.name === updated.name);
        return (
          orig &&
          (orig.earliest !== updated.earliest || orig.latest !== updated.latest)
        );
      });

      // 4. Detect renamed courts (already handled)
      const renamedCourts = updatedCourts.filter((updated, idx) => {
        const orig = originalCourts[idx];
        return orig && updated.name !== orig.name;
      });

      let anyCourtChange = false;

      // Handle added courts
      for (const court of addedCourts) {
        const res = await authFetch(
          `/api/admin/${encodeURIComponent(sport.name)}/courts`,
          {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
              courts: [
                {
                  name: court.name,
                  earliestTime: timeToSeconds(court.earliest),
                  latestTime: timeToSeconds(court.latest),
                  status: getStatusValue(court.status),
                },
              ],
            }),
          }
        );
        if (!res.ok) {
          const errorData = await res.json();
          throw new Error(errorData.error || "Failed to add court");
        }
        anyCourtChange = true;
      }

      // Handle deleted courts
      for (const court of deletedCourts) {
        const res = await authFetch(
          `/api/admin/${encodeURIComponent(sport.name)}/courts?courtName=${encodeURIComponent(court.name)}`,
          { method: "DELETE" }
        );
        if (!res.ok) {
          const errorData = await res.json();
          throw new Error(errorData.error || "Failed to delete court");
        }
        anyCourtChange = true;
      }

      // Handle time changes
      for (const court of timeChangedCourts) {
        const orig = originalCourts.find(o => o.name === court.name);
        const res = await authFetch(
          `/api/admin/${encodeURIComponent(sport.name)}/courts`,
          {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
              oldCourtName: court.name,
              newCourtName: "",
              earliestTime: timeToSeconds(court.earliest),
              latestTime: timeToSeconds(court.latest),
              status: getStatusValue(court.status),
            }),
          }
        );
        if (!res.ok) {
          const errorData = await res.json();
          throw new Error(errorData.error || "Failed to update court time");
        }
        anyCourtChange = true;
      }

      // Handle renamed courts (already in your code)
      for (const updatedCourt of renamedCourts) {
        const originalCourt = originalCourts.find(
          c =>
            c.name !== updatedCourt.name &&
            c.earliest === updatedCourt.earliest &&
            c.latest === updatedCourt.latest
        );
        if (originalCourt) {
          const res = await authFetch(
            `/api/admin/${encodeURIComponent(sport.name)}/courts`,
            {
              method: "PUT",
              headers: { "Content-Type": "application/json" },
              body: JSON.stringify({
                oldCourtName: originalCourt.name,
                newCourtName: updatedCourt.name,
                earliestTime: timeToSeconds(updatedCourt.earliest),
                latestTime: timeToSeconds(updatedCourt.latest),
                status: getStatusValue(updatedCourt.status),
              }),
            }
          );
          if (!res.ok) {
            const errorData = await res.json();
            throw new Error(errorData.error || "Failed to update court");
          }
          anyCourtChange = true;
        }
      }

      // Only call updateSportFacility if facility-level fields changed
      if (
        formData.name !== sport.name ||
        formData.location !== sport.location
      ) {
        await onSave(formData);
      }

      setToast({
        msg: anyCourtChange
          ? "Court(s) updated successfully"
          : "Sport facility updated successfully",
        type: "success",
      });

      setTimeout(() => {
        onClose();
        window.location.reload(); 
      }, 500);
    } catch (error) {
      console.error("Error in form submission:", error);
      setToast({
        msg: error.message || "Failed to update sport facility",
        type: "error",
      });
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="modal-overlay">
      <div className="modal" style={{ maxWidth: "700px" }}>
        <div className="modal-header">
          <h3>Edit Sport Facility</h3>
          <button
            className="close-btn"
            onClick={onClose}
            disabled={isSubmitting}
          >
            <X size={20} />
          </button>
        </div>
        <form className="modal-form" onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Sport Name *</label>
            <input
              type="text"
              value={formData.name}
              onChange={(e) =>
                setFormData({ ...formData, name: e.target.value })
              }
              required
              disabled={isSubmitting}
              placeholder="Enter sport facility name"
            />
          </div>

          <div className="form-group">
            <label>Location *</label>
            <input
              type="url"
              value={formData.location}
              onChange={(e) =>
                setFormData({ ...formData, location: e.target.value })
              }
              placeholder="Google Maps link (e.g., https://maps.google.com/...)"
              required
              disabled={isSubmitting}
            />
            <small className="form-hint">
              Please provide a valid Google Maps link. <br></br>Accepted
              formats: maps.google.com, google.com/maps, goo.gl/maps,
              maps.app.goo.gl
            </small>
          </div>

          <div className="form-group">
            <label>Court Details</label>
            <div className="court-input-group">
              <input
                type="text"
                value={newCourt}
                onChange={(e) => setNewCourt(e.target.value)}
                placeholder="Enter new court name"
                disabled={isSubmitting}
              />
              <button
                type="button"
                onClick={addCourt}
                className="add-court-btn"
                disabled={isSubmitting}
              >
                Add Court
              </button>
            </div>

            <div className="existing-courts-list">
              {formData.courts.length === 0 ? (
                <p className="no-courts-message">
                  No courts added yet. Add courts using the form above.
                </p>
              ) : (
                formData.courts.map((court, index) => (
                  <div key={index} className="court-edit-item">
                    <div className="court-header">
                      <div className="court-name-input">
                        <label>Court Name</label>
                        <input
                          type="text"
                          value={court.name}
                          onChange={(e) =>
                            handleCourtNameChange(index, e.target.value)
                          }
                          disabled={isSubmitting}
                          placeholder="Court name"
                        />
                      </div>
                      <button
                        type="button"
                        onClick={() => removeCourt(index)}
                        className="remove-court-btn"
                        disabled={isSubmitting}
                        title="Remove this court"
                      >
                        <X size={16} />
                      </button>
                    </div>

                    <div className="court-time-settings">
                      <div className="time-range-group">
                        <div className="time-input">
                          <label>Earliest Time</label>
                          <input
                            type="time"
                            value={court.earliest}
                            onChange={(e) =>
                              handleCourtTimeChange(
                                index,
                                "earliest",
                                e.target.value,
                              )
                            }
                            disabled={isSubmitting}
                          />
                        </div>
                        <div className="time-input">
                          <label>Latest Time</label>
                          <input
                            type="time"
                            value={court.latest}
                            onChange={(e) =>
                              handleCourtTimeChange(
                                index,
                                "latest",
                                e.target.value,
                              )
                            }
                            disabled={isSubmitting}
                          />
                        </div>
                      </div>

                      <div className="court-status-display">
                        <span
                          className={`status-badge ${court.status.toLowerCase()}`}
                        >
                          {court.status}
                        </span>
                      </div>
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>

          <div className="modal-actions">
            <button
              type="button"
              onClick={onClose}
              className="cancel-btn"
              disabled={isSubmitting}
            >
              Cancel
            </button>
            <button type="submit" className="save-btn" disabled={isSubmitting}>
              {isSubmitting ? "Saving Changes..." : "Save Changes"}
            </button>
          </div>
        </form>

        {toast.msg && (
          <Toast
            message={toast.msg}
            type={toast.type}
            onClose={() => setToast({ msg: "", type: "success" })}
          />
        )}
      </div>
    </div>
  );
};

// Add Court Modal Component with Real Functionality
const AddCourtModal = ({ sportName, onClose, onSave }) => {
  const [formData, setFormData] = useState({
    courtName: "",
    earliest: "08:00",
    latest: "23:00",
  });
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [toast, setToast] = useState({ msg: "", type: "success" });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsSubmitting(true);

    try {
      // Frontend validation
      if (!formData.courtName.trim()) {
        setToast({ msg: "Court name is required", type: "error" });
        return;
      }

      const earliestTime = new Date(`1970-01-01T${formData.earliest}:00`);
      const latestTime = new Date(`1970-01-01T${formData.latest}:00`);

      if (earliestTime >= latestTime) {
        setToast({
          msg: "Latest time must be after earliest time",
          type: "error",
        });
        return;
      }

      const requestData = {
        courts: [
          {
            name: formData.courtName.trim(),
            earliestTime: timeToSeconds(formData.earliest),
            latestTime: timeToSeconds(formData.latest),
            status: 0,
          },
        ],
      };

      const res = await authFetch(
        `/api/admin/${encodeURIComponent(sportName)}/courts`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify(requestData),
        },
      );

      if (!res.ok) {
        const errorData = await res.json();
        throw new Error(errorData.error || "Failed to add court");
      }

      const result = await res.json();
      setToast({
        msg: result.message || "Court added successfully",
        type: "success",
      });

      // Close modal and refresh both sections
      setTimeout(() => {
        onSave(); // This will trigger court section refresh
        onClose();
      }, 1500);
    } catch (err) {
      console.error("Error adding court:", err);
      setToast({ msg: err.message, type: "error" });
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="modal-overlay">
      <div className="modal">
        <div className="modal-header">
          <h3>Add New Court</h3>
          <button
            className="close-btn"
            onClick={onClose}
            disabled={isSubmitting}
          >
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
              onChange={(e) =>
                setFormData({ ...formData, courtName: e.target.value })
              }
              required
              placeholder="Enter court name"
              disabled={isSubmitting}
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
                  onChange={(e) =>
                    setFormData({ ...formData, earliest: e.target.value })
                  }
                  disabled={isSubmitting}
                />
              </div>
              <div>
                <label>Latest Time</label>
                <input
                  type="time"
                  value={formData.latest}
                  onChange={(e) =>
                    setFormData({ ...formData, latest: e.target.value })
                  }
                  disabled={isSubmitting}
                />
              </div>
            </div>
          </div>
          <div className="modal-actions">
            <button
              type="button"
              onClick={onClose}
              className="cancel-btn"
              disabled={isSubmitting}
            >
              Cancel
            </button>
            <button type="submit" className="save-btn" disabled={isSubmitting}>
              {isSubmitting ? "Adding Court..." : "Add Court"}
            </button>
          </div>
        </form>
      </div>

      {toast.msg && (
        <Toast
          message={toast.msg}
          type={toast.type}
          onClose={() => setToast({ msg: "", type: "success" })}
        />
      )}
    </div>
  );
};

// Delete Court Modal Component
const DeleteCourtModal = ({ sportName, courts, onClose, onSave }) => {
  const [selectedCourt, setSelectedCourt] = useState("");
  const [isDeleting, setIsDeleting] = useState(false);
  const [toast, setToast] = useState({ msg: "", type: "success" });

  const handleDelete = async () => {
    if (!selectedCourt) {
      setToast({ msg: "Please select a court to delete", type: "error" });
      return;
    }

    setIsDeleting(true);
    try {
      const res = await authFetch(
        `/api/admin/${encodeURIComponent(sportName)}/courts?courtName=${encodeURIComponent(selectedCourt)}`,
        { method: "DELETE" }
      );

      if (!res.ok) {
        const errorData = await res.json();
        throw new Error(errorData.error || "Failed to delete court");
      }

      const result = await res.json();
      setToast({
        msg: result.message || "Court deleted successfully",
        type: "success",
      });

      // Close modal and refresh both sections
      setTimeout(() => {
        onSave(); // This will trigger court section refresh
        onClose();
      }, 1500);
    } catch (err) {
      console.error("Error deleting court:", err);
      setToast({ msg: err.message, type: "error" });
    } finally {
      setIsDeleting(false);
    }
  };

  return (
    <div className="modal-overlay">
      <div className="modal">
        <div className="modal-header">
          <h3>Delete Court</h3>
          <button className="close-btn" onClick={onClose} disabled={isDeleting}>
            <X size={20} />
          </button>
        </div>
        <div className="modal-form">
          <div className="form-group">
            <label>Associated Sport Facility</label>
            <input type="text" value={sportName} disabled />
          </div>
          <div className="form-group">
            <label>Select Court to Delete *</label>
            <select
              value={selectedCourt}
              onChange={(e) => setSelectedCourt(e.target.value)}
              required
              disabled={isDeleting}
            >
              <option value="">Select a court</option>
              {courts.map((court) => (
                <option key={court.name} value={court.name}>
                  Court {court.name}
                </option>
              ))}
            </select>
          </div>

          <div className="modal-actions">
            <button
              onClick={onClose}
              className="cancel-btn"
              disabled={isDeleting}
            >
              Cancel
            </button>
            <button
              onClick={handleDelete}
              className="delete-btn"
              disabled={isDeleting || !selectedCourt}
            >
              {isDeleting ? (
                <>
                  <div className="button-spinner"></div>
                  Deleting...
                </>
              ) : (
                "Delete Court"
              )}
            </button>
          </div>
        </div>

        {toast.msg && (
          <Toast
            message={toast.msg}
            type={toast.type}
            onClose={() => setToast({ msg: "", type: "success" })}
          />
        )}
      </div>
    </div>
  );
};

// Update Availability Modal Component
const UpdateAvailabilityModal = ({
  onClose,
  onSave,
  selectedFacility,
  availableCourts,
}) => {
  const [formData, setFormData] = useState({
    court: "",
    status: "MAINTENANCE",
    startDate: "",
    endDate: "",
  });
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [toast, setToast] = useState({ msg: "", type: "success" });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsSubmitting(true);

    try {
      // Frontend validation
      if (!formData.court) {
        setToast({ msg: "Please select a court", type: "error" });
        return;
      }

      // Only status is updated, other params are default
      const requestBody = {
        oldCourtName: formData.court,
        newCourtName: "",
        earliestTime: 0,
        latestTime: 0,
        status: getStatusValue(formData.status),
      };

      const res = await authFetch(
        `/api/admin/${encodeURIComponent(selectedFacility)}/courts`,
        {
          method: "PUT",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify(requestBody),
        }
      );

      if (!res.ok) {
        const errorData = await res.json();
        throw new Error(
          errorData.error || "Failed to update court availability"
        );
      }

      const result = await res.json();
      setToast({
        msg: result.message || "Court availability updated successfully",
        type: "success",
      });

      // Close modal and refresh data
      setTimeout(() => {
        onSave();
        onClose();
      }, 1500);
    } catch (err) {
      console.error("Error updating court availability:", err);
      setToast({ msg: err.message, type: "error" });
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="modal-overlay">
      <div className="modal">
        <div className="modal-header">
          <h3>Update Court Availability</h3>
          <button
            className="close-btn"
            onClick={onClose}
            disabled={isSubmitting}
          >
            <X size={20} />
          </button>
        </div>
        <form className="modal-form" onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Sport Facility</label>
            <input type="text" value={selectedFacility} disabled />
          </div>

          <div className="form-group">
            <label>Court *</label>
            <select
              value={formData.court}
              onChange={(e) =>
                setFormData({ ...formData, court: e.target.value })
              }
              required
              disabled={isSubmitting}
            >
              <option value="">Select a court</option>
              {availableCourts.map((court) => (
                <option key={court.name} value={court.name}>
                  Court {court.name}
                </option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label>Status *</label>
            <select
              value={formData.status}
              onChange={(e) =>
                setFormData({ ...formData, status: e.target.value })
              }
              disabled={isSubmitting}
            >
              <option value="MAINTENANCE">Maintenance</option>
              <option value="CLOSED">Closed</option>
              <option value="OPEN">Open</option>
            </select>
          </div>

          <div className="time-range-group">
            <div className="form-group">
              <label>Start Date *</label>
              <input
                type="date"
                value={formData.startDate}
                onChange={(e) =>
                  setFormData({ ...formData, startDate: e.target.value })
                }
                required
                disabled={isSubmitting}
                min={new Date().toISOString().split("T")[0]}
              />
            </div>

            <div className="form-group">
              <label>End Date *</label>
              <input
                type="date"
                value={formData.endDate}
                onChange={(e) =>
                  setFormData({ ...formData, endDate: e.target.value })
                }
                required
                disabled={isSubmitting}
                min={
                  formData.startDate || new Date().toISOString().split("T")[0]
                }
              />
            </div>
          </div>

          <div className="modal-actions">
            <button
              type="button"
              onClick={onClose}
              className="cancel-btn"
              disabled={isSubmitting}
            >
              Cancel
            </button>
            <button
              type="submit"
              className="save-btn"
              disabled={isSubmitting || !formData.court}
            >
              {isSubmitting ? (
                <>
                  Updating...
                </>
              ) : (
                "Update Availability"
              )}
            </button>
          </div>
        </form>

        {toast.msg && (
          <Toast
            message={toast.msg}
            type={toast.type}
            onClose={() => setToast({ msg: "", type: "success" })}
          />
        )}
      </div>
    </div>
  );
};

// Delete Confirmation Modal Component
const DeleteConfirmModal = ({
  title,
  message,
  onClose,
  onConfirm,
  isLoading = false,
}) => {
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
          <button
            onClick={onConfirm}
            className="delete-btn"
            disabled={isLoading}
          >
            {isLoading ? (
              <>
                <div className="button-spinner"></div>
                Deleting...
              </>
            ) : (
              "Delete"
            )}
          </button>
        </div>
      </div>
    </div>
  );
};

export default SportAndCourtManagement;
