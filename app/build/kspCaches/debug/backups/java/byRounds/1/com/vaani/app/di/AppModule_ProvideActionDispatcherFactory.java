package com.vaani.app.di;

import com.vaani.app.core.accessibility.ActionDispatcher;
import com.vaani.app.core.accessibility.SmartElementFinder;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class AppModule_ProvideActionDispatcherFactory implements Factory<ActionDispatcher> {
  private final Provider<SmartElementFinder> smartElementFinderProvider;

  public AppModule_ProvideActionDispatcherFactory(
      Provider<SmartElementFinder> smartElementFinderProvider) {
    this.smartElementFinderProvider = smartElementFinderProvider;
  }

  @Override
  public ActionDispatcher get() {
    return provideActionDispatcher(smartElementFinderProvider.get());
  }

  public static AppModule_ProvideActionDispatcherFactory create(
      Provider<SmartElementFinder> smartElementFinderProvider) {
    return new AppModule_ProvideActionDispatcherFactory(smartElementFinderProvider);
  }

  public static ActionDispatcher provideActionDispatcher(SmartElementFinder smartElementFinder) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideActionDispatcher(smartElementFinder));
  }
}
