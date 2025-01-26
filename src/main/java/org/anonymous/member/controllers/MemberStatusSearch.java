package org.anonymous.member.controllers;

import lombok.Data;
import org.anonymous.global.paging.CommonSearch;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@Data
public class MemberStatusSearch extends CommonSearch {
    private List<String> email;
    private List<String> type;
    private List<Boolean> isBlock;

    private String dateType;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate sDate;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate eDate;
}
