package com.sciencelove.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {

    private UUID id;
    private UUID senderId;
    private String senderName;
    private String content;
    private Boolean hasAttachment;
    private String attachmentUrl;
    private Boolean isRead;
    private LocalDateTime createdAt;
}