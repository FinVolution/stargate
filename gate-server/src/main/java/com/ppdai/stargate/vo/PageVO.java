package com.ppdai.stargate.vo;

import java.util.List;

import lombok.Data;

@Data
public class PageVO<T> {

    private List<T> content;
    private long totalElements;
    private int totalPages;
    private boolean last;
    private boolean first;
    private int numberOfElements;
    private int size;
    private int number;

}