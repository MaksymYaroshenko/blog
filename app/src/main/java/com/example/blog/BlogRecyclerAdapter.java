package com.example.blog;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;


import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;

public class BlogRecyclerAdapter extends RecyclerView.Adapter<BlogRecyclerAdapter.ViewHolder> {

    public List<BlogPost> blogList;
    public List<User> userList;
    public Context context;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    public BlogRecyclerAdapter(List<BlogPost> blogList, List<User> userList) {
        this.blogList = blogList;
        this.userList = userList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.blog_list_item, viewGroup, false);
        context = viewGroup.getContext();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int i) {
        viewHolder.setIsRecyclable(false);

        final String blogPostId = blogList.get(i).BlogPostId;
        final String currentUserId = firebaseAuth.getCurrentUser().getUid();

        String descData = blogList.get(i).getDesc();
        viewHolder.setDescText(descData);

        String imageUrl = blogList.get(i).getImage_url();
        String thumbUri = blogList.get(i).getImage_thumb();
        viewHolder.setBlogImage(imageUrl, thumbUri);

        String userId = blogList.get(i).getUser_id();

        if(userId.equals(currentUserId)){
            viewHolder.blogDeleteButton.setEnabled(true);
            viewHolder.blogDeleteButton.setVisibility(View.VISIBLE);
        }

        firebaseFirestore.collection("Users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    String userName = task.getResult().getString("name");
                    String userImage = task.getResult().getString("image");
                    viewHolder.setUserData(userName, userImage);
                }
            }
        });

        try {
            long millisecond = blogList.get(i).getTimestamp().getTime();
            String dateString = DateFormat.format("MM/dd/yyyy", new Date(millisecond)).toString();
            viewHolder.setTime(dateString);
        } catch (Exception e) {
            Toast.makeText(context, "Exception: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (!documentSnapshot.isEmpty()) {
                    int count = documentSnapshot.size();
                    viewHolder.updateLikesCount(count);
                } else {
                    viewHolder.updateLikesCount(0);
                }
            }

        });

        firebaseFirestore.collection("Posts/" + blogPostId + "/Comments").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (queryDocumentSnapshots == null) return;
                if (!queryDocumentSnapshots.isEmpty()) {
                    int count = queryDocumentSnapshots.size();
                    viewHolder.updateCommentsCount(count);
                } else {
                    viewHolder.updateCommentsCount(0);
                }
            }
        });


        firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").document(currentUserId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (documentSnapshot.exists()) {
                    viewHolder.blogLikeButton.setImageDrawable(context.getDrawable(R.mipmap.action_like_accept));
                } else {
                    viewHolder.blogLikeButton.setImageDrawable(context.getDrawable(R.mipmap.action_like_gray));
                }
            }
        });

        viewHolder.blogLikeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (!task.getResult().exists()) {
                            Map<String, Object> likesMap = new HashMap<>();
                            likesMap.put("timestamp", FieldValue.serverTimestamp());
                            firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").document(currentUserId).set(likesMap);
                        } else {
                            firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").document(currentUserId).delete();
                        }
                    }
                });

            }
        });

        viewHolder.blogCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent commentIntent = new Intent(context, CommentsActivity.class);
                commentIntent.putExtra("blog_post_id", blogPostId);
                context.startActivity(commentIntent);
            }
        });

        viewHolder.blogDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseFirestore.collection("Posts").document(blogPostId).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        blogList.remove(i);
                        userList.remove(i);
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return blogList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView descView;
        private View mView;
        private ImageView blogImageView;
        private TextView blogDate;
        private TextView blogUserName;
        private CircleImageView blogUserImage;
        private ImageView blogLikeButton;
        private TextView blogLikeCount;
        private ImageView blogCommentBtn;
        private TextView blogCommentCount;
        private Button blogDeleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;

            blogLikeButton = mView.findViewById(R.id.blogLikeBtn);
            blogCommentBtn = mView.findViewById(R.id.blogCommentsBtn);
            blogDeleteButton = mView.findViewById(R.id.blogDeleteBtn);
        }

        public void setDescText(String text) {
            descView = mView.findViewById(R.id.blogDescription);
            descView.setText(text);
        }

        public void setBlogImage(String downloadImage, String thumbUri) {
            blogImageView = mView.findViewById(R.id.blogImage);
            RequestOptions requestOptions = new RequestOptions();
            requestOptions.placeholder(R.drawable.image_placeholder);
            Glide.with(context).applyDefaultRequestOptions(requestOptions).load(downloadImage).thumbnail(Glide.with(context).load(thumbUri)).into(blogImageView);
        }

        public void setTime(String date) {
            blogDate = mView.findViewById(R.id.blogDate);
            blogDate.setText(date);
        }

        public void setUserData(String name, String image) {
            blogUserImage = mView.findViewById(R.id.blogUserImage);
            blogUserName = mView.findViewById(R.id.blogUserName);

            RequestOptions placeholderOption = new RequestOptions();
            placeholderOption.placeholder(R.drawable.profile_placeholder);
            Glide.with(context).applyDefaultRequestOptions(placeholderOption).load(image).into(blogUserImage);
            blogUserName.setText(name);
        }

        public void updateLikesCount(int count) {
            blogLikeCount = mView.findViewById(R.id.blogLikeCount);
            blogLikeCount.setText(count + " Likes");
        }

        public void updateCommentsCount(int count) {
            blogCommentCount = mView.findViewById(R.id.blogCommentsCount);
            blogCommentCount.setText(count + " Comments");
        }
    }
}
