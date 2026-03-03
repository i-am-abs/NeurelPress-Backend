package com.neurelpress.blogs.mapper;

import com.neurelpress.blogs.dto.response.TagResponse;
import com.neurelpress.blogs.dao.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class TagMapper {

    public TagResponse toResponse(Tag tag) {
        log.info("Mapping tag: {}", tag);
        return new TagResponse(
                tag.getId(),
                tag.getName(),
                tag.getSlug(),
                tag.getDescription(),
                tag.getArticleCount()
        );
    }

    public Set<TagResponse> toResponseSet(Set<Tag> tags) {
        log.info("Mapping tags: {}", tags);
        return tags.stream()
                .map(this::toResponse)
                .collect(Collectors.toSet());
    }
}
