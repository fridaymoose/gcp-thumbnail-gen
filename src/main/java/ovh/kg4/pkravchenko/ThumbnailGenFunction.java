package ovh.kg4.pkravchenko;

import com.google.cloud.functions.Context;
import com.google.cloud.functions.RawBackgroundFunction;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import ovh.kg4.pkravchenko.dagger.DaggerFunctionComponent;
import ovh.kg4.pkravchenko.dagger.FunctionComponent;
import ovh.kg4.pkravchenko.dto.StorageEvent;
import ovh.kg4.pkravchenko.service.ThumbnailGenerator;

@Slf4j
public class ThumbnailGenFunction implements RawBackgroundFunction {

  private static final FunctionComponent component = DaggerFunctionComponent.create();

  private final ThumbnailGenerator thumbnailGenerator;
  private final Gson gson;

  public ThumbnailGenFunction() {
    thumbnailGenerator = component.thumbnailGenerator();
    gson = component.gson();
  }

  @Override
  public void accept(String json, Context context) throws Exception {

    log.debug("json payload '{}'", json);

    StorageEvent event = gson.fromJson(json, StorageEvent.class);

    if (thumbnailGenerator.canGenerate(event)) {
      thumbnailGenerator.generate(event);
    } else {
      log.info("Thumbnail generation skipped for object '{}'", event.name());
    }
  }

}
