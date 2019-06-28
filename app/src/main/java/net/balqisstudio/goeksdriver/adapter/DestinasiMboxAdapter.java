package net.balqisstudio.goeksdriver.adapter;

/**
 * Created by Balqis Studio on 27/11/2017.
 */

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import net.balqisstudio.goeksdriver.R;
import net.balqisstudio.goeksdriver.model.DestinasiMbox;

import java.util.ArrayList;

public class DestinasiMboxAdapter extends RecyclerView.Adapter<DestinasiMboxAdapter.MyViewHolder> {

    private ItemListener.OnItemTouchListener onItemTouchListener;
    ArrayList<DestinasiMbox> prodList = new ArrayList<>();

    public DestinasiMboxAdapter(ArrayList<DestinasiMbox> prodList, ItemListener.OnItemTouchListener onItemTouchListener){
        this.prodList = prodList;
        this.onItemTouchListener = onItemTouchListener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_destinasi, parent, false);
        MyViewHolder viewHolder = new MyViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.urutanPenerima.setText("Penerima "+String.valueOf(position+1));
        holder.namaPenerima.setText(prodList.get(position).nama_penerima);
        holder.teleponPenerima.setText(prodList.get(position).telepon_penerima);
        holder.lokasi.setText(prodList.get(position).lokasi);
        holder.detailLokasi.setText(prodList.get(position).detail_lokasi);
        holder.instruksi.setText(prodList.get(position).instruksi);
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

        public TextView urutanPenerima, namaPenerima, teleponPenerima, lokasi, detailLokasi, instruksi;
        public Button butCall;

        public MyViewHolder(View itemView){
            super(itemView);

            urutanPenerima = (TextView) itemView.findViewById(R.id.urutanPenerima);
            namaPenerima = (TextView) itemView.findViewById(R.id.namaPenerima);
            teleponPenerima = (TextView) itemView.findViewById(R.id.teleponPenerima);
            lokasi = (TextView) itemView.findViewById(R.id.alamatPenerima);
            detailLokasi = (TextView) itemView.findViewById(R.id.detailAlamatPenerima);
            instruksi = (TextView) itemView.findViewById(R.id.instruksi);
            butCall = (Button) itemView.findViewById(R.id.callPenerima);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemTouchListener.onCardViewTap(v, getLayoutPosition());
                }


            });

            butCall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onItemTouchListener.onButton1Click(view, getLayoutPosition());
                }
            });
        }


    }





}