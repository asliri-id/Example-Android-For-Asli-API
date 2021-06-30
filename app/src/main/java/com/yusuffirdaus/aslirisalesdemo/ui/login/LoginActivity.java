package com.yusuffirdaus.aslirisalesdemo.ui.login;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONException;
import org.json.JSONObject;

import com.yusuffirdaus.aslirisalesdemo.MainActivity;
import com.yusuffirdaus.aslirisalesdemo.upil.HttpRequest;

import com.yusuffirdaus.aslirisalesdemo.BuildConfig;
import com.yusuffirdaus.aslirisalesdemo.R;
import com.yusuffirdaus.aslirisalesdemo.TelephonyInfo;


import static com.android.volley.Request.Method.POST;

public class LoginActivity extends AppCompatActivity {
    ProgressDialog pDialog;
    Button btn_login, btn_login2;
    EditText txt_username, txt_password;
    TextView eVersion;

    public final static String TAG_USERNAME = "username";
    public final static String TAG_IMEI = "imei";

    String IMEI_Number_Holder;
    String PHONETYPE;
    String VERSION_APK;
    SharedPreferences sharedpreferences;
    Boolean session = false;
    Boolean session2 = false;
    String username;
    public static final String my_shared_preferences = "my_shared_preferences";
    public static final String session_status = "session_status";
    public static final String session_logout = "android";

    //    dialogform compatible
    LayoutInflater inflater;
    View dialogView;
    private Button btnSetuju, btnLainkali;
    private int compability = 1;
    //    end dialogform compatible
    private int codenya = 99;
    private String messagenya = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        final View vshow = (View) findViewById(R.id.vshow);
        final View vhidden = (View) findViewById(R.id.vhidden);
        btn_login = (Button) findViewById(R.id.btn_login);
        btn_login2 = (Button) findViewById(R.id.btn_login2);
        txt_username = (EditText) findViewById(R.id.txt_username);
        txt_password = (EditText) findViewById(R.id.txt_password);
        eVersion = (TextView) findViewById(R.id.eVersion);


        vshow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vshow.setVisibility(View.GONE);
                vhidden.setVisibility(View.VISIBLE);
                txt_password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            }
        });
        vhidden.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vshow.setVisibility(View.VISIBLE);
                vhidden.setVisibility(View.GONE);
                txt_password.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
        });
        // Cek session login jika TRUE maka langsung buka MainActivitys
        sharedpreferences = getSharedPreferences(my_shared_preferences, Context.MODE_PRIVATE);
        session = sharedpreferences.getBoolean(session_status, false);
        username = sharedpreferences.getString(TAG_USERNAME, null);
        VERSION_APK = getversionapk();
        eVersion.setText("Version " + VERSION_APK);
        hasPermissions(this);


        btn_login.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                String usernamez = txt_username.getText().toString();
                String password = txt_password.getText().toString();

                // mengecek kolom yang kosong
                if (usernamez.trim().length() > 0 && password.trim().length() > 0) {
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putBoolean(session_status, true);
                    editor.putString(TAG_USERNAME, username);
                    editor.putString(TAG_IMEI, IMEI_Number_Holder);
                    editor.commit();
//                              // Memanggil main activity
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);

                    startActivity(intent);
                    finish();
                } else if (usernamez.trim().length() == 0 && password.trim().length() == 0) {
                    Toast.makeText(getApplicationContext(), getString(R.string.password_username_empty), Toast.LENGTH_LONG).show();
                } else if (usernamez.trim().length() == 0) {
                    Toast.makeText(getApplicationContext(), getString(R.string.username_empty), Toast.LENGTH_LONG).show();
                } else if (password.trim().length() == 0) {
                    Toast.makeText(getApplicationContext(), getString(R.string.password_empty), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.password_username_empty), Toast.LENGTH_LONG).show();
                }
            }
        });


        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        pDialog.setMessage("Checking Connection");
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    ActivityCompat.finishAffinity(this);
                }
                return;
            }
        }
    }



    public static String getversionapk() {
        String versionapk = BuildConfig.VERSION_NAME;
        return versionapk;
    }


    @Override
    protected void onResume() {
        super.onResume();

    }


}
