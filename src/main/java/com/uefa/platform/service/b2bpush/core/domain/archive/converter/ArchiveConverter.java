package com.uefa.platform.service.b2bpush.core.domain.archive.converter;

import com.uefa.platform.dto.common.archive.Archive;
import com.uefa.platform.dto.common.archive.Archive.Provider;
import com.uefa.platform.dto.common.archive.Archive.Status;
import com.uefa.platform.service.b2bpush.core.domain.archive.data.entity.MessageArchiveEntity;

import java.util.List;

public class ArchiveConverter {

    private ArchiveConverter() {
        // no instantiation
    }

    public static List<Archive> convertArchiveEntities(List<MessageArchiveEntity> listArchives) {
        return listArchives.stream()
                .map(ArchiveConverter::toDto)
                .toList();
    }

    private static Archive toDto(MessageArchiveEntity archiveEntity) {
        return new Archive(
                archiveEntity.getId(),
                archiveEntity.getSentTimestamp(),
                Provider.valueOf(archiveEntity.getProvider().name()),
                archiveEntity.getSentContent(),
                archiveEntity.getTags(),
                Status.valueOf(archiveEntity.getStatus().name())
        );
    }

}
