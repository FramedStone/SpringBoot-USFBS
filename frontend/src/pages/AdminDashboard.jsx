import React from "react";
import "../styles/AdminDashboard.css";

function AdminDashboard() {
  return (
    <div>
      <nav className="navbar">
        <a className="nav-item active" href="/dashboard" title="Dashboard">
          <i className="fa-solid fa-table-columns"></i>
          Dashboard
        </a>
        <a className="nav-item" href="/user-management">
          <i className="fa-solid fa-user-group"></i>
          User Management
        </a>
        <a className="nav-item" href="/court-slot-management">
          <i className="fa-solid fa-layer-group"></i>
          Court &amp; Slot Management
        </a>
        <a className="nav-item" href="/booking-management">
          <i className="fa-solid fa-calendar-check"></i>
          Booking Management
        </a>
      </nav>

      <div className="container">
        <div className="logs box">
          <h3 className="section-header">
            <i className="fas fa-history"></i> System Logs
            <a href="/system-logs" className="external-link" title="Go to System Logs">
              <i className="fa-solid fa-arrow-up-right-from-square"></i>
            </a>
          </h3>
          <div className="log-entry received">Form Received</div>
          <div className="log-entry action">Admin Action</div>
          <div className="log-entry approved">Booking Approved</div>
          <div className="log-entry action">Admin Action</div>
        </div>

        <div className="main">
          <div className="announcement box">
            <div className="export-controls">
              <h3 className="section-header">
                <i className="fas fa-bullhorn"></i> Announcement
              </h3>
              <a href="/admin/add-announcement" className="btn-primary">Add New</a>
            </div>
            <table className="announcement-table">
              <thead>
                <tr>
                  <th>Title</th>
                  <th>Time Left</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td>Court Maintenance</td>
                  <td>1 Days</td>
                  <td className="action-buttons">
                    <a>Edit</a>
                    <a className="delete">Delete</a>
                  </td>
                </tr>
                <tr>
                  <td>Basketball Tournament</td>
                  <td>7 Days</td>
                  <td className="action-buttons">
                    <a>Edit</a>
                    <a className="delete">Delete</a>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <div className="bookings box">
            <div className="export-controls">
              <h3 className="section-header">
                <i className="fas fa-clipboard-list"></i> Bookings
                <i className="fa-solid fa-arrow-up-right-from-square external-link"></i>
              </h3>
              <button className="btn-primary">Export Report</button>
            </div>
            <table className="booking-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>User</th>
                  <th>Court</th>
                  <th>Time</th>
                  <th>Sport</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td>BK001</td>
                  <td>123120000</td>
                  <td>Court A</td>
                  <td>2025-03-24<br />10 AM - 12 PM</td>
                  <td>Badminton</td>
                  <td><button className="btn-reject">Reject</button></td>
                </tr>
                <tr>
                  <td>BK002</td>
                  <td>123120001</td>
                  <td>Court B</td>
                  <td>2025-03-25<br />8 AM - 10 AM</td>
                  <td>Volleyball</td>
                  <td><button className="btn-reject">Reject</button></td>
                </tr>
                <tr>
                  <td>BK003</td>
                  <td>123120002</td>
                  <td>Court C</td>
                  <td>2025-03-26<br />13 PM - 15 PM</td>
                  <td>Basketball</td>
                  <td><button className="btn-reject">Reject</button></td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
}

export default AdminDashboard;