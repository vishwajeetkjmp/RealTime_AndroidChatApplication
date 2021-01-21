package com.example.lovers;

import android.graphics.Color;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Messages> mMessageList;
    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;

    public MessageAdapter(List<Messages> mMessageList) {
        this.mMessageList = mMessageList;

    }

    @NonNull
    @Override
    public MessageAdapter.MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_single_layout , parent, false);

        return new MessageViewHolder(view);

    }


    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView messageText;
        public CircleImageView profileImage;
        public TextView displayName;
        public ImageView messageImage;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            messageText = (TextView) itemView.findViewById(R.id.message_text_layout);
            profileImage = (CircleImageView) itemView.findViewById(R.id.message_profile_layout);
            displayName = (TextView) itemView.findViewById(R.id.name_text_layout);
            messageImage = (ImageView) itemView.findViewById(R.id.message_image_layout);
        }
    }


    @Override
    public void onBindViewHolder(@NonNull final MessageAdapter.MessageViewHolder holder, int position) {

        mAuth=FirebaseAuth.getInstance();
        String current_user_id = mAuth.getCurrentUser().getUid();

        Messages c = mMessageList.get(position);

        String from_user = c.getFrom();
        String message_type = c.getType();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(from_user);

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                holder.displayName.setText(name);

                Picasso.get().load(image).placeholder(R.drawable.profile).into(holder.profileImage);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        if(message_type.equals("text")){
            holder.messageText.setText(c.getMessage());
            holder.messageImage.setVisibility(View.INVISIBLE);
        }else {
            holder.messageText.setVisibility(View.INVISIBLE);

            Picasso.get().load(c.getMessage()).placeholder(R.drawable.profile).into(holder.messageImage);

        }
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }





}
