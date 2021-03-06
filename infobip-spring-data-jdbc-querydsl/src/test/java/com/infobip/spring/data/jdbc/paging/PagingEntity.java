package com.infobip.spring.data.jdbc.paging;

import lombok.*;
import org.springframework.data.annotation.Id;

@Value
public class PagingEntity {

    @With
    @Id
    private final Long id;
    private final String value;
}
