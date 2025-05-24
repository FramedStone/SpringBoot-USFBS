// SPDX-License-Identifier: MIT 
pragma solidity 0.8.19;

library Strings {
    // Converts a `uint256` to its ASCII `string` decimal representation.
    function toString(uint256 value) internal pure returns (string memory) {
        if (value == 0) {
            return "0";
        }
        uint256 temp = value;
        uint256 digits;
        while (temp != 0) {
            digits++;
            temp /= 10;
        }
        bytes memory buffer = new bytes(digits);
        while (value != 0) {
            digits -= 1;
            buffer[digits] = bytes1(uint8(48 + uint256(value % 10)));
            value /= 10;
        }
        return string(buffer);
    }

    // Converts a `uint256` to 24 hours time format
    function uintTo24Hour(uint256 secs) internal pure returns (string memory) {
        uint256 hh = secs / 3600;
        uint256 mm = (secs % 3600) / 60;
        return string.concat(_padZero(hh), ":", _padZero(mm));
    }

    // Helper function to pad zero to strings
    function _padZero(uint256 v) internal pure returns (string memory) {
        string memory s = toString(v);
        if (v < 10) {
            return string.concat("0", s);
        }
        return s;
    }
}
