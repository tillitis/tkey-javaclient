public enum CmdLen {
    CmdLen1(0),
    CmdLen4(1),
    CmdLen32(2),
    CmdLen128(3);

    private final int value;

    CmdLen(int value) {
        this.value = value;
    }

    public int getBytelen() {
        switch (this) {
            case CmdLen1:
                return 1;
            case CmdLen4:
                return 4;
            case CmdLen32:
                return 32;
            case CmdLen128:
                return 128;
        }
        return 0;
    }

    public int getByteVal(){
        switch (this) {
            case CmdLen1:
                return 0;
            case CmdLen4:
                return 1;
            case CmdLen32:
                return 2;
            case CmdLen128:
                return 3;
        }
        return 0;
    }
}
