package ovh.kg4.pkravchenko.service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageClass;
import com.google.cloud.storage.StorageException;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import ovh.kg4.pkravchenko.dto.StorageEvent;
import ovh.kg4.pkravchenko.exception.FunctionException;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.Set;

@Slf4j
public class ThumbnailGeneratorImpl implements ThumbnailGenerator {

  private final Storage storage;
  private final String thumbnailSuffix;
  private final Integer thumbnailWidth;
  private final Integer thumbnailHeight;
  private final Set<String> suppotedTypes;

  @Inject
  public ThumbnailGeneratorImpl(
    Storage storage,
    @Named("thumbnail.suffix") String thumbnailSuffix,
    @Named("thumbnail.width") Integer thumbnailWidth,
    @Named("thumbnail.height") Integer thumbnailHeight,
    @Named("supported.types") Set<String> suppotedTypes) {

    this.storage = storage;
    this.thumbnailSuffix = thumbnailSuffix;
    this.thumbnailWidth = thumbnailWidth;
    this.thumbnailHeight = thumbnailHeight;
    this.suppotedTypes = suppotedTypes;
  }

  @Override
  public boolean canGenerate(StorageEvent event) {

    int extInd = event.name().lastIndexOf('.');
    String ext = extInd < 0 ? "" : event.name().substring(extInd);

    log.debug("ext={}, thumbnailSuffix={}, suppotedTypes={}", ext, thumbnailSuffix, suppotedTypes);

    return suppotedTypes.contains(ext) && !event.name().endsWith(thumbnailSuffix + ext);
  }

  @Override
  public void generate(StorageEvent event) {

    Blob blob = storage.get(BlobId.of(event.bucket(), event.name()));

    int extInd = event.name().lastIndexOf('.');
    String bareFullName = extInd < 0 ? event.name() : event.name().substring(0, extInd);
    String ext = extInd < 0 ? "" : event.name().substring(extInd);

    int folderIndex = event.name().lastIndexOf('/');
    String bareName = folderIndex < 0 ? bareFullName : bareFullName.substring(folderIndex + 1);

    Path thumbnail = null;
    Path original = null;
    try {
      original = Files.createTempFile(bareName, ext);
      blob.downloadTo(original);

      thumbnail = Files.createTempFile(bareName + thumbnailSuffix, ext);
      Thumbnails.of(original.toFile())
        .size(thumbnailWidth, thumbnailHeight)
        .toFile(thumbnail.toFile());

      BlobInfo blobInfo = BlobInfo.newBuilder(event.bucket(), bareFullName + thumbnailSuffix + ext)
        .setContentType(event.contentType())
        .setStorageClass(StorageClass.valueOf(event.storageClass()))
        .build();

      storage.createFrom(blobInfo, thumbnail);

      log.info("Thumbnail generation finished for object '{}', thumbnail object is '{}'", event.name(), blobInfo.getName());

    } catch (StorageException e) {
      throw new FunctionException("Can't persist generated image!", e);
    } catch (IOException e) {
      throw new FunctionException("Can't process IO operation!", e);
    } finally {
      try {
        safeDelete(original);
        safeDelete(thumbnail);
      } catch (IOException e) {
        log.error("Can't delete temporary file", e);
      }
    }
  }

  private static void safeDelete(Path tmpFile) throws IOException {

    if (Objects.isNull(tmpFile)) {
      return;
    }

    // important on Linux system
    try (FileChannel fc = FileChannel.open(tmpFile, StandardOpenOption.WRITE)) {
      fc.truncate(0);
    }

    Files.delete(tmpFile);
  }
}
