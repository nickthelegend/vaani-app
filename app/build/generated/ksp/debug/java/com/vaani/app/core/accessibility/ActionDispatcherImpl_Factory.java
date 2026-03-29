package com.vaani.app.core.accessibility;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class ActionDispatcherImpl_Factory implements Factory<ActionDispatcherImpl> {
  private final Provider<SmartElementFinder> smartElementFinderProvider;

  public ActionDispatcherImpl_Factory(Provider<SmartElementFinder> smartElementFinderProvider) {
    this.smartElementFinderProvider = smartElementFinderProvider;
  }

  @Override
  public ActionDispatcherImpl get() {
    return newInstance(smartElementFinderProvider.get());
  }

  public static ActionDispatcherImpl_Factory create(
      Provider<SmartElementFinder> smartElementFinderProvider) {
    return new ActionDispatcherImpl_Factory(smartElementFinderProvider);
  }

  public static ActionDispatcherImpl newInstance(SmartElementFinder smartElementFinder) {
    return new ActionDispatcherImpl(smartElementFinder);
  }
}
