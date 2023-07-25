package com.example.demo.todos.repository;

import com.example.demo.todos.domain.dto.TodoCondition;
import com.example.demo.todos.domain.entity.Todo;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import static com.example.demo.todos.domain.entity.QTodo.todo;
import static com.example.demo.members.domain.entity.QMember.member;

public class CustomTodoRepositoryImpl implements CustomTodoRepository {

    private final JPAQueryFactory queryFactory;

    public CustomTodoRepositoryImpl(EntityManager entityManager) { // ㅋㅓ리 팩토리
        this.queryFactory = new JPAQueryFactory(entityManager);
    }
    @Override
    public Page<Todo> findAllByCondition(PageRequest request,
                                         TodoCondition condition
    ) {
        JPAQuery<Todo> query = queryFactory
                .select(todo)
                .from(todo)
                .leftJoin(todo.member, member)
                .fetchJoin()
                .where(
                        contentContains(condition.getContent()),
                        titleEq(condition.getTitle())
                )
                .offset(request.getPageNumber())
                .limit(request.getPageSize());
        List<Todo> content = query.fetch();
        Long totalSize = queryFactory
                .select(todo.count())
                .from(todo)
                .where(
                        contentContains(condition.getContent()),
                        titleEq(condition.getTitle())
                )
                .fetchOne();
        return new PageImpl<>(content, request, totalSize); // list, pageable, long 순서로 넣어주는 것.
    }
    private  BooleanExpression contentContains(String content) { // 여기 안에서만 쓸거라 static지워줌. 여기는 콘텐츠만 있으면 되ㅑㄴ다.
        return
                content == null ? null
                        : todo.content.contains(content);  // condition.getContent()이게 null이면  null을 리턴할거고 : 아니면 이것 반환.
    }

    private  BooleanExpression titleEq(String title) {
        return
                title == null ? null
                        : todo.content.contains(title);
    }
}
