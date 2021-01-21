package com.example.lovers;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {

    private RecyclerView mFriendsList;
    private DatabaseReference mFriendsDatabase;
    private FirebaseAuth mAuth;
    FirebaseRecyclerAdapter<Friends, FriendsviewHolder> firebaseRecyclerAdapter;
    private DatabaseReference mUserDatabase;
    private String mCurrent_user_id;
    private View mMainView;

    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mMainView = inflater.inflate(R.layout.fragment_friends, container, false);
        mFriendsList = (RecyclerView) mMainView.findViewById(R.id.friends_list);
        mAuth = FirebaseAuth.getInstance();
        mCurrent_user_id = mAuth.getCurrentUser().getUid();
        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrent_user_id);
        mFriendsDatabase.keepSynced(true);
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUserDatabase.keepSynced(true);
        mFriendsList.setHasFixedSize(true);
        mFriendsList.setLayoutManager(new LinearLayoutManager(getContext()));
        return mMainView;

    }

    @Override
    public void onStart() {
        super.onStart();

        Query query = mFriendsDatabase.limitToLast(50).orderByPriority();
        FirebaseRecyclerOptions<Friends> options = new FirebaseRecyclerOptions.Builder<Friends>()
                .setQuery(query, Friends.class).build();
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Friends, FriendsviewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final FriendsviewHolder friendsviewHolder, int i, @NonNull Friends friends) {

                friendsviewHolder.setDate(friends.getDate());
                final String list_user_id = getRef(i).getKey();
                mUserDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {


                        final String userName = dataSnapshot.child("name").getValue().toString();
                        String userThumb = dataSnapshot.child("image").getValue().toString();

                        if(dataSnapshot.hasChild("online")){
                            Boolean userOnline = (boolean)dataSnapshot.child("online").getValue();
                            friendsviewHolder.setIcon(userOnline);
                        }

                        friendsviewHolder.setName(userName);
                        friendsviewHolder.setImage(userThumb,getContext());

                        friendsviewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CharSequence options[] = new CharSequence[]{"Open Profile", "Send message"};
                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle("Select Options");
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if(which==0){
                                            Intent profileIntent=new Intent(getContext(),ProfileActivity.class);
                                            profileIntent.putExtra("user_id",list_user_id);
                                            startActivity(profileIntent);

                                        }
                                        if(which==1){
                                            Intent chatIntent=new Intent(getContext(),ChatActivity.class);
                                            chatIntent.putExtra("user_id",list_user_id);
                                            chatIntent.putExtra("user_name",userName);
                                            startActivity(chatIntent);
                                        }
                                    }
                                });
                                builder.show();
                            }
                        });


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public FriendsviewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.user_single_layout, parent, false);
                return new FriendsviewHolder(view);
            }
        };
        firebaseRecyclerAdapter.startListening();
        mFriendsList.setAdapter(firebaseRecyclerAdapter);

    }
    private class FriendsviewHolder extends RecyclerView.ViewHolder {

        View mView;

        public FriendsviewHolder(View view) {
            super(view);

            mView = view;
        }

        public void setDate(String date) {

            TextView userNameView = (TextView) mView.findViewById(R.id.default_status);
            userNameView.setText(date);
        }
        public void setName(String name) {
            TextView usersname = (TextView) mView.findViewById(R.id.display_name);
            usersname.setText(name);
        }
        public void setImage(String image, Context ctx){
            CircleImageView imageuser=(CircleImageView)mView.findViewById(R.id.profile_image);
            Picasso.get().load(image).placeholder(R.drawable.profile).into(imageuser);
        }
        public void setIcon(boolean online_Icon){
            ImageView icon =(ImageView) mView.findViewById(R.id.user_single_online);
            if(online_Icon==true ){
                icon.setVisibility(View.VISIBLE);
            }else {
                icon.setVisibility(View.INVISIBLE);
            }
        }
    }
}
