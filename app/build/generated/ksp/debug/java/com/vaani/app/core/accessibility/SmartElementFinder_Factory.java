package com.vaani.app.core.accessibility;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class SmartElementFinder_Factory implements Factory<SmartElementFinder> {
  @Override
  public SmartElementFinder get() {
    return newInstance();
  }

  public static SmartElementFinder_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static SmartElementFinder newInstance() {
    return new SmartElementFinder();
  }

  private static final class InstanceHolder {
    private static final SmartElementFinder_Factory INSTANCE = new SmartElementFinder_Factory();
  }
}
