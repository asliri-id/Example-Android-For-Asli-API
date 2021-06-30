package com.yusuffirdaus.aslirisalesdemo.ui.liveness;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.Drawable;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import androidx.recyclerview.widget.RecyclerView;

import com.github.ybq.android.spinkit.sprite.Sprite;
import com.github.ybq.android.spinkit.style.Circle;
import com.github.ybq.android.spinkit.style.FadingCircle;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.yusuffirdaus.aslirisalesdemo.R;
import com.yusuffirdaus.aslirisalesdemo.api.APIrequestDdata;
import com.yusuffirdaus.aslirisalesdemo.api.RetroServer;
import com.yusuffirdaus.aslirisalesdemo.model.DataCode;
import com.yusuffirdaus.aslirisalesdemo.model.Liveness;
import com.yusuffirdaus.aslirisalesdemo.utils.FileUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LivenessFragment extends Fragment {


    private List<DataCode> DataCodes = new ArrayList<>();
    ProgressDialog pDialog;
    private String TAG = "ysf";
    private String base64_ktp = "";
    private static String imageString = "";
    private Button bProcess, bOk;
    private TextureView textureView;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    private ImageReader imageReader;
    private File file;
    List<Uri> files = new ArrayList<>();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 270);
//        ORIENTATIONS.append(Surface.ROTATION_90, 90);
//        ORIENTATIONS.append(Surface.ROTATION_180, 180);
//        ORIENTATIONS.append(Surface.ROTATION_270, 270);
    }

    private String cameraId;
    protected CameraDevice cameraDevice;
    protected CameraCaptureSession cameraCaptureSessions;
    protected CaptureRequest captureRequest;
    protected CaptureRequest.Builder captureRequestBuilder;
    private Size imageDimension;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;

    private BottomSheetDialog bsOcr;
    private RecyclerView rvData;
    private RecyclerView.LayoutManager lmData;
    private RecyclerView.Adapter adData;
    private ImageView iKtp, iFace, ivOverlay, ivExample, ivExampleTop, ivSuccess;
    private TextView tFace, tvCounter, tvLltop, tDescription, tTitle;
    private LinearLayout llMidle, llTop;
    private CountDownTimer cTimer = null;
    private Integer gesture;
    private ProgressBar progressBar, pbPercentage;
    private Circle mCircleDrawable;
    private FadingCircle mFadingCircle;
    private Boolean isSucceesed = false;

    private AlertDialog dialogSuccess;
    LayoutInflater inflater;
    View dialogView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_liveness, container, false);
        pDialog = new ProgressDialog(getContext());
        pDialog.setTitle("Loading Data");
        bProcess = root.findViewById(R.id.bProcess);
        llMidle = root.findViewById(R.id.llMidle);
        llTop = root.findViewById(R.id.llTop);
        ivExample = root.findViewById(R.id.ivExample);
        ivExampleTop = root.findViewById(R.id.ivExampleTop);
        tvCounter = root.findViewById(R.id.tvCounter);
        tvLltop = root.findViewById(R.id.tvLltop);
        progressBar = root.findViewById(R.id.spin_kit);
        pbPercentage = root.findViewById(R.id.pbPercentage);
        Sprite doubleBounce = new FadingCircle();
        progressBar.setIndeterminateDrawable(doubleBounce);
        textureView = (TextureView) root.findViewById(R.id.texture);
        assert textureView != null;
        textureView.setSurfaceTextureListener(textureListener);
//        llMidle.setVisibility(View.GONE);
        mCircleDrawable = new Circle();
        mCircleDrawable.setBounds(0, 0, 70, 70);
        mCircleDrawable.setColor(Color.WHITE);

        bProcess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String buttonName = bProcess.getText().toString();
                if (buttonName.equalsIgnoreCase("next")) {
                    bProcess.setText("START");
                    gesture = getGestureset();
                    if (gesture == 2 || gesture == 1) gesture = 3;
                    setCommand(gesture);
                    ivExampleTop.setVisibility(View.VISIBLE);
                } else if (buttonName.equalsIgnoreCase("START")) {
                    startTimer();
                    bProcess.setCompoundDrawables(mCircleDrawable, null, null, null);
                    mCircleDrawable.start();
                    bProcess.setEnabled(false);
                    bProcess.setText("Loading");
                }
            }
        });
        return root;
    }

    //show result when taking picture process done
    private void DialogForm() {
        files.clear();
        dialogSuccess = new AlertDialog.Builder(getContext()).create();
        inflater = getLayoutInflater();
        dialogView = inflater.inflate(R.layout.modal_success, null);

        dialogSuccess.setView(dialogView);
        dialogSuccess.setCancelable(true);
        bOk = dialogView.findViewById(R.id.bOk);
        tTitle = dialogView.findViewById(R.id.tTitle);
        ivSuccess = dialogView.findViewById(R.id.ivSuccess);
        tDescription = dialogView.findViewById(R.id.tDescription);


        if (isSucceesed) {
            tTitle.setText("Liveness Success");
            tDescription.setVisibility(View.GONE);
            bOk.setText("OK");
            ivSuccess.setImageDrawable(getActivity().getDrawable(R.drawable.ic_baseline_check_green));
        } else {
            tTitle.setText("Liveness Failed");
            tDescription.setVisibility(View.VISIBLE);
            bOk.setText("Retry");
            ivSuccess.setImageDrawable(getActivity().getDrawable(R.drawable.ic_baseline_cancel_24));
        }

        bOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogSuccess.dismiss();
                bProcess.setCompoundDrawables(null, null, null, null);
                mCircleDrawable.stop();
                bProcess.setEnabled(true);
                bProcess.setText("next");
                progressBar.setVisibility(View.GONE);
                tvLltop.setText("Pastikan wajah tepat ditengah layar, Kemudian ikuti instruksi berikutnya");
                ivExampleTop.setVisibility(View.GONE);
                pbPercentage.setProgress(0);

            }
        });
        dialogSuccess.setCancelable(false);
        dialogSuccess.show();
    }

    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            //open your camera here
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            // Transform you image captured size according to the surface width and height
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    };
    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            //This is called when the camera is open
            Log.e(TAG, "onOpened");
            cameraDevice = camera;
            createCameraPreview();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            camera.close();
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            camera.close();
            cameraDevice = null;
        }
    };
    final CameraCaptureSession.CaptureCallback captureCallbackListener = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            createCameraPreview();
        }
    };

    protected void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    protected void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    protected void createCameraPreview() {
        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
            Surface surface = new Surface(texture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    //The camera is already closed
                    if (null == cameraDevice) {
                        return;
                    }
                    // When the session is ready, we start displaying the preview.
                    cameraCaptureSessions = cameraCaptureSession;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(getActivity(), "Configuration change", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void openCamera() {
        CameraManager manager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
        Log.e(TAG, "is camera open");
        try {
            cameraId = manager.getCameraIdList()[1];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            imageDimension = map.getOutputSizes(SurfaceTexture.class)[1];
            // Add permission for camera and let user grant the permission
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
                return;
            }
            manager.openCamera(cameraId, stateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        Log.e(TAG, "openCamera X");
    }

    protected void updatePreview() {
        if (null == cameraDevice) {
            Log.e(TAG, "updatePreview error, return");
        }
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        try {
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void closeCamera() {
        if (null != cameraDevice) {
            cameraDevice.close();
            cameraDevice = null;
        }
        if (null != imageReader) {
            imageReader.close();
            imageReader = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                // close the app
                Toast.makeText(getActivity(), "Sorry!!!, you can't use this app without granting permission", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e(TAG, "onResume");
        startBackgroundThread();
        if (textureView.isAvailable()) {
            openCamera();
        } else {
            textureView.setSurfaceTextureListener(textureListener);
        }
    }

    @Override
    public void onPause() {
        Log.e(TAG, "onPause");
        //closeCamera();
        stopBackgroundThread();
        super.onPause();
    }

    //menutup loading progress
    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

    }

    //take picture n times
    private void doLoop(int i, int n) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                //show progressbar process status
                float ix = (float) i;
                float nx = (float) n;
                float percentnya = ((ix + 1) / nx) * 100;
                int precentint = (int) percentnya;
                pbPercentage.setProgress(precentint);
                if (i < n) {

                    if (null == cameraDevice) {
                        Log.e(TAG, "cameraDevice is null");
                        return;
                    }
                    CameraManager manager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
                    try {
                        CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());
                        Size[] jpegSizes = null;
                        if (characteristics != null) {
                            jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
                        }
                        int width = 480;
                        int height = 640;
                        if (jpegSizes != null && 0 < jpegSizes.length) {
                            width = jpegSizes[0].getWidth();
                            height = jpegSizes[0].getHeight();
                        }
                        ImageReader reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
                        List<Surface> outputSurfaces = new ArrayList<Surface>(2);
                        outputSurfaces.add(reader.getSurface());
                        outputSurfaces.add(new Surface(textureView.getSurfaceTexture()));
                        final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
                        captureBuilder.addTarget(reader.getSurface());
                        captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

                        captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(0));
//                        file = new File(Environment.getExternalStorageDirectory() + "/pic.jpg");
                         file = Environment.getExternalStorageDirectory();

                        ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
                            @Override
                            public void onImageAvailable(ImageReader reader) {
                                Image image = null;
                                try {
                                    image = reader.acquireLatestImage();
                                    ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                                    byte[] bytes = new byte[buffer.capacity()];
                                    buffer.get(bytes);

                                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                                    String imgPath = FileUtil.getPath(getContext(), getImageUri(getContext(), bitmap));
                                    files.add(Uri.parse(imgPath));
                                    save(bytes);

                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } finally {
                                    if (image != null) {
                                        image.close();
                                    }
                                }
                            }

                            private void save(byte[] bytes) throws IOException {
//                                OutputStream output = null;
                                try {
//                        output = new FileOutputStream(file);
//                        output.write(bytes);
                                } finally {
//                                    if (null != output) {
//                                        output.close();
////
//
//                                    }
                                }
                            }
                        };
                        reader.setOnImageAvailableListener(readerListener, mBackgroundHandler);
                        final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                            @Override
                            public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                                super.onCaptureCompleted(session, request, result);
                                createCameraPreview();
                            }
                        };
                        cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                            @Override
                            public void onConfigured(CameraCaptureSession session) {
                                try {
                                    session.capture(captureBuilder.build(), captureListener, mBackgroundHandler);
                                } catch (CameraAccessException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onConfigureFailed(CameraCaptureSession session) {
                            }
                        }, mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                    doLoop(i + 1, n);
                } else {
                    uploadLiveness(String.valueOf(gesture));
                }
            }
            //set 2 second delay per taking picture
        }, 2000);
    }

    //start timer function
    void startTimer() {
        cTimer = new CountDownTimer(4000, 1000) {
            public void onTick(long millisUntilFinished) {

                tvCounter.setVisibility(View.VISIBLE);
                tvCounter.setText(String.valueOf(millisUntilFinished / 1000));
                if (millisUntilFinished < 1000) {
                    tvCounter.setVisibility(View.GONE);
                    progressBar.setVisibility(View.VISIBLE);
                }
            }

            public void onFinish() {
                cancelTimer();
                doLoop(0, 4);
            }
        };
        cTimer.start();
    }


    //cancel timer
    void cancelTimer() {
        if (cTimer != null)
            cTimer.cancel();
    }

    //random gesture
    private int getGestureset() {
        Random random = new Random();
        int gestureSet = random.nextInt(7);
        return gestureSet;

    }

    //get command base on gesture
    private void setCommand(int gestureset) {
        String description;

        switch (gestureset) {
            case 0:
                description = "mata kiri tertutup, mulut tertutup, mata kanan tertutup";
                tvLltop.setText(description);
                ivExampleTop.setImageResource(R.drawable.nol);
                break;
            case 1:
                description = "mata kiri tertutup, mulut tertutup, mata kanan terbuka";
                tvLltop.setText(description);
                ivExampleTop.setImageResource(R.drawable.satu);
                break;
            case 2:
                description = "mata kiri tertutup, mulut terbuka, mata kanan tertutup";
                tvLltop.setText(description);
                ivExampleTop.setImageResource(R.drawable.dua);
                break;
            case 3:
                description = "mata kiri tertutup, mulut terbuka, mata kanan terbuka";
                tvLltop.setText(description);
                ivExampleTop.setImageResource(R.drawable.tiga);
                break;
            case 4:
                description = "mata kiri terbuka, mulut tertutup, mata kanan tertutup";
                tvLltop.setText(description);
                ivExampleTop.setImageResource(R.drawable.empat);
                break;
            case 5:
                description = "mata kiri terbuka, mulut tertutup, mata kanan terbuka";
                tvLltop.setText(description);
                ivExampleTop.setImageResource(R.drawable.lima);
                break;
            case 6:
                description = "mata kiri terbuka, mulut terbuka, mata kanan tertutup";
                tvLltop.setText(description);
                ivExampleTop.setImageResource(R.drawable.enam);
                break;
            case 7:
                description = "mata kiri terbuka, mulut terbuka, mata kanan terbuka";
                tvLltop.setText(description);
                ivExampleTop.setImageResource(R.drawable.tujuh);
                break;
        }
    }

    //add multipart body from files
    @NonNull
    private MultipartBody.Part prepareFilePart(String partName, Uri fileUri) {

        File file = new File(fileUri.getPath());
        Log.i("here is error", file.getAbsolutePath());
        // create RequestBody instance from file
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"),file);

        // MultipartBody.Part is used to send also the actual file name
        return MultipartBody.Part.createFormData(partName, file.getName(), requestFile);

    }

    //upload picture files and gesture to asliri api
    public void uploadLiveness(String gesture_set) {
        List<MultipartBody.Part> list = new ArrayList<>();

        for (Uri uri : files) {
            Log.i("uris", uri.getPath());
            list.add(prepareFilePart("file", uri));
        }

        RequestBody gesturnya = RequestBody.create(MediaType.parse("text/plain"), gesture_set);
        HashMap<String, RequestBody> map = new HashMap<>();
        map.put("gestures_set", gesturnya);

        APIrequestDdata arData = RetroServer.konekRetrofit().create(APIrequestDdata.class);
        Call<Liveness> tampilData = arData.ardLiveness(getHeadersApi(), list, map);
        tampilData.enqueue(new Callback<Liveness>() {
            @Override
            public void onResponse(Call<Liveness> call, Response<Liveness> response) {

                if (response.body().getData().getPassed() != null) {
                    String passed = response.body().getData().getPassed();
                    if (passed.equalsIgnoreCase("true")) {
                        isSucceesed = true;
                    } else {
                        isSucceesed = false;
                    }
                    DialogForm();
                }
            }

            @Override
            public void onFailure(Call<Liveness> call, Throwable t) {
                Log.e("error", t.toString());
                Toast.makeText(getContext(), "Something error", Toast.LENGTH_SHORT).show();
                hideDialog();

            }
        });
    }

    //set header token
    public HashMap<String, String> getHeadersApi() {
        HashMap<String, String> headerHashMap = new HashMap<>();
        headerHashMap.put("Accept", "application/json");
        headerHashMap.put("token", getString(R.string.token));

        return headerHashMap;
    }

    //get image uri
    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 10, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "liveness", null);
        Log.d("image uri", path);
        return Uri.parse(path);
    }


}