package sigmod14.mem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

//import sigmod14.mem.Database.RelTypes;

public class DataLoader {	
	public static final DataLoader INSTANCE = new DataLoader(Database.INSTANCE);
	
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
		
		System.err.println("LOADED COMMENTS");
		
		// data used for query2
		loadTags();
		loadPersonsInterest();
		
		// data used for query3
		loadPlaces();
		loadPersonsPlace();
		loadOrganizationsPlace();
		loadPersonWorkStudy();
		loadPlaceAtPlace();
		
		// data used for query4
		loadForumTag();
		loadForumMember();
	}
	
	private void loadCommentReplyTo() throws IOException {
		String file = dataDir + commentReplyFName + ".csv";
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line = br.readLine();
		while ((line = br.readLine()) != null) {
			String[] fields = line.split("\\|");

			// (*) reply will already be on DB iff it has a creator who knows
			//     someone. Otherwise it is useless for query1
			Long replyID = Long.parseLong(fields[0]);
			if (!db.commentHasCreator(replyID))
				continue;
			
			Long repliedToID = Long.parseLong(fields[1]);
			if (!db.commentHasCreator(repliedToID)) 
				continue; // see (*) above
			
			db.addReply(replyID, repliedToID);	
		}
		br.close();
	}

	private void loadPersonKnowsPerson() throws FileNotFoundException {
		Scanner scanner = new Scanner(new File(dataDir + personKnows + ".csv"),
				                      charset);
		scanner.nextLine();
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			String[] fields = line.split("\\|");			
			Integer person1ID = Integer.parseInt(fields[0]);
			Integer person2ID = Integer.parseInt(fields[1]);
			db.addKnowsRelationship(person1ID, person2ID);
		}
		scanner.close();
	}


	private void loadPersons() throws FileNotFoundException, ParseException {
		String file = dataDir + personFName + ".csv";
		Scanner scanner = new Scanner(new File(file), charset);
		scanner.nextLine();
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			String[] fields = line.split("\\|");
			Integer id = Integer.parseInt(fields[0]);
			Date birthday = sdf.parse(fields[4] + ":00:00:00");
			db.addPerson(id, birthday);
		}
		scanner.close();
	}

	// this method assumes loadPersonKnowsPerson() has already been called
	private void loadCommentsCreator() throws IOException {
		String file = dataDir + commentCreatorFName + ".csv";
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line = br.readLine();
		while ((line = br.readLine()) != null) {
			String[] fields = line.split("\\|");

			// if creator is not already in DB then there is no point
			// in storing this comment because creator doesn't know anyone
			Integer personID = Integer.parseInt(fields[1]);
			if (!db.containsPerson(personID)) continue;
			
			Long commentID = Long.parseLong(fields[0]);
			db.addCommentCreator(commentID, personID);		
		}
		br.close();
	}
	
	private void loadTags() throws FileNotFoundException {
		File file = new File(dataDir + tagFName + ".csv");
		Scanner scanner = new Scanner(file, charset);
		scanner.nextLine();
		while (scanner.hasNextLine()) {			
			String line = scanner.nextLine();
			String[] fields = line.split("\\|");
			Integer id = Integer.parseInt(fields[0]);
			String name = fields[1];
			db.addTag(id, name);		
		}
		scanner.close();
	}
		
	private void loadPersonsInterest() throws FileNotFoundException {
		String file = dataDir + personTagFName + ".csv";
		Scanner scanner = new Scanner(new File(file), charset);
		scanner.nextLine();
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			String[] fields = line.split("\\|");			
			Integer personID = Integer.parseInt(fields[0]);			
			Integer tagID = Integer.parseInt(fields[1]);
			db.addInterestRelationship(personID, tagID);
		}
		scanner.close();
	}

	private void loadPlaces() throws FileNotFoundException {
		String file = dataDir + placeFName + ".csv";
		Scanner scanner = new Scanner(new File(file), charset);
		scanner.nextLine();
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();			
			String[] fields = line.split("\\|");
			Integer idPlace = Integer.parseInt(fields[0]);
			String name = fields[1];
			db.addPlaceNamed(name, idPlace);
		}
		scanner.close();
	}
	
	private void loadOrganizationsPlace() throws FileNotFoundException {
		File file = new File(dataDir + orgLocFName + ".csv");
		Scanner scanner = new Scanner(file, charset);
		scanner.nextLine();
		while (scanner.hasNextLine()) {			
			String line = scanner.nextLine();
			String[] fields = line.split("\\|");
			Long orgID = Long.parseLong(fields[0]);
			Long placeID = Long.parseLong(fields[1]);
			db.addPlaceOrg(orgID, placeID);
		}
		scanner.close();		
	}
	
	// this method assumes loadPersonKnowsPerson() has already been called
	private void loadPersonsPlace() throws FileNotFoundException {
		String file = dataDir + personLocation + ".csv";
		Scanner scanner = new Scanner(new File(file), charset);
		scanner.nextLine();		
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			String[] fields = line.split("\\|");
			Integer personID = Integer.parseInt(fields[0]);
			if (!db.containsPerson(personID)) 
				continue;	// person must know other persons
			Integer placeID = Integer.parseInt(fields[1]);
			db.addPersonLocatedRelationship(personID, placeID);
			
		}
		scanner.close();
	}
	
	private void loadPersonWorkStudy() throws FileNotFoundException {
		loadPersonsOrg(personWorkFName);
		loadPersonsOrg(personStudyFName);
	}
	
	// this method assumes loadPersonKnowsPerson() and loadOrganizationsPlace()
	// have already been called
	private void loadPersonsOrg(String fileName) throws FileNotFoundException {
		String file = dataDir + fileName + ".csv";
		Scanner scanner = new Scanner(new File(file), charset);
		scanner.nextLine();
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			String[] fields = line.split("\\|");
			
			Integer personID = Integer.parseInt(fields[0]);
			if (!db.containsPerson(personID)) 
				continue;	// person doesn't know other persons
			
			Long orgID = Long.parseLong(fields[1]);
			if (!db.containsPlaceOrg(orgID))
				continue;	// no place for this organization
			int placeID = (int) db.getPlaceOrg(orgID);
			db.addPersonLocatedRelationship(personID, placeID);
		}
		scanner.close();
	}
	
	private void loadPlaceAtPlace() throws FileNotFoundException {
		File file = new File(dataDir + placePlaceFName + ".csv");
		Scanner scanner = new Scanner(file, charset);
		scanner.nextLine();
		while (scanner.hasNextLine()) {			
			String line = scanner.nextLine();
			String[] fields = line.split("\\|");
			Long place1ID = Long.parseLong(fields[0]);
			Long place2ID = Long.parseLong(fields[1]);
			db.addPlaceLocatedRelationship(place1ID, place2ID);
		}
		scanner.close();
	}
	
	private void loadForumTag() throws FileNotFoundException {
		File file = new File(dataDir + forumTagFName + ".csv");
		Scanner scanner = new Scanner(file, charset);
		scanner.nextLine();
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			String[] fields = line.split("\\|");
			Integer forumID = Integer.parseInt(fields[0]);
			Integer tagID = Integer.parseInt(fields[1]);
			db.addForumTagRelationship(forumID, tagID);
		}
		scanner.close();
	}

	// this method assumes loadForumTag() was called before
	private void loadForumMember() throws IOException {
		File file = new File(dataDir + forumMemberFName + ".csv");			
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line = br.readLine();
		while ((line = br.readLine()) != null) {
			String[] fields = line.split("\\|");
			Integer forumID = Integer.parseInt(fields[0]);
			Integer personID = Integer.parseInt(fields[1]);
			db.addInterestAllForumTags(personID, forumID);
		}
		br.close();
	}
}
