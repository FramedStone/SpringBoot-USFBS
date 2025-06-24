// SPDX-License-Identifier: MIT 
pragma solidity 0.8.19;

import "./SportFacility.sol";
import "./Management.sol"; 
import "./Strings.sol";

contract Booking is Management {
    // Variable & Modifier Initialization
    SportFacility immutable sfContract;
    enum status { APPROVED, REJECTED, CANCELLED }

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
        if(s == status.CANCELLED) return "cancelled";
        return "unknown"; 
    }

    function updateBooking_(
        string memory ipfsHash_,
        string memory ipfsHash,
        status nStatus 
    ) internal {
        require(keccak256(bytes(bookings[ipfsHash].ipfsHash)) == keccak256(bytes(ipfsHash)), "ipfsHash doesn't match in blockchain / booking not found");
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

    function deleteBooking_(
        string memory ipfsHash
    ) internal {
        require(keccak256(bytes(bookings[ipfsHash].ipfsHash)) == keccak256(bytes(ipfsHash)), "ipfsHash doesn't match in blockchain / booking not found");
        require(bookings_.length > 0, "No bookings saved in blockchain");

        for(uint256 i=0; i<bookings_.length; i++) {
            if(keccak256(bytes(ipfsHash)) == keccak256(bytes(bookings_[i].ipfsHash))) {
                bookings_[i] = bookings_[bookings_.length - 1];
                bookings_.pop();
            }
        }
        delete bookings[ipfsHash];

        emit bookingDeleted(msg.sender, ipfsHash, block.timestamp);
    }

    // Main Functions
    constructor(address[] memory admins_, address sfAddress) Management(admins_) {
        sfContract = SportFacility(sfAddress);
    }

    function createBooking(
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
                revert("Sport Facility / Court can't be found");
            }
        }

        bookingTransaction memory nBooking = bookingTransaction(
            msg.sender,
            ipfsHash,
            fname,
            cname,
            time,
            status_
        );

        bookings_.push(nBooking);
        bookings[ipfsHash] = nBooking;

        emit bookingCreated(
            msg.sender, 
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
    ) external isAdmin view returns(bookingTransaction memory booking) {
        require(keccak256(bytes(bookings[ipfsHash].ipfsHash)) == keccak256(bytes(ipfsHash)), "ipfsHash doesn't match in blockchain / booking not found");
        return bookings[ipfsHash];
    }
    function getAllBookings_() external isAdmin view returns(bookingTransaction[] memory bookingList) {
        require(bookings_.length > 0, "No bookings saved in blockchain");
        return bookings_; 
    }
    
    function getBooking(
        string memory ipfsHash
    ) external view returns(bookingTransaction memory booking) {
        require(keccak256(bytes(bookings[ipfsHash].ipfsHash)) == keccak256(bytes(ipfsHash)), "ipfsHash doesn't match in blockchain / booking not found");
        require(bookings[ipfsHash].owner == msg.sender, "Access Denied (not booking owner)");
        return bookings[ipfsHash];
    }
    function getAllBookings() external view returns(bookingTransaction[] memory bookingList) {
        uint256 count = 0;
        for (uint256 i = 0; i < bookings_.length; i++) {
            if (bookings_[i].owner == msg.sender) {
                count++;
            }
        }

        bookingTransaction[] memory temp = new bookingTransaction[](count);
        uint256 idx = 0;
        for (uint256 i = 0; i < bookings_.length; i++) {
            if (bookings_[i].owner == msg.sender) {
                temp[idx] = bookings_[i];
                idx++;
            }
        }
        return temp;
    }

    function completeBooking(
        string memory ipfsHash
    ) external isAdmin {
        deleteBooking_(ipfsHash);
        emit bookingCompleted(ipfsHash, block.timestamp);
    }
    function rejectBooking(
        string memory ipfsHash_,
        string memory ipfsHash,
        string memory reason
    ) external isAdmin {
        deleteBooking_(ipfsHash_);
        emit bookingRejected(msg.sender, ipfsHash, reason, block.timestamp);
    }
    function cancelBooking(
        string memory ipfsHash_,
        string memory ipfsHash
    ) external {
        require(bookings[ipfsHash_].owner == msg.sender, "Access Denied (not booking owner)");
        deleteBooking_(ipfsHash_);
        emit bookingCancelled(ipfsHash, block.timestamp); 
    }
}
