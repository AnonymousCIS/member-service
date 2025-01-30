package org.anonymous.member.controllers;

import lombok.Data;
import org.anonymous.global.paging.CommonSearch;
import org.anonymous.member.constants.DomainStatus;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@Data
public class MemberStatusSearch extends CommonSearch {
    private List<String> email;
    private List<String> type;
    private List<DomainStatus> domainStatuses;

    private String dateType;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate sDate;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate eDate;
}
