package dev.tianmi.sussypatches.api.util;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.annotation.ParametersAreNonnullByDefault;

import dev.tianmi.sussypatches.api.util.Result.Err;
import dev.tianmi.sussypatches.api.util.Result.Ok;
import mcp.MethodsReturnNonnullByDefault;

// TODO)) UnwrappingException
/// Basically a re-impl of Result ADT from Rust
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
// spotless:off
public sealed interface Result<T, E> extends Iterable<T> permits Err, Ok {
// spotless:on

    record Ok<T, Nothing> (T t) implements Result<T, Nothing> {}

    record Err<Nothing, E> (E e) implements Result<Nothing, E> {}

    default boolean isOk() {
        return this instanceof Ok;
    }

    default boolean isOkAnd(Predicate<T> f) {
        return switch (this) {
            case Err(_) -> false;
            case Ok(T t) -> f.test(t);
        };
    }

    default boolean isErr() {
        return !this.isOk();
    }

    default boolean isErrAnd(Predicate<E> f) {
        return switch (this) {
            case Ok(_) -> false;
            case Err(E e) -> f.test(e);
        };
    }

    default Optional<T> ok() {
        return switch (this) {
            case Err(_) -> Optional.empty();
            case Ok(T t) -> Optional.of(t);
        };
    }

    default Optional<E> err() {
        return switch (this) {
            case Ok(_) -> Optional.empty();
            case Err(E e) -> Optional.of(e);
        };
    }

    default <U> Result<U, E> map(Function<T, U> op) {
        return switch (this) {
            case Ok(T t) -> new Ok<>(op.apply(t));
            case Err(E e) -> new Err<>(e);
        };
    }

    default <U> U mapOr(U fallback, Function<T, U> f) {
        return switch (this) {
            case Ok(T t) -> f.apply(t);
            case Err(E e) -> fallback;
        };
    }

    default <U> U mapOrElse(Function<E, U> fallback, Function<T, U> f) {
        return switch (this) {
            case Ok(T t) -> f.apply(t);
            case Err(E e) -> fallback.apply(e);
        };
    }

    default <F> Result<T, F> mapErr(Function<E, F> op) {
        return switch (this) {
            case Ok(T t) -> new Ok<>(t);
            case Err(E e) -> new Err<>(op.apply(e));
        };
    }

    default Result<T, E> inspect(Consumer<T> f) {
        if (this instanceof Ok(T t)) {
            f.accept(t);
        }
        return this;
    }

    default Result<T, E> inspectErr(Consumer<E> f) {
        if (this instanceof Err(E e)) {
            f.accept(e);
        }
        return this;
    }

    @Override
    default Iterator<T> iterator() {
        return switch (this) {
            case Ok(T t) -> singletonIterator(t);
            case Err(_) -> Collections.emptyIterator();
        };
    }

    default T expect(String msg) {
        return switch (this) {
            case Ok(T t) -> t;
            case Err(E e) -> throw new NoSuchElementException(msg + ": " + e);
        };
    }

    default T unwrap() {
        return switch (this) {
            case Ok(T t) -> t;
            case Err(E e) -> throw new NoSuchElementException("Unwrapped Err: " + e);
        };
    }

    default T unwrapOrDefault(T fallback) {
        return switch (this) {
            case Ok(T t) -> t;
            case Err(_) -> fallback;
        };
    }

    default E expectErr(String msg) {
        return switch (this) {
            case Ok(T t) -> throw new NoSuchElementException(msg + ": " + t);
            case Err(E e) -> e;
        };
    }

    default E unwrapErr() {
        return switch (this) {
            case Ok(T t) -> throw new NoSuchElementException("Unwrapped Ok: " + t);
            case Err(E e) -> e;
        };
    }

    default <U> Result<U, E> and(Result<U, E> res) {
        return switch (this) {
            case Ok(_) -> res;
            case Err(E e) -> new Err<>(e);
        };
    }

    default <U> Result<U, E> andThen(Function<T, Result<U, E>> op) {
        return switch (this) {
            case Ok(T t) -> op.apply(t);
            case Err(E e) -> new Err<>(e);
        };
    }

    default Result<T, E> or(Result<T, E> res) {
        return switch (this) {
            case Ok(T t) -> new Ok<>(t);
            case Err(_) -> res;
        };
    }

    default Result<T, E> orElse(Function<E, Result<T, E>> op) {
        return switch (this) {
            case Ok(T t) -> new Ok<>(t);
            case Err(E e) -> op.apply(e);
        };
    }

    default T unwrapOr(T fallback) {
        return switch (this) {
            case Ok(T t) -> t;
            case Err(_) -> fallback;
        };
    }

    default T unwrapOrElse(Function<E, T> op) {
        return switch (this) {
            case Ok(T t) -> t;
            case Err(E e) -> op.apply(e);
        };
    }

    private static <V> Iterator<V> singletonIterator(final V v) {
        return new Iterator<>() {

            private boolean hasNext = true;

            public boolean hasNext() {
                return hasNext;
            }

            public V next() {
                if (hasNext) {
                    hasNext = false;
                    return v;
                }
                throw new NoSuchElementException();
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }

            @Override
            public void forEachRemaining(Consumer<? super V> action) {
                Objects.requireNonNull(action);
                if (hasNext) {
                    hasNext = false;
                    action.accept(v);
                }
            }
        };
    }
}
