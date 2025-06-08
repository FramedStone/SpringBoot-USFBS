// SPDX-License-Identifier: MIT 
pragma solidity 0.8.19;

import "./Management.sol";
import "./Strings.sol";

contract SportFacility is Management {
    // Variable & Modifier Initialization
    enum status { OPEN, CLOSED, MAINTENANCE, BOOKED }

    struct court {
        string name;
        uint256 earliestTime;
        uint256 latestTime;
        status status;
    }
    struct sportFacility {
        string name;
        string location;
        status status;
        court[] courts;
        mapping(string => uint256) cIndex;
    }
    sportFacility[] private sportFacilities; 
    mapping(string => uint256) private sfIndex; // for O(1) lookup

    // Events
    event sportFacilityAdded(
        address indexed from,
        string facilityName,
        string Location,
        string status,
        string courts,
        uint256 timestamp
    );
    event sportFacilityModified(
        address indexed from,
        string facilityName,
        string oldData,
        string newData,
        uint256 timestamp
    );
    event sportFacilityDeleted(
        address indexed from,
        string facilityName,
        uint256 timestamp
    );
    event courtAdded(
        address indexed from,
        string courtName,
        uint256 earliestTime,
        uint256 latestTime,
        string status,
        uint256 timestamp
    );
    event courtModified(
        address indexed from,
        string facilityName,
        string courtName,
        string oldData,
        string newData,
        uint256 timestamp
    );
    event courtDeleted(
        address indexed from,
        string courtName,
        uint256 timestamp
    );
    event facilityDetailsRequested(
        address indexed from,
        string facilityName,
        string note,
        uint256 timestamp
    );
    event courtDetailsRequested(
        address indexed from,
        string facilityName,
        string courtName,
        string note,
        uint256 timestamp
    );

    // Helper Functions
    function statusToString(status status_) internal pure returns(string memory sString) {
        if(status_ == status.OPEN) return "open";
        if(status_ == status.CLOSED) return "closed";
        if(status_ == status.MAINTENANCE) return "maintenance";
        if(status_ == status.BOOKED) return "booked";
        return "unknown"; 
    }

    // Main Functions
    constructor(address[] memory admins_) Management(admins_) {
        // for(uint256 i=0; i<admins_.length; i++) {
        //     admins[admins_[i]] = true; 
        // }
    }

    // Sport Facility CRUD
    function addSportFacility(
        string memory facilityName,
        string memory facilityLocation,
        status facilityStatus,
        court[] memory facilityCourts 
    ) external isAdmin {
        require(bytes(facilityName).length > 0, "Sport Facility name not provided");
        require(bytes(facilityLocation).length > 0, "Sport Facility location not provided");
        require(sfIndex[facilityName] == 0, "Sport Facility name already exists");

        sportFacility storage sf = sportFacilities.push();
        sf.name = facilityName;
        sf.location = facilityLocation;
        sf.status = facilityStatus;

        string memory courtNames = "";
        for(uint256 i = 0; i < facilityCourts.length; i++) {
            require(bytes(facilityCourts[i].name).length > 0, "Court name cannot be empty");
            require(sf.cIndex[facilityCourts[i].name] == 0, "Court name already exists in this facility");
            require(facilityCourts[i].earliestTime < facilityCourts[i].latestTime, "Invalid time range");
            
            sf.courts.push(facilityCourts[i]);
            sf.cIndex[facilityCourts[i].name] = i + 1; 
            courtNames = i == 0
                ? facilityCourts[i].name
                : string(abi.encodePacked(courtNames, ",", facilityCourts[i].name));
        }
        sfIndex[facilityName] = sportFacilities.length; 

        emit sportFacilityAdded(msg.sender, facilityName, facilityLocation, statusToString(facilityStatus), courtNames, block.timestamp);
    }

    function updateSportFacilityName(
        string memory oldFacilityName,
        string memory newFacilityName
    ) external isAdmin {
        require(bytes(oldFacilityName).length > 0, "Sport Facility name not provided (old)");
        require(bytes(newFacilityName).length > 0, "Sport Facility name not provided (new)");
        require(sfIndex[oldFacilityName] != 0, "Sport Facility not found");
        require(sfIndex[newFacilityName] == 0, "New facility name already exists");

        uint256 facilityIndex = sfIndex[oldFacilityName];
        sportFacility storage sf = sportFacilities[facilityIndex - 1];

        sf.name = newFacilityName;
        sfIndex[newFacilityName] = facilityIndex; 
        delete sfIndex[oldFacilityName]; 

        emit sportFacilityModified(msg.sender, oldFacilityName, oldFacilityName, newFacilityName, block.timestamp);
    }

    function updateSportFacilityLocation(
        string memory fname,
        string memory flocation
    ) external isAdmin {
       require(bytes(fname).length > 0, "Sport Facility name not provided");
        require(sfIndex[fname] != 0, "Sport Facility not found");

        uint256 index = sfIndex[fname];
        string memory flocation_ = sportFacilities[index - 1].location;
        sportFacility storage sf = sportFacilities[index - 1];

        sf.location = flocation;

        emit sportFacilityModified(msg.sender, fname, flocation_, flocation, block.timestamp);
    }

    function updateSportFacilityStatus(
        string memory fname,
        status fstatus
    ) external isAdmin {

       require(bytes(fname).length > 0, "Sport Facility name not provided");
        require(sfIndex[fname] != 0, "Sport Facility not found");

        uint256 index = sfIndex[fname];
        status fstatus_ = sportFacilities[index - 1].status;
        sportFacility storage sf = sportFacilities[index - 1];

        sf.status = fstatus;

        emit sportFacilityModified(msg.sender, fname, statusToString(fstatus_), statusToString(fstatus), block.timestamp);
    }

    function deleteSportFacility(string memory fname) external isAdmin {
        require(bytes(fname).length > 0, "Sport Facility name not provided");
        require(sfIndex[fname] != 0, "Sport Facility not found");
        
        uint256 facilityIndex = sfIndex[fname] - 1;
        sportFacility storage facilityToDelete = sportFacilities[facilityIndex];
        
        // Clean up court mappings for the facility being deleted
        for(uint256 i = 0; i < facilityToDelete.courts.length; i++) {
            delete facilityToDelete.cIndex[facilityToDelete.courts[i].name];
        }
        
        if (facilityIndex != sportFacilities.length - 1) {
            sportFacility storage lastFacility = sportFacilities[sportFacilities.length - 1];
            
            // Copy fields manually
            facilityToDelete.name = lastFacility.name;
            facilityToDelete.location = lastFacility.location;
            facilityToDelete.status = lastFacility.status;
            
            // Clear and copy courts array
            delete facilityToDelete.courts;
            for(uint256 i = 0; i < lastFacility.courts.length; i++) {
                facilityToDelete.courts.push(lastFacility.courts[i]);
                facilityToDelete.cIndex[lastFacility.courts[i].name] = i + 1;
            }
            
            // Update facility mapping for moved facility
            sfIndex[lastFacility.name] = facilityIndex + 1;
        }
        
        emit sportFacilityDeleted(msg.sender, fname, block.timestamp);
        
        sportFacilities.pop();
        delete sfIndex[fname];
    }

    // Sport Facility getters
    function getSportFacility_(
        string memory fname
    ) external isAdmin returns(string memory name_, string memory location_, string memory status_) {
        require(sfIndex[fname] != 0, "Sport Facility not found");
        sportFacility storage sf = sportFacilities[sfIndex[fname] - 1];
        emit facilityDetailsRequested(msg.sender, sf.name, "Requested by admin", block.timestamp);
        return (sf.name, sf.location, statusToString(sf.status));
    }

    function getAllSportFacility_()
        external
        isAdmin
        returns (string[] memory names, string[] memory locations, string[] memory statuses)
    {
        require(sportFacilities.length > 0, "No Sport Facility found in blockchain");

        uint256 len = sportFacilities.length;
        names = new string[](len);
        locations = new string[](len);
        statuses = new string[](len);

        for (uint256 i = 0; i < len; i++) {
            names[i] = sportFacilities[i].name;
            locations[i] = sportFacilities[i].location;
            statuses[i] = statusToString(sportFacilities[i].status);
        }
        emit facilityDetailsRequested(msg.sender, "ALL", "Requested all facilities by admin", block.timestamp);
        return (names, locations, statuses);
    }

    function getSportFacility(
        string memory fname
    ) external isUser returns(string memory name_, string memory status_) {
        require(sfIndex[fname] != 0, "Sport Facility not found");
        sportFacility storage sf = sportFacilities[sfIndex[fname] - 1];
        emit facilityDetailsRequested(msg.sender, sf.name, "Requested by user", block.timestamp);
        return (sf.name, statusToString(sf.status));
    }

    function getAllSportFacility()
        external
        isUser
        returns (string[] memory names, string[] memory statuses)
    {
        require(sportFacilities.length > 0, "No Sport Facility found in blockchain");

        uint256 len = sportFacilities.length;
        names = new string[](len);
        statuses = new string[](len);

        for (uint256 i = 0; i < len; i++) {
            names[i] = sportFacilities[i].name;
            statuses[i] = statusToString(sportFacilities[i].status);
        }
        emit facilityDetailsRequested(msg.sender, "ALL", "Requested all facilities by user", block.timestamp);
        return (names, statuses);
    }

    // Court CRUD
    // Smart Court Addition - handles both single and multiple courts
    function addCourt(
        string memory facilityName,
        court[] memory newCourts
    ) external isAdmin {
        require(sfIndex[facilityName] != 0, "Sport Facility not found");
        require(newCourts.length > 0, "No courts provided");

        sportFacility storage sf = sportFacilities[sfIndex[facilityName] - 1];
        
        for(uint256 i = 0; i < newCourts.length; i++) {
            require(bytes(newCourts[i].name).length > 0, "Court name not provided");
            require(newCourts[i].earliestTime != 0, "earliestTime not provided");
            require(newCourts[i].latestTime != 0, "latestTime not provided");
            require(newCourts[i].earliestTime < newCourts[i].latestTime, "Invalid time range");
            require(sf.cIndex[newCourts[i].name] == 0, "Court name already exists in this facility");
            
            sf.courts.push(newCourts[i]);
            sf.cIndex[newCourts[i].name] = sf.courts.length; 
            
            emit courtAdded(msg.sender, newCourts[i].name, newCourts[i].earliestTime, 
                           newCourts[i].latestTime, statusToString(newCourts[i].status), block.timestamp);
        }
    }

    function updateCourtName(
        string memory facilityName,
        string memory oldCourtName,
        string memory newCourtName
    ) external isAdmin {
        require(bytes(newCourtName).length > 0, "Court name not provided");
        require(sfIndex[facilityName] != 0, "Sport Facility not found");
        
        uint256 facilityIdx = sfIndex[facilityName] - 1;
        require(facilityIdx < sportFacilities.length, "Facility index out of bounds");
        
        sportFacility storage sf = sportFacilities[facilityIdx];
        require(sf.cIndex[oldCourtName] != 0, "Court not found");
        require(sf.cIndex[newCourtName] == 0, "New court name already exists in this facility");

        uint256 courtIdx = sf.cIndex[oldCourtName] - 1;
        require(courtIdx < sf.courts.length, "Court index out of bounds");

        court storage courtToUpdate = sf.courts[courtIdx];
        courtToUpdate.name = newCourtName;
        
        sf.cIndex[newCourtName] = sf.cIndex[oldCourtName];
        delete sf.cIndex[oldCourtName];
        
        emit courtModified(msg.sender, facilityName, newCourtName, oldCourtName, newCourtName, block.timestamp);
    }

    function updateCourtTime(
        string memory fname,
        string memory cname,
        uint256 earliestTime,
        uint256 latestTime
    ) external isAdmin {
        // Input validation
        require(bytes(fname).length > 0, "Facility name not provided");
        require(bytes(cname).length > 0, "Court name not provided");
        require(earliestTime < latestTime, "Invalid time range: earliest must be before latest");
        require(earliestTime < 86400, "Earliest time must be within 24 hours (0-86399)");
        require(latestTime < 86400, "Latest time must be within 24 hours (0-86399)");
        
        // Facility and court validation
        require(sfIndex[fname] != 0, "Sport Facility not found");
        uint256 facilityIdx = sfIndex[fname] - 1;
        require(facilityIdx < sportFacilities.length, "Facility index out of bounds");

        sportFacility storage sf = sportFacilities[facilityIdx];
        require(sf.cIndex[cname] != 0, "Court not found");
        uint256 courtIdx = sf.cIndex[cname] - 1;
        require(courtIdx < sf.courts.length, "Court index out of bounds");

        court storage c = sf.courts[courtIdx];

        // Store old values for event
        uint256 earliestTime_ = c.earliestTime;
        uint256 latestTime_ = c.latestTime;

        // Update court times
        c.earliestTime = earliestTime;
        c.latestTime   = latestTime;

        // Emit event with formatted time strings
        string memory oldData = string.concat(
           Strings.uintTo24Hour(earliestTime_), "-",
           Strings.uintTo24Hour(latestTime_)
        );
        string memory newData = string.concat(
           Strings.uintTo24Hour(earliestTime), "-",
           Strings.uintTo24Hour(latestTime)
        );

        emit courtModified(msg.sender, fname, cname, oldData, newData, block.timestamp);
    }

    function updateCourtStatus(
        string memory fname,
        string memory cname,
        status cstatus 
    ) external isAdmin {
        require(sfIndex[fname] != 0, "Sport Facility not found");
        uint256 facilityIdx = sfIndex[fname] - 1;
        require(facilityIdx < sportFacilities.length, "Facility index out of bounds");

        sportFacility storage sf = sportFacilities[facilityIdx];
        require(sf.cIndex[cname] != 0, "Court not found");
        uint256 courtIdx = sf.cIndex[cname] - 1;
        require(courtIdx < sf.courts.length, "Court index out of bounds");

        court storage c = sf.courts[courtIdx];

        status cstatus_ = c.status;
        c.status = cstatus;

        emit courtModified(msg.sender, fname, cname, statusToString(cstatus_), statusToString(cstatus), block.timestamp);
    }

    function deleteCourt(
        string memory fname, 
        string memory cname
    ) external isAdmin {
        require(sfIndex[fname] != 0, "Sport Facility not found");
        uint256 facilityIdx = sfIndex[fname] - 1;
        require(facilityIdx < sportFacilities.length, "Facility index out of bounds");

        sportFacility storage sf = sportFacilities[facilityIdx];
        require(sf.cIndex[cname] != 0, "Court not found");
        uint256 courtIdx = sf.cIndex[cname] - 1;
        require(courtIdx < sf.courts.length, "Court index out of bounds");
        
        if (courtIdx != sf.courts.length - 1) {
            sf.courts[courtIdx] = sf.courts[sf.courts.length - 1];
            sf.cIndex[sf.courts[courtIdx].name] = courtIdx + 1;
        }
        
        sf.courts.pop(); 
        delete sf.cIndex[cname]; 
        
        emit courtDeleted(msg.sender, cname, block.timestamp);
    }

    // Court getters 
    // admin
    function getCourt_(
        string memory fname,
        string memory cname
    ) external isAdmin returns(string memory name_, uint256 earliestTime_, uint256 latestTime_, string memory status_) {
        require(sfIndex[fname] != 0, "Sport Facility not found");
        uint256 facilityIdx = sfIndex[fname] - 1;
        require(facilityIdx < sportFacilities.length, "Facility index out of bounds");

        sportFacility storage sf = sportFacilities[facilityIdx];
        require(sf.cIndex[cname] != 0, "Court not found");
        uint256 courtIdx = sf.cIndex[cname] - 1;
        require(courtIdx < sf.courts.length, "Court index out of bounds");

        court storage c = sf.courts[courtIdx];

        emit courtDetailsRequested(msg.sender, fname, cname, "Requested by admin", block.timestamp);
        return (c.name, c.earliestTime, c.latestTime, statusToString(c.status));
    }

    function getAllCourts_(string memory fname) external isAdmin returns(
        string[] memory names,
        uint256[] memory earliestTimes,
        uint256[] memory latestTimes,
        string[] memory statuses
    ) {
        require(sfIndex[fname] != 0, "Sport Facility not found");
        uint256 facilityIdx = sfIndex[fname] - 1;
        require(facilityIdx < sportFacilities.length, "Facility index out of bounds");
        
        sportFacility storage sf = sportFacilities[facilityIdx];
        
        uint256 courtCount = sf.courts.length;
        names = new string[](courtCount);
        earliestTimes = new uint256[](courtCount);
        latestTimes = new uint256[](courtCount);
        statuses = new string[](courtCount);
        
        for(uint256 i = 0; i < courtCount; i++) {
            names[i] = sf.courts[i].name;
            earliestTimes[i] = sf.courts[i].earliestTime;
            latestTimes[i] = sf.courts[i].latestTime;
            statuses[i] = statusToString(sf.courts[i].status);
        }
        
        emit courtDetailsRequested(msg.sender, fname, "All courts", "Requested by admin", block.timestamp);
        return (names, earliestTimes, latestTimes, statuses);
    }

    // user
    function getCourt(
        string memory fname,
        string memory cname
    ) external isUser returns(string memory name_, uint256 earliestTime_, uint256 latestTime_, string memory status_) {
        require(sfIndex[fname] != 0, "Sport Facility not found");
        uint256 facilityIdx = sfIndex[fname] - 1;
        require(facilityIdx < sportFacilities.length, "Facility index out of bounds");

        sportFacility storage sf = sportFacilities[facilityIdx];
        require(sf.cIndex[cname] != 0, "Court not found");
        uint256 courtIdx = sf.cIndex[cname] - 1;
        require(courtIdx < sf.courts.length, "Court index out of bounds");

        court storage c = sf.courts[courtIdx];

        emit courtDetailsRequested(msg.sender, fname, cname, "Requested by user", block.timestamp);
        return (c.name, c.earliestTime, c.latestTime, statusToString(c.status));
    }

    function getAllCourts(string memory fname) external isUser returns(
        string[] memory names,
        uint256[] memory earliestTimes,
        uint256[] memory latestTimes,
        string[] memory statuses
    ) {
        require(sfIndex[fname] != 0, "Sport Facility not found");
        uint256 facilityIdx = sfIndex[fname] - 1;
        require(facilityIdx < sportFacilities.length, "Facility index out of bounds");
        
        sportFacility storage sf = sportFacilities[facilityIdx];
        
        uint256 courtCount = sf.courts.length;
        names = new string[](courtCount);
        earliestTimes = new uint256[](courtCount);
        latestTimes = new uint256[](courtCount);
        statuses = new string[](courtCount);
        
        for(uint256 i = 0; i < courtCount; i++) {
            names[i] = sf.courts[i].name;
            earliestTimes[i] = sf.courts[i].earliestTime;
            latestTimes[i] = sf.courts[i].latestTime;
            statuses[i] = statusToString(sf.courts[i].status);
        }
        
        emit courtDetailsRequested(msg.sender, fname, "All courts", "Requested by user", block.timestamp);
        return (names, earliestTimes, latestTimes, statuses);
    }

    function getAvailableTimeRange_(
        string memory fname,
        string memory cname,
        address adminAddress
    ) external returns(uint256 earliestTime_, uint256 latestTime_) {
        require(admins[adminAddress] == true, "Access Denied");
        require(sfIndex[fname] != 0, "Sport Facility not found");
        uint256 facilityIdx = sfIndex[fname] - 1;
        require(facilityIdx < sportFacilities.length, "Facility index out of bounds");

        sportFacility storage sf = sportFacilities[facilityIdx];
        require(sf.cIndex[cname] != 0, "Court not found");
        uint256 courtIdx = sf.cIndex[cname] - 1;
        require(courtIdx < sf.courts.length, "Court index out of bounds");

        court storage c = sf.courts[courtIdx];

        require(sf.status == status.OPEN, "Sport Facility status is not OPENED");
        require(c.status == status.OPEN, "Court status is not OPENED");

        emit courtDetailsRequested(msg.sender, fname, cname, "Requested by admin", block.timestamp);
        return(c.earliestTime, c.latestTime);
    }

    function getAvailableTimeRange(
        string memory fname,
        string memory cname ,
        address userAddress
    ) external isUser returns(uint256 earliestTime_, uint256 latestTime_) {
        require(admins[userAddress] == true, "Access Denied");
        require(sfIndex[fname] != 0, "Sport Facility not found");
        uint256 facilityIdx = sfIndex[fname] - 1;
        require(facilityIdx < sportFacilities.length, "Facility index out of bounds");

        sportFacility storage sf = sportFacilities[facilityIdx];
        require(sf.cIndex[cname] != 0, "Court not found");
        uint256 courtIdx = sf.cIndex[cname] - 1;
        require(courtIdx < sf.courts.length, "Court index out of bounds");

        court storage c = sf.courts[courtIdx];

        require(sf.status == status.OPEN, "Sport Facility status is not OPENED");
        require(c.status == status.OPEN, "Court status is not OPENED");

        emit courtDetailsRequested(msg.sender, fname, cname, "Requested by user", block.timestamp);
        return(c.earliestTime, c.latestTime);
    }
}