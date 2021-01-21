package com.example.lovers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class HomeActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private Toolbar mToolbar;
    private ViewPager mViewPager;

    private TabAccessorAdapter myTabAccessorAdapter;
    private TabLayout mTabLayout;

    private DatabaseReference mUserRef;
    private DatabaseReference RootRef;
    private String currentUserID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_activity);

        mAuth = FirebaseAuth.getInstance();
//        currentUserID = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();


        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("WhatsApp");

        if(mAuth.getCurrentUser()!=null) {
            mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());

        }
        mViewPager = (ViewPager) findViewById(R.id.main_tabs_pager);
        myTabAccessorAdapter = new TabAccessorAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(myTabAccessorAdapter);
        mTabLayout = (TabLayout) findViewById(R.id.main_tabs);
        mTabLayout.setupWithViewPager(mViewPager);

    }
    @Override
    public void onStart() {
        super.onStart();



        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            sendToStart();
        }
        else {
            mUserRef.child("online").setValue(true);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null) {
            mUserRef.child("online").setValue(false);
            //mUserRef.child("lastSeen").setValue(ServerValue.TIMESTAMP);
        }
    }

    private void sendToStart() {
            Intent startIntent = new Intent(HomeActivity.this, MainActivity.class);
            startActivity(startIntent);
            finish();
        }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.menu, menu);

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == R.id.main_log_out) {

                FirebaseAuth.getInstance().signOut();
                sendToStart();

        }
        if(item.getItemId()==R.id.main_settings){
            Intent settingIntent = new Intent(HomeActivity.this, SettingActivity.class);
            startActivity(settingIntent);
        }
        if(item.getItemId()==R.id.main_all_users){
            Intent settingIntent = new Intent(HomeActivity.this, UsersActivity.class);
            startActivity(settingIntent);
        }
        if(item.getItemId()==R.id.grp_chat){
            RequestNewGroup();
        }
        return true;
    }

    private void RequestNewGroup()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this, R.style.AlertDialog);
        builder.setTitle("Enter Group Name :");

        final EditText groupNameField = new EditText(HomeActivity.this);
        groupNameField.setHint("e.g Coding Cafe");
        builder.setView(groupNameField);

        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                String groupName = groupNameField.getText().toString();

                if (TextUtils.isEmpty(groupName))
                {
                    Toast.makeText(HomeActivity.this, "Please write Group Name...", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    CreateNewGroup(groupName);
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                dialogInterface.cancel();
            }
        });

        builder.show();
    }

    private void CreateNewGroup(final String groupName)
    {
        RootRef.child("Groups").child(groupName).setValue("")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            Toast.makeText(HomeActivity.this, groupName + " group is Created Successfully...", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }



}
