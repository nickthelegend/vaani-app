package com.vaani.app.core.pipeline;

import com.vaani.app.core.ai.GeminiClient;
import com.vaani.app.core.voice.TTSManager;
import com.vaani.app.data.repository.TaskRepository;
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
public final class TaskPipeline_Factory implements Factory<TaskPipeline> {
  private final Provider<GeminiClient> geminiClientProvider;

  private final Provider<ActionExecutor> actionExecutorProvider;

  private final Provider<TTSManager> ttsManagerProvider;

  private final Provider<TaskRepository> taskRepositoryProvider;

  public TaskPipeline_Factory(Provider<GeminiClient> geminiClientProvider,
      Provider<ActionExecutor> actionExecutorProvider, Provider<TTSManager> ttsManagerProvider,
      Provider<TaskRepository> taskRepositoryProvider) {
    this.geminiClientProvider = geminiClientProvider;
    this.actionExecutorProvider = actionExecutorProvider;
    this.ttsManagerProvider = ttsManagerProvider;
    this.taskRepositoryProvider = taskRepositoryProvider;
  }

  @Override
  public TaskPipeline get() {
    return newInstance(geminiClientProvider.get(), actionExecutorProvider.get(), ttsManagerProvider.get(), taskRepositoryProvider.get());
  }

  public static TaskPipeline_Factory create(Provider<GeminiClient> geminiClientProvider,
      Provider<ActionExecutor> actionExecutorProvider, Provider<TTSManager> ttsManagerProvider,
      Provider<TaskRepository> taskRepositoryProvider) {
    return new TaskPipeline_Factory(geminiClientProvider, actionExecutorProvider, ttsManagerProvider, taskRepositoryProvider);
  }

  public static TaskPipeline newInstance(GeminiClient geminiClient, ActionExecutor actionExecutor,
      TTSManager ttsManager, TaskRepository taskRepository) {
    return new TaskPipeline(geminiClient, actionExecutor, ttsManager, taskRepository);
  }
}
