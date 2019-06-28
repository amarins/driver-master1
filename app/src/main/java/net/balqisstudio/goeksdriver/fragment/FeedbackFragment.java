package net.balqisstudio.goeksdriver.fragment;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import net.balqisstudio.goeksdriver.MainActivity;
import net.balqisstudio.goeksdriver.R;
import net.balqisstudio.goeksdriver.adapter.FeedbackAdapter;
import net.balqisstudio.goeksdriver.adapter.ItemListener;
import net.balqisstudio.goeksdriver.database.DBHandler;
import net.balqisstudio.goeksdriver.database.Queries;
import net.balqisstudio.goeksdriver.model.Driver;
import net.balqisstudio.goeksdriver.model.Feedback;
import net.balqisstudio.goeksdriver.network.HTTPHelper;
import net.balqisstudio.goeksdriver.network.Log;
import net.balqisstudio.goeksdriver.network.NetworkActionResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import at.blogc.android.views.ExpandableTextView;


public class FeedbackFragment extends Fragment{
    private static final String TAG = FeedbackFragment.class.getSimpleName();
    private View rootView;
    MainActivity activity;

    LinearLayout butExpanding;
    ImageView tempFeed;
    ExpandableTextView etv;
    ArrayList<Feedback> arrFeedback;
    private ItemListener.OnItemTouchListener onItemTouchListener;
    private RecyclerView reviFeedBack;
    private FeedbackAdapter feedbackAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private SwipeRefreshLayout swipe;
    Queries que;
    int maxRetry = 4;

    public FeedbackFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.content_feedback, container, false);

        activity = (MainActivity) getActivity();
        activity.getSupportActionBar().setTitle("Feedback");

        reviFeedBack = (RecyclerView) rootView.findViewById(R.id.reviFeedback);
        reviFeedBack.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(activity);
        que = new Queries(new DBHandler(activity));
        arrFeedback = que.getAllFeedback();
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

        return rootView;
    }

    private void initData(){
        Driver driver = que.getDriver();
        final ProgressDialog sl = showLoading();
        JSONObject jFeed = new JSONObject();
        try {
            jFeed.put("id", driver.id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        HTTPHelper.getInstance(activity).getFeedback(jFeed, new NetworkActionResult() {
            @Override
            public void onSuccess(JSONObject obj) {
                arrFeedback = HTTPHelper.getInstance(activity).parseFeedback(obj);
                que.truncate(DBHandler.TABLE_FEEDBACK);
                que.insertFeedback(arrFeedback);
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
                    Toast.makeText(activity, "Koneksi bermasalah..", Toast.LENGTH_SHORT).show();
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
                ExpandableTextView etv = (ExpandableTextView) view.findViewById(R.id.isiFeedback);
                ImageView more = (ImageView) view.findViewById(R.id.expandBut);
                etv.setInterpolator(new OvershootInterpolator());
                etv.toggle();
                more.setImageResource(etv.isExpanded() ? R.drawable.ic_expandable : R.drawable.ic_collapseable);
                more.setColorFilter(Color.WHITE);
            }

            @Override
            public void onButton2Click(View view, int position) {
            }
        };
    }

    private void updateListView(){
        reviFeedBack.setLayoutManager(mLayoutManager);
        feedbackAdapter = new FeedbackAdapter(arrFeedback, onItemTouchListener);
        reviFeedBack.setAdapter(feedbackAdapter);
        reviFeedBack.setVerticalScrollbarPosition(arrFeedback.size()-1);
    }

    private void notifyDataSetChanged(){
        feedbackAdapter.notifyDataSetChanged();
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


    public void changeFragment(Fragment frag, boolean addToBackStack) {
        FragmentManager fm = activity.getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.container_body, frag);
        if (addToBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }

    private ProgressDialog showLoading(){
        ProgressDialog ad = ProgressDialog.show(activity, "", "Loading...", true);
        return ad;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
