package edu.oregonstate.AiMLiteMobile.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import edu.oregonstate.AiMLiteMobile.Models.Action;
import edu.oregonstate.AiMLiteMobile.Models.CurrentUser;
import edu.oregonstate.AiMLiteMobile.Models.Note;
import edu.oregonstate.AiMLiteMobile.Models.WorkOrder;
import edu.oregonstate.AiMLiteMobile.R;

/**
 * Created by sellersk on 2/19/2015.
 */
public class ActionAdapter extends ArrayAdapter<Action> {
    private final static String TAG = "ActionAdapter";

    private final Context mContext;
    private ArrayList<Action> mActions;

    public ActionAdapter(Context c, ArrayList<Action> actions) {
        super(c, 0, actions);
        mContext = c;
        mActions = actions;

        /*%% DEBUG %%*/
        if(mActions.size() < 3) {
            WorkOrder workOrder = CurrentUser.get(mContext).getWorkOrders().get(0);
            Action newAction = new Action(workOrder, "Fixed some pipes under the sink.", "WORK COMPLETE", 4, new ArrayList<Note>());
            mActions.add(newAction);
            mActions.add(newAction);
            //mActions.add(newAction);
        }
        /*## END DEBUG %%*/
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_action_test, parent, false);
        }

        Action action = mActions.get(position);
        /*OLD COLOR SETTING FOR SYNCED/UNSYNCED */
/*        RelativeLayout relativeLayout = (RelativeLayout)convertView.findViewById(R.id.action_layout);
        if(action.isSynced()){
            relativeLayout.setBackgroundColor(mContext.getResources().getColor(R.color.synced_green));
        }else{
            relativeLayout.setBackgroundColor(mContext.getResources().getColor(R.color.unsynced_red));
        }*/

        //Populate the layout items with the action data
        ((TextView) convertView.findViewById(R.id.action_work_order_id)).setText(action.getWorkOrder().getProposalPhase());

        ((TextView) convertView.findViewById(R.id.action_taken)).setText(action.getActionTakenString());

        if (action.getUpdatedStatus() != null && action.getUpdatedStatus() != action.getWorkOrder().getStatus()){
            ((TextView) convertView.findViewById(R.id.action_oldStatus)).setText(action.getWorkOrder().getStatus());
            ((TextView) convertView.findViewById(R.id.action_newStatus)).setText(action.getUpdatedStatus());
        } else {
            convertView.findViewById(R.id.action_oldStatus).setVisibility(View.GONE);
            convertView.findViewById(R.id.action_changed_arrow).setVisibility(View.GONE);
            ((TextView) convertView.findViewById(R.id.action_newStatus)).setText(action.getWorkOrder().getStatus());
        }


        ((TextView) convertView.findViewById(R.id.action_timeSince)).setText(prettyPrintDate(action.getDateStamp()));


        ((TextView) convertView.findViewById(R.id.action_hours)).setText(String.valueOf(action.getHours()));

        ((TextView) convertView.findViewById(R.id.action_notes_count)).setText(String.valueOf(action.getNotes().size() + " Notes"));

        return convertView;
    }

    private String prettyPrintDate(Date date){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd-yyyy");
        return simpleDateFormat.format(date);
    }
}
