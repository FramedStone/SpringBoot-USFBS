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

    // Converts an `address` to its ASCII `string` hexadecimal representation.
    function toString(address addr) internal pure returns (string memory) {
        return toHexString(uint256(uint160(addr)), 20);
    }

    // Converts a `uint256` to its ASCII `string` hexadecimal representation with fixed length.
    function toHexString(uint256 value, uint256 length) internal pure returns (string memory) {
        bytes memory buffer = new bytes(2 * length + 2);
        buffer[0] = "0";
        buffer[1] = "x";
        for (uint256 i = 2 * length + 1; i > 1; --i) {
            buffer[i] = _HEX_SYMBOLS[value & 0xf];
            value >>= 4;
        }
        require(value == 0, "Strings: hex length insufficient");
        return string(buffer);
    }

    // Hexadecimal character set
    bytes16 private constant _HEX_SYMBOLS = "0123456789abcdef";

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
