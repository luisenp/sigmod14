package sigmod14.mem;

import java.util.Collection;
import java.util.Date;

import sigmod14.mem.graph.KnowsEdge;
import sigmod14.mem.graph.Node;
import sigmod14.mem.graph.NotFoundException;
import sigmod14.mem.graph.Person;
import sigmod14.mem.graph.Tag;

public interface DB {
	public DB instance();
	// this method is used by DataLoader.loadCommentReplyTo() 
	// to quickly find whether two persons know e/o
	/* (non-Javadoc)
	 * @see sigmod14.mem.DB#findUndirectedEdge(sigmod14.mem.graph.Node, sigmod14.mem.graph.Node)
	 */
	public KnowsEdge findUndirectedEdge(Node n1, Node n2)
			throws NotFoundException;

	/* (non-Javadoc)
	 * @see sigmod14.mem.DB#clearCommentCreator()
	 */
	public void clearCommentCreator();

	/* (non-Javadoc)
	 * @see sigmod14.mem.DB#addCommentCreator(long, long)
	 */
	public void addCommentCreator(long commentID, long personID);

	/* (non-Javadoc)
	 * @see sigmod14.mem.DB#commentHasCreator(long)
	 */
	public boolean commentHasCreator(long id);

	/* (non-Javadoc)
	 * @see sigmod14.mem.DB#getCommentCreator(long)
	 */
	public Person getCommentCreator(long id);

	/* (non-Javadoc)
	 * @see sigmod14.mem.DB#containsPerson(long)
	 */
	public boolean containsPerson(long id);

	/* (non-Javadoc)
	 * @see sigmod14.mem.DB#addPerson(long)
	 */
	public Person addPerson(long id);

	/* (non-Javadoc)
	 * @see sigmod14.mem.DB#addPerson(long, java.util.Date)
	 */
	public Person addPerson(long id, Date birthday);

	/* (non-Javadoc)
	 * @see sigmod14.mem.DB#getPerson(long)
	 */
	public Person getPerson(long id);

	/* (non-Javadoc)
	 * @see sigmod14.mem.DB#getAllPersons()
	 */
	public Collection<Long> getAllPersons();

	/* (non-Javadoc)
	 * @see sigmod14.mem.DB#addReply(long, long)
	 */
	public void addReply(long replyID, long repliedToID);

	/* (non-Javadoc)
	 * @see sigmod14.mem.DB#addKnowsRelationship(long, long)
	 */
	public void addKnowsRelationship(long person1ID, long person2ID);

	/* (non-Javadoc)
	 * @see sigmod14.mem.DB#addTag(long)
	 */
	public Tag addTag(long id);

	/* (non-Javadoc)
	 * @see sigmod14.mem.DB#addTag(long, java.lang.String)
	 */
	public Tag addTag(long id, String name);

	/* (non-Javadoc)
	 * @see sigmod14.mem.DB#getTag(long)
	 */
	public Tag getTag(long id);

	/* (non-Javadoc)
	 * @see sigmod14.mem.DB#getTagName(long)
	 */
	public String getTagName(long id) throws NotFoundException;

	/* (non-Javadoc)
	 * @see sigmod14.mem.DB#getAllTags()
	 */
	public Collection<Long> getAllTags();

	/* (non-Javadoc)
	 * @see sigmod14.mem.DB#addInterestRelationship(long, long)
	 */
	public void addInterestRelationship(long personID, long tagID);

	/* (non-Javadoc)
	 * @see sigmod14.mem.DB#addPlace(long)
	 */
	public Node addPlace(long id);

	/* (non-Javadoc)
	 * @see sigmod14.mem.DB#addPlaceNamed(java.lang.String, long)
	 */
	public Node addPlaceNamed(String name, long id);

	/* (non-Javadoc)
	 * @see sigmod14.mem.DB#getPlacesNamed(java.lang.String)
	 */
	public String getPlacesNamed(String name);

	/* (non-Javadoc)
	 * @see sigmod14.mem.DB#addPlaceOrg(long, long)
	 */
	public void addPlaceOrg(long orgID, long placeID);

	/* (non-Javadoc)
	 * @see sigmod14.mem.DB#getPlaceOrg(long)
	 */
	public long getPlaceOrg(long orgID);

	/* (non-Javadoc)
	 * @see sigmod14.mem.DB#containsPlaceOrg(long)
	 */
	public boolean containsPlaceOrg(long id);

	/* (non-Javadoc)
	 * @see sigmod14.mem.DB#addPersonLocatedRelationship(long, long)
	 */
	public void addPersonLocatedRelationship(long personID, long placeID);

	/* (non-Javadoc)
	 * @see sigmod14.mem.DB#addPlaceLocatedRelationship(long, long)
	 */
	public void addPlaceLocatedRelationship(long place1ID, long place2ID);

	/* (non-Javadoc)
	 * @see sigmod14.mem.DB#getPlaceLocation(long)
	 */
	public Long getPlaceLocation(long id);

	/* (non-Javadoc)
	 * @see sigmod14.mem.DB#addForumTagRelationship(long, long)
	 */
	public void addForumTagRelationship(long forumID, long tagID);

	/* (non-Javadoc)
	 * @see sigmod14.mem.DB#containsForum(long)
	 */
	public boolean containsForum(long forumID);

	/* (non-Javadoc)
	 * @see sigmod14.mem.DB#addInterestAllForumTags(long, long)
	 */
	public void addInterestAllForumTags(long personID, long forumID);

}