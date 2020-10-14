package org.cloudfoundry.samples.music.repositories.redis;

import org.cloudfoundry.samples.music.domain.Album;
import org.cloudfoundry.samples.music.domain.RandomIdGenerator;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.repository.CrudRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class RedisAlbumRepository implements CrudRepository<Album, String> {
    public static final String ALBUMS_KEY = "albums";

    private final RandomIdGenerator idGenerator;
    private final HashOperations<String, String, Album> hashOps;

    public RedisAlbumRepository(RedisTemplate<String, Album> redisTemplate) {
        this.hashOps = redisTemplate.opsForHash();
        this.idGenerator = new RandomIdGenerator();
    }

    @Override
    public <S extends Album> S save(S album) {
        if (album.getId() == null) {
            album.setId(idGenerator.generateId());
        }

        hashOps.put(ALBUMS_KEY, album.getId(), album);

        return album;
    }

    @Override
    public <S extends Album> Iterable<S> saveAll(Iterable<S> entities) {
        List<S> savedEntities = new ArrayList<>();
        for (S entity : entities) {
            savedEntities.add(save(entity));
        }
        return savedEntities;
    }

    @Override
    public Optional<Album> findById(String s) {
        return Optional.of(hashOps.get(ALBUMS_KEY, s));
    }

    @Override
    public boolean existsById(String s) {
        return hashOps.hasKey(ALBUMS_KEY, s);
    }

    @Override
    public Iterable<Album> findAll() {
        return hashOps.values(ALBUMS_KEY);
    }

    @Override
    public Iterable<Album> findAllById(Iterable<String> strings) {
        return hashOps.multiGet(ALBUMS_KEY, convertIterableToList(strings));
    }

    @Override
    public long count() {
        return hashOps.keys(ALBUMS_KEY).size();
    }

    @Override
    public void deleteById(String s) {
        hashOps.delete(ALBUMS_KEY, s);
    }

    @Override
    public void delete(Album album) {
        hashOps.delete(ALBUMS_KEY, album.getId());
    }

    @Override
    public void deleteAll(Iterable<? extends Album> albums) {
        for (Album album : albums) {
            delete(album);
        }
    }

    @Override
    public void deleteAll() {
        Set<String> ids = hashOps.keys(ALBUMS_KEY);
        for (String id : ids) {
            deleteById(id);
        }
    }

    private <T> List<T> convertIterableToList(Iterable<T> iterable) {
        List<T> list = new ArrayList<>();
        for (T object : iterable) {
            list.add(object);
        }
        return list;
    }
}
