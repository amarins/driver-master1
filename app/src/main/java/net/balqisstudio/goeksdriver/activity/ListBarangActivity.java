package net.balqisstudio.goeksdriver.activity;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import net.balqisstudio.goeksdriver.R;
import net.balqisstudio.goeksdriver.adapter.BarangBelanjaAdapter;
import net.balqisstudio.goeksdriver.adapter.ItemListener;
import net.balqisstudio.goeksdriver.database.DBHandler;
import net.balqisstudio.goeksdriver.database.Queries;
import net.balqisstudio.goeksdriver.model.BarangBelanja;
import net.balqisstudio.goeksdriver.network.Log;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class ListBarangActivity extends AppCompatActivity {
    ArrayList<BarangBelanja> arrBarangBelanja;
    private ItemListener.OnItemTouchListener onItemTouchListener;
    private RecyclerView reviBarangBelanja;
    private BarangBelanjaAdapter barangBelanjaAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    Queries que;
    ListBarangActivity activity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_barang);
        activity = ListBarangActivity.this;
        activity.getSupportActionBar().setTitle("Daftar Barang Belanja");

        reviBarangBelanja = (RecyclerView) findViewById(R.id.reviListBarang);
        reviBarangBelanja.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(activity);
        que = new Queries(new DBHandler(activity));
        arrBarangBelanja = que.getAllBarangBelanja();
        Log.d("Isi_barang", arrBarangBelanja.get(0).nama_barang+" "+arrBarangBelanja.get(0).isChecked);
        TextView estimasiBiaya = (TextView) findViewById(R.id.estimasiBiaya);
        TextView namaToko = (TextView) findViewById(R.id.namaToko);

        namaToko.setText("Toko "+getIntent().getStringExtra("nama_toko"));
        estimasiBiaya.setText("Estimasi Biaya : "+amountAdapter(getIntent().getIntExtra("estimasi_biaya", 0)));
        initListener();
        updateListView();
    }

    private void initListener() {
        onItemTouchListener = new ItemListener.OnItemTouchListener() {
            @Override
            public void onCardViewTap(View view, int position) {
            }

            @Override
            public void onButton1Click(View view, final int position) {
//                CheckBox cekBarang = (CheckBox) view.findViewById(R.id.cekBarang);
//                cekBarang.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        if(arrBarangBelanja.get(position).isChecked == 0){
////                            arrBarangBelanja.get(position).isChecked = 1;
//                            que.checkedBarang(position, 1);
//                            Toast.makeText(activity, "Clicked 0", Toast.LENGTH_SHORT).show();
//                        }else{
//                            arrBarangBelanja.get(position).isChecked = 0;
//                            Toast.makeText(activity, "Clicked 1", Toast.LENGTH_SHORT).show();
//                            que.checkedBarang(position, 0);
//                        }
//                    }
//                });
//                cekBarang.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                    @Override
//                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//
//                    }
//                });
            }

            @Override
            public void onButton2Click(View view, int position) {
            }
        };
    }

    private void updateListView(){
        reviBarangBelanja.setLayoutManager(mLayoutManager);
        barangBelanjaAdapter = new BarangBelanjaAdapter(arrBarangBelanja, onItemTouchListener);
        reviBarangBelanja.setAdapter(barangBelanjaAdapter);
//        reviBarangBelanja.setVerticalScrollbarPosition(arrBarangBelanja.size()-1);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        que.closeDatabase();
    }

    private String amountAdapter(int amo){
        return "$ "+ NumberFormat.getNumberInstance(Locale.GERMANY).format(amo)+",-";
    }
}
