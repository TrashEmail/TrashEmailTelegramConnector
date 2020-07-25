package io.github.trashemailtelegramconnector.repository;

import io.github.trashemailtelegramconnector.models.FreeUserId;
import org.springframework.data.repository.CrudRepository;

public interface FreeUserIdRepository
        extends CrudRepository<FreeUserId, Integer> {
    public FreeUserId findTopByOrderByIdAsc();
}
