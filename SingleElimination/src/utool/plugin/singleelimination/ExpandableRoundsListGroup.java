package utool.plugin.singleelimination;

import java.util.ArrayList;

/**
 * Represents group in the ExpandableList of rounds
 * @author hoguet
 *
 */
public class ExpandableRoundsListGroup {

	/**
	 * Name of the group (text that is displayed)
	 */
	private String name;
	
	/**
	 * Children that are under this group
	 */
	private ArrayList<ExpandableRoundsListChild> items;

	/**
	 * @return name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @param name name of group
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * @return items
	 */
	public ArrayList<ExpandableRoundsListChild> getItems() {
		return items;
	}
	
	/**
	 * @param Items the children of this group
	 */
	public void setItems(ArrayList<ExpandableRoundsListChild> Items) {
		this.items = Items;
	}

}
