// SPDX-License-Identifier: MIT 
pragma solidity 0.8.19;

import "./Strings.sol";

contract Management {
    // Variable & Modifier Initialization
    struct Announcement {
        string  ipfsHash;
        string title;
        uint256 startTime;
        uint256 endTime;
    }
    Announcement[] private announcements_;

    mapping(address => bool) internal admins;

    struct User {
        address userAddress;
        string bannedReason;
    }
    User[] private users_;
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
        string note,
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
        string title,
        uint256 startTime,
        uint256 endTime,
        uint256 timestamp
    );
    event announcementModified(
        address indexed from,
        string ipfsHash_,
        string ipfsHash,
        string title_,
        string title,
        uint256 startTime_,
        uint256 endTime_,
        uint256 startTime,
        uint256 endTime,
        uint256 timestamp
    );
    event announcementDeleted(
        address indexed from,
        string ipfsHash,
        string title,
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

        users_.push(User(user, "-"));
        users[user] = true;
        emit userAdded(
            msg.sender,
            user,
            Strings.toString(user),
            block.timestamp
        );
    }
    
    function banUser(
        address user,
        string memory reason
    ) external isAdmin {
        require(user != address(0), "User address not provided");
        require(users[user] == true, "User not found (system)");
        require(bytes(reason).length > 0, "Ban reason not provided");

        bannedUsers[user] = true;
        for(uint256 i=0; i<users_.length; i++) {
            if(users_[i].userAddress == user) {
                users_[i].bannedReason = reason;
                break;
            }
        }
        emit userBanned(
            msg.sender,
            user,
            string(abi.encodePacked(Strings.toString(user), " - Reason: ", reason)),
            block.timestamp
        );
    }
    
    function unbanUser(
        address user,
        string memory reason
    ) external isAdmin {
        require(user != address(0), "User address not provided");
        require(bannedUsers[user] == true, "User is not banned");
        require(bytes(reason).length > 0, "Unban reason not provided");

        bannedUsers[user] = false;
        for(uint256 i=0; i<users_.length; i++) {
            if(users_[i].userAddress == user) {
                users_[i].bannedReason = "-";
                break;
            }
        }
        emit userUnbanned(
            msg.sender,
            user,
            string(abi.encodePacked(Strings.toString(user), " - Reason: ", reason)),
            block.timestamp
        );
    }

    // Getters
    function getUsers() external view returns(User[] memory userList) {
        require(users_.length > 0, "No registered users found in blockchain");
        return users_;
    }
    function getUser(address user) external view isAdmin returns(bool isRegistered) {
        return users[user];
    }
    function getBannedUser(address user) external view isAdmin returns(bool isBanned) {
        return bannedUsers[user];
    }

    // Announcement CRUD
    function addAnnouncement(
        string memory ipfsHash,
        string memory title,
        uint256 startTime,
        uint256 endTime
    ) external isAdmin {
        require(startTime != 0, "startTime not provided");
        require(endTime   != 0, "endTime not provided");
        require(bytes(title).length != 0, "title not provided");

        // Check for duplicated ipfsHash
        for (uint256 i = 0; i < announcements_.length; i++) {
            if (keccak256(bytes(announcements_[i].ipfsHash)) == keccak256(bytes(ipfsHash))) {
                revert("duplicated ipfsHash");
            }
        }

        announcements_.push(Announcement(ipfsHash, title, startTime, endTime));
        emit announcementAdded(msg.sender, ipfsHash, title, startTime, endTime, block.timestamp);
    }

    function updateAnnouncement(
        string memory ipfsHash_,
        string memory ipfsHash,
        string memory title,
        uint256 startTime,
        uint256 endTime
    ) external isAdmin {
        require(bytes(ipfsHash).length != 0, "ipfsHash not provided");

        for (uint256 i = 0; i < announcements_.length; i++) {
            if (keccak256(bytes(announcements_[i].ipfsHash)) == keccak256(bytes(ipfsHash_))) {
                Announcement memory temp = announcements_[i];

                announcements_[i].ipfsHash = ipfsHash;
                if(bytes(title).length != 0) {
                    announcements_[i].title = title;
                }
                if(startTime != 0) {
                    announcements_[i].startTime = startTime;
                }
                if(endTime !=0) {
                    announcements_[i].endTime = endTime;
                }
                emit announcementModified(
                    msg.sender, 
                    ipfsHash_, 
                    ipfsHash, 
                    temp.title, 
                    title, 
                    temp.startTime, 
                    temp.endTime, 
                    startTime, 
                    endTime, 
                    block.timestamp
                );
                break;
            }
        }
    }

    function deleteAnnouncement(
        string memory ipfsHash
    ) external isAdmin {
        for (uint256 i = 0; i < announcements_.length; i++) {
            if (keccak256(bytes(announcements_[i].ipfsHash)) == keccak256(bytes(ipfsHash))) {
                string memory tempTitle = announcements_[i].title;
                announcements_[i] = announcements_[announcements_.length - 1];
                announcements_.pop();
                emit announcementDeleted(msg.sender, ipfsHash, tempTitle, block.timestamp);
                return;
            }
        }
        revert("Announcement not found");
    }

    function getAnnouncements() external view returns(Announcement[] memory anns_) {
        require(announcements_.length > 0, "No Announcement found in blockchain");

        return announcements_;
    }
}
