package sigmod14.mem;

import java.util.Collection;
import java.util.Date;

import sigmod14.mem.graph.KnowsEdge;
import sigmod14.mem.graph.Node;
import sigmod14.mem.graph.NotFoundException;
import sigmod14.mem.graph.Person;
import sigmod14.mem.graph.Tag;



public class MapDatabase implements DB {
	public static final MapDatabase INSTANCE = new MapDatabase();
	
	public DB instance() {
		return INSTANCE;
	}

	@Override
	public KnowsEdge findUndirectedEdge(Node n1, Node n2)
			throws NotFoundException {
		
		return null;
	}

	@Override
	public void addCommentCreator(long commentID, long personID) {
		
		
	}

	@Override
	public boolean commentHasCreator(long id) {
		
		return false;
	}

	@Override
	public Person getCommentCreator(long id) {
		
		return null;
	}

	@Override
	public boolean containsPerson(int id) {
		
		return false;
	}

	@Override
	public Person addPerson(int id) {
		
		return null;
	}

	@Override
	public Person addPerson(int id, Date birthday) {
		
		return null;
	}

	@Override
	public Person getPerson(int id) {
		
		return null;
	}

	@Override
	public Person[] getAllPersons() {
		
		return null;
	}

	@Override
	public void addReply(long replyID, long repliedToID) {
		
		
	}

	@Override
	public void addKnowsRelationship(int person1id, int person2id) {
		
		
	}

	@Override
	public Tag addTag(int id) {
		
		return null;
	}

	@Override
	public Tag addTag(int id, String name) {
		
		return null;
	}

	@Override
	public Tag getTag(int id) {
		
		return null;
	}

	@Override
	public String getTagName(int id) throws NotFoundException {
		
		return null;
	}

	@Override
	public Collection<Integer> getAllTags() {
		
		return null;
	}

	@Override
	public void addInterestRelationship(int personID, int tagID) {
		
		
	}

	@Override
	public Node addPlace(int id) {
		
		return null;
	}

	@Override
	public Node addPlaceNamed(String name, int id) {
		
		return null;
	}

	@Override
	public String getPlacesNamed(String name) {
		
		return null;
	}

	@Override
	public void addPlaceOrg(long orgID, long placeID) {
		
		
	}

	@Override
	public long getPlaceOrg(long orgID) {
		
		return 0;
	}

	@Override
	public boolean containsPlaceOrg(long id) {
		
		return false;
	}

	@Override
	public void addPersonLocatedRelationship(int personID, int placeID) {
		
		
	}

	@Override
	public void addPlaceLocatedRelationship(long place1id, long place2id) {
		
		
	}

	@Override
	public Long getPlaceLocation(long id) {
		
		return null;
	}

	@Override
	public void addForumTagRelationship(int forumID, int tagID) {
		
		
	}

	@Override
	public boolean containsForum(int forumID) {
		
		return false;
	}

	@Override
	public void addInterestAllForumTags(int personID, int forumID) {
		
		
	}
}
