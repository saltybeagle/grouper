/*
	$Header: /home/hagleyj/i2mi/signet/src/edu/internet2/middleware/signet/util/TreeXmlLoader.java,v 1.13 2007-10-24 21:48:10 ddonn Exp $
TreeXmlLoader.java
Created on Feb 22, 2005

Copyright 2006 Internet2, Stanford University

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package edu.internet2.middleware.signet.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.apache.commons.collections.set.UnmodifiableSet;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import edu.internet2.middleware.signet.SignetFactory;
import edu.internet2.middleware.signet.Status;
import edu.internet2.middleware.signet.tree.Tree;
import edu.internet2.middleware.signet.tree.TreeAdapter;
import edu.internet2.middleware.signet.tree.TreeNode;

public class TreeXmlLoader
{
	protected JComponent parent = null;

  private static SessionFactory sessionFactory;
  private        Session        session;
  private        Connection     conn;
  
  private        int            treesAdded = 0;
  private        int            treeNodesAdded = 0;
  private        int            treeNodeRelationshipsAdded = 0;

  private String[] deletionTableNames
    = new String[]
        {
          "signet_treeNodeRelationship",
          "signet_treeNode",
          "signet_tree"
        };
  
  private String insertTreeSQL
    = "insert into signet_tree"
      + "(treeID,"
      + " name,"
      + " adapterClass,"
      + " modifyDatetime)"
      + " values (?, ?, ?, ?)";
  
  private String insertTreeNodeSQL
    = "insert into signet_treeNode"
      + "(treeID,"
      + " nodeID,"
      + " nodeType,"
      + " status,"
      + " name,"
      + " modifyDatetime)"
      + "values (?, ?, ?, ?, ?, ?)";

  private String insertTreeNodeRelationshipSQL
    = "insert into signet_treeNodeRelationship"
      + "(treeID,"
      + " nodeID,"
      + " parentNodeID)"
      + "values (?, ?, ?)";
  
  private String ELEMENTNAME_TREE         = "Tree";
  private String ELEMENTNAME_ID           = "Id";
  private String ELEMENTNAME_NAME         = "Name";
  private String ELEMENTNAME_ORGANIZATION = "Organization";
  private String ELEMENTNAME_TYPE         = "Type";
  
  private String ADAPTERCLASSNAME
    = "edu.internet2.middleware.signet.TreeAdapterImpl";

  
    
  /**
   * Opens a connection to the database for subsequent use in loading
   * and deleting Trees.
   *
   */
  public TreeXmlLoader()
  {
    try
    {
      Configuration cfg = new Configuration();

      // Read the "hibernate.cfg.xml" file.
      cfg.configure();
      sessionFactory = cfg.buildSessionFactory();

      this.session = sessionFactory.openSession();
      this.conn = session.connection();
    }
    catch (HibernateException he)
    {
      throw new RuntimeException(he);
    }
  }
  
  
  /**
   * Creates a new Tree.
   * This method updates the database, but does not commit any transaction.
   * 
   * @param id
   * @param name
   * @param adapterClassName
   * @return A new Tree
   * @throws SQLException
   */
  public Tree newTree
    (String id,
     String name,
     String adapterClassName)
  throws
    SQLException
  {
    PreparedStatement pStmt = null;
    try {
      pStmt = this.conn.prepareStatement(insertTreeSQL);
      pStmt.setString(1, id);
      pStmt.setString(2, name);
      pStmt.setString(3, adapterClassName);
      pStmt.setDate(4, new Date(Calendar.getInstance().getTimeInMillis()));
      pStmt.executeUpdate();
    }
    finally {
      if (pStmt != null) {
        pStmt.close();
      }
    }

    
    Tree tree = new TreeImpl(id, name, adapterClassName);

    this.treesAdded++;
    return tree;
  }
  
  /**
   * Creates a new TreeNode, and stores that value in the database, along with
   * any node-relationship information.
   * This method updates the database, but does not commit any transaction.
   * 
   * @param tree
   * @param nodeID
   * @param nodeType
   * @param status
   * @param name
   * @param parent
   * @throws SQLException
   */
  public TreeNode newTreeNode
    (Tree     tree,
     String   nodeID,
     String   nodeType,
     Status   status,
     String   name,
     TreeNode parent)
  throws
    SQLException
  {
    PreparedStatement pStmt = null;
    try {
      pStmt = this.conn.prepareStatement(insertTreeNodeSQL);
      pStmt.setString(1, tree.getId());
      pStmt.setString(2, nodeID);
      pStmt.setString(3, nodeType);
      pStmt.setString(4, status.toString());
      pStmt.setString(5, name);
      pStmt.setDate(6, new Date(Calendar.getInstance().getTimeInMillis()));
      pStmt.executeUpdate();
    }
    finally {
      if (pStmt != null) {
        pStmt.close();
      }
    }

    TreeNode newNode = new NodeImpl(tree, nodeID, nodeType, status, name);
    
    if (parent == null)
    {
      tree.addRoot(newNode);
    }
    else
    {
      newTreeNodeRelationship(newNode, parent);
    }
    
    this.treeNodesAdded++;
    return newNode;
  }
  
  
  /**
   * Creates a new TreeNodeRelationship record, and stores that value in the
   * database.
   * This method updates the database, but does not commit any transaction.
   * 
   * @param tree
   * @param nodeID
   * @param nodeType
   * @param status
   * @param name
   * @param parent
   * @throws SQLException
   */
  private void newTreeNodeRelationship
    (TreeNode child,
     TreeNode parent)
  throws
    SQLException
  {
    PreparedStatement pStmt = null;
    try {
      pStmt = this.conn.prepareStatement(insertTreeNodeRelationshipSQL);
      pStmt.setString(1, child.getTree().getId());
      pStmt.setString(2, child.getId());
      pStmt.setString(3, parent.getId());
      pStmt.executeUpdate();
    }
    finally {
      if (pStmt != null) {
        pStmt.close();
      }
    }


    parent.addChild(child);
    
    this.treeNodeRelationshipsAdded++;
  }

  
  public boolean removeTrees(boolean isQuiet)
  {
	  boolean status = isQuiet;
	  if ( !isQuiet)
		  status = readYesOrNo("\nYou are about to delete and replace all trees.\nDo you wish to continue (Y/N)? ");

	  if (status)
	  {
	      try { deleteAll(); }
	      catch (SQLException sqle) {
	         System.out.println("-Error: unable to delete trees");
	         System.out.println(sqle.getMessage());
	         status = false;
	      }
	  }
	  return (status);
   }


  /**
   * Deletes all Tree data and associated TreeNode and TreeNodeRelationship
   * data.
   * This method updates the database, but does not commit any transaction.
   * 
   * @throws SQLException
   */
  private void deleteAll()
  throws SQLException
  {
    try
    {
//      conn.setAutoCommit(true);
      for (int i = 0; i < this.deletionTableNames.length; i++)
      {
        executeDeletion(conn, this.deletionTableNames[i]);
      }
    }
    catch (SQLException ex)
    {
      conn.rollback();
      System.out.println("SQL error occurred: " + ex.getMessage());
    }
  }
  
  private void executeDeletion(Connection conn, String tableName)
  throws SQLException
  {
    PreparedStatement ps = null;
    try {
      ps = conn.prepareStatement("delete from " + tableName);
      int rows = ps.executeUpdate();
      System.out.println
        (rows
         + (rows == 1 ? " row " : " rows ")
         + "deleted from table "
         + tableName);
      commit();
    }
    finally {
      if (ps != null) {
        ps.close();
      }
    }
  }

	public void processFile(String[] filenames, JComponent parent)
	{
		if (null == filenames)
			return;

		try
		{
			for (int i = 0; i < filenames.length; i++)
			{
				processFile(filenames[i], parent);
				commit();
			}
			conn.close();
		}
		catch (SQLException e) { e.printStackTrace(); }
	}


	public void processFile(String filename, JComponent parent)
	{
		if ((null == filename) || (0 >= filename.length()))
			return;
		System.out.println("Processing file \"" + filename + "\"");

		this.parent = parent;

		try
		{
			BufferedReader in = new BufferedReader(new FileReader(filename));
			processFile(in);
			in.close();
		}
		catch (FileNotFoundException e) { e.printStackTrace(); }
		catch (XMLStreamException e) { e.printStackTrace(); }
		catch (SQLException e) { e.printStackTrace(); }
		catch (IOException e) { e.printStackTrace(); }
	}

  
  private void processFile(BufferedReader in) throws XMLStreamException, SQLException
  {
    System.out.println("Inserting new Tree...");
    
    System.setProperty
      ("javax.xml.stream.XMLInputFactory",
       "com.ctc.wstx.stax.WstxInputFactory");
    XMLInputFactory factory = XMLInputFactory.newInstance();
    ((com.ctc.wstx.stax.WstxInputFactory)factory).configureForMaxConvenience();
    XMLStreamReader parser = factory.createXMLStreamReader(in);
    
    //  Get current time
    long start = System.currentTimeMillis();
      
    while (true)
    {
      int event = parser.next();
      if (event == XMLStreamConstants.END_DOCUMENT)
      {
         parser.close();
         break;
      }
        
      if (event == XMLStreamConstants.START_ELEMENT)
      {
        if (parser.getLocalName().equals(ELEMENTNAME_TREE))
        {
          processSignetTree(parser);
        }
        else
        {
          Set expectedElementSet = new HashSet();
          expectedElementSet.add(ELEMENTNAME_TREE);
          reportUnexpectedElement(parser, expectedElementSet);
        }
      }
    }
    
    //  Get elapsed time in milliseconds
    long elapsedTimeMillis = System.currentTimeMillis() - start;
    
    // Get elapsed time in seconds
    float elapsedTimeSec = elapsedTimeMillis / 1000F;
    float nodesPerSecond = treeNodesAdded / elapsedTimeSec;
    
    System.out.println
      ("Loaded "
       + this.treeNodesAdded
       + " TreeNodes in "
       + elapsedTimeSec
       + " seconds ("
       + nodesPerSecond
       + " nodes per second).");
  }
  
  private Tree processSignetTree(XMLStreamReader  parser)
  			throws XMLStreamException, SQLException
  {
    String treeId   = null;
    String treeName = null;
    Tree   tree     = null;
    
    while (true)
    {
      int event = parser.next();
      
      switch (event)
      {
        case XMLStreamConstants.CHARACTERS:
          // We don't care about this.
          break;
        
        case XMLStreamConstants.END_ELEMENT:
          if (parser.getLocalName().equals(ELEMENTNAME_TREE))
          {
            // We've finished processing the "Tree" element.
            // If we failed to create a Tree, then that's an error.
            if (tree == null)
            {
              reportIncompleteTree(treeId, treeName);
            }
            else
            {
              return tree;
            }
          }
          else
          {
            reportUnexpectedEndElement
              (parser, parser.getLocalName(), ELEMENTNAME_TREE);
          }
            
          break;
          
        case XMLStreamConstants.START_ELEMENT:
          String localName = parser.getLocalName();
          if (localName.equals(ELEMENTNAME_ID))
          {
            if (treeId != null)
            {
              reportRepeatedElement(parser);
            }
            else
            {
              treeId = processId(parser);
            }
          }
          else if (localName.equals(ELEMENTNAME_NAME))
          {
            if (treeName != null)
            {
              reportRepeatedElement(parser);
            }
            else
            {
              treeName = processName(parser);
            }
          }
          else if (localName.equals(ELEMENTNAME_ORGANIZATION))
          {
            processOrganization(parser, tree, null);
          }
          
          break;
          
        default:
            System.out.println("FOUND NEW EVENT: " + event);
      }
      
      if (tree == null)
      {
        tree = buildTreeIfComplete(treeId, treeName);
      }
    }
  }
  
  private TreeNode processOrganization(XMLStreamReader parser, Tree tree, TreeNode parent)
  		throws XMLStreamException, SQLException
  {
    String id   = null;
    String type = null;
    String name = null;
    TreeNode treeNode = null;
    
    if (tree == null)
    {
      System.out.println
        ("A '"
         + ELEMENTNAME_ORGANIZATION
         + "' element was encountered before its enclosing '"
         + ELEMENTNAME_TREE
         + "' element was successfully created. This is an error.");
    }
    else
    {
      while (true)
      {
        int event = parser.next();
        
        switch (event)
        {
          case XMLStreamConstants.CHARACTERS:
            // We don't care about this.
            break;
          
          case XMLStreamConstants.END_ELEMENT:
            if (parser.getLocalName().equals(ELEMENTNAME_ORGANIZATION))
            {
              // We've finished processing the "Organization" element.
              // If we failed to create a TreeNode, then that's an error.
              if (treeNode == null)
              {
                reportIncompleteTreeNode(id, type, name);
              }
              else
              {
                return treeNode;
              }
            }
            else
            {
              reportUnexpectedEndElement
                (parser, parser.getLocalName(), ELEMENTNAME_ORGANIZATION);
            }
              
            break;
            
          case XMLStreamConstants.START_ELEMENT:
            String localName = parser.getLocalName();
            if (localName.equals(ELEMENTNAME_ID))
            {
              if (id != null)
              {
                reportRepeatedElement(parser);
              }
              else
              {
                id = processId(parser);
              }
            }
            if (localName.equals(ELEMENTNAME_TYPE))
            {
              if (type != null)
              {
                reportRepeatedElement(parser);
              }
              else
              {
                type = processType(parser);
              }
            }
            else if (localName.equals(ELEMENTNAME_NAME))
            {
              if (name != null)
              {
                reportRepeatedElement(parser);
              }
              else
              {
                name = processName(parser);
              }
            }
            else if (localName.equals(ELEMENTNAME_ORGANIZATION))
            {
              processOrganization(parser, tree, treeNode);
            }
            
            break;
            
          default:
              System.out.println("FOUND NEW EVENT: " + event);
        }
        
        if (treeNode == null)
        {
          treeNode = buildTreeNodeIfComplete(tree, parent, id, type, name);
        }
      }
      
    }
    
    return treeNode;
  }
  

  private String processId(XMLStreamReader parser)
  throws XMLStreamException
  {
    return parser.getElementText();
  }
  

  private String processName(XMLStreamReader parser)
  throws XMLStreamException
  {
    return parser.getElementText();
  }
  

  private String processType(XMLStreamReader parser)
  throws XMLStreamException
  {
    return parser.getElementText();
  }
  

  private Tree buildTreeIfComplete(String id, String name)
  			throws SQLException
  {
    Tree tree = null;
    
    if ((id != null) && (name != null))
    {
      tree = newTree(id, name, ADAPTERCLASSNAME);
    }
    
    return tree;
  }
  

  private TreeNode buildTreeNodeIfComplete(Tree tree, TreeNode parent,
		  				String id, String type, String name)
  		throws SQLException
  {
    TreeNode treeNode = null;
    
    if ((id != null) && (type != null) && (name != null))
    {
      treeNode = newTreeNode(tree, id, type, Status.ACTIVE, name, parent);
    }
    
    return treeNode;
  }
  
 
 private void reportIncompleteTree
    (String treeId,
     String treeName)
  {
    if (treeId == null)
    {
      System.out.println
        ("The XML input file contained an incomplete '"
         + ELEMENTNAME_TREE
         + "' definition. The required element '"
         + ELEMENTNAME_ID
         + "' was missing. This is an error.");
    }
    
    if (treeName == null)
    {
      System.out.println
        ("The XML input file contained an incomplete '"
         + ELEMENTNAME_TREE
         + "' definition. The required element '"
         + ELEMENTNAME_NAME
         + "' was missing. This is an error.");
    }
    
    if ((treeId != null) && (treeName != null))
    {
      throw new RuntimeException
        ("An incomplete '"
         + ELEMENTNAME_TREE
         + "' definition was reported, but its '"
         + ELEMENTNAME_ID
         + "' and '"
         + ELEMENTNAME_NAME
         + "' elements were both defined. This is an unexpected program "
         + "condition.");
    }
  }
  

  private void reportIncompleteTreeNode
    (String id,
     String type,
     String name)
  {
    if (id == null)
    {
      System.out.println
        ("The XML input file contained an incomplete '"
         + ELEMENTNAME_ORGANIZATION
         + "' definition. The required element '"
         + ELEMENTNAME_ID
         + "' was missing. This is an error.");
    }
    
    if (type == null)
    {
      System.out.println
        ("The XML input file contained an incomplete '"
         + ELEMENTNAME_ORGANIZATION
         + "' definition. The required element '"
         + ELEMENTNAME_TYPE
         + "' was missing. This is an error.");
    }
    
    if (name == null)
    {
      System.out.println
        ("The XML input file contained an incomplete '"
         + ELEMENTNAME_ORGANIZATION
         + "' definition. The required element '"
         + ELEMENTNAME_NAME
         + "' was missing. This is an error.");
    }
    
    if ((id != null) && (type != null) && (name != null))
    {
      throw new RuntimeException
        ("An incomplete '"
         + ELEMENTNAME_ORGANIZATION
         + "' definition was reported, but its '"
         + ELEMENTNAME_ID
         + "' and '"
         + ELEMENTNAME_TYPE
         + "' and '"
         + ELEMENTNAME_NAME
         + "' elements were all defined. This is an unexpected program "
         + "condition.");
    }
  }
  

  private void reportRepeatedElement
    (XMLStreamReader parser)
  {
    System.out.println
      ("XML parser encountered unexpected element '"
       + parser.getLocalName()
       + "' at line "
       + parser.getLocation().getLineNumber()
       + ", column "
       + parser.getLocation().getColumnNumber()
       + ". This element is illegally repeated: It is allowed to appear only "
       + "once within its enclosing element, and it has already appeared "
       + "within the current enclosing element.");
  }
  
  private void reportUnexpectedEndElement
    (XMLStreamReader parser,
     String unexpectedName,
     String expectedName)
  {
    System.out.println
      ("XML parser encountered unexpected end-element '"
       + unexpectedName
       + "' at line "
       + parser.getLocation().getLineNumber()
       + ", column "
       + parser.getLocation().getColumnNumber()
       + ". Only an end-element '"
       + expectedName
       + "' is allowed at this point in the file.");
  }
  

  private void reportUnexpectedElement
    (XMLStreamReader parser,
     Set             expectedElementNames)
  {
    System.out.println
      ("XML parser encountered unexpected element '"
       + parser.getLocalName()
       + "' at line "
       + parser.getLocation().getLineNumber()
       + ", column "
       + parser.getLocation().getColumnNumber()
       + ". These are the element-names which are expected at this point: "
       + commaSeparatedList(expectedElementNames));
  }
  

  private String commaSeparatedList(Set strings)
  {
    StringBuffer output = new StringBuffer();
    Iterator iterator = strings.iterator();
    while (iterator.hasNext())
    {
      if (output.length() > 0)
      {
        output.append(", ");
      }
      
      output.append((String)(iterator.next()));
    }
    
    return output.toString();
  }
  
   
  private boolean readYesOrNo(String prompt) {
      while (true) {
          String response = promptedReadLine(prompt);
          if (response.length() > 0) {
              switch (Character.toLowerCase(response.charAt(0))) {
              case 'y':
                  return true;
              case 'n':
                  return false;
              default:
                  System.out.println("Please enter Y or N. ");
              }
          }
      }
  }
  

  /**
   * Commits the current database transaction in use by the TreeXmlLoader.
   * 
   * @throws SQLException
   */
  public void commit() throws SQLException
  {
    this.conn.commit();
  }
  

	/**
	 * HypersonicSQL needs a 'shutdown' command in order for commits to actually occur
	 */
	public void shutdownDB()
	{
		try
		{
			DatabaseMetaData md = conn.getMetaData();
			if (md.getDriverName().indexOf("HSQL") != -1) // if it's HypersonicSQL
			{
				PreparedStatement pStmt = null;
				pStmt = conn.prepareStatement("SHUTDOWN");
				pStmt.executeUpdate();
				pStmt.close();
			}
		}
		catch (SQLException se)
		{
			throw new RuntimeException(se);
		}
	}


	private String promptedReadLine(String prompt)
	{
		String retval = "";

		if (null == parent)
		{
			try
			{
				System.out.print(prompt);
				BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
				retval = reader.readLine();
			}
			catch (java.io.IOException e) { /* don't care */ }
		}
		else
		{
			int response = JOptionPane.showConfirmDialog(parent, prompt);
			switch (response)
			{
				case (JOptionPane.YES_OPTION):
					retval = "y";
					break;
				default:
					retval = "n";
					break;
			}
		}

		return (retval);
	}


	////////////////////////////////////////////
	// Nested classes
	////////////////////////////////////////////

  private class TreeImpl implements Tree
  {
    private String id;
    private String name;
    private String adapterClassName;
    private Set    roots;
    
    protected TreeImpl
      (String id,
       String name,
       String adapterClassName)
    {
      this.id = id;
      this.name = name;
      this.adapterClassName = adapterClassName;
      
      this.roots = new HashSet();
    }
    
    public void addRoot(TreeNode rootNode)
    {
      this.roots.add(rootNode);
    }
    
    public TreeAdapter getAdapter()
    {
      throw new UnsupportedOperationException
        ("This implementation of the Tree interface has '"
         + this.adapterClassName
         + "' as its adapterClassName, but does not yet support"
         + " instantiation of that adapter.");
    }
    
    public String getId()
    {
      return this.id;
    }
    
    public String getName()
    {
      return this.name;
    }
    
    public TreeNode getNode(String nodeId)
    {
      throw new UnsupportedOperationException();
    }
    
    public Set getRoots()
    {
      return UnmodifiableSet.decorate(this.roots);
    }
    
    public Set getTreeNodes()
    {
      throw new UnsupportedOperationException();
    }
}
  
  private class NodeImpl implements TreeNode
  {
    private Tree    tree;
    private String  id;
    private String  type;
    private Status  status;
    private String  name;
    
    private Set     parents;
    private Set     children;
    
    NodeImpl
      (Tree   tree,
       String id,
       String type,
       Status status,
       String name)
    {
      this.tree = tree;
      this.id = id;
      this.type = type;
      this.status = status;
      this.name = name;
      
      this.children = new HashSet();
      this.parents = new HashSet();
    }
    
    public Tree getTree()
    {
      return this.tree;
    }
    
    public String getId()
    {
      return this.id;
    }
    
    public String getType()
    {
      return this.type;
    }
    
    public Status getStatus()
    {
      return this.status;
    }
    
    public String getName()
    {
      return this.name;
    }

    public Set getParents()
    {
      return UnmodifiableSet.decorate(this.parents);
    }

    public Set getChildren()
    {
      return UnmodifiableSet.decorate(this.children);
    }

    public void addChild(TreeNode treeNode)
    {
      this.children.add(treeNode);
    }
    
    public boolean isAncestorOf(TreeNode treeNode)
    {
      throw new UnsupportedOperationException();
    }

    public boolean isAncestorOfAll(Set treeNodes)
    {
      throw new UnsupportedOperationException();
    }

    public boolean isDescendantOf(TreeNode treeNode)
    {
      throw new UnsupportedOperationException();
    }

    public boolean isDescendantOfAny(Set treeNodes)
    {
      throw new UnsupportedOperationException();
    }

    public int compareTo(Object o)
    {
      TreeNode otherNode = (TreeNode)o;
      return this.id.compareTo(otherNode.getId());
    }
    
  /**
   * Returns a String of the form {treeAdapterClassName}:{treeId}:{nodeId}
   * This is used by UI code to determine which node from the Select Scope tree
   * was selected.
   */
	public String getScopePath()
	{
		StringBuffer buf = new StringBuffer();

		Tree tree = getTree(); // just in case it's not pre-fetched
		buf.append(tree.getAdapter().getClass().getName());
		buf.append(SignetFactory.SCOPE_PART_DELIMITER);
		buf.append(tree.getId());
		buf.append(SignetFactory.SCOPE_PART_DELIMITER);
		buf.append(getId());

		return (buf.toString());
	}


    public int hashCode()
    {
      return this.id.hashCode();
    }
    
    public boolean equals(Object obj)
    {
      TreeNode otherNode = (TreeNode)obj;
      return this.id.equals(otherNode.getId());
    }
  }


  ///////////////////////////////////////
  // Statics
  ///////////////////////////////////////

	/**
	 * Main
	 * @param args
	 */
	public static void main(String[] args)
	{
		TreeXmlLoader loader = new TreeXmlLoader();

		String[] fileargs = parseArgs(args);
		if (1 > fileargs.length)
		{
			System.out.println("Signet TreeXmlLoader, $Revision: 1.13 $");
			System.out.println("Usage:\n\tTreeXmlLoader [-q] <inputfile> [inputfile] ...");
			System.out.println("\t\t-q : Quiet, do not prompt on overwrite");
			System.out.println("\t\tinputfile : a file containing Signet Tree data");
			System.exit(1);
		}

		if ( !loader.removeTrees(isQuiet(args)))
			return;
    
		loader.processFile(fileargs, null);
//		loader.shutdownDB();
	}


	protected static String[] parseArgs(String[] args)
	{
		Vector retval = new Vector();

		if ((null != args) && (0 < args.length))
			for (int i = 0; i < args.length; i++)
				if ( !isQuietArg(args[i]))
					retval.add(args[i]);

		String[] retArray = (String[])retval.toArray(new String[retval.size()]);
		return (retArray);
	}


	protected static boolean isQuiet(String[] args)
	{
		boolean retval = false; // assume failure

		for (int i = 0; (i < args.length) && !retval; i++)
			retval = isQuietArg(args[i]);

		return (retval);
	}

	protected static boolean isQuietArg(String arg)
	{
		return (arg.equalsIgnoreCase("-q"));
	}

}