package forezp.com.selectpicandcamera;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity  implements EasyPermissions.PermissionCallbacks {

    private Button select_album ;
    private ImageView imageview;
    private Button take_photo;
    private final int RC_CAMERA_PERM = 123;
    private final int RC_ALBUM_PERM = 124;
    private String mPhotoPath;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        select_album=(Button)findViewById(R.id.select_album);
        imageview=(ImageView)findViewById(R.id.imageview) ;
        select_album.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (EasyPermissions.hasPermissions(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    toAlbum();
                } else {
                    EasyPermissions.requestPermissions(MainActivity.this, "", RC_ALBUM_PERM, Manifest.permission.READ_EXTERNAL_STORAGE);
                }
            }
        });

        take_photo=(Button)findViewById(R.id.take_photo);
        take_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (EasyPermissions.hasPermissions(MainActivity.this, Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    toCamera();
                } else {
                    EasyPermissions.requestPermissions(MainActivity.this, "", RC_CAMERA_PERM, Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE);
                }
            }
        });
    }

    private void toCamera() {
        // 判断是否挂载了SD卡
        String savePath = "";
        String storageState = Environment.getExternalStorageState();
        if (storageState.equals(Environment.MEDIA_MOUNTED)) {
            savePath = Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + "/oschina/Camera/";
            File savedir = new File(savePath);
            if (!savedir.exists()) {
                savedir.mkdirs();
            }
        }

        // 没有挂载SD卡，无法保存文件
        if (TextUtils.isEmpty(savePath)) {
           // AppContext.showToastShort("无法保存照片，请检查SD卡是否挂载");
            return;
        }

        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String fileName = timeStamp + ".jpg";// 照片命名
        File out = new File(savePath, fileName);
        Uri uri = Uri.fromFile(out);
        //tweet.setImageFilePath(savePath + fileName); // 该照片的绝对路径
        mPhotoPath=savePath + fileName;
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(intent, ImageUtils.REQUEST_CODE_GETIMAGE_BYCAMERA);
    }
    /**
     * 进入图库
     * requestCode = REQUEST_CODE_GETIMAGE_BYSDCARD;
     */

    private void toAlbum() {
        Intent intent;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "选择图片"), ImageUtils.REQUEST_CODE_GETIMAGE_BYSDCARD);
        } else {
            intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "选择图片"), ImageUtils.REQUEST_CODE_GETIMAGE_BYSDCARD);
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK)
            return;
        if (requestCode == ImageUtils.REQUEST_CODE_GETIMAGE_BYSDCARD) {
            if (data == null)
                return;
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                String path = ImageUtils.getImagePath(selectedImageUri, this);
                setImageFromPath(path);
            }
        } else if (requestCode == ImageUtils.REQUEST_CODE_GETIMAGE_BYCAMERA) {
            //setImageFromPath(tweet.getImageFilePath());
            setImageFromPath(mPhotoPath);


        }
    }


    /**
     * 根据文件路径上传动弹图片
     *
     * @param path 图片在本地的路径
     */
    private void setImageFromPath(final String path) {
        if (TextUtils.isEmpty(path)) {
            return;
        }
        int degree =ImageUtils.readPictureDegree(path);
        try {
            Bitmap bitmap = BitmapCreate.bitmapFromStream(new FileInputStream(path), 512, 512);
            if(degree!=0){
                Bitmap newbitmap = ImageUtils.rotaingImageView(degree, bitmap);
                setImageFromBitmap(newbitmap);
                bitmap.recycle();
                newbitmap.recycle();
            }else {
                setImageFromBitmap(bitmap);
                // 本地图片在这里销毁
                bitmap.recycle();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据bitmap上传动弹图片
     *
     * @param bitmap bitmap
     */
    private void setImageFromBitmap(final Bitmap bitmap) {
        if (bitmap == null) return;
        String temp = FileUtils.getSDCardPath() + "/OSChina/tempfile.png";
        FileUtils.bitmapToFile(bitmap, temp);
       // tweet.setImageFilePath(temp);

        // 压缩小图片用于界面显示
      //  Bitmap minBitmap = ImageUtils.zoomBitmap(bitmap, 100, 100);
        Bitmap minBitmap = ImageUtils.zoomBitmap(bitmap, 300, 300);
        // 销毁之前的图片
        // 这里销毁会导致动弹界面图片无法重新预览,KJ框架问题
        // bitmap.recycle();

        imageview.setImageBitmap(minBitmap);
        imageview.setVisibility(View.VISIBLE);
    }



    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        // int tipres = R.string.pub_tweet_required_album_tip;
        if (perms.get(0).equals(Manifest.permission.CAMERA)) {
            //  tipres = R.string.pub_tweet_required_camera_tip;
        } else if (perms.get(0).equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            //  tipres = R.string.pub_tweet_required_album_tip;
        }
        // String tip = getString(tipres);
        // 权限被拒绝了
        //  DialogHelp.getConfirmDialog(this,
        //          "权限申请",
        //        tip,
        //         "去设置",
        //        "取消",
        //        new DialogInterface.OnClickListener() {
        //           @Override
        //            public void onClick(DialogInterface dialog, int which) {
        //                startActivity(new Intent(Settings.ACTION_APPLICATION_SETTINGS));
        //         }
        //      },
        //        null).show();
    }
}
