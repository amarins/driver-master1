package net.balqisstudio.goeksdriver.adapter;

/**
 * Created by Balqis Studio on 27/11/2017.
 */

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import net.balqisstudio.goeksdriver.R;
import net.balqisstudio.goeksdriver.model.BarangBelanja;

import java.util.ArrayList;

public class BarangBelanjaAdapter extends RecyclerView.Adapter<BarangBelanjaAdapter.MyViewHolder> {

    private ItemListener.OnItemTouchListener onItemTouchListener;
    ArrayList<BarangBelanja> prodList = new ArrayList<>();

    public BarangBelanjaAdapter(ArrayList<BarangBelanja> prodList, ItemListener.OnItemTouchListener onItemTouchListener){
        this.prodList = prodList;
        this.onItemTouchListener = onItemTouchListener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_barang, parent, false);
        MyViewHolder viewHolder = new MyViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {


        holder.jumlahBarang.setText(prodList.get(position).jumlah_barang);
        holder.namaBarang.setText(prodList.get(position).nama_barang);
//        if(prodList.get(position).isChecked == 1){
//            holder.isChecked.setChecked(true);
//        }else{
//            holder.isChecked.setChecked(false);
//        }
    }



    @Override
    public int getItemCount() {
        return prodList.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        public TextView jumlahBarang, namaBarang;
        public CheckBox isChecked;

        public MyViewHolder(View itemView){
            super(itemView);

            jumlahBarang = (TextView) itemView.findViewById(R.id.jumlahBarang);
            namaBarang = (TextView) itemView.findViewById(R.id.namaBarang);
//            isChecked = (CheckBox) itemView.findViewById(R.id.cekBarang);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemTouchListener.onCardViewTap(v, getLayoutPosition());

                }


            });

//            isChecked.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    onItemTouchListener.onButton1Click(view, getLayoutPosition());
//                }
//            });

        }


    }





}