package com.example.blog;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    private RecyclerView blogListView;
    private List<BlogPost> blogList;
    private List<User> userList;

    private FirebaseFirestore firebaseFirestore;
    private BlogRecyclerAdapter blogRecyclerAdapter;
    private FirebaseAuth firebaseAuth;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        blogList = new ArrayList<>();
        userList = new ArrayList<>();
        blogListView = view.findViewById(R.id.blogListView);

        blogRecyclerAdapter = new BlogRecyclerAdapter(blogList, userList);
        blogListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        blogListView.setAdapter(blogRecyclerAdapter);
        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null) {
            firebaseFirestore = FirebaseFirestore.getInstance();

            Query nextQuery = firebaseFirestore.collection("Posts").orderBy("timestamp", Query.Direction.DESCENDING);//.limit(3);
            nextQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                    for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                        if (doc.getType() == DocumentChange.Type.ADDED) {
                            String blogPostId = doc.getDocument().getId();
                            final BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).whithId(blogPostId);

                            firebaseFirestore.collection("Users").document(blogPostId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        User user = task.getResult().toObject(User.class);

                                            userList.add(user);
                                            blogList.add(blogPost);

                                        blogRecyclerAdapter.notifyDataSetChanged();
                                    }
                                }
                            });
                        }
                    }
                }
            });
        }
        return view;
    }

}
