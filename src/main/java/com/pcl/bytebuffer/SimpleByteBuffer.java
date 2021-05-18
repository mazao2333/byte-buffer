package com.pcl.bytebuffer;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.pcl.tool.NumberConverter.*;

/**
 * 实现了只读和只写buffer接口，实际上就是懒得单独写
 */
public class SimpleByteBuffer implements ReadOnlyByteBuffer, WriteOnlyByteBuffer {

    private byte[] bytes;
    private int index;

    public SimpleByteBuffer(byte[] bytes) {
        this.bytes = bytes;
        this.index = 0;
    }

    public SimpleByteBuffer(String str) {
        this.bytes = str.getBytes(StandardCharsets.US_ASCII);
        this.index = 0;
    }

    public byte[] getBytes() {
        return this.bytes;
    }

    public byte[] getBytes(int l) {
        return Arrays.copyOf(this.bytes, l);
    }

    public byte[] getBytes(int from, int to) {
        return Arrays.copyOfRange(this.bytes, from, to);
    }

    public byte[] getReadableBytes() {
        return Arrays.copyOfRange(this.bytes, this.index, this.bytes.length);
    }

    public WriteOnlyByteBuffer append(byte[] v) {
        int o = this.bytes.length;
        int a = v.length;
        int n = o + a;
        byte[] newBytes = new byte[n];
        System.arraycopy(this.bytes, 0, newBytes, 0, o);
        System.arraycopy(v, 0, newBytes, o, a);
        this.bytes = newBytes;
        return this;
    }

    public WriteOnlyByteBuffer append(String v) {
        return append(v.getBytes(StandardCharsets.US_ASCII));
    }

    public WriteOnlyByteBuffer append(int v) {
        return append(int2Bytes(v));
    }

    public int readableBytes() {
        return this.bytes.length - this.index;
    }

    public byte[] readBytes(int l) {
        byte[] v = Arrays.copyOfRange(this.bytes, this.index, this.index + l);
        this.index += l;
        return v;
    }

    public boolean readBoolean() {
        return readableBytes() >= 1 && readByte() == 1;
    }

    public byte readByte() {
        return readableBytes() >= 1 ? readBytes(1)[0] : (byte) 0;
    }

    public short readUnsignedByte() {
        return readableBytes() >= 1 ? (short) (readByte() & 255) : (short) 0;
    }

    public short readShort() {
        return readableBytes() >= 2 ? bytes2Short(readBytes(2)) : (short) 0;
    }

    public int readUnsignedShort() {
        return readableBytes() >= 2 ? readShort() & '\uffff' : 0;
    }

    public int readInt() {
        return readableBytes() >= 4 ? bytes2Int(readBytes(4)) : 0;
    }

    public long readUnsignedInt() {
        return readableBytes() >= 4 ? (long) readInt() & 4294967295L : 0;
    }

    public long readLong() {
        return readableBytes() >= 8 ? bytes2Long(readBytes(8)) : 0;
    }

    public double readDouble() {
        return readableBytes() >= 8 ? bytes2Double(readBytes(8)) : 0;
    }

    /**
     * 读一个复数a+bi
     *
     * @return double数组[a, b]
     */
    public List<Double> readComplex() {
        List<Double> complex = new ArrayList<>();
        if (readableBytes() >= 16) {
            double a = readDouble();
            double b = readDouble();
            complex.add(a);
            complex.add(b);
        }
        return complex;
    }

    public List<Double> readDoubleArray() {
        long l = readUnsignedInt();
        List<Double> doubles = new ArrayList<>();
        if (readableBytes() >= l * 8) {
            for (int i = 0; i < l; i++) {
                double d = readDouble();
                doubles.add(d);
            }
        }
        return doubles;
    }

    public List<Double> readDoubleArray(int sampleRate) {
        if (sampleRate == 1) {
            return readDoubleArray();
        }
        long l = readUnsignedInt();
        List<Double> doubles = new ArrayList<>();
        if (readableBytes() >= l * 8) {
            int tempIndex = this.index;
            for (int i = 0; i < l; i += sampleRate) {
                double d = readDouble();
                doubles.add(d);
                ignore((sampleRate - 1) * 8);
            }
            this.index = tempIndex + (int) l * 8;
        }
        return doubles;
    }

    public List<Double> readSampledWaveform(int sampleRate) {
        if (sampleRate == 1) {
            return readDoubleArray();
        }
        long l = readUnsignedInt();
        List<Double> waveform = new ArrayList<>();
        if (readableBytes() >= l * 8) {
            double t0 = readDouble();
            double deltaT = readDouble();
            waveform.add(t0);
            waveform.add(deltaT);
            l -= 2;
            int tempIndex = this.index;
            for (int i = 0; i < l; i += sampleRate) {
                double value = readDouble();
                waveform.add(value);
                ignore((sampleRate - 1) * 8);
            }
            this.index = tempIndex + (int) l * 8;
        }
        return waveform;
    }

    /**
     * 读普通字符串时用，例如clientName、tagName等
     *
     * @param l 字符串长度
     * @return 常规String
     */
    public String readString(int l) {
        return readableBytes() >= l ? new String(readBytes(l), StandardCharsets.US_ASCII) : null;
    }

    /**
     * 读协议中自定义的字符串时用，会先读一个代表字符串长度的int并舍弃
     *
     * @return 常规String
     */
    public String readFormattedString() {
        long l = readUnsignedInt();
        return readableBytes() >= l ? readString((int) l) : null;
    }

    /**
     * 把剩余的字节全部按照String读出，一般读payload用
     *
     * @return 没有则返回空字符串，由于ConcurrentHashMap对键值的限制，不能返回null
     */
    public String readString2End() {
        return readableBytes() > 0 ? readString(readableBytes()) : "";
    }

    /**
     * 丢弃长度为l的字节，如果剩余字节长度不足l，则全部丢弃
     *
     * @param l 丢弃字节的长度
     */
    public void ignore(int l) {
        if (readableBytes() > l) {
            this.index += l;
        } else {
            this.index = this.bytes.length;
        }
    }

    /**
     * 将字节码以两位十六进制的形式打印出来
     * @return e.g. "00 01 0A 0F 10 1A AA FF"
     */
    @Override
    public String toString() {
        StringBuilder hexString = new StringBuilder();
        if (this.bytes == null || this.bytes.length == 0) {
            return null;
        }
        for (byte b : this.bytes) {
            int v = b & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                hexString.append("0");
            }
            hexString.append(hv);
            hexString.append(" ");
        }
        return hexString.toString().toUpperCase();
    }

}