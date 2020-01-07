package com.infobip.spring.data.jpa;

import com.querydsl.core.types.*;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.HQLTemplates;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.*;
import com.querydsl.jpa.sql.JPASQLQuery;
import com.querydsl.sql.SQLTemplates;
import com.querydsl.sql.SQLTemplatesRegistry;
import org.hibernate.engine.spi.SessionImplementor;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.QuerydslJpaRepository;
import org.springframework.data.querydsl.SimpleEntityPathResolver;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.io.Serializable;
import java.sql.*;
import java.util.*;
import java.util.function.*;

@Transactional(readOnly = true)
public class SimpleExtendedQuerydslJpaRepository<T, ID extends Serializable> extends QuerydslJpaRepository<T, ID>
        implements ExtendedQuerydslJpaRepository<T, ID> {

    private final EntityPath<T> path;
    private final JPAQueryFactory jpaQueryFactory;
    private final Supplier<JPASQLQuery<T>> jpaSqlFactory;
    private final EntityManager entityManager;

    SimpleExtendedQuerydslJpaRepository(JpaEntityInformation<T, ID> entityInformation,
                                        EntityManager entityManager) throws SQLException {
        super(entityInformation, entityManager, SimpleEntityPathResolver.INSTANCE);
        this.jpaQueryFactory = new JPAQueryFactory(HQLTemplates.DEFAULT, entityManager);
        this.path = SimpleEntityPathResolver.INSTANCE.createPath(entityInformation.getJavaType());
        SQLTemplates sqlTemplates = getSQLServerTemplates(entityManager.getEntityManagerFactory());
        this.jpaSqlFactory = () -> new JPASQLQuery<>(entityManager, sqlTemplates);
        this.entityManager = entityManager;
    }

    @SafeVarargs
    @Override
    public final List<T> save(T... iterable) {
        return saveAll(Arrays.asList(iterable));
    }

    @Override
    public <O> O query(Function<JPAQuery<?>, O> query) {
        return query.apply(jpaQueryFactory.query());
    }

    @Transactional
    @Override
    public void update(Consumer<JPAUpdateClause> update) {

        update.accept(jpaQueryFactory.update(path));
    }

    @Transactional
    @Override
    public long deleteWhere(Predicate predicate) {

        return jpaQueryFactory.delete(path).where(predicate).execute();
    }

    @Override
    public <O> O jpaSqlQuery(Function<JPASQLQuery<T>, O> query) {
        return query.apply(jpaSqlFactory.get());
    }

    @Override
    public SubQueryExpression<T> jpaSqlSubQuery(Function<JPASQLQuery<T>, SubQueryExpression<T>> query) {
        return jpaSqlQuery(query);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected JPQLQuery<T> createQuery(Predicate... predicate) {
        return (JPQLQuery<T>) super.createQuery(predicate);
    }

    @Transactional
    @Override
    public <O> O executeStoredProcedure(String name, Function<StoredProcedureQueryBuilder, O> query) {
        return query.apply(new StoredProcedureQueryBuilder(name, entityManager));
    }

    private SQLTemplates getSQLServerTemplates(EntityManagerFactory entityManagerFactory) throws SQLException {
        DatabaseMetaData databaseMetaData = getDatabaseMetaData(entityManagerFactory.createEntityManager());
        return new SQLTemplatesRegistry().getTemplates(databaseMetaData);
    }

    private DatabaseMetaData getDatabaseMetaData(EntityManager entityManager) throws SQLException {
        SessionImplementor sessionImplementor = entityManager.unwrap(SessionImplementor.class);
        DatabaseMetaData metaData = sessionImplementor.connection().getMetaData();
        entityManager.close();
        return metaData;
    }
}