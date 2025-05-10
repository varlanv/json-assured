package com.varlanv.jsonassured;

import java.util.function.Supplier;
import org.jetbrains.annotations.Nullable;

final class MemoizedSupplier<T> implements Supplier<T> {

  private Supplier<T> supplier;
  @Nullable private T value;

  MemoizedSupplier(Supplier<T> supplier) {
    this.supplier = supplier;
  }

  @Override
  public T get() {
    @Nullable var val = value;
    if (val != null) {
      return val;
    }
    val = supplier.get();
    value = val;
    supplier = null;
    return val;
  }
}
