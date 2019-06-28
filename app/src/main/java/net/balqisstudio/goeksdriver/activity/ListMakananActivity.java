package net.balqisstudio.goeksdriver.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;

import net.balqisstudio.goeksdriver.R;
import net.balqisstudio.goeksdriver.adapter.ItemListener;
import net.balqisstudio.goeksdriver.adapter.MakananBelanjaAdapter;
import net.balqisstudio.goeksdriver.database.DBHandler;
import net.balqisstudio.goeksdriver.database.Queries;
import net.balqisstudio.goeksdriver.model.MakananBelanja;
import net.balqisstudio.goeksdriver.network.Log;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class ListMakananActivity extends AppCompatActivity {
    ArrayList<MakananBelanja> arrMakananBelanja;
    private ItemListener.OnItemTouchListener onItemTouchListener;
    private RecyclerView reviMakananBelanja;
    private MakananBelanjaAdapter barangBelanjaAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    Queries que;
    ListMakananActivity activity;
    TextView estimasiBiaya, namaResto;
    Button callResto;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_makanan);
        activity = ListMakananActivity.this;
        activity.getSupportActionBar().setTitle("Daftar Belanja");

        reviMakananBelanja = (RecyclerView) findViewById(R.id.reviListBarang);
        reviMakananBelanja.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(activity);
        que = new Queries(new DBHandler(activity));
        arrMakananBelanja = que.getAllMakananBelanja();
        Log.d("Isi_makanan", arrMakananBelanja.get(0).nama_makanan);
        estimasiBiaya = (TextView) findViewById(R.id.estimasiBiaya);
        namaResto = (TextView) findViewById(R.id.namaResto);
        callResto = (Button) findViewById(R.id.callResto);

        estimasiBiaya.setText("Estimasi Biaya : " + amountAdapter(getIntent().getIntExtra("estimasi_biaya", 0)));
        namaResto.setText(getIntent().getStringExtra("nama_resto"));
        callResto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showWarningCall(getIntent().getStringExtra("telepon_resto"));
            }
        });

        initListener();
        updateListView();
    }

    private void initListener() {
        onItemTouchListener = new ItemListener.OnItemTouchListener() {
            @Override
            public void onCardViewTap(View view, int position) {
//                Toast.makeText(activity, "", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onButton1Click(View view, final int position) {
//                Toast.makeText(activity, "harga ke click", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onButton2Click(View view, int position) {
            }
        };
    }

    private void updateListView() {
        reviMakananBelanja.setLayoutManager(mLayoutManager);
        barangBelanjaAdapter = new MakananBelanjaAdapter(arrMakananBelanja, onItemTouchListener);
        reviMakananBelanja.setAdapter(barangBelanjaAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        que.closeDatabase();
    }

    private String amountAdapter(int amo) {
        return "$ " + NumberFormat.getNumberInstance(Locale.GERMANY).format(amo) + ",-";
    }

    private MaterialDialog showWarningCall(final String nomor) {
        final MaterialDialog md = new MaterialDialog.Builder(this)
                .title("Peringatan Biaya")
                .content("Apakah anda ingin menghubungi nomor ini?")
                .icon(new IconicsDrawable(this)
                        .icon(FontAwesome.Icon.faw_phone)
                        .color(Color.BLUE)
                        .sizeDp(24))
                .positiveText("Iya")
                .positiveColor(Color.BLUE)
                .negativeText("Batal")
                .negativeColor(Color.DKGRAY)
                .show();

        View positive = md.getActionButton(DialogAction.POSITIVE);
        View negative = md.getActionButton(DialogAction.NEGATIVE);

        positive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent phoneIntent = new Intent(Intent.ACTION_CALL);
                phoneIntent.setData(Uri.parse("tel:" + nomor));
                if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                startActivity(phoneIntent);
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
}
