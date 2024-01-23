package ovh.kg4.pkravchenko.service;

import ovh.kg4.pkravchenko.dto.StorageEvent;

import java.io.IOException;

public interface ThumbnailGenerator {

  boolean canGenerate(StorageEvent data);
  void generate(StorageEvent data) throws IOException;
}
