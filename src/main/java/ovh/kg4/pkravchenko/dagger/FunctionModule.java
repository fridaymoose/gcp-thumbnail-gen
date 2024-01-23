package ovh.kg4.pkravchenko.dagger;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dagger.Module;
import dagger.Provides;
import ovh.kg4.pkravchenko.service.ThumbnailGenerator;
import ovh.kg4.pkravchenko.service.ThumbnailGeneratorImpl;

import javax.inject.Named;
import java.io.IOException;
import java.util.Objects;

@Module
public class FunctionModule {

  @Provides
  static Gson gson() {
    return new GsonBuilder().serializeNulls().create();
  }

  @Provides
  static Storage storage(@Named("gcp.project.id") String projectId) {

    if (Objects.isNull(projectId) || projectId.isEmpty()) {
      throw new RuntimeException("Project id hasn't been set!");
    }

    try {
      StorageOptions storageOptions =
        StorageOptions.getDefaultInstance().toBuilder()
          .setProjectId(projectId)
          .setCredentials(GoogleCredentials.getApplicationDefault())
          .build();
      return storageOptions.getService();
    } catch (IOException ioe) {
      throw new RuntimeException("Cloud storage initialization failed!", ioe);
    }
  }

  @Provides
  static ThumbnailGenerator thumbnailGenerator(ThumbnailGeneratorImpl thumbnailGeneratorImpl) {
    return thumbnailGeneratorImpl;
  }
}

