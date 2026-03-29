package com.vaani.app.core.pipeline;

import com.vaani.app.core.ai.GeminiClient;
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
public final class ErrorRecovery_Factory implements Factory<ErrorRecovery> {
  private final Provider<GeminiClient> geminiClientProvider;

  private final Provider<ActionExecutor> actionExecutorProvider;

  public ErrorRecovery_Factory(Provider<GeminiClient> geminiClientProvider,
      Provider<ActionExecutor> actionExecutorProvider) {
    this.geminiClientProvider = geminiClientProvider;
    this.actionExecutorProvider = actionExecutorProvider;
  }

  @Override
  public ErrorRecovery get() {
    return newInstance(geminiClientProvider.get(), actionExecutorProvider.get());
  }

  public static ErrorRecovery_Factory create(Provider<GeminiClient> geminiClientProvider,
      Provider<ActionExecutor> actionExecutorProvider) {
    return new ErrorRecovery_Factory(geminiClientProvider, actionExecutorProvider);
  }

  public static ErrorRecovery newInstance(GeminiClient geminiClient,
      ActionExecutor actionExecutor) {
    return new ErrorRecovery(geminiClient, actionExecutor);
  }
}
