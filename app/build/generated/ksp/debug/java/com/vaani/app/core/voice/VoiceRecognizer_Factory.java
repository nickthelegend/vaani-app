package com.vaani.app.core.voice;

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
public final class VoiceRecognizer_Factory implements Factory<VoiceRecognizer> {
  private final Provider<Context> contextProvider;

  public VoiceRecognizer_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public VoiceRecognizer get() {
    return newInstance(contextProvider.get());
  }

  public static VoiceRecognizer_Factory create(Provider<Context> contextProvider) {
    return new VoiceRecognizer_Factory(contextProvider);
  }

  public static VoiceRecognizer newInstance(Context context) {
    return new VoiceRecognizer(context);
  }
}
