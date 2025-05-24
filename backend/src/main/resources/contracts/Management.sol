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

    mapping(address => bool) internal admins;
    mapping(address => bool) internal users;
    mapping(address => bool) private bannedUsers;

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
    event userBanned(
        address indexed from,
        address indexed user,
        string note,
        uint256 timestamp
    );
    event userUnbanned(
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
        require(user != address(0), "User address not provided");

        users[user] = true;
        emit userAdded(
            msg.sender,
            user,
            block.timestamp
        );
    }
    function banUser(address user) external isAdmin {
        require(user != address(0), "User address not provided");
        require(users[user] == true, "User not found (system)");

        bannedUsers[user] = true;
        delete users[user];
        emit userBanned(
            msg.sender,
            user,
            "User banned by admin",
            block.timestamp
        );
    }
    function unbanUser(address user) external isAdmin {
        require(user != address(0), "User address not provided");

        bannedUsers[user] = false;
        emit userUnbanned(
            msg.sender,
            user,
            "User unbanned by admin",
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