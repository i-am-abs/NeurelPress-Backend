package com.neurelpress.blogs.dao;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.UUID;

@Document(collection = "email_otps")
@CompoundIndex(name = "idx_email_otp_user_code", def = "{'user.$id': 1, 'code': 1}")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailOtp {

    @Id
    @Builder.Default
    private UUID id = UUID.randomUUID();

    @DBRef
    private User user;

    @Indexed
    private String email;

    @Indexed
    private String code;

    private Instant expiresAt;

    @Builder.Default
    private boolean used = false;

    @CreatedDate
    private Instant createdAt;
}
