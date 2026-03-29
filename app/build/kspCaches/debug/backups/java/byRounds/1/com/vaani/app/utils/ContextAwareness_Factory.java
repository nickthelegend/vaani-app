package com.vaani.app.utils;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class ContextAwareness_Factory implements Factory<ContextAwareness> {
  private final Provider<Context> contextProvider;

  public ContextAwareness_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public ContextAwareness get() {
    return newInstance(contextProvider.get());
  }

  public static ContextAwareness_Factory create(Provider<Context> contextProvider) {
    return new ContextAwareness_Factory(contextProvider);
  }

  public static ContextAwareness newInstance(Context context) {
    return new ContextAwareness(context);
  }
}
