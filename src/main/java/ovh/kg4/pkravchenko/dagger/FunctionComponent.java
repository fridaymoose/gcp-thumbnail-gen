package ovh.kg4.pkravchenko.dagger;

import com.google.gson.Gson;
import dagger.Component;
import ovh.kg4.pkravchenko.service.ThumbnailGenerator;

import javax.inject.Singleton;

@Singleton
@Component(modules = {
  EnvModule.class,
  FunctionModule.class
})
public interface FunctionComponent {

  Gson gson();

  ThumbnailGenerator thumbnailGenerator();
}
