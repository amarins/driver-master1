package net.balqisstudio.goeksdriver;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.navigation.NavigationView;
import androidx.fragment.app.Fragment;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.siyamed.shapeimageview.CircularImageView;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;

import net.balqisstudio.goeksdriver.database.DBHandler;
import net.balqisstudio.goeksdriver.database.Queries;
import net.balqisstudio.goeksdriver.fragment.DashboardFragment;
import net.balqisstudio.goeksdriver.fragment.DepositFragment;
import net.balqisstudio.goeksdriver.fragment.FeedbackFragment;
import net.balqisstudio.goeksdriver.fragment.RiwayatTransaksiFragment;
import net.balqisstudio.goeksdriver.fragment.OrderFragment;
import net.balqisstudio.goeksdriver.fragment.SettingFragment;
import net.balqisstudio.goeksdriver.fragment.WithdrawFragment;
import net.balqisstudio.goeksdriver.model.Content;
import net.balqisstudio.goeksdriver.model.Driver;
import net.balqisstudio.goeksdriver.model.Kendaraan;
import net.balqisstudio.goeksdriver.model.TransaksiMcar;
import net.balqisstudio.goeksdriver.network.AsyncTaskHelper;
import net.balqisstudio.goeksdriver.network.HTTPHelper;
import net.balqisstudio.goeksdriver.network.Log;
import net.balqisstudio.goeksdriver.network.NetworkActionResult;
import net.balqisstudio.goeksdriver.preference.KendaraanPreference;
import net.balqisstudio.goeksdriver.preference.SettingPreference;
import net.balqisstudio.goeksdriver.preference.UserPreference;
import net.balqisstudio.goeksdriver.service.LocationService;
import net.balqisstudio.goeksdriver.service.MyConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    Toolbar toolbar;
    MainActivity activity;

    DrawerLayout drawer;
//    UserPreference up;
    Driver driver;
    public boolean statusFragment, ordering = false;
    CircularImageView imageDriver;
    public TextView saldo, textRating;
    Intent service;
    ProgressDialog pd;
    int maxRetry1 = 4;

    private static final int REQUEST_PERMISSION_LOCATION = 991;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle recv = getIntent().getExtras();
        activity = MainActivity.this;
//        up = new UserPreference(activity);
        Queries que = new Queries(new DBHandler(activity));
        driver = que.getDriver();
        que.closeDatabase();

        drawerLayout();

        if (savedInstanceState == null && recv != null) {
            if (recv.getString("SOURCE").equals(MyConfig.orderFragment)) {
                changeFragment(new OrderFragment(), false);
            }else if(recv.getString("SOURCE").equals(MyConfig.dashFragment)){
                if(recv.getInt("response") == 2){
                    Toast.makeText(activity, "Transaksi Dibatalkan", Toast.LENGTH_LONG).show();
                }
                changeFragment(new DashboardFragment(), false);
            }
        }else{
            if(driver.status == 2 || driver.status == 3){
                changeFragment(new OrderFragment(), false);
            }else{
                changeFragment(new DashboardFragment(), false);
            }
        }
        if (ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION_LOCATION);
            return;
        }
        service = new Intent(this, LocationService.class);
        startService(service);
    }

    private void drawerLayout(){
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navViewLeft = (NavigationView) findViewById(R.id.nav_view);
        navViewLeft.setNavigationItemSelectedListener(this);

        int width = getResources().getDisplayMetrics().widthPixels*8/10;
        DrawerLayout.LayoutParams drawLeftParam = (DrawerLayout.LayoutParams) navViewLeft.getLayoutParams();
        drawLeftParam.width = width;
        navViewLeft.setLayoutParams(drawLeftParam);

        View headerL = navViewLeft.getHeaderView(0);
        initMenuDrawer(headerL);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSION_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent service = new Intent(this, LocationService.class);
                startService(service);
            } else {
                Toast.makeText(activity, "Gagal menggunakan servis lokasi.", Toast.LENGTH_SHORT).show();
                // TODO: 10/15/2016 Tell user to use GPS
            }
        }
    }

    public void changeFragment(Fragment frag, boolean addToBackStack) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        transaction.replace(R.id.container_body, frag);
        if (addToBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        loadImageFromStorage(imageDriver);
    }

    private void initMenuDrawer(View headerL){
        TextView menuRW, menuDeposit, menuWithdraw, menuPerforma,
                menuRating, menuBooking, menuInbox, menuFeedback, menuAccount, namaDriver, namaKendaraan, platNomor;
        ImageView mobilAktif;

        namaDriver = (TextView) headerL.findViewById(R.id.namaDriver);
        namaKendaraan = (TextView) headerL.findViewById(R.id.carName);
        platNomor = (TextView) headerL.findViewById(R.id.carPlat);
        saldo = (TextView) headerL.findViewById(R.id.saldoDriver);
        imageDriver = (CircularImageView) headerL.findViewById(R.id.imageDriver);
        menuRW = (TextView) headerL.findViewById(R.id.menu_rw);
        menuDeposit = (TextView) headerL.findViewById(R.id.menu_deposit);
        menuWithdraw = (TextView) headerL.findViewById(R.id.menu_withdraw);
//        menuPerforma = (CardView) headerL.findViewById(R.id.menu_performa);
        menuRating = (TextView) headerL.findViewById(R.id.menu_rating);
        menuBooking = (TextView) headerL.findViewById(R.id.menu_booking);
        menuInbox = (TextView) headerL.findViewById(R.id.menu_inbox);
        menuFeedback = (TextView) headerL.findViewById(R.id.menu_feedback);
        menuAccount = (TextView) headerL.findViewById(R.id.menu_account);
        textRating = (TextView) headerL.findViewById(R.id.textRating);


        SettingPreference sp = new SettingPreference(activity);







        loadImageFromStorage(imageDriver);
        Kendaraan myRide = new KendaraanPreference(this).getKendaraan();
        namaDriver.setText(driver.name);
        textRating.setText(convertJarak(Double.parseDouble(driver.rating))+" / 5");
        saldo.setText(amountAdapter(driver.deposit));
        namaKendaraan.setText(myRide.merek);
        platNomor.setText(myRide.nopol);

        menuRW.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ordering){
                }else{
                    changeFragment(new RiwayatTransaksiFragment(), false);
                    statusFragment = true;
                }
                closeLeftDrawer();

            }
        });
        menuInbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent toNotif = new Intent(activity, ChatActivity.class);
//                startActivity(toNotif);
                if(ordering){
                }else{
                }
                closeLeftDrawer();
//                statusFragment = true;
            }
        });
        menuDeposit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(ordering){
                }else{
                    changeFragment(new DepositFragment(), false);
                    statusFragment = true;
                }
                closeLeftDrawer();

            }
        });
        menuBooking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent toRate = new Intent(activity, RatingUserActivity.class);
//                startActivity(toRate);
                if(ordering){
                }else {
                }
                closeLeftDrawer();
//                statusFragment = true;
            }
        });
        menuRating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                changeFragment(new OrderFragment(), false);
                if(ordering){
                }else{
//                    Intent toChat = new Intent(activity, ChatActivity.class);
//                    startActivity(toChat);
                }
                closeLeftDrawer();
//                statusFragment = true;
            }
        });
//        menuPerforma.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
////                sendResponse(106);
//                closeLeftDrawer();
//                statusFragment = true;
//            }
//        });

        menuWithdraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ordering){
                }else{
                    changeFragment(new WithdrawFragment(), false);
                    statusFragment = true;
                }
                closeLeftDrawer();
            }
        });
        menuFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ordering){
                }else {
                    changeFragment(new FeedbackFragment(), false);
                    statusFragment = true;
                }
                closeLeftDrawer();
            }
        });
        menuAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ordering){
                }else {
                    changeFragment(new SettingFragment(), false);
                    statusFragment = true;
                }
                closeLeftDrawer();
            }
        });
    }

    private String convertJarak(Double jarak){
        int range = (int)(jarak*10);
        jarak = (double)range/10;
        return String.valueOf(jarak);
    }

    private String amountAdapter(int amo){
        return "$ "+NumberFormat.getNumberInstance(Locale.GERMANY).format(amo);
    }

    int status = -1;
    private void sendResponse(final int acc){
        final String myCGM = new UserPreference(activity).getDriver().gcm_id;
        AsyncTaskHelper asyncTask = new AsyncTaskHelper(activity, true);
        asyncTask.setAsyncTaskListener(new AsyncTaskHelper.OnAsyncTaskListener() {
            @Override
            public void onAsyncTaskDoInBackground(AsyncTaskHelper asyncTask) {
                Map<String, String> dd = new TransaksiMcar().dataDummy();
                dd.put("reg_id_pelanggan", new UserPreference(activity).getDriver().gcm_id);
                Content content = new Content();
                content.addRegId(myCGM);
                content.createDataDummy(dd);
                status = HTTPHelper.sendToGCMServer(content);
            }

            @Override
            public void onAsyncTaskProgressUpdate(AsyncTaskHelper asyncTask) {
            }

            @Override
            public void onAsyncTaskPostExecute(AsyncTaskHelper asyncTask) {
                if (status == 1){
                    Toast.makeText(activity, "Message Sent", Toast.LENGTH_SHORT).show();
                }else if(status == 0){
                    Toast.makeText(activity, "Message sending failed", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onAsyncTaskPreExecute(AsyncTaskHelper asyncTask) {

            }
        });
        asyncTask.execute();
    }

    private void closeLeftDrawer(){
        drawer.closeDrawer(GravityCompat.START);
    }

    private void closeRightDrawer(){
        drawer.closeDrawer(GravityCompat.END);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            drawer.openDrawer(GravityCompat.END);
            closeLeftDrawer();
            return true;
        }
//        if (id == R.id.action_refresh) {
//            syncronizingAccount();
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
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
    public void onBackPressed() {
        if(!ordering){
            exitByBackKey();
        }
    }

    protected void exitByBackKey() {

        if(statusFragment){
            changeFragment(new DashboardFragment(), false);
            statusFragment = false;
        }else{
            if(drawer.isDrawerOpen(GravityCompat.START) || drawer.isDrawerOpen(GravityCompat.END)){
                closeLeftDrawer();
            }else{
                showWarnExit();
            }
        }
    }

    private MaterialDialog showWarnExit(){
        final MaterialDialog md = new  MaterialDialog.Builder(activity)
                .title("alert!")
                .content("Do you want to leave the application? You will not receive an order.")
                .icon(new IconicsDrawable(activity)
                        .icon(FontAwesome.Icon.faw_exclamation_triangle)
                        .color(Color.RED)
                        .sizeDp(24))
                .positiveText("Yes")
                .positiveColor(Color.BLUE)
                .negativeColor(Color.DKGRAY)
                .negativeText("No")
                .show();

        View positive = md.getActionButton(DialogAction.POSITIVE);
        View negative = md.getActionButton(DialogAction.NEGATIVE);

        positive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                md.dismiss();
                Queries que = new Queries(new DBHandler(activity));
                Driver dr = que.getDriver();
                if(dr.status == 4){
                    activity.finish();
                    stopService(service);
                }else{
                    pd = showLoading();
                    turningTheJob(false);
                }
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

    private ProgressDialog showLoading(){
        ProgressDialog ad = ProgressDialog.show(activity, "", "Loading...", true);
        return ad;
    }

    private void initializeRigthDrawer(Button butAutoBid){
        SettingPreference sp = new SettingPreference(this);
        if(sp.getSetting()[0].equals("OFF")){
            butAutoBid.setText("OFF");
        }else{
            butAutoBid.setText("ON");
        }
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
                try {
                    if(obj.getString("message").equals("banned")){
                        showMessage(true, "Alert!", "Your account is currently being suspended, please contact our office immediately!");
                    }else if(obj.getString("message").equals("success")){
                        turningActOff();
                    }else{
                        Toast.makeText(activity, "Already Off", Toast.LENGTH_SHORT).show();
                        turningActOff();
                    }
                    pd.dismiss();
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
                    showMessage(false, "Sorry", "A network error occurred, please try again!");
                    pd.dismiss();
                    maxRetry1 = 4;
                }else{
                    turningTheJob(false);
                    Log.d("try_ke", String.valueOf(maxRetry1));
                    maxRetry1--;
                }
            }
        });
    }

    private void turningActOff(){
        Queries que = new Queries(new DBHandler(activity));
        que.updateStatus(4);
        que.closeDatabase();
        activity.finish();
        stopService(service);
    }

    private MaterialDialog showMessage(final boolean exit, String title, String message){
        final MaterialDialog md  = new  MaterialDialog.Builder(activity)
                .title(title)
                .content(message)
                .icon(new IconicsDrawable(activity)
                        .icon(FontAwesome.Icon.faw_exclamation_triangle)
                        .color(Color.GREEN)
                        .sizeDp(24))
                .positiveText("Close")
                .positiveColor(Color.DKGRAY)
                .show();

        View positive = md.getActionButton(DialogAction.POSITIVE);
//        View negative = md.getActionButton(DialogAction.NEGATIVE);

        positive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                md.dismiss();
                if(exit){
                    turningActOff();
                }
            }
        });

        return md;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
