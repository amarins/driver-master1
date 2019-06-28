package net.balqisstudio.goeksdriver.fragment;

import android.app.ProgressDialog;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import net.balqisstudio.goeksdriver.MainActivity;
import net.balqisstudio.goeksdriver.R;
import net.balqisstudio.goeksdriver.database.DBHandler;
import net.balqisstudio.goeksdriver.database.Queries;
import net.balqisstudio.goeksdriver.model.Driver;
import net.balqisstudio.goeksdriver.network.HTTPHelper;
import net.balqisstudio.goeksdriver.network.Log;
import net.balqisstudio.goeksdriver.network.NetworkActionResult;

import org.json.JSONException;
import org.json.JSONObject;


public class WithdrawFragment extends Fragment{
    private static final String TAG = WithdrawFragment.class.getSimpleName();
    private View rootView;
    MainActivity activity;
    Driver driver;
    int maxRetry = 4;

    public WithdrawFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.content_withdraw, container, false);

        activity = (MainActivity) getActivity();
        activity.getSupportActionBar().setTitle("Withdraw");
        Queries que = new Queries(new DBHandler(activity));
        driver = que.getDriver();
        que.closeDatabase();
        initView();
        return rootView;
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
    }


    private void initView() {
        TextView butSubmit;
        final EditText nominalWithdraw;

        butSubmit = (TextView) rootView.findViewById(R.id.butSubmitW);
        nominalWithdraw = (EditText) rootView.findViewById(R.id.nominalWithdraw);

        butSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(driver.no_rek.equals("")){
                    Toast.makeText(activity, "Please update your account data on the settings menu!", Toast.LENGTH_LONG).show();
                }else{
                    if(nominalWithdraw.getText().toString().equals("")){
                        Toast.makeText(activity, "Please enter a nominal withdrawal!", Toast.LENGTH_SHORT).show();
                    }else{
                        if(Integer.parseInt(nominalWithdraw.getText().toString()) > (driver.deposit - 50)){
                            Toast.makeText(activity, "The minimum required balance is $50", Toast.LENGTH_LONG).show();
                        }else{
                            withdrawal(nominalWithdraw.getText().toString());
                        }
                    }
                }
            }
        });
    }

    private ProgressDialog showLoading(){
        ProgressDialog ad = ProgressDialog.show(activity, "", "Loading...", true);
        return ad;
    }

    private void withdrawal(final String nominal){
        final ProgressDialog md1 = showLoading();
        JSONObject jWith = new JSONObject();
        try {
            jWith.put("jumlah", nominal);
            jWith.put("id_driver", driver.id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
//        Log.d("Update_withdrawal", jWith.toString());
        HTTPHelper.getInstance(activity).withdrawal(jWith, new NetworkActionResult() {
            @Override
            public void onSuccess(JSONObject obj) {
                try {
                    if(obj.getString("message").equals("success")){
                        Toast.makeText(activity, "Withdraw will be processed immediately..", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(activity, "Withdraw Failed", Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                md1.dismiss();
                maxRetry = 4;
            }


            @Override
            public void onFailure(String message) {
                md1.dismiss();
            }

            @Override
            public void onError(String message) {
                if(maxRetry == 0){
                    md1.dismiss();
                    Toast.makeText(activity, "Problem Connection..", Toast.LENGTH_SHORT).show();
                    maxRetry = 4;
                }else{
                    withdrawal(nominal);
                    maxRetry--;
                    Log.d("Try_ke_withdraw", String.valueOf(maxRetry));
                    md1.dismiss();
                }
            }
        });
    }
}