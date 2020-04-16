package com.sheotest.app.facerecognitiontest;

import android.Manifest;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.sheotest.app.facerecognitiontest.network.RetrofitManager;
import com.sheotest.app.facerecognitiontest.network.model.Faces;
import com.sheotest.app.facerecognitiontest.network.model.NaverRepo;
import com.soundcloud.android.crop.Crop;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    private ImageView iv_photo;
    private String mCurrentPhotoPath;

    private static final int REQUEST_TAKE_PHOTO = 1;
    private static final int REQUEST_GET_GALLERY = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermission();

        findViewById(R.id.bt_camera).setOnClickListener(this);
        findViewById(R.id.bt_gallery).setOnClickListener(this);
        findViewById(R.id.bt_request).setOnClickListener(this);
        iv_photo = findViewById(R.id.iv_photo);
    }

    private void checkPermission() {

        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {

            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {

            }
        };

        TedPermission.with(this)
                .setPermissionListener(permissionListener)
                .setRationaleMessage("사진 및 파일을 저장하기 위하여 접근 권한이 필요합니다.")
                .setDeniedMessage("[설정] > [권한] 에서 권한을 허용할 수 있습니다.")
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                .check();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_camera:
//                getImageFromCamera();
//                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
//                startActivityForResult(cameraIntent, REQUEST_TAKE_PHOTO);

                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // Ensure that there's a camera activity to handle the intent
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    // Create the File where the photo should go
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {
                        // Error occurred while creating the File
                    }
                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        Uri photoURI = FileProvider.getUriForFile(this,
                                "com.sheotest.app.facerecognitiontest.fileprovider",
                                photoFile);
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                    }
                }

                break;

//            case R.id.bt_gallery:
//
//                File photoFile = null;
//                try {
//                    photoFile = createImageFile();
//                } catch (IOException ex) {
//                    // Error occurred while creating the File
//                }
//
//                Intent intent = new Intent(Intent.ACTION_PICK);
//                intent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
//                startActivityForResult(intent, REQUEST_GET_GALLERY);
//
//                break;

            case R.id.bt_request:
                if(mCurrentPhotoPath == null) {
                    Toast.makeText(getApplicationContext(), "사진촬영을 진행해주세요.", Toast.LENGTH_LONG).show();
                    return;
                }
                File file = new File(mCurrentPhotoPath);
                uploadImage(file);
                break;
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println("[sheotest] data: " + data);
        try {
            switch (requestCode) {
                case REQUEST_TAKE_PHOTO: {
                    if (resultCode == RESULT_OK) {
                        File file = new File(mCurrentPhotoPath);
                        System.out.println("[sheotest] 1111 " + file.getPath());
                        Uri photoUri = Uri.fromFile(file);
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), photoUri);
                        if (bitmap != null) {
                            cropImage(photoUri);
                        }
                    }
                    break;
                }

                case Crop.REQUEST_CROP: {
                    Uri cropUri = Crop.getOutput(data);
                    System.out.println("[sheotest] getOutput uri: " + cropUri);
                    File cropFile = new File(cropUri.getPath());
                    System.out.println("[sheotest] 2222 " + cropFile.getPath());
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), cropUri);
                    iv_photo.setImageBitmap(getRotateImage(bitmap)); // 이미지 출력
                    // iv_photo.setImageURI(cropUri);

                    checkFileSize(cropFile);
                    resizeAndSave(cropUri, 512);
                    checkFileSize(cropFile);
                    System.out.println("[sheotest] mCurrentPhotoPath: " + mCurrentPhotoPath);
                }

                case REQUEST_GET_GALLERY: {
                    if (resultCode == RESULT_OK && data.getData() != null) {

                        Uri uri = data.getData();
                        cropImage(uri);

//                        File file = new File(getRealPathFromURI(uri));
//                        checkFileSize(file);
//                        Bitmap temp = resizeAndSave(uri, 512);
//                        mCurrentPhotoPath = file.getAbsolutePath();
//                        System.out.println("[sheotest] mCurrentPhotoPath: " + mCurrentPhotoPath);
//                        iv_photo.setImageBitmap(temp); // 이미지 출력
                    }
                    break;
                }
            }

        } catch (Exception error) {
            error.printStackTrace();
        }
    }

    private void checkFileSize(File file) {
        System.out.println("[sheotest] 파일체크");
        String strFileSize = null;
        long lFileSize;

        if (file.exists()) {
            lFileSize = file.length();
            strFileSize = lFileSize + " bytes";
        } else {
            strFileSize = "파일없음";
        }

        System.out.println("[sheotest] strFileSize: " + strFileSize);
    }

    private Bitmap getRotateImage(Bitmap image) throws IOException {
        ExifInterface ei = new ExifInterface(mCurrentPhotoPath);
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
        Bitmap rotatedBitmap;

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                rotatedBitmap = rotateImage(image, 90);
                break;

            case ExifInterface.ORIENTATION_ROTATE_180:
                rotatedBitmap = rotateImage(image, 180);
                break;

            case ExifInterface.ORIENTATION_ROTATE_270:
                rotatedBitmap = rotateImage(image, 270);
                break;

            case ExifInterface.ORIENTATION_NORMAL:
            default:
                rotatedBitmap = image;
        }
        return rotatedBitmap;
    }

    private Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    private void cropImage(Uri photoUri) {
        //크롭 후 저장할 Uri
        Uri savingUri = photoUri;
        Crop.of(photoUri, savingUri).asSquare().start(this);
    }

    @NonNull
    private MultipartBody.Part prepareFilePart(String partName, File file) {
//        File file = new File(getRealPathFromURI(fileUri));
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        return MultipartBody.Part.createFormData(partName, file.getName(), requestFile);
    }

    private void uploadImage(File file) {

        MultipartBody.Part body = prepareFilePart("image", file);

        Call<NaverRepo> call = RetrofitManager.getService().test2("lL1DuMSVssWahYB80wAX", "jU5MU86HyE", body);
        call.enqueue(new Callback<NaverRepo>() {
            @Override
            public void onResponse(Call<NaverRepo> call, Response<NaverRepo> response) {
                System.out.println("[sheotest] onResponse: " + response);
                if (response.isSuccessful()) {

                    System.out.println("[sheotest] getInfo: " + response.body().getInfo());
                    System.out.println("[sheotest] getInfo: " + response.body().getInfo().getFacecount());
                    System.out.println("[sheotest] getInfo: " + response.body().getInfo().getSize());

                    if (response.body().getInfo().getFacecount() > 0) {
                        Faces[] faces = response.body().getFaces();
                        String message = "";
                        for (Faces face : faces) {
                            System.out.println("[sheotest] face: " + face.getAge().getValue());
                            System.out.println("[sheotest] face: " + face.getEmotion().getValue());
                            System.out.println("[sheotest] face: " + face.getGender().getValue());
                            System.out.println("[sheotest] face: " + face.getPose().getValue());

                            message += "age: ";
                            message += face.getAge().getValue();
                            message += "\nemotion: ";
                            message += face.getEmotion().getValue();
                            message += "\ngender: ";
                            message += face.getGender().getValue();
                        }

                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "얼굴을 찾을수 없습니다.", Toast.LENGTH_LONG).show();
                    }

                } else {
                    Toast.makeText(getApplicationContext(), "onResponse: " + response, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call call, Throwable t) {
                System.out.println("[sheotest] onFailure: " + t);
                Toast.makeText(getApplicationContext(), "onFailure", Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean resizeAndSave(Uri uri, int resize) {
        Bitmap resizeBitmap = null;
        boolean isSuccess = false;

        BitmapFactory.Options options = new BitmapFactory.Options();
        try {
            BitmapFactory.decodeStream(getApplicationContext().getContentResolver().openInputStream(uri), null, options); // 1번

            int width = options.outWidth;
            int height = options.outHeight;
            int samplesize = 2;

            while (true) {// 2번
                if (width / 2 < resize || height / 2 < resize)
                    break;
                width /= 2;
                height /= 2;
                samplesize *= 2;
            }

            System.out.println("[sheotest] samplesize: " + samplesize);
            options.inSampleSize = samplesize;
            Bitmap bitmap = BitmapFactory.decodeStream(getApplicationContext().getContentResolver().openInputStream(uri), null, options); // 3번
            resizeBitmap = bitmap;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // save
        File fileCacheItem = new File(mCurrentPhotoPath);
        OutputStream out = null;
        try {
            fileCacheItem.createNewFile();
            out = new FileOutputStream(fileCacheItem);
            resizeBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            isSuccess = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return isSuccess;
        }
    }

    public String getRealPathFromURI(Uri contentUri) {

        String[] proj = {MediaStore.Images.Media.DATA};

        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        cursor.moveToNext();
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
        Uri uri = Uri.fromFile(new File(path));

        System.out.println("getRealPathFromURI(), path : " + uri.toString());

        cursor.close();
        return path;
    }

}
