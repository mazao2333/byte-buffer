package com.pcl.bytebuffer;

import java.util.List;

public interface ReadOnlyByteBuffer extends BasicByteBuffer {

    byte[] getReadableBytes();

    int readableBytes();

    byte[] readBytes(int l);

    boolean readBoolean();

    byte readByte();

    short readUnsignedByte();

    short readShort();

    int readUnsignedShort();

    int readInt();

    long readUnsignedInt();

    long readLong();

    double readDouble();

    List<Double> readComplex();

    List<Double> readDoubleArray();

    List<Double> readDoubleArray(int sampleRate);

    List<Double> readSampledWaveform(int sampleRate);

    String readString(int l);

    String readFormattedString();

    String readString2End();

    void ignore(int l);

}