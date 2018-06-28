package com.syst.apps.myjournalalc;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CreateEntryActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    public static String JOURNAL;
    MyDbHelper myDbHelper;
    TextView pickDateTv;
    EditText noteEditTv;
    Journal j;
    private String noteText;
    private DatabaseReference mDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_entry);

        myDbHelper = MyDbHelper.getInstance(this);
        pickDateTv = findViewById(R.id.diaryDate);
        noteEditTv = findViewById(R.id.diaryEditText);


        pickDateTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar c = Calendar.getInstance();
                c.setTime(new Date());
        DatePickerDialog dialog = new DatePickerDialog(CreateEntryActivity.this,CreateEntryActivity.this,c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH));
                dialog.show();
            }
        });

        if(getIntent().getSerializableExtra(JOURNAL) != null){
            j = (Journal) getIntent().getSerializableExtra(JOURNAL);
            pickDateTv.setText(j.getJdate());
            noteEditTv.setText(j.getJentry());
        }
        else
            j = new Journal();

        noteText = noteEditTv.getText().toString();
        mDatabase = FirebaseDatabase.getInstance().getReference();


    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR,year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH,dayOfMonth);

        String tStamp = new SimpleDateFormat("dd-MMM-yyyy", Locale.UK).format(cal.getTime());
        pickDateTv.setText(tStamp);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_entry, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.save) {

            String dt = pickDateTv.getText().toString();
            String note = noteEditTv.getText().toString();
            if(!dt.equals("") && !note.equals("")){
                j.setJdate(dt);
                j.setJentry(note);
                j.setJmod((noteText.equalsIgnoreCase(note)? "0":"1"));
                if(j.getJkey().equals(""))
                    j.setJkey(mDatabase.child("journals").push().getKey());

                if(myDbHelper.addJournal(j)){
                    showMessage("Note Saved Successfully");
                    writeJournal(j);


                }
                else {
                    showMessage("Note Saving Failed");
                }
            }
            else
                showMessage("Missing field. Fill appropriately");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void writeJournal(Journal j) {
        // Create new post at /user-posts/$userid/$postid and at
        // /posts/$postid simultaneously


        String suid = MyDbHelper.getSharedPref(CreateEntryActivity.this).getString("SUID","");
        if(suid.equalsIgnoreCase("")){
            showMessage("Data Syncing Failed. No User Login Found!");
            return;
        }
        j.setUid(suid);


        Map<String, Object> postValues = j.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        //childUpdates.put("/journals/" + key, postValues);
        childUpdates.put("/user-journals/" + j.getUid() + "/" + j.getJkey(), postValues);

        mDatabase.updateChildren(childUpdates)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        myDbHelper.showLog("FireData Saved Successfully");
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                finish();
                            }
                        },2000);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        myDbHelper.showLog(e.toString());
                        myDbHelper.showLog("FireData Failed woefully");
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                finish();
                            }
                        },2000);
                    }
                });
    }

    void showMessage(String msg){
        Snackbar.make(pickDateTv,msg,Snackbar.LENGTH_SHORT).show();
    }
}
