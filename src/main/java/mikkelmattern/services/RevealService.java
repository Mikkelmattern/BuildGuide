package mikkelmattern.services;

import mikkelmattern.DAOs.IGuideDao;
import mikkelmattern.DAOs.INodeDao;
import mikkelmattern.DAOs.IRevealDao;
import mikkelmattern.DAOs.IUserDao;
import mikkelmattern.entities.Node;
import mikkelmattern.entities.Reveal;
import mikkelmattern.entities.User;
import mikkelmattern.exceptions.ApiException;
import mikkelmattern.security.AuthUser;
import mikkelmattern.utils.TreeUtil;

import java.util.List;
import java.util.Set;

/**
 * Reveals and hides guide content for a reader.
 * <p>
 * Logged-in users' reveals are saved in the database. Guests' reveals are kept in a set
 * the controller stores in their session; this service only changes that set.
 */
public class RevealService {
    private final INodeDao nodeDao;
    private final IGuideDao guideDao;
    private final IRevealDao revealDao;
    private final IUserDao userDao;

    /**
     * Creates a reveal service.
     *
     * @param nodeDao   the DAO used to load a guide's nodes
     * @param guideDao  the DAO used to check that a guide exists
     * @param revealDao the DAO used to save and delete logged-in users' reveals
     * @param userDao   the DAO used to look up the logged-in user
     */
    public RevealService(INodeDao nodeDao, IGuideDao guideDao, IRevealDao revealDao, IUserDao userDao) {
        this.nodeDao = nodeDao;
        this.guideDao = guideDao;
        this.revealDao = revealDao;
        this.userDao = userDao;
    }

    /**
     * Reveals a node and every node under it.
     *
     * @param nodeId       the id of the node to reveal
     * @param authUser     the logged-in user, or {@code null} for a guest
     * @param guestReveals the guest's revealed node ids; changed in place, ignored for logged-in users
     * @throws ApiException with status 404 if the node or user doesn't exist
     */
    public void revealNode(long nodeId, AuthUser authUser, Set<Long> guestReveals) {
        Node node = findNodeOrThrow(nodeId);
        Long guideId = node.getGuide().getId();
        List<Node> guideNodes = nodeDao.findByGuideId(guideId);
        Set<Long> subtreeIds = TreeUtil.collectSubtreeIds(nodeId, guideNodes);

        if (authUser == null) {
            guestReveals.addAll(subtreeIds);
            return;
        }

        User user = userDao.findByID(authUser.id());
        if (user == null) {
            throw new ApiException(404, "Could not find the user");
        }

        // Skip nodes already revealed, or the unique constraint on (user, node) fails the save
        Set<Long> alreadyRevealed = revealDao.findRevealedNodeIds(authUser.id(), guideId);
        List<Reveal> newReveals = guideNodes.stream()
                .filter(n -> subtreeIds.contains(n.getId()))
                .filter(n -> !alreadyRevealed.contains(n.getId()))
                .map(n -> new Reveal(user, n))
                .toList();

        revealDao.saveAll(newReveals);
    }

    /**
     * Hides a node and every node under it again.
     *
     * @param nodeId       the id of the node to hide
     * @param authUser     the logged-in user, or {@code null} for a guest
     * @param guestReveals the guest's revealed node ids; changed in place, ignored for logged-in users
     * @throws ApiException with status 404 if the node doesn't exist
     */
    public void hideNode(long nodeId, AuthUser authUser, Set<Long> guestReveals) {
        Node node = findNodeOrThrow(nodeId);
        List<Node> guideNodes = nodeDao.findByGuideId(node.getGuide().getId());
        Set<Long> subtreeIds = TreeUtil.collectSubtreeIds(nodeId, guideNodes);

        if (authUser == null) {
            guestReveals.removeAll(subtreeIds);
        } else {
            revealDao.deleteByUserAndNodes(authUser.id(), subtreeIds);
        }
    }

    /**
     * Hides every node in a guide, resetting the reader's progress.
     *
     * @param guideId      the id of the guide to reset
     * @param authUser     the logged-in user, or {@code null} for a guest
     * @param guestReveals the guest's revealed node ids; changed in place, ignored for logged-in users
     * @throws ApiException with status 404 if the guide doesn't exist
     */
    public void hideGuide(long guideId, AuthUser authUser, Set<Long> guestReveals) {
        if (guideDao.find(guideId) == null) {
            throw new ApiException(404, "Could not find the guide");
        }

        if (authUser == null) {
            // The session set holds ids from all guides, so only remove this guide's nodes
            nodeDao.findByGuideId(guideId).forEach(n -> guestReveals.remove(n.getId()));
        } else {
            revealDao.deleteByUserAndGuide(authUser.id(), guideId);
        }
    }

    private Node findNodeOrThrow(long id) {
        Node node = nodeDao.findById(id);
        if (node == null) {
            throw new ApiException(404, "Could not find the node");
        }
        return node;
    }
}