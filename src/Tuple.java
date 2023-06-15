public class Tuple {
    private int intValue;
    private int[] intArray;

    public Tuple(int[] byteArray, int intValue) {
        this.intArray = byteArray;
        this.intValue = intValue;
    }

    public int[] getIntArray() {
        return intArray;
    }
    public int getIntValue() {
        return intValue;
    }
}
