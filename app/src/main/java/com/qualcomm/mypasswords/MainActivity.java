package com.qualcomm.mypasswords;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    EditText masterKeyEditText, newKeyEditText, setAccEditText, setIDEditText, setPassEditText, getAccEditText, getIDEditText, consoleView;
    Button setKeyButton, setPassButton, getPassButton, clearButton, deleteButton;
    SharedPreferences sharedPreferences;
    public String TAG = "aditya";
    public String backupKey = "aditya1311";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        masterKeyEditText = (EditText) findViewById(R.id.masterKeyEditText);
        newKeyEditText = (EditText) findViewById(R.id.newKeyEditText);
        setAccEditText = (EditText) findViewById(R.id.setAccEditText);
        setIDEditText = (EditText) findViewById(R.id.setIDEditText);
        setPassEditText = (EditText) findViewById(R.id.setPassEditText);
        getAccEditText = (EditText) findViewById(R.id.getAccEditText);
        masterKeyEditText = (EditText) findViewById(R.id.masterKeyEditText);
        getIDEditText = (EditText) findViewById(R.id.getIDEditText);
        consoleView = (EditText) findViewById(R.id.consoleView);

        setKeyButton = (Button) findViewById(R.id.setKeyButton);
        setPassButton = (Button) findViewById(R.id.setPassButton);
        getPassButton = (Button) findViewById(R.id.getPassButton);
        clearButton = (Button) findViewById(R.id.clearButton);
        deleteButton = (Button) findViewById(R.id.deleteButton);

        sharedPreferences = getSharedPreferences("sharedPreferences", Context.MODE_PRIVATE);

        masterKeyEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        newKeyEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        setPassEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        if(sharedPreferences.contains("masterKey")){
            Log.d(TAG, "Dict already contains masterKey: <" + sharedPreferences.getString("masterKey", "") + ">");
        }
        else{
            editor.putString("masterKey", "");
            editor.apply();
            Log.d(TAG, "Created masterKey: <" + sharedPreferences.getString("masterKey", "") + ">");
        }
        Log.d(TAG, "Masterkey is <" + sharedPreferences.getString("masterKey", "") + ">");

        //SET/CHANGE THE MASTER KEY
        setKeyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String masterKey = masterKeyEditText.getText().toString();
                if(!masterKey.equals(sharedPreferences.getString("masterKey", ""))){
                    Log.d(TAG, "Entered masterkey does not match stored masterkey");
                    Log.d(TAG, "Stored masterKey: " + sharedPreferences.getString("masterKey", ""));
                    Log.d(TAG, "Entered masterKey: " + masterKey);
                    consoleView.setText("ERROR: Please enter correct master key");
                }
                else{
                    editor.putString("masterKey", newKeyEditText.getText().toString());
                    editor.apply();
                    Log.d(TAG, "Changed master key to " + sharedPreferences.getString("masterKey", ""));
                    consoleView.setText("INFO: Successfully updated master key");
                    masterKeyEditText.setText("");
                    newKeyEditText.setText("");
                    masterKeyEditText.clearFocus();
                    newKeyEditText.clearFocus();
                }

                hideKeyboard(v);
            }
        });
        //SET/CHANGE THE PASSWORD FOR AN ACCOUNT
        setPassButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sharedPreferences.getString("masterKey", "").equals("")){
                    consoleView.setText("ERROR: Please set a master key before doing any action");
                    return;
                }
                String account = setAccEditText.getText().toString().trim().toLowerCase();
                String reqID = setIDEditText.getText().toString().trim();
                String reqPW = setPassEditText.getText().toString();
                boolean alreadyExists = false;
                //IF REQUIRED ID AND REQUIRED PASSWORD ARE NON EMPTY
                if (!reqID.equals("") & !reqPW.equals("")) {
                    Log.d(TAG, "Required ID to be added: " + reqID);
                    Log.d(TAG, "Required PW to be added: " + reqPW);
                    //IF ACCOUNT DICT ALREADY EXISTS
                    if (sharedPreferences.contains(account)) {
                        Log.d(TAG, "dict contains the account " + account);
                        String credsStr = sharedPreferences.getString(account, "");
                        //GET ARRAY OF CREDENTIALS
                        String[] credsList = credsStr.split(";");
                        String outcredsList = "";
                        String temp = "";
                        //ITERATE CREDENTIALS IN LIST FOR AN ACCOUNT
                        for (int i = 0; i < credsList.length; i++) {
                            String[] creds = credsList[i].split(",");
                            Log.d(TAG, "Checking in creds: " + creds[0] + "," + creds[1]);
                            //IF ID ALREADY EXISTS FOR THAT ACCOUNT
                            if (creds[0].equals(reqID)) {
                                alreadyExists = true;
                                creds[1] = reqPW;
                                Log.d(TAG, "Updating " + account + "'s " + creds[0] + " with " + creds[1]);
                            }
                            temp = creds[0] + "," + creds[1] + ";";
                            outcredsList = outcredsList + temp;
                            Log.d(TAG, "temp: " + temp);
                            Log.d(TAG, "outcredslist: " + outcredsList);
                        }
                        //CREATE NEW ID FOR THAT ACCOUNT
                        if (!alreadyExists) {
                            String out = reqID + "," + reqPW + ";";
                            outcredsList = outcredsList + out;
                        }
                        Log.d(TAG, "Final outstring for " + account + " is " + outcredsList);
                        if(sharedPreferences.getString("masterKey", "").equals(masterKeyEditText.getText().toString())) {
                            editor.putString(account, outcredsList);
                            editor.apply();
                            consoleView.setText("INFO: Successfully updated password for " + account + "'s " + reqID);
                            Log.d(TAG, "dict for " + account + ": " + sharedPreferences.getString(account, ""));
                        }
                        else{
                            consoleView.setText("ERROR: Please enter correct master key");
                            Log.d(TAG, "Mismatch in master key entered and sharedpref " + sharedPreferences.getString("masterKey", "") + " " + masterKeyEditText.getText().toString());
                        }
                    }
                    //CREATE NEW DICT FOR THAT ACCOUNT
                    else {
                        String id = setIDEditText.getText().toString();
                        String pw = setPassEditText.getText().toString();
                        String out = id + "," + pw + ";";
                        Log.d(TAG, "Inserting " + out + " into " + account);
                        if(sharedPreferences.getString("masterKey", "").equals(masterKeyEditText.getText().toString())) {
                            editor.putString(account, out);
                            editor.apply();
                            consoleView.setText("INFO: Successfully updated password for " + account + "'s " + reqID);
                            Log.d(TAG, "dict for " + account + ": " + sharedPreferences.getString(account, ""));
                        }
                        else{
                            consoleView.setText("ERROR: Please enter correct master key");
                            Log.d(TAG, "Mismatch in master key entered and sharedpref " + sharedPreferences.getString("masterKey", "") + " " + masterKeyEditText.getText().toString());
                        }

                    }
                    masterKeyEditText.setText("");
                    setAccEditText.setText("");
                    setIDEditText.setText("");
                    setPassEditText.setText("");
                    hideKeyboard(v);
                }

                //IF REQ ID OR REQ PW ARE EMPTY
                else {
                    Log.d(TAG, "Please enter ID and PW to store");
                    consoleView.setText("ERROR: Empty ID or Password fields");
                }
            }
        });
        //GET PASSWORD FOR REQUIRED ACCOUNT AND ID
        getPassButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sharedPreferences.getString("masterKey", "").equals("")){
                    consoleView.setText("ERROR: Please set a master key before doing any action");
                    return;
                }
                String account = getAccEditText.getText().toString().trim().toLowerCase();
                String id = getIDEditText.getText().toString().trim();
                if(account.equals("") & id.equals("")){
                    Log.d(TAG, "enter proper account, id");
                    consoleView.setText("ERROR: Empty ID or Password fields");
                    return;
                }

                if(!sharedPreferences.getString("masterKey", "").equals(masterKeyEditText.getText().toString())){
                    Log.d(TAG, "Master key mismatch");
                    consoleView.setText("ERROR: Please enter correct master key");
                    return;
                }

                if(!sharedPreferences.contains(account)){
                    Log.d(TAG, "Dict does not contain any account entries");
                    consoleView.setText("ERROR: No data for " + account + " as requested");
                    getAccEditText.setText("");
                    getIDEditText.setText("");
                    masterKeyEditText.setText("");
                    return;
                }
                if(!account.equals("") & id.equals("")){
                    Log.d(TAG, "Getting users list");
                    String[] credsList = sharedPreferences.getString(account,"").split(";");
                    Log.d(TAG, "credslist: " + credsList);
                    String out = "users:\n";
                    for(int i=0; i<credsList.length; i++){
                        out = out + credsList[i].split(",")[0] + "\n";
                        Log.d(TAG,"In for loop: out: " + out);
                    }
                    Log.d(TAG, "after for loop, out: " + out);
                    String out1 = out.substring(0,out.length() - 1);
                    Log.d(TAG, "after cutting, out: " + out1);
                    consoleView.setText("" + out1);
                    return;
                }
                String data = sharedPreferences.getString(account, "");
                String[] credsList = data.split(";");
                for(int i=0; i<credsList.length; i++){
                    String[] creds = credsList[i].split(",");
                    if(creds[0].equals(id)){
                        consoleView.setText(("ID: " + creds[0] + "\nPW: " + creds[1]));
                        hideKeyboard(v);
                        getAccEditText.setText("");
                        getIDEditText.setText("");
                        masterKeyEditText.setText("");
                        return;
                    }
                }
                consoleView.setText("ERROR: ID " + id + " does not exist for the " + account + " account");
                getAccEditText.setText("");
                getIDEditText.setText("");
                masterKeyEditText.setText("");
                hideKeyboard(v);
            }
        });

        getPassButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(sharedPreferences.getString("masterKey", "").equals("")){
                    consoleView.setText("ERROR: Please set a master key before doing any action");
                    return true;
                }
                String mKey = masterKeyEditText.getText().toString();
                String bKey = newKeyEditText.getText().toString();
                String check = sharedPreferences.getString("masterKey", "");
                if(mKey.equals(check) & bKey.equals(check)){
                    consoleView.setText("" + sharedPreferences.getAll());
                }
                else{
                    consoleView.setText("ERROR: Please enter masterkey in both key fields");
                    return true;
                }
                masterKeyEditText.setText("");
                newKeyEditText.setText("");
                return true;
            }
        });

        //CLEAR CONSOLE
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("");
                Log.d(TAG, "SharedPreferences: "+sharedPreferences.getAll());
                consoleView.setText("");
                hideKeyboard(v);
            }
        });

        //CLEAR ALL DATA
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sharedPreferences.getString("masterKey", "").equals("")){
                    consoleView.setText("ERROR: Please set a master key before doing any action");
                    return;
                }

                String masterKey = masterKeyEditText.getText().toString();
                if(!masterKey.equals(sharedPreferences.getString("masterKey", ""))){
                    consoleView.setText("ERROR: Please enter correct masterkey in both key fields");
                    return;
                }

                String nKey = newKeyEditText.getText().toString();
                if(!nKey.equals(sharedPreferences.getString("masterKey", ""))){
                    consoleView.setText("ERROR: Please enter correct masterkey in both key fields");
                    return;
                }
                Log.d(TAG, "Clearing all the dict");
                consoleView.setText("INFO: Cleared all data");
                editor.clear();
                editor.putString("masterKey", masterKey);
                editor.apply();
                masterKeyEditText.setText("");
                newKeyEditText.setText("");
                hideKeyboard(v);
            }
        });

        masterKeyEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {@Override public void onFocusChange(View v, boolean hasFocus) { if (!hasFocus) { hideKeyboard(v); } }});
        newKeyEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {@Override public void onFocusChange(View v, boolean hasFocus) { if (!hasFocus) { hideKeyboard(v); } }});
        setAccEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {@Override public void onFocusChange(View v, boolean hasFocus) { if (!hasFocus) { hideKeyboard(v); } }});
        setIDEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {@Override public void onFocusChange(View v, boolean hasFocus) { if (!hasFocus) { hideKeyboard(v); } }});
        setPassEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {@Override public void onFocusChange(View v, boolean hasFocus) { if (!hasFocus) { hideKeyboard(v); } }});
        getAccEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {@Override public void onFocusChange(View v, boolean hasFocus) { if (!hasFocus) { hideKeyboard(v); } }});
        getIDEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {@Override public void onFocusChange(View v, boolean hasFocus) { if (!hasFocus) { hideKeyboard(v); } }});
        consoleView.setOnFocusChangeListener(new View.OnFocusChangeListener() {@Override public void onFocusChange(View v, boolean hasFocus) { if (!hasFocus) { hideKeyboard(v); } }});

    }
    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}