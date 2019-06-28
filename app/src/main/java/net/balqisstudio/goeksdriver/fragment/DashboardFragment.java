package net.balqisstudio.goeksdriver.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.siyamed.shapeimageview.CircularImageView;
import com.github.siyamed.shapeimageview.RoundedImageView;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;

import net.balqisstudio.goeksdriver.MainActivity;

import net.balqisstudio.goeksdriver.R;
import net.balqisstudio.goeksdriver.database.DBHandler;
import net.balqisstudio.goeksdriver.database.Queries;
import net.balqisstudio.goeksdriver.model.BarangBelanja;
import net.balqisstudio.goeksdriver.model.DestinasiMbox;
import net.balqisstudio.goeksdriver.model.Driver;
import net.balqisstudio.goeksdriver.model.MakananBelanja;
import net.balqisstudio.goeksdriver.model.Transaksi;
import net.balqisstudio.goeksdriver.network.HTTPHelper;
import net.balqisstudio.goeksdriver.network.Log;
import net.balqisstudio.goeksdriver.network.NetworkActionResult;
import net.balqisstudio.goeksdriver.preference.SettingPreference;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;


public class DashboardFragment extends Fragment implements OnMapReadyCallback, LocationListener {
    private static final String TAG = DashboardFragment.class.getSimpleName();

    private GoogleMap mMap;
    private View rootView;
    MainActivity activity;
    ImageView imageBekerja;
    Switch switchBekerja;
    private UiSettings mUiSettings;
    CircularImageView fotoDriver;
    TextView namaDriver;
    TextView driverWallet;
    TextView driveRating;
    TextView phoneDriver;
    TextView emailDriver;
    Button driverTopup;
    public boolean statusFragment, ordering = false;
    private static final int REQUEST_PERMISSION_LOCATION = 991;
    RoundedImageView imageDriver;
    FrameLayout switchWrapper;
    Location myLocation;
    Marker myMarker;
    boolean isOn = false;
    Driver driver;
    int maxRetry = 4;
    int maxRetry1 = 4;
    int maxRetry2 = 4;
    ProgressDialog pd, pd1;

    public DashboardFragment(){
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.content_main, container, false);
        setHasOptionsMenu(true);

        final Spinner uangBelanja;
        final Button butAutoBid;

        imageBekerja = (ImageView) rootView.findViewById(R.id.iconBekerja);
        switchBekerja = (Switch) rootView.findViewById(R.id.switch_bekerja);
        switchWrapper = (FrameLayout) rootView.findViewById(R.id.switch_wrapper);
        fotoDriver = (CircularImageView) rootView.findViewById(R.id.image_Driver);
        namaDriver = (TextView) rootView.findViewById(R.id.driver_name);
        driverWallet = (TextView) rootView.findViewById(R.id.wallet);
        driveRating = (TextView) rootView.findViewById(R.id.driver_rating);
        phoneDriver = (TextView) rootView.findViewById(R.id.phone_number);
        emailDriver = (TextView) rootView.findViewById(R.id.driver_email);
        uangBelanja = (Spinner) rootView.findViewById(R.id.spinMaximal);
        butAutoBid = (Button) rootView.findViewById(R.id.butAutoBid);
        activity = (MainActivity) getActivity();
        activity.getSupportActionBar().setTitle("GOEKS DRIVER");
        Queries quem = new Queries(new DBHandler(activity));
        driver = quem.getDriver();
        quem.closeDatabase();
        activate();
        pd = showLoading();
        syncronizingAccount();

        SettingPreference sp = new SettingPreference(activity);
        uangBelanja.setSelection(Integer.parseInt(sp.getSetting()[1]));

        uangBelanja.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                updateUangBelanja(i+1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        butAutoBid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SettingPreference sp = new SettingPreference(activity);
                if(sp.getSetting()[0].equals("OFF")){
                    sp.updateAutoBid("ON");
                    butAutoBid.setText("ON");
                }else{
                    sp.updateAutoBid("OFF");
                    butAutoBid.setText("OFF");
                }
            }
        });


        loadImageFromStorage(fotoDriver);
        driveRating.setText(convertJarak(Double.parseDouble(driver.rating))+" / 5");
        driverWallet.setText(amountAdapter(driver.deposit));
        namaDriver.setText(driver.name);
        phoneDriver.setText(driver.phone);
        emailDriver.setText(driver.email);



        switchWrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Queries que = new Queries(new DBHandler(activity));
                Driver driver = que.getDriver();
                que.closeDatabase();
                if(driver.status == 4){
                    pd1 = showLoading();
                    turningTheJob(true);
                }else{
                    showWarning();
                }
            }
        });
        return rootView;
    }

    private void initializeRigthDrawer(Button butAutoBid){
        SettingPreference sp = new SettingPreference(activity);
        if(sp.getSetting()[0].equals("OFF")){
            butAutoBid.setText("OFF");
        }else{
            butAutoBid.setText("ON");
        }
    }

    private void updateUangBelanja(final int uang){
        JSONObject jUang = new JSONObject();
        try {
            jUang.put("id_driver", driver.id);
            jUang.put("id_uang", uang);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final ProgressDialog pdi = showLoading();
        HTTPHelper.getInstance(activity).settingUangBelanja(jUang, new NetworkActionResult() {
            @Override
            public void onSuccess(JSONObject obj) {
                try {
                    if(obj.getString("message").equals("success")){
//                        Toast.makeText(activity, "Update Ok", Toast.LENGTH_SHORT).show();
                        new SettingPreference(activity).updateMaksimalBelanja(String.valueOf(uang-1));
                    }else{
//                        Toast.makeText(activity, "Update Fail", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                pdi.dismiss();
            }

            @Override
            public void onFailure(String message) {

            }

            @Override
            public void onError(String message) {
                pdi.dismiss();
//                Toast.makeText(activity, "Koneksi bermasalah", Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void loadImageFromStorage(CircularImageView civ){
        if(!driver.image.equals("")){
            ContextWrapper cw = new ContextWrapper(activity);
            File directory = cw.getDir("fotoDriver", Context.MODE_PRIVATE);
            File f = new File(directory, "profile.jpg");
            Bitmap circleBitmap = decodeFile(f);
            civ.setImageBitmap(circleBitmap);
        }
    }
    private Bitmap decodeFile(File f) {
        try {
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f), null, o);

            final int REQUIRED_SIZE=200;
            int scale = 1;
            while(o.outWidth / scale / 2 >= REQUIRED_SIZE &&
                    o.outHeight / scale / 2 >= REQUIRED_SIZE) {
                scale *= 2;
            }
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (FileNotFoundException e) {}
        return null;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
//        que.closeDatabase();
    }

    private MaterialDialog showWarning(){
        final MaterialDialog md  = new  MaterialDialog.Builder(activity)
                .title("Alert")
                .content("Are you sure?")
                .icon(new IconicsDrawable(activity)
                        .icon(FontAwesome.Icon.faw_exclamation_triangle)
                        .color(Color.RED)
                        .sizeDp(24))
                .positiveText("Yes")
                .negativeText("No")
                .positiveColor(Color.BLUE)
                .negativeColor(Color.DKGRAY)
                .show();

        View positive = md.getActionButton(DialogAction.POSITIVE);
        View negative = md.getActionButton(DialogAction.NEGATIVE);

        positive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pd1 = showLoading();
                turningTheJob(false);
                md.dismiss();
            }
        });
        negative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                md.dismiss();
            }
        });

        return md;
    }

    private MaterialDialog showMessage(String title, String message){
        final MaterialDialog md  = new  MaterialDialog.Builder(activity)
                .title(title)
                .content(message)
                .icon(new IconicsDrawable(activity)
                        .icon(FontAwesome.Icon.faw_exclamation_triangle)
                        .color(Color.GREEN)
                        .sizeDp(24))
                .positiveText("Tutup")
                .positiveColor(Color.DKGRAY)
                .show();

        View positive = md.getActionButton(DialogAction.POSITIVE);
//        View negative = md.getActionButton(DialogAction.NEGATIVE);

        positive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                md.dismiss();
            }
        });

        return md;
    }

    public void activate(){
        Queries que = new Queries(new DBHandler(activity));
        Driver driver = que.getDriver();
        que.closeDatabase();
        if(driver.status == 4){
            switchBekerja.setChecked(false);
        }else{
            switchBekerja.setChecked(true);
        }
    }

    private void turningOff(){
        Queries que = new Queries(new DBHandler(activity));

//        SettingPreference sp = new SettingPreference(activity);
        switchBekerja.setChecked(false);
//        sp.updateKerja("OFF");
        que.updateStatus(4);
        que.closeDatabase();
    }

    private void turningOn(){
        Queries que = new Queries(new DBHandler(activity));

//        SettingPreference sp = new SettingPreference(activity);
        switchBekerja.setChecked(true);
        que.updateStatus(1);
        que.closeDatabase();

//        sp.updateKerja("ON");
//        Intent service = new Intent(activity, LocationService.class);
//        activity.startService(service);
    }

    private void turningTheJob(final boolean action){
        JSONObject jTurn = new JSONObject();
        try {
            jTurn.put("is_turn", action);
            jTurn.put("id_driver", driver.id);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("JSON_turning_on", jTurn.toString());
        HTTPHelper.getInstance(activity).turningOn(jTurn, new NetworkActionResult() {
            @Override
            public void onSuccess(JSONObject obj) {
                android.util.Log.d("DashboardFragment", "onSuccess: " + obj);
                pd1.dismiss();
                maxRetry1 = 4;
                try {
                    if(obj.getString("message").equals("banned")){
                        showMessage("Sorry", "Currently at rest, please contact our office immediately!");
                        turningOff();
                    }else if(obj.getString("message").equals("success")){
                        if(action){
                            turningOn();
                            showMessage("Thank You", "Good luck again.");
                        }else{
                            turningOff();
                        }
                    }else{
                        activate();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(String message) {
            }

            @Override
            public void onError(String message) {
                if(maxRetry1 == 0){
                    showMessage("Sorry", "A network error occurred, please try again!");
                    pd1.dismiss();
                    maxRetry1 = 4;
                }else{
//                    pd = showLoading();
                    turningTheJob(action);
                    Log.d("try_ke_turn_off", String.valueOf(maxRetry1));
                    maxRetry1--;
                }
            }
        });
    }

    private ProgressDialog showLoading(){
        ProgressDialog ad = ProgressDialog.show(activity, "", "Loading...", true);
        return ad;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            pd = showLoading();
            syncronizingAccount();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void syncronizingAccount(){
        //get saldo
        //get status account
        //get get order

        JSONObject jSync = new JSONObject();
        try {
            jSync.put("id", driver.id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        HTTPHelper.getInstance(activity).syncAccount(jSync, new NetworkActionResult() {
            @Override
            public void onSuccess(JSONObject obj) {
                try {
                    if(obj.getString("message").equals("success")){
                        driver = HTTPHelper.getInstance(activity).parseUserSync(activity, obj.toString());
                        activity.saldo.setText(amountAdapter(driver.deposit));
                        activity.textRating.setText(convertJarak(Double.parseDouble(driver.rating))+" / 5");
                        Queries que = new Queries(new DBHandler(activity));
                        que.updateDeposit(driver.deposit);
                        que.updateRating(driver.rating);
                        que.updateStatus(driver.status);
                        Transaksi runTrans = HTTPHelper.getInstance(activity).parseTransaksi(activity, obj.toString());
                        if(!runTrans.id_transaksi.equals("0")){
                            selectRunTrans(runTrans);
                        }
                        que.closeDatabase();
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
//                pd.dismiss();
                if(maxRetry == 0){
                    Toast.makeText(activity, "Connection Error..", Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                    maxRetry = 4;
                }else{
//                    pd = showLoading();
                    syncronizingAccount();
                    Log.d("try_ke_sync ", String.valueOf(maxRetry));
                    maxRetry--;
                }

            }
        });
    }

    private String convertJarak(Double jarak){
        int range = (int)(jarak*10);
        jarak = (double)range/10;
        return String.valueOf(jarak);
    }

    private String amountAdapter(int amo){
        return "$ "+ NumberFormat.getNumberInstance(Locale.GERMANY).format(amo);
    }

    private void selectRunTrans(Transaksi runTruns){
        JSONObject jTrans = new JSONObject();
        try {
            jTrans.put("id_transaksi", runTruns.id_transaksi);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        switch (runTruns.order_fitur){
            case "3":{
                get_data_transaksi_mfood(jTrans, runTruns);
                break;
            }
            case "4":{
                get_data_transaksi_mmart(jTrans, runTruns);
                break;
            }
            case "5":{
                get_data_transaksi_msend(jTrans, runTruns);
                break;
            }
            case "6":{
                get_data_transaksi_mmassage(jTrans, runTruns);
                break;
            }
            case "7":{
                get_data_transaksi_mbox(jTrans, runTruns);
                break;
            }
            case "8":{
                get_data_transaksi_mservice(jTrans, runTruns);
                break;
            }
            default:{
                Queries que = new Queries(new DBHandler(activity));
                que.truncate(DBHandler.TABLE_IN_PROGRESS_TRANSAKSI);
                que.insertInProgressTransaksi(runTruns);
                que.closeDatabase();
                changeFragment(new OrderFragment(), false);
                break;
            }
        }
    }

    private void get_data_transaksi_mmart(final JSONObject jTrans, final Transaksi currTrans){
        final ProgressDialog pd = showLoading();
        HTTPHelper.getInstance(activity).getTransaksiMmart(jTrans, new NetworkActionResult() {
            @Override
            public void onSuccess(JSONObject obj) {
                Transaksi transaksi = HTTPHelper.parseDataMmart(currTrans, obj);
                ArrayList<BarangBelanja> arrBarang = HTTPHelper.parseBarangBelanja(obj);
                Queries que = new Queries(new DBHandler(activity));
                que.truncate(DBHandler.TABLE_IN_PROGRESS_TRANSAKSI);
                que.insertInProgressTransaksi(transaksi);
                que.truncate(DBHandler.TABLE_BARANG_BELANJA);
                que.insertBarangBelanja(arrBarang);
                que.closeDatabase();
                changeFragment(new OrderFragment(), false);
                pd.dismiss();
                maxRetry2 = 4;
            }

            @Override
            public void onFailure(String message) {
            }

            @Override
            public void onError(String message) {
                if(maxRetry2 == 0){
                    pd.dismiss();
                    Toast.makeText(activity, "Error Connection..", Toast.LENGTH_SHORT).show();
                    Log.d("data_sync_mmart", "Retrieving Data Null");
                    maxRetry2 = 4;
                }else{
                    get_data_transaksi_mmart(jTrans, currTrans);
                    maxRetry2--;
                    Log.d("Try_ke_data_mmart", String.valueOf(maxRetry2));
                    pd.dismiss();
                }
            }
        });
    }

    private void get_data_transaksi_mfood(final JSONObject jTrans, final Transaksi currTrans){
        final ProgressDialog pd = showLoading();
        HTTPHelper.getInstance(activity).getTransaksiMfood(jTrans, new NetworkActionResult() {
            @Override
            public void onSuccess(JSONObject obj) {
                Transaksi transaksi = HTTPHelper.parseDataMmart(currTrans, obj);
                ArrayList<MakananBelanja> arrBarang = HTTPHelper.parseMakananBelanja(obj);
                Queries que = new Queries(new DBHandler(activity));
                que.truncate(DBHandler.TABLE_IN_PROGRESS_TRANSAKSI);
                que.insertInProgressTransaksi(transaksi);
                que.truncate(DBHandler.TABLE_MAKANAN_BELANJA);
                que.insertMakananBelanja(arrBarang);
                que.closeDatabase();
                changeFragment(new OrderFragment(), false);
                pd.dismiss();
                maxRetry2 = 4;
            }

            @Override
            public void onFailure(String message) {
            }

            @Override
            public void onError(String message) {
                if(maxRetry2 == 0){
                    pd.dismiss();
                    Toast.makeText(activity, "Error Connection..", Toast.LENGTH_SHORT).show();
                    Log.d("data_sync_mfood", "Retrieving Data Null");
                    maxRetry2 = 4;
                }else{
                    get_data_transaksi_mfood(jTrans, currTrans);
                    maxRetry2--;
                    Log.d("Try_ke_data_mfood", String.valueOf(maxRetry2));
                    pd.dismiss();
                }
            }
        });
    }

    private void get_data_transaksi_mbox(final JSONObject jTrans, final Transaksi currTrans){
        final ProgressDialog pd = showLoading();
        HTTPHelper.getInstance(activity).getTransaksiMbox(jTrans, new NetworkActionResult() {
            @Override
            public void onSuccess(JSONObject obj) {
                Transaksi transaksi = HTTPHelper.parseDataMbox(currTrans, obj);
                ArrayList<DestinasiMbox> arrDestinasi = HTTPHelper.parseDestinasiMbox(obj);
                Queries que = new Queries(new DBHandler(activity));
                que.truncate(DBHandler.TABLE_IN_PROGRESS_TRANSAKSI);
                que.insertInProgressTransaksi(transaksi);
                que.truncate(DBHandler.TABLE_DESTINASI_MBOX);
                que.insertDestinasiMbox(arrDestinasi);
                que.closeDatabase();
                changeFragment(new OrderFragment(), false);
                pd.dismiss();
                maxRetry2 = 4;
            }

            @Override
            public void onFailure(String message) {
            }

            @Override
            public void onError(String message) {
                if(maxRetry2 == 0){
                    pd.dismiss();
                    Toast.makeText(activity, "Error Connection..", Toast.LENGTH_SHORT).show();
                    Log.d("data_sync_mbox", "Retrieving Data Null");
                    maxRetry2 = 4;
                }else{
                    get_data_transaksi_mbox(jTrans, currTrans);
                    maxRetry2--;
                    Log.d("Try_ke_data_mbox", String.valueOf(maxRetry2));
                    pd.dismiss();
                }
            }
        });
    }

    private void get_data_transaksi_msend(final JSONObject jTrans, final Transaksi currTrans){
        final ProgressDialog pd = showLoading();
        HTTPHelper.getInstance(activity).getTransaksiMsend(jTrans, new NetworkActionResult() {
            @Override
            public void onSuccess(JSONObject obj) {
                Transaksi transaksi = HTTPHelper.parseDataMsend(currTrans, obj);
                Queries que = new Queries(new DBHandler(activity));
                que.truncate(DBHandler.TABLE_IN_PROGRESS_TRANSAKSI);
                que.insertInProgressTransaksi(transaksi);
                que.closeDatabase();
                changeFragment(new OrderFragment(), false);
                pd.dismiss();
                maxRetry2 = 4;
            }

            @Override
            public void onFailure(String message) {
            }

            @Override
            public void onError(String message) {
                if(maxRetry2 == 0){
                    pd.dismiss();
                    Toast.makeText(activity, "Error Connection..", Toast.LENGTH_SHORT).show();
                    Log.d("data_sync_msend", "Retrieving Data Null");
                    maxRetry2 = 4;
                }else{
                    get_data_transaksi_msend(jTrans, currTrans);
                    maxRetry2--;
                    Log.d("Try_ke_data_msend", String.valueOf(maxRetry2));
                    pd.dismiss();
                }
            }
        });
    }

    private void get_data_transaksi_mservice(final JSONObject jTrans, final Transaksi currTrans){
        final ProgressDialog pd = showLoading();
        HTTPHelper.getInstance(activity).getTransaksiMservice(jTrans, new NetworkActionResult() {
            @Override
            public void onSuccess(JSONObject obj) {
                Transaksi transaksi = HTTPHelper.parseDataMservice(currTrans, obj);
                Queries que = new Queries(new DBHandler(activity));
                que.truncate(DBHandler.TABLE_IN_PROGRESS_TRANSAKSI);
                que.insertInProgressTransaksi(transaksi);
                que.closeDatabase();
                changeFragment(new OrderFragment(), false);
                pd.dismiss();
                maxRetry2 = 4;
            }

            @Override
            public void onFailure(String message) {
            }

            @Override
            public void onError(String message) {
                if(maxRetry2 == 0){
                    pd.dismiss();
                    Toast.makeText(activity, "Error Connection..", Toast.LENGTH_SHORT).show();
                    Log.d("data_sync_mservice", "Retrieving Data Null");
                    maxRetry2 = 4;
                }else{
                    get_data_transaksi_mservice(jTrans, currTrans);
                    maxRetry2--;
                    Log.d("Try_ke_data_mservice", String.valueOf(maxRetry2));
                    pd.dismiss();
                }
            }
        });
    }

    private void get_data_transaksi_mmassage(final JSONObject jTrans, final Transaksi currTrans){
        final ProgressDialog pd = showLoading();
        HTTPHelper.getInstance(activity).getTransaksiMmassage(jTrans, new NetworkActionResult() {
            @Override
            public void onSuccess(JSONObject obj) {
                Transaksi transaksi = HTTPHelper.parseDataMmassage(currTrans, obj);
                Queries que = new Queries(new DBHandler(activity));
                que.truncate(DBHandler.TABLE_IN_PROGRESS_TRANSAKSI);
                que.insertInProgressTransaksi(transaksi);
                que.closeDatabase();
                changeFragment(new OrderFragment(), false);
                pd.dismiss();
                maxRetry2 = 4;
            }

            @Override
            public void onFailure(String message) {
            }

            @Override
            public void onError(String message) {
                if(maxRetry2 == 0){
                    pd.dismiss();
                    Toast.makeText(activity, "Error Connection..", Toast.LENGTH_SHORT).show();
                    Log.d("data_sync_mmassage", "Retrieving Data Null");
                    maxRetry2 = 4;
                }else{
                    get_data_transaksi_mmassage(jTrans, currTrans);
                    maxRetry2--;
                    Log.d("Try_ke_data_mmassage", String.valueOf(maxRetry2));
                    pd.dismiss();
                }
            }
        });
    }

    public void changeFragment(Fragment frag, boolean addToBackStack) {
        FragmentManager fm = activity.getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        transaction.replace(R.id.container_body, frag);
        if (addToBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }


    @Override
    public void onLocationChanged(Location location) {
        Toast.makeText(activity, "Location Changed " + location.getLatitude()
                + location.getLongitude(), Toast.LENGTH_LONG).show();

        myLocation = location;
        if (myMarker != null) {
            myMarker.remove();
        }

        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        myMarker = mMap.addMarker(markerOptions);

        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));

        Log.d("IsMyLocationChange", "Yes : "+location.getLatitude()+" "+location.getLongitude());

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        updateLastLocation(true);
        setMyLocationLayerEnabled();

//        if(mMap != null){
//            LatLng myLoc = new LatLng(mMap.getMyLocation().getLatitude(), mMap.getMyLocation().getLongitude());
//            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLoc, 16));
//        }else{

    }

    private void updateLastLocation(boolean move) {
        if (ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION_LOCATION);
            return;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSION_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                setMyLocationLayerEnabled();
            } else {
                // TODO: 10/15/2016 Tell user to use GPS
            }
        }
    }

    public void setMyLocationLayerEnabled() {
        if (ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION_LOCATION);
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);

        LatLng makassar = new LatLng(-2.986167, 104.763305);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(makassar, 1));

        mUiSettings = mMap.getUiSettings();
        mUiSettings.setZoomControlsEnabled(false);
        mUiSettings.setAllGesturesEnabled(true);
        mUiSettings.setCompassEnabled(true);
        mUiSettings.setMyLocationButtonEnabled(true);

        LatLng start = new LatLng(-2.986167, 104.763305);
    }


}
