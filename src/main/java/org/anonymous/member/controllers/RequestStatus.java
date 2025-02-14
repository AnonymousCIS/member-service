package org.anonymous.member.controllers;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.anonymous.member.constants.DomainStatus;
import org.anonymous.member.entities.Member;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RequestStatus {

    private Member member;

    private DomainStatus status;

    private String type;

    private Long seq;
}
