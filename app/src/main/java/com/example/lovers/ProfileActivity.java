package com.example.lovers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {
    private ImageView profileImage;
    private TextView userstatus,username,userfriend;
    private Button sendreq,declinereq;
    private FirebaseUser mcurrentUser;
    private DatabaseReference databaseReference;
    private DatabaseReference mReqDatabse;
    private DatabaseReference mRootRef;
    private DatabaseReference notificationDatabase;
    private ProgressDialog progressDialog;
    private String current_state;
    private DatabaseReference friendDatabse;
    private Toolbar mtoolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String user_id = getIntent().getStringExtra("user_id");

        mRootRef = FirebaseDatabase.getInstance().getReference();

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mReqDatabse=FirebaseDatabase.getInstance().getReference().child("req_data");
        friendDatabse=FirebaseDatabase.getInstance().getReference().child("Friends");
        notificationDatabase=FirebaseDatabase.getInstance().getReference().child("Notification");
        mcurrentUser= FirebaseAuth.getInstance().getCurrentUser();
        username = (TextView) findViewById(R.id.userid);
        profileImage = (ImageView) findViewById(R.id.imageprofile);
        userstatus = (TextView) findViewById(R.id.prostatus);
        userfriend = (TextView) findViewById(R.id.profriend);

        declinereq=(Button)findViewById(R.id.decline);
        declinereq.setVisibility(View.INVISIBLE);
        declinereq.setEnabled(false);

        current_state = "not friend";
        sendreq = (Button) findViewById(R.id.proreq);
        mtoolbar=(Toolbar)findViewById(R.id.p_toolbar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle("Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Image Uploading");
        progressDialog.setMessage("Uploading....");
        progressDialog.setCanceledOnTouchOutside(false);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String disimage = dataSnapshot.child("image").getValue().toString();
                String disname = dataSnapshot.child("name").getValue().toString();
                String disstatus = dataSnapshot.child("status").getValue().toString();

                username.setText(disname);
                userstatus.setText(disstatus);
                Picasso.get().load(disimage).placeholder(R.drawable.profile).into(profileImage);

                mReqDatabse.child(mcurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(dataSnapshot.hasChild(user_id)){
                            String req_type=dataSnapshot.child(user_id).child("request_type").getValue().toString();
                            if(req_type.equals("received")){
                                sendreq.setEnabled(true);
                                current_state="req_received";
                                sendreq.setText("Accept Friend Request");
                                declinereq.setVisibility(View.VISIBLE);
                                declinereq.setEnabled(true);
                            }
                            else if(req_type.equals("req sent")){
                                current_state="req_sent";
                                sendreq.setText("Cancel Friend Request");
                            }
                            progressDialog.dismiss();
                        }else {
                            friendDatabse.child(mcurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(user_id)) {
                                        current_state = "Friends";
                                        sendreq.setText("Unfriend");
                                        declinereq.setVisibility(View.INVISIBLE);
                                        declinereq.setEnabled(false);
                                    }
                                    progressDialog.dismiss();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                    progressDialog.dismiss();
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        sendreq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendreq.setEnabled(false);
//-----------------------Not Friends----------------------//
                if (current_state.equals("not friend")) {

                    DatabaseReference newNotificationref = mRootRef.child("Notification").child(user_id).push();
                    String newNotificationId = newNotificationref.getKey();

                    HashMap<String, String> notificationdata = new HashMap<>();
                    notificationdata.put("from", mcurrentUser.getUid());
                    notificationdata.put("type", "request");


                    Map requestMap = new HashMap();
                    requestMap.put("req_data/" + mcurrentUser.getUid() + "/" + user_id + "/request_type", "sent");
                    requestMap.put("req_data/" + user_id + "/" + mcurrentUser.getUid() + "/request_type", "received");
                    requestMap.put("Notification/" + user_id + "/" + newNotificationId, notificationdata);
                    mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if (databaseError != null) {
                                Toast.makeText(ProfileActivity.this, "There was some Error in sending request", Toast.LENGTH_SHORT).show();
                            }
//                            notificationDatabase.child(user_id).push().setValue(notificationdata).addOnSuccessListener(new OnSuccessListener<Void>() {
//                                @Override
//                                public void onSuccess(Void aVoid) {
                            current_state = "req sent";
                            sendreq.setEnabled(true);
                            sendreq.setText("Cancel Friend Request");
                            declinereq.setVisibility(View.INVISIBLE);
                            declinereq.setEnabled(false);
//                                }
//                            });

                        }
                    });

                }
                //------------------------Cancel Request State---------------------//
                if (current_state.equals("req sent")) {

                    mReqDatabse.child(mcurrentUser.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mReqDatabse.child(user_id).child(mcurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    notificationDatabase.child(mcurrentUser.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            notificationDatabase.child(user_id).child(mcurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                    HashMap<String, String> notificationdata = new HashMap<>();
                                                    notificationdata.put("from", mcurrentUser.getUid());
                                                    notificationdata.put("type", "sent");
                                                    sendreq.setEnabled(true);
                                                    current_state = "not friend";
                                                    sendreq.setText("Send Friend Request");
                                                    declinereq.setVisibility(View.INVISIBLE);
                                                    declinereq.setEnabled(false);
                                                }
                                            });
                                        }
                                    });
                                }
                            });
                        }
                    });
                }
                //---------------Request received State------------//
                if (current_state.equals("req_received")) {
                    final String currentdate = DateFormat.getDateTimeInstance().format(new Date());

                    friendDatabse.child(mcurrentUser.getUid()).child(user_id).child("date").setValue(currentdate).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            friendDatabse.child(user_id).child(mcurrentUser.getUid()).child("date").setValue(currentdate).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mReqDatabse.child(mcurrentUser.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            mReqDatabse.child(user_id).child(mcurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                    sendreq.setEnabled(true);
                                                    current_state = "Friends";
                                                    sendreq.setText("Unfriend");
                                                    declinereq.setVisibility(View.INVISIBLE);
                                                    declinereq.setEnabled(false);
                                                }
                                            });
                                        }
                                    });
                                }
                            });
                        }
                    });
                }

                //-----------------UNFRIEND---------------//
                if (current_state.equals("Friends")) {
                    Map unfriendMap = new HashMap();
                    unfriendMap.put("Friends/" + mcurrentUser.getUid() + "/" + user_id, null);
                    unfriendMap.put("Friends/" + user_id + "/" + mcurrentUser.getUid(), null);
                    mRootRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if (databaseError == null) {

                                current_state = "not friends";
                                sendreq.setText("Send Friend Request");
                                declinereq.setVisibility(View.INVISIBLE);
                                declinereq.setEnabled(false);
                            } else {
                                String error = databaseError.getMessage();
                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();
                            }
                            sendreq.setEnabled(true);
                        }

                    });
                }

            }
        });
        declinereq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Map declineMap = new HashMap();

                declineMap.put("req_data/" + mcurrentUser.getUid() + "/" + user_id, null);
                declineMap.put("req_data/" + user_id + "/" + mcurrentUser.getUid(), null);

                mRootRef.updateChildren(declineMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                        if(databaseError == null)
                        {

                            current_state = "not friends";
                            sendreq.setText("Send Friend Request");

                            declinereq.setVisibility(View.INVISIBLE);
                            declinereq.setEnabled(false);
                        }else{
                            String error = databaseError.getMessage();
                            Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_LONG).show();
                        }

                        sendreq.setEnabled(true);
                    }
                });

            }
        });
    }

}
