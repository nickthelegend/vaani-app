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
public final class AppResolver_Factory implements Factory<AppResolver> {
  private final Provider<Context> contextProvider;

  public AppResolver_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public AppResolver get() {
    return newInstance(contextProvider.get());
  }

  public static AppResolver_Factory create(Provider<Context> contextProvider) {
    return new AppResolver_Factory(contextProvider);
  }

  public static AppResolver newInstance(Context context) {
    return new AppResolver(context);
  }
}
