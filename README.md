# Tkey-jClient
Currently a somewhat messsy code repo which allows one to send apps, and recieve info, to/from a Tillitis TKey (tillitis.se). In effect, this is a Java version of the tkey-client library (see: https://github.com/tillitis/tkeyclient). 

Serial USB communication is achieved through the use of the external library jSerialComm: https://github.com/Fazecast/jSerialComm.

Note: Due to Gradle lacking support for JDK20, JDK19 (or lower) must be used. 

To send apps to the TKey: Build the device app (see https://github.com/tillitis/tillitis-key1-apps), and place the .bin or .S file the root of this program's directory. Specify the loadAppFromFile string in Main, connect a TKey, and then run the program.

To Do: 

a. Code Cleanup. Particularly in regards to duplicate code. 

b. Implement a couple more methods which are present in the original tkey-client library.

c. Implement UDI get method(s).

d. Documentation.
