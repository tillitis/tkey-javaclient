package com.knek;

public class UDI {

    private int vpr;
    private int unnamed;
    private int vendorID;
    private int productID;
    private int productRevision;
    private int serial;

    public UDI(int vpr, int unnamed, int vendorID, int productID, int productRevision, int serial) {
        this.vpr = vpr;
        this.unnamed = unnamed;
        this.vendorID = vendorID;
        this.productID = productID;
        this.productRevision = productRevision;
        this.serial = serial;
    }

    public int getVpr() {
        return vpr;
    }

    public int getUnnamed() {
        return unnamed;
    }

    public int getVendorID() {
        return vendorID;
    }

    public int getProductID() {
        return productID;
    }

    public int getProductRevision() {
        return productRevision;
    }

    public int getSerial() {
        return serial;
    }
}
