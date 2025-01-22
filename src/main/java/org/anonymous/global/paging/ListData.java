package org.anonymous.global.paging;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListData<T> {

    private List<T> data;
    private Pagination pagination;
}
