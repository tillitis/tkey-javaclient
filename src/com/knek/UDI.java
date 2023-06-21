package com.knek;

record UDI(int vpr, int unnamed, int vendorID, int productID, int productRevision, int serial, short[] udi) {
}

record FwCmd(int code, String name, CmdLen cmdLen, byte endpoint) {
    public FwCmd {
        endpoint = 2;
    }
}
