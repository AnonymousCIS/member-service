package org.anonymous.member.controllers;

import lombok.Data;
import org.anonymous.member.constants.DomainStatus;

@Data
public class RequestStatus {

    private String email;

    private DomainStatus status;

    private String type;

    private Long seq;
}
