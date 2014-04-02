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

	public KnowsEdge findUndirectedEdge(Node n1, Node n2)
			throws NotFoundException;

	public void addCommentCreator(long commentID, long personID);

	public boolean commentHasCreator(long id);
	
	public Person getCommentCreator(long id);

	public boolean containsPerson(int id);

	public Person addPerson(int id);

	public Person addPerson(int id, Date birthday);

	public Person getPerson(int id);
	
	public Person[] getAllPersons();

	public void addReply(long replyID, long repliedToID);

	public void addKnowsRelationship(int person1ID, int person2ID);

	public Tag addTag(int id);
	
	public Tag addTag(int id, String name);

	public Tag getTag(int id);

	public String getTagName(int id) throws NotFoundException;

	public Collection<Integer> getAllTags();

	public void addInterestRelationship(int personID, int tagID);

	public Node addPlace(int id);

	public Node addPlaceNamed(String name, int id);

	public String getPlacesNamed(String name);

	public void addPlaceOrg(long orgID, long placeID);

	public long getPlaceOrg(long orgID);

	public boolean containsPlaceOrg(long id);

	public void addPersonLocatedRelationship(int personID, int placeID);

	public void addPlaceLocatedRelationship(long place1ID, long place2ID);

	public Long getPlaceLocation(long id);

	public void addForumTagRelationship(int forumID, int tagID);

	public boolean containsForum(int forumID);

	public void addInterestAllForumTags(int personID, int forumID);
}