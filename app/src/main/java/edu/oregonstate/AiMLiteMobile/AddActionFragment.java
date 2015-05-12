package edu.oregonstate.AiMLiteMobile;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by sellersk on 2/17/2015.
 */
public class AddActionFragment extends Fragment {
    public static final String TAG = "AddActionFragment";

    private boolean editMode; //False = add new action from scratch //True = edit existing action

    private Activity mActivity;
    private Context mContext;

    private static CurrentUser sCurrentUser;
    private static WorkOrder mWorkOrder;
    private static Action mActionToEdit;
    private static Action newAction;

    private static LinearLayout layout_action;
    private static TextView label_action;
    private static Spinner spinner_actionTaken;
    private static TextView actionCustomText;
    private static TextView textView_hours;
    private static int hoursEntered = -1;
    private static Spinner spinner_updateStatus;

    private static Button button_addNote;
    private static TextView notesAddedText;
    private static TextView notesExistingText;
    private static ListView notesListView;
    private static NoteAdapter notesAdapter;
    private static ArrayList<Note> newActionNotes;

    private int HOURS_MIN = 0;
    private int HOURS_MAX = 8;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mActivity = getActivity();
        mContext = mActivity.getApplicationContext();
        sCurrentUser = CurrentUser.get(mContext);

        Bundle bundle = getArguments();
        editMode = bundle.getBoolean("editMode");

        if (editMode){
            mActionToEdit = ((AddActionActivity) mActivity).getAction();
            if (mActionToEdit != null) mWorkOrder = mActionToEdit.getWorkOrder();
        } else {
            mWorkOrder = ((AddActionActivity) mActivity).getWorkOrder();
        }

        if (savedInstanceState != null){
            mWorkOrder = (WorkOrder) savedInstanceState.getSerializable("WorkOrder");
            mActionToEdit = (Action) savedInstanceState.getSerializable("Action");
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent,Bundle savedInstanceState) {
        return inflater.inflate(R.layout.action_add, parent, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        TextView workOrderIdText = (TextView)mActivity.findViewById(R.id.workOrderIdText);
        TextView workOrderLocationText = (TextView)mActivity.findViewById(R.id.workOrderLocationText);
        TextView workOrderDescriptionText = (TextView)mActivity.findViewById(R.id.workOrderDescriptionText);

        layout_action = (LinearLayout) mActivity.findViewById(R.id.action_layout);
        label_action = ((TextView)mActivity.findViewById(R.id.action_label));
        spinner_actionTaken = (Spinner)mActivity.findViewById(R.id.spinner_actionTaken);
        textView_hours = (TextView)mActivity.findViewById(R.id.hoursText);
        spinner_updateStatus = (Spinner)mActivity.findViewById(R.id.spinner_updateStatus);
        button_addNote = (Button)mActivity.findViewById(R.id.button_addNote);

        workOrderIdText.setText(mWorkOrder.getProposalPhase());
        workOrderLocationText.setText(mWorkOrder.getBuilding());
        workOrderDescriptionText.setText(mWorkOrder.getDescription());

        // Pre-populate fields as needed
        String defaultStatus;
        if (editMode){
            ArrayAdapter actionTakenAdapter = (ArrayAdapter) spinner_actionTaken.getAdapter();
            int defaultActionTakenSpinnerPosition = actionTakenAdapter.getPosition(mActionToEdit.getActionTakenString());
            spinner_actionTaken.setSelection(defaultActionTakenSpinnerPosition);

            defaultStatus = mActionToEdit.getUpdatedStatus(); //Sets the status spinner to the current status of the work order
            newActionNotes = mActionToEdit.getNotes();//Associate new notes with notes list view
            hoursEntered = mActionToEdit.getHours();

            if (hoursEntered != -1){
                textView_hours.setText(String.valueOf(hoursEntered));
            }
        } else {
            defaultStatus = mWorkOrder.getStatus(); //Sets the status spinner to the current status of the work order
            if (newActionNotes == null) newActionNotes = new ArrayList<Note>();//Associate new notes with notes list view
        }

        spinner_actionTaken.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                layout_action.setBackgroundResource(0);
                label_action.setTextColor(getResources().getColor(android.R.color.tertiary_text_light));
                if (position == 1) {
                    actionCustomText = (TextView) view;
                    createCustomActionEntryDialog();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        ArrayAdapter statusAdapter = (ArrayAdapter) spinner_updateStatus.getAdapter();
        int defaultStatusSpinnerPosition = statusAdapter.getPosition(defaultStatus);
        spinner_updateStatus.setSelection(defaultStatusSpinnerPosition);

        notesAdapter = new NoteAdapter(getActivity(), newActionNotes);
        notesAddedText = (TextView) mActivity.findViewById(R.id.notes_added_text);
        //notesExistingText = (TextView) mActivity.findViewById(R.id.notes_existing_text);
        //notesExistingText.setText(mWorkOrder.getNotes().size() + " existing");

        notesAddedText.setText(newActionNotes.size() + " added");
        if (newActionNotes.size() > 0 && notesAddedText.getVisibility() == View.INVISIBLE){
            notesAddedText.setVisibility(View.VISIBLE);
        }

        createHoursEntryDialog();

        button_addNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNoteEntryDialog(null);
            }
        });


    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable("WorkOrder", mWorkOrder);
        outState.putSerializable("Action", mActionToEdit);
        super.onSaveInstanceState(outState);
    }

    private void createHoursEntryDialog(){
        Button setHoursButton = (Button)getActivity().findViewById(R.id.button_setHours);
        setHoursButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder enterHoursAlert = new AlertDialog.Builder(mActivity);
                enterHoursAlert.setTitle("Enter hours:");
                final EditText input = new EditText(getActivity());
                input.setInputType(InputType.TYPE_CLASS_NUMBER);
                input.setFilters(new InputFilter[]{ new InputFilterMinMax(HOURS_MIN, HOURS_MAX), new InputFilter.LengthFilter(1)});
                input.setWidth(50);
                enterHoursAlert.setView(input);

                enterHoursAlert.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (input.getText() != null && !input.getText().toString().equals("")) {
                            Log.d(TAG, "input = "+ input.getText().toString());
                            textView_hours.setText(input.getText());
                            hoursEntered = Integer.valueOf(input.getText().toString());
                        }
                    }
                });
                enterHoursAlert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });

                AlertDialog alert = enterHoursAlert.create();
                alert.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                alert.show();
            }
        });

        Button clearHoursButton = (Button)getActivity().findViewById(R.id.button_clearHours);
        clearHoursButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textView_hours.setText("-");
                hoursEntered = -1;
            }
        });
    }

    private void createCustomActionEntryDialog(){
        final AlertDialog.Builder enterActionAlert = new AlertDialog.Builder(mActivity);
        enterActionAlert.setTitle("Enter action:");
        //TODO populate if editing
        final EditText input = new EditText(getActivity());
        input.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        input.setHorizontallyScrolling(false);
        input.setLines(6);
        input.setMinLines(6);
        input.setGravity(Gravity.TOP | Gravity.LEFT);
        input.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 26);
        enterActionAlert.setView(input);

        enterActionAlert.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                actionCustomText.setText(input.getText());
            }
        });

        enterActionAlert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        AlertDialog alert = enterActionAlert.create();
        alert.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        alert.show();
    }


    //PARAMS: toBeEditedText: set if 'editing an existing note'
    private void createNoteEntryDialog(final Note toBeEditedNote) {
        final AlertDialog.Builder enterNoteAlert = new AlertDialog.Builder(mActivity);
        enterNoteAlert.setTitle("Enter note:");

        //Populate input if editing
        final EditText input = new EditText(getActivity());
        if(toBeEditedNote != null){
            input.setText(toBeEditedNote.getNote());
        }


        input.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        input.setHorizontallyScrolling(false);
        input.setLines(6);
        input.setMinLines(6);
        input.setGravity(Gravity.TOP | Gravity.LEFT);
        input.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 26);
        enterNoteAlert.setView(input);

        enterNoteAlert.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Check for null input
                String noteString = input.getText().toString();
                if (!noteString.equals("")) {
                    if(toBeEditedNote != null){
                        //Update existing note
                        toBeEditedNote.setNote(noteString);
                        //Toast.makeText(mContext, "Note updated", Toast.LENGTH_SHORT).show();
                        SnackbarManager.show(Snackbar.with(getActivity()).text("Note updated").duration(Snackbar.SnackbarDuration.LENGTH_SHORT));
                    }else{
                        //Create noteObject and add to notesArray
                        Note newNote = new Note(noteString, sCurrentUser.getUsername(), new Date(System.currentTimeMillis()));
                        newNote.setNew();
                        newActionNotes.add(0, newNote);
                        //Toast.makeText(mContext, "Note added", Toast.LENGTH_SHORT).show();
                        SnackbarManager.show(Snackbar.with(getActivity()).text("Note added").duration(Snackbar.SnackbarDuration.LENGTH_SHORT));
                    }
                    notesAdapter.notifyDataSetChanged();
/*
                    if (notesListView.getVisibility() == View.INVISIBLE) {
                        notesListView.setVisibility(View.VISIBLE);
                    }
                    */
                    if (notesAddedText.getVisibility() == View.INVISIBLE) {
                        notesAddedText.setVisibility(View.VISIBLE);
                    }

                    notesAddedText.setText(newActionNotes.size()+" added");
                }
            }
        });

        enterNoteAlert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        AlertDialog alert = enterNoteAlert.create();
        alert.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        alert.show();

        //Sets cursor to end of input for editing when in EDIT mode
        if(toBeEditedNote != null) {
            input.setSelection(toBeEditedNote.getNote().length(), toBeEditedNote.getNote().length());
        }
    }


    private void createConfirmDialog(){
        final AlertDialog.Builder confirmAddActionDialog = new AlertDialog.Builder(mActivity);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_confirm, null);
        confirmAddActionDialog.setView(dialogView);

        Button confirmButton = (Button)dialogView.findViewById(R.id.dialogConfirm_buttonConfirm);
        Button cancelButton = (Button)dialogView.findViewById(R.id.dialogConfirm_buttonCancel);

        if (editMode){
            confirmAddActionDialog.setTitle("Save action changes");

            confirmAddActionDialog.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    //Create Action object from form fields
                    //Add new Action object to CurrentUser.Actions
                    //Return to QueueListFragment and update to show added Action

                    getActivity().finish();
                }
            });
        } else {
            confirmAddActionDialog.setTitle("Add new action");

            confirmAddActionDialog.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    //Create Action object from form fields
                    //Add new Action object to CurrentUser.Actions

                    sCurrentUser.addAction(newAction);




                    //Return to QueueListFragment and update to show added Action
                    Intent intent = new Intent(getActivity(), ActionQueueListActivity.class);
                    startActivity(intent);

                    getActivity().finish();
                }
            });
        }

        confirmAddActionDialog.setMessage("Confirm?");

        confirmAddActionDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });

        AlertDialog alert = confirmAddActionDialog.create();
        alert.show();
    }


    private void saveEdits(){
        String newStatus = mActionToEdit.getUpdatedStatus();

        //STATUS
       // if(checkBox_updateStatus.isChecked()){
            //Assign newStatus the value of selected Status from spinner
            newStatus = spinner_updateStatus.getSelectedItem().toString();
       // }// else newStatus stays null

        //ACTION
        String actionTaken = spinner_actionTaken.getSelectedItem().toString();

        //NOTE  -- handled in notes dialog. new notes added to newActionNotes arrayList

        //TODO: don't edit immediately, allow confirm first
        mActionToEdit.setActionTakenString(actionTaken);
        mActionToEdit.setHours(hoursEntered);
        mActionToEdit.setUpdatedStatus(newStatus);
        mActionToEdit.setNotes(newActionNotes);
        mActionToEdit.setDateStamp(new Date(System.currentTimeMillis()));

    }


    //Get all values from form. Create Action object. Add to CurrentUser.Actions
    //Displays error or success
    private boolean validateAction(){
        int hours = -1; //-1 signifies no hours entered
        String selectedStatus = spinner_updateStatus.getSelectedItem().toString();
        String currentStatus = mWorkOrder.getStatus();
        String newStatus = null;

        //ACTION
        String actionTaken = spinner_actionTaken.getSelectedItem().toString();

        if (spinner_actionTaken.getSelectedItemPosition() == 0){
            layout_action.setBackgroundColor(getResources().getColor(R.color.confirm_red));
            label_action.setTextColor(Color.WHITE);
            return false;
        }

        //STATUS
        if(!selectedStatus.equals(currentStatus)){ //If selectedStatus is different than current
            newStatus = selectedStatus;
            Log.d(TAG, "New status set.");
        } //else newStatus stays null

        //HOURS
        if(hoursEntered != -1){
            hours = hoursEntered;
        }


        //NOTE  -- handled in notes dialog. new notes added to newActionNotes arrayList


        //TODO: don't add action immediately, allow confirming first
        Log.i(TAG, "Adding new Action ( " + actionTaken + " ) for Work Order " + mWorkOrder.getProposalPhase() + " to Queue");
        newAction = new Action(mWorkOrder, actionTaken, newStatus, hours, newActionNotes);
        newAction.setTimeType(Action.TimeType.REG_NB); //%% DEBUG %% Default time type

        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_queue:

                if (editMode){
                    saveEdits();
                    createConfirmDialog();
                } else {
                    if (validateAction()) createConfirmDialog();
                }

                //TODO: finish form validation
                //All fields checked to be correct at this point


                return true;
            case android.R.id.home:
                getActivity().finish();
                return false;
        }
        return false;
    }
}
