package fi.thl.termed.util.collect;

import static java.util.Objects.requireNonNull;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Function;

public interface Either<L, R> {

  static <L, R> Either<L, R> left(L left) {
    return Left.of(left);
  }

  static <L, R> Either<L, R> right(R right) {
    return Right.of(right);
  }

  boolean isLeft();

  boolean isRight();

  L getLeft();

  R getRight();

  default <T> Either<T, R> mapLeft(Function<? super L, ? extends T> mapping) {
    return isLeft() ? new Left<>(mapping.apply(getLeft())) : new Right<>(getRight());
  }

  default <T> Either<L, T> mapRight(Function<? super R, ? extends T> mapping) {
    return isRight() ? new Right<>(mapping.apply(getRight())) : new Left<>(getLeft());
  }

  class Left<L, R> implements Either<L, R> {

    private final L left;

    private Left(L left) {
      this.left = requireNonNull(left);
    }

    public static <L, R> Left<L, R> of(L left) {
      return new Left<>(left);
    }

    @Override
    public boolean isLeft() {
      return true;
    }

    @Override
    public boolean isRight() {
      return false;
    }

    @Override
    public L getLeft() {
      return left;
    }

    @Override
    public R getRight() {
      throw new NoSuchElementException();
    }

    @Override
    public String toString() {
      return String.format("Left[%s]", left);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      Left<?, ?> that = (Left<?, ?>) o;
      return Objects.equals(left, that.left);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(left);
    }

  }

  class Right<L, R> implements Either<L, R> {

    private final R right;

    private Right(R right) {
      this.right = requireNonNull(right);
    }

    public static <L, R> Right<L, R> of(R right) {
      return new Right<>(right);
    }

    @Override
    public boolean isLeft() {
      return false;
    }

    @Override
    public boolean isRight() {
      return true;
    }

    @Override
    public L getLeft() {
      throw new NoSuchElementException();
    }

    @Override
    public R getRight() {
      return right;
    }

    @Override
    public String toString() {
      return String.format("Right[%s]", right);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      Right<?, ?> that = (Right<?, ?>) o;
      return Objects.equals(right, that.right);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(right);
    }

  }

}
