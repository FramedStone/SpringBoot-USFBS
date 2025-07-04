// SPDX-License-Identifier: MIT 
pragma solidity 0.8.19;

import "./SportFacility.sol";
import "./Management.sol"; 
import "./Strings.sol";

contract Booking is Management {
    // Variable & Modifier Initialization
    SportFacility immutable sfContract;
    enum status { APPROVED, REJECTED, COMPLETED, CANCELLED }

    struct timeSlot {
        uint256 startTime;
        uint256 endTime;
    }
    struct bookingTransaction {
        address owner;
        string ipfsHash;
        string fname;
        string cname;
        timeSlot time;
        status status;
    }
    bookingTransaction[] private bookings_;
    mapping(string ipfsHash => bookingTransaction) private bookings;

    // Events
    event bookingCreated(
        address indexed from,
        string ipfsHash,
        string fname,
        string cname,
        uint256 startTime,
        uint256 endTime,
        string status,
        uint256 timestamp
    );
    event bookingDeleted(
        address indexed from,
        string ipfsHash,
        uint256 timestamp
    );
    event bookingRejected(
        address indexed from,
        string ipfsHash,
        string reason,
        uint256 timestamp
    );
    event bookingCompleted(
        string ipfsHash,
        uint256 timestamp
    );
    event bookingCancelled(
        string ipfsHash,
        uint256 timestamp
    );

    // Helper Functions
    function statusToString(status s) internal pure returns(string memory sString) {
        if(s == status.APPROVED) return "approved";
        if(s == status.REJECTED) return "rejected";
        if(s == status.COMPLETED) return "completed";
        if(s == status.CANCELLED) return "cancelled";
        return "unknown"; 
    }

    function updateBooking_(
        string memory ipfsHash_,
        string memory ipfsHash,
        status nStatus 
    ) internal {
        require(bookings_.length > 0, "No bookings saved in blockchain");

        for(uint256 i=0; i<bookings_.length; i++) {
            if(keccak256(bytes(ipfsHash_)) == keccak256(bytes(bookings_[i].ipfsHash))) {
                bookings_[i].ipfsHash = ipfsHash;
                bookings_[i].status = nStatus;
                bookings[ipfsHash] = bookings_[i];
                delete bookings[ipfsHash_];
                return;
            }
        }
    }

    // function deleteBooking_(
    //     string memory ipfsHash
    // ) internal {
    //     require(keccak256(bytes(bookings[ipfsHash].ipfsHash)) == keccak256(bytes(ipfsHash)), "ipfsHash doesn't match in blockchain / booking not found");
    //     require(bookings_.length > 0, "No bookings saved in blockchain");

    //     for(uint256 i=0; i<bookings_.length; i++) {
    //         if(keccak256(bytes(ipfsHash)) == keccak256(bytes(bookings_[i].ipfsHash))) {
    //             bookings_[i] = bookings_[bookings_.length - 1];
    //             bookings_.pop();
    //         }
    //     }
    //     delete bookings[ipfsHash];

    //     emit bookingDeleted(msg.sender, ipfsHash, block.timestamp);
    // }

    // Main Functions
    constructor(address[] memory admins_, address sfAddress) Management(admins_) {
        sfContract = SportFacility(sfAddress);
    }

    function createBooking(
        address owner,
        string memory ipfsHash,
        string memory fname,
        string memory cname,
        timeSlot memory time
    ) external {
        try sfContract.getCourt(fname, cname) returns (
            string memory /*name*/,
            uint256 /*earliestTime*/,
            uint256 /*latestTime*/,
            SportFacility.status /*status_*/
        ) {
            // Court exists, continue
        } catch {
            revert("Sport Facility or Court does not exist");
        }
        require(time.endTime > time.startTime, "End time must be greater than Start time");
        uint256 duration = time.endTime - time.startTime;
        require(duration % 1 == 0, "Booking time must be 1 increment");

        status status_ = status.APPROVED;
        for(uint256 i=0; i<bookings_.length; i++) {
            bookingTransaction memory existing = bookings_[i];

            if(
                keccak256(bytes(existing.fname)) == keccak256(bytes(fname)) &&                 
                keccak256(bytes(existing.cname)) == keccak256(bytes(cname)) 
            ) {
                bool isOverlap = (
                    (time.startTime < existing.time.endTime) &&
                    (time.endTime > existing.time.startTime)
                );
                if(isOverlap && (existing.status == status.APPROVED)) {
                    status_ = status.REJECTED;
                    break;
                }
            } else {
                // revert("Sport Facility / Court can't be found");
            }
        }

        bookingTransaction memory nBooking = bookingTransaction(
            owner,
            ipfsHash,
            fname,
            cname,
            time,
            status_
        );

        bookings_.push(nBooking);
        bookings[ipfsHash] = nBooking;

        emit bookingCreated(
            owner, 
            ipfsHash, 
            fname, 
            cname, 
            time.startTime, 
            time.endTime, 
            statusToString(status_), 
            block.timestamp
        );
    }

    function getBooking_(
        string memory ipfsHash
    ) external isAdmin view returns(
        address owner,
        string memory ipfsHash_,
        string memory fname,
        string memory cname,
        uint256 startTime,
        uint256 endTime,
        uint8 status_
    ) {
        bookingTransaction memory booking = bookings[ipfsHash];
        owner = booking.owner;
        ipfsHash_ = booking.ipfsHash;
        fname = booking.fname;
        cname = booking.cname;
        startTime = booking.time.startTime;
        endTime = booking.time.endTime;
        status_ = uint8(booking.status);
    }

    function getAllBookings_() external isAdmin view returns(bookingTransaction[] memory bookingList) {
        require(bookings_.length > 0, "No bookings saved in blockchain");
        return bookings_; 
    }
    
    function getBooking(
        string memory ipfsHash
    ) external view returns(
        address owner,
        string memory ipfsHash_,
        string memory fname,
        string memory cname,
        uint256 startTime,
        uint256 endTime,
        uint8 status_
    ) {
        bookingTransaction memory booking = bookings[ipfsHash];
        owner = booking.owner;
        ipfsHash_ = booking.ipfsHash;
        fname = booking.fname;
        cname = booking.cname;
        startTime = booking.time.startTime;
        endTime = booking.time.endTime;
        status_ = uint8(booking.status);
    }

    function getAllBookings(address owner) external view returns(bookingTransaction[] memory bookingList) {
        uint256 count = 0;
        for (uint256 i = 0; i < bookings_.length; i++) {
            if (bookings_[i].owner == owner) {
                count++;
            }
        }

        bookingTransaction[] memory temp = new bookingTransaction[](count);
        uint256 idx = 0;
        for (uint256 i = 0; i < bookings_.length; i++) {
            if (bookings_[i].owner == owner) {
                temp[idx] = bookings_[i];
                idx++;
            }
        }
        return temp;
    }
    function getBookedTimeSlots(
        string memory fname,
        string memory cname
    ) external view returns(timeSlot[] memory timeSlots) {
        try sfContract.getCourt(fname, cname) returns (
            string memory /*name*/,
            uint256 /*earliestTime*/,
            uint256 /*latestTime*/,
            SportFacility.status /*status_*/
        ) {
            // Court exists, continue
        } catch {
            revert("Sport Facility or Court does not exist");
        }

        // Count how many approved bookings for this court
        uint256 count = 0;
        for (uint256 i = 0; i < bookings_.length; i++) {
            if (
                keccak256(bytes(bookings_[i].fname)) == keccak256(bytes(fname)) &&
                keccak256(bytes(bookings_[i].cname)) == keccak256(bytes(cname)) &&
                bookings_[i].status == status.APPROVED
            ) {
                count++;
            }
        }

        // Populate the array
        timeSlot[] memory slots = new timeSlot[](count);
        uint256 idx = 0;
        for (uint256 i = 0; i < bookings_.length; i++) {
            if (
                keccak256(bytes(bookings_[i].fname)) == keccak256(bytes(fname)) &&
                keccak256(bytes(bookings_[i].cname)) == keccak256(bytes(cname)) &&
                bookings_[i].status == status.APPROVED
            ) {
                slots[idx] = bookings_[i].time;
                idx++;
            }
        }
        return slots;
    }

    function completeBooking(
        string memory ipfsHash_,
        string memory ipfsHash
    ) external isAdmin {
        require(bookings[ipfsHash_].status == status.APPROVED, "Booking status not currently APPROVED");
        updateBooking_(ipfsHash_, ipfsHash, status.COMPLETED);
        emit bookingCompleted(ipfsHash, block.timestamp);
    }
    function rejectBooking(
        string memory ipfsHash_,
        string memory ipfsHash,
        string memory reason
    ) external isAdmin {
        require(bookings[ipfsHash_].status == status.APPROVED, "Booking status not currently APPROVED");
        updateBooking_(ipfsHash_, ipfsHash, status.REJECTED);
        emit bookingRejected(msg.sender, ipfsHash, reason, block.timestamp);
    }
    function cancelBooking(
        string memory ipfsHash_,
        string memory ipfsHash
    ) external {
        updateBooking_(ipfsHash_, ipfsHash, status.CANCELLED);
        emit bookingCancelled(ipfsHash, block.timestamp); 
    }

    function updateIPFSHash_(
        string memory ipfsHash_,
        string memory ipfsHash
    ) external isAdmin {
        require(bookings_.length > 0, "No bookings saved in blockchain");

        for(uint256 i=0; i<bookings_.length; i++) {
            if(keccak256(bytes(ipfsHash_)) == keccak256(bytes(bookings_[i].ipfsHash))) {
                bookings_[i].ipfsHash = ipfsHash;
                bookings[ipfsHash] = bookings_[i];
                delete bookings[ipfsHash_];
                return;
            }
        }
    }
    function updateIPFSHash(
        string memory ipfsHash_,
        string memory ipfsHash
    ) external {
        require(bookings_.length > 0, "No bookings saved in blockchain");

        for(uint256 i=0; i<bookings_.length; i++) {
            if(keccak256(bytes(ipfsHash_)) == keccak256(bytes(bookings_[i].ipfsHash))) {
                bookings_[i].ipfsHash = ipfsHash;
                bookings[ipfsHash] = bookings_[i];
                delete bookings[ipfsHash_];
                return;
            }
        }
    }
}
