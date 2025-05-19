// SPDX-License-Identifier: MIT 
pragma solidity 0.8.19;

import "./Management.sol";

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
       addUser(admin);
    }

    // Sport Facility CRUD
    function addSportFacility(
        string memory fname,
        string memory flocation,
        status fstatus, // default (if not provided) = 0 (OPEN)
        court[] memory fcourts 
    ) external isAdmin returns(bool isSuccess) {
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
        return true;
    }
    function updateSportFacilityName(string memory fname) external isAdmin returns(bool isSuccess) {
        require(keccak256(bytes(fname)) != keccak256(bytes("")), "Sport Facility name not provided");
        require(sfIndex[fname] != 0, "Sport Facility not found");

        uint256 index = sfIndex[fname];
        string memory oldData = sportFacilities[index].name;
        string memory newData = fname;
        sportFacilities[index].name = newData;

        sportFacilityModified(msg.sender, fname, oldData, newData, block.timestamp);
        return true;
    }
    function updateSportFacilityLocation(
        string memory fname,
        string memory flocation
    ) external isAdmin returns(bool isSuccess) {
        require(keccak256(bytes(fname)) != keccak256(bytes("")), "Sport Facility name not provided");
        require(sfIndex[fname] != 0, "Sport Facility not found");

        uint256 index = sfIndex[fname];
        string memory oldData = sportFacilities[index].location;
        string memory newData = flocation;
        sportFacilities[index].location = flocation;

        sportFacilityModified(msg.sender, fname, oldData, newData, block.timestamp);
        return true;
    }
    function updateSportFacilityStatus(
        string memory fname,
        status fstatus
    ) external isAdmin returns(bool isSuccess) {

        require(keccak256(bytes(fname)) != keccak256(bytes("")), "Sport Facility name not provided");
        require(sfIndex[fname] != 0, "Sport Facility not found");

        uint256 index = sfIndex[fname];
        status oldData = sportFacilities[index].status;
        status newData = fstatus;
        sportFacilities[index].status = fstatus;

        sportFacilityModified(msg.sender, fname, statusToString(oldData), statusToString(newData), block.timestamp);
        return true;
    }
    function deleteSportFacility(string memory fname) external isAdmin returns(bool isSuccess) {
        require(keccak256(bytes(fname)) != keccak256(bytes("")), "fname not provided");
        require(keccak256(bytes(sportFacilities[sfIndex[fname]].name)) != keccak256(bytes(fname)), "Sport Facility not found");
        emit sportFacilityDeleted(msg.sender, fname, block.timestamp);

        delete sportFacilities[sfIndex[fname]];
        return true;
    }

    // Sport Facility getters
    function getAllSportFacility_(
        string memory fname
    ) external isAdmin view returns(string memory name_, string memory location_, string memory status_) {
        require(sfIndex[fname] != 0, "Sport Facility not found");

        sportFacility storage sf = sportFacilities[sfIndex[fname]];
        facilityDetailsRequested(msg.sender, sf.name, "Requested by admin", block.timestamp);
        return (
            sf.name,
            sf.location,
            statusToString(sf.status)
        );
    }
    function getAllSportFacility(
        string memory fname
    ) external isUser view returns(string memory name_, string memory status_) {
        require(sfIndex[fname] != 0, "Sport Facility not found");

        sportFacility storage sf = sportFacilities[sfIndex[fname]];
        facilityDetailsRequested(msg.sender, sf.name, "Requested by user", block.timestamp);
        return (
            sf.name,
            statusToString(sf.status)
        );
    }
    function getFacilityLocation(
        string memory fname
    ) external isUser view returns(string memory location_) {
        require(sfIndex[fname] != 0, "Sport Facility not found");

        sportFacility storage sf = sportFacilities[sfIndex[fname]];
        facilityDetailsRequested(msg.sender, sf.name, "Requested by user", block.timestamp);
        return (
            sf.location
        );
    }

    // Court CRUD
    function addCourt(
        string memory fname,
        court memory newCourt
    ) external isAdmin returns(bool isSuccess) {
        require(keccak256(bytes(newCourt.name)) != keccak256(bytes("")), "cname not provided");
        require(newCourt.earliestTime != 0, "earlistTime not provided");
        require(newCourt.latestTime != 0, "latestTime not provided");
        require(sfIndex[fname] != 0, "Sport Facility not found");

        sportFacility storage sf = sportFacilities[sfIndex[fname]];
        sf.courts.push(newCourt);
        sf.cIndex[newCourt.name] = sf.courts.length - 1;

        emit courtAdded(msg.sender, newCourt.name, newCourt.earliestTime, newCourt.latestTime, statusToString(newCourt.status), block.timestamp);
        return true;
    }

    function updateCourtName(
        string memory fname,
        string memory cname
    ) external isAdmin returns(bool isSuccess) {

    }
    function updateCourtEarliestTime(
        string memory fname,
        string memory cname,
        uint256 earliestTime
    ) external isAdmin returns(bool isSuccess) {

    }
    function updateCourtLatestTime(
        string memory fname,
        string memory cname,
        uint256 latestTime
    ) external isAdmin returns(bool isSuccess) {

    }

    function deleteCourt(
        string memory fname, 
        string memory cname
    ) external isAdmin returns(bool isSuccess) {

    }

    // Court getters
    function getCourt(string memory fname) external isUser returns(court memory court_) {

    }
    function getAllCourts(string memory fname) external isUser returns(court[] memory courts) {

    }

    // to be called by booking.sol
    function getAvailableTimeRange(
        string memory fname,
        string memory cname 
    ) public isUser view returns(uint256 earliestTime, uint256 latestTime) {
        require(
            sportFacilities[facilityName].courts[courtName].status == status.OPEN &&
            sportFacilities[facilityName].status == status.OPEN,
            "Sport Facility or Court status is not OPEN (system)"
        );

        return(sportFacilities[facilityName].courts[courtName].earliestTime, sportFacilities[facilityName].courts[courtName].latestTime);
    }
}