package com.vaani.app.core.pipeline;

import com.vaani.app.core.accessibility.ActionDispatcher;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class ActionExecutor_Factory implements Factory<ActionExecutor> {
  private final Provider<ActionDispatcher> actionDispatcherProvider;

  public ActionExecutor_Factory(Provider<ActionDispatcher> actionDispatcherProvider) {
    this.actionDispatcherProvider = actionDispatcherProvider;
  }

  @Override
  public ActionExecutor get() {
    return newInstance(actionDispatcherProvider.get());
  }

  public static ActionExecutor_Factory create(Provider<ActionDispatcher> actionDispatcherProvider) {
    return new ActionExecutor_Factory(actionDispatcherProvider);
  }

  public static ActionExecutor newInstance(ActionDispatcher actionDispatcher) {
    return new ActionExecutor(actionDispatcher);
  }
}
