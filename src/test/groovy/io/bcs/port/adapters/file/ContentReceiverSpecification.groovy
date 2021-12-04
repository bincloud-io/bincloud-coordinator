package io.bcs.port.adapters.file

import static io.bcs.domain.model.file.FileMetadata.Disposition.ATTACHMENT
import static io.bcs.domain.model.file.FileMetadata.Disposition.INLINE

import io.bce.interaction.streaming.Source
import io.bce.interaction.streaming.binary.BinaryChunk
import io.bce.interaction.streaming.binary.InputStreamSource
import io.bcs.domain.model.file.ContentFragment
import io.bcs.domain.model.file.FileContent
import io.bcs.domain.model.file.FileMetadata
import io.bcs.domain.model.file.FileStatus
import io.bcs.domain.model.file.FileContent.ContentPart
import io.bcs.domain.model.file.FileMetadata.Disposition
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
