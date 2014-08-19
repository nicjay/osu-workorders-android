package com.jordann.AiMMobile;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;


public class WorkOrderListActivity extends SingleFragmentActivity implements WorkOrderListFragment.Callbacks, WorkOrderDetailFragment.Callbacks {

    @Override
    protected Fragment createFragment() {

        return new WorkOrderListFragment();
    }


    @Override
    protected int getLayoutResId() {
        return R.layout.activity_masterdetail;
    }


    public void onWorkOrderUpdated(WorkOrder wo) {
        FragmentManager fm = getFragmentManager();
        WorkOrderListFragment listFragment = (WorkOrderListFragment)
                fm.findFragmentById(R.id.fragmentContainer);
        listFragment.updateUI();
    }


    public void onWorkOrderSelected(WorkOrder wo){
        if (findViewById(R.id.detailFragmentContainer) == null) {
        // Start an instance of WorkOrderDetailActivity
            Intent i = new Intent(this, WorkOrderDetailActivity.class);
            i.putExtra(WorkOrderDetailFragment.WORK_ORDER_ID, wo.getId());
            startActivity(i);
        } else {
            FragmentManager fm = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            Fragment oldDetail = fm.findFragmentById(R.id.detailFragmentContainer);
            Fragment newDetail = WorkOrderDetailFragment.newInstance(wo.getId());
            if (oldDetail != null) {
                ft.remove(oldDetail);
            }
            ft.add(R.id.detailFragmentContainer, newDetail);
            ft.commit();
        }

    }


    @Override
    public void onBackPressed(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Choose your destiny");

        // Add the buttons
        builder.setPositiveButton("Logout", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Logout stuff
                finish();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // cancel
            }
        });



        AlertDialog dialog = builder.create();
        dialog.show();
    }

}
