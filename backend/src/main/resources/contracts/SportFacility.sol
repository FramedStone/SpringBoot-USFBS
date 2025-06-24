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
        string imageIPFS;
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
        string imageIPFS,
        string status,
        string courts,
        uint256 timestamp
    );
    event sportFacilityModified(
        address indexed from,
        string fname_,
        string fname,
        string flocation_,
        string flocation,
        string fimageIPFS_,
        string fimageIPFS,
        string fstatus_,
        string fstatus,
        uint256 timestamp
    );
    event sportFacilityDeleted(
        address indexed from,
        string facilityName,
        uint256 timestamp
    );
    event courtAdded(
        address indexed from,
        string facilityName,
        string courtName,
        uint256 earliestTime,
        uint256 latestTime,
        string status,
        uint256 timestamp
    );
    event courtModified(
        address indexed from,
        string fname,
        string cname_,
        string cname,
        uint256 earliestTime_,
        uint256 latestTime_,
        uint256 earliestTime,
        uint256 latestTime,
        string status_,
        string status,
        uint256 timestamp
    );
    event courtDeleted(
        address indexed from,
        string facilityName,
        string courtName,
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
        string memory imageIPFS,
        status facilityStatus,
        court[] memory facilityCourts 
    ) external isAdmin {
        require(bytes(facilityName).length > 0, "Sport Facility name not provided");
        require(bytes(facilityLocation).length > 0, "Sport Facility location not provided");
        require(sfIndex[facilityName] == 0, "Sport Facility name already exists");

        sportFacility storage sf = sportFacilities.push();
        sf.name = facilityName;
        sf.location = facilityLocation;
        sf.imageIPFS = imageIPFS;
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

        emit sportFacilityAdded(msg.sender, facilityName, facilityLocation, imageIPFS, statusToString(facilityStatus), courtNames, block.timestamp);
    }

    function updateSportFacility (
        string memory fname_,
        string memory fname,
        string memory flocation,
        string memory fimageIPFS,
        status fstatus
    ) external isAdmin {
        require(bytes(fname_).length > 0, "Sport Facility name not provided");

        uint256 facilityIndex = sfIndex[fname_];
        sportFacility storage sf = sportFacilities[facilityIndex - 1];
        sportFacility storage temp = sportFacilities[facilityIndex - 1];

        if(bytes(fname).length > 0) {
            sf.name = fname;
            sfIndex[fname] = facilityIndex; 
            delete sfIndex[fname_]; 
        }
        if(bytes(flocation).length > 0) {
            sf.location = flocation;
        }
        if(bytes(fimageIPFS).length > 0) {
            sf.imageIPFS = fimageIPFS;
        }
        if(fstatus != sf.status) {
            sf.status = fstatus;
        }

        emit sportFacilityModified(
            msg.sender,
            fname_,
            fname,
            temp.location,
            flocation,
            temp.imageIPFS,
            fimageIPFS,
            statusToString(temp.status),
            statusToString(fstatus),
            block.timestamp
        );
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
    function getSportFacility(
        string memory fname
    ) external view returns(
        string memory name,
        string memory location,
        string memory imageIPFS,
        status facilityStatus,
        court[] memory courts
    ) {
        require(sfIndex[fname] != 0, "Sport Facility not found");
        sportFacility storage sf = sportFacilities[sfIndex[fname] - 1];
        return (sf.name, sf.location, sf.imageIPFS, sf.status, sf.courts);
    }
    function getAllSportFacility() external view returns (
            string[] memory names,
            string[] memory locations,
            string[] memory imageIPFS,
            status[] memory statuses
    ) {
        require(sportFacilities.length > 0, "No Sport Facility found in blockchain");
        
        names = new string[](sportFacilities.length);
        locations = new string[](sportFacilities.length);
        imageIPFS = new string[](sportFacilities.length);
        statuses = new status[](sportFacilities.length);
        
        for(uint256 i = 0; i < sportFacilities.length; i++) {
            names[i] = sportFacilities[i].name;
            locations[i] = sportFacilities[i].location;
            imageIPFS[i] = sportFacilities[i].imageIPFS;
            statuses[i] = sportFacilities[i].status;
        }
        return(names, locations, imageIPFS, statuses);
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
            
            emit courtAdded(msg.sender, facilityName, newCourts[i].name, newCourts[i].earliestTime, 
                           newCourts[i].latestTime, statusToString(newCourts[i].status), block.timestamp);
        }
    }

    function updateCourt(
        string memory fname,
        string memory cname_,
        string memory cname,
        uint256 earliestTime,
        uint256 latestTime,
        status cstatus 
    ) external isAdmin {
        require(sfIndex[fname] != 0, "Sport Facility not found");
        uint256 facilityIdx = sfIndex[fname] - 1;
        require(facilityIdx < sportFacilities.length, "Facility index out of bounds");

        sportFacility storage sf = sportFacilities[facilityIdx];
        require(sf.cIndex[cname_] != 0, "Court not found");
        require(sf.cIndex[cname] == 0, "New court name already exists in this facility");

        uint256 courtIdx = sf.cIndex[cname_] - 1;
        require(courtIdx < sf.courts.length, "Court index out of bounds");
        court storage courtToUpdate = sf.courts[courtIdx];
        court memory temp = sf.courts[courtIdx];
        if(bytes(cname).length > 0) {
            courtToUpdate.name = cname;
            sf.cIndex[cname] = sf.cIndex[cname_];
            delete sf.cIndex[cname_];
        }
        if(earliestTime != 0) {
            require(earliestTime < 86400, "Earliest time must be within 24 hours (0-86399)");
            require(earliestTime < courtToUpdate.latestTime, "Invalid time range: earliest must be before latest");

            courtToUpdate.earliestTime = earliestTime;
        }
        if(latestTime != 0) {
            require(latestTime < 86400, "Latest time must be within 24 hours (0-86399)");
            require(latestTime > courtToUpdate.earliestTime, "Invalid time range: latest must be after earliest");

            courtToUpdate.latestTime = latestTime;
        }
        if(cstatus != courtToUpdate.status) {
            courtToUpdate.status = cstatus;
        }
        string memory tempStatus = statusToString(cstatus);

        emit courtModified(
            msg.sender,
            fname,
            cname_,
            cname,
            temp.earliestTime,
            temp.latestTime,
            earliestTime,
            latestTime,
            statusToString(temp.status),
            tempStatus,
            block.timestamp
        );
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
        
        emit courtDeleted(msg.sender, fname, cname, block.timestamp);
    }

    // Court getters 
    function getCourt(
        string memory fname,
        string memory cname
    ) external view returns(
        string memory name,
        uint256 earliestTime,
        uint256 latestTime,
        status status_
    ) {
        require(sfIndex[fname] != 0, "Sport Facility not found");
        uint256 facilityIdx = sfIndex[fname] - 1;
        require(facilityIdx < sportFacilities.length, "Facility index out of bounds");

        sportFacility storage sf = sportFacilities[facilityIdx];
        require(sf.cIndex[cname] != 0, "Court not found");
        uint256 courtIdx = sf.cIndex[cname] - 1;
        require(courtIdx < sf.courts.length, "Court index out of bounds");

        court storage c = sf.courts[courtIdx];

        return (c.name, c.earliestTime, c.latestTime, c.status);
    }

    function getAllCourts(string memory fname) external view returns(court[] memory courts_) {
        require(sfIndex[fname] != 0, "Sport Facility not found");
        uint256 facilityIdx = sfIndex[fname] - 1;
        require(facilityIdx < sportFacilities.length, "Facility index out of bounds");
        
        sportFacility storage sf = sportFacilities[facilityIdx];
        return sf.courts;
    }

    function getAvailableTimeRange(
        string memory fname,
        string memory cname
    ) external view returns(uint256 earliestTime_, uint256 latestTime_) {
        require(sfIndex[fname] != 0, "Sport Facility not found");
        uint256 facilityIdx = sfIndex[fname] - 1;
        require(facilityIdx < sportFacilities.length, "Facility index out of bounds");

        sportFacility storage sf = sportFacilities[facilityIdx];
        require(sf.cIndex[cname] != 0, "Court not found");
        uint256 courtIdx = sf.cIndex[cname] - 1;
        require(courtIdx < sf.courts.length, "Court index out of bounds");

        court storage c = sf.courts[courtIdx];

        return(c.earliestTime, c.latestTime);
    }
}