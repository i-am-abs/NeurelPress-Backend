package com.neurelpress.blogs.service.impl;

import com.neurelpress.blogs.dto.response.TagResponse;
import com.neurelpress.blogs.mapper.TagMapper;
import com.neurelpress.blogs.repository.TagRepository;
import com.neurelpress.blogs.service.TagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;
    private final TagMapper tagMapper;

    @Override
    @Cacheable(cacheNames = "allTags")
    public List<TagResponse> getAllTags() {
        log.info("TagServiceImpl.getAllTags");
        return tagRepository.findAll()
                .stream()
                .map(tagMapper::toResponse)
                .toList();
    }

    @Override
    @Cacheable(cacheNames = "topTags", key = "#limit")
    public List<TagResponse> getTopTags(int limit) {
        log.info("TagServiceImpl.getTopTags: limit={}", limit);
        return tagRepository.findTopTags(PageRequest.of(0, limit))
                .stream()
                .map(tagMapper::toResponse)
                .toList();
    }

    @Override
    public Optional<TagResponse> getBySlug(String slug) {
        return tagRepository.findBySlug(slug).map(tagMapper::toResponse);
    }

    @Override
    public Optional<TagResponse> getByNameIgnoreCase(String name) {
        return tagRepository.findByNameIgnoreCase(name).map(tagMapper::toResponse);
    }
}
