package com.pcl.tool;

/**
 * 大端形式的各种数据类型转字节码
 */
public class NumberConverter {

    private NumberConverter() {
    }

    public static byte[] int2Bytes(int v) {
        byte[] bytes = new byte[4];
        bytes[0] = (byte) ((v >> 24) & 0xff);
        bytes[1] = (byte) ((v >> 16) & 0xff);
        bytes[2] = (byte) ((v >> 8) & 0xff);
        bytes[3] = (byte) (v & 0xff);
        return bytes;
    }

    public static short bytes2Short(byte[] v) {
        if (v.length == 2) {
            return (short) (v[1] & 0xff | (v[0] & 0xff) << 8);
        } else {
            return (short) 0;
        }
    }

    public static int bytes2Int(byte[] v) {
        if (v.length == 4) {
            return v[3] & 0xff | (v[2] & 0xff) << 8 | (v[1] & 0xff) << 16 | (v[0] & 0xff) << 24;
        } else {
            return 0;
        }
    }

    public static long bytes2Long(byte[] v) {
        if (v.length == 8) {
            long num = 0;
            for (int ix = 0; ix < 8; ++ix) {
                num <<= 8;
                num |= (v[ix] & 0xff);
            }
            return num;
        } else {
            return 0;
        }
    }

    public static double bytes2Double(byte[] v) {
        if (v.length == 8) {
            long value = bytes2Long(v);
            return Double.longBitsToDouble(value);
        } else {
            return 0;
        }
    }
}