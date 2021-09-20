package kafdrop.model;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

// @ApiModel("Create topic model")
@Schema(name = "Create topic model")
public final class CreateTopicVO {
  
  @Schema(name = "Topic name")
  String name;

  @Schema(name = "Number of partitions")
  int partitionsNumber;

  @Schema(name = "Replication factor")
  int replicationFactor;

  public CreateTopicVO(String name, int partitionsNumber, int replicationFactor) {
    this.name = name;
    this.partitionsNumber = partitionsNumber;
    this.replicationFactor = replicationFactor;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getPartitionsNumber() {
    return partitionsNumber;
  }

  public void setPartitionsNumber(int partitionsNumber) {
    this.partitionsNumber = partitionsNumber;
  }

  public int getReplicationFactor() {
    return replicationFactor;
  }

  public void setReplicationFactor(int replicationFactor) {
    this.replicationFactor = replicationFactor;
  }


}
