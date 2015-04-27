package edu.oregonstate.AiMLiteMobile;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/*
 * Created by jordan_n on 8/14/2014.
 */

public class TaskGetWorkOrders extends AsyncTask<String, Void, ResponsePair> {
    private static final String TAG = "TaskGetWorkOrders";

    private static NetworkHandler sNetworkHandler;
    private OnTaskCompleted listener;
    private boolean forceRefresh;
    private ArrayList<WorkOrder> mWorkOrders;
    private JSONArray json;
    private static CurrentUser sCurrentUser;
    private Context mContext;
    private Date retrievedDate;



    public interface OnTaskCompleted{
        void onTaskSuccess();
        void onNetworkFail();
        void onAuthenticateFail();
    }

    public TaskGetWorkOrders(OnTaskCompleted l, CurrentUser currentUser, Context context, boolean forceRefresh) {
        listener = l;
        this.sCurrentUser = currentUser;
        this.mContext = context;
        this.forceRefresh = forceRefresh;
        this.mWorkOrders = new ArrayList<WorkOrder>();
        retrievedDate = new Date();
        sNetworkHandler = NetworkHandler.get(mContext);
    }

    @Override
    protected void onPostExecute(final ResponsePair responsePair) {

        switch (responsePair.getStatus()) {
            case SUCCESS:
                Log.i(TAG, "Task Success");
                listener.onTaskSuccess();
                break;
            case AUTH_FAIL:
                Log.i(TAG, "Auth Fail");
                listener.onAuthenticateFail();
                break;
            case NET_FAIL:
                Log.i(TAG, "Net Fail");
                  listener.onNetworkFail();
                break;
            case JSON_FAIL:
                Log.i(TAG, "JSON Fail");
                // listener.onNetworkFail();//TODO: custom json failure handler
                break;
            case NO_DATA:
                Log.i(TAG, "No data");
                //  listener.onNetworkFail();//TODO: no network no data, should tell user to get network access
                break;
            default:
                break;
        }
    }

    protected ResponsePair doInBackground(final String... args) {
        ResponsePair responsePair = new ResponsePair(ResponsePair.Status.NONE, null);

        //Network available
        if (isNetworkOnline(mContext)) {

            Log.i(TAG, "Network is available");

            boolean needRefresh = isRefreshNeeded();
            Log.i(TAG, "Need normal refresh? " + needRefresh);
            Log.i(TAG, "Force refresh? " + forceRefresh);
            if (needRefresh || forceRefresh) {

                try {
                    responsePair = sNetworkHandler.downloadUrl(sCurrentUser.getURLGetAll(), true, responsePair, null);
                } catch (IOException e){
                    Log.e(TAG, e.toString()); //TODO: why is this giving malformedURL exception?
                }


                if (responsePair.getStatus() != ResponsePair.Status.SUCCESS) {
                    Log.e(TAG, "Connection error!");
                    return responsePair;
                }

                json = responsePair.getJarray();
            }
            else{
                //Have network but don't need refresh. Assume data exists locally. Is this even needed? Maybe from login?
                try {
                    Log.i(TAG, "Retrieving stored data");
                    json = new JSONArray(sCurrentUser.getPrefs().getString("jsondata", "[]"));
                    responsePair.setStatus(ResponsePair.Status.SUCCESS);
                }
                catch (JSONException e){
                    Log.e(TAG, "Failed to get stored data");
                    responsePair.setStatus(ResponsePair.Status.JSON_FAIL);
                    return responsePair;
                }
            }

        }

        //No network, try getting the stored data
        else if (sCurrentUser.getPrefs().contains("jsondata")) {

            try {
                Log.i(TAG, "Trying to get stored data");
                json = new JSONArray(sCurrentUser.getPrefs().getString("jsondata", "[]"));
                responsePair.setStatus(ResponsePair.Status.SUCCESS);
            }
            catch (JSONException e){
                Log.e(TAG, "Failed to get stored data");
                responsePair.setStatus(ResponsePair.Status.JSON_FAIL);
                return responsePair;
            }
        }

        //No network, no stored data. Bye!
        else {
            Log.e(TAG, "No network, no stored data");
            responsePair.setStatus(ResponsePair.Status.NO_DATA);
            return responsePair;
        }

        /*
        Only reached here if:
            1) there is network and refresh is needed
            2) there is network and refresh is not needed, and stored data exists
            3) no network, and stored data exists

        Flow of first time run to get data:
            There is network, refresh needed, fills data, saves local jsondata in prefs
        */

        if (json == null) {
            Log.e(TAG, "Json is null");
            //return false;
        }


        for (int i = 0; i < json.length(); i++) {
                try {
                    JSONObject mJsonObj = json.getJSONObject(i);
                    WorkOrder wo = new WorkOrder();
                    wo.setBeginDate(mJsonObj.getString("beg_dt"));
                    wo.setEndDate(mJsonObj.getString("end_dt"));
                    wo.setCraftCode(mJsonObj.getString("craft_code"));
                    wo.setShop(mJsonObj.getString("shop"));
                    wo.setBuilding(mJsonObj.getString("building"));
                    wo.setDescription(mJsonObj.getString("description"));
                    wo.setCategory(mJsonObj.getString("category"));
                    wo.setPriority(mJsonObj.getString("pri_code"));
                    wo.setDateElements(mJsonObj.getString("ent_date"));
                    wo.setStatus(mJsonObj.getString("status_code"));
                    wo.setContactName(mJsonObj.getString("contact"));
                    wo.setDepartment(mJsonObj.getString("department"));
                    wo.setProposalPhase(String.format("%s-%s", mJsonObj.getString("proposal"), mJsonObj.getString("sort_code")));


                    // %%%% TEST BLOCK - marks first 5 as daily
                    if (i < 5){
                        wo.setSection("Daily");
                    } else {
                        wo.setSection("Backlog");
                    }
                    // %%%%

                    mWorkOrders.add(wo);
                }
                catch (JSONException e) {
                    e.printStackTrace();
                    responsePair.setStatus(ResponsePair.Status.JSON_FAIL);
                    return responsePair;
                }
        }

        //Save the raw json array for offline use
        String jsonStr = json.toString();
        sCurrentUser.getPrefsEditor().putString("jsondata", jsonStr);

        //Save time in last updated
        Log.i(TAG, "Saving new last_updated: " + retrievedDate);
        sCurrentUser.getPrefsEditor().putLong("last_updated", retrievedDate.getTime());
        sCurrentUser.getPrefsEditor().apply();

        //Save the work orders array into current user
        sCurrentUser.setWorkOrders(mWorkOrders);

        return responsePair;
    }

    //Gets the last updated time from url, compares with stored time.
    private boolean isRefreshNeeded(){
        String lastUpdated = "";
        Date storedDate;

        //Get last_updated from stored prefs
        Long storedTime = sCurrentUser.getPrefs().getLong("last_updated", 0L);

        if (storedTime == 0L){
            //No stored time, need refresh
            return true;
        }
        storedDate = new Date(storedTime);
        Log.i(TAG, "Stored last_updated: " + storedDate);

        //Retrieve last_updated from updateUrl for user

        ResponsePair responsePair = new ResponsePair(ResponsePair.Status.NONE, null);
        try {
            responsePair = sNetworkHandler.downloadUrl(sCurrentUser.getURLGetLastUpdated(), false, responsePair, null);
        } catch (IOException e) {
            Log.e(TAG, e.toString()); //TODO: why is this giving malformedURL exception?
        }

        if (responsePair.getStatus() == ResponsePair.Status.SUCCESS){
            lastUpdated = responsePair.getReturnedString();
            retrievedDate = convertToDate(lastUpdated);
            Log.i(TAG, "Online last_updated: "+retrievedDate);
        }else{
            Log.e(TAG, "Connection error!");
            return true;
        }

        if (retrievedDate.after(storedDate)){
            //Retrieved time is new than stored time, need refresh
            Log.i(TAG, "Online date NEWER than stored date");
            return true;
        } else{
            //Retrieved time is not newer than stored time, no refresh
            Log.i(TAG, "Online date NOT newer than stored date");
            return false;
        }


    }


    private Date convertToDate(String dateString){
        dateString = dateString.replace("\"", "");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.US);
        Date convertedDate = new Date();
        try{
            convertedDate = dateFormat.parse(dateString);
        } catch (ParseException e){
            e.printStackTrace();
            Log.e(TAG, "Unable to parse dateString: " + dateString);
        }
        return convertedDate;
    }

    protected boolean isNetworkOnline(Context c) {
        boolean status = false;
        try {
            ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getNetworkInfo(0);
            if (netInfo != null && netInfo.getState() == NetworkInfo.State.CONNECTED) {
                status = true;
            } else {
                netInfo = cm.getNetworkInfo(1);
                if (netInfo != null && netInfo.getState() == NetworkInfo.State.CONNECTED)
                    status = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return status;
    }
}





