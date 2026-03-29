package com.vaani.app.core.ai;

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
public final class ConversationManager_Factory implements Factory<ConversationManager> {
  @Override
  public ConversationManager get() {
    return newInstance();
  }

  public static ConversationManager_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static ConversationManager newInstance() {
    return new ConversationManager();
  }

  private static final class InstanceHolder {
    private static final ConversationManager_Factory INSTANCE = new ConversationManager_Factory();
  }
}
