// SPDX-License-Identifier: MIT 
pragma solidity 0.8.19;

contract Management {
    // Variable & Modifier Initialization
    struct Announcement {
        // string message;
        string   ipfsHash;
        uint256 startTime;
        uint256 endTime;
    }
   mapping(string => Announcement)  announcements;

    mapping(address => bool) admins;
    mapping(address => bool) internal users;

    modifier isAdmin {
        require(admins[msg.sender] == true, "Access denied");
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
        uint256 timestamp
    );
    event userDeleted(
        address indexed from,
        address indexed user,
        string note,
        uint256 timestamp
    );

    event announcementAdded(
        address indexed from,
        string ipfsHash,
        uint256 startTime,
        uint256 endTime,
        uint256 timestamp
    );
    event announcementIpfsHashModified(
        address indexed from,
        string ipfsHash_,
        string ipfsHash,
        uint256 timestamp
    );
    event announcementTimeModified(
        address indexed from,
        string ipfsHash,
        uint256 startTime_,
        uint256 endTime_,
        uint256 startTime,
        uint256 endTime,
        uint256 timestamp
    );
    event announcementDeleted(
        address indexed from,
        string ipfsHash,
        uint256 timestamp
    );
    event announcementRequested(
        address indexed from,
        string ipfsHash,
        uint256 timestamp
    );

    // Main functions
    constructor(address admin) {
       admins[admin] = true; 
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
        string memory ipfsHash,
        uint256 startTime,
        uint256 endTime
    ) external isAdmin {
       require(bytes(announcements[ipfsHash].ipfsHash).length == 0, "Duplicated ipfsHash");
        require(startTime != 0, "startTime not provided");
        require(endTime   != 0, "endTime not provided");

        announcements[ipfsHash] = Announcement(ipfsHash, startTime, endTime);
        emit announcementAdded(msg.sender, ipfsHash, startTime, endTime, block.timestamp);
    }

    function updateAnnouncementIpfsHash(
        string memory ipfsHash_,
        string memory ipfsHash
    ) external isAdmin {
       require(bytes(announcements[ipfsHash_].ipfsHash).length != 0, "Announcement not found");
       require(bytes(ipfsHash).length != 0, "ipfsHash not provided");

        announcements[ipfsHash] = Announcement(ipfsHash, announcements[ipfsHash_].startTime, announcements[ipfsHash_].endTime);
        delete announcements[ipfsHash_];

        emit announcementIpfsHashModified(msg.sender, ipfsHash_, ipfsHash, block.timestamp);
    }

    function updateAnnouncementTime(
        string memory ipfsHash,
        uint256 startTime,
        uint256 endTime
    ) external isAdmin {
       require(bytes(announcements[ipfsHash].ipfsHash).length != 0, "Announcement not found");
        require(startTime != 0, "startTime not provided");
        require(endTime   != 0, "endTime not provided");

        uint256 oldStart = announcements[ipfsHash].startTime;
        uint256 oldEnd   = announcements[ipfsHash].endTime;

        announcements[ipfsHash].startTime = startTime;
        announcements[ipfsHash].endTime   = endTime;

        emit announcementTimeModified(msg.sender, ipfsHash, oldStart, oldEnd, startTime, endTime, block.timestamp);
    }

    function deleteAnnouncement(
        string memory ipfsHash
    ) external isAdmin {
       require(bytes(announcements[ipfsHash].ipfsHash).length != 0, "Announcement not found");

        delete announcements[ipfsHash];
        emit announcementDeleted(msg.sender, ipfsHash, block.timestamp);
    }

    function getAnnouncement(
        string memory ipfsHash
    ) external returns(Announcement memory) {
       require(bytes(announcements[ipfsHash].ipfsHash).length != 0, "Announcement not found");

        Announcement memory announcement = announcements[ipfsHash];
        emit announcementRequested(msg.sender, announcement.ipfsHash, block.timestamp);
        return announcement;
    }
}