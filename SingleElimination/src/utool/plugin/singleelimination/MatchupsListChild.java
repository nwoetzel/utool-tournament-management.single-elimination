package utool.plugin.singleelimination;

/**
 * Holds a Child of the matchup List
 * Holds a single matchup
 * @author hoguet
 */
public class MatchupsListChild {
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
