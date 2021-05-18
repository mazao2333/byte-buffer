package com.pcl.bytebuffer;

public interface WriteOnlyByteBuffer extends BasicByteBuffer {

    WriteOnlyByteBuffer append(byte[] v);

    WriteOnlyByteBuffer append(String v);

    WriteOnlyByteBuffer append(int v);

}