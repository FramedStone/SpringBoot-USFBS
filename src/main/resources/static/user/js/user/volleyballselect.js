document.addEventListener('DOMContentLoaded', function () {
    const statusCells = document.querySelectorAll('.status-cell');
    const notificationCountEl = document.getElementById('notificationCount');
    let bookingCount = 0;

    statusCells.forEach(cell => {
      if (cell.classList.contains('not-available')) return;

      // Make cell initially available
      cell.classList.remove('booked');
      cell.classList.add('available');
      cell.style.cursor = 'pointer';

      // Add click handler
      cell.addEventListener('click', function () {
        if (cell.classList.contains('available')) {
          cell.classList.remove('available');
          cell.classList.add('booked');
          cell.style.cursor = 'default';

          // Increase booking count
          bookingCount++;
          notificationCountEl.textContent = bookingCount;

          // Show popup message
          alert("You have booked this venue!");

          // Disable further clicks
          const newCell = cell.cloneNode(true);
          cell.replaceWith(newCell);
        }
      });
    });
  });