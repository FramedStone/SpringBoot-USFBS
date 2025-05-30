
    function exportPDF() {
      const { jsPDF } = window.jspdf;
      const doc = new jsPDF();

      doc.text("Booking Report", 14, 15);

      doc.autoTable({
        startY: 20,
        head: [['ID', 'User', 'Court', 'Time', 'Sport', 'Status']],
        body: [
          ['BK001', '123120000', 'Court A', '2025-03-24 | 10 AM - 12 PM', 'Badminton', 'Approved'],
          ['BK002', '123120001', 'Court B', '2025-03-25 | 8 AM - 10 AM', 'Volleyball', 'Pending'],
          ['BK003', '123120002', 'Court C', '2025-03-26 | 1 PM - 3 PM', 'Basketball', 'Rejected'],
        ]
      });

      doc.save('booking_report.pdf');
    }
  