package com.example.demo.todos.repository;

import com.example.demo.todos.domain.entity.Todo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TodoRepository extends JpaRepository<Todo, Long>, CustomTodoRepository {
}
