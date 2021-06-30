package com.yusuffirdaus.aslirisalesdemo.ui.ocr;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.os.Bundle;
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
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.yusuffirdaus.aslirisalesdemo.R;
import com.yusuffirdaus.aslirisalesdemo.adapter.OcrAdapter;
import com.yusuffirdaus.aslirisalesdemo.api.APIrequestDdata;
import com.yusuffirdaus.aslirisalesdemo.api.RetroServer;
import com.yusuffirdaus.aslirisalesdemo.model.DataCode;
import com.yusuffirdaus.aslirisalesdemo.model.DataParamFaceCrop;
import com.yusuffirdaus.aslirisalesdemo.model.DataParamOcrExtra;
import com.yusuffirdaus.aslirisalesdemo.model.FaceCrop;
import com.yusuffirdaus.aslirisalesdemo.model.Ocr;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.Activity.RESULT_OK;

public class OcrFragment extends Fragment {


    private List<DataCode> DataCodes = new ArrayList<>();
    ProgressDialog pDialog;
    private String TAG = "ysf";
    private String base64_ktp = "";
    private static String imageString = "";
    private Button bProcess;
    private TextureView textureView;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
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
    private ImageView iKtp, iFace, ivOverlay;
    private TextView tFace;
    private Bitmap bmp, bmpFace;
    private CheckBox  cbFaceCrop;
    private Boolean isExtra = true, isFaceCrop = false;
    private RadioGroup rGroup;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_ocr, container, false);
        pDialog = new ProgressDialog(getContext());
        pDialog.setTitle("Loading Data");

        ivOverlay = root.findViewById(R.id.ivOverlay);
        bProcess = root.findViewById(R.id.bProcess);
        textureView = (TextureView) root.findViewById(R.id.texture);

        rGroup = root.findViewById(R.id.rGroup);

        rGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (i == R.id.rExtra) {
                    isExtra = true;



                } else {
                    isExtra = false;
                }

            }
        });
        cbFaceCrop = root.findViewById(R.id.cbFace);

        cbFaceCrop.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    isFaceCrop = true;
                } else {
                    isFaceCrop = false;
                }
            }
        });
        assert textureView != null;
        textureView.setSurfaceTextureListener(textureListener);
        bProcess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePicture();

            }
        });

        return root;
    }

        protected void takePicture() {
            showDialog();
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
                int width = 640;
                int height = 250;
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

                // Orientation
                int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
                captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, 90);


                final File file = new File(Environment.getExternalStorageDirectory() + "/pic.jpg");
                ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
                    @Override
                    public void onImageAvailable(ImageReader reader) {
                        Image image = null;
                        try {

                            Image image2 = reader.acquireLatestImage();
                            ByteBuffer buffer = image2.getPlanes()[0].getBuffer();
                            byte[] bytes = new byte[buffer.capacity()];
                            buffer.get(bytes);

                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            int panjang = bmp.getHeight();
                            int lebar = bmp.getWidth();

                            Matrix matrix = new Matrix();

                            matrix.postRotate(90);


                            if (panjang < lebar) {
                                bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
                            }
                            panjang = bmp.getHeight();
                            lebar = bmp.getWidth();
                            bmp = Bitmap.createBitmap(bmp, 0, (panjang / 2) - (panjang / 6), lebar, panjang / 3);
                            bmp.compress(Bitmap.CompressFormat.JPEG, 0, baos);


                            byte[] b = baos.toByteArray();
                            base64_ktp = Base64.encodeToString(b, Base64.NO_WRAP);
                            loadDataOcr(base64_ktp);
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            if (image != null) {
                                image.close();
                            }
                        }
                    }

                    private void save(byte[] bytes) throws IOException {
                        OutputStream output = null;
                        try {
                            output = new FileOutputStream(file);
                            output.write(bytes);
                        } finally {
                            if (null != output) {
                                output.close();
                            }
                        }
                    }
                };
                reader.setOnImageAvailableListener(readerListener, mBackgroundHandler);
                final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                    @Override
                    public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                        super.onCaptureCompleted(session, request, result);
    //                    Toast.makeText(getContext(), "Saved:" + file, Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(getContext(), "Configuration change", Toast.LENGTH_SHORT).show();
                    }
                }, null);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

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
                cameraDevice.close();
            }

            @Override
            public void onError(CameraDevice camera, int error) {
                cameraDevice.close();
                cameraDevice = null;
            }
        };

        private void openCamera() {
            CameraManager manager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
            Log.e(TAG, "is camera open");
            try {
                cameraId = manager.getCameraIdList()[0];
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                assert map != null;
                imageDimension = map.getOutputSizes(SurfaceTexture.class)[1];
                // Add permission for camera and let user grant the permission
                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
                    return;
                }
                manager.openCamera(cameraId, stateCallback, null);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
            Log.e(TAG, "openCamera X");
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

        private void loadDataOcr(String foto_ktp) {
            DataParamOcrExtra pm = new DataParamOcrExtra();
            pm.setKtp_image(foto_ktp);
            DataParamFaceCrop pmf = new DataParamFaceCrop();
            pmf.setImage(foto_ktp);
            bmpFace = null;
            if (isFaceCrop) {
                APIrequestDdata arData = RetroServer.konekRetrofit().create(APIrequestDdata.class);
                Call<FaceCrop> tampilData = arData.ardFaceCrop(getHeaders(), pmf);
                tampilData.enqueue(new Callback<FaceCrop>() {
                    @Override
                    public void onResponse(Call<FaceCrop> call, Response<FaceCrop> response) {
                        List<String> facecrop = response.body().getData().getCropped_image();
                        Log.e(TAG, "onResponse: " + facecrop);
                        byte[] decodedString = Base64.decode(facecrop.get(0), Base64.DEFAULT);
                        bmpFace = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        hideDialog();

                    }

                    @Override
                    public void onFailure(Call<FaceCrop> call, Throwable t) {
                        Log.e("error", t.toString());
                        Toast.makeText(getContext(), "Error " + t.toString(), Toast.LENGTH_SHORT).show();
                        hideDialog();
                    }
                });
            }
            if (isExtra) {
                APIrequestDdata arData = RetroServer.konekRetrofit().create(APIrequestDdata.class);
                Call<Ocr> tampilData = arData.ardOcrExtra(getHeaders(), pm);
                tampilData.enqueue(new Callback<Ocr>() {

                    @Override
                    public void onResponse(Call<Ocr> call, Response<Ocr> response) {

                        try {

                            Map<String, String> myMap = response.body().getData();
                            DataCodes.clear();
                            for (Map.Entry<String, String> entry : myMap.entrySet()) {
                                DataCode procode = new DataCode(entry.getKey(), entry.getValue());
                                DataCodes.add(procode);
                            }
                            Log.e(TAG, "onResponse: " + DataCodes);
                            if (DataCodes.size() > 0) {
                                bsOcr = new BottomSheetDialog(getContext());
                                bsOcr.setContentView(R.layout.bs_ocr);

                                rvData = bsOcr.findViewById(R.id.rv_data);
                                iKtp = bsOcr.findViewById(R.id.ivKtp);
                                iKtp.setImageBitmap(bmp);
                                tFace = bsOcr.findViewById(R.id.tFace);
                                iFace = bsOcr.findViewById(R.id.ivFace);
                                if (isFaceCrop) {
                                    iFace.setImageBitmap(bmpFace);
                                } else {
                                    iFace.setVisibility(View.GONE);
                                    tFace.setVisibility(View.GONE);
                                }
                                adData = new OcrAdapter(getContext(), DataCodes);
                                rvData.setAdapter(adData);
                                lmData = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
                                rvData.setLayoutManager(lmData);

                                bsOcr.show();

                            } else {

                            }
                        } catch (Exception e) {

                        }

                        hideDialog();

                    }

                    @Override
                    public void onFailure(Call<Ocr> call, Throwable t) {
                        Log.e("error", t.toString());
                        Toast.makeText(getContext(), "Error " + t.toString(), Toast.LENGTH_SHORT).show();
                        hideDialog();
                    }
                });
            } else {
                APIrequestDdata arData = RetroServer.konekRetrofit().create(APIrequestDdata.class);
                Call<Ocr> tampilData = arData.ardOcr(getHeaders(), pm);
                tampilData.enqueue(new Callback<Ocr>() {

                    @Override
                    public void onResponse(Call<Ocr> call, Response<Ocr> response) {

                        try {

                            Map<String, String> myMap = response.body().getData();
                            DataCodes.clear();
                            for (Map.Entry<String, String> entry : myMap.entrySet()) {
                                DataCode procode = new DataCode(entry.getKey(), entry.getValue());
                                DataCodes.add(procode);
                            }
                            Log.e(TAG, "onResponse: " + DataCodes);
                            if (DataCodes.size() > 0) {
                                bsOcr = new BottomSheetDialog(getContext());
                                bsOcr.setContentView(R.layout.bs_ocr);

                                rvData = bsOcr.findViewById(R.id.rv_data);
                                iKtp = bsOcr.findViewById(R.id.ivKtp);
                                iKtp.setImageBitmap(bmp);
                                tFace = bsOcr.findViewById(R.id.tFace);
                                iFace = bsOcr.findViewById(R.id.ivFace);
                                if (isFaceCrop) {
                                    iFace.setImageBitmap(bmpFace);
                                } else {
                                    iFace.setVisibility(View.GONE);
                                    tFace.setVisibility(View.GONE);
                                }

                                adData = new OcrAdapter(getContext(), DataCodes);
                                rvData.setAdapter(adData);
                                lmData = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
                                rvData.setLayoutManager(lmData);

                                bsOcr.show();

                            } else {

                            }
                        } catch (Exception e) {

                        }

                        hideDialog();

                    }

                    @Override
                    public void onFailure(Call<Ocr> call, Throwable t) {
                        Log.e("error", t.toString());
                        Toast.makeText(getContext(), "Error " + t.toString(), Toast.LENGTH_SHORT).show();
                        hideDialog();
                    }
                });
            }

        }

        private void showDialog() {
            if (!pDialog.isShowing())
                pDialog.show();

        }

        //menutup loading progress
        private void hideDialog() {
            if (pDialog.isShowing())
                pDialog.dismiss();
        }


        public HashMap<String, String> getHeaders() {
            HashMap<String, String> headerHashMap = new HashMap<>();
            headerHashMap.put("Accept", "application/json");
            headerHashMap.put("Content-Type", "application/json");
            headerHashMap.put("token", getString(R.string.token));
            return headerHashMap;
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

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
}