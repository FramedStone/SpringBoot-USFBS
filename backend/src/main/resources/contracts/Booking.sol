// SPDX-License-Identifier: MIT 
pragma solidity 0.8.19;

import "./SportFacility.sol";
import "./Management.sol"; 
import "./Strings.sol";

contract Booking is Management {
    // Variable & Modifier Initialization
    SportFacility immutable sfContract;
    enum status { APPROVED, PENDING, REJECTED, COMPLETED, CANCELLED }

    struct timeSlot {
        uint256 startTime;
        uint256 endTime;
    }
    struct bookingTransaction {
        address owner;
        uint256 bookingId;
        string ipfsHash;
        string facilityName;
        string courtName;
        string note;
        timeSlot time;
        status status;
    }
    bookingTransaction[] private bookings;
    mapping(address owner => bookingTransaction) private bookingOwner;

    // Events
    event bookingCreated(
        address indexed from,
        uint256 bookingId,
        string ipfsHash,
        string facilityName,
        string courtName,
        string note,
        string startTime,
        string endTime,
        string status,
        uint256 timestamp
    );
    event bookingUpdated(
        address indexed from,
        uint256 bookingId,
        string oldData,
        string newData,
        string note,
        uint256 timestamp
    );

    // Helper Functions
    function statusToString(status s) internal pure returns(string memory sString) {
        if(s == status.APPROVED) return "approved";
        if(s == status.PENDING) return "pending";
        if(s == status.REJECTED) return "rejected";
        if(s == status.COMPLETED) return "completed";
        if(s == status.CANCELLED) return "cancelled";
        return "unknown"; 
    }

    function updateAllBookingStatus_() external {
        for(uint256 i=0; i<bookings.length; i++) {
            bookingTransaction storage b = bookings[i];

            if(bookings[i].status == status.APPROVED) {
                if(block.timestamp >= b.time.endTime) {
                    b.status = status.COMPLETED;
                    emit bookingUpdated(msg.sender, b.bookingId, statusToString(status.APPROVED), statusToString(b.status), "updated by system (booking completed)", block.timestamp);
                }
            }
        }
    }

    function isAvailable_(
        string memory fname,
        string memory cname,
        timeSlot memory time
    ) internal isAdmin view returns(bool result) {
        require(bytes(fname).length > 0, "Facility name not provided");
        require(bytes(cname).length > 0, "Court name not provided");
        require(time.startTime != 0 && time.endTime != 0, "Start or End time not provided");

        (uint256 dailyStart, uint256 dailyEnd) = sfContract.getAvailableTimeRange_(fname, cname, msg.sender);
        
        // Convert booking times to time-of-day (seconds since midnight)
        uint256 bookingStartTime = time.startTime % 86400;
        uint256 bookingEndTime = time.endTime % 86400;
        
        require(bookingStartTime >= dailyStart && bookingEndTime <= dailyEnd, 
                "Booking request not within court daily operating hours");

        for(uint256 i=0; i<bookings.length; i++) {
            if(
                keccak256(bytes(bookings[i].facilityName)) == keccak256(bytes(fname)) &&
                keccak256(bytes(bookings[i].courtName))  == keccak256(bytes(cname)) &&
                bookings[i].time.startTime             >= time.startTime &&
                bookings[i].time.endTime               <= time.endTime
            ) {
                if(bookings[i].status == status.APPROVED) {
                    return false;
                } else {
                    return true;
                }
            } 
        }
        return true;
    }

    function isAvailable(
        string memory fname,
        string memory cname,
        timeSlot memory time
    ) internal isUser view returns(bool result) {
        require(bytes(fname).length > 0, "Facility name not provided");
        require(bytes(cname).length > 0, "Court name not provided");
        require(time.startTime != 0 && time.endTime != 0, "Start or End time not provided");

        (uint256 dailyStart, uint256 dailyEnd) = sfContract.getAvailableTimeRange(fname, cname, msg.sender);

        // Convert booking times to time-of-day (seconds since midnight)
        uint256 bookingStartTime = time.startTime % 86400;
        uint256 bookingEndTime = time.endTime % 86400;
        
        require(bookingStartTime >= dailyStart && bookingEndTime <= dailyEnd, 
                "Booking request not within court daily operating hours");

        for(uint256 i=0; i<bookings.length; i++) {
            if(
                keccak256(bytes(bookings[i].facilityName)) == keccak256(bytes(fname)) &&
                keccak256(bytes(bookings[i].courtName))  == keccak256(bytes(cname)) &&
                bookings[i].time.startTime             >= time.startTime &&
                bookings[i].time.endTime               <= time.endTime
            ) {
                if(bookings[i].status == status.APPROVED) {
                    return false;
                } else {
                    return true;
                }
            } 
        }
        return true;
    }

    function updateIPFSHash_(
        uint256 bookingId_,
        string memory ipfsHash_
    ) internal isAdmin {
        string memory oldData = bookings[bookingId_].ipfsHash;
        bookings[bookingId_].ipfsHash = ipfsHash_;
        string memory newData = ipfsHash_;

        emit bookingUpdated(msg.sender, bookingId_, oldData, newData, "IPFS Hash updated by admin", block.timestamp);
    }
    function updateIPFSHash(
        uint256 bookingId_,
        string memory ipfsHash_
    ) internal isUser {
        string memory oldData = bookings[bookingId_].ipfsHash;
        bookings[bookingId_].ipfsHash = ipfsHash_;
        string memory newData = ipfsHash_;

        emit bookingUpdated(msg.sender, bookingId_, oldData, newData, "IPFS Hash updated by user", block.timestamp);
    }

    // Main Functions
    constructor(address[] memory admins_, address sfAddress) Management(admins_) {
        sfContract = SportFacility(sfAddress);
    }

    // universal
    function getBookedtimeSlots(
        string memory fname,
        string memory cname
    ) external view returns(timeSlot[] memory timeSlots_) {
        require(users[msg.sender] == true || admins[msg.sender] == true, "Access denied: Must be user or admin");
        require(bytes(fname).length > 0, "Facility name not provided");
        require(bytes(cname).length > 0, "Court name not provided");

        // First pass: count matching active bookings
        uint256 activeBookingCount = 0;
        for(uint256 i = 0; i < bookings.length; i++) {
            if(
                keccak256(bytes(bookings[i].facilityName)) == keccak256(bytes(fname)) &&
                keccak256(bytes(bookings[i].courtName)) == keccak256(bytes(cname)) &&
                (bookings[i].status == status.APPROVED || bookings[i].status == status.PENDING)
            ) {
                activeBookingCount++;
            }
        }

        require(activeBookingCount > 0, "No active bookings found for this court");

        // Second pass: populate correctly sized array
        timeSlot[] memory timeSlots = new timeSlot[](activeBookingCount);
        uint256 arrayIndex = 0;
        
        for(uint256 i = 0; i < bookings.length; i++) {
            if(
                keccak256(bytes(bookings[i].facilityName)) == keccak256(bytes(fname)) &&
                keccak256(bytes(bookings[i].courtName)) == keccak256(bytes(cname)) &&
                (bookings[i].status == status.APPROVED || bookings[i].status == status.PENDING)
            ) {
                timeSlots[arrayIndex] = bookings[i].time;
                arrayIndex++;
            }
        }
        
        return timeSlots;
    }

    // admin
    // CRUD
    function createBooking_(
        string memory ipfsHash,
        string memory fname,
        string memory cname,
        string memory note,
        timeSlot memory time
    ) external isAdmin returns(uint256 bookingId_){
        require(bytes(fname).length > 0, "Facility name not provided");
        require(bytes(cname).length > 0, "Court name not provided");
        require(time.startTime != 0 && time.endTime != 0, "Start or End time not provided");
        uint256 duration = time.endTime - time.startTime;
        require(duration % 1 hours == 0, "Duration must be in 1 hour increments");

        uint256 bookingId = bookings.length;

        // look for any empty index in bookings
        for(uint256 i=0; i<bookings.length; i++) {
            if(bookings[i].bookingId == 0 && bytes(bookings[i].ipfsHash).length == 0) {
                bookingId = i;
            }
        }

        bookingTransaction memory b = bookingTransaction(
            msg.sender,
            bookingId,
            ipfsHash,
            fname,
            cname,
            note,
            time,
            status.PENDING
        );
        bookings.push(b);
        bookingOwner[msg.sender] = b;

        bookings[bookingId].status = status.PENDING;
        emit bookingCreated(
            msg.sender,
            bookingId,
            ipfsHash,
            fname,
            cname,
            "Booking created by admin",
            Strings.uintTo24Hour(time.startTime),
            Strings.uintTo24Hour(time.endTime),
            statusToString(status.PENDING),
            block.timestamp
        );

        if(isAvailable_(fname, cname, time)) {
            bookings[bookingId].status = status.APPROVED;
            emit bookingUpdated(
                msg.sender,
                bookingId,
                statusToString(status.PENDING),
                statusToString(status.APPROVED),
                "Booking approved by system",
                block.timestamp
            );
        } else if(!isAvailable_(fname, cname, time)) {
            bookings[bookingId].status = status.REJECTED;
            emit bookingUpdated(
                msg.sender,
                bookingId,
                statusToString(status.PENDING),
                statusToString(status.REJECTED),
                "Booking rejected by system due to clashing",
                block.timestamp
            );
        }
        return(bookingId);
    }

    function attachBookingNote(
        uint256 bookingId,
        string memory note
    ) external isAdmin {
        require(bookingId != 0, "Booking not found");
        require(bytes(note).length != 0, "note not provided");

        bookingTransaction storage b = bookings[bookingId];
        b.note = note;
        emit bookingUpdated(msg.sender, bookingId, "", note, "Note attached by admin", block.timestamp);
    }

    function rejectBooking(
        uint256 bookingId,
        string memory reason
    ) external isAdmin returns(string memory reason_) {
        require(bookingId != 0, "Booking not found");

        bookingTransaction storage b = bookings[bookingId];
        emit bookingUpdated(
            msg.sender,
            bookingId,
            statusToString(b.status),
            statusToString(status.REJECTED),
            string.concat("Booking rejected by admin: ", reason),
            block.timestamp
        );
        b.status = status.REJECTED;

        return reason;
    }

    // Getters
    function getBooking_(uint256 bookingId) external isAdmin view returns(bookingTransaction memory booking) {
        require(bookings.length > 0, "No bookings exist");
        require(bookingId < bookings.length, "Booking ID out of range");
        require(bookings[bookingId].bookingId != 0 || bytes(bookings[bookingId].ipfsHash).length > 0, "Booking not found");
        require(bookings[bookingId].owner == msg.sender, "Not booking owner");
        
        return bookings[bookingId];
    }
    function getAllBookings_() external isAdmin view returns(bookingTransaction[] memory booking) {
        require(bookings.length > 0, "Empty bookings saved in blockchain");

        return bookings;
    }

    // user
    // CRUD
    function createBooking(
        string memory ipfsHash,
        string memory fname,
        string memory cname,
        string memory note,
        timeSlot memory time
    ) external isUser returns(uint256 bookingId_){
        require(bytes(fname).length > 0, "Facility name not provided");
        require(bytes(cname).length > 0, "Court name not provided");
        require(time.startTime != 0 && time.endTime != 0, "Start or End time not provided");
        uint256 duration = time.endTime - time.startTime;
        require(duration == 1 hours || duration == 2 hours, "Booking must be exactly 1 hour or 2 hours only");

        uint256 bookingId = bookings.length;

        // look for any empty index in bookings
        for(uint256 i=0; i<bookings.length; i++) {
            if(bookings[i].bookingId == 0 && bytes(bookings[i].ipfsHash).length == 0) {
                bookingId = i;
            }
        }

        bookingTransaction memory b = bookingTransaction(
            msg.sender,
            bookingId,
            ipfsHash,
            fname,
            cname,
            note,
            time,
            status.PENDING
        );
        bookings.push(b);
        bookingOwner[msg.sender] = b;

        bookings[bookingId].status = status.PENDING;
        emit bookingCreated(
            msg.sender,
            bookingId,
            ipfsHash,
            fname,
            cname,
            "Booking created by admin",
            Strings.uintTo24Hour(time.startTime),
            Strings.uintTo24Hour(time.endTime),
            statusToString(status.PENDING),
            block.timestamp
        );

        if(isAvailable(fname, cname, time)) {
            bookings[bookingId].status = status.APPROVED;
            emit bookingUpdated(
                msg.sender,
                bookingId,
                statusToString(status.PENDING),
                statusToString(status.APPROVED),
                "Booking approved by system",
                block.timestamp
            );
        } else if(!isAvailable(fname, cname, time)) {
            bookings[bookingId].status = status.REJECTED;
            emit bookingUpdated(
                msg.sender,
                bookingId,
                statusToString(status.PENDING),
                statusToString(status.REJECTED),
                "Booking rejected by system due to clashing",
                block.timestamp
            );
        }
        return(bookingId);
    }

    function cancelBooking(
        uint256 bookingId
    ) external isUser {
        require(bookingId !=0, "BookingId not provided");
        require(bookings[bookingId].owner == msg.sender, "Not booking owner");

        bookingTransaction storage b = bookings[bookingId];
        string memory oldData = statusToString(b.status);
        string memory newData = statusToString(status.CANCELLED);
        emit bookingUpdated(msg.sender, bookingId, oldData, newData, "Booking cancelled by user", block.timestamp);
    }

    // Getters
    function getBooking(uint256 bookingId) external isUser view returns(bookingTransaction memory booking) {
        require(bookings.length > 0, "No bookings exist");
        require(bookingId < bookings.length, "Booking ID out of range");
        require(bookings[bookingId].bookingId != 0 || bytes(bookings[bookingId].ipfsHash).length > 0, "Booking not found");
        require(bookings[bookingId].owner == msg.sender, "Not booking owner");

        return bookings[bookingId];
    }
    function getAllBookings() external isUser view returns(bookingTransaction[] memory booking) {
        require(bookings.length > 0, "Empty bookings found in blockchain");

        bookingTransaction[] memory bookings_;
        for(uint256 i=0; i<bookings.length; i++) {
            if(bookings[i].owner == msg.sender) {
                bookings_[i] = bookings[i];
            }
        }
        return bookings_;
    }
}
