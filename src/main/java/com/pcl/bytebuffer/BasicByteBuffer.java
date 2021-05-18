package com.pcl.bytebuffer;

public interface BasicByteBuffer {

    byte[] getBytes();

    byte[] getBytes(int l);

    byte[] getBytes(int from, int to);

}