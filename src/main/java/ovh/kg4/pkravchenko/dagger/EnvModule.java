package ovh.kg4.pkravchenko.dagger;

import dagger.Module;
import dagger.Provides;

import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Module
public class EnvModule {

  @Provides
  @Singleton
  Properties env() {
    Properties properties = new Properties();
    try {
      InputStream in = EnvModule.class.getClassLoader().getResourceAsStream("application.properties");
      if (in != null) {
        properties.load(in);
      }
    } catch (IOException e) {
      // do nothing
    }

    final Map<String, String> env = System.getenv();
    properties.putAll(env);

    return properties;
  }

  @Provides
  @Named("gcp.project.id")
  String storageProjectId(Properties env) {
    return env.getProperty("gcp.project.id");
  }

  @Provides
  @Named("thumbnail.suffix")
  String thumbnailSuffix(Properties env) {
    return env.getProperty("thumbnail.suffix");
  }

  @Provides
  @Named("thumbnail.width")
  Integer thumbnailWidth(Properties env) {
    return Integer.parseInt(env.getProperty("thumbnail.width"));
  }

  @Provides
  @Named("thumbnail.height")
  Integer thumbnailHeight(Properties env) {
    return Integer.parseInt(env.getProperty("thumbnail.height"));
  }

  @Provides
  @Named("supported.types")
  Set<String> supportedTypes(Properties env) {
    return Stream.of(env.getProperty("supported.types").split(","))
      .map(v -> '.' + v)
      .collect(Collectors.toUnmodifiableSet());
  }
}
