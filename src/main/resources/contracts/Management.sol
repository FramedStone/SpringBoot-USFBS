// SPDX-License-Identifier: MIT 
pragma solidity 0.8.19;

contract Management {
    // Variable & Modifier Initialization
    struct Announcement {
        // string message;
        bytes32 ipfsHash;
        uint256 startTime;
        uint256 endTime;
    }
    // Annoucement[] private annoucements;
    mapping(bytes32 => Announcement) announcements;

    address private immutable admin_;
    mapping(address => bool) internal users;

    modifier isAdmin {
        require(msg.sender == admin_, "Access denied");
        _;
    } 
    modifier isUser {
        require(users[msg.sender] == true, "Access denied");
        _;
    }

    // Events
    event userAdded(
        address indexed from,
        address indexed user,
        uint256 time
    );
    event userDeleted(
        address indexed from,
        address indexed user,
        string note,
        uint256 time
    );

    event announcementAdded(
        address indexed from,
        bytes32 ipfsHash,
        uint256 startTime,
        uint256 endTime,
        uint256 time
    );
    event announcementIpfsHashModified(
        address indexed from,
        bytes32 ipfsHash_,
        bytes32 ipfsHash,
        uint256 time
    );
    event announcementTimeModified(
        address indexed from,
        bytes32 ipfsHash,
        uint256 startTime_,
        uint256 endTime_,
        uint256 startTime,
        uint256 endTime,
        uint256 time
    );
    event announcementDeleted(
        address indexed from,
        bytes32 ipfsHash,
        uint256 time
    );
    event announcementRequested(
        address indexed from,
        bytes32 ipfsHash,
        uint256 time
    );

    // Main functions
    constructor(address admin) {
       admin_ = admin; 
    }
    function addUser(address user) public isAdmin {
        users[user] = true;
        emit userAdded(
            msg.sender,
            user,
            block.timestamp
        );
    }
    function deleteUser(address user) external isAdmin {
        require(users[user] == true, "User not found (system)");
        require(user != msg.sender, "Access denied (system)");

        delete users[user];
        emit userDeleted(
            msg.sender,
            user,
            "User removed (admin)",
            block.timestamp
        );
    }

    // Announcement CRUD
    function addAnnouncement(
        bytes32 ipfsHash,
        uint256 startTime,
        uint256 endTime
    ) external isAdmin returns(bool isSuccess) {
        require(announcements[ipfsHash].ipfsHash == 0x00, "Duplicated ipfsHash"); 
        require(startTime != 0, "startTime not provided");
        require(endTime != 0, "endTime not provided");

        announcements[ipfsHash] = Announcement(ipfsHash, startTime, endTime);
        emit announcementAdded(msg.sender, ipfsHash, startTime, endTime, block.timestamp);
        return true;
    }
    function updateAnnouncementIpfsHash(
        bytes32 ipfsHash_,
        bytes32 ipfsHash
    ) external isAdmin returns(bool isSuccess) {
        require(announcements[ipfsHash_].ipfsHash != 0x00, "Announcement ipfsHash not found");
        require(ipfsHash != 0x00, "ipfsHash not provided");

        announcements[ipfsHash] = Announcement(ipfsHash, announcements[ipfsHash_].startTime, announcements[ipfsHash_].endTime);
        delete announcements[ipfsHash_];
        emit announcementIpfsHashModified(msg.sender, ipfsHash_, ipfsHash, block.timestamp);
        return true;
    }
    function updateAnnouncementTime(
        bytes32 ipfsHash,
        uint256 startTime,
        uint256 endTime
    ) external isAdmin returns(bool isSuccess) {
        require(announcements[ipfsHash].ipfsHash != 0x00, "Announcement ipfsHash not found");
        require(startTime != 0, "startTime not provided");
        require(endTime != 0, "endTime not provided");

        uint256 startTime_ = announcements[ipfsHash].startTime;
        uint256 endTime_ = announcements[ipfsHash].endTime;

        announcements[ipfsHash].startTime = startTime;
        announcements[ipfsHash].endTime = endTime;
        emit announcementTimeModified(msg.sender, ipfsHash, startTime_, endTime_, startTime, endTime, block.timestamp);
        return true;
    }
    function deleteAnnouncement(bytes32 ipfsHash) external isAdmin returns(bool isSuccess) {
        require(announcements[ipfsHash].ipfsHash != 0x00, "Announcement ipfsHash not found");

        delete announcements[ipfsHash];
        emit announcementDeleted(msg.sender, ipfsHash, block.timestamp);
        return true;
    }

    // Announcement Getters
    function getAnnouncement(bytes32 ipfsHash) external returns(Announcement memory announcement_) {
        require(announcements[ipfsHash].ipfsHash != 0x00, "Announcement not found");

        Announcement memory announcement = announcements[ipfsHash];
        emit announcementRequested(msg.sender, announcement.ipfsHash, block.timestamp);
        return(announcement);
    }
}