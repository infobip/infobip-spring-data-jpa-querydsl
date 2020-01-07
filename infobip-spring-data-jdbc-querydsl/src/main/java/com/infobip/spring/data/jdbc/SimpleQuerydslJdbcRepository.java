/*
 * Copyright 2017-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.infobip.spring.data.jdbc;

import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLQueryFactory;
import org.springframework.data.jdbc.core.JdbcAggregateOperations;
import org.springframework.data.jdbc.repository.support.SimpleJdbcRepository;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Transactional(readOnly = true)
public class SimpleQuerydslJdbcRepository<T, ID> extends SimpleJdbcRepository<T, ID> implements QuerydslJdbcRepository<T, ID> {

	private final SQLQueryFactory sqlQueryFactory;

	public SimpleQuerydslJdbcRepository(JdbcAggregateOperations entityOperations,
	                                    PersistentEntity<T, ?> entity,
	                                    SQLQueryFactory sqlQueryFactory) {
		super(entityOperations, entity);
		this.sqlQueryFactory = sqlQueryFactory;
	}

	@SafeVarargs
//	@Override
	public final List<T> save(T... iterable) {
		return Stream.of(iterable)
		             .map(this::save)
		             .collect(Collectors.toList());
	}

	@Override
	public <O> O query(Function<SQLQuery<?>, O> query) {
		return query.apply(sqlQueryFactory.query());
	}
}