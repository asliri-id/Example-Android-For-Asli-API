package com.yusuffirdaus.aslirisalesdemo.ui.profesional;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
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
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
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
import com.yusuffirdaus.aslirisalesdemo.model.DataParamProfesional;
import com.yusuffirdaus.aslirisalesdemo.model.FaceCrop;
import com.yusuffirdaus.aslirisalesdemo.model.Ocr;
import com.yusuffirdaus.aslirisalesdemo.model.Profesional;
import com.yusuffirdaus.aslirisalesdemo.result.ResultActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.Activity.RESULT_OK;

public class ProfesionalFragment extends Fragment {


    ProgressDialog pDialog;
    private String TAG = "ysf";
    private Button bProcess, bOCR, bReset;
    private RadioGroup rGroup;
    private Boolean isOCR = false;
    private ImageView ivOCR;
    private EditText etNik, etName, etDOB, etPOB;
    Calendar myCalendar;
    private Button bSelfie;
    private static final int REQUEST_IMAGE_CAPTURE = 101;
    private ImageView ivSefile;
    private File fileSelvie;
    private static String imageString = "";
    private Boolean setReset = false;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_profesional, container, false);
        pDialog = new ProgressDialog(getContext());
        pDialog.setTitle("Loading Data");
        bProcess = root.findViewById(R.id.bProcess);
        bOCR = root.findViewById(R.id.bOcr);
        etNik = root.findViewById(R.id.etNik);
        etName = root.findViewById(R.id.etName);
        etDOB = root.findViewById(R.id.etBOD);
        etPOB = root.findViewById(R.id.etePob);
        bSelfie = root.findViewById(R.id.bSelfie);
        ivSefile = root.findViewById(R.id.ivSelfie);
        bReset = root.findViewById(R.id.bReset);


        bReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetData();
                setReset=true;
                Log.e("reset",setReset.toString());
            }
        });
        PackageManager pm = getActivity().getPackageManager();
        ivSefile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent imageTakeintent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (imageTakeintent.resolveActivity(pm) != null) {
                    imageTakeintent.putExtra("android.intent.extras.CAMERA_FACING", 1);
                    startActivityForResult(imageTakeintent, REQUEST_IMAGE_CAPTURE);
                }
            }
        });
        bSelfie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent imageTakeintent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (imageTakeintent.resolveActivity(pm) != null) {
                    imageTakeintent.putExtra("android.intent.extras.CAMERA_FACING", 1);
                    startActivityForResult(imageTakeintent, REQUEST_IMAGE_CAPTURE);
                }
            }
        });
        myCalendar = Calendar.getInstance();
        etDOB.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() != MotionEvent.ACTION_DOWN) return false;
                DatePickerDialog dialog = new DatePickerDialog(view.getContext(), new DatePickerDialog.OnDateSetListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {


                        myCalendar.set(Calendar.YEAR, year);
                        myCalendar.set(Calendar.MONTH, month);
                        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        String formatTanggal = "dd-MM-yyyy";
                        SimpleDateFormat sdf = new SimpleDateFormat(formatTanggal);

                        etDOB.setError(null);
                        etDOB.setText(sdf.format(myCalendar.getTime()));


                        getActivity().getWindow().setSoftInputMode(
                                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
                        );


                    }
                },
                        myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH));

                Calendar besok = Calendar.getInstance();
                besok.set(Calendar.DATE, besok.get(Calendar.DATE));

                Calendar min = Calendar.getInstance();
                min.set(Calendar.MONTH, Calendar.MAY);
                min.set(Calendar.YEAR, 1920);
                min.set(Calendar.DATE, 20);

                dialog.getDatePicker().setMaxDate(besok.getTime().getTime());
                dialog.getDatePicker().setMinDate(min.getTime().getTime());
                dialog.show();

                return false;
            }
        });

        rGroup = root.findViewById(R.id.rGroup);
        rGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (i == R.id.rOcr) {
                    isOCR = true;
                    bOCR.setVisibility(View.VISIBLE);

                } else {
                    isOCR = false;
                    bOCR.setVisibility(View.GONE);
                }

            }
        });
        bOCR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setReset=false;
                Intent intent = new Intent(getContext(), OcrActivity.class);
                startActivity(intent);
            }
        });
        bProcess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validasiVerify();
                setReset=true;

            }
        });

//        dummyData();
        return root;
    }

    private void validasiVerify() {
        String nik = etNik.getText().toString();
        String nama = etName.getText().toString();
        String pob = etPOB.getText().toString();
        String dob = etDOB.getText().toString();

        if (nik.equals("")) {
            etNik.requestFocus();
            etNik.setError("NIK Required!");
        } else if (nama.equals("")) {
            etName.requestFocus();
            etName.setError("Name Required!");
        } else if (pob.equals("")) {
            etPOB.requestFocus();
            etPOB.setError("POB Required!");
        } else if (dob.equals("")) {
            etDOB.requestFocus();
            etDOB.setError("DOB Required!");
        } else if (imageString.equals("")) {
            Toast.makeText(getContext(), "Foto Selfie required!", Toast.LENGTH_SHORT).show();
        } else {
            loadDataPro(nik, nama, pob, dob);


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
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            setReset=true;
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            ivSefile.setImageBitmap(imageBitmap);
            ivSefile.setVisibility(View.VISIBLE);
            bSelfie.setVisibility(View.GONE);
            File filesDir = getContext().getFilesDir();
            fileSelvie = new File(filesDir, "selfie" + ".png");

            OutputStream os;
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                Bitmap bitmap = imageBitmap;
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] imageBytes = baos.toByteArray();
                imageString = Base64.encodeToString(imageBytes, Base64.NO_WRAP);

                Log.e("test", imageString);
                os = new FileOutputStream(fileSelvie);
                os.flush();
                os.close();
            } catch (Exception e) {
                Log.e(getClass().getSimpleName(), "Error writing bitmap", e);
            }

        }
    }

    private void loadDataPro(final String nik, final String nama, final String pob, final String dob) {
        showDialog();
        DataParamProfesional pm = new DataParamProfesional();
        pm.setNik(nik);
        pm.setName(nama);
        pm.setBirthdate(dob);
        pm.setBirthplace(pob);
        pm.setSelfie_photo(imageString);

        APIrequestDdata arData = RetroServer.konekRetrofit().create(APIrequestDdata.class);
        Call<Profesional> tampilData = arData.ardPro(getHeaders(), pm);
        tampilData.enqueue(new Callback<Profesional>() {
            @Override
            public void onResponse(Call<Profesional> call, Response<Profesional> response) {
                boolean result_name = response.body().getData().getName();
                boolean result_pob = response.body().getData().getBirthplace();
                boolean result_dob = response.body().getData().getBirthdate();
                String address = response.body().getData().getAddress();
                Double result_selfie = response.body().getData().getSelfie_photo();

                hideDialog();
                showResult(result_name, result_pob, result_dob, address, result_selfie);

            }

            @Override
            public void onFailure(Call<Profesional> call, Throwable t) {
             Log.e("error", t.toString());
                Toast.makeText(getContext(), "Error " + t.toString(), Toast.LENGTH_SHORT).show();
                hideDialog();
            }
        });
    }

    private void dummyData() {
        etNik.setText("3201011108910007");
        etName.setText("Yusup Firdaus");
        etDOB.setText("11-08-1991");
        etPOB.setText("BOGOR");
    }

    private void resetData() {
        etNik.setText("");
        etName.setText("");
        etDOB.setText("");
        etPOB.setText("");
        ivSefile.setVisibility(View.GONE);
        bSelfie.setVisibility(View.VISIBLE);
        imageString = "";
    }

    private void showResult(final Boolean nama, final Boolean pob, final Boolean dob, final String address, final Double selfie) {

        Intent intent = new Intent(getContext(), ResultActivity.class);
        intent.putExtra("nama", nama);
        intent.putExtra("pob", pob);
        intent.putExtra("dob", dob);
        intent.putExtra("address", address);
        intent.putExtra("selfie", selfie);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        if (setReset == false) {
            try {
                String nik = getActivity().getIntent().getExtras().getString("nik");
                String nama = getActivity().getIntent().getExtras().getString("nama");
                String dob = getActivity().getIntent().getExtras().getString("dob");
                String pob = getActivity().getIntent().getExtras().getString("pob");

                etNik.setText(nik);
                etName.setText(nama);
                etPOB.setText(pob);
                etDOB.setText(dob);

            } catch (Exception e) {
                Log.e("errornya ", e.toString());
            }
        }

        super.onResume();
    }
}