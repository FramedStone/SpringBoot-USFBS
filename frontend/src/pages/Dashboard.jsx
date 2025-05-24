import React from "react";

function Dashboard() {
  const handleBooking = (booking) => {
    console.log("Booking submitted:", booking);
  };

  return (
    <div>
      <h2>Dashboard</h2>
      {/*
      <BookingForm onSubmit={handleBooking} />
      <BookingList />
      */}
      <p>
        🛠️ BookingForm and BookingList components are not created yet.
      </p>
    </div>
  );
}

export default Dashboard;