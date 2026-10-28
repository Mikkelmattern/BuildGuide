package mikkelmattern.services;

import mikkelmattern.DAOs.*;
import mikkelmattern.DTOs.GuideDTO;
import mikkelmattern.DTOs.GuideSummaryDTO;
import mikkelmattern.entities.Guide;
import mikkelmattern.entities.Node;
import mikkelmattern.entities.User;
import mikkelmattern.enums.ReadingMode;
import mikkelmattern.exceptions.ApiException;
import mikkelmattern.mappers.GuideMapper;
import mikkelmattern.security.AuthUser;
import mikkelmattern.security.SecurityService;

import java.util.List;
import java.util.Set;

/**
 * Business logic for guides. Calls the DAOs, checks who may change a guide,
 * and converts the results to {@link GuideDTO}s.
 */
public class GuideService {
    private final IGuideDao guideDao;
    private final IUserDao userDao;
    private final SecurityService securityService;
    private final INodeDao nodeDao;
    private final IRevealDao revealDao;

    /**
     * Creates a guide service.
     *
     * @param guideDao        the DAO used to read and write guides
     * @param userDao         the DAO used to look up a guide's author
     * @param securityService the service used to check that a user owns a guide
     */
    public GuideService(IGuideDao guideDao, IUserDao userDao, SecurityService securityService, INodeDao nodeDao, IRevealDao revealDao) {
        this.guideDao = guideDao;
        this.userDao = userDao;
        this.securityService = securityService;
        this.nodeDao = nodeDao;
        this.revealDao = revealDao;
    }

    /**
     * Gets all guides.
     *
     * @return every guide as a DTO
     */
    public List<GuideSummaryDTO> getAllGuides() {
        return guideDao.getAll().stream()
                .map(GuideMapper::toSummaryDTO)
                .toList();
    }

    /**
     * Gets one guide by id, with its node tree. Content is only included for
     * nodes the reader has revealed; other nodes have {@code content} set to {@code null}.
     *
     * @param id           the guide's id
     * @param authUser     the logged-in user, or {@code null} for a guest
     * @param guestReveals the node ids a guest has revealed this session, or {@code null} if none;
     *                     ignored when {@code authUser} is set
     * @param mode         the order to sort nodes in: story order or fastest route
     * @return the guide as a DTO
     * @throws ApiException with status 404 if no guide has that id
     */
    public GuideDTO getGuideById(long id, AuthUser authUser,
                                 Set<Long> guestReveals, ReadingMode mode) {
        Guide guide = findGuideOrThrow(id);

        List<Node> nodesList = nodeDao.findByGuideId(id);
        Set<Long> revealedIds;
        if (authUser != null) {
            revealedIds = revealDao.findRevealedNodeIds(authUser.id(), id);
        } else {
            revealedIds = guestReveals != null ? guestReveals : Set.of();
        }
        return GuideMapper.toDTO(guide, nodesList, revealedIds, mode);
    }

    public List<GuideSummaryDTO> getGuidesByGame(long id) {
        List<Guide> guides = guideDao.getGuidesByGame(id);
        return guides.stream().map(GuideMapper::toSummaryDTO).toList();
    }

    /**
     * Creates a guide written by the logged-in user.
     * <p>
     * Only the title and content are read from {@code guideDTO}. The id, author and timestamps
     * are set by the server, so a client can't pick them.
     *
     * @param guideDTO the new guide's title and content
     * @param authUser the logged-in user, who becomes the author
     * @return the saved guide as a DTO, with its generated id
     * @throws ApiException with status 400 if the title or content is missing,
     *                      or 404 if the logged-in user no longer exists
     */
    public GuideDTO createGuide(GuideDTO guideDTO, AuthUser authUser) {
        if (isBlank(guideDTO.title())) {
            throw new ApiException(400, "No title set");
        }

        // The token can outlive the account, so the author is looked up again
        User author = userDao.findByID(authUser.id());
        if (author == null) {
            throw new ApiException(404, "Could not find the user");
        }

        Guide guide = Guide.builder()
                .title(guideDTO.title())
                .author(author)
                .build();
        return GuideMapper.toDTO(guideDao.create(guide));
    }

    /**
     * Updates a guide's title and content. Only fields that are set in {@code changes} are changed.
     *
     * @param id       the id of the guide to update
     * @param changes  the new values; {@code null} fields are left unchanged
     * @param authUser the logged-in user making the request
     * @return the updated guide as a DTO
     * @throws ApiException with status 404 if no guide has that id,
     *                      or 403 if {@code authUser} is neither the author nor an admin
     */
    public GuideSummaryDTO updateGuide(long id, GuideDTO changes, AuthUser authUser) {
        Guide guide = findGuideOrThrow(id);
        securityService.verifyAccess(guide.getAuthor(), authUser);

        if (changes.title() != null) guide.setTitle(changes.title());

        guideDao.update(guide);
        // Read it again, so the DTO has the updatedAt that JPA set during the update
        return GuideMapper.toSummaryDTO(guideDao.find(id));
    }

    /**
     * Deletes a guide.
     *
     * @param id       the id of the guide to delete
     * @param authUser the logged-in user making the request
     * @return the deleted guide as a DTO
     * @throws ApiException with status 404 if no guide has that id,
     *                      or 403 if {@code authUser} is neither the author nor an admin
     */
    public GuideSummaryDTO deleteGuide(long id, AuthUser authUser) {
        Guide guide = findGuideOrThrow(id);
        securityService.verifyAccess(guide.getAuthor(), authUser);
        guideDao.delete(guide);
        return GuideMapper.toSummaryDTO(guide);
    }

    /**
     * Finds a guide by id.
     *
     * @param id the guide's id
     * @return the guide
     * @throws ApiException with status 404 if no guide has that id
     */
    private Guide findGuideOrThrow(long id) {
        Guide guide = guideDao.find(id);
        if (guide == null) {
            throw new ApiException(404, "Could not find the guide");
        }
        return guide;
    }

    /**
     * Checks whether a string is missing or only whitespace.
     *
     * @param value the string to check
     * @return {@code true} if {@code value} is {@code null} or blank, otherwise {@code false}
     */
    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
