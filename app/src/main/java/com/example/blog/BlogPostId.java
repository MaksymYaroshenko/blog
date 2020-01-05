package com.example.blog;

import io.reactivex.annotations.NonNull;

public class BlogPostId {
    public String BlogPostId;

    public <T extends BlogPostId> T whithId(@NonNull final String id) {
        this.BlogPostId = id;
        return (T) this;
    }

}
