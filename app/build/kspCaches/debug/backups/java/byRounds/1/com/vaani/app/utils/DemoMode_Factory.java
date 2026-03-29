package com.vaani.app.utils;

import com.vaani.app.core.pipeline.TaskPipeline;
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
public final class DemoMode_Factory implements Factory<DemoMode> {
  private final Provider<TaskPipeline> taskPipelineProvider;

  public DemoMode_Factory(Provider<TaskPipeline> taskPipelineProvider) {
    this.taskPipelineProvider = taskPipelineProvider;
  }

  @Override
  public DemoMode get() {
    return newInstance(taskPipelineProvider.get());
  }

  public static DemoMode_Factory create(Provider<TaskPipeline> taskPipelineProvider) {
    return new DemoMode_Factory(taskPipelineProvider);
  }

  public static DemoMode newInstance(TaskPipeline taskPipeline) {
    return new DemoMode(taskPipeline);
  }
}
