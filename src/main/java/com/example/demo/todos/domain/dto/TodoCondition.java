package com.example.demo.todos.domain.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter @Setter
public class TodoCondition {
    private String title;
    private String content;
    private Boolean isDone;
    private Integer likeGoe;
    private Integer likeLoe;
}
