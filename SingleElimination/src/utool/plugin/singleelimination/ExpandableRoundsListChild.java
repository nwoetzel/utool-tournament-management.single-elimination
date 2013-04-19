package utool.plugin.singleelimination;

/**
 * Represents a child element of ExpandableList of rounds
 * 
 * @author hoguet
 * 10-14-12
 *
 */
public class ExpandableRoundsListChild {
	
		/**
		 * Name of the child element (text displayed on it)
		 */
	    private String name;
	    
	    /**
	     * Tag of the child element (not really used currently)
	     */
	    private String tag;
	    
	    /**
	     * The matchup displayed by this child; stored so that scores can be saved, etc.
	     */
	    private Matchup matchup;
	     
	    /**
	     * @return name
	     */
	    public String getName() {
	        return name;
	    }
	    
	    /**
	     * @param Name name of child
	     */
	    public void setName(String Name) {
	        this.name = Name;
	    }
	    
	    /**
	     * @return tag
	     */
	    public String getTag() {
	        return tag;
	    }
	    
	    /**
	     * @param Tag tag of child
	     */
	    public void setTag(String Tag) {
	        this.tag = Tag;
	    }
	    
	    /**
	     * @return matchup
	     */
	    public Matchup getMatchup(){
	    	return matchup;
	    }
	    
	    /**
	     * @param m matchup associated with child
	     */
	    public void setMatchup(Matchup m){
	    	matchup = m;
	    }

}
