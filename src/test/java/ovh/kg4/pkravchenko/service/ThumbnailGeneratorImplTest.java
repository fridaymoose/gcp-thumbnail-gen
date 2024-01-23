package ovh.kg4.pkravchenko.service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageClass;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ovh.kg4.pkravchenko.dto.StorageEvent;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ThumbnailGeneratorImplTest {

  @Mock
  Storage storage;

  @Mock
  Blob inBlob;
  @Mock
  Blob outBlob;

  ThumbnailGenerator generator;

  AutoCloseable closeable;

  @BeforeEach
  void setUp() {
    closeable = MockitoAnnotations.openMocks(this);
    generator = new ThumbnailGeneratorImpl(storage, "_thumbnail", 256, 256, Set.of(".jpeg", ".jpg", ".png"));
  }

  @ParameterizedTest
  @MethodSource("provideEventsForValidation")
  void canGenerate(StorageEvent event, boolean expected) {

    assertEquals(expected, generator.canGenerate(event));

  }

  static Stream<Arguments> provideEventsForValidation() {

    String bucket = "my-cool-bucket";
    String storageClass = StorageClass.STANDARD.name();

    return Stream.of(
      Arguments.of(new StorageEvent(bucket, "donuts.png", "image/png", storageClass), true),
      Arguments.of(new StorageEvent(bucket, "folder/donuts.jpg", "image/jpg", storageClass), true),
      Arguments.of(new StorageEvent(bucket, "donuts.on.a.tree.jpeg", "image/jpeg", storageClass), true),
      Arguments.of(new StorageEvent(bucket, "unknown-image", "binary", storageClass), false),
      Arguments.of(new StorageEvent(bucket, "donuts_thumbnail.png", "image/png", storageClass), false)
    );
  }

  @ParameterizedTest
  @MethodSource("provideEventsForGeneration")
  void generate(StorageEvent event, String thumbnail) throws IOException {

    doAnswer(
      invocation -> {
        Path arg0 = invocation.getArgument(0);

        InputStream in = ThumbnailGeneratorImplTest.class.getClassLoader().getResourceAsStream("donuts.png");
        Files.copy(in, arg0, StandardCopyOption.REPLACE_EXISTING);

        return null;
      }
    ).when(inBlob).downloadTo(any(Path.class));

    BlobId blobId = BlobId.of(event.bucket(), event.name());
    when(storage.get(blobId)).thenReturn(inBlob);

    BlobInfo blobInfo = BlobInfo.newBuilder(event.bucket(), thumbnail)
      .setContentType(event.contentType())
      .setStorageClass(StorageClass.valueOf(event.storageClass()))
      .build();
    when(storage.createFrom(eq(blobInfo), any(Path.class))).thenReturn(outBlob);

    generator.generate(event);

    verify(storage, times(1)).get(blobId);
    verify(storage, times(1)).createFrom(eq(blobInfo), any(Path.class));
  }

  static Stream<Arguments> provideEventsForGeneration() {

    String bucket = "my-cool-bucket";
    String storageClass = StorageClass.STANDARD.name();

    return Stream.of(
      Arguments.of(new StorageEvent(bucket, "donuts.png", "image/png", storageClass), "donuts_thumbnail.png"),
      Arguments.of(new StorageEvent(bucket, "folder/donuts.jpg", "image/jpg", storageClass), "folder/donuts_thumbnail.jpg"),
      Arguments.of(new StorageEvent(bucket, "donuts.on.a.tree.jpeg", "image/jpeg", storageClass), "donuts.on.a.tree_thumbnail.jpeg")
    );
  }

}