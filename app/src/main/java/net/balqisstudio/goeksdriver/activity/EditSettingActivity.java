package net.balqisstudio.goeksdriver.activity;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;

import net.balqisstudio.goeksdriver.database.DBHandler;
import net.balqisstudio.goeksdriver.database.Queries;
import net.balqisstudio.goeksdriver.network.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.mikepenz.iconics.IconicsDrawable;

import net.balqisstudio.goeksdriver.R;
import net.balqisstudio.goeksdriver.model.Driver;
import net.balqisstudio.goeksdriver.network.HTTPHelper;
import net.balqisstudio.goeksdriver.network.NetworkActionResult;
import net.balqisstudio.goeksdriver.preference.UserPreference;

import org.json.JSONException;
import org.json.JSONObject;

public class EditSettingActivity extends AppCompatActivity {

    EditSettingActivity activity;
    Driver driver;
    String whatUpd;
    EditText kolomSeb, kolomSed, kolKon;
    TextView title;
    Queries que;
    int maxRetry = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_setting);

        activity = EditSettingActivity.this;
        que = new Queries(new DBHandler(activity));
        driver = que.getDriver();
//
        kolomSeb = (EditText) findViewById(R.id.kolomSebelum);
        kolomSed = (EditText) findViewById(R.id.kolomSesudah);
        kolKon = (EditText) findViewById(R.id.kolomKonfirmasi);
        title = (TextView) findViewById(R.id.judulEdit);
        whatUpd = getIntent().getStringExtra("edit");

        title.setText("Ubah "+whatUpd);
        initSelection();
        initView();
    }

    private void initSelection(){
        switch (whatUpd){
            case "password":{
                kolomSeb.setHint("masukkan password saat ini");
                kolomSed.setHint("masukkan password baru");
                kolomSed.setTransformationMethod(PasswordTransformationMethod.getInstance());
                kolKon.setVisibility(View.VISIBLE);
                kolKon.setTransformationMethod(PasswordTransformationMethod.getInstance());
                kolKon.setHint("konfirmasi password baru");
                break;
            }
            case "nomor":{
                kolomSeb.setVisibility(View.GONE);
                kolomSed.setHint("masukkan nomor baru");
                kolKon.setVisibility(View.GONE);
                kolomSed.setInputType(InputType.TYPE_CLASS_PHONE);
                break;
            }
            case "email":{
                kolomSeb.setHint("masukkan email saat ini");
                kolomSed.setHint("masukkan email baru");
                kolKon.setVisibility(View.VISIBLE);
                kolKon.setTransformationMethod(PasswordTransformationMethod.getInstance());
                kolKon.setHint("masukkan password");
                break;
            }
            case "rekening":{
                kolomSeb.setText(driver.nama_bank);
                kolomSeb.setHint("nama bank");
                kolomSed.setText(driver.no_rek);
                kolomSed.setHint("nomor rekening");
                kolKon.setVisibility(View.VISIBLE);
                kolKon.setText(driver.atas_nama);
                kolKon.setHint("pemilik rekening");
                break;
            }
            default:
                break;
        }
    }

    private boolean checkCompleteTelepon(){
        boolean isValid1 = false;

        if(kolomSeb.getText().toString().equals("")){
            Toast.makeText(activity, "Masukkan email saat ini", Toast.LENGTH_SHORT).show();
            isValid1 = false;
        }else {
            isValid1 = true;
        }

        return (isValid1);
    }

    private boolean checkCompleteRekening(){
        boolean isValid1, isValid2, isValid3 = false;

        if(kolomSeb.getText().toString().equals("")){
            Toast.makeText(activity, "Masukkan Nama Bank", Toast.LENGTH_SHORT).show();
            isValid1 = false;
        }else {
            isValid1 = true;
        }
        if(kolomSed.getText().toString().equals("")){
            Toast.makeText(activity, "Masukkan Nomor Rekening", Toast.LENGTH_SHORT).show();
            isValid2 = false;
        }else {
            isValid2 = true;
        }
        if(kolKon.getText().toString().equals("")){
            Toast.makeText(activity, "Masukkan Pemilik Rekening", Toast.LENGTH_SHORT).show();
            isValid3 = false;
        }else {
            isValid3 = true;
        }
        return (isValid1 && isValid2 && isValid3);
    }

    private boolean checkCompleteEmail(){
        boolean isValid1, isValid2, isValid3, isValid4, isValid5 = false;

        if(kolomSeb.getText().toString().equals("")){
            Toast.makeText(activity, "Masukkan email saat ini", Toast.LENGTH_SHORT).show();
            isValid1 = false;
        }else {
            isValid1 = true;
        }
        if(!kolomSeb.getText().toString().equalsIgnoreCase(driver.email)){
            Toast.makeText(activity, "Email saat ini salah", Toast.LENGTH_SHORT).show();
            isValid3 = false;
        }else {
            isValid3 = true;
        }
        if(kolomSed.getText().toString().equals("")){
            Toast.makeText(activity, "Masukkan email baru", Toast.LENGTH_SHORT).show();
            isValid2 = false;
        }else {
            isValid2 = true;
        }
        if(kolKon.getText().toString().equals("")){
            Toast.makeText(activity, "Masukkan password", Toast.LENGTH_SHORT).show();
            isValid4 = false;
        }else {
            isValid4 = true;
        }
        if(!kolKon.getText().toString().equalsIgnoreCase(driver.password)){
            Toast.makeText(activity, "Password saat ini salah", Toast.LENGTH_SHORT).show();
            isValid5 = false;
        }else {
            isValid5 = true;
        }


        return (isValid1 && isValid2 && isValid3 && isValid4 && isValid5);
    }

    private boolean checkCompletePassword(){
        boolean isValid1, isValid2, isValid3, isValid4, isValid5 = false;
        if(kolomSeb.getText().toString().equals("")){
            Toast.makeText(activity, "Kolom password saat ini harap diisi!", Toast.LENGTH_SHORT).show();
            isValid1 = false;
        }else{
            isValid1 = true;
        }
        if(!kolomSeb.getText().toString().equalsIgnoreCase(driver.password)){
            Toast.makeText(activity, "Password saat ini salah.", Toast.LENGTH_SHORT).show();
            isValid5 = false;
        }else{
            isValid5 = true;
        }
        if(kolomSed.getText().toString().equals("")){
            Toast.makeText(activity, "Kolom password harap diisi!", Toast.LENGTH_SHORT).show();
            isValid2 = false;
        }else {
            isValid2 = true;
        }
        if (kolKon.getText().toString().equals("")){
            Toast.makeText(activity, "Kolom konfirmasi password harap diisi!", Toast.LENGTH_SHORT).show();
            isValid3 = false;
        }else{
            isValid3 = true;
        }
        if(!kolomSed.getText().toString().equalsIgnoreCase(kolKon.getText().toString())){
            Toast.makeText(activity, "Password saat ini dan konfirmasi tidak cocok.", Toast.LENGTH_SHORT).show();
            isValid4 = false;
        }else{
            isValid4 = true;
        }

        return (isValid1 && isValid2 && isValid3 & isValid4 && isValid5);
    }

    private void initView(){
        TextView butSubmit = (TextView) findViewById(R.id.butSubmitU);

        butSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (whatUpd.equals("password")){
                    if(checkCompletePassword()){
                        updateSetting(whatUpd);

                    }
                }else if(whatUpd.equals("nomor")){
                    if(checkCompleteTelepon()){
                        updateSetting(whatUpd);
                    }
                }else if(whatUpd.equals("rekening")){
                    if(checkCompleteRekening()){
                        updateRekening();
                    }
                }else{
                    if(checkCompleteEmail()){
                        updateSetting(whatUpd);
                    }
                }
            }
        });
    }

    private void updateSetting(final String what){
        JSONObject jUp = new JSONObject();
        try {
            jUp.put("email", driver.email);
            jUp.put("id_driver", driver.id);
            if(what.equals("password")){
                jUp.put("whatUpd", "password");
                jUp.put("id_driver", driver.id);
                jUp.put("value", kolomSed.getText().toString());
            }else if(what.equals("nomor")){
                jUp.put("whatUpd", "no_telepon");
                jUp.put("id_driver", driver.id);
                jUp.put("value", kolomSed.getText().toString());
            }else{
                jUp.put("whatUpd", "email");
                jUp.put("id_driver", driver.id);
                jUp.put("value", kolomSed.getText().toString());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final ProgressDialog sd = showLoading();
//        Log.d("JSON_EDIT", jUp.toString());
        HTTPHelper.getInstance(activity).updateProfile(jUp, new NetworkActionResult() {
            @Override
            public void onSuccess(JSONObject obj) {
                try {
                    if(obj.getString("message").equals("success")){
                        if(what.equals("password")){
                            new UserPreference(activity).updatePassword(kolomSed.getText().toString());
                            que.updatePassword(kolomSed.getText().toString());
                        }else if(what.equals("nomor")){
                            new UserPreference(activity).updateTelepon(kolomSed.getText().toString());
                            que.updateTelepon(kolomSed.getText().toString());
                        }else{
                            new UserPreference(activity).updateEmail(kolomSed.getText().toString());
                            que.updateEmail(kolomSed.getText().toString());
                        }
                        showFinishMessage(what);
                    }else{
                        Toast.makeText(activity, obj.getString("data"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                sd.dismiss();
                maxRetry = 4;
            }

            @Override
            public void onFailure(String message) {
            }

            @Override
            public void onError(String message) {
                sd.dismiss();
                if(maxRetry == 0){
                    sd.dismiss();
                    Toast.makeText(activity, "Koneksi bermasalah", Toast.LENGTH_SHORT).show();
                    maxRetry = 4;
                }else{
                    updateSetting(what);
                    maxRetry--;
                    Log.d("Try_ke_edit_setting", String.valueOf(maxRetry));
                    sd.dismiss();
                }
            }
        });
    }

    private void updateRekening(){
        JSONObject jUp = new JSONObject();
        try {
            jUp.put("email", driver.email);
            jUp.put("id_driver", driver.id);
            jUp.put("nama_bank", kolomSeb.getText().toString());
            jUp.put("atas_nama", kolKon.getText().toString());
            jUp.put("rekening_bank", kolomSed.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("Jupdate_rekening", jUp.toString());
        final ProgressDialog sd = showLoading();
//        Log.d("JSON_EDIT", jUp.toString());
        HTTPHelper.getInstance(activity).updateRekening(jUp, new NetworkActionResult() {
            @Override
            public void onSuccess(JSONObject obj) {
                try {
                    if(obj.getString("message").equals("success")){
                        showFinishMessage("Rekening");
                        que.updateRekening(new String[]{kolomSeb.getText().toString(),kolomSed.getText().toString(),kolKon.getText().toString()});
                    }else{
                        Toast.makeText(activity, obj.getString("data"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                sd.dismiss();
                maxRetry = 4;
            }

            @Override
            public void onFailure(String message) {
            }

            @Override
            public void onError(String message) {
                if(maxRetry == 0){
                    sd.dismiss();
                    Toast.makeText(activity, "Koneksi bermasalah", Toast.LENGTH_SHORT).show();
                    maxRetry = 4;
                }else{
                    updateRekening();
                    maxRetry--;
                    Log.d("Try_ke_update_rekening", String.valueOf(maxRetry));
                    sd.dismiss();
                }
            }
        });
    }
    private MaterialDialog showFinishMessage(String isi) {
        final MaterialDialog md = new MaterialDialog.Builder(activity)
                .title("Update "+isi+" berhasil")
                .icon(new IconicsDrawable(activity)
                        .color(Color.BLUE)
                        .sizeDp(24))
                .positiveText("Tutup")
                .positiveColor(Color.DKGRAY)
                .show();

        View positive = md.getActionButton(DialogAction.POSITIVE);

        positive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                md.dismiss();
//                Intent toMaps = new Intent(activity, MainActivity.class);
//                startActivity(toMaps);
                finish();
            }
        });
        return md;
    }

    private ProgressDialog showLoading(){
        ProgressDialog ad = ProgressDialog.show(activity, "", "Loading...", true);
        return ad;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        que.closeDatabase();
    }
}
