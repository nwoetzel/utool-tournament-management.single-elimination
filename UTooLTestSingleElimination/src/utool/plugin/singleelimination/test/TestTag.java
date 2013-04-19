package utool.plugin.singleelimination.test;

import utool.plugin.singleelimination.communications.Tag;
import junit.framework.TestCase;

/**
 * Tests for the tag class
 * @author waltzm
 * @version 10/25/2012
 */
public class TestTag extends TestCase{


	//This method is invoked before every test
	@Override
	protected void setUp() throws Exception
	{
		super.setUp();

	}

	/**
	 * Tests that the two constructors work as intended in tag
	 * @since 10/13/2012
	 */
	public void testConstructors() 
	{		
		//attr and msg
		String attr = "Attribute";
		String msg = "Message";
		
		Tag t = new Tag(attr,msg);
		
		assertNotNull(t);
		assertEquals(t.getAttr(),"Attribute");
		assertEquals(t.getMsg(),"Message");
		
		//default constructor
		Tag d = new Tag();
		
		assertNotNull(d);
		assertEquals(d.getAttr(),"");
		assertEquals(d.getMsg(),"");
	}
	
	/**
	 * Tests that the getters and setters function as intended
	 * @since 10/13/2012
	 */
	public void testGettersSetters() 
	{	
		Tag t = new Tag();
		
		//set attr
		t.setAttr("dog");
		
		//get attr
		assertEquals("dog",t.getAttr());
		
		//set msg
		t.setMsg("Hippo");
		
		//get msg
		assertEquals(t.getMsg(),"Hippo");
			
	}
	
	
	/**
	 * Tests that the toString prints out the attribute then : then the message
	 * @since 10/13/2012
	 */
	public void testToString() 
	{		
		Tag t = new Tag("sommmeeeeText","someText");
		assertEquals(t.toString(),"sommmeeeeText: someText");
	}
	
	
	
}
