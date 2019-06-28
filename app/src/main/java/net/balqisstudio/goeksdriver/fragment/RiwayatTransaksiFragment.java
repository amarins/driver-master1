package net.balqisstudio.goeksdriver.fragment;

import android.app.ProgressDialog;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import net.balqisstudio.goeksdriver.MainActivity;
import net.balqisstudio.goeksdriver.R;
import net.balqisstudio.goeksdriver.adapter.ItemListener;
import net.balqisstudio.goeksdriver.adapter.RiwayatTransaksiAdapter;
import net.balqisstudio.goeksdriver.database.DBHandler;
import net.balqisstudio.goeksdriver.database.Queries;
import net.balqisstudio.goeksdriver.model.RiwayatTransaksi;
import net.balqisstudio.goeksdriver.network.HTTPHelper;
import net.balqisstudio.goeksdriver.network.Log;
import net.balqisstudio.goeksdriver.network.NetworkActionResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class RiwayatTransaksiFragment extends Fragment{
    private static final String TAG = RiwayatTransaksiFragment.class.getSimpleName();
    private View rootView;
    MainActivity activity;
    ArrayList<RiwayatTransaksi> arrRiwayat;
    private ItemListener.OnItemTouchListener onItemTouchListener;
    private RecyclerView reviRiwayat;
    private RiwayatTransaksiAdapter riwayatAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private SwipeRefreshLayout swipe;
    Queries que;
    int maxRetry = 4;

    public RiwayatTransaksiFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.riwayat_transaksi_fragments, container, false);

        activity = (MainActivity) getActivity();
        activity.getSupportActionBar().setTitle("History");

        que = new Queries(new DBHandler(activity));
        reviRiwayat = (RecyclerView) rootView.findViewById(R.id.reviRiwayat);
        reviRiwayat.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(activity);

        arrRiwayat = que.getAllRiwayatTransaksi();
//        initData();
        initListener();
        updateListView();

        swipe = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeFeedback);
        swipe.setRefreshing(false);
        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                initData();
            }
        });
        swipe.post(new Runnable() {
            @Override
            public void run() {
                initData();
            }
        });

        return rootView;
    }

    private void initData(){
        final ProgressDialog sl = showLoading();
        JSONObject jFeed = new JSONObject();
        try {
            jFeed.put("id", que.getDriver().id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        HTTPHelper.getInstance(activity).getRiwayatTransaksi(jFeed, new NetworkActionResult() {
            @Override
            public void onSuccess(JSONObject obj) {
                arrRiwayat = HTTPHelper.getInstance(activity).parseRiwayatTransaksi(obj);
                que.truncate(DBHandler.TABLE_RIWAYAT_TRANSAKSI);
                que.insertRiwayatTransaksi(arrRiwayat);
                swipe.setRefreshing(false);
                updateListView();
                sl.dismiss();
                maxRetry = 4;
            }

            @Override
            public void onFailure(String message) {
            }

            @Override
            public void onError(String message) {
                if(maxRetry == 0){
                    sl.dismiss();
                    Toast.makeText(activity, "Problem connection..", Toast.LENGTH_SHORT).show();
                    maxRetry = 4;
                    swipe.setRefreshing(false);
                }else{
                    initData();
                    maxRetry--;
                    Log.d("Try_ke_feedback", String.valueOf(maxRetry));
                    sl.dismiss();
                }
            }
        });
    }

    private void initListener() {
        onItemTouchListener = new ItemListener.OnItemTouchListener() {
            @Override
            public void onCardViewTap(View view, int position) {
            }

            @Override
            public void onButton1Click(View view, int position) {
            }

            @Override
            public void onButton2Click(View view, int position) {
            }
        };
    }

    private void updateListView(){
        reviRiwayat.setLayoutManager(mLayoutManager);
        arrRiwayat = que.getAllRiwayatTransaksi();
        riwayatAdapter = new RiwayatTransaksiAdapter(arrRiwayat, onItemTouchListener, activity);
        reviRiwayat.setAdapter(riwayatAdapter);
        reviRiwayat.setVerticalScrollbarPosition(arrRiwayat.size()-1);
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
        que.closeDatabase();
    }

    private ProgressDialog showLoading(){
        ProgressDialog ad = ProgressDialog.show(activity, "", "Loading...", true);
        return ad;
    }

}
