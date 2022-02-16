package com.mjsoftking.tcpserviceapp.test;

/**
 * 用途：
 * <p>
 * 作者：MJSoftKing
 */
public class CommonDataUtils {


    /**
     * 快速截取数组的某一段。
     *
     * @param size   新数组的长度
     * @param parent 要被切割的数组
     * @param begin  要被切割的数组的起始位置
     * @return
     */
    public static byte[] splitByte(int size, byte[] parent, int begin) {
        byte[] bytes = new byte[size];
        System.arraycopy(parent, begin, bytes, 0, bytes.length);
        return bytes;
    }

    public static byte[] concatByte(int size, byte[] addsize, int begin) {
        byte[] bytes = new byte[size];
        System.arraycopy(addsize, begin, bytes, 0, bytes.length);
        return bytes;


    }

    /**
     * 十六进制字符串转换为字节数组
     *
     * @param hex
     * @return
     */
    public static int[] hexStringToByte4(String hex) {
        int len = (hex.length() / 4);
        int[] result = new int[len];

        char[] achar = hex.toCharArray();
        for (int i = 0; i < len; i++) {
            int pos = i * 4;
            result[i] = 0x0000000000000000;
//            result[i] = (byte) (toByte(achar[pos]) << 16 | toByte(achar[pos + 1]) | toByte(achar[pos + 2]) | toByte(achar[pos + 3]));
            result[i] = result[i] << 4 | toByte(achar[pos]);
            result[i] = result[i] << 4 | toByte(achar[pos + 1]);
            result[i] = result[i] << 4 | toByte(achar[pos + 2]);
            result[i] = result[i] << 4 | toByte(achar[pos + 3]);
        }
        return result;
    }

    private static byte toByte(char c) {
        byte b = (byte) "0123456789ABCDEF".indexOf(c);
        return b;
    }

    /**
     * 十六进制前自动补0(String)
     *
     * @param value
     * @return
     */
    public static String hexAddZer0(int value) {
        String HexString = Integer.toHexString(value);
        while (HexString.length() < 8)
            HexString = "0" + HexString;
        return HexString;
    }

    /**
     * 把字节数组转换成16进制字符串
     *
     * @param bArray byte[] 数组
     * @return bArray为null或者大小为0时返回空字符串
     */
    public static String bytesToHexString(byte... bArray) {
        return bytesToHexString(bArray, "");
    }

    /**
     * 把字节数组转换成16进制字符串
     *
     * @param bArray byte[] 数组
     * @param split  每个byte后增加的分隔符
     * @return bArray为null或者大小为0时返回空字符串
     */
    public static String bytesToHexString(byte[] bArray, String split) {
        return bytesToHexString(bArray, "", split);
    }

    /**
     * 把字节数组转换成16进制字符串
     *
     * @param bArray byte[] 数组
     * @param header 每个byte前增加的前缀
     * @param split  每个byte后增加的分隔符
     * @return bArray为null或者大小为0时返回空字符串
     */
    public static String bytesToHexString(byte[] bArray, String header, String split) {
        if (null == bArray || bArray.length == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder(bArray.length);
        String sTemp;
        for (byte b : bArray) {
            sTemp = Integer.toHexString(0xFF & b);
            if (sTemp.length() < 2) {
                sb.append(header).append(0).append(sTemp.toUpperCase());
            } else {
                sb.append(header).append(sTemp.toUpperCase());
            }
            sb.append(split);
        }
        return sb.substring(0, sb.length() - split.length());
    }

    /**
     * 合并字节数组，参数顺序决定新数组内的数据顺序
     *
     * @param bt 任意数量的数组，按参数顺序合并
     * @return 合并后的总数组
     */
    public static byte[] mergeBytes(byte[]... bt) {
        int length = 0;
        for (byte[] bt1 : bt) {
            length += bt1.length;
        }
        byte[] result = new byte[length];
        int i = 0;

        for (byte[] bt1 : bt) {
            for (byte bt2 : bt1) {
                result[i] = bt2;
                i++;
            }
        }
        return result;
    }

    public static byte[] intToByteArray(int integer) {
        int byteNum = (40 - Integer.numberOfLeadingZeros(integer < 0 ? ~integer : integer)) / 8;
        byte[] byteArray = new byte[4];

        for (int n = 0; n < byteNum; n++)
            byteArray[3 - n] = (byte) (integer >>> (n * 8));

        return byteArray;


    }

    /**
     * 二维测距数组转换为一维算法需求数组
     * 算法要求数组为：1通道第1次测量50次，1通道第2次测量50次...
     *
     * @param data 计算得到的数据二维数组
     * @return 算法要求的一维数组
     */
    public static double[] dataArithmeticConvert(double[][] data) {
        int length = 0;
        for (double[] bt1 : data) {
            length += bt1.length;
        }
        double[] source = new double[length];
        for (int i = 0; i < data.length; ++i) {
            for (int y = 0; y < data[i].length; ++y) {
                source[i + y * data.length] = data[i][y];
            }
        }
        return source;
    }

    /**
     * 将16进制字符串转换为byte数组
     *
     * @param data 16进制字符串，处理时每2字符算一个byte，空格会自动去除
     */
    public static byte[] hexStringToBytes(String data) {
        if (null == data)
            return null;

        data = data.replace(" ", "");
        int length = data.length() / 2;
        byte[] result = new byte[length];
        for (int i = 0; i < length; i++) {
            char hi = data.charAt(i * 2);
            char lo = data.charAt(i * 2 + 1);
            String strKey = hi + "" + lo;
            result[i] = Integer.valueOf(strKey, 16).byteValue();
        }
        return result;
    }


}
