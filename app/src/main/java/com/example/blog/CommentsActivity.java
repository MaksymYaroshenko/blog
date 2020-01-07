package com.example.blog;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentsActivity extends AppCompatActivity {
    private Toolbar commentToolbar;
    private EditText commentField;
    private ImageView commentPostButton;
    private RecyclerView commentList;

    private String blogPostId;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private String currentUserId;
    private List<Comments> commentsList;
    private CommentsRecyclerAdapter commentsRecyclerAdapter;
    private Boolean isFirstPageFirstLoad = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        commentToolbar = findViewById(R.id.commentsToolbar);
        setSupportActionBar(commentToolbar);
        getSupportActionBar().setTitle("Comments");

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        currentUserId = firebaseAuth.getCurrentUser().getUid();
        blogPostId = getIntent().getStringExtra("blog_post_id");

        commentField = findViewById(R.id.commentField);
        commentPostButton = findViewById(R.id.commentPostButton);
        commentList = findViewById(R.id.commentList);

        commentsList = new ArrayList<>();
        commentsRecyclerAdapter = new CommentsRecyclerAdapter(commentsList);
        commentList.setHasFixedSize(true);
        commentList.setLayoutManager(new LinearLayoutManager(this));
        commentList.setAdapter(commentsRecyclerAdapter);


        Query query = firebaseFirestore.collection("Posts/" + blogPostId + "/Comments").orderBy("timestamp", Query.Direction.ASCENDING);
        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (queryDocumentSnapshots == null) return;
                if (!queryDocumentSnapshots.isEmpty()) {
                    for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                        if (doc.getType() == DocumentChange.Type.ADDED) {
                            String commentId = doc.getDocument().getId();
                            Comments comments = doc.getDocument().toObject(Comments.class);
                            if (isFirstPageFirstLoad) {
                            commentsList.add(comments);
                            } else {
                                commentsList.add(0, comments);
                            }
                            commentsRecyclerAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }
        });


        commentPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String commentMessage = commentField.getText().toString();
                if (!commentMessage.isEmpty()) {
                    Map<String, Object> commentsMap = new HashMap<>();
                    commentsMap.put("message", commentMessage);
                    commentsMap.put("user_id", currentUserId);
                    commentsMap.put("timestamp", FieldValue.serverTimestamp());
                    firebaseFirestore.collection("Posts/" + blogPostId + "/Comments").add(commentsMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            if(!task.isSuccessful()){
                                Toast.makeText(CommentsActivity.this, "Error Posting Comment: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }else{
                                commentField.setText("");
                            }
                        }
                    });
                }
            }
        });
    }
}
