package net.balqisstudio.goeksdriver.adapter;

/**
 * Created by Balqis Studio on 11/8/2017.
 */

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.balqisstudio.goeksdriver.R;
import net.balqisstudio.goeksdriver.model.RiwayatTransaksi;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;


public class RiwayatTransaksiAdapter extends RecyclerView.Adapter<RiwayatTransaksiAdapter.MyViewHolder> {

    private ItemListener.OnItemTouchListener onItemTouchListener;
    ArrayList<RiwayatTransaksi> prodList = new ArrayList<>();
    Context context;

    public RiwayatTransaksiAdapter(ArrayList<RiwayatTransaksi> prodList, ItemListener.OnItemTouchListener onItemTouchListener, Context context){
        this.prodList = prodList;
        this.onItemTouchListener = onItemTouchListener;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_history, parent, false);
        MyViewHolder viewHolder = new MyViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        holder.tanggalRT.setText(tanggalAdapter(prodList.get(position).waktu_riwayat));
        holder.saldoAkhirRT.setText(amountAdapter(prodList.get(position).saldo));
        switch (prodList.get(position).tipe_transaksi){
            case "4":{
                holder.idRT.setVisibility(View.GONE);
                holder.namaRT.setText("Transaction : Topup");
                holder.keterangan.setText("Top Up Success");
                holder.nominalRT.setText(amountAdapter(prodList.get(position).kredit));
                break;
            }
            case "5":{
                holder.idRT.setText("ID "+  prodList.get(position).id_transaksi);
                holder.namaRT.setText(prodList.get(position).fitur);
                holder.keterangan.setText("MR/MRS "+prodList.get(position).nama_depan +" "+
                        "Distance "+convertJarak(Double.parseDouble(prodList.get(position).jarak)));
                holder.nominalRT.setText("-"+amountAdapter(prodList.get(position).debit));
                holder.nominalRT.setTextColor(context.getResources().getColor(R.color.textColorRed));
                break;
            }
            case "6":{
                holder.idRT.setText(prodList.get(position).id_transaksi);
                holder.namaRT.setText(prodList.get(position).fitur);
                holder.keterangan.setText("Payment");
                holder.nominalRT.setText("+"+amountAdapter(prodList.get(position).kredit));
                holder.nominalRT.setTextColor(context.getResources().getColor(R.color.textColorGreen));
                break;
            }
            case "7":{
                holder.idRT.setVisibility(View.GONE);
                holder.namaRT.setText("Transaction : Bonus");
                holder.keterangan.setText("Get Bonus");
                holder.nominalRT.setText("+"+amountAdapter(prodList.get(position).kredit));
                holder.nominalRT.setTextColor(context.getResources().getColor(R.color.textColorGreen));
                break;
            }
            case "8":{
                holder.idRT.setText(prodList.get(position).id_transaksi);
                holder.namaRT.setText(prodList.get(position).fitur);
                holder.keterangan.setText("Get Bonus "+prodList.get(position).nama_depan);
                holder.nominalRT.setText("+"+amountAdapter(prodList.get(position).kredit));
                holder.nominalRT.setTextColor(context.getResources().getColor(R.color.textColorGreen));
                break;
            }
            case "9":{
                holder.idRT.setVisibility(View.GONE);
                holder.namaRT.setText("Transaction : Fine");
                holder.keterangan.setText(prodList.get(position).keterangan);
                holder.nominalRT.setText("-"+amountAdapter(prodList.get(position).debit));
                holder.nominalRT.setTextColor(context.getResources().getColor(R.color.textColorRed));
                break;
            }
            default:
                break;
        }
    }

    @Override
    public int getItemCount() {
        return prodList.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    private String tanggalAdapter(String tgl){
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy");
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf.format(new Date(Long.parseLong(tgl)*1000));
    }

    private String amountAdapter(int amo){
        return "$ "+NumberFormat.getNumberInstance(Locale.GERMANY).format(amo)+",00";
    }

    private String convertJarak(Double jarak){
        int range = (int)(jarak*10);
        jarak = (double)range/10;
        return String.valueOf(jarak)+" KM";
    }

    private long timeAdapter(long timestamp){
        return (timestamp*1000);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        public TextView tanggalRT;
        public TextView idRT;
        public TextView namaRT;
        public TextView keterangan;
        public TextView nominalRT;
        public TextView saldoAkhirRT;

        public MyViewHolder(View itemView){
            super(itemView);
            tanggalRT = (TextView) itemView.findViewById(R.id.tanggalRT);
            idRT = (TextView) itemView.findViewById(R.id.idRT);
            namaRT = (TextView) itemView.findViewById(R.id.namaRT);
            keterangan = (TextView) itemView.findViewById(R.id.keterangRT);
            nominalRT = (TextView) itemView.findViewById(R.id.nominalRT);
            saldoAkhirRT = (TextView) itemView.findViewById(R.id.saldoAkhirRT);
            saldoAkhirRT = (TextView) itemView.findViewById(R.id.saldoAkhirRT);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemTouchListener.onCardViewTap(v, getLayoutPosition());
                }
            });

        }
    }
}