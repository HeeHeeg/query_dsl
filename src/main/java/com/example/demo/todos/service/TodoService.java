package com.example.demo.todos.service;

import com.example.demo.config.service.MemberLoginService;
import com.example.demo.members.domain.entity.Member;
import com.example.demo.todos.domain.dto.TodoCondition;
import com.example.demo.todos.domain.entity.Todo;
import com.example.demo.todos.domain.request.TodoRequest;
import com.example.demo.todos.domain.response.TodoResponse;
import com.example.demo.todos.repository.TodoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TodoService {
    private final TodoRepository todoRepository;
    private final MemberLoginService memberLoginService;
    public void insert(TodoRequest request){
//        memberLoginService.findByMember(request.getMemberId());
        todoRepository.save(request.toEntity());
    }
//PUT {todoId}/check (isDone -> true)
//202
//404
//TODOS_NOT_FOUND
//401
//CHECK LOGIN USRE
    @Transactional
    public void check(Long todoId, Long memberId){
        Optional<Todo> byId = todoRepository.findById(todoId);
        Todo todo = byId
                .orElseThrow(() -> new RuntimeException("TODOS NOT FOUND"));
        if(!todo.getMember().getId().equals(memberId))
            throw new RuntimeException("MEMBER NOT FOUND");
        todo.changeIsDone();

    }

    @Transactional(readOnly = true)
    public Page<TodoResponse> getAll(PageRequest request, TodoCondition condition) {
        Page<Todo> allByCondition = todoRepository
                .findAllByCondition(request, condition);
        return allByCondition
                .map(TodoResponse::new); //page에서 콘텐츠를 부를때는 이렇게 바로 map으로
    }
}
