package com.knek;

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
        return switch (this) {
            case CmdLen1 -> 1;
            case CmdLen4 -> 4;
            case CmdLen32 -> 32;
            case CmdLen128 -> 128;
        };
    }

    public int getByteVal(){
        return switch (this) {
            case CmdLen1 -> 0;
            case CmdLen4 -> 1;
            case CmdLen32 -> 2;
            case CmdLen128 -> 3;
        };
    }
}
