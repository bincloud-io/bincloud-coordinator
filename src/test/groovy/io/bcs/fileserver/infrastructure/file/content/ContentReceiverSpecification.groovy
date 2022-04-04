package io.bcs.fileserver.infrastructure.file.content


import io.bce.interaction.streaming.Source
import io.bce.interaction.streaming.binary.BinaryChunk
import io.bce.interaction.streaming.binary.InputStreamSource
import io.bcs.fileserver.domain.model.content.ContentFragment
import io.bcs.fileserver.domain.model.content.FileContent
import io.bcs.fileserver.domain.model.content.FileMetadata
import io.bcs.fileserver.domain.model.content.FileContent.ContentPart
import io.bcs.fileserver.domain.model.file.Disposition
import io.bcs.fileserver.domain.model.file.FileStatus
import spock.lang.Specification

abstract class ContentReceiverSpecification extends Specification {
  public static final String MEDIA_TYPE = "application/media-type-xxx"
  public static final String FILE_NAME = "file.txt"
  public static final Long DISTRIBUTIONING_CONTENT_LENGTH = 100L

  protected FileContent createFullSizeContent() {
    return Stub(FileContent) {
      getParts() >> [
        createContentPart(0L, 14L, new InputStreamSource(new ByteArrayInputStream("Hello World!!!".getBytes()), 1000))
      ]
    }
  }

  protected FileContent createSingleRangeContent() {
    return Stub(FileContent) {
      getParts() >> [
        createContentPart(0L, 14L, new InputStreamSource(new ByteArrayInputStream("Hello World!!!".getBytes()), 1000))
      ]
    }
  }

  protected FileContent createMultiRangeContent() {
    return Stub(FileContent) {
      getParts() >> [
        createContentPart(0L, 14L, new InputStreamSource(new ByteArrayInputStream("Hello World!!!".getBytes()), 1000)),
        createContentPart(20L, 15L, new InputStreamSource(new ByteArrayInputStream("Hello People!!!".getBytes()), 1000))
      ]
    }
  }

  protected ContentPart createContentPart(Long offset, Long length, Source<BinaryChunk> source) {
    return Stub(ContentPart) {
      getContentSource() >> source
      getContentFragment() >> Stub(ContentFragment) {
        getOffset() >> offset
        getLength() >> length
      }
    }
  }

  protected FileMetadata createFileMetadata(Disposition disposition, Long totalLength) {
    return Stub(FileMetadata) {
      getFileName() >> FILE_NAME
      getContentDisposition() >> disposition
      getMediaType() >> MEDIA_TYPE
      getTotalLength() >> totalLength
    }
  }
}
