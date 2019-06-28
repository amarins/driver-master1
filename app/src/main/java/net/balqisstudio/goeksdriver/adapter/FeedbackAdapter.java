package net.balqisstudio.goeksdriver.adapter;

/**
 * Created by Balqis Studio on 27/11/2017.
 */

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.balqisstudio.goeksdriver.R;
import net.balqisstudio.goeksdriver.model.Feedback;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

public class FeedbackAdapter extends RecyclerView.Adapter<FeedbackAdapter.MyViewHolder> {

    private ItemListener.OnItemTouchListener onItemTouchListener;
    ArrayList<Feedback> prodList = new ArrayList<>();

    public FeedbackAdapter(ArrayList<Feedback> prodList, ItemListener.OnItemTouchListener onItemTouchListener){
        this.prodList = prodList;
        this.onItemTouchListener = onItemTouchListener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_feedback, parent, false);
        MyViewHolder viewHolder = new MyViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        timeConverter(holder, prodList.get(position).waktu);
        holder.catatan.setText(prodList.get(position).catatan);
    }

    private void timeConverter(MyViewHolder holder, long waktu){
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy");
        sdf.setTimeZone(TimeZone.getDefault());
        Date timing = new Date(waktu*1000);
        holder.waktu.setText(sdf.format(timing));
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

        public TextView waktu, catatan;
        public ImageView more;
        LinearLayout butExpanding;

        public MyViewHolder(View itemView){
            super(itemView);

            catatan = (TextView) itemView.findViewById(R.id.isiFeedback);
            waktu = (TextView) itemView.findViewById(R.id.waktuFeedback);
            butExpanding = (LinearLayout) itemView.findViewById(R.id.butExpanding);
            more = (ImageView) itemView.findViewById(R.id.expandBut);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemTouchListener.onCardViewTap(v, getLayoutPosition());

                }


            });

            butExpanding.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onItemTouchListener.onButton1Click(view, getLayoutPosition());
                }
            });

        }


    }





}