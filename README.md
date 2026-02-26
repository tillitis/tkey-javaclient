# Tkey-jClient

*WARNING*: Unsupported and unmaintained.

This is currently a somewhat messsy code repo which allows one to send apps, and recieve info, to/from a [Tillitis TKey](https://www.tillitis.se). In effect, this is a Java version of the [tkey-client Golang package](https://github.com/tillitis/tkeyclient). 

## Licenses and SPDX tags

Unless otherwise noted, the project sources are licensed under the
terms and conditions of the "GNU General Public License v2.0 only":

> Copyright Tillitis AB.
>
> These programs are free software: you can redistribute it and/or
> modify it under the terms of the GNU General Public License as
> published by the Free Software Foundation, version 2 only.
>
> These programs are distributed in the hope that they will be useful,
> but WITHOUT ANY WARRANTY; without even the implied warranty of
> MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
> General Public License for more details.

> You should have received a copy of the GNU General Public License
> along with this program. If not, see:
>
> https://www.gnu.org/licenses

See [LICENSE](LICENSE) for the full GPLv2-only license text.

External source code we have imported are isolated in their own
directories. They may be released under other licenses. This is noted
with a similar `LICENSE` file in every directory containing imported
sources.

The project uses single-line references to Unique License Identifiers
as defined by the Linux Foundation's [SPDX project](https://spdx.org/)
on its own source files, but not necessarily imported files. The line
in each individual source file identifies the license applicable to
that file.

The current set of valid, predefined SPDX identifiers can be found on
the SPDX License List at:

https://spdx.org/licenses/

## Usage
There are two ways to send apps and communicate with a TKey using this library/project:

### 1: As a stand-alone program

1. Build the TKey device app, [see here for more information](https://github.com/tillitis/tillitis-key1-apps). Note: Official documention does not currently exist for building device apps on Windows. Use WSL.
2. Place the .bin file in the root of this program's directory (not required, but makes life easier).
3. Specify the filepath as the input in the method call loadAppFromFile in main().
4. Connect a TKey.
5. Run the program.

Note: If you simply wish to get the TKey name or UDI, call getNameVersion() and/or getUDI() respectively, and comment out the method for app loading.

### 2. As a library
1. Clone this library and run the command ```.\gradlew build``` to build the jar file. Copy it from ```tkeyclient\build\libs```. 

## Good To Know
- Due to Gradle lacking support for JDK20, JDK19 (or lower, 17 has been tested) must be used.
  
## To Do

a. Continuous code cleanup. Particularly in regards to unnecessary code. 

b. Improve error handling.
