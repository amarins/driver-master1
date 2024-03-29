package net.balqisstudio.goeksdriver.adapter;

/**
 * Created by Balqis Studio on 27/11/2017.
 */

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.balqisstudio.goeksdriver.R;
import net.balqisstudio.goeksdriver.model.MakananBelanja;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class MakananBelanjaAdapter extends RecyclerView.Adapter<MakananBelanjaAdapter.MyViewHolder> {

    private ItemListener.OnItemTouchListener onItemTouchListener;
    ArrayList<MakananBelanja> prodList = new ArrayList<>();

    public MakananBelanjaAdapter(ArrayList<MakananBelanja> prodList, ItemListener.OnItemTouchListener onItemTouchListener){
        this.prodList = prodList;
        this.onItemTouchListener = onItemTouchListener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_makanan, parent, false);
        MyViewHolder viewHolder = new MyViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {


        holder.jumlahBarang.setText(prodList.get(position).jumlah_makanan+"");
        holder.namaBarang.setText(prodList.get(position).nama_makanan);
        holder.hargaBarang.setText(amountAdapter(prodList.get(position).jumlah_makanan * prodList.get(position).harga_makanan));
//        if(prodList.get(position).isChecked == 1){
//            holder.isChecked.setChecked(true);
//        }else{
//            holder.isChecked.setChecked(false);
//        }
    }

    private String amountAdapter(int amo){
        return "$ "+ NumberFormat.getNumberInstance(Locale.GERMANY).format(amo)+",-";
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

        public TextView jumlahBarang, namaBarang, hargaBarang;

        public MyViewHolder(View itemView){
            super(itemView);

            jumlahBarang = (TextView) itemView.findViewById(R.id.jumlahBarang);
            namaBarang = (TextView) itemView.findViewById(R.id.namaBarang);
            hargaBarang = (TextView) itemView.findViewById(R.id.hargaBarang);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemTouchListener.onCardViewTap(v, getLayoutPosition());

                }


            });
            hargaBarang.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onItemTouchListener.onButton1Click(view, getLayoutPosition());
                }
            });
        }


    }





}