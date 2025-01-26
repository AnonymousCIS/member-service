package org.anonymous.member.controllers;

import lombok.Data;
import org.anonymous.global.entities.BaseEntity;

@Data
public class RequestStatus {

    private String email;

    private boolean isBlock;

    private String type;

    private Long seq;
}
