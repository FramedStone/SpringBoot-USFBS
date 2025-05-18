// SPDX-License-Identifier: MIT 
pragma solidity 0.8.19;

import "./SportFacility.sol";

contract Booking {
    // Variable & Modifier Initialization
    address private immutable admin_;
    SportFacility immutable sportFacilityContract;
    enum bookingStatus { APPROVED, PENDING, REJECTED, COMPLETED, CANCELLED }

    struct bookingTransaction {
        address user;
        uint256 bookingId;
        bytes32 ipfsHash;
        string sportFacility;
        string court;
        uint256 startTime;
        uint256 endTime;
        bookingStatus status;
        string[] note;
    } 
    bookingTransaction[] private bookings; 

    modifier isAdmin {
        require(msg.sender == admin_, "Access denied");
        _;
    } 

    // Events
    event bookingCreated(
        address indexed from,
        uint256 bookingId,
        bytes32 ipfsHash,
        string sportFacility,
        string court,
        uint256 startTime,
        uint256 endTime,
        string status,
        string note,
        uint256 time
    );
    event bookingStatusUpdated(
        uint256 bookingId,
        bytes32 ipfsHash,
        string sportFacility,
        string court,
        uint256 startTime,
        uint256 endTime,
        string status,
        string note,
        uint256 time
    );
    event bookingDeleted(
        uint256 bookingId,
        bytes32 ipfsHash,
        string sportFacility,
        string court,
        uint256 startTime,
        uint256 endTime,
        string status,
        string note,
        uint256 time
    );
    event bookingNoteAppended(
        uint256 bookingId,
        bytes32 ipfsHash,
        string sportFacility,
        string court,
        uint256 startTime,
        uint256 endTime,
        string status,
        string note,
        uint256 time
    );

    // Helper Functions
    function bookingStatusToString(bookingStatus status) internal pure returns(string memory) {
        if(status == bookingStatus.APPROVED) return "approved";
        if(status == bookingStatus.PENDING) return "pending";
        if(status == bookingStatus.REJECTED) return "rejected";
        if(status == bookingStatus.COMPLETED) return "completed";
        if(status == bookingStatus.CANCELLED) return "cancelled";
        return "unknown"; 
    }

    // check for sport facility availability 
    function isAvailable(
        string memory sportFacility,
        string memory court,
        uint256 startTime,
        uint256 endTime
    ) internal view returns(bool) {
        (uint256 earliestTime, uint256 latestTime) = sportFacilityContract.getAvailableTimeRange(sportFacility, court);
        if(startTime >= earliestTime && endTime <= latestTime) {
            for(uint256 i=0; i<bookings.length; i++) {
                if(
                    keccak256(bytes(bookings[i].sportFacility)) == keccak256(bytes(sportFacility)) &&
                    keccak256(bytes(bookings[i].court)) == keccak256(bytes(court))
                    ) {
                        if(bookings[i].startTime < endTime && bookings[i].endTime > startTime && bookings[i].status != bookingStatus.COMPLETED) {
                            return false;
                        }
                    }
            }
        } else {
            return false;
        }
        return true;
    }

    // Main Functions
    constructor(address admin, address sportFacilityAddress) {
       admin_ = admin; 
       sportFacilityContract = SportFacility(sportFacilityAddress);
    }

    // create booking
    // admin (to book more than 2 hours)
    function createBooking_(
        bytes32 ipfsHash,
        string memory sportFacility,
        string memory court,
        uint256 startTime,
        uint256 endTime
    ) external isAdmin returns(uint256) {
        uint256 bookingId = bookings.length;
        // check for available element slot
        for(uint256 i=0; i<bookings.length; i++) {
            if(bookings[i].bookingId == 0)
                bookingId = i;
        }
        bookingTransaction memory booking = bookingTransaction(msg.sender, bookingId, ipfsHash, sportFacility, court, startTime, endTime, bookingStatus.PENDING, new string[](0));
        emit bookingCreated(msg.sender, bookingId, ipfsHash, sportFacility, court, startTime, endTime, bookingStatusToString(bookingStatus.PENDING), "", block.timestamp);

        if(isAvailable(sportFacility, court, startTime, endTime)) {
            booking.status = bookingStatus.APPROVED;
            emit bookingStatusUpdated(bookingId, ipfsHash, sportFacility, court, startTime, endTime, bookingStatusToString(bookingStatus.APPROVED), "Approved (system)", block.timestamp);
        } else {
            booking.status = bookingStatus.REJECTED;
            emit bookingStatusUpdated(bookingId, ipfsHash, sportFacility, court, startTime, endTime, bookingStatusToString(bookingStatus.REJECTED), "Rejected due to conflict (system)", block.timestamp);
        }
        if(bookingId == bookings.length) 
            bookings.push(booking);
        else {
            bookings[bookingId] = booking;
        }

        return bookingId;
    }
    // user (max 2 hours, min 1 hour)
    function createBooking(
        bytes32 ipfsHash,
        string memory sportFacility,
        string memory court,
        uint256 startTime,
        uint256 endTime
    ) external returns(uint256) {
        require(endTime - startTime == 3600 || endTime - startTime == 7200, "Booking time minimum 1hour, maximum 2hours (system)");

        uint256 bookingId = bookings.length;
        // check for available element slot
        for(uint256 i=0; i<bookings.length; i++) {
            if(bookings[i].bookingId == 0)
                bookingId = i;
        }
        bookingTransaction memory booking = bookingTransaction(msg.sender, bookingId, ipfsHash, sportFacility, court, startTime, endTime, bookingStatus.PENDING, new string[](0));
        emit bookingCreated(msg.sender, bookingId, ipfsHash, sportFacility, court, startTime, endTime, bookingStatusToString(bookingStatus.PENDING), "", block.timestamp);

        if(isAvailable(sportFacility, court, startTime, endTime)) {
            booking.status = bookingStatus.APPROVED;
            emit bookingStatusUpdated(bookingId, ipfsHash, sportFacility, court, startTime, endTime, bookingStatusToString(bookingStatus.APPROVED), "Approved (system)", block.timestamp);
        } else {
            booking.status = bookingStatus.REJECTED;
            emit bookingStatusUpdated(bookingId, ipfsHash, sportFacility, court, startTime, endTime, bookingStatusToString(bookingStatus.REJECTED), "Rejected due to conflict (system)", block.timestamp);
        }
        if(bookingId == bookings.length) 
            bookings.push(booking);
        else {
            bookings[bookingId] = booking;
        }

        return bookingId;
    }

    // delete booking on-chain if completed
    function deleteBooking_(bytes32 ipfsHash, uint256 bookingId) internal {
        require(bookingId < bookings.length, "Index out of bound (bookings)");
        require(bookings[bookingId].ipfsHash == ipfsHash, "ipfsHash doesn't match (bookings)");
        require(bookings[bookingId].bookingId == bookingId, "bookingId doesn't match (bookings)");
        require(block.timestamp >= bookings[bookingId].endTime, "booking not yet expired (bookings)"); 

        // cache values before delete
        string memory sportFacility = bookings[bookingId].sportFacility;
        string memory court = bookings[bookingId].court;
        uint256 startTime = bookings[bookingId].startTime;
        uint256 endTime = bookings[bookingId].endTime;
        delete bookings[bookingId];

        emit bookingDeleted(bookingId, ipfsHash, sportFacility, court, startTime, endTime, bookingStatusToString(bookingStatus.COMPLETED), "Deleted on-chain (system)", block.timestamp);
    }

    // cancel booking
    function cancelBooking(bytes32 ipfsHash, uint256 bookingId) external {
        require(bookings[bookingId].ipfsHash == ipfsHash, "ipfsHash doesn't match (bookings)");
        require(bookings[bookingId].bookingId == bookingId, "bookingId doesn't match (bookings)");
        require(bookings[bookingId].user == msg.sender, "Access denied (bookings)");

        bookings[bookingId].status = bookingStatus.CANCELLED;
        emit bookingStatusUpdated(bookingId, ipfsHash, bookings[bookingId].sportFacility, bookings[bookingId].court, bookings[bookingId].startTime, bookings[bookingId].endTime, bookingStatusToString(bookingStatus.CANCELLED), "Cancelled by user manually", block.timestamp);
    }

    // reject booking 
    function rejectBooking(bytes32 ipfsHash, uint256 bookingId) external isAdmin {
        require(bookings[bookingId].ipfsHash == ipfsHash, "ipfsHash doesn't match (bookings)");
        require(bookings[bookingId].bookingId == bookingId, "bookingId doesn't match (bookings)");

        bookings[bookingId].status = bookingStatus.REJECTED;
        emit bookingStatusUpdated(bookingId, ipfsHash, bookings[bookingId].sportFacility, bookings[bookingId].court, bookings[bookingId].startTime, bookings[bookingId].endTime, bookingStatusToString(bookingStatus.REJECTED), "Rejected by admin manually", block.timestamp);
    }

    // append booking note
    function appendBookingNote(bytes32 ipfsHash, uint256 bookingId, string memory note) external isAdmin {
        require(bookings[bookingId].ipfsHash == ipfsHash, "ipfsHash doesn't match (bookings)");
        require(bookings[bookingId].bookingId == bookingId, "bookingId doesn't match (bookings)");

        bookings[bookingId].note.push(note);
        emit bookingNoteAppended(bookingId, ipfsHash, bookings[bookingId].sportFacility, bookings[bookingId].court, bookings[bookingId].startTime, bookings[bookingId].endTime, bookingStatusToString(bookings[bookingId].status), note, block.timestamp);
    }

    // get booking status (all and selected)
    // admin
    function getBookingStatus_(uint256 bookingId) external view isAdmin returns(string memory) {
        require(bookings[bookingId].bookingId == bookingId, "bookingId doesn't match (bookings)");
        return bookingStatusToString(bookings[bookingId].status);
    }

    function getAllBookingStatus_() external view isAdmin returns(uint256[] memory, string[] memory) {
        // solidity doesn't support returning an array of structs with dynamic types (e.g. note[])
        require(bookings.length > 0, "empty array (bookings)");

        uint256[] memory bookingIds = new uint256[](bookings.length);
        string[] memory statuses = new string[](bookings.length);
        for(uint256 i=0; i<bookings.length; i++) {
            bookingIds[i] = bookings[i].bookingId;
            statuses[i] = bookingStatusToString(bookings[i].status);
        }
        return(bookingIds, statuses);
    }

    // user
    function getBookingStatus(uint256 bookingId) external view returns(string memory) {
        require(bookings[bookingId].bookingId == bookingId, "bookingId doesn't match (bookings)");
        require(bookings[bookingId].user == msg.sender, "Invalid access (bookings)");
        return bookingStatusToString(bookings[bookingId].status);
    }

    function getAllBookingStatus() external view returns(uint256[] memory, string[] memory){
        require(bookings.length > 0, "empty array (bookings)");

        uint256 count = 0;
        for (uint256 i = 0; i < bookings.length; i++) {
            if (bookings[i].user == msg.sender) count++;
        }

        uint256[] memory bookingIds = new uint256[](count);
        string[] memory statuses = new string[](count);
        uint256 index = 0;

        for (uint256 i = 0; i < bookings.length; i++) {
            if (bookings[i].user == msg.sender) {
                bookingIds[index] = bookings[i].bookingId;
                statuses[index] = bookingStatusToString(bookings[i].status);
                index++;
            }
        }
        return(bookingIds, statuses);
    }

    // universal
    struct timeSlot {
        uint256 startTime;
        uint256 endTime;
    }
    function getTimeSlots(string memory facilityName, string memory courtName) external view returns(timeSlot[] memory) {
        timeSlot[] memory timeSlots;

        if(bookings.length > 0) {
            for(uint256 i=0; i<bookings.length; i++) {
                if(keccak256(bytes(bookings[i].sportFacility)) == keccak256(bytes(facilityName)) && keccak256(bytes(bookings[i].court)) == keccak256(bytes(courtName))) {
                    timeSlots[i].startTime = bookings[i].startTime;
                    timeSlots[i].endTime = bookings[i].endTime;
                }
            }
        }

        return timeSlots; 
    }
}
