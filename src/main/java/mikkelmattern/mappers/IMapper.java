package mikkelmattern.mappers;

import java.util.List;

/**
 * Converts between an entity and its DTOs.
 * <p>
 * The two {@code toEntity} methods have different names because generic parameters
 * are erased at compile time, so {@code toEntity(DTOIn)} and {@code toEntity(DTOOut)}
 * would clash.
 *
 * @param <T>      the entity type
 * @param <DTOIn>  the DTO the client sends in, e.g. when creating
 * @param <DTOOut> the DTO the API sends out
 */
public interface IMapper<T, DTOIn, DTOOut> {

    /**
     * Converts an incoming DTO to an entity.
     *
     * @param dto the DTO to convert
     * @return the entity
     */
    public abstract T toEntity(DTOIn dto);

    /**
     * Converts an outgoing DTO back to an entity.
     *
     * @param dto the DTO to convert
     * @return the entity
     */
    public abstract T toEntityFromOut(DTOOut dto);

    /**
     * Converts a list of incoming DTOs to entities.
     *
     * @param dtos the DTOs to convert
     * @return the entities, in the same order
     */
    public abstract List<T> toEntityList(List<DTOIn> dtos);

    /**
     * Converts an entity to an outgoing DTO.
     *
     * @param entity the entity to convert
     * @return the DTO
     */
    public abstract DTOOut toDTO(T entity);

    /**
     * Converts a list of entities to outgoing DTOs.
     *
     * @param entities the entities to convert
     * @return the DTOs, in the same order
     */
    public abstract List<DTOOut> toDTOList(List<T> entities);
}
