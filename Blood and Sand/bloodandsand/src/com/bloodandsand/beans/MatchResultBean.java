/**
 *  Created by Andy Hayward
 *  Jan 4, 2013
 */
package com.bloodandsand.beans;


import java.util.Date;
import java.util.Formatter;
import java.util.Random;
import java.util.logging.Logger;


import com.bloodandsand.utilities.CoreBean;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.Text;

public class MatchResultBean extends CoreBean implements java.io.Serializable {
	/**
	 * 
	 */
	protected static final Logger log = Logger.getLogger(MatchResultBean.class.getName());
	private boolean logEnabled = true;
	
	private static final long serialVersionUID = 5687618169542410632L;
	private GladiatorDataBean challenger;
	private GladiatorDataBean incumbant;
	
	private String winner;
	
	private String incumbantName;
	private String challengerName;
	private Key incumbantKey;
	private Key challengerKey;
	
	private String challengerPossessive;
	private String incumbantPossessive;
	
	private String challengerWeapon;
	private String incumbantWeapon;
	
	private String challengerSubject = "";
	private String incumbantSubject = "";
	
	private String challengerObject = "";
	private String incumbantObject = "";
	
	private String challengerWeapon2 = "";
	private String incumbantWeapon2 = "";
	
	private String challengerStatus = "OK";//OK, tiring, exhausted
	private String incumbantStatus = "OK";
	
	private boolean challengerInjured = false;
	private boolean incumbantInjured = false;
	
	private String incumbantStats;
	private String challengerStats;
	
	
	//match statisitic variables
	private int incumbantHits;
	private int incumbantMisses;
	private int incumbantCriticals;
	private int incumbantDodges;
	private int incumbantBlocks;
	private int incumbantCritResists;
	private int incumbantDeathResists;
	private int incumbantContinues;
	private int incumbantRests;
	private int incumbantRipostes;
	private int incumbantAvoids;
	
	private int challengerHits;
	private int challengerMisses;
	private int challengerCriticals;
	private int challengerDodges;
	private int challengerBlocks;
	private int challengerCritResists;
	private int challengerDeathResists;
	private int challengerContinues;
	private int challengerRests;
	private int challengerRipostes;
	private int challengerAvoids;
	
	private Date matchDate;	
	private long round;
	
	private String fightDescription;
	
	private Entity thisEntity = new Entity(matchResultEntity);
	private Key resultKey;
	
	
	Random rng = new Random();
	
	
	private String[] openings = {"The bout between %1$s and %2$s is about to begin. The gladiators enter the arena from opposite sides. %1$s, the challenger, is wielding %11$s and %2$s wields %12$s. The fighters meet face to face in the middle, and as the announcer asks the crowd for silence for the start of the match ",
			"The crowd goes quiet as the announcer calls for their attention. %1$s and %2$s enter the arena and the crowd cheers, drowning out the announcer again. %1$s is carrying %11$s and %2$s has chosen %12$s as %6$s weapon. As the announcer describes the rules of the match, ",
			"As the loser from the last fight is dragged out of the arena to the boos and laughter of the crowd, the announcer introduces the next fight. %2$s walks into the arena, carrying %12$s. From the other side of the arena, %1$s appears, wielding %11$s. The crowd cheers loudly at their arrival. As they approach the middle of the arena, "};
	
	private String[] pauseDescription = {"The fighters step away from each other for a moment, breathing heavily and assessing each other closely. ",
			"%1$s steps backward, moving away from %2$s for a few moments. The two fighters pause and scrutinize each other, %1$s swinging %5$s %9$s and smiling. ",
			"%2$s steps backward, moving away from %1$s for a few moments. The two fighters pause to catch their breath, %2$s spinning %6$s %10$s casually. ",
			"The crowd is following the fighters' every move, loudly cheering each swing and booing each pause. ",
			"The fighters pause for a moment, and pace back and forth analyzing each other's moves. "};
	
	//////////////////////////////////////////////////////////////////////////////////////
	
	private String[] attackDescription = {"%1$s presses forward, bringing %3$s %5$s to bear. ", "%1$s attacks quickly, leaping forward. ",
			"%1$s swings %3$s %5$s in a broad arc. ", "%1$s presses the attack, thrusting and jabbing. ", "%1$s brings %3$s %5$s down in a devastating blow. ", 
			"%1$s attacks repeatedly, pushing the other fighter backwards. ", "%1$s feints with a jab, then brings %3$s %5$s around in a powerful swing. ", 
			"%1$s is gaining some confidence, and makes 3 quick attacks in succession. ", "%1$s attacks. ", "%1$s attacks cleverly with %3$s %5$s. ", 
			"%1$s swings %3$s %5$s. ", "Suddenly %1$s jabs with %3$s %5$s. "};
	
	private String[] dodgeDescription = {"%1$s steps neatly to the side, easily avoiding the attack. ", "%1$s leaps out of the way. ", "%1$s ducks under the attack, ",
		"%1$s slides under the blow. ", "%1$s ducks under the blow. ", "%1$s just arches %3$s body out of the way. ", "%1$s steps just out of range, avoiding the attack. "	
	};
	
	private String[] blockDescription = {"%1$s blocks the attack with %3$s %5$s", "%1$s uses %3$s %5$s to brush the attack aside. ", "%1$s uses %3$s %5$s to knock the attack away. ",
			"%1$s uses %3$s %5$s to redirect the attack away. ", "%1$s deftly redirects the attack with %3$s %5$s. "};
	
	private String[] riposteAvoidDescription = {"%1$s narrowly blocks the counterattack with %3$s %5$s", "%1$s uses %3$s %5$s to brush the riposte aside. ", "%1$s uses %3$s %5$s to knock the attack away. ",
			"%1$s uses %3$s %5$s to redirect the riposte. ", "%1$s just barely redirects the attack with  %3$s %5$s. ", 
			"%1$s steps to the side, only just avoiding the counterstrike. ", "%1$s leaps out of the way. ", "%1$s ducks under the attack. ",
			"%1$s slides under the counter blow. ", "%1$s ducks under the blow. ", "%1$s just arches %3$s body out of the way. ", "%1$s jumps just out of range, avoiding the attack. "};
	
	private String[] missDescription = {"%1$s fumbles with %3$s %5$s nearly dropping it, and the crowd laughs and boos. ", "%1$s swings wildly, nowhere near %3$s target. ",   
			"%1$s swings wildly, %3$s %5$s hitting nothing but sand. ", "%1$s attacks wildly, missing completely. ", "%1$s lunges forward wildly, completely missing the target. ",
			"%1$s swings his weapon back and forth, trying to push the other fighter back. "};
	
	private String[] riposteDescription = {"%1$s quickly counterattacks. ", "%1$s sees an opening and leaps forward. ", "%1$s sees an opening and whips %3$s %5$s forward. ",
			"%1$s slips %3$s %5$s around his counterpart's weapon for a sudden counterattack. ", "%1$s catches his opponent off balance with a swift riposte. "};
	
	private String[] concedeDescription = {"%1$s cannot continue. %2$s drops to one knee and raises %3$s hand in surrender. ", "Gasping and grimacing, %1$s drops %3$s weapon and admits defeat. ",
			"%1$s falls to the sand, gasping for air and calling for the medic. %1$s concedes the fight. "};
	
	private String[] normalHitDescription = {"%1$s gets a lucky strike, just barely hitting ", "%1$s's %5$s just glances off", "%1$s grazes ", "%1$s slips %3$s %5$s weapon in close and knicks ",
			"%1$s's %5$s marks ", "The crowd roars as %1$s's %5$s draws blood from ", "The crowd cheers as %1$s slips %3$s %5$s weapon in close and knicks ", 
			"%1$s's %5$s leaves a mark on"};
	
	private String[] tiringDescription = {"%1$s looks to be tiring a little, and is breathing heavily. ", "It looks like the fight is wearing %1$s down, and %2$s is starting to tire. ",
			"Sweat begins to trickle down %1$s's face. ", "%1$s pauses to wipe the sweat from %3$s eyes. ", "%1$s  is slowing down a little, trying to conserve energy. "};
	
	private String[] exhaustedDescription = {"%1$s is breathing very heavily and looks very tired. ", "Sweat pours down %1$s's face and back, and %2$s looks exhausted. However, %2$s forces %4$sself to continue.  "
			};
	
	private String[] restDescription = {"%1$s is exhausted and tries to step away to rest. ", "Dragging %3$s weapon behind %4$s, %1$s tries to catch his breath. ", 
			"%1$s almost collapses from exhaustion and can't lift %3$s %5$s. ", "%1$s bends over, %3$s hands on %3$s knees, gasping for breath. "};
	
	private String[] injuredDescription = {"%1$s is bleeding heavily, leaving a trail of blood across the sand. ", "Blood flows from %1$s's wounds, and %3$s chest is covered in blood. ",
			"Despite %3$s many wounds, %1$s struggles to carry on, grimacing in pain. ", "A lesser fighter would have give up if they had %1$s's wounds, but %2$s struggles on valiantly. " 
	};
	
	private String[] criticalHitDescription = {"The spectators gasp as blood sprays from ", "Someone in the audience screams as %1$s's weapon slams hard into ", 
			"The crowd goes silent, and then erupts with a roar as %1$s's %5$s smashes into ", "There is a gush of blood as %1$s's %5$s hits ", "%1$s yells in triumph as %3$s %5$s lands squarely on ",
			"The crowd gasps as blood sprays from "};
	
	private String[] criticalResistDescription = {"%2$s staggers from the blow, but regains %3$s footing and lifts %3$s weapon to continue. ", 
			"%2$s turns %3$s head to the side, spits out some blood, then turns to %3$s enemy and advances, ignoring the pain", "%2$s shrugs off the wound, determined to continue. ", 
			"The wound would have killed most fighters, but %1$s carries on, grim determination in %3$s eyes. ", "%2$s screams in pain, staggering back a few steps. Then %2$s glares at %3$s opponent and charges forward. "};
	
	private String[] deathDescription = {"%1$s clutches %3$s wound in agony and slowly drops to the sand, where %2$s dies of %3$s wounds. ", "%1$s instantly dies from %3$s wound, collapsing in a pile in the sand. ",
			"The crowd goes silent, as %1$s drops in a heap on the ground, then erupts into cheers of victory!"};
	
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////
	private String[] personalityHonourable = {"%1$s holds one hand up and says 'This one's for you Father' and nods %3$s head solemnly. ",
			"%1$s asks the crowd for silence, and then announces that %2$s dedicates this fight to %3$s mother. ",
			"%1$s faces %3$s opponent and bows respectifully ", "%1$s bends down on one knee and prays to %3$s god for fortune and mercy ", 
			"%1$s salutes %3$s opponent and then salutes the crowd. "};
	
	private String[] personalityCrazy = {"%1$s licks %3$s %5$s and grins maniacally at it. ", "%1$s seems to be having an argument with %3$s %5$s. ",
			"%1$s is sitting in the sand making a sand castle. ", "%1$s is laying on %3$s back, making a sand angel. ",
			"%1$s announces to the crowd that %2$s dedicates this fight to %3$s pet goat Nibbles. "};
	
	private String[] personalityStupid = {"%1$s holds %3$s %5$s to the sky and says 'Go for the eyes Boo!' ",
			"%1$s is fascinated by something %2$s just pulled out of %3$s ear. ", "%1$s is picking %3$s teeth. ", 
			"%1$s is chewing on something %2$s saved from last night's dinner. ", "%1$s is writing %3$s name in the sand. "	};
	
	private String[] personalitySerious = {"%1$s paces back and forth slowly, glaring at %3$s opponent like a tiger in a cage next to a fat, juicy goat. ",
			"%1$s is warming up by doing some pushups. ", "%1$s is doing some stretches and warming up for the fight. ", 
			"%1$s looks straight at %3$s opponent with no expression or emotion. ", "%1$s is testing the balance of his weapon with a few trial swings and thrusts. "};
	
	private String[] personalityVain = {"%1$s faces the crowd and bows ceremoniously and blows kisses to %3$s fans. ", 
			"%1$s waves at the audience and encourages them to cheer %4$s on. ", 
			"%1$s performs for the crowds by swinging %3$s %5$s in some clever flourishes and then throwing it up in the air and catching it. ",
			"%1$s flexes %3$s muscles for the crowd. "};
	
	private String[] personalityClumsy = {"%1$s flourishes %3$s %5$s by swinging it around and accidentally throws it into the crowd. ",
			"%1$s seems unsure which end of %3$s %5$s is the dangerous end. ", "%1$s jogs on the spot to warm up, but trips on something and falls flat, spitting up sand. "};
	
	private String[] personalityCoward = {"%1$s looks like %2$s wishes %2$s were somewhere else. Anywhere else. ",
			"%1$s stares at the crowd, %3$s face white and %3$s jaw hanging open. ",
			"%1$s is frantically trying to find the arena exit. ", "%1$s is looking for something to hide behind. "};
	
	public MatchResultBean(Entity e){
		thisEntity = e;
		setUpBean();
	}
	
	public MatchResultBean (GladiatorDataBean challenger, GladiatorDataBean incumbant, String challengerWeapon, String incumbantWeapon){
		
		this.challenger = challenger;
		this.incumbant = incumbant;
		
		this.challengerKey = challenger.getDataStoreKey();
		this.incumbantKey = incumbant.getDataStoreKey();
		
		this.challengerName = challenger.getCapitalizedName();
		this.incumbantName = incumbant.getCapitalizedName();
		
		this.challengerWeapon = challengerWeapon;
		this.incumbantWeapon = incumbantWeapon;
		
		setUpChallengerFormatter();
		setUpIncumbantFormatter();
		
		fightDescription = ""; 
		incumbantStats = "";
		challengerStats = "";
		
		matchDate = new Date();
		
		round = 0;	
		winner = "";

		setUpEntity();
		resultKey = thisEntity.getKey();

	}
	//these two functions set up the various variables for formatting strings in the fight description	
	private void setUpIncumbantFormatter() {
		if (incumbant.getGender().equalsIgnoreCase("M")){
			incumbantPossessive = "his";
			incumbantSubject = "he";
			incumbantObject = "him";			
		} else {
			incumbantPossessive = "her";
			incumbantSubject = "she";
			incumbantObject = "her";
		}
		
		if (incumbantWeapon.equals("daggers")){
			incumbantWeapon2 = "daggers";
		} else {
			incumbantWeapon2 = "a " + incumbantWeapon;
		}		
	}

	private void setUpChallengerFormatter() {
		if (challenger.getGender().equalsIgnoreCase("M")){
			challengerPossessive = "his";
			challengerSubject = "he";
			challengerObject = "him";			
		} else {
			challengerPossessive = "her";
			challengerSubject = "she";
			challengerObject = "her";
		}
		
		if (challengerWeapon.equals("daggers")){
			challengerWeapon2 = "daggers";
		} else {
			challengerWeapon2 = "a " + challengerWeapon;
		}
		
	}
	//used for formatting general strings such as the opening statement for the fight
	private String writer(String in){
		Formatter formatter = new Formatter();
		StringBuilder sb = new StringBuilder();
		
		//sb.append(in);
		 sb.append(formatter.format(in, challengerName, incumbantName, challengerSubject, incumbantSubject, challengerPossessive, incumbantPossessive, 
				 						challengerObject, incumbantObject, challengerWeapon, incumbantWeapon, challengerWeapon2, incumbantWeapon2));
		 formatter.close();
		 return sb.toString();
	}
	
	//used for formatting strings about the challenger
	private String writerChallenger(String in){
		Formatter formatter = new Formatter();
		StringBuilder sb = new StringBuilder();	
		log.info("String coming in: " + in);
		sb.append(formatter.format(in, challengerName, challengerSubject, challengerPossessive,  
					challengerObject, challengerWeapon, challengerWeapon2));
		formatter.close();
		return sb.toString();
	}
	//used for formatting strings about the incumbant
	private String writerIncumbant(String in){
		Formatter formatter = new Formatter();
		StringBuilder sb = new StringBuilder();		
		log.info("String coming in: " + in);

		sb.append(formatter.format(in, incumbantName, incumbantSubject, incumbantPossessive, 
					incumbantObject, incumbantWeapon, incumbantWeapon2));
		formatter.close();
		return sb.toString();
	}
	
	public void describeMatchStart() {
		
		fightDescription = writer(openings[rng.nextInt(openings.length)]) + getOpening(challenger) + getOpening(incumbant) + 
				"The announcer signals the fighters to start. The fight has begun! \n\n";	
		
	}
	
	private String getOpening(GladiatorDataBean glad){
		String opening = "";
		if (glad.getPersonality().equals("Cowardly")){
															//"Honourable", "Crazy", "Stupid", "Serious", "Vain", "Clumsy", "Cowardly"
			opening = personalityCoward[rng.nextInt(personalityCoward.length)];
		}
		if (glad.getPersonality().equals("Crazy")){			
			opening = personalityCrazy[rng.nextInt(personalityCrazy.length)];
		}
		if (glad.getPersonality().equals("Stupid")){			
			opening = personalityStupid[rng.nextInt(personalityStupid.length)];
		}
		if (glad.getPersonality().equals("Serious")){			
			opening = personalitySerious[rng.nextInt(personalitySerious.length)];
		}
		if (glad.getPersonality().equals("Honourable")){			
			opening = personalityHonourable[rng.nextInt(personalityHonourable.length)];
		}
		if (glad.getPersonality().equals("Vain")){			
			opening = personalityVain[rng.nextInt(personalityVain.length)];
		}
		if (glad.getPersonality().equals("Clumsy")){			
			opening = personalityClumsy[rng.nextInt(personalityClumsy.length)];
		}		
		log.info(opening);
		if (glad.key == challenger.key){
			return writerChallenger(opening);
		} else {
			return writerIncumbant(opening);
		}
	}

	private void setUpBean(){
		challengerKey = (Key) this.thisEntity.getProperty("challengerKey");
		incumbantKey = (Key) this.thisEntity.getProperty("incumbantKey");
		challengerName = (String) this.thisEntity.getProperty("challengerName");
		incumbantName = (String) this.thisEntity.getProperty("incumbantName");
		matchDate = (Date) this.thisEntity.getProperty("matchDate");
		fightDescription = ((Text) this.thisEntity.getProperty("fightDescription")).getValue();
		round = (Long) this.thisEntity.getProperty("totalRounds");	
		winner = (String) this.thisEntity.getProperty("winner");
		if (this.thisEntity.hasProperty("incumbantStats")){
			incumbantStats = ((Text) this.thisEntity.getProperty("incumbantStats")).getValue();
		}
		if (this.thisEntity.hasProperty("challengerStats")){
			challengerStats = ((Text) this.thisEntity.getProperty("challengerStats")).getValue();
		}
		
		resultKey = this.thisEntity.getKey();
	}
	
	private void setUpEntity(){
		//ensure all properties are set up
		
		this.thisEntity.setProperty("challengerKey", challengerKey);
		this.thisEntity.setProperty("incumbantKey", incumbantKey);
		this.thisEntity.setProperty("challengerName", challengerName);
		this.thisEntity.setProperty("incumbantName", incumbantName);
		this.thisEntity.setProperty("matchDate", matchDate);
		this.thisEntity.setProperty("fightDescription", new Text(fightDescription));
		this.thisEntity.setProperty("totalRounds", round);	
		this.thisEntity.setProperty("winner", winner);
		this.thisEntity.setProperty("incumbantStats", new Text(incumbantStats));
		this.thisEntity.setProperty("challengerStats", new Text(challengerStats));
	}
	
	public String getResultKey(){
		return KeyFactory.keyToString(this.resultKey);
	}
	
	public String getIncumbantStats(){
		return this.incumbantStats;
	}
	
	public String getChallengerStats(){
		return this.challengerStats;
	}
	
	public GladiatorDataBean getChallenger() {
		return challenger;
	}

	public void setChallenger(GladiatorDataBean challenger) {
		this.challenger = challenger;
	}

	public GladiatorDataBean getIncumbant() {
		return incumbant;
	}

	public void setIncumbant(GladiatorDataBean incumbant) {
		this.incumbant = incumbant;
	}

	public String getIncumbantName() {
		return capitalizeWord(incumbantName);
	}

	public void setIncumbantName(String incumbantName) {
		this.incumbantName = incumbantName;
		thisEntity.setProperty("incumbantName", incumbantName);
	}

	public String getChallengerName() {
		return capitalizeWord(challengerName);
	}

	public void setChallengerName(String challengerName) {
		this.challengerName = challengerName;
		thisEntity.setProperty("challengerName", challengerName);
	}

	public Key getIncumbantKey() {
		return incumbantKey;
	}

	public void setIncumbantKey(Key incumbantKey) {
		this.incumbantKey = incumbantKey;
		thisEntity.setProperty("incumbantKey", incumbantKey);
	}

	public Key getChallengerKey() {
		return challengerKey;
	}

	public void setChallengerKey(Key challengerKey) {
		this.challengerKey = challengerKey;
		thisEntity.setProperty("challengerKey", challengerKey);
	}

	public String getChallengerPossessive() {
		return challengerPossessive;
	}

	public void setChallengerPossessive(String challengerPossessive) {
		this.challengerPossessive = challengerPossessive;
	}

	public String getIncumbantPossessive() {
		return incumbantPossessive;
	}

	public void setIncumbantPossessive(String incumbantPossessive) {
		this.incumbantPossessive = incumbantPossessive;
	}

	public Date getMatchDate() {
		return matchDate;
	}

	public void setMatchDate(Date matchDate) {
		this.matchDate = matchDate;
		thisEntity.setProperty("matchDate", matchDate);
	}

	
	public long getRound() {
		return round;
	}

	public void setRound(long round) {
		this.round = round;
		thisEntity.setProperty("totalRounds", round);
	}

	public String getFightDescription() {
		return fightDescription;
	}

	public void setFightDescription(String fightDescription) {
		this.fightDescription = fightDescription;
		thisEntity.setProperty("fightDescription", new Text(fightDescription));
	}

	public void initializeNewRound (long round){
		this.round = round;
		if ((round%4)== 0){
			fightDescription += "\n\n";
			fightDescription += writer(pauseDescription[rng.nextInt(pauseDescription.length)]);
			if (challengerInjured){
				fightDescription += writerChallenger(injuredDescription[rng.nextInt(injuredDescription.length)]);
			}
			if (challengerStatus.equals("Tired")){
				fightDescription += writerChallenger(tiringDescription[rng.nextInt(tiringDescription.length)]);
			}
			
			if (challengerStatus.equals("Exhausted")){
				fightDescription += writerChallenger(exhaustedDescription[rng.nextInt(exhaustedDescription.length)]);
			}
			
			if (incumbantInjured){
				fightDescription += writerIncumbant(injuredDescription[rng.nextInt(injuredDescription.length)]);
			}
			if (incumbantStatus.equals("Tired")){
				fightDescription += writerIncumbant(tiringDescription[rng.nextInt(tiringDescription.length)]);
			}
			
			if (incumbantStatus.equals("Exhausted")){
				fightDescription += writerIncumbant(exhaustedDescription[rng.nextInt(exhaustedDescription.length)]);
			}
			fightDescription += "\n\n";
		}
		if (TESTTOGGLE){fightDescription += "\nStart of round " + round +"\n";}
	}	


	public void saveNewResults(TournamentDataBean tourney) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();	
		thisEntity = new Entity(matchResultEntity, tourney.getDataStoreKey());
		
		setUpEntity();
		
		Transaction txn = datastore.beginTransaction();

		try {			
			datastore.put(thisEntity);
			txn.commit();
		} finally  {
			if (txn.isActive()) {
		        txn.rollback();
		        log.warning("Save match result transaction failed: rolled back");
		    }
		}		
	}

	public void setWinner(String winner) {
		this.winner = winner;
		thisEntity.setProperty("winner", winner);		
	}
	
	public String getWinner(){
		return capitalizeWord(this.winner);
	}

	public void recordHit(String fighterName) {
		
		if (fighterName.equalsIgnoreCase(challenger.name)){
			challengerHits +=1;
			fightDescription += writerChallenger(attackDescription[rng.nextInt(attackDescription.length)]);
		} else {
			incumbantHits += 1;
			fightDescription += writerIncumbant(attackDescription[rng.nextInt(attackDescription.length)]);
		}
		
	}

	public void recordBlock(String fighterName) {
		if (fighterName.equalsIgnoreCase(challenger.name)){
			challengerBlocks +=1;
			fightDescription += writerChallenger(blockDescription[rng.nextInt(blockDescription.length)]);
		} else {
			incumbantBlocks += 1;
			fightDescription += writerIncumbant(blockDescription[rng.nextInt(blockDescription.length)]);
		}		
	}

	public void recordDodge(String fighterName) {
		if (fighterName.equalsIgnoreCase(challenger.name)){
			challengerDodges +=1;
			fightDescription += writerChallenger(dodgeDescription[rng.nextInt(dodgeDescription.length)]);
		} else {
			incumbantDodges += 1;
			fightDescription += writerIncumbant(dodgeDescription[rng.nextInt(dodgeDescription.length)]);
		}
		
	}

	public void recordMiss(String fighterName) {
		if (fighterName.equalsIgnoreCase(challenger.name)){
			challengerMisses +=1;
			fightDescription += writerChallenger(missDescription[rng.nextInt(missDescription.length)]);
		} else {
			incumbantMisses += 1;
			fightDescription += writerIncumbant(missDescription[rng.nextInt(missDescription.length)]);
		}		
	}

	public void recordRest(String fighterName) {
		
		if (fighterName.equalsIgnoreCase(challenger.name)){
			challengerRests +=1;
			fightDescription += writerChallenger(restDescription[rng.nextInt(restDescription.length)]);
		} else {
			incumbantRests += 1;
			fightDescription += writerIncumbant(restDescription[rng.nextInt(restDescription.length)]);
		}
	}

	public void recordRiposte(String fighterName) {
		if (fighterName.equalsIgnoreCase(challenger.name)){
			challengerRipostes +=1;
			fightDescription += writerChallenger(riposteDescription[rng.nextInt(riposteDescription.length)]);
		} else {
			incumbantRipostes += 1;
			fightDescription += writerIncumbant(riposteDescription[rng.nextInt(riposteDescription.length)]);
		}		
	}

	public void recordAvoidRiposte(String fighterName) {
		if (fighterName.equalsIgnoreCase(challenger.name)){			
			challengerAvoids +=1;
			fightDescription += writerChallenger(riposteAvoidDescription[rng.nextInt(riposteAvoidDescription.length)]);
		} else {
			incumbantAvoids += 1;
			fightDescription += writerIncumbant(riposteAvoidDescription[rng.nextInt(riposteAvoidDescription.length)]);
		}		
	}

	public void recordCriticalHit(String fighterName) {
		if (fighterName.equalsIgnoreCase(challenger.name)){
			challengerCriticals +=1;
			fightDescription += writerChallenger(criticalHitDescription[rng.nextInt(criticalHitDescription.length)]);
		} else {
			incumbantCriticals += 1;
			fightDescription += writerIncumbant(criticalHitDescription[rng.nextInt(criticalHitDescription.length)]);
		}
		
	}

	public void recordResistedCritical(String fighterName) {
		// TODO Auto-generated method stub
		fightDescription += fighterName + " shrugs off the hit. \n";
		if (fighterName.equalsIgnoreCase(challenger.name)){
			challengerCritResists +=1;
			fightDescription += writerChallenger(criticalResistDescription[rng.nextInt(criticalResistDescription.length)]);
		} else {
			incumbantCritResists += 1;
			fightDescription += writerIncumbant(criticalResistDescription[rng.nextInt(criticalResistDescription.length)]);
		}		
	}

	public void recordNormalHit(String fighterName) {
		// TODO Auto-generated method stub
		if (fighterName.equalsIgnoreCase(challenger.name)){
			
			fightDescription += writerChallenger(normalHitDescription[rng.nextInt(normalHitDescription.length)]);
		} else {
			
			fightDescription += writerIncumbant(normalHitDescription[rng.nextInt(normalHitDescription.length)]);
		}				
	}

	public void writeSummaryStats() {
		// TODO Auto-generated method stub
		challengerStats = "Summary stats:\n" + incumbantName + ":\n";
		challengerStats += "Good attacks: " + incumbantHits + "\n";
		challengerStats += "Misses: " + incumbantMisses + "\n";
		challengerStats += "Critical hits: " + incumbantCriticals + "\n";
		challengerStats += "Dodges: " + incumbantDodges + "\n";
		challengerStats += "Blocks: " + incumbantBlocks + "\n";
		challengerStats += "Criticals resisted: " + incumbantCritResists + "\n";
		challengerStats += "Deathblows resisted: " + incumbantDeathResists + "\n";
		challengerStats += "Continued despite wounds and exhaustion: " + incumbantContinues + " times." + "\n";
		challengerStats += "Rested: " + incumbantRests + "times." + "\n";
		challengerStats += "Ripostes: " + incumbantRipostes + "\n";
		challengerStats += "Ripostes avoided: " + incumbantAvoids + "\n";
		
		incumbantStats += "Summary stats:\n" + challengerName + ":\n";
		incumbantStats += "Good attacks: " + challengerHits + "\n";
		incumbantStats += "Misses: " + challengerMisses + "\n";
		incumbantStats += "Critical hits: " + challengerCriticals + "\n";
		incumbantStats += "Dodges: " + challengerDodges + "\n";
		incumbantStats += "Blocks: " + challengerBlocks + "\n";
		incumbantStats += "Criticals resisted: " + challengerCritResists + "\n";
		incumbantStats += "Deathblows resisted: " + challengerDeathResists + "\n";
		incumbantStats += "Continued despite wounds and exhaustion: " + challengerContinues + " times." + "\n";
		incumbantStats += "Rested: " + challengerRests + "times." + "\n";
		incumbantStats += "Ripostes: " + challengerRipostes + "\n";
		incumbantStats += "Ripostes avoided: " + challengerAvoids + "\n";
		
		thisEntity.setProperty("incumbantStats", new Text(incumbantStats));
		thisEntity.setProperty("challengerStats", new Text(challengerStats));
		thisEntity.setProperty("fightDescription", new Text(fightDescription));
	}

	public void recordNonCriticalInjury(String fighterName, String location) {
		fightDescription += fighterName + "'s " + location.toLowerCase() + ". ";
		
	}

	public void recordCriticalInjury(String fighterName, String location) {
		fightDescription += fighterName + "'s " + location.toLowerCase() + ". ";
		if (fighterName.equalsIgnoreCase(challenger.name)){
			challengerInjured = true;
		} else {
			incumbantInjured=true;
		}		
	}

	public void recordDeath(String fighterName) {
		if (fighterName.equalsIgnoreCase(challenger.name)){
			fightDescription += writerChallenger(deathDescription[rng.nextInt(deathDescription.length)]);
		} else {
			fightDescription += writerIncumbant(deathDescription[rng.nextInt(deathDescription.length)]);
		}
	}

	public void recordExhausted(String fighterName) {
		fightDescription += fighterName + " looks exhausted, and can barely stand.";
		
	}

	public void recordTired(String fighterName) {
		if (fighterName.equalsIgnoreCase(challenger.name)){
			challengerStatus = "Exhausted";
		} else {
			incumbantStatus="Exhausted";

		}
	}

	public void recordTiring(String fighterName) {
		if (fighterName.equalsIgnoreCase(challenger.name)){
			challengerStatus = "Tired";
		} else {
			incumbantStatus="Tired";
		}
	}

	public void recordConcede(String fighterName) {
		if (fighterName.equalsIgnoreCase(challenger.name)){
			fightDescription += writerChallenger(concedeDescription[rng.nextInt(concedeDescription.length)]);
		} else {
			fightDescription += writerIncumbant(concedeDescription[rng.nextInt(concedeDescription.length)]);

		}		
	}
}
