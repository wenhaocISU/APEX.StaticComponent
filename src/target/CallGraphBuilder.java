package target;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import apex.staticFamily.StaticApp;
import apex.staticFamily.StaticClass;
import apex.staticFamily.StaticMethod;
import apex.staticFamily.StaticStmt;
import apex.symbolic.SymbolicExecutionBlacklist;

public class CallGraphBuilder {

	public StaticApp staticApp;
	public DirectedGraph<StaticMethod, DefaultEdge> callGraph;
	ConnectivityInspector<StaticMethod, DefaultEdge> inspector;
	
	public CallGraphBuilder(StaticApp staticApp)
	{
		this.staticApp = staticApp;
		this.generateCallGraph();
	}
	
	public List<StaticMethod> getSources()
	{
		List<StaticMethod> result = new ArrayList<StaticMethod>();
		for (StaticMethod m : callGraph.vertexSet())
		{
			if (callGraph.inDegreeOf(m) == 0)
				result.add(m);
		}
		return result;
	}
	
	public List<StaticMethod> getSources(StaticMethod m)
	{
		List<StaticMethod> candidates = new ArrayList<StaticMethod>();
		if (inspector == null)
			inspector = new ConnectivityInspector<StaticMethod, DefaultEdge>(callGraph);
		
		// Connectivity Inspector
		Set<StaticMethod> set = inspector.connectedSetOf(m);
		
		for (StaticMethod mm : set)
		{
			
			// Dijstra shortest path
			List<DefaultEdge> path = DijkstraShortestPath.findPathBetween(callGraph, mm, m);
			
			// if path exists, and source node has no incoming edges, it's a candidate
			if (path != null && !path.isEmpty())
			{
				if (callGraph.inDegreeOf(mm) == 0)
				{
					candidates.add(mm);
				}
			}
		}
		return candidates;
	}
	
	public void generateCallGraph()
	{
		if (callGraph != null)
			return;
		callGraph = new DefaultDirectedGraph<StaticMethod, DefaultEdge>(DefaultEdge.class);
		addVertices();
		addEdges();
	}
	
	private void addVertices()
	{
		for (StaticClass c : staticApp.getClasses())
		{
			if (SymbolicExecutionBlacklist.classInBlackList(c.getDexName()))
				continue;
			//System.out.println("adding vertices from class " + c.getDexName());
			for (StaticMethod m : c.getMethods())
			{
				callGraph.addVertex(m);
			}
		}
	}
	
	private void addEdges()
	{
		for (StaticMethod m : callGraph.vertexSet())
		{
			for (StaticStmt s : m.getStatements())
			{
				if (!s.isInvokeStmt())
					continue;
				StaticMethod targetM = staticApp.getMethod(s.getInvokeSignature());
				if (targetM != null && callGraph.vertexSet().contains(targetM))
				{
					callGraph.addEdge(m, targetM);
				}
			}
		}
	}
	

	
}
