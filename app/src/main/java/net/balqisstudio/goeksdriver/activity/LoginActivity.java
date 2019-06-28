package net.balqisstudio.goeksdriver.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import net.balqisstudio.goeksdriver.database.DBHandler;
import net.balqisstudio.goeksdriver.database.Queries;
import net.balqisstudio.goeksdriver.network.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;

import net.balqisstudio.goeksdriver.BuildConfig;
import net.balqisstudio.goeksdriver.MainActivity;
import net.balqisstudio.goeksdriver.R;
import net.balqisstudio.goeksdriver.model.Driver;
import net.balqisstudio.goeksdriver.network.AppController;
import net.balqisstudio.goeksdriver.network.HTTPHelper;
import net.balqisstudio.goeksdriver.network.NetworkActionResult;
import net.balqisstudio.goeksdriver.preference.SettingPreference;
import net.balqisstudio.goeksdriver.preference.UserPreference;
import net.balqisstudio.goeksdriver.service.MyFirebaseInstanceIdService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;

public class LoginActivity extends AppCompatActivity {

    Button login;
    EditText email, password;
    LoginActivity activity;
    Driver user;
//    UserPreference userPreference;
    Intent locSev;
    String token = "";
    FirebaseInstanceId fireId;
    int maxRetry = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        activity = LoginActivity.this;
        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);

        locSev = new Intent(this, MyFirebaseInstanceIdService.class);
        startService(locSev);

//        userPreference = new UserPreference(activity);
        login = (Button) findViewById(R.id.loginButton);
        new SettingPreference(this).insertSetting(new String[]{"OFF", "0", "OFF", String.valueOf(BuildConfig.VERSION_CODE)});



//        Log.d("NewToken", token);
//        Log.d("NewToken", userPreference.getDriver().gcm_id);

        ImageView clear = (ImageView) findViewById(R.id.clearEmail);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                email.setText("");
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fireId = FirebaseInstanceId.getInstance();
                if(fireId.getToken() != null)
                    token = fireId.getToken();

                if(token.equals("")){
                    Toast.makeText(activity, "Waiting for Connection..", Toast.LENGTH_SHORT).show();
//                    showWarning();
                }else{
                    if(email.getText().toString().equals("") || password.getText().toString().equals("")){
                        Toast.makeText(activity, "Mohon lengkapi form input", Toast.LENGTH_SHORT).show();
                    }else{
//                        if(password.getText().toString().length() < 7){
//                            Toast.makeText(activity, "Minimum password 6 karakter", Toast.LENGTH_SHORT).show();
//                        }
                        signin(token);
                    }
                }

            }
        });
    }

    private void signin(final String token){
        final JSONObject logData = new JSONObject();
        try {
//            logData.put("email", email.getText().toString());
            logData.put("no_telepon", email.getText().toString());
            logData.put("password", password.getText().toString());
            logData.put("reg_id", token);
        } catch (JSONException e) {
            e.printStackTrace();
        }

//        Log.d("login_data", logData.toString());
        final ProgressDialog pd = showLoading();
        HTTPHelper.getInstance(activity).login(logData, new NetworkActionResult() {
            @Override
            public void onSuccess(JSONObject obj) {
                Log.d("login_data", obj.toString());
                try {
                    if(obj.getString("message").equals("found")){
                        pd.dismiss();
                        user = HTTPHelper.parseUserJSONData(activity, obj.toString());
                        user.gcm_id = token;
                        user.password = password.getText().toString();
                        Queries que = new Queries(new DBHandler(activity));
                        que.insertDriver(user);
                        que.closeDatabase();
                        new UserPreference(activity).insertDriver(user);
                        if(loadImageFromServer(user.image)){
                            FirebaseMessaging.getInstance().subscribeToTopic("info");
                            Intent change = new Intent(activity, MainActivity.class);
                            startActivity(change);
                            finish();
                        }
                    }else if(obj.getString("message").equals("banned")){
                        pd.dismiss();
                        showWarning("Akun anda telah dibanned, harap konfirmasi ke kantor PT Mangjek Indonesia");
                    }else{
                        pd.dismiss();
                        showWarning("Nomor handphone atau password salah");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                maxRetry = 4;
            }

            @Override
            public void onFailure(String message) {

            }

            @Override
            public void onError(String message) {
//                Log.d("Pesan_error", message);
                if(maxRetry == 0){
                    pd.dismiss();
                    showWarning();
                    maxRetry = 4;
                }else{
                    signin(token);
                    maxRetry--;
                    Log.d("Try_ke_login", String.valueOf(maxRetry));
                    pd.dismiss();
                }

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(locSev);
    }

    private ProgressDialog showLoading(){
        ProgressDialog ad = ProgressDialog.show(activity, "", "Loading...", true);
        return ad;
    }


    private MaterialDialog showWarning() {
        final MaterialDialog md = new MaterialDialog.Builder(activity)
                .title("Koneksi bermasalah")
                .content("Silakan coba lagi!")
                .icon(new IconicsDrawable(activity)
                        .icon(FontAwesome.Icon.faw_exclamation_triangle)
                        .color(Color.YELLOW)
                        .sizeDp(24))
                .positiveText("Tutup")
                .positiveColor(Color.DKGRAY)
                .show();

        View positive = md.getActionButton(DialogAction.POSITIVE);

        positive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                md.dismiss();
            }
        });
        return md;
    }

    private MaterialDialog showWarning(String content) {
        final MaterialDialog md = new MaterialDialog.Builder(activity)
                .title("Login Gagal")
                .content(content)
                .icon(new IconicsDrawable(activity)
                        .icon(FontAwesome.Icon.faw_exclamation_triangle)
                        .color(Color.RED)
                        .sizeDp(20))
                .positiveText("Tutup")
                .positiveColor(Color.DKGRAY)
                .show();

        View positive = md.getActionButton(DialogAction.POSITIVE);

        positive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                md.dismiss();
            }
        });
        return md;
    }

    private boolean loadImageFromServer(String image) {
        final ProgressDialog pd = showLoading();
        ImageLoader imageLoader = AppController.getInstance(this).getImageLoader();
        imageLoader.get(image, new ImageLoader.ImageListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
//                Log.d("Image Load Error: ", error.getMessage());
                pd.dismiss();
            }
            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean arg1) {
                if (response.getBitmap() != null) {
                    Bitmap circleBitmap = response.getBitmap();
                    saveToInternalStorage(circleBitmap);
                }
                pd.dismiss();
            }
        });
        return true;
    }

    private String saveToInternalStorage(Bitmap bitmapImage){
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir("fotoDriver", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath=new File(directory, "profile.jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 50, fos);
            fos.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return directory.getAbsolutePath();
    }
}
