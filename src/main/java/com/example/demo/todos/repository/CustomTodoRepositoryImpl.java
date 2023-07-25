package com.example.demo.todos.repository;

import com.example.demo.members.domain.entity.QMember;
import com.example.demo.todos.domain.dto.TodoCondition;
import com.example.demo.todos.domain.entity.QTodo;
import com.example.demo.todos.service.CustomTodoRepository;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.PageRequest;

public class CustomTodoRepositoryImpl implements CustomTodoRepository {

    private final JPAQueryFactory queryFactory;
    private final QTodo qTodo = QTodo.todo;
    private final QMember qMember = QMember.member;

    public CustomTodoRepositoryImpl(EntityManager entityManager) { // ㅋㅓ리 팩토리
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    public void findAllByCondition(PageRequest request,
                                   TodoCondition condition
    ) {
        queryFactory
                .select(qTodo)
                .from(qTodo)
                .leftJoin(qTodo.member, qMember)
                .fetchJoin()
                .where(
                        contentContains(condition.getContent()),
                        titleEq(condition.getTitle())
                )
                .offset(request.getPageNumber())
                .limit(request.getPageSize());

        contentContains(condition.getContent());

    }
    private  BooleanExpression contentContains(String content) { // 여기 안에서만 쓸거라 static지워줌. 여기는 콘텐츠만 있으면 되ㅑㄴ다.
        return
                content == null ? null
                        : qTodo.content.contains(content);  // condition.getContent()이게 null이면  null을 리턴할거고 : 아니면 이것 반환.
    }

    private  BooleanExpression titleEq(String title) {
        return
                title == null ? null
                        : qTodo.content.contains(title);
    }
}
