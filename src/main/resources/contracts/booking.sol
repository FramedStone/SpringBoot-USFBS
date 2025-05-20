// SPDX-License-Identifier: MIT 
pragma solidity 0.8.19;

import "./SportFacility.sol";
import "./Management.sol"; 

contract Booking is Management {
    // Variable & Modifier Initialization
    address private immutable admin_;
    SportFacility immutable sfContract;
    enum status { APPROVED, PENDING, REJECTED, COMPLETED, CANCELLED }

    struct timeSlot {
        uint256 startTime;
        uint256 endTime;
    }
    struct bookingTransaction {
        uint256 bookingId;
        bytes32 ipfsHash;
        string facilityName;
        string courtName;
        string note;
        timeSlot time;
        status status;
    }
    bookingTransaction[] private bookings;

    // Events
    event bookingCreated( 
        address indexed from,
        uint256 bookingId,
        bytes32 ipfsHash,
        string facilityName,
        string courtName,
        string note,
        string timeSlot,
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
    event bookingDeleted(
        address indexed from,
        uint256 bookingId,
        bytes32 ipfsHash,
        string status,
        string note,
        uint256 timestamp
    );
    event bookingRequested(
        address indexed from,
        uint256 bookingId,
        bytes32 ipfsHash,
        uint256 timestamp
    );
    event timeSlotRequested(
        address indexed from,
        string facilityName,
        string courtName,
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

    function freeUpStorage() internal {
        for(uint256 i=0; i<bookings.length; i++) {
            bookingTransaction storage b = bookings[i];

            if(bookings[i].status == status.APPROVED) {
                if(block.timestamp >= b.time.endTime) {
                    b.status = status.COMPLETED;
                    emit bookingUpdated(msg.sender, b.bookingId, statusToString(status.APPROVED), statusToString(b.status), "updated by system (booking completed)", block.timestamp);
                }
            } else if(bookings[i].status == status.CANCELLED) {
                emit bookingDeleted(msg.sender, b.bookingId, b.ipfsHash, statusToString(b.status), "deleted by system", block.timestamp);
                delete bookings[i];
            }
        }
    }

    function isAvailable_(
        string memory fname,
        string memory cname,
        timeSlot memory time
    ) internal isAdmin returns(bool result) {
        require(keccak256(bytes(fname)) != keccak256(bytes("")), "Facility name not provided");
        require(keccak256(bytes(cname)) != keccak256(bytes("")), "Court name not provided");
        require(time.startTime != 0 && time.endTime != 0, "Start or End time not provided");

        (uint256 startTime, uint256 endTime) = sfContract.getAvailableTimeRange_(fname, cname);
        require(time.startTime >= startTime && time.endTime <= endTime, "Booking request not within court time range");

        for(uint256 i=0; i<bookings.length; i++) {
            if(
                keccak256(bytes(bookings[i].facilityName)) == keccak256(bytes(fname)) &&
                keccak256(bytes(bookings[i].courtName)) == keccak256(bytes(cname)) &&
                bookings[i].time.startTime >= time.startTime &&
                bookings[i].time.endTime <= time.endTime
            ) {
                if(bookings[i].status == status.APPROVED) {
                    return false;
                } else if(bookings[i].status == status.PENDING) {
                    return false;
                } else {
                    return true;
                }
            } 
        }
    }
    function isAvailable(
        string memory fname,
        string memory cname,
        timeSlot memory time
    ) internal isUser returns(bool result) {
        require(keccak256(bytes(fname)) != keccak256(bytes("")), "Facility name not provided");
        require(keccak256(bytes(cname)) != keccak256(bytes("")), "Court name not provided");
        require(time.startTime != 0 && time.endTime != 0, "Start or End time not provided");

        (uint256 startTime, uint256 endTime) = sfContract.getAvailableTimeRange(fname, cname);
        require(time.startTime >= startTime && time.endTime <= endTime, "Booking request not within court time range");

        for(uint256 i=0; i<bookings.length; i++) {
            if(
                keccak256(bytes(bookings[i].facilityName)) == keccak256(bytes(fname)) &&
                keccak256(bytes(bookings[i].courtName)) == keccak256(bytes(cname)) &&
                bookings[i].time.startTime >= time.startTime &&
                bookings[i].time.endTime <= time.endTime
            ) {
                if(bookings[i].status == status.APPROVED) {
                    return false;
                } else if(bookings[i].status == status.PENDING) {
                    return false;
                } else {
                    return true;
                }
            } 
        }
    }

    // Main Functions
    constructor(address admin, address sfAddress) Management(admin) {
       admin_ = admin; 
       sfContract = SportFacility(sfAddress);
    }

    // universal
    function getBookedtimeSlots(
        string memory fname,
        string memory cname
    ) external returns(timeSlot[] memory timeSlots_) {
        require(keccak256(bytes(fname)) != keccak256(bytes("")), "Facility name not provided");
        require(keccak256(bytes(cname)) != keccak256(bytes("")), "Court name not provided");

        timeSlot[] memory timeSlots;
        for(uint256 i=0; i<bookings.length; i++) {
            if(
                keccak256(bytes(bookings[i].facilityName)) == keccak256(bytes(fname)) &&
                keccak256(bytes(bookings[i].courtName)) == keccak256(bytes(cname)) &&
                (bookings[i].status == status.APPROVED || bookings[i].status == status.PENDING)
            ) {
                timeSlots[i] = bookings[i].time;
                emit timeSlotRequested(msg.sender, fname, cname, block.timestamp);
            }
        }
        return timeSlots;
    }

    function updateIPFSHash
    // admin
    // CRUD

    // Getters

    // user
    // CRUD
    // Getters
}
