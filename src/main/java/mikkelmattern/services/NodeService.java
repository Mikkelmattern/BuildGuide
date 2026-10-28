package mikkelmattern.services;

import mikkelmattern.DAOs.IGuideDao;
import mikkelmattern.DAOs.INodeDao;
import mikkelmattern.DTOs.NodeDTO;
import mikkelmattern.entities.Guide;
import mikkelmattern.entities.Node;
import mikkelmattern.exceptions.ApiException;
import mikkelmattern.mappers.NodeMapper;
import mikkelmattern.records.NodeRequest;
import mikkelmattern.security.AuthUser;
import mikkelmattern.security.SecurityService;
import mikkelmattern.utils.TreeUtil;

import java.util.*;

/**
 * Business logic for nodes. Checks that the user owns the guide
 * before nodes are added, changed or deleted.
 */
public class NodeService {
    private final INodeDao nodeDao;
    private final IGuideDao guideDao;
    private final SecurityService securityService;

    /**
     * Creates a node service.
     *
     * @param nodeDao         the DAO used to read and write nodes
     * @param guideDao        the DAO used to look up the guide a node belongs to
     * @param securityService the service used to check that a user owns a guide
     */
    public NodeService(INodeDao nodeDao, IGuideDao guideDao, SecurityService securityService) {
        this.nodeDao = nodeDao;
        this.guideDao = guideDao;
        this.securityService = securityService;
    }

    /**
     * Adds a node to a guide, either at the top level or under a parent node.
     *
     * @param guideId     the id of the guide to add the node to
     * @param nodeRequest the new node's title, content, parent and ordering numbers
     * @param authUser    the logged-in user, who must be the guide's author or an admin
     * @return the created node as a DTO, with its generated id
     * @throws ApiException with status 400 if the title is missing or the parent is in another guide,
     *                      403 if the user doesn't own the guide,
     *                      or 404 if the guide or parent doesn't exist
     */
    public NodeDTO createNode(long guideId, NodeRequest nodeRequest, AuthUser authUser) {
        if (nodeRequest.title() == null || nodeRequest.title().isBlank()) {
            throw new ApiException(400, "No title set");
        }

        Guide guide = findGuideOrThrow(guideId);
        securityService.verifyAccess(guide.getAuthor(), authUser);

        Node parent = null;
        if (nodeRequest.parentId() != null) {
            parent = findNodeOrThrow(nodeRequest.parentId());
            if (!parent.getGuide().getId().equals(guideId)) {
                throw new ApiException(400, "Parent node belongs to another guide");
            }
        }

        Node node = new Node();
        node.setTitle(nodeRequest.title());
        node.setContent(nodeRequest.content());
        node.setGuide(guide);
        node.setParent(parent);
        node.setPriorityNumber(nodeRequest.priorityNumber() != null ? nodeRequest.priorityNumber() : 0);
        node.setStoryOrderNumber(nodeRequest.storyOrderNumber() != null ? nodeRequest.storyOrderNumber() : 0);

        return NodeMapper.toAuthorDTO(nodeDao.create(node));
    }

    /**
     * Updates a node's title, content, ordering numbers and parent.
     * Only fields that are set in {@code changes} are changed.
     * Moving a node to a new parent also moves all nodes under it.
     *
     * @param id       the id of the node to update
     * @param changes  the new values; {@code null} fields are left unchanged
     * @param authUser the logged-in user making the request
     * @return the updated node as a DTO
     * @throws ApiException with status 404 if the node or new parent doesn't exist,
     *                      403 if {@code authUser} is neither the guide's author nor an admin,
     *                      or 400 if the title is blank or the new parent is in another guide or under the node itself
     */
    public NodeDTO updateNode(long id, NodeRequest changes, AuthUser authUser) {
        Node node = findNodeOrThrow(id);
        Guide guide = findGuideOrThrow(node.getGuide().getId());
        securityService.verifyAccess(guide.getAuthor(), authUser);

        if (changes.title() != null) {
            if (changes.title().isBlank()) {
                throw new ApiException(400, "Title can't be blank");
            }
            node.setTitle(changes.title());
        }
        if (changes.content() != null) node.setContent(changes.content());
        if (changes.priorityNumber() != null) node.setPriorityNumber(changes.priorityNumber());
        if (changes.storyOrderNumber() != null) node.setStoryOrderNumber(changes.storyOrderNumber());
        if (Boolean.TRUE.equals(changes.moveToTop())) {
            node.setParent(null);
        } else if (changes.parentId() != null) {
            Node newParent = findNodeOrThrow(changes.parentId());
            if (!newParent.getGuide().getId().equals(guide.getId())) {
                throw new ApiException(400, "Parent node belongs to another guide");
            }
            List<Node> guideNodes = nodeDao.findByGuideId(guide.getId());
            // A node can't be moved under itself or one of its own children, since that would make a loop
            if (TreeUtil.collectSubtreeIds(id, guideNodes).contains(newParent.getId())) {
                throw new ApiException(400, "A node can't be moved under itself");
            }
            node.setParent(newParent);
        }

        return NodeMapper.toAuthorDTO(nodeDao.update(node));
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
     * Finds a node by id.
     *
     * @param id the node's id
     * @return the node
     * @throws ApiException with status 404 if no node has that id
     */
    private Node findNodeOrThrow(long id) {
        Node node = nodeDao.findById(id);
        if (node == null) {
            throw new ApiException(404, "Could not find the node");
        }
        return node;
    }

    /**
     * Deletes a node and all nodes under it.
     *
     * @param id       the id of the node to delete
     * @param authUser the logged-in user making the request
     * @return the deleted node as a DTO
     * @throws ApiException with status 404 if no node has that id,
     *                      or 403 if {@code authUser} is neither the guide's author nor an admin
     */
    public NodeDTO deleteNode(long id, AuthUser authUser) {
        Node node = findNodeOrThrow(id);
        Guide guide = findGuideOrThrow(node.getGuide().getId());
        securityService.verifyAccess(guide.getAuthor(), authUser);
        List<Node> guideNodes = nodeDao.findByGuideId(guide.getId());
        nodeDao.deleteAll(TreeUtil.collectSubtreeIds(id, guideNodes));
        return NodeMapper.toAuthorDTO(node);
    }
}