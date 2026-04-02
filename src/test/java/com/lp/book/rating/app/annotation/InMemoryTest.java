package com.lp.book.rating.app.annotation;

import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@TestPropertySource(properties = {
        "spring.datasource.hikari.schema=book_rating"
})
@Sql(statements = {
        "DELETE FROM book_rating.refresh_token",
        "DELETE FROM book_rating.rating",
        "DELETE FROM book_rating.book",
        "DELETE FROM book_rating.users"
}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public @interface InMemoryTest {
}
