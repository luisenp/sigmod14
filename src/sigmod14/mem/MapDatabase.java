package sigmod14.mem;

import java.util.Collection;
import java.util.Date;

import org.mapdb.*;

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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void clearCommentCreator() {
		// TODO Auto-generated method stub

	}

	@Override
	public void addCommentCreator(long commentID, long personID) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean commentHasCreator(long id) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Person getCommentCreator(long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean containsPerson(long id) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Person addPerson(long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Person addPerson(long id, Date birthday) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Person getPerson(long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Long> getAllPersons() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addReply(long replyID, long repliedToID) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addKnowsRelationship(long person1id, long person2id) {
		// TODO Auto-generated method stub

	}

	@Override
	public Tag addTag(long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Tag addTag(long id, String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Tag getTag(long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getTagName(long id) throws NotFoundException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Long> getAllTags() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addInterestRelationship(long personID, long tagID) {
		// TODO Auto-generated method stub

	}

	@Override
	public Node addPlace(long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node addPlaceNamed(String name, long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPlacesNamed(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addPlaceOrg(long orgID, long placeID) {
		// TODO Auto-generated method stub

	}

	@Override
	public long getPlaceOrg(long orgID) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean containsPlaceOrg(long id) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void addPersonLocatedRelationship(long personID, long placeID) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addPlaceLocatedRelationship(long place1id, long place2id) {
		// TODO Auto-generated method stub

	}

	@Override
	public Long getPlaceLocation(long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addForumTagRelationship(long forumID, long tagID) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean containsForum(long forumID) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void addInterestAllForumTags(long personID, long forumID) {
		// TODO Auto-generated method stub

	}

}
