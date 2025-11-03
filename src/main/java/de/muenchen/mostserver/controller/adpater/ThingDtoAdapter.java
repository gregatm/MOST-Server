package de.muenchen.mostserver.controller.adpater;

import de.muenchen.mostserver.controller.dto.Thing;
import de.muenchen.mostserver.data.dao.ThingDao;

public class ThingDtoAdapter {
    public static Thing daoToDto(ThingDao thing) {
        return new Thing(thing.getId(), thing.getName());
    }

    public static ThingDao dtoToDao(Thing dto) {
        return ThingDao.builder()
                .id(dto.getId())
                .name(dto.getName())
                .build();
    }
}
