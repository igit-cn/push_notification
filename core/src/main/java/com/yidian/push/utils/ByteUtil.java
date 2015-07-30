package com.yidian.push.utils;

/**
 * Created by yidianadmin on 14-8-20.
 */
public class ByteUtil {
    private final static char[] mChars = "0123456789abcdef".toCharArray();
    private final static String mHexStr = "0123456789abcdef";

    public static int toInt(byte b) {
        return (int)b & 0XFF;
    }

    public static byte[] subBytes(byte[] bytes, int start, int end) {
        if (start > end) {
            throw new RuntimeException();
        }
        byte[] res = new byte[end-start];
        int index = 0;
        for (int i = start; i < end; i ++) {
            res[index++] = bytes[i];
        }
        return res;
    }

    public static byte[] subBytes(byte[] bytes, int start) {
        return subBytes(bytes, start, bytes.length);
    }

    public static long unsigned4BytesToInt(byte[] buf, int pos) {
        int firstByte = 0;
        int secondByte = 0;
        int thirdByte = 0;
        int fourthByte = 0;
        int index = pos;
        firstByte = (0x000000FF & ((int) buf[index]));
        secondByte = (0x000000FF & ((int) buf[index + 1]));
        thirdByte = (0x000000FF & ((int) buf[index + 2]));
        fourthByte = (0x000000FF & ((int) buf[index + 3]));
        return ((long) (firstByte << 24 | secondByte << 16 | thirdByte << 8 | fourthByte)) & 0xFFFFFFFFL;
    }

    public static String byte2HexStr(byte[] b, int begin, int end){
        StringBuilder sb = new StringBuilder();
        for (int n=begin; n < end; n++){
            sb.append(mChars[(b[n] & 0xFF) >> 4]);
            sb.append(mChars[b[n] & 0x0F]);
        }
        return sb.toString();
    }

    public static byte[] hexStr2Bytes(String hexStr) {
        if (null == hexStr || hexStr.isEmpty() || hexStr.length()%2 != 0) {
            return new byte[0];
        }
        hexStr = hexStr.toLowerCase();
        int len = hexStr.length()/2;
        byte[] bytes = new byte[len];
        for (int i = 0; i < len; i ++) {
            bytes[i] = (byte)(charToByte((char)hexStr.charAt(2*i)) << 4 | charToByte((char)hexStr.charAt(2*i+1)));
        }
        return bytes;
    }

    public static String bytesToString(byte[] bytes) {
        if (null == bytes) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (byte by : bytes) {
            sb.append((char)by);
        }
        return sb.toString();
    }

    private static byte charToByte(char c) {
        return (byte) mHexStr.indexOf(c);
    }

    public static byte[] intToByteArray(int i) {
        byte[] result = new byte[4];
        //from high to low
        result[0] = (byte)((i >> 24) & 0xFF);
        result[1] = (byte)((i >> 16) & 0xFF);
        result[2] = (byte)((i >> 8) & 0xFF);
        result[3] = (byte)(i & 0xFF);
        return result;
    }

    public static void main(String[] args) {
        String str = "01111111";
        Byte b = Byte.valueOf(str, 2);
        System.out.println(toInt(b));
    }



}
