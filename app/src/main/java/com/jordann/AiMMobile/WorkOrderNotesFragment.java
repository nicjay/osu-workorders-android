package com.jordann.AiMMobile;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by sellersk on 8/19/2014.
 */
public class WorkOrderNotesFragment extends ListFragment {

    private static final String TAG = "WorkOrderNotesFragment";
    private Callbacks mCallbacks;
    private static CurrentUser sCurrentUser;
    private WorkOrder mWorkOrder;

    public interface Callbacks {
        void onWorkOrderUpdated(WorkOrder wo);
    }

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        mCallbacks = (Callbacks)activity;
    }

/*
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
*/

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mWorkOrder = ((WorkOrderDetailActivity)getActivity()).mWorkOrder;

        WorkOrderNotesAdapter adapter = new WorkOrderNotesAdapter(getActivity(), mWorkOrder.getNotes());

        setListAdapter(adapter);

    }
}