package com.vaani.app.di;

import com.vaani.app.data.db.TaskDao;
import com.vaani.app.data.db.VaaniDatabase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class AppModule_ProvideTaskDaoFactory implements Factory<TaskDao> {
  private final Provider<VaaniDatabase> databaseProvider;

  public AppModule_ProvideTaskDaoFactory(Provider<VaaniDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public TaskDao get() {
    return provideTaskDao(databaseProvider.get());
  }

  public static AppModule_ProvideTaskDaoFactory create(Provider<VaaniDatabase> databaseProvider) {
    return new AppModule_ProvideTaskDaoFactory(databaseProvider);
  }

  public static TaskDao provideTaskDao(VaaniDatabase database) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideTaskDao(database));
  }
}
