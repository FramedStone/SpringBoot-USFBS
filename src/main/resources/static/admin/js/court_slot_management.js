
        // Wait for DOM to load
        document.addEventListener('DOMContentLoaded', function() {
            // Add click event to all edit buttons
            document.querySelectorAll('.edit').forEach(button => {
                button.addEventListener('click', function() {
                    const courtName = this.getAttribute('data-court');
                    const locationUrl = this.getAttribute('data-location');
                    openEditModal(courtName, locationUrl);
                });
            });

            // Add click event to all delete buttons
            document.querySelectorAll('.delete').forEach(button => {
                button.addEventListener('click', function() {
                    if (confirm('Are you sure you want to delete this court?')) {
                        const row = this.closest('tr');
                        row.remove();
                    }
                });
            });

            // Add new sport button
            document.getElementById('addSport').addEventListener('click', function() {
                const sportName = prompt('Enter the name of the new sport:');
                if (sportName) {
                    const tbody = document.querySelector('.sport-table tbody');
                    const newRow = document.createElement('tr');
                    newRow.innerHTML = `
                        <td><input type="radio" name="sport"> ${sportName}</td>
                        <td><button class="delete"><i class="fas fa-trash"></i> Delete</button></td>
                    `;
                    tbody.appendChild(newRow);
                    
                    // Add event listener to the new delete button
                    newRow.querySelector('.delete').addEventListener('click', function() {
                        if (confirm('Are you sure you want to delete this sport?')) {
                            newRow.remove();
                        }
                    });
                }
            });
            
            // Add new court button
            document.getElementById('addCourt').addEventListener('click', function() {
                openAddCourtModal();
            });
        });

        // Modal functions
        function openEditModal(courtName, locationUrl) {
            document.getElementById('courtName').textContent = courtName;
            document.getElementById('editLocation').value = locationUrl;
            
            // Find the row for this court
            const rows = document.querySelectorAll('#courtTable tbody tr');
            let currentStatus = 'normal';
            
            for (const row of rows) {
                if (row.cells[0].textContent.trim() === courtName) {
                    currentStatus = row.querySelector('.status-select').value;
                    break;
                }
            }
            
            document.getElementById('editStatus').value = currentStatus;
            document.getElementById('editModal').style.display = 'block';
            
            // Generate time slots
            const container = document.getElementById('timeSlotsContainer');
            container.innerHTML = '';
            
            for (let hour = 8; hour <= 23; hour++) {
                const timeSlot = document.createElement('div');
                timeSlot.className = 'time-slot';
                timeSlot.innerHTML = `<i class="fas fa-clock"></i> ${hour}:00`;
                timeSlot.onclick = function() {
                    this.classList.toggle('selected');
                };
                container.appendChild(timeSlot);
            }
        }
        
        function closeModal() {
            document.getElementById('editModal').style.display = 'none';
        }
        
        function applyChanges() {
            const courtName = document.getElementById('courtName').textContent;
            const newStatus = document.getElementById('editStatus').value;
            const newLocation = document.getElementById('editLocation').value;
            
            // Find the row for this court
            const rows = document.querySelectorAll('#courtTable tbody tr');
            
            for (const row of rows) {
                if (row.cells[0].textContent.trim() === courtName) {
                    // Update status in table
                    row.querySelector('.status-select').value = newStatus;
                    
                    // Update location in table
                    const locationLink = row.querySelector('.location-link');
                    locationLink.href = newLocation;
                    locationLink.textContent = newLocation.includes('maps.google.com') ? 'Google Map link' : newLocation;
                    
                    break;
                }
            }
            
            alert('Changes applied successfully!');
            closeModal();
        }
        
        // Add Court Modal functions
        function openAddCourtModal() {
            document.getElementById('addCourtModal').style.display = 'block';
            document.getElementById('newCourtName').value = '';
            document.getElementById('newCourtStatus').value = 'normal';
            document.getElementById('newCourtLocation').value = 'https://maps.google.com';
            document.getElementById('startTime').value = '8';
            document.getElementById('endTime').value = '23';
            
            // Initialize time slots as all unavailable
            const container = document.getElementById('newCourtTimeSlots');
            container.innerHTML = '';
            
            for (let hour = 8; hour <= 23; hour++) {
                const timeSlot = document.createElement('div');
                timeSlot.className = 'time-slot booked';
                timeSlot.innerHTML = `<i class="fas fa-times"></i> ${hour}:00`;
                timeSlot.onclick = function() {
                    if (this.classList.contains('booked')) {
                        this.classList.remove('booked');
                        this.classList.add('available');
                        this.innerHTML = `<i class="fas fa-check"></i> ${hour}:00`;
                    } else {
                        this.classList.remove('available');
                        this.classList.add('booked');
                        this.innerHTML = `<i class="fas fa-times"></i> ${hour}:00`;
                    }
                };
                container.appendChild(timeSlot);
            }
        }
        
        function closeAddCourtModal() {
            document.getElementById('addCourtModal').style.display = 'none';
        }
        
        function applyTimeRange() {
            const startHour = parseInt(document.getElementById('startTime').value);
            const endHour = parseInt(document.getElementById('endTime').value);
            
            if (startHour >= endHour) {
                alert('End time must be after start time');
                return;
            }
            
            const timeSlots = document.querySelectorAll('#newCourtTimeSlots .time-slot');
            
            timeSlots.forEach(slot => {
                const hourText = slot.textContent.split(':')[0].trim();
                const hour = parseInt(hourText);
                
                if (hour >= startHour && hour < endHour) {
                    slot.classList.remove('booked');
                    slot.classList.add('available');
                    slot.innerHTML = `<i class="fas fa-check"></i> ${hour}:00`;
                } else {
                    slot.classList.remove('available');
                    slot.classList.add('booked');
                    slot.innerHTML = `<i class="fas fa-times"></i> ${hour}:00`;
                }
            });
        }
        
        function addNewCourt() {
            const courtName = document.getElementById('newCourtName').value.trim();
            const status = document.getElementById('newCourtStatus').value;
            const location = document.getElementById('newCourtLocation').value;
            
            if (!courtName) {
                alert('Please enter a court name');
                return;
            }
            
            // Check if court with this name already exists
            const existingCourts = document.querySelectorAll('#courtTable tbody tr td:first-child');
            for (const court of existingCourts) {
                if (court.textContent.trim() === courtName) {
                    alert('A court with this name already exists');
                    return;
                }
            }
            
            // Get selected time slots
            const timeSlots = document.querySelectorAll('#newCourtTimeSlots .time-slot');
            const timeSlotStatus = [];
            
            timeSlots.forEach(slot => {
                timeSlotStatus.push(slot.classList.contains('available') ? 'available' : 'booked');
            });
            
            // Add new row to table
            const tbody = document.querySelector('#courtTable tbody');
            const newRow = document.createElement('tr');
            newRow.className = 'time-data-row';
            
            let timeSlotCells = '';
            for (let i = 0; i < timeSlotStatus.length; i++) {
                const statusClass = timeSlotStatus[i];
                const icon = statusClass === 'available' ? 'fa-check' : 'fa-times';
                timeSlotCells += `<td class="${statusClass}"><i class="fas ${icon} time-icon"></i></td>`;
            }
            
            newRow.innerHTML = `
                <td>${courtName}</td>
                <td>
                    <select class="status-select">
                        <option value="normal" ${status === 'normal' ? 'selected' : ''}>Normal</option>
                        <option value="maintenance" ${status === 'maintenance' ? 'selected' : ''}>Maintenance</option>
                        <option value="damaged" ${status === 'damaged' ? 'selected' : ''}>Damaged</option>
                    </select>
                </td>
                <td><a href="${location}" class="location-link">${location.includes('maps.google.com') ? 'Google Map link' : location}</a></td>
                ${timeSlotCells}
                <td>
                    <div class="action-buttons-container">
                        <button class="edit" data-court="${courtName}" data-location="${location}"><i class="fas fa-edit"></i></button>
                        <button class="delete"><i class="fas fa-trash"></i></button>
                    </div>
                </td>
            `;
            
            tbody.appendChild(newRow);
            
            // Add event listeners to new buttons
            newRow.querySelector('.edit').addEventListener('click', function() {
                const courtName = this.getAttribute('data-court');
                const locationUrl = this.getAttribute('data-location');
                openEditModal(courtName, locationUrl);
            });
            
            newRow.querySelector('.delete').addEventListener('click', function() {
                if (confirm('Are you sure you want to delete this court?')) {
                    const row = this.closest('tr');
                    row.remove();
                }
            });
            
            alert('Court added successfully!');
            closeAddCourtModal();
        }
        
        // Close modal when clicking outside of it
        window.onclick = function(event) {
            if (event.target == document.getElementById('editModal')) {
                closeModal();
            }
            if (event.target == document.getElementById('addCourtModal')) {
                closeAddCourtModal();
            }
        }