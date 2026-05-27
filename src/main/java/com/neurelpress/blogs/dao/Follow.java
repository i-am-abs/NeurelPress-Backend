package com.neurelpress.blogs.dao;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.UUID;

@Document(collection = "follows")
@CompoundIndex(name = "uk_follow_follower_following", def = "{'follower.$id': 1, 'following.$id': 1}", unique = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Follow {

    @Id
    @Builder.Default
    private UUID id = UUID.randomUUID();

    @DBRef
    private User follower;

    @DBRef
    private User following;

    @CreatedDate
    private Instant createdAt;
}
