package net.balqisstudio.goeksdriver.activity;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;

import net.balqisstudio.goeksdriver.database.DBHandler;
import net.balqisstudio.goeksdriver.database.Queries;
import net.balqisstudio.goeksdriver.model.Content;
import net.balqisstudio.goeksdriver.model.Driver;
import net.balqisstudio.goeksdriver.model.Transaksi;
import net.balqisstudio.goeksdriver.network.AsyncTaskHelper;
import net.balqisstudio.goeksdriver.network.HTTPHelper;

import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import net.balqisstudio.goeksdriver.R;
import net.balqisstudio.goeksdriver.network.Log;
import net.balqisstudio.goeksdriver.network.NetworkActionResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class KonfirmasiBarangActivity extends AppCompatActivity {

    KonfirmasiBarangActivity activity;
    private static final int TAKE_PICTURE = 1;
    private Uri imageUri;
    ImageView uploadFoto;
    String fotoInvoice = "";
    Transaksi myTrans;
    int status;
    TextView butSubmit;
    Queries que;
    Driver driver;
    int maxRetry = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_konfirmasi_barang);

        activity = KonfirmasiBarangActivity.this;
        myTrans = (Transaksi) getIntent().getSerializableExtra("transaksi");

        que = new Queries(new DBHandler(activity));
        driver = que.getDriver();
        initView();
    }

    private void initView(){
        butSubmit = (TextView) findViewById(R.id.butSubmitU);
        uploadFoto = (ImageView) findViewById(R.id.uploadFoto);
        final EditText totalBiaya = (EditText) findViewById(R.id.totalBiaya);
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            butSubmit.setEnabled(false);
            ActivityCompat.requestPermissions(activity, new String[] { Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE }, 0);
        }

        uploadFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePhoto();
            }
        });

        butSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isOk1, isOk2 = false;
                if(totalBiaya.getText().toString().equals("")){
                    Toast.makeText(activity, "Mohon masukkan total biaya.", Toast.LENGTH_SHORT).show();
                    isOk1 = false;
                }else{
                    isOk1 = true;
                }
                if(fotoInvoice.equals("")){
                    Toast.makeText(activity, "Mohon masukkan foto struk belanja.", Toast.LENGTH_SHORT).show();
                    isOk2 = false;
                }else{
                    isOk2 = true;
                }

                if(isOk1 && isOk2){
                    JSONObject jFins = new JSONObject();
                    try {
                        jFins.put("id", driver.id);
                        jFins.put("id_transaksi", myTrans.id_transaksi);
                        jFins.put("foto_struk", fotoInvoice);
                        jFins.put("harga_akhir", totalBiaya.getText().toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if(String.valueOf(myTrans.order_fitur).equals("4")){
                        finishTransaksiMmart(jFins);
                    }else{
                        finishTransaksiMfood(jFins);
                    }
                }
            }
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case TAKE_PICTURE:
                if (resultCode == Activity.RESULT_OK) {
                    Uri selectedImage = imageUri;
                    getContentResolver().notifyChange(selectedImage, null);
                    ContentResolver cr = getContentResolver();
                    Bitmap bitmap;
                    try {
                        bitmap = android.provider.MediaStore.Images.Media.getBitmap(cr, selectedImage);
                        fotoInvoice = compressJSON(bitmap);

                        uploadFoto.setImageBitmap(bitmap);
//                        Toast.makeText(activity, selectedImage.toString(), Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(activity, "Failed to load", Toast.LENGTH_SHORT).show();
//                        Log.e("Camera", e.toString());
                    }
                }
                break;
            default:
                break;
        }
    }

    public void takePhoto() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "Invoice_" + timeStamp +".jpg";

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        File photo = new File(Environment.getExternalStorageDirectory(),  imageFileName);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            intent.putExtra(MediaStore.EXTRA_OUTPUT,
                    Uri.fromFile(photo));
            imageUri = Uri.fromFile(photo);
        } else {
            File file = new File(photo.getPath());
            Uri photoUri = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName() + ".fileProvider", file);
            imageUri = photoUri;
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        }
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(intent, TAKE_PICTURE);
    }


    public String compressJSON(Bitmap bmp){
        byte[] imageBytes0;
        ByteArrayOutputStream baos0 = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG ,50, baos0);
        imageBytes0 = baos0.toByteArray();

        //image.setImageBitmap(bmp);

        String encodedImage= Base64.encodeToString(imageBytes0, Base64.DEFAULT);
        return encodedImage;
    }

    private void finishTransaksiMmart(final JSONObject jFins){
        final ProgressDialog pd= showLoading();
        HTTPHelper.getInstance(activity).driverFinishMamrt(jFins, new NetworkActionResult() {
            @Override
            public void onSuccess(JSONObject obj) {
                pd.dismiss();
                try {
                    if(obj.getString("message").equals("finish")){
                        announceToUser(myTrans.id_transaksi, 4, myTrans.order_fitur);
                    }else{
                        Toast.makeText(activity, "Terjadi kesalahan", Toast.LENGTH_SHORT).show();
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
                if(maxRetry == 0){
                    pd.dismiss();
                    Toast.makeText(activity, "Koneksi bermasalah...", Toast.LENGTH_SHORT).show();
                    maxRetry = 4;
                }else{
                    finishTransaksiMmart(jFins);
                    maxRetry--;
                    Log.d("Try_ke_konfirmasi", String.valueOf(maxRetry));
                    pd.dismiss();
                }
            }
        });
    }

    private void finishTransaksiMfood(final JSONObject jFins){
        final ProgressDialog pd= showLoading();
        HTTPHelper.getInstance(activity).driverFinishMfood(jFins, new NetworkActionResult() {
            @Override
            public void onSuccess(JSONObject obj) {
                pd.dismiss();
                try {
                    if(obj.getString("message").equals("finish")){
                        announceToUser(myTrans.id_transaksi, 4, myTrans.order_fitur);
                    }else{
                        Toast.makeText(activity, "Terjadi kesalahan", Toast.LENGTH_SHORT).show();
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
                if(maxRetry == 0){
                    pd.dismiss();
                    Toast.makeText(activity, "Koneksi bermasalah", Toast.LENGTH_SHORT).show();
                    maxRetry = 4;
                }else{
                    finishTransaksiMfood(jFins);
                    maxRetry--;
                    Log.d("Try_ke_konfirmasi", String.valueOf(maxRetry));
                    pd.dismiss();
                }
            }
        });
    }

    private ProgressDialog showLoading(){
        ProgressDialog ad = ProgressDialog.show(activity, "", "Loading...", true);
        return ad;
    }

    private void announceToUser(String id_trans, int acc, String orderFitur){
        Content content = new Content();
        content.addRegId(myTrans.reg_id_pelanggan);
        content.createDataOrder(driver.id, id_trans, String.valueOf(acc), orderFitur);
        sendResponseToPelanggan(content);
    }

    private void sendResponseToPelanggan(final Content content){

        AsyncTaskHelper asyncTask = new AsyncTaskHelper(activity, true);
        asyncTask.setAsyncTaskListener(new AsyncTaskHelper.OnAsyncTaskListener() {
            @Override
            public void onAsyncTaskDoInBackground(AsyncTaskHelper asyncTask) {
                status = HTTPHelper.sendToGCMServer(content);
            }

            @Override
            public void onAsyncTaskProgressUpdate(AsyncTaskHelper asyncTask) {
            }

            @Override
            public void onAsyncTaskPostExecute(AsyncTaskHelper asyncTask) {
                if (status == 1) {
                }else{
                    Toast.makeText(activity, "Message sending failed to customer", Toast.LENGTH_SHORT).show();
                }
                que.truncate(DBHandler.TABLE_CHAT);
                que.truncate(DBHandler.TABLE_IN_PROGRESS_TRANSAKSI);
                if(myTrans.order_fitur.equals("3")){
                    que.truncate(DBHandler.TABLE_MAKANAN_BELANJA);
                }else{
                    que.truncate(DBHandler.TABLE_BARANG_BELANJA);
                }
                que.updateStatus(1);

                Intent toRate = new Intent(activity, RatingUserActivity.class);
                toRate.putExtra("id_transaksi", myTrans.id_transaksi);
                toRate.putExtra("id_pelanggan", myTrans.id_pelanggan);
                toRate.putExtra("order_fitur", myTrans.order_fitur);
                toRate.putExtra("id_driver", driver.id);
                startActivity(toRate);
                finish();
        }
            @Override
            public void onAsyncTaskPreExecute(AsyncTaskHelper asyncTask) {

            }
        });
        asyncTask.execute();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                butSubmit.setEnabled(true);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        que.closeDatabase();
    }
}
