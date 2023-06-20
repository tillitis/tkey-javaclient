# Tkey-jClient
This is currently a somewhat messsy code repo which allows one to send apps, and recieve info, to/from a Tillitis TKey (tillitis.se). In effect, this is a Java version of the tkey-client library (see: https://github.com/tillitis/tkeyclient). 

## Usage
There are two ways to send apps and communicate with a TKey using this library/project:

### 1: Using this library as a stand-alone program

1. Build the TKey device app (see https://github.com/tillitis/tillitis-key1-apps).
2. Place the .bin file in the root of this program's directory (not required, but makes life easier).
3. Specify the filepath as the input in the method call loadAppFromFile in main()
4. Connect a TKey
5. Run the program.

Note: If you simply wish to get the TKey name or UDI, call getNameVersion() and/or getUDI() respectively, and comment out the method for app loading.

### 2. Using this library with TKeyJGUI

1. Clone the TKeyJGUI found at: https://github.com/iknek/TKeyJGUI
2. Add the jSerialComm library and this library as local libraries.
3. Start the GUI and load apps/get Tkey information through it.

## Good To Know

- Serial USB communication is achieved through the use of the external library jSerialComm: https://github.com/Fazecast/jSerialComm. This library jar is already placed in the root of this library folder.
- Due to Gradle lacking support for JDK20, JDK19 (or lower) must be used.
  
## To Do

a. Minor code cleanup. Particularly in regards to unnecessary code. 

b. Documentation.

c. Implement the Blake2s & USS parts of the code.
