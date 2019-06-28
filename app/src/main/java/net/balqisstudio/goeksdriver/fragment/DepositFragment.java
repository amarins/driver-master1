package net.balqisstudio.goeksdriver.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import android.util.Base64;

import net.balqisstudio.goeksdriver.database.DBHandler;
import net.balqisstudio.goeksdriver.database.Queries;
import net.balqisstudio.goeksdriver.network.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;

import net.balqisstudio.goeksdriver.MainActivity;
import net.balqisstudio.goeksdriver.R;
import net.balqisstudio.goeksdriver.model.Driver;
import net.balqisstudio.goeksdriver.network.HTTPHelper;
import net.balqisstudio.goeksdriver.network.NetworkActionResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;


public class DepositFragment extends Fragment{
    private static final String TAG = DepositFragment.class.getSimpleName();
    private View rootView;
    MainActivity activity;

    private static final int TAKE_PICTURE = 1;
    private Uri imageUri;
    TextView upload;
    Button daftarBank;
    public boolean statusFragment, ordering = false;
    String bukti = "";
    Driver driver;
    int maxRetry = 4;

    EditText nama, norek, jumlah;

    public DepositFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.content_deposit, container, false);

        activity = (MainActivity) getActivity();
        activity.getSupportActionBar().setTitle("Deposit");
        Queries que = new Queries(new DBHandler(activity));
        driver = que.getDriver();
        que.closeDatabase();
        initView();

        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            upload.setEnabled(false);
            ActivityCompat.requestPermissions(activity, new String[] { Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE }, 0);
        }
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    String bankName = "";
    private void initView() {
        TextView topup;
        final Spinner spinBank;


        nama = (EditText) rootView.findViewById(R.id.pemilikRekening);
        norek = (EditText) rootView.findViewById(R.id.nomorRekening);
        jumlah = (EditText) rootView.findViewById(R.id.nominalTransfer);
        spinBank = (Spinner) rootView.findViewById(R.id.spinBank);
        daftarBank = (Button) rootView.findViewById(R.id.daftarbank);


        spinBank.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                bankName = spinBank.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        daftarBank.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeFragment(new DepositFragment(), true);
                statusFragment = false;
            }
        });

        upload = (Button) rootView.findViewById(R.id.butUploadBukti);
        topup = (TextView) rootView.findViewById(R.id.butTopup);

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePhoto();
//                take_photo();
            }
        });

        topup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(cekIsFill(nama, norek, jumlah)){


                    JSONObject jVer = new JSONObject();
                    try {
                        jVer.put("id", driver.id);
                        jVer.put("no_rekening", norek.getText().toString());
                        jVer.put("jumlah", jumlah.getText().toString());
                        jVer.put("atas_nama", nama.getText().toString());
                        jVer.put("bank", bankName);
                        jVer.put("bukti", bukti);

                        verifikasiTopup(jVer);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else{

                }
            }
        });
    }

    private boolean cekIsFill(EditText nama, EditText norek, EditText jumlah){
        boolean isFill = true;
        if(nama.getText().toString().equals("")){
            isFill = false;
        }
        if(norek.getText().toString().equals("")){
            isFill = false;
        }
        if(jumlah.getText().toString().equals("")){
            isFill = false;
        }
        if (bukti.equals("")){
            isFill = false;
            Toast.makeText(activity, "Masukkan bukti pembyaran", Toast.LENGTH_SHORT).show();
        }
        return isFill;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                upload.setEnabled(true);
            }
        }
    }

    public void changeFragment(Fragment frag, boolean addToBackStack) {
        FragmentManager fm = activity.getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.container_body, frag);
        if (addToBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }

    private ProgressDialog showLoading(){
        ProgressDialog ad = ProgressDialog.show(activity, "", "Loading...", true);
        return ad;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case TAKE_PICTURE:
                if (resultCode == Activity.RESULT_OK) {
//                    MimeTypeMap mime = MimeTypeMap.getSingleton();
//                    String ext = newFile.getName().substring(newFile.getName().lastIndexOf(".") + 1);
//                    String type = mime.getMimeTypeFromExtension(ext);
                    activity.getContentResolver().notifyChange(imageUri, null);
                    ContentResolver cr = activity.getContentResolver();
                    Bitmap bitmap;
                    try {
//                        Uri selectedImage = data.getData();
//                        String[] filePathColumn = { MediaStore.Images.Media.DATA };
//                                android.provider.MediaStore.Images.Media.getBitmap(cr, imageUri);
//                        Cursor cursor = cr.query(imageUri,
//                                filePathColumn, null, null, null);
//                        cursor.moveToFirst();
//                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
//                        String imgDecodableString = cursor.getString(columnIndex);
                        bitmap = android.provider.MediaStore.Images.Media.getBitmap(cr, imageUri);
//                        bitmap = decodeFile(imgDecodableString, 200);
//                        Log.d("after_comppres", String.valueOf(bitmap.getByteCount()));
                        bukti = compressJSON(bitmap);
                        if(!bukti.equals("")){
                            TextView centang = (TextView) activity.findViewById(R.id.centang);
                            centang.setVisibility(View.VISIBLE);

                            TextView btnupload = (TextView) activity.findViewById(R.id.butUploadBukti);
                            btnupload.setVisibility(View.GONE);


                        }

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
            Uri photoUri = FileProvider.getUriForFile(getContext(), getContext().getPackageName() + ".fileProvider", file);
            imageUri = photoUri;
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        }
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(intent, TAKE_PICTURE);
    }

    private Bitmap decodeFile(final String path, final int thumbnailSize) {
        Bitmap bitmap;
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, o);
        if ((o.outWidth == -1) || (o.outHeight == -1)) {
            bitmap = null;
        }

        int originalSize = (o.outHeight > o.outWidth) ? o.outHeight
                : o.outWidth;
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inSampleSize = originalSize / thumbnailSize;
        bitmap = BitmapFactory.decodeFile(path, opts);
        return bitmap;
    }


    public String compressJSON(Bitmap bmp){
        byte[] imageBytes0;
        ByteArrayOutputStream baos0 = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG ,50, baos0);
        imageBytes0 = baos0.toByteArray();
        String encodedImage= Base64.encodeToString(imageBytes0, Base64.DEFAULT);
        return encodedImage;
    }

    private void verifikasiTopup(final JSONObject jVer){

        final ProgressDialog pd= showLoading();

        HTTPHelper.getInstance(activity).verifikasiTopUp(jVer, new NetworkActionResult() {
            @Override
            public void onSuccess(JSONObject obj) {
                try {
                    if(obj.getString("message").equals("success")){
                        Toast.makeText(activity, "Terima kasih. Verifikasi akan segera diproses..", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(activity, "Verifikasi bermasalah..", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                pd.dismiss();
                maxRetry = 4;
            }

            @Override
            public void onFailure(String message) {

            }

            @Override
            public void onError(String message) {
                if(maxRetry == 0){
                    pd.dismiss();
                    showWarning();
                    maxRetry = 4;
                }else{
                    verifikasiTopup(jVer);
                    maxRetry--;
                    Log.d("Try_ke_verifikasi", String.valueOf(maxRetry));
                    pd.dismiss();
                }

            }
        });

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



}
