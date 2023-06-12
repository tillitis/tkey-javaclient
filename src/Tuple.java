public class Tuple {

    private int intValue;
    private int[] intArray;

    private byte[] byteArray;
    private FramingHdr hdr;

    public Tuple(int[] byteArray, int intValue) {
        this.intArray = byteArray;
        this.intValue = intValue;
    }

    public Tuple(byte[] byteArray, FramingHdr hdr) {
        this.byteArray = byteArray;
        this.hdr = hdr;
    }

    public Tuple(int[] intArray, FramingHdr hdr) {
        this.intArray = intArray;
        this.hdr = hdr;
    }

    public int[] getIntArray() {
        return intArray;
    }

    public byte[] getByteArray(){
        return byteArray;
    }

    public FramingHdr getHdr() {
        return hdr;
    }

    public int getIntValue() {
        return intValue;
    }
}
