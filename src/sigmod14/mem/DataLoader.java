package sigmod14.mem;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

//import sigmod14.mem.Database.RelTypes;

public class DataLoader {	
	public static final DataLoader INSTANCE = new DataLoader(Database.INSTANCE);
	
	private int pagesize = 1024 * 8;
	
	private Database db;
	
	private String charset = "UTF-8";
	public final SimpleDateFormat sdf =
			new SimpleDateFormat("yyyy-MM-dd:HH:mm:SS");
	
	// file names 
	public static final String personFName = "person";
	public static final String tagFName = "tag";
	public static final String commentCreatorFName = "comment_hasCreator_person";
	public static final String commentReplyFName = "comment_replyOf_comment";
	public static final String personKnows = "person_knows_person";
	public static final String personTagFName = "person_hasInterest_tag";
	public static final String personLocation = "person_isLocatedIn_place";
	public static final String placeFName = "place";
	public static final String placePlaceFName = "place_isPartOf_place";
	public static final String personStudyFName = "person_studyAt_organisation";
	public static final String personWorkFName = "person_workAt_organisation";
	public static final String orgLocFName = "organisation_isLocatedIn_place";
	public static final String forumTagFName = "forum_hasTag_tag";
	public static final String forumMemberFName = "forum_hasMember_person";

	private String dataDir;
	
	private DataLoader(Database db) {
		this.db = db;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}
	
	public void setDataDirectory(String dir) {
		this.dataDir = dir + "/";
	}

	public void loadData() throws IOException, ParseException {
		// data used to create person graph 
		loadPersons();
		loadPersonKnowsPerson();
		
		// data used for query1
		loadCommentsCreator();
		loadCommentReplyTo();
		// no need to store comments anymore
		db.clearCommentCreator(); 
		
		System.out.println("LOADED COMMENTS");
		
		// data used for query2
		loadTags();
		loadPersonsInterest();
		
		// data used for query3
		loadPlaces();
		loadPersonsPlace();
		loadOrganizationsPlace();
		loadPersonWorkStudy();
		loadPlaceAtPlace();
//		orgPlace.clear();	// TODO fix this
		
		// data used for query4
		loadForumTag();
		loadForumMember();
		
		System.out.println();
	}
	
	private void loadCommentReplyTo() throws IOException {
		String filename = dataDir + commentReplyFName + ".csv";
		FastFileIterator reader = new FastFileIterator(filename);
		
		while(reader.hasNext()) {
			long replyID = reader.next();
			long repliedToID = reader.next();
			if (!db.commentHasCreator(repliedToID)) 
				continue;
			db.addReply(replyID, repliedToID);
		}
		reader.close();
	}

	private void loadPersonKnowsPerson() throws IOException {
		FastFileIterator reader = new FastFileIterator(dataDir + personKnows + ".csv");
		
		while(reader.hasNext()) {
			Long person1ID = reader.next();
			Long person2ID = reader.next();
			db.addKnowsRelationship(person1ID, person2ID);
		}
		reader.close();
	}


//	private void loadPersons() throws ParseException, IOException {
//		FastFileIterator reader = new FastFileIterator(dataDir + personFName + ".csv");
//		
//		while (reader.hasNext()) {
//			Long id = reader.next();
//			for(int i = 0; i < 2 ; i++)
//				reader.next();
//			Date birthday = sdf.parse(reader.next() + ":00:00:00");
//			db.addPerson(id, birthday);
//		}
//		reader.close();
//	}
	private void loadPersons() throws FileNotFoundException, ParseException {
		String file = dataDir + personFName + ".csv";
		Scanner scanner = new Scanner(new File(file), charset);
		scanner.nextLine();
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			String[] fields = line.split("\\|");
			Long id = Long.parseLong(fields[0]);
			Date birthday = sdf.parse(fields[4] + ":00:00:00");
			db.addPerson(id, birthday);
		}
		scanner.close();
	}

	// this method assumes loadPersonKnowsPerson() has already been called
	private void loadCommentsCreator() throws IOException {
		FastFileIterator reader = new FastFileIterator(dataDir + commentCreatorFName + ".csv");
		while (reader.hasNext()) {
			

			// if creator is not already in DB then there is no point
			// in storing this comment because creator doesn't know anyone
			Long commentID = reader.next();
			Long personID = reader.next();
			
			if (!db.containsPerson(personID)) continue;
			
			db.addCommentCreator(commentID, personID);		
		}
		reader.close();
	}
	
	private void loadTags() throws FileNotFoundException {
		File file = new File(dataDir + tagFName + ".csv");
		Scanner scanner = new Scanner(file, charset);
		scanner.nextLine();
		while (scanner.hasNextLine()) {			
			String line = scanner.nextLine();
			String[] fields = line.split("\\|");
			Long id = Long.parseLong(fields[0]);
			String name = fields[1];
			db.addTag(id, name);		
		}
		scanner.close();
	}
		
	private void loadPersonsInterest() throws IOException {
		FastFileIterator reader = new FastFileIterator(dataDir + personTagFName + ".csv");
		
		while (reader.hasNext()) {
			Long personID = reader.next();			
			Long tagID = reader.next();
			db.addInterestRelationship(personID, tagID);
		}
		reader.close();
	}

	private void loadPlaces() throws FileNotFoundException {
		String file = dataDir + placeFName + ".csv";
		Scanner scanner = new Scanner(new File(file), charset);
		scanner.nextLine();
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();			
			String[] fields = line.split("\\|");
			Long idPlace = Long.parseLong(fields[0]);
			String name = fields[1];
			db.addPlaceNamed(name, idPlace);
		}
		scanner.close();
	}
	
	private void loadOrganizationsPlace() throws IOException {
		FastFileIterator reader = new FastFileIterator(dataDir + orgLocFName + ".csv");
		
		while (reader.hasNext()) {			
			
			Long orgID = reader.next();
			Long placeID = reader.next();
			db.addPlaceOrg(orgID, placeID);
		}
		reader.close();		
	}
	
	// this method assumes loadPersonKnowsPerson() has already been called
	private void loadPersonsPlace() throws IOException {
		FastFileIterator reader = new FastFileIterator(dataDir + personLocation + ".csv");
		
		while (reader.hasNext()) {
			Long personID = reader.next();
			Long placeID = reader.next();
			if (!db.containsPerson(personID)) 
				continue;	// person must know other persons
			db.addPersonLocatedRelationship(personID, placeID);
			
		}
		reader.close();
	}
	
	private void loadPersonWorkStudy() throws IOException {
		loadPersonsOrg(personWorkFName);
		loadPersonsOrg(personStudyFName);
	}
	
	// this method assumes loadPersonKnowsPerson() and loadOrganizationsPlace()
	// have already been called
	private void loadPersonsOrg(String fileName) throws IOException {
		FastFileIterator reader = new FastFileIterator(dataDir + fileName + ".csv");
		
		while (reader.hasNext()) {
			Long personID = reader.next();
			Long orgID = reader.next();
			if (!db.containsPerson(personID)) 
				continue;	// person doesn't know other persons
			
			if (!db.containsPlaceOrg(orgID))
				continue;	// no place for this organization
			Long placeID = db.getPlaceOrg(orgID);
			db.addPersonLocatedRelationship(personID, placeID);
		}
		reader.close();
	}
	
	private void loadPlaceAtPlace() throws IOException {
		FastFileIterator reader = new FastFileIterator(dataDir + placePlaceFName + ".csv");
		while (reader.hasNext()) {			
			Long place1ID = reader.next();
			Long place2ID = reader.next();
			db.addPlaceLocatedRelationship(place1ID, place2ID);
		}
		reader.close();
	}
	
	private void loadForumTag() throws IOException {
		FastFileIterator reader = new FastFileIterator(dataDir + forumTagFName + ".csv");
		while (reader.hasNext()) {
			Long forumID = reader.next();
			Long tagID = reader.next();
			db.addForumTagRelationship(forumID, tagID);
		}
		reader.close();
	}

//	// this method assumes loadForumTag() was called before
//	private void loadForumMember() throws IOException {
//		FastFileIterator reader = new FastFileIterator(dataDir + forumMemberFName + ".csv");			
//
//		while (reader.hasNext()) {
//			
//			Long forumID = reader.next();
//			Long personID = reader.next();
//			db.addInterestAllForumTags(personID, forumID);
//		}
//		reader.close();
//	}
	private void loadForumMember() throws IOException {
		File file = new File(dataDir + forumMemberFName + ".csv");			
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line = br.readLine();
		while ((line = br.readLine()) != null) {
			String[] fields = line.split("\\|");
			Long forumID = Long.parseLong(fields[0]);
			Long personID = Long.parseLong(fields[1]);
			db.addInterestAllForumTags(personID, forumID);
		}
		br.close();
	}
}
