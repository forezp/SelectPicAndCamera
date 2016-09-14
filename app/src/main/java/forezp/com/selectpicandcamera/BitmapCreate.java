package forezp.com.selectpicandcamera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.InputStream;

/**
 * Created by Administrator on 2016/9/14 0014.
 */
public class BitmapCreate {

    /**
     * 获取一个指定大小的bitmap<br>
     * 实际调用的方法是bitmapFromByteArray(data, 0, data.length, w, h);
     *
     * @param is        从输入流中读取Bitmap
     * @param reqWidth  目标宽度
     * @param reqHeight 目标高度
     */
    public static Bitmap bitmapFromStream(InputStream is, int reqWidth,
                                          int reqHeight) {
        if (reqHeight == 0 || reqWidth == 0) {
            try {
                return BitmapFactory.decodeStream(is);
            } catch (OutOfMemoryError e) {
            }
        }
        byte[] data = FileUtils.input2byte(is);
        return bitmapFromByteArray(data, 0, data.length, reqWidth, reqHeight);
    }

    /**
     * 获取一个指定大小的bitmap
     *
     * @param data      Bitmap的byte数组
     * @param offset    image从byte数组创建的起始位置
     * @param length    the number of bytes, 从offset处开始的长度
     * @param reqWidth  目标宽度
     * @param reqHeight 目标高度
     */
    public static Bitmap bitmapFromByteArray(byte[] data, int offset,
                                             int length, int reqWidth, int reqHeight) {
        if (reqHeight == 0 || reqWidth == 0) {
            try {
                return BitmapFactory.decodeByteArray(data, offset, length);
            } catch (OutOfMemoryError e) {
            }
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inPurgeable = true;
        BitmapFactory.decodeByteArray(data, offset, length, options);
        options = calculateInSampleSize(options, reqWidth, reqHeight);
        return BitmapFactory.decodeByteArray(data, offset, length, options);
    }

    /**
     * 图片压缩处理（使用Options的方法）
     * <br>
     * <b>说明</b> 使用方法：
     * 首先你要将Options的inJustDecodeBounds属性设置为true，BitmapFactory.decode一次图片 。
     * 然后将Options连同期望的宽度和高度一起传递到到本方法中。
     * 之后再使用本方法的返回值做参数调用BitmapFactory.decode创建图片。
     * <br>
     * <b>说明</b> BitmapFactory创建bitmap会尝试为已经构建的bitmap分配内存
     * ，这时就会很容易导致OOM出现。为此每一种创建方法都提供了一个可选的Options参数
     * ，将这个参数的inJustDecodeBounds属性设置为true就可以让解析方法禁止为bitmap分配内存
     * ，返回值也不再是一个Bitmap对象， 而是null。虽然Bitmap是null了，但是Options的outWidth、
     * outHeight和outMimeType属性都会被赋值。
     *
     * @param reqWidth  目标宽度,这里的宽高只是阀值，实际显示的图片将小于等于这个值
     * @param reqHeight 目标高度,这里的宽高只是阀值，实际显示的图片将小于等于这个值
     */
    public static BitmapFactory.Options calculateInSampleSize(
            final BitmapFactory.Options options, final int reqWidth,
            final int reqHeight) {
        // 源图片的高度和宽度
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            // 计算出实际宽高和目标宽高的比率
            final int heightRatio = Math.round((float) height
                    / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            // 选择宽和高中最小的比率作为inSampleSize的值，这样可以保证最终图片的宽和高
            // 一定都会大于等于目标的宽和高。
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        // 设置压缩比例
        options.inSampleSize = inSampleSize;
        options.inJustDecodeBounds = false;
        return options;
    }

}
