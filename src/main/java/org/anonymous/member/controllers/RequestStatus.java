package org.anonymous.member.controllers;

import lombok.Data;
import org.anonymous.member.constants.MemberDomainStatus;

@Data
public class RequestStatus {

    private String email;

    private MemberDomainStatus status;

    private String type;

    private Long seq;
}
