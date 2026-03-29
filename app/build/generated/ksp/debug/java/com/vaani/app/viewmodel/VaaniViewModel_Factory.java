package com.vaani.app.viewmodel;

import com.vaani.app.core.pipeline.TaskPipeline;
import com.vaani.app.core.voice.VoiceRecognizer;
import com.vaani.app.data.repository.TaskRepository;
import com.vaani.app.utils.DemoMode;
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
public final class VaaniViewModel_Factory implements Factory<VaaniViewModel> {
  private final Provider<TaskPipeline> taskPipelineProvider;

  private final Provider<VoiceRecognizer> voiceRecognizerProvider;

  private final Provider<TaskRepository> taskRepositoryProvider;

  private final Provider<DemoMode> demoModeProvider;

  public VaaniViewModel_Factory(Provider<TaskPipeline> taskPipelineProvider,
      Provider<VoiceRecognizer> voiceRecognizerProvider,
      Provider<TaskRepository> taskRepositoryProvider, Provider<DemoMode> demoModeProvider) {
    this.taskPipelineProvider = taskPipelineProvider;
    this.voiceRecognizerProvider = voiceRecognizerProvider;
    this.taskRepositoryProvider = taskRepositoryProvider;
    this.demoModeProvider = demoModeProvider;
  }

  @Override
  public VaaniViewModel get() {
    return newInstance(taskPipelineProvider.get(), voiceRecognizerProvider.get(), taskRepositoryProvider.get(), demoModeProvider.get());
  }

  public static VaaniViewModel_Factory create(Provider<TaskPipeline> taskPipelineProvider,
      Provider<VoiceRecognizer> voiceRecognizerProvider,
      Provider<TaskRepository> taskRepositoryProvider, Provider<DemoMode> demoModeProvider) {
    return new VaaniViewModel_Factory(taskPipelineProvider, voiceRecognizerProvider, taskRepositoryProvider, demoModeProvider);
  }

  public static VaaniViewModel newInstance(TaskPipeline taskPipeline,
      VoiceRecognizer voiceRecognizer, TaskRepository taskRepository, DemoMode demoMode) {
    return new VaaniViewModel(taskPipeline, voiceRecognizer, taskRepository, demoMode);
  }
}
