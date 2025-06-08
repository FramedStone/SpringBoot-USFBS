// SPDX-License-Identifier: MIT 
pragma solidity 0.8.19;

contract Management {
    // Variable & Modifier Initialization
    struct Announcement {
        // string message;
        string  ipfsHash;
        uint256 startTime;
        uint256 endTime;
    }
    Announcement[] private announcements_;
    mapping(string => Announcement) private announcements;

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
    constructor(address[] memory admins_) {
        for(uint256 i=0; i<admins_.length; i++) {
            admins[admins_[i]] = true; 
        }
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

    // Getters
    function getUser(address user) external view isAdmin returns(bool isRegistered) {
        return users[user];
    }
    function getBannedUser(address user) external view isAdmin returns(bool isBanned) {
        return bannedUsers[user];
    }

    // Announcement CRUD
    function addAnnouncement(
        string memory ipfsHash,
        uint256 startTime,
        uint256 endTime
    ) external isAdmin {
        require(startTime != 0, "startTime not provided");
        require(endTime   != 0, "endTime not provided");

        // Check for duplicated ipfsHash
        for (uint256 i = 0; i < announcements_.length; i++) {
            if (keccak256(bytes(announcements_[i].ipfsHash)) == keccak256(bytes(ipfsHash))) {
                revert("duplicated ipfsHash");
            }
        }

        uint256 aId = announcements_.length;

        // look for any empty index in announcements_ 
        for(uint256 i=0; i<announcements_.length; i++) {
            if(bytes(announcements_[i].ipfsHash).length == 0) {
                aId = i;
            }
        }

        if(aId != announcements_.length) {
            announcements_[aId] = (Announcement(ipfsHash, startTime, endTime));
        } else {
            announcements_.push(Announcement(ipfsHash, startTime, endTime));
        }
        emit announcementAdded(msg.sender, ipfsHash, startTime, endTime, block.timestamp);
    }

    function updateAnnouncementIpfsHash(
        string memory ipfsHash_,
        string memory ipfsHash
    ) external isAdmin {
        require(bytes(ipfsHash).length != 0, "ipfsHash not provided");

        for (uint256 i = 0; i < announcements_.length; i++) {
            if (keccak256(bytes(announcements_[i].ipfsHash)) == keccak256(bytes(ipfsHash_))) {
                announcements_[i].ipfsHash = ipfsHash;
                emit announcementIpfsHashModified(msg.sender, ipfsHash_, ipfsHash, block.timestamp);
                break;
            }
        }
    }

    function updateAnnouncementTime(
        string memory ipfsHash,
        uint256 startTime,
        uint256 endTime
    ) external isAdmin {
        require(startTime != 0, "startTime not provided");
        require(endTime != 0, "endTime not provided");

        for (uint256 i = 0; i < announcements_.length; i++) {
            if (keccak256(bytes(announcements_[i].ipfsHash)) == keccak256(bytes(ipfsHash))) {
                uint256 oldStart = announcements_[i].startTime;
                uint256 oldEnd = announcements_[i].endTime;
                announcements_[i].startTime = startTime;
                announcements_[i].endTime = endTime;
                emit announcementTimeModified(msg.sender, ipfsHash, oldStart, oldEnd, startTime, endTime, block.timestamp);
                break;
            }
        }
    }

    function deleteAnnouncement(
        string memory ipfsHash
    ) external isAdmin {
        for (uint256 i = 0; i < announcements_.length; i++) {
            if (keccak256(bytes(announcements_[i].ipfsHash)) == keccak256(bytes(ipfsHash))) {
                delete announcements_[i];
            }
        }
        emit announcementDeleted(msg.sender, ipfsHash, block.timestamp);
    }

    function getAnnouncements() external isAdmin view returns(Announcement[] memory anns_) {
        require(announcements_.length > 0, "No Announcement found in blockchain");

        // Announcement[] memory anns = new Announcement[](announcements_.length);
        // for(uint256 i=0; i<announcements_.length; i++) {
        //     anns[i] = announcements_[i];
        //     emit announcementRequested(msg.sender, announcements_[i].ipfsHash, block.timestamp);
        // }
        // return anns;
        return announcements_;
    }
}
