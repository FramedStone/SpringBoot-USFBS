// SPDX-License-Identifier: MIT 
pragma solidity 0.8.19;

import "./Management.sol";
import "./Strings.sol";

contract SportFacility is Management {
    // Variable & Modifier Initialization
    address private immutable admin_;
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

    // modifier isAdmin {
    //     require(msg.sender == admin_, "Access denied");
    //     _;
    // } 

    // Events
    event sportFacilityAdded(
        address indexed from,
        string facilityName,
        string Location,
        string status,
        court[] courts,
        uint256 time
    );
    event sportFacilityModified(
        address indexed from,
        string facilityName,
        string oldData,
        string newData,
        uint256 time
    );
    event sportFacilityDeleted(
        address indexed from,
        string facilityName,
        uint256 time
    );
    event courtAdded(
        address indexed from,
        string courtName,
        uint256 earliestTime,
        uint256 latestTime,
        string status,
        uint256 time
    );
    event courtModified(
        address indexed from,
        string facilityName,
        string courtName,
        string oldData,
        string newData,
        uint256 time
    );
    event courtDeleted(
        address indexed from,
        string courtName,
        uint256 time
    );
    event facilityDetailsRequested(
        address indexed from,
        string facilityName,
        string note,
        uint256 time
    );
    event courtDetailsRequested(
        address indexed from,
        string facilityName,
        string courtName,
        string note,
        uint256 time
    );

    // Helper Functions
    function statusToString(status status_) internal pure returns(string memory) {
        if(status_ == status.OPEN) return "open";
        if(status_ == status.CLOSED) return "closed";
        if(status_ == status.MAINTENANCE) return "maintenance";
        if(status_ == status.BOOKED) return "booked";
        return "unknown"; 
    }

    // Main Functions
    constructor(address admin) Management(admin) {
       admin_ = admin; 
    }

    // Sport Facility CRUD
    function addSportFacility(
        string memory fname,
        string memory flocation,
        status fstatus, // default (if not provided) = 0 (OPEN)
        court[] memory fcourts 
    ) external isAdmin {
        require(keccak256(bytes(fname)) != keccak256(bytes("")), "Sport Facility name not provided");
        require(keccak256(bytes(flocation)) != keccak256(bytes("")), "Sport Facility location not provided");

        sportFacility storage sf = sportFacilities.push();
        sf.name = fname;
        sf.location = flocation;
        sf.status = fstatus;
        for(uint256 i=0; i<fcourts.length; i++) {
            sf.courts.push(fcourts[i]);
            sf.cIndex[fcourts[i].name] = i;
        }
        sfIndex[fname] = sportFacilities.length;

        emit sportFacilityAdded(msg.sender, fname, flocation, statusToString(fstatus), fcourts, block.timestamp);
    }

    function updateSportFacilityName(
        string memory fname_,
        string memory fname
    ) external isAdmin {
        require(keccak256(bytes(fname_)) != keccak256(bytes("")), "Sport Facility name not provided (old)");
        require(keccak256(bytes(fname)) != keccak256(bytes("")), "Sport Facility name not provided (new)");
        require(sfIndex[fname_] != 0, "Sport Facility not found");

        uint256 index = sfIndex[fname_];
        sportFacility storage sf = sportFacilities[index - 1];

        sf.name = fname;
        sfIndex[fname] = sfIndex[fname_]; // remap 

        emit sportFacilityModified(msg.sender, fname_, fname_, fname, block.timestamp);
    }

    function updateSportFacilityLocation(
        string memory fname,
        string memory flocation
    ) external isAdmin {
        require(keccak256(bytes(fname)) != keccak256(bytes("")), "Sport Facility name not provided");
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

        require(keccak256(bytes(fname)) != keccak256(bytes("")), "Sport Facility name not provided");
        require(sfIndex[fname] != 0, "Sport Facility not found");

        uint256 index = sfIndex[fname];
        status fstatus_ = sportFacilities[index - 1].status;
        sportFacility storage sf = sportFacilities[index - 1];

        sf.status = fstatus;

        emit sportFacilityModified(msg.sender, fname, statusToString(fstatus_), statusToString(fstatus), block.timestamp);
    }

    function deleteSportFacility(string memory fname) external isAdmin {
        require(keccak256(bytes(fname)) != keccak256(bytes("")), "fname not provided");
        require(keccak256(bytes(sportFacilities[sfIndex[fname]].name)) != keccak256(bytes(fname)), "Sport Facility not found");
        emit sportFacilityDeleted(msg.sender, fname, block.timestamp);

        delete sportFacilities[sfIndex[fname]];
        delete sfIndex[fname];
    }

    // Sport Facility getters
    function getAllSportFacility_(
        string memory fname
    ) external isAdmin returns(string memory name_, string memory location_, string memory status_) {
        require(sfIndex[fname] != 0, "Sport Facility not found");

        sportFacility storage sf = sportFacilities[sfIndex[fname] - 1];
        emit facilityDetailsRequested(msg.sender, sf.name, "Requested by admin", block.timestamp);
        return (
            sf.name,
            sf.location,
            statusToString(sf.status)
        );
    }

    function getAllSportFacility(
        string memory fname
    ) external isUser returns(string memory name_, string memory status_) {
        require(sfIndex[fname] != 0, "Sport Facility not found");

        sportFacility storage sf = sportFacilities[sfIndex[fname] - 1];
        emit facilityDetailsRequested(msg.sender, sf.name, "Requested by user", block.timestamp);
        return (
            sf.name,
            statusToString(sf.status)
        );
    }

    function getFacilityLocation(
        string memory fname
    ) external isUser returns(string memory location_) {
        require(sfIndex[fname] != 0, "Sport Facility not found");

        sportFacility storage sf = sportFacilities[sfIndex[fname] - 1];
        emit facilityDetailsRequested(msg.sender, sf.name, "Requested by user", block.timestamp);
        return (
            sf.location
        );
    }

    // Court CRUD
    function addCourt(
        string memory fname,
        court memory newCourt
    ) external isAdmin {
        require(keccak256(bytes(newCourt.name)) != keccak256(bytes("")), "cname not provided");
        require(newCourt.earliestTime != 0, "earlistTime not provided");
        require(newCourt.latestTime != 0, "latestTime not provided");
        require(sfIndex[fname] != 0, "Sport Facility not found");

        sportFacility storage sf = sportFacilities[sfIndex[fname] - 1];
        sf.courts.push(newCourt);
        sf.cIndex[newCourt.name] = sf.courts.length;

        emit courtAdded(msg.sender, newCourt.name, newCourt.earliestTime, newCourt.latestTime, statusToString(newCourt.status), block.timestamp);
    }

    function updateCourtName(
        string memory fname,
        string memory cname_,
        string memory cname
    ) external isAdmin {
        require(keccak256(bytes(cname)) != keccak256(bytes("")), "cname not provided");
        require(sfIndex[fname] != 0, "Sport Facility not found");
        require(sportFacilities[sfIndex[fname]].cIndex[cname_] != 0, "Court not found");

        sportFacility storage sf = sportFacilities[sfIndex[fname] - 1];
        court storage c = sf.courts[sf.cIndex[cname_] - 1];

        c.name = cname;
        sf.cIndex[cname] = sf.cIndex[cname_]; // remap
        
        emit courtModified(msg.sender, fname, cname, cname_, cname, block.timestamp);
    }

    function updateCourtTime(
        string memory fname,
        string memory cname,
        uint256 earliestTime,
        uint256 latestTime
    ) external isAdmin {
        require(sfIndex[fname] != 0, "Sport Facility not found");
        require(sportFacilities[sfIndex[fname]].cIndex[cname] != 0, "Court not found");

        sportFacility storage sf = sportFacilities[sfIndex[fname] - 1];
        court storage c = sf.courts[sf.cIndex[cname] - 1];

        uint256 earliestTime_ = c.earliestTime;
        uint256 latestTime_   = c.latestTime;

        c.earliestTime = earliestTime;
        c.latestTime   = latestTime;

        // convert into 24hour format 
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
        require(sportFacilities[sfIndex[fname]].cIndex[cname] != 0, "Court not found");

        sportFacility storage sf = sportFacilities[sfIndex[fname] - 1];
        court storage c = sf.courts[sf.cIndex[cname] - 1];

        status cstatus_ = c.status;
        c.status = cstatus;

        emit courtModified(msg.sender, fname, cname, statusToString(cstatus_), statusToString(cstatus), block.timestamp);
    }

    function deleteCourt(
        string memory fname, 
        string memory cname
    ) external isAdmin {
        require(sfIndex[fname] != 0, "Sport Facility not found");
        require(sportFacilities[sfIndex[fname]].cIndex[cname] != 0, "Court not found");

        sportFacility storage sf = sportFacilities[sfIndex[fname] - 1];
        court memory c = sf.courts[sf.cIndex[cname] - 1];

        delete c;
        delete sf.cIndex[cname]; // delete mapping
    }

    // Court getters
    // admin
    function getCourt_(
        string memory fname,
        string memory cname
    ) external isAdmin returns(court memory court_) {
        require(sfIndex[fname] != 0, "Sport Facility not found");
        require(sportFacilities[sfIndex[fname]].cIndex[cname] != 0, "Court not found");

        sportFacility storage sf = sportFacilities[sfIndex[fname] - 1];
        court storage c = sf.courts[sf.cIndex[cname] - 1];

        emit courtDetailsRequested(msg.sender, fname, cname, "Requested by admin", block.timestamp);
        return c;
    }

    function getAllCourts_(string memory fname) external isAdmin returns(court[] memory courts) {
        require(sfIndex[fname] != 0, "Sport Facility not found");

        sportFacility storage sf = sportFacilities[sfIndex[fname] - 1];

        emit courtDetailsRequested(msg.sender, fname, "All courts", "Requested by admin", block.timestamp);
        return sf.courts;
    }

    function getAvailableTimeRange_(
        string memory fname,
        string memory cname 
    ) public isAdmin returns(uint256 earliestTime_, uint256 latestTime_) {
        require(sfIndex[fname] != 0, "Sport Facility not found");
        require(sportFacilities[sfIndex[fname]].cIndex[cname] != 0, "Court not found");

        sportFacility storage sf = sportFacilities[sfIndex[fname] - 1];
        court storage c = sf.courts[sf.cIndex[cname] - 1];

        require(sf.status == status.OPEN, "Sport Facility status is not OPENED");
        require(c.status == status.OPEN, "Court status is not OPENED");

        uint256 earliestTime = c.earliestTime;
        uint256 latestTime = c.latestTime;
        emit courtDetailsRequested(msg.sender, fname, cname, "Requested by admin", block.timestamp);

        return(earliestTime, latestTime);
    }

    // user
    function getCourt(
        string memory fname,
        string memory cname
    ) external isUser returns(court memory court_) {
        require(sfIndex[fname] != 0, "Sport Facility not found");
        require(sportFacilities[sfIndex[fname]].cIndex[cname] != 0, "Court not found");

        sportFacility storage sf = sportFacilities[sfIndex[fname] - 1];
        court storage c = sf.courts[sf.cIndex[cname] - 1];

        emit courtDetailsRequested(msg.sender, fname, cname, "Requested by user", block.timestamp);
        return c;
    }

    function getAllCourts(string memory fname) external isUser returns(court[] memory courts) {
        require(sfIndex[fname] != 0, "Sport Facility not found");

        sportFacility storage sf = sportFacilities[sfIndex[fname] - 1];

        emit courtDetailsRequested(msg.sender, fname, "All courts", "Requested by user", block.timestamp);
        return sf.courts;
    }

    function getAvailableTimeRange(
        string memory fname,
        string memory cname 
    ) public isUser returns(uint256 earliestTime_, uint256 latestTime_) {
        require(sfIndex[fname] != 0, "Sport Facility not found");
        require(sportFacilities[sfIndex[fname]].cIndex[cname] != 0, "Court not found");

        sportFacility storage sf = sportFacilities[sfIndex[fname] - 1];
        court storage c = sf.courts[sf.cIndex[cname] - 1];

        require(sf.status == status.OPEN, "Sport Facility status is not OPENED");
        require(c.status == status.OPEN, "Court status is not OPENED");

        uint256 earliestTime = c.earliestTime;
        uint256 latestTime = c.latestTime;
        emit courtDetailsRequested(msg.sender, fname, cname, "Requested by user", block.timestamp);

        return(earliestTime, latestTime);
    }
}