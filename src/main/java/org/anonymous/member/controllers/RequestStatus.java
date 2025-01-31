package org.anonymous.member.controllers;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.anonymous.member.constants.DomainStatus;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RequestStatus {

    private String email;

    private DomainStatus status;

    private String type;

    private Long seq;
}
