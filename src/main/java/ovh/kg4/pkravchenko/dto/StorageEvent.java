package ovh.kg4.pkravchenko.dto;

public record StorageEvent(String bucket, String name, String contentType, String storageClass) {
}
