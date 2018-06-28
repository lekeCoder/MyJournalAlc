package com.syst.apps.myjournalalc;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StartActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    private static final int RC_SIGN_IN = 1001;
    RecyclerView recyVw;
    FloatingActionButton addFab;
    Button signBut;
    MyDbHelper myDbHelper;
    JournalAdapter adapter;

    private FirebaseAuth mAuth;
    private GoogleApiClient mGoogleApiClient;
    private boolean isLoggedin;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        myDbHelper = MyDbHelper.getInstance(this);

        recyVw = findViewById(R.id.recyvw);
        recyVw.setHasFixedSize(true);
        recyVw.setLayoutManager(new LinearLayoutManager(this));
        adapter = new JournalAdapter(this);


        addFab = findViewById(R.id.fab);
        addFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isLoggedin) {
                    startActivity(new Intent(StartActivity.this, CreateEntryActivity.class));
                    //startActivity(new Intent(StartActivity.this, MainActivity.class));
                }
                else {
                    myDbHelper.showSnack("Login Required",signBut);
                }
            }
        });
        signBut  = findViewById(R.id.signBut);
        signBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isLoggedin)
                    signOut();
                else
                    signIn();
            }
        });
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.clientid))
                .requestEmail()
                .build();

        if(mGoogleApiClient==null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .enableAutoManage(this,this)
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_start, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.clear();
        adapter.addData(myDbHelper.getJournals(10));
        recyVw.setAdapter(adapter);

        SwipeHelper swipeHelper = new SwipeHelper(this, recyVw) {
            @Override
            public void instantiateUnderlayButton(RecyclerView.ViewHolder viewHolder, List<UnderlayButton> underlayButtons) {
                underlayButtons.add(new SwipeHelper.UnderlayButton(
                        "Delete",
                        R.drawable.ic_cancel_white_36dp,
                        Color.parseColor("#FF3C30"),
                        new SwipeHelper.UnderlayButtonClickListener() {
                            @Override
                            public void onClick(int pos) {
                                // TODO: onDelete
                                myDbHelper.showLog("Delete clicked");
                                Journal j = adapter.getJournal(pos);
                                if(j != null){
                                    if(myDbHelper.delJournal(j)){
                                        adapter.removeAt(pos);
                                        delFireBaseJournal(j);
                                    }
                                }
                                //myDbHelper
                            }
                        }
                ));

                /*underlayButtons.add(new SwipeHelper.UnderlayButton(
                        "Transfer",
                        0,
                        Color.parseColor("#FF9502"),
                        new SwipeHelper.UnderlayButtonClickListener() {
                            @Override
                            public void onClick(int pos) {
                                // TODO: OnTransfer
                            }
                        }
                ));
                underlayButtons.add(new SwipeHelper.UnderlayButton(
                        "Unshare",
                        0,
                        Color.parseColor("#C7C7CB"),
                        new SwipeHelper.UnderlayButtonClickListener() {
                            @Override
                            public void onClick(int pos) {
                                // TODO: OnUnshare
                            }
                        }
                ));*/
            }
        };
    }

    private void delFireBaseJournal(Journal j) {

        String suid = MyDbHelper.getSharedPref(StartActivity.this).getString("SUID","");
        j.setUid(suid);
        //Map<String, Object> postValues = j.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        //childUpdates.put("/journals/" + key, postValues);
        childUpdates.put("/user-journals/" + j.getUid() + "/" + j.getJkey(), null);

        mDatabase.updateChildren(childUpdates)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        myDbHelper.showLog("FireData Deletion Successfully");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        myDbHelper.showLog(e.toString());
                        myDbHelper.showLog("FireData Deletion Failed");
                    }
                });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            }
            else
                Snackbar.make(signBut,"Sign in failed. Try again",Snackbar.LENGTH_SHORT).show();

        }

    }

    private void signIn() {
        if(mAuth.getCurrentUser()== null) {
            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
            startActivityForResult(signInIntent, RC_SIGN_IN);
            return;
        }
        signOut();
    }
    private void signOut() {
        mAuth.signOut();
        updateUI(null);
        GsignOut();
        //startActivity(new Intent(LoginActivity.this, BeginScrollingActivity.class));
    }
    private void GsignOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        isLoggedin = false;
                    }
                });
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        //myDbHelper.showLog("firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            //myDbHelper.showLog("signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            try {
                                //myDbHelper.showLog("signInWithCredential:failure    "+ task.getException());
                                myDbHelper.showSnack("Authentication Failed: "+task.getException(),signBut);
                            } catch (Exception e) {
                                myDbHelper.showLog(e.toString());
                            }
                            updateUI(null);
                            GsignOut();
                        }

                        // ...
                    }
                });
    }

    private void updateUI(final FirebaseUser user) {
        if(user!=null) {
            isLoggedin = true;
            signBut.setText("Signed in as "+user.getDisplayName());
            myDbHelper.saveStrSharedPref("SUID",user.getUid());

            //DatabaseReference myRef = mDatabase.child("user-journals").getRef();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    final Query myTopPostsQuery = mDatabase.child("user-journals").child(user.getUid());
                    myTopPostsQuery.addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                            Journal dwnJournal = dataSnapshot.getValue(Journal.class);
                            String journalKey = dataSnapshot.getKey();
                            //myDbHelper.showLog("journalKey: "+commentKey);
                            myDbHelper.showLog("journal jdate: "+dwnJournal.getJdate());
                            dwnJournal.setJkey(journalKey);
                            myDbHelper.showLog("OnChildAdded Journal: " + myDbHelper.addJournal(dwnJournal));
                        }

                        @Override
                        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                            Journal dwnJournal = dataSnapshot.getValue(Journal.class);
                            String journalKey = dataSnapshot.getKey();
                            //myDbHelper.showLog("journalKey: "+commentKey);
                            myDbHelper.showLog("journal jdate: "+dwnJournal.getJdate());
                            dwnJournal.setJkey(journalKey);
                            myDbHelper.showLog("onChildChanged Journal: " + myDbHelper.addJournal(dwnJournal));
                        }

                        @Override
                        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                            Journal dwnJournal = dataSnapshot.getValue(Journal.class);
                            String journalKey = dataSnapshot.getKey();
                            myDbHelper.showLog("journal jdate: "+dwnJournal.getJdate());
                            dwnJournal.setJkey(journalKey);
                            myDbHelper.showLog("Delete Journal: " + myDbHelper.delJournal(dwnJournal));
                        }

                        @Override
                        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            myDbHelper.showLog("onCancelled: "+databaseError.getMessage());
                        }
                    });

                }
            }, 3000);
            //final Query myTopPostsQuery = mDatabase.child("user-journals").child(user.getUid());//.orderByChild("starCount");
            /*myTopPostsQuery.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot jsnap: dataSnapshot.getChildren()){

                        Journal dwnJournal = jsnap.getValue(Journal.class);
                        String journalKey = jsnap.getKey();
                        //myDbHelper.showLog("journalKey: "+commentKey);
                        myDbHelper.showLog("journal jdate: "+dwnJournal.getJdate());
                        dwnJournal.setJkey(journalKey);
                        myDbHelper.showLog("Save Journal: " + myDbHelper.addJournal(dwnJournal));

                    }
                    adapter.clear();
                    adapter.addData(myDbHelper.getJournals(10));

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    myDbHelper.showLog("startActy() "+databaseError.toString());
                }
            });*/



        }


    }

}
