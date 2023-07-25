package com.example.demo.todos.controller;

import com.example.demo.config.aspect.TokenRequired;
import com.example.demo.config.auth.AuthService;
import com.example.demo.todos.domain.dto.TodoCondition;
import com.example.demo.todos.domain.request.TodoRequest;
import com.example.demo.todos.domain.response.TodoResponse;
import com.example.demo.todos.service.TodoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/todos")
public class TodoController {
    private final TodoService todoService;
    private final AuthService authService;
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @TokenRequired
    public void postTodos(
            @RequestBody TodoRequest request,
            @RequestHeader("Authorization") String token
    ){
        Map<String, Object> map = authService
                .getClaims(token.replace("Bearer ", ""));
        request.setMemberId(((Integer) map.get("memberId")).longValue());
        todoService.insert(request);
    }
    @PutMapping("{todoId}/check")
    @TokenRequired
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void check(
            @PathVariable("todoId") Long todoId,
            @RequestHeader("Authorization") String token
            ){
        Map<String, Object> map =
                authService.getClaims(token.replace("Bearer ", ""));
        Long memberId = ((Integer) map.get("memberId")).longValue();
        todoService.check(todoId, memberId);
    }

    @GetMapping
    public Page<TodoResponse> getAll(
            @RequestParam(value = "page"
                    , required = false
                    , defaultValue = "0") Integer page,
            @RequestParam(value = "size"
                    , required = false
                    , defaultValue = "20") Integer size,
            TodoCondition condition
            ) {
        return todoService.getAll(
                PageRequest.of(page, size),
                condition);
    }
}
